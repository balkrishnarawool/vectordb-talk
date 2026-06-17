package com.balarawool.vectordb.example5;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

///
/// Uses Elasticsearch to search within the Epic Comic Co FAQ
@Service
public class VectorSearchService {
    private static final Logger log = LoggerFactory.getLogger(VectorSearchService.class);

    private final ElasticsearchClient client;
    public static final String INDEX_NAME = "epic_comic_store_vector_index";

    public VectorSearchService(ElasticsearchClient client) {
        this.client = client;
    }

    // Executes k-NN Search using float vector query input
    public List<String> findSimilar(List<Float> queryVector) throws IOException {
        SearchResponse<QuestionDocument> response = client.search(s -> s
                        .index(INDEX_NAME)
                        .knn(k -> k
                                .field("textVector")
                                .queryVector(queryVector)
                                .k(2)                  // Retrieve closest 2 results
                                .numCandidates(10)     // HNSW depth traversal threshold
                        ),
                QuestionDocument.class
        );

        List<String> results = new ArrayList<>();
        for (Hit<QuestionDocument> hit : response.hits().hits()) {
            QuestionDocument doc = hit.source();
            if (doc != null) {
                log.info("Match: '{}' (Score: {})", doc.getText(), hit.score());
                results.add(doc.getText());
            }
        }
        return results;
    }
}
