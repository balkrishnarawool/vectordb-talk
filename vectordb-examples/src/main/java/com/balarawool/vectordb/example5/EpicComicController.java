package com.balarawool.vectordb.example5;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EpicComicController {
    private EpicComicStore epicComicStore;

    public EpicComicController(EpicComicStore epicComicStore) {
        this.epicComicStore = epicComicStore;
    }

    public record Response(String message) { }
    @GetMapping("/epic-support/chat")
    public Response chat(String message) {
        return new Response(epicComicStore.chat(message));
    }
}