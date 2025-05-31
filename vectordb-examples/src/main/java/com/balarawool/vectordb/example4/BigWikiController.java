package com.balarawool.vectordb.example4;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BigWikiController {
    BigWikiHNSW bigWikiHNSW;

    public BigWikiController(BigWikiHNSW bigWikiHNSW) {
        this.bigWikiHNSW = bigWikiHNSW;
    }

    @GetMapping("/big-wiki/search")
    public List<String> search(String query) {
        return bigWikiHNSW.search(query);
    }
}