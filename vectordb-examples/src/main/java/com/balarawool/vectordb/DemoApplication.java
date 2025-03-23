package com.balarawool.vectordb;

import com.pgvector.PGvector;
import io.weaviate.client.Config;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.v1.schema.model.Property;
import io.weaviate.client.v1.schema.model.WeaviateClass;
import org.apache.commons.io.FileUtils;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaEmbeddingClient;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

///
/// Don't forget --add-modules jdk.incubator.vector
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunnerMathBig(JdbcTemplate jdbcTemplate) {
        return args -> {
            var initializeDb = false;
            if (initializeDb) {
                System.out.println("Initializing...");
                var ollamaApi = new OllamaApi();
                var embeddingClient = new OllamaEmbeddingClient(ollamaApi).withDefaultOptions(OllamaOptions.create().withModel("llama3"));

                var path = ResourceUtils.getFile("classpath:data/mathematics_lines.txt").toPath();
                var n = new AtomicInteger(1);
                var set = new HashSet<String>();
                Files.lines(path, Charset.defaultCharset()).forEach(s -> {
                    if (s.split("\\s+").length > 5) {
                        if (set.add(s)) {
                            System.out.println(s);
                            EmbeddingResponse embeddingResponse = embeddingClient.embedForResponse(List.of(s.toLowerCase()));
                            List<Double> vector = embeddingResponse.getResults().get(0).getOutput();
                            vector.subList(2000, 4096).clear(); // remove dimensions before sending it to DB.

                            jdbcTemplate.update("INSERT INTO math_big_vector_store(content, embedding) values(?, ?)", s, new PGvector(vector));
                            System.out.println("Line " + n.getAndIncrement() + " of approximately 2000 added.");
                        }
                    }
                });
            }
        };
    }

    @Bean
    ApplicationRunner applicationRunnerEpic(JdbcTemplate jdbcTemplate) {
        return args -> {
            var initializeDb = false;
            if (initializeDb) {
                System.out.println("Initializing...");
                var ollamaApi = new OllamaApi();
                var embeddingClient = new OllamaEmbeddingClient(ollamaApi).withDefaultOptions(OllamaOptions.create().withModel("llama3"));

                var path = ResourceUtils.getFile("classpath:data/epic_comic_co_faq.txt").toPath();
                var n = new AtomicInteger(1);
                var sb = new StringBuilder();
                Files.lines(path, Charset.defaultCharset()).forEach(s -> {
                    if (!s.trim().isEmpty()) {
                        sb.append(s).append(" ");
                    } else {
                        var chunk = sb.toString();
                        if (!chunk.isEmpty()) {
                            System.out.println(chunk);
                            EmbeddingResponse embeddingResponse = embeddingClient.embedForResponse(List.of(chunk.toLowerCase()));
                            List<Double> vector = embeddingResponse.getResults().get(0).getOutput();
                            vector.subList(2000, 4096).clear(); // remove dimensions before sending it to DB.

                            jdbcTemplate.update("INSERT INTO epic_vector_store(content, orig_content, embedding) values(?, ?, ?)", chunk.toLowerCase(), chunk, new PGvector(vector));
                            sb.delete(0, sb.length());
                        }
                    }
                });
            }
        };
    }

    @Bean
    WeaviateClient weaviateClient() {
        Config config = new Config("http", "localhost:8080");
        return new WeaviateClient(config);
    }

    @Bean
    ApplicationRunner applicationRunnerWonders(WeaviateClient weaviateClient) {
        return args -> {
            var initializeDb = false;
            initialize(weaviateClient, initializeDb, "WonderImage", "data/wonder-images");
        };
    }

    @Bean
    ApplicationRunner applicationRunnerCelebrities(WeaviateClient weaviateClient) {
        return args -> {
            var initializeDb = false;
            initialize(weaviateClient, initializeDb, "Celebrities", "data/celebrities");
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
                                System.out.println("File stored: " + f.getPath());
                            }
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
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
