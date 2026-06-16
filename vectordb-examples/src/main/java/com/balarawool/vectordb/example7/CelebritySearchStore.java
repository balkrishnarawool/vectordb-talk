package com.balarawool.vectordb.example7;

import com.pgvector.PGvector;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import org.bytedeco.flycapture.FlyCapture2.Image;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Optional;

@Service
public class CelebritySearchStore {
    private WeaviateClient weaviateClient;
    private JdbcTemplate jdbcTemplate;

    public CelebritySearchStore(WeaviateClient weaviateClient, JdbcTemplate jdbcTemplate) {
        this.weaviateClient = weaviateClient;
        this.jdbcTemplate = jdbcTemplate;
    }

//    public ResponseEntity<Result<GraphQLResponse>> search(MultipartFile file) {
//        try {
//            Result<GraphQLResponse> r = weaviateClient.graphQL().get()
//                    .withClassName("Celebrities")
//                    .withFields(Field.builder().name("image").build())
//                    .withNearImage(NearImageArgument.builder()
//                            .image(Base64.getEncoder().encodeToString(file.getBytes()))
//                            .build())
//                    .withLimit(6)
//                    .run();
//
//            return ResponseEntity.ok(r);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public Optional<ImageData> search(MultipartFile file) {
        try {
            System.out.println(System.getProperty("java.io.tmpdir"));
            Path tempFile = Files.createTempFile("upload-", file.getOriginalFilename());
            file.transferTo(tempFile.toFile());
            var vector = FaceVectorCalculator.calculateFaceVector(tempFile.toAbsolutePath().toString());

            if (!vector.isEmpty()) {
                var neighborParams = new Object[]{new PGvector(vector)};
                var rows = jdbcTemplate.queryForList("SELECT * FROM celebrity ORDER BY embedding <-> ? LIMIT 1", neighborParams);
                var res = rows.stream().map(r -> new ImageData(r.get("filename").toString(), r.get("content").toString())).toList();
                return res.stream().findFirst();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public record ImageData(String filename, String content) { }
}
