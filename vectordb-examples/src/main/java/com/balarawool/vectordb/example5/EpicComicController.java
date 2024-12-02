package com.balarawool.vectordb.example5;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EpicComicController {
    @Autowired
    EpicComicStore epicComicStore;

    public record Response(String message) { }
    @GetMapping("/epic-support/chat")
    public Response chat(@RequestParam("message") String message) {
        return new Response(epicComicStore.chat(message));
    }
}