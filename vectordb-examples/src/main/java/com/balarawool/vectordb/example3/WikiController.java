package com.balarawool.vectordb.example3;

import com.balarawool.vectordb.example2.RgbColors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class WikiController {

    @GetMapping("/wiki/embedding")
    public WikiWord2Vec.Embedding embedding(@RequestParam("word") String word) throws IOException {
        return WikiWord2Vec.embedding(word);
    }

    @GetMapping("/wiki/nearest-neighbours")
    public List<WikiWord2Vec.Entry> nearestNeighbours(@RequestParam("word") String word) throws IOException {
        return WikiWord2Vec.nearestNeighbours(word);
    }

    @GetMapping("/wiki/equation")
    public List<WikiWord2Vec.Entry> equation(@RequestParam("start") String start, @RequestParam("toSubtract") String toSubtract, @RequestParam("toAdd") String toAdd) throws IOException {
        return WikiWord2Vec.equation(start, toSubtract, toAdd);
    }


}