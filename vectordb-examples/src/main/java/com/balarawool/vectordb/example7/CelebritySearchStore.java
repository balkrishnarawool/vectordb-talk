package com.balarawool.vectordb.example7;

import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import io.weaviate.client.v1.graphql.query.argument.NearImageArgument;
import io.weaviate.client.v1.graphql.query.fields.Field;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Component
public class CelebritySearchStore {
    private WeaviateClient weaviateClient;

    public CelebritySearchStore(WeaviateClient weaviateClient) {
        this.weaviateClient = weaviateClient;
    }

    public ResponseEntity<Result<GraphQLResponse>> search(MultipartFile file) {
        try {
            Result<GraphQLResponse> r = weaviateClient.graphQL().get()
                    .withClassName("Celebrities")
                    .withFields(Field.builder().name("image").build())
                    .withNearImage(NearImageArgument.builder().image(Base64.getEncoder().encodeToString(file.getBytes())).build())
                    .withLimit(6)
                    .run();

            return ResponseEntity.ok(r);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
