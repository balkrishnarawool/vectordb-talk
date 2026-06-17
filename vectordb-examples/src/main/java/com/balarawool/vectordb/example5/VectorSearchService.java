package com.balarawool.vectordb.example5;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

///
/// Uses Elasticsearch to search within the Epic Comic Co FAQ
@Service
public class VectorSearchService {
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
                                .k(2)                  // Retrieve closest 1 result
                                .numCandidates(10)     // HNSW depth traversal threshold
                        ),
                QuestionDocument.class
        );

        List<String> results = new ArrayList<>();
        for (Hit<QuestionDocument> hit : response.hits().hits()) {
            QuestionDocument doc = hit.source();
            if (doc != null) {
                System.out.println(String.format("Match: '%s' (Score: %.4f)", doc.getText(), hit.score()));
                results.add(doc.getText());
            }
        }
        return results;
    }
}
