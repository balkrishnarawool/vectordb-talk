package com.balarawool.vectordb.example1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GrayController {

    @GetMapping("/gray-colors/nearest-neighbours")
    public GrayColors.ThreeColors nearestNeighbours(@RequestParam("color") String color) {
        return GrayColors.nearestNeighbours(color);
    }


}