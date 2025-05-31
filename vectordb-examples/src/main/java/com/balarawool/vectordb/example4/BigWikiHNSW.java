package com.balarawool.vectordb.example4;

import com.pgvector.PGvector;
import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BigWikiHNSW {
    JdbcTemplate jdbcTemplate;

    public BigWikiHNSW(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<String> search(String query) {
        var ollamaApi = new OllamaApi();

        var embeddingClient = new OllamaEmbeddingClient(ollamaApi).withDefaultOptions(OllamaOptions.create().withModel("llama3"));
        var embeddingResponse = embeddingClient.embedForResponse(List.of(query.toLowerCase()));
        var vector = embeddingResponse.getResults().get(0).getOutput();
        vector.subList(2000, 4096).clear(); // remove dimensions before sending it to DB.
        var neighborParams = new Object[] { new PGvector(vector) };
        var rows = jdbcTemplate.queryForList("SELECT * FROM math_big_vector_store ORDER BY embedding <-> ? LIMIT 5", neighborParams);
        return rows.stream().map(r -> r.get("content").toString()).toList();
    }
}
