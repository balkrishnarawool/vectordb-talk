package com.balarawool.vectordb.example7;

import io.weaviate.client.base.Result;
import io.weaviate.client.v1.graphql.model.GraphQLResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class CelebritySearchController {
    private CelebritySearchStore celebritySearchStore;

    public CelebritySearchController(CelebritySearchStore froogleSearchStore) {
        this.celebritySearchStore = froogleSearchStore;
    }

    @PostMapping("/celebrity-search")
    public ResponseEntity<Result<GraphQLResponse>> search(MultipartFile file) {
        return celebritySearchStore.search(file);
    }
}