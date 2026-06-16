package com.balarawool.vectordb.example4;

import com.pgvector.PGvector;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BigWikiHNSW {
    private JdbcTemplate jdbcTemplate;
    private EmbeddingModel embeddingModel;
    private OllamaEmbeddingOptions ollamaEmbeddingOptions;

    public BigWikiHNSW(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel, OllamaEmbeddingOptions ollamaEmbeddingOptions) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
        this.ollamaEmbeddingOptions = ollamaEmbeddingOptions;
    }

    public List<String> search(String query) {
        var embeddingResponse = embeddingModel.call(
                new EmbeddingRequest(List.of(query.toLowerCase()), ollamaEmbeddingOptions));
        var vector = embeddingResponse.getResults().get(0).getOutput();
        var neighborParams = new Object[] { new PGvector(vector) };
        var rows = jdbcTemplate.queryForList("SELECT * FROM math_big_vector_store ORDER BY embedding <-> ? LIMIT 5", neighborParams);
        return rows.stream().map(r -> r.get("content").toString()).toList();
    }
}
