package com.axonivy.utils.aiassistant.core.embedding;

import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.util.Collections.singletonList;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.message.BasicHeader;
import org.opensearch.client.json.JsonData;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.InlineScript;
import org.opensearch.client.opensearch._types.Time;
import org.opensearch.client.opensearch._types.mapping.Property;
import org.opensearch.client.opensearch._types.mapping.TextProperty;
import org.opensearch.client.opensearch._types.mapping.TypeMapping;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch._types.query_dsl.ScriptScoreQuery;
import org.opensearch.client.opensearch._types.query_dsl.TermQuery;
import org.opensearch.client.opensearch.core.BulkRequest;
import org.opensearch.client.opensearch.core.BulkResponse;
import org.opensearch.client.opensearch.core.DeleteByQueryRequest;
import org.opensearch.client.opensearch.core.DeleteByQueryResponse;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.indices.DeleteIndexRequest;
import org.opensearch.client.opensearch.indices.DeleteIndexResponse;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.opensearch.client.transport.endpoints.BooleanResponse;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.opensearch.OpenSearchEmbeddingStore;
import dev.langchain4j.store.embedding.opensearch.OpenSearchRequestFailedException;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

public class IvyOpenSearchEmbeddingStore {
  private static final Logger log = LoggerFactory
      .getLogger(OpenSearchEmbeddingStore.class);

  private final String indexName;
  private final OpenSearchClient client;
  private final String serverUrl;

  private static final String INDEX_NOT_EXIST_ERROR = "Cannot find vector store [%s]";
  private static final String CANNOT_CONNECT_TO_OPEN_SEARCH = "Cannot connect to the OpenSearch server with URL %s";

  /**
   * Creates an instance of OpenSearchEmbeddingStore to connect with
   * OpenSearch clusters running locally and network reachable.
   *
   * @param serverUrl OpenSearch Server URL.
   * @param apiKey    OpenSearch API key (optional)
   * @param userName  OpenSearch username (optional)
   * @param password  OpenSearch password (optional)
   * @param indexName OpenSearch index name.
   */
  public IvyOpenSearchEmbeddingStore(String serverUrl,
                                  String apiKey,
                                  String userName,
                                  String password,
                                  String indexName) {
      HttpHost openSearchHost;
      try {
          openSearchHost = HttpHost.create(serverUrl);
      } catch (URISyntaxException se) {
          log.error("[I/O OpenSearch Exception]", se);
          throw new OpenSearchRequestFailedException(se.getMessage());
      }

      OpenSearchTransport transport = ApacheHttpClient5TransportBuilder
              .builder(openSearchHost)
              .setMapper(new JacksonJsonpMapper())
              .setHttpClientConfigCallback(httpClientBuilder -> {

                  if (!isNullOrBlank(apiKey)) {
                      httpClientBuilder.setDefaultHeaders(singletonList(
                              new BasicHeader("Authorization", "ApiKey " + apiKey)
                      ));
                  }

                  if (!isNullOrBlank(userName) && !isNullOrBlank(password)) {
                      BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                      credentialsProvider.setCredentials(new AuthScope(openSearchHost),
                              new UsernamePasswordCredentials(userName, password.toCharArray()));
                      httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                  }

                  httpClientBuilder.setConnectionManager(PoolingAsyncClientConnectionManagerBuilder.create().build());

                  return httpClientBuilder;
              })
              .build();

      this.serverUrl = serverUrl;
      this.client = new OpenSearchClient(transport);
      this.indexName = ensureNotNull(indexName, "indexName");
  }

  /**
   * Creates an instance of OpenSearchEmbeddingStore to connect with
   * OpenSearch clusters running as a fully managed service at AWS.
   *
   * @param serverUrl   OpenSearch Server URL.
   * @param serviceName The AWS signing service name, one of `es` (Amazon OpenSearch) or `aoss` (Amazon OpenSearch Serverless).
   * @param region      The AWS region for which requests will be signed. This should typically match the region in `serverUrl`.
   * @param options     The options to establish connection with the service. It must include which credentials should be used.
   * @param indexName   OpenSearch index name.
   */
  public IvyOpenSearchEmbeddingStore(String serverUrl,
                                  String serviceName,
                                  String region,
                                  AwsSdk2TransportOptions options,
                                  String indexName) {

      Region selectedRegion = Region.of(region);

      SdkHttpClient httpClient = ApacheHttpClient.builder().build();
      OpenSearchTransport transport = new AwsSdk2Transport(httpClient, serverUrl, serviceName, selectedRegion, options);

      this.serverUrl = serverUrl;
      this.client = new OpenSearchClient(transport);
      this.indexName = ensureNotNull(indexName, "indexName");
  }

  /**
   * Creates an instance of OpenSearchEmbeddingStore using provided OpenSearchClient
   *
   * @param openSearchClient OpenSearch client provided
   * @param indexName        OpenSearch index name.
   */
  public IvyOpenSearchEmbeddingStore(OpenSearchClient openSearchClient,
                                  String indexName) {

      this.serverUrl = StringUtils.EMPTY;
      this.client = ensureNotNull(openSearchClient, "openSearchClient");
      this.indexName = ensureNotNull(indexName, "indexName");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String serverUrl;
    private String apiKey;
    private String userName;
    private String password;
    private String serviceName;
    private String region;
    private AwsSdk2TransportOptions options;
    private String indexName = "default";
    private OpenSearchClient openSearchClient;

    public Builder serverUrl(String serverUrl) {
      this.serverUrl = serverUrl;
      return this;
    }

    public Builder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public Builder userName(String userName) {
      this.userName = userName;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder serviceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public Builder region(String region) {
      this.region = region;
      return this;
    }

    public Builder options(AwsSdk2TransportOptions options) {
      this.options = options;
      return this;
    }

    public Builder indexName(String indexName) {
      this.indexName = indexName;
      return this;
    }

    public Builder openSearchClient(OpenSearchClient openSearchClient) {
      this.openSearchClient = openSearchClient;
      return this;
    }

    public IvyOpenSearchEmbeddingStore build() {
      if (openSearchClient != null) {
        return new IvyOpenSearchEmbeddingStore(openSearchClient, indexName);
      }
      if (!isNullOrBlank(serviceName) && !isNullOrBlank(region)
          && options != null) {
        return new IvyOpenSearchEmbeddingStore(serverUrl, serviceName, region,
            options, indexName);
      }
      return new IvyOpenSearchEmbeddingStore(serverUrl, apiKey, userName,
          password,
          indexName);
    }

  }

  /**
   * This implementation uses the exact k-NN with scoring script to calculate
   * See https://opensearch.org/docs/latest/search-plugins/knn/knn-score-script/
   */

  public SearchResponse<EmbeddingDocument> findRelevantDocuments(
      Embedding referenceEmbedding, int maxResults, double minScore) {
    try {
      ScriptScoreQuery scriptScoreQuery = buildDefaultScriptScoreQuery(
          referenceEmbedding.vector(), (float) minScore);
      SearchResponse<EmbeddingDocument> response = client.search(
          SearchRequest.of(s -> s.index(indexName)
              .query(n -> n.scriptScore(scriptScoreQuery)).size(maxResults)),
          EmbeddingDocument.class);
      return response;
    } catch (IOException ex) {
      return null;
    }
  }

  private ScriptScoreQuery buildDefaultScriptScoreQuery(float[] vector,
      float minScore) throws JsonProcessingException {

    return ScriptScoreQuery.of(q -> q.minScore(minScore)
        .query(Query.of(qu -> qu.matchAll(m -> m)))
        .script(s -> s.inline(InlineScript.of(i -> i.source("knn_score")
            .lang("knn").params("field", JsonData.of("vector"))
            .params("query_value", JsonData.of(vector))
            .params("space_type", JsonData.of("cosinesimil")))))
        .boost(0.5f));

    // ===> From the OpenSearch documentation:
    // "Cosine similarity returns a number between -1 and 1, and because
    // OpenSearch
    // relevance scores can't be below 0, the k-NN plugin adds 1 to get the
    // final score."
    // See
    // https://opensearch.org/docs/latest/search-plugins/knn/knn-score-script
    // Thus, the query applies a boost of `0.5` to keep score in the range [0,
    // 1]
  }

  public void createIndexIfNotExist(int dimension) throws IOException {
    BooleanResponse response;
    response = client.indices().exists(c -> c.index(indexName));
    if (!response.value()) {
      client.indices().create(c -> c.index(indexName).settings(s -> s.knn(true))
          .mappings(getDefaultMappings(dimension)));
    }
  }

  private TypeMapping getDefaultMappings(int dimension) {
    Map<String, Property> properties = new HashMap<>(4);
    properties.put("text", Property.of(p -> p.text(TextProperty.of(t -> t))));
    properties.put("vector",
        Property.of(p -> p.knnVector(k -> k.dimension(dimension))));
    return TypeMapping.of(c -> c.properties(properties));
  }

  public BulkResponse bulkIndex(String indexName,
      List<EmbeddingDocument> docs) throws IOException {

    BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
    for (int i = 0; i < docs.size(); i++) {
      int finalI = i;
      bulkBuilder.operations(op -> op.index(idx -> idx.index(indexName)
          .id(String.join("_", indexName, Integer.toString(finalI)))
          .document(docs.get(finalI))));
    }

    return client.bulk(bulkBuilder.build());
  }

  public boolean removeIndex() throws IOException {
    if (StringUtils.isNotBlank(isIndexActive())) {
      return false;
    }
    DeleteIndexRequest request = new DeleteIndexRequest.Builder()
        .index(indexName).timeout(new Time.Builder().time("1m").build())
        .build();
    DeleteIndexResponse response = client.indices().delete(request);
    return response.acknowledged();
  }

  public String isIndexActive() {
    try {
      ExistsRequest request = new ExistsRequest.Builder().index(indexName)
          .build();
      BooleanResponse existsResponse = client.indices().exists(request);
      return existsResponse.value() ? StringUtils.EMPTY
          : String.format(INDEX_NOT_EXIST_ERROR, indexName);
    } catch (Exception e) {
      return String.format(CANNOT_CONNECT_TO_OPEN_SEARCH, serverUrl);
    }

  }

  public BulkResponse bulkEmbeddingDocumentById(String indexName,
      String documentId, EmbeddingDocument doc) throws IOException {

    BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();
    bulkBuilder.operations(op -> op
        .index(idx -> idx.index(indexName).id(documentId).document(doc)));
    return client.bulk(bulkBuilder.build());
  }

  public boolean removeEmbeddingDocumentById(String id) throws IOException {
    DeleteByQueryRequest request = new DeleteByQueryRequest.Builder()
        .index(indexName)
        .query(TermQuery.of(t -> t.field("id").value(FieldValue.of(id))).toQuery())
        .timeout(new Time.Builder().time("1m").build())
        .build();

    DeleteByQueryResponse response = client.deleteByQuery(request);
    return response.deleted() != 0;
  }
}