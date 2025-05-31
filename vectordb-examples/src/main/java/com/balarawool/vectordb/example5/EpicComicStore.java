package com.balarawool.vectordb.example5;

import com.pgvector.PGvector;
import org.springframework.ai.ollama.OllamaChatClient;
import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EpicComicStore {
    private static final String PROMPT0 = "Please be concise with your responses. ";
    private static final String PROMPT1 = "You are a helpful and friendly support assistant for Epic Comic Co, a comic book store. " +
            "Below is more info you can use to answer the question from the customer. ";
    private static final String PROMPT2 = "Please answer this question from the customer: ";
    private static final boolean RAG = true;

    private JdbcTemplate jdbcTemplate;

    public EpicComicStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String chat(String message) {
        var ollamaApi = new OllamaApi();
        var messageToSend = "";

        if (RAG) {
            var embeddingClient = new OllamaEmbeddingClient(ollamaApi).withDefaultOptions(OllamaOptions.create().withModel("llama3"));
            var embeddingResponse = embeddingClient.embedForResponse(List.of(message.toLowerCase()));
            var vector = embeddingResponse.getResults().get(0).getOutput();
            vector.subList(2000, 4096).clear(); // remove dimensions before sending it to DB.
            var neighborParams = new Object[]{new PGvector(vector)};
            var rows = jdbcTemplate.queryForList("SELECT * FROM epic_vector_store ORDER BY embedding <-> ? LIMIT 2", neighborParams);
            var messages = rows.stream().map(r -> r.get("orig_content").toString()).toList();
            var sb = new StringBuilder();
            sb.append(PROMPT1);
            messages.forEach(s -> sb.append(s).append(" "));
            sb.append(PROMPT2);
            messageToSend = sb.toString();
        }

        var chatClient = new OllamaChatClient(ollamaApi).withDefaultOptions(OllamaOptions.create().withModel("llama3"));
        messageToSend = PROMPT0+ messageToSend + message;
        System.out.println("Sending message to LLM");
        return chatClient.call(messageToSend);
    }
}
