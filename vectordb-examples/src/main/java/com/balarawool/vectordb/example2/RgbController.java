package com.balarawool.vectordb.example2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RgbController {

    @GetMapping("/rgb-colors/nearest-neighbours")
    public RgbColors.SevenColors nearestNeighbours(@RequestParam("r") int r, @RequestParam("g") int g, @RequestParam("b") int b) {
        var vector = RgbColors.embed(r,g,b);
        return RgbColors.nearestNeighbours(vector[0], vector[1], vector[2]);
    }


}