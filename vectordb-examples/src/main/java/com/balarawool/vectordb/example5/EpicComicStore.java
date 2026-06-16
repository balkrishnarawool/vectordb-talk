package com.balarawool.vectordb.example5;

import com.pgvector.PGvector;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
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
    private EmbeddingModel embeddingModel;
    private ChatModel chatModel;
    private OllamaEmbeddingOptions ollamaEmbeddingOptions;

    public EpicComicStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel, ChatModel chatModel, OllamaEmbeddingOptions ollamaEmbeddingOptions) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.ollamaEmbeddingOptions = ollamaEmbeddingOptions;
    }

    public String chat(String message) {
        String messageToSend = "";
        if (RAG) {
            var embeddingResponse = embeddingModel.call(
                    new EmbeddingRequest(List.of(message.toLowerCase()), ollamaEmbeddingOptions));
            var vector = embeddingResponse.getResults().get(0).getOutput();
            var neighborParams = new Object[]{new PGvector(vector)};
            var rows = jdbcTemplate.queryForList("SELECT * FROM epic_vector_store ORDER BY embedding <-> ? LIMIT 2", neighborParams);
            var messages = rows.stream().map(r -> r.get("orig_content").toString()).toList();
            var sb = new StringBuilder();
            sb.append(PROMPT1);
            messages.forEach(s -> sb.append(s).append(" "));
            sb.append(PROMPT2);
            messageToSend = sb.toString();
        }

        messageToSend = PROMPT0 + messageToSend + message;
        System.out.println("Sending message to LLM: Message: " + messageToSend);
        ChatResponse response = chatModel.call(new Prompt(messageToSend,
                OpenAiChatOptions.builder()
                        .model("llama-3.1-8b-instant") //Although this is set in properties file, have to set it here again
                        .temperature(0.4).build()));
        return response.getResult().getOutput().getText();
    }
}
