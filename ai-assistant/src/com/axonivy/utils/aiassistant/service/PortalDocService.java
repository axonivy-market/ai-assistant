package com.axonivy.utils.aiassistant.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.axonivy.utils.aiassistant.constant.AiConstants;
import com.axonivy.utils.aiassistant.core.AbstractAIBot;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

public class PortalDocService {
  private static final String IVY_DOC_HOST = "https://market.axonivy.com/market-cache/portal/portal-guide/12.0.0-m266/doc/";

  private static final String HEADER_PREFIX = "# ";
  private static final String SUB_HEADER_PREFIX = "## ";
  private static final String SMALL_SUB_HEADER_PREFIX = "#### ";
  private static final String LIST_ITEM_PREFIX = "- ";

  private static final String TWO_LEVELS_PATH_PREFIX = "../../";
  private static final String LINK_FORMAT = "[%s](%s)";
  private static final String IMAGE_FORMAT = "!" + LINK_FORMAT;
  private static final String CODE_BLOCK = "```";

  public static void createTextIndex(AbstractAIBot bot, String index,
      List<String> contents) throws IOException {
    if (CollectionUtils.isEmpty(contents)) {
      return;
    }

    List<TextSegment> data = new ArrayList<>();
    contents.forEach(content -> data.add(TextSegment.from(content)));

    bot.embed(generateVectorStoreName(index), data);
  }

  public static void createPortalIndex(AbstractAIBot bot, String index,
      List<String> contents) throws IOException {
    if (CollectionUtils.isEmpty(contents)) {
      return;
    }

    List<TextSegment> data = new ArrayList<>();
    contents.forEach(content -> data.addAll(proceedEachContent(content)));

    bot.embed(generateVectorStoreName(index), data);
  }

  private static String generateVectorStoreName(String index) {
    return String.join("-", AiConstants.DEFAULT_AXON_IVY_VECTOR_STORE_PREFIX,
        index);
  }

  private static List<TextSegment> proceedEachContent(String content) {

    List<String> lines = Arrays.asList(content.split(StringUtils.LF));
    String headerKeyword = StringUtils.EMPTY;
    String keyword = StringUtils.EMPTY;
    List<String> blockText = new ArrayList<>();
    List<TextSegment> result = new ArrayList<>();

    for (String line : lines) {
      line = line.stripTrailing();
      if (StringUtils.isBlank(line)) {
        continue;
      }

      if (line.startsWith(HEADER_PREFIX)) {
        headerKeyword = line.strip();
        keyword = headerKeyword;
        handleBlockText(headerKeyword, keyword, blockText, result);
        continue;
      }

      if (line.startsWith(SUB_HEADER_PREFIX)) {
        handleBlockText(headerKeyword, keyword, blockText, result);
        keyword = line.strip();
        continue;
      }

      // If a line does not contain keywords, append them to the block text
      blockText.add(line);
    }

    if (!blockText.isEmpty()) {
      handleBlockText(headerKeyword, keyword, blockText, result);
    }

    return result;
  }

  private static void handleBlockText(String headerKeyword, String keyword,
      List<String> blockText, List<TextSegment> parsedDocuments) {
    if (CollectionUtils.isNotEmpty(blockText)) {
      Metadata meta = Metadata.from("keywords",
          String.join(" ", Arrays.asList(headerKeyword, keyword)));
      String keywords = String.format("Keywords: %s, %s",
          headerKeyword, keyword).concat(System.lineSeparator());

      List<String> blockTextWithHeader = new ArrayList<>();
      blockTextWithHeader.add(keywords);
      blockTextWithHeader.addAll(blockText);

      TextSegment parsed = new TextSegment(
          String.join(StringUtils.LF, blockTextWithHeader), meta);

      parsedDocuments.add(parsed);
      blockText.clear();
    }
  }

  public static String convertPortalDocument(String raw) {
    Document document = Jsoup.parse(raw);

    // Remove specific elements by tag name
    removeElements(document, "head");
    removeElements(document, "div[role=search]");
    removeElements(document, "ul.current");
    removeElements(document, "footer");
    removeElements(document, "ul.wy-breadcrumbs");
    removeElements(document, "div.wy-side-nav-search");
    removeElements(document, "p.caption[role=heading]");
    removeElements(document, "nav.wy-nav-top");
    removeElements(document, "nav.wy-nav-side");
    removeElements(document, "ul.wy-breadcrumbs");
    removeElements(document, "script");
    removeElements(document, "link[rel=stylesheet]");
    removeElements(document, "hr");
    removeElements(document, "div.toctree-wrapper");
    removeElements(document, "a.headerlink");
    removeElements(document, "div.toctree-wrapper");

    // Set h1 tags as header will be used as metadata keyword
    for (Element h1Tag : document.select("h1")) {
      h1Tag.text(HEADER_PREFIX + h1Tag.text());
    }

    // Add a prefix "Information about" and remove special characters for <h2>
    // tags
    for (Element h2Tag : document.select("h2")) {
      h2Tag.text(
          SUB_HEADER_PREFIX + h2Tag.text().replace("ïƒ�", StringUtils.EMPTY));
    }

    // Replace image tags with their source links
    for (Element imgTag : document.select("img")) {
      String alt = StringUtils.defaultIfBlank(imgTag.attr("alt"),
          imgTag.attr("title"));
      if (StringUtils.isBlank(alt)) {
        alt = Optional.ofNullable(imgTag.select("span.std-ref"))
            .map(Elements::first).map(Element::text).orElse("");
      }

      String srcLink = imgTag.attr("src").replace(TWO_LEVELS_PATH_PREFIX,
          IVY_DOC_HOST);
      if (srcLink.startsWith("screenshots/")) {
        var srcLinkParts = Arrays.asList(srcLink.split("/"));
        srcLink = IVY_DOC_HOST + "_images/"
            + srcLinkParts.get(srcLinkParts.size() - 1);
      }

      imgTag
          .replaceWith(new TextNode(String.format(IMAGE_FORMAT, alt, srcLink)));
    }

    // Transform <a> tags to markdown link format
    for (Element anchorTag : document.select("a")) {
      String hrefLink = anchorTag.attr("href");
      if (hrefLink.startsWith("#")) {
        anchorTag.remove();
      } else {
        hrefLink = hrefLink.replace(TWO_LEVELS_PATH_PREFIX, IVY_DOC_HOST);
        if (hrefLink.endsWith("svg")) {
          // If a link is a svg file, remove it
          anchorTag.remove();
        } else {
          String alt = StringUtils.defaultIfBlank(anchorTag.attr("alt"), anchorTag.attr("title"));
          if (StringUtils.isBlank(alt)) {
            alt = Optional.ofNullable(anchorTag.select("span.std-ref"))
                .map(Elements::first).map(Element::text).orElse("");
          }
          anchorTag.replaceWith(new TextNode(
              String.format(LINK_FORMAT, alt, hrefLink)));
        }
      }
    }

    // Transform notes title to sub header
    for (Element noteTag : document.select("div.admonition")) {
      Element title = noteTag.select(".admonition-title").first();
      title.replaceWith(new TextNode(
          StringUtils.LF + SMALL_SUB_HEADER_PREFIX + title.text()
              + StringUtils.LF));
    }

    // Transform tables into readable format
    for (Element tableTag : document.select("table")) {
        tableTag.replaceWith(Jsoup.parse(convertTableToMarkdown(tableTag)));
    }

    // Transformed code block to markdown format
    for (Element codeBlockDiv : document.select("div.notranslate")) {
      codeBlockDiv.before(CODE_BLOCK);
      codeBlockDiv.after(CODE_BLOCK);
    }

    // Replace '\n' inside <p> tags with a single space
    for (Element paragraphTag : document.select("p")) {
      String converted = paragraphTag.text().replace(StringUtils.LF,
          StringUtils.SPACE);
      paragraphTag.replaceWith(new TextNode(converted));
    }

    // Add '- ' before <li> tags
    for (Element listTag : document.select("li")) {
      listTag.replaceWith(new TextNode(LIST_ITEM_PREFIX + listTag.text()));
    }

    // Add a new line after each tag for better readability
    for (Element tag : document.getAllElements()) {
      if (!Optional.ofNullable(tag).map(Element::parents).isPresent()) {
        return StringUtils.EMPTY;
      }
      if (!Optional.ofNullable(tag).map(Element::parents)
          .map(elem -> elem.select(".notranslate"))
          .orElseGet(() -> new Elements()).isEmpty()) {
        tag.after(StringUtils.LF);
      }
    }

    // Replace "|ivy|" with "Axon Ivy"
    String htmlStr = document.wholeText().replace("|ivy|", "Axon Ivy");

    // Remove all blank lines
    List<String> lines = Arrays.asList(htmlStr.split(StringUtils.LF)).stream()
        .map(String::stripTrailing).filter(StringUtils::isNotBlank).toList();
    return String.join(StringUtils.LF, lines);
  }

  private static void removeElements(Document document, String cssQuery) {
    Elements elements = document.select(cssQuery);
    for (Element element : elements) {
      element.remove();
    }
  }

  private static String convertTableToMarkdown(Element table) {
    StringBuilder markdown = new StringBuilder();

    // Convert table headers
    Elements headers = table.select("thead tr th");
    if (!headers.isEmpty()) {
      for (Element header : headers) {
        markdown.append("| ").append(header.text()).append(StringUtils.SPACE);
      }
      markdown.append("|").append(StringUtils.LF);

      // Add separator line for headers
      headers.forEach(header -> markdown.append("|---"));
      markdown.append("|").append(StringUtils.LF);
    }

    // Convert table rows
    Elements rows = table.select("tbody tr");
    for (Element row : rows) {
      Elements cells = row.select("td");
      for (Element cell : cells) {
        markdown.append("| ").append(cell.text()).append(StringUtils.SPACE);
      }
      markdown.append("|").append(StringUtils.LF);
    }

    return markdown.toString();
  }

}