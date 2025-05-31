package com.balarawool.vectordb.example1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GrayController {
    private GrayColors grayColors;

    public GrayController(GrayColors grayColors) {
        this.grayColors = grayColors;
    }

    @GetMapping("/gray-colors/nearest-neighbours")
    public GrayColors.ThreeColors nearestNeighbours(@RequestParam("color") String color) {
        return grayColors.nearestNeighbours(color);
    }
}