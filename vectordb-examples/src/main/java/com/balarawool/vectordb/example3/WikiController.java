package com.balarawool.vectordb.example3;

import com.balarawool.vectordb.example2.RgbColors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class WikiController {
    private WikiWord2Vec wikiWord2Vec;

    public WikiController(WikiWord2Vec wikiWord2Vec) {
        this.wikiWord2Vec = wikiWord2Vec;
    }

    @GetMapping("/wiki/embedding")
    public WikiWord2Vec.Embedding embedding(String word) throws IOException {
        return wikiWord2Vec.embedding(word);
    }

    @GetMapping("/wiki/nearest-neighbours")
    public List<WikiWord2Vec.Entry> nearestNeighbours(String word) throws IOException {
        return wikiWord2Vec.nearestNeighbours(word);
    }

    @GetMapping("/wiki/equation")
    public List<WikiWord2Vec.Entry> equation(String start, String toSubtract, String toAdd) throws IOException {
        return wikiWord2Vec.equation(start, toSubtract, toAdd);
    }


}