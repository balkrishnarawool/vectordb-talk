package com.balarawool.vectordb;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorProperty;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import co.elastic.clients.elasticsearch._types.mapping.TextProperty;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.balarawool.vectordb.example5.QuestionDocument;
import com.balarawool.vectordb.example7.FaceVectorCalculator;
import com.pgvector.PGvector;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.balarawool.vectordb.example5.VectorSearchService.INDEX_NAME;

///
/// Don't forget --add-modules jdk.incubator.vector
/// Added to spring-boot-maven-plugin in pom.xml
@SpringBootApplication
public class DemoApplication {
    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    // We need to do this because there are two beans of EmbeddingModel from different libraries:
    // OllamaEmbeddingModel and OpenAiEmbeddingModel.
    // We want to use OllamaEmbeddingModel.
    @Bean
    @Primary
    public EmbeddingModel embeddingModel(@Qualifier("ollamaEmbeddingModel") EmbeddingModel ollamaEmbeddingModel) {
        return ollamaEmbeddingModel;
    }

    // We need to do this because there are two beans of ChatModel from different libraries:
    // OllamaChatModel and OpenAiChatModel.
    // We want to use OpenAiChatModel.
    @Bean
    @Primary
    public ChatModel chatModel(@Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        return openAiChatModel;
    }

    @Bean
    public OllamaEmbeddingOptions ollamaEmbeddingOptions() {
        return OllamaEmbeddingOptions.builder()
                .model("nomic-embed-text")
                .truncate(false)
                .build();
    }

    @Bean
    public RestClient restClient() {
        return RestClient.builder(
                new HttpHost("localhost", 9200, "http")
        ).build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }

    // Store vectors for Mathematics (Example 4) in postgres
    @Bean
    ApplicationRunner applicationRunnerMathBig(JdbcTemplate jdbcTemplate, OllamaEmbeddingModel ollamaEmbeddingModel, OllamaEmbeddingOptions ollamaEmbeddingOptions) {
        return args -> {
            var initializeDb = false;
            if (initializeDb) {
                log.info("Initializing...");
                var path = ResourceUtils.getFile("classpath:data/mathematics_lines.txt").toPath();
                var n = new AtomicInteger(1);
                var set = new HashSet<String>();
                Files.lines(path, Charset.defaultCharset()).forEach(s -> {
                    if (s.split("\\s+").length > 5) {
                        if (set.add(s)) {
                            log.info(s);
                            EmbeddingResponse embeddingResponse = ollamaEmbeddingModel.call(
                                    new EmbeddingRequest(List.of(s.toLowerCase()), ollamaEmbeddingOptions));
                            var vector = embeddingResponse.getResults().get(0).getOutput();

                            jdbcTemplate.update("INSERT INTO math_big_vector_store(content, embedding) values(?, ?)", s, new PGvector(vector));
                            log.info("Line {} of approximately 1000 added.", n.getAndIncrement());
                        }
                    }
                });
            }
        };
    }

    // Store vectors for Epic Comics Co (Example 5) in postgres
    @Bean
    ApplicationRunner applicationRunnerEpic(JdbcTemplate jdbcTemplate, OllamaEmbeddingModel ollamaEmbeddingModel, OllamaEmbeddingOptions ollamaEmbeddingOptions) {
        return args -> {
            var initializeDb = false;
            if (initializeDb) {
                log.info("Initializing...");
                var path = ResourceUtils.getFile("classpath:data/epic_comic_co_faq.txt").toPath();
                var n = new AtomicInteger(1);
                var sb = new StringBuilder();
                Files.lines(path, Charset.defaultCharset()).forEach(s -> {
                    if (!s.trim().isEmpty()) {
                        sb.append(s).append(" ");
                    } else {
                        var chunk = sb.toString();
                        if (!chunk.isEmpty()) {
                            log.info(chunk);
                            EmbeddingResponse embeddingResponse = ollamaEmbeddingModel.call(
                                    new EmbeddingRequest(List.of(chunk.toLowerCase()), ollamaEmbeddingOptions));
                            var vector = embeddingResponse.getResults().get(0).getOutput();

                            jdbcTemplate.update("INSERT INTO epic_vector_store(content, orig_content, embedding) values(?, ?, ?)", chunk.toLowerCase(), chunk, new PGvector(vector));
                            sb.delete(0, sb.length());
                        }
                    }
                });
            }
        };
    }

    @Bean
    ApplicationRunner applicationRunnerEpic2(ElasticsearchClient elasticsearchClient, OllamaEmbeddingModel ollamaEmbeddingModel, OllamaEmbeddingOptions ollamaEmbeddingOptions) {
        return args -> {
            var initializeDb = false;
            if (initializeDb) {
                setupIndex(elasticsearchClient);
                log.info("Initializing...");
                var path = ResourceUtils.getFile("classpath:data/epic_comic_co_faq.txt").toPath();
                var n = new AtomicInteger(1);
                var sb = new StringBuilder();
                Files.lines(path, Charset.defaultCharset()).forEach(s -> {
                    try {
                        if (!s.trim().isEmpty()) {
                            sb.append(s).append(" ");
                        } else {
                            var chunk = sb.toString();
                            if (!chunk.isEmpty()) {
                                log.info(chunk);
                                EmbeddingResponse embeddingResponse = ollamaEmbeddingModel.call(
                                        new EmbeddingRequest(List.of(chunk.toLowerCase()), ollamaEmbeddingOptions));
                                var vector = embeddingResponse.getResults().get(0).getOutput();
                                var vectorList = IntStream.range(0, vector.length).mapToObj(i -> vector[i]).toList();

                                var doc = new QuestionDocument(String.valueOf(n.getAndIncrement()), chunk, vectorList);
                                elasticsearchClient.index(i -> i.index(INDEX_NAME).id(doc.getId()).document(doc));
                                elasticsearchClient.indices().refresh(r -> r.index(INDEX_NAME));

                                sb.delete(0, sb.length());
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        };
    }

    // Creates index with dense_vector and seeds mock items
    private void setupIndex(ElasticsearchClient client) throws IOException {
        // Reset index if it exists
        if (client.indices().exists(e -> e.index(INDEX_NAME)).value()) {
            client.indices().delete(d -> d.index(INDEX_NAME));
        }

        // Define Dense Vector schema with 768 dimensions and Cosine Similarity
        client.indices().create(c -> c
                .index(INDEX_NAME)
                .mappings(m -> m
                        .properties("text", co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.text(TextProperty.of(t -> t))))
                        .properties("textVector", co.elastic.clients.elasticsearch._types.mapping.Property.of(p -> p.denseVector(DenseVectorProperty.of(dv -> dv
                                .dims(768)
                                .index(true)
                                .similarity(DenseVectorSimilarity.Cosine)
                        ))))
                )
        );

//        // Populate Mock Data
//        List<ArticleDocument> mockDatabase = Arrays.asList(
//                new ArticleDocument("1", "The quick brown fox jumps over the lazy dog", List.of(0.1f, 0.9f, 0.2f)),
//                new ArticleDocument("2", "Artificial Intelligence and Machine Learning trends", List.of(0.8f, 0.1f, 0.7f)),
//                new ArticleDocument("3", "Healthy breakfast recipes for an energetic morning", List.of(0.2f, 0.3f, 0.9f))
//        );
//
//        for (ArticleDocument doc : mockDatabase) {
//            client.index(i -> i.index(INDEX_NAME).id(doc.getId()).document(doc));
//        }
//
//        // Refresh cluster memory
//        client.indices().refresh(r -> r.index(INDEX_NAME));
    }

    @Bean
    WeaviateClient weaviateClient() {
        Config config = new Config("http", "localhost:8080");
        return new WeaviateClient(config);
    }

    // Store vectors for Froogle Search (Example 6) in Weaviate
    @Bean
    ApplicationRunner applicationRunnerWonders(WeaviateClient weaviateClient) {
        return args -> {
            var initializeDb = false;
            initialize(weaviateClient, initializeDb, "WonderImage", "data/wonder-images");
        };
    }

    // Store vectors for Celebrities (Example 7) in Postgres
    @Bean
    ApplicationRunner applicationRunnerCelebrities(JdbcTemplate jdbcTemplate, WeaviateClient weaviateClient) {
        return args -> {
            var initializeDb = false;
//            initialize(weaviateClient, initializeDb, "Celebrities", "data/celebrities");
            initialize(jdbcTemplate, initializeDb, "data/celebrities");

        };
    }

    private void initialize(WeaviateClient weaviateClient, boolean initializeDb, String className, String dataPathInRes) {
        if (initializeDb) {
            // First check if class is present. If it is not present, then create it and store data.
            // If you want to delete it then do this: weaviateClient.schema().classDeleter().withClassName("Meme").run();
            var memeClass = weaviateClient.schema().classGetter().withClassName(className).run();
            if (memeClass.getResult() == null) {
                // Create Class/Collection in Weaviate
                var clazz = WeaviateClass.builder()
                        .className(className)
                        .vectorizer("img2vec-neural")
                        .vectorIndexType("hnsw")
                        .moduleConfig(Map.of("img2vec-neural", Map.of("imageFields", List.of("image"))))
                        .properties(List.of(Property.builder().name("image").dataType(List.of("blob")).build(),
                                Property.builder().name("text").dataType(List.of("string")).build()))
                        .build();
                var res = weaviateClient.schema().classCreator().withClass(clazz).run();

                if (res.getError() == null) {
                    // Store image files and their vectors
                    // Files are present in resources directory with this structure:
                    // resources
                    //    └ data
                    //       └ wonder-images
                    //           ├ china-wall
                    //           ├ coloseum
                    //           ├ machu-picchu
                    //           ├ pisa-tower
                    //           ├ pyramids
                    //           └ taj-mahal
                    // Each of these subdirectories contain .jpg, .png etc. files.
                    try {
                        var sampleDir = ResourceUtils.getFile("classpath:"+dataPathInRes);
                        for (var subDir : sampleDir.listFiles()) {
                            for (var f : subDir.listFiles()) {
                                embedAndStore(className, weaviateClient, f);
                                log.info("File stored: " + f.getPath());
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private void initialize(JdbcTemplate jdbcTemplate, boolean initializeDb, String dataPathInRes) {
        if (initializeDb) {
            try {
                var sampleDir = ResourceUtils.getFile("classpath:"+dataPathInRes);
                for (var subDir : sampleDir.listFiles()) {
                    for (var f : subDir.listFiles()) {
                        embedAndStore(jdbcTemplate, f);
                        log.info("File stored: " + f.getPath());
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void embedAndStore(JdbcTemplate jdbcTemplate, File f) {
        var vector = FaceVectorCalculator.calculateFaceVector(f.getAbsolutePath());
        if (!vector.isEmpty()) {
            try {
                byte[] fileBytes = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
                String content = Base64.getEncoder().encodeToString(fileBytes);
                jdbcTemplate.update("INSERT INTO celebrity(filename, content, embedding) values(?, ?, ?)", f.getAbsolutePath(), content, new PGvector(vector));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void embedAndStore(String className, WeaviateClient weaviateClient, File image) {
        try {
            byte[] fileContent = FileUtils.readFileToByteArray(image);
            String encodedString = Base64.getEncoder().encodeToString(fileContent);

            weaviateClient.data().creator()
                    .withClassName(className)
                    .withProperties(Map.of("image", encodedString, "text", image.getName()))
                    .run();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
