package com.balarawool.vectordb.example6;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@RestController
public class FroogleSearchController {
    FroogleSearchStore froogleSearchStore;

    public FroogleSearchController(FroogleSearchStore froogleSearchStore) {
        this.froogleSearchStore = froogleSearchStore;
    }

    @PostMapping("/froogle-search")
    public ResponseEntity<Result<GraphQLResponse>> search(MultipartFile file) {
        return froogleSearchStore.search(file);
    }
}