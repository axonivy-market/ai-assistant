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

import com.axonivy.utils.aiassistant.core.AbstractAIBot;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;

public class PortalDocService {
  private static final String IVY_DOC_HOST = "https://market.axonivy.com/portal/11.3/doc/";
  public static final String USER_GUIDE_DIR = "portal-user-guide";
  public static final String DEVELOPER_GUIDE_DIR = "portal-developer-guide";
  public static final String PORTAL_COMPONENT_GUIDE_DIR = "portal-components";

  private static final String HEADER_PREFIX = "__header: ";
  private static final String SUB_HEADER_PREFIX = "__sub_header: ";

  public static void createTextIndex(AbstractAIBot bot, String index,
      List<String> contents) throws IOException {
    if (CollectionUtils.isEmpty(contents)) {
      return;
    }

    List<TextSegment> data = new ArrayList<>();
    contents.forEach(content -> data.add(TextSegment.from(content)));

    bot.embed(index, data);
  }

  public static void createPortalIndex(AbstractAIBot bot, String index,
      List<String> contents) throws IOException {
    if (CollectionUtils.isEmpty(contents)) {
      return;
    }

    List<TextSegment> data = new ArrayList<>();
    contents.forEach(content -> data.addAll(proceedEachContent(content)));

    bot.embed(index, data);
  }

  private static List<TextSegment> proceedEachContent(String content) {

    List<String> lines = Arrays.asList(content.split("\n"));
    String headerKeyword = "";
    String keyword = "";
    List<String> blockText = new ArrayList<>();
    List<TextSegment> result = new ArrayList<>();

    for (String line : lines) {
      line = line.stripTrailing();
      if (StringUtils.isBlank(line)) {
        continue;
      }

      if (line.startsWith(HEADER_PREFIX)) {
        headerKeyword = line.replace(HEADER_PREFIX, "").strip();
        handleBlockText(headerKeyword, keyword, blockText, result);
        continue;
      }

      if (line.startsWith(SUB_HEADER_PREFIX)) {
        keyword = line.replace(SUB_HEADER_PREFIX, "").strip();
        handleBlockText(headerKeyword, keyword, blockText, result);
        continue;
      }

      // If a line does not contain keywords, append them to the block text
      blockText.add(line);
    }
    return result;
  }

  private static void handleBlockText(String headerKeyword, String keyword,
      List<String> blockText, List<TextSegment> parsedDocuments) {
    if (CollectionUtils.isNotEmpty(blockText)) {
      Metadata meta = Metadata.from("keywords",
          String.join(" ", Arrays.asList(headerKeyword, keyword)));
      String keywords = String.join(": ",
          Arrays.asList(headerKeyword, keyword));

      List<String> blockTextWithHeader = new ArrayList<>();
      blockTextWithHeader.add(keywords);
      blockTextWithHeader.addAll(blockText);

      TextSegment parsed = new TextSegment(
          String.join("\n", blockTextWithHeader), meta);

      parsedDocuments.add(parsed);
      blockText.clear();
    }
  }

  public static String convertPortalDocument(String raw) {
    Document document = Jsoup.parse(raw);

    // Remove specific elements by tag name
    removeElements(document, "div[role=search]");
    removeElements(document, "ul.current");
    removeElements(document, "footer");
    removeElements(document, "ul.wy-breadcrumbs");
    removeElements(document, "div.wy-side-nav-search");
    removeElements(document, "p.caption[role=heading]");
    removeElements(document, "nav.wy-nav-top");
    removeElements(document, "ul.wy-breadcrumbs");
    removeElements(document, "script");
    removeElements(document, "link[rel=stylesheet]");
    removeElements(document, "hr");
    removeElements(document, "div.toctree-wrapper");
    removeElements(document, "a.headerlink");

    // Set h1 tags as header will be used as metadata keyword
    for (Element h1Tag : document.select("h1")) {
      h1Tag.text("__header: " + h1Tag.text());
    }

    // Add a prefix "Information about" and remove special characters for <h2>
    // tags
    for (Element h2Tag : document.select("h2")) {
      h2Tag.text("__sub_header: " + h2Tag.text().replace("ïƒ�", ""));
    }

    // Replace image tags with their source links
    for (Element imgTag : document.select("img")) {
      String srcLink = imgTag.attr("src").replace("../../", IVY_DOC_HOST);
      if (srcLink.startsWith("screenshots/")) {
        var srcLinkParts = Arrays.asList(srcLink.split("/"));
        srcLink = IVY_DOC_HOST + "_images/"
            + srcLinkParts.get(srcLinkParts.size() - 1);
      }
      imgTag.replaceWith(new TextNode("<image>" + srcLink + "</image>"));
    }

    // Transform <a> tags to "<link> url here </link>" format
    for (Element anchorTag : document.select("a")) {
      String hrefLink = anchorTag.attr("href");
      if (hrefLink.startsWith("#")) {
        anchorTag.remove();
      } else {
        hrefLink = hrefLink.replace("../../", IVY_DOC_HOST);
        if (hrefLink.endsWith("svg")) {
          // If a link is a svg file, remove it
          anchorTag.remove();
        } else {
          anchorTag.replaceWith(new TextNode("<link>" + hrefLink + "</link>"));
        }
      }
    }

    // Transform tables into readable format
    for (Element tableTag : document.select("table")) {
      Elements rows = tableTag.select("tr");

      Elements headers = rows.get(0).select("thead");
      if (headers.size() > 0 && rows.size() > 1) { // Ensure there are headers
                                                   // and at least one row of
                                                   // data

        StringBuilder tableBuilder = new StringBuilder("Table content:\n");

        for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) { // Skip the
                                                                     // header
                                                                     // row
          Elements dataCols = rows.get(rowIndex).select("td");
          tableBuilder.append("row ").append(rowIndex).append(":\n");

          for (int colIndex = 0; colIndex < headers.size()
              && colIndex < dataCols.size(); colIndex++) {
            tableBuilder.append(headers.get(colIndex).text()).append(": ")
                .append(dataCols.get(colIndex).text());

            if (colIndex < headers.size() - 1) {
              tableBuilder.append(", ");
            }
          }

          tableBuilder.append("\n");
        }

        tableTag.replaceWith(Jsoup.parse(tableBuilder.toString()));
      }

    }

    // Added "Code block: " tag for all code blocks
    for (Element codeBlockDiv : document.select("div.notranslate")) {
      codeBlockDiv.before("__code_block: ");
    }

    // Add a new line after each tag for better readability
    for (Element tag : document.getAllElements()) {
      if (!Optional.ofNullable(tag).map(Element::parents).isPresent()) {
        return "";
      }
      if (!Optional.ofNullable(tag).map(Element::parents)
          .map(elem -> elem.select(".notranslate"))
          .orElseGet(() -> new Elements()).isEmpty()) {
        tag.after("\n");
      }
    }

    // Replace "|ivy|" with "Axon Ivy"
    String htmlStr = document.wholeText().replace("|ivy|", "Axon Ivy");

    // Remove all blank lines
    List<String> lines = Arrays.asList(htmlStr.split("\n")).stream()
        .map(String::stripTrailing).filter(StringUtils::isNotBlank).toList();
    return String.join("\n", lines);
  }

  private static void removeElements(Document document, String cssQuery) {
    Elements elements = document.select(cssQuery);
    for (Element element : elements) {
      element.remove();
    }
  }
}