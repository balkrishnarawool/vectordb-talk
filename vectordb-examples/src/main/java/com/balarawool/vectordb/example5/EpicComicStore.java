package com.balarawool.vectordb.example5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class EpicComicStore {
    private static final Logger log = LoggerFactory.getLogger(EpicComicStore.class);

    private static final String PROMPT0 = "Please be concise with your responses. ";
    private static final String PROMPT1 = "You are a helpful and friendly support assistant for Epic Comic Co, a comic book store. " +
            "Below is more info you can use to answer the question from the customer. ";
    private static final String PROMPT2 = "Please answer this question from the customer: ";

    @Value("${spring.ai.ollama.chat.model}")
    private String ollamaChatModel = "gemma4:e2b";

    @Value("${spring.ai.openai.chat.model}")
    private String openaiChatModel = "openai/gpt-oss-20b";

    @Value("${example05.model.provider}")
    private String modelProvider = "ollama";

    @Value("${example05.use-rag}")
    private boolean useRag = false;

    private JdbcTemplate jdbcTemplate;
    private EmbeddingModel embeddingModel;
    private ChatModel chatModel;
    private OllamaEmbeddingOptions ollamaEmbeddingOptions;
    private final VectorSearchService vectorSearchService;

    public EpicComicStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel, ChatModel chatModel, OllamaEmbeddingOptions ollamaEmbeddingOptions, VectorSearchService vectorSearchService) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
        this.chatModel = chatModel;
        this.ollamaEmbeddingOptions = ollamaEmbeddingOptions;
        this.vectorSearchService = vectorSearchService;
    }

    public String chat(String message) {
        try {
            String messageToSend = "";
            if (useRag) {
                var embeddingResponse = embeddingModel.call(
                        new EmbeddingRequest(List.of(message.toLowerCase()), ollamaEmbeddingOptions));
                var output = embeddingResponse.getResults().get(0).getOutput();
                var vector = IntStream.range(0, output.length).mapToObj(i -> output[i]).toList();

                var messages = vectorSearchService.findSimilar(vector);

                var sb = new StringBuilder();
                sb.append(PROMPT1);
                messages.forEach(s -> sb.append(s).append(" "));
                sb.append(PROMPT2);
                messageToSend = sb.toString();
            }

            messageToSend = PROMPT0 + messageToSend + message;
            log.info("Sending message to LLM: Message: {}", messageToSend);
            ChatResponse response = chatModel.call(new Prompt(messageToSend,
                    OpenAiChatOptions.builder()
                            .model(getChatModel()) //Although this is set in properties file, have to set it here again
                            .temperature(0.4).build()));
            return response.getResult().getOutput().getText();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getChatModel() {
        return switch (modelProvider) {
          case "ollama" -> ollamaChatModel;
          case "groq" -> openaiChatModel;
            default -> ollamaChatModel;
        };
    }

}
