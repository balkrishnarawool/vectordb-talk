package com.balarawool.vectordb.example4;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class BigWikiController {
    @Autowired
    BigWikiPGVectorHNSW bigWikiPGVectorHNSW;

    @GetMapping("/big-wiki/search")
    public List<String> search(@RequestParam("query") String query) {
        return bigWikiPGVectorHNSW.search(query);
    }
}