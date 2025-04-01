package com.balarawool.vectordb.example2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RgbController {
    private RgbColors rgbColors;

    public RgbController(RgbColors rgbColors) {
        this.rgbColors = rgbColors;
    }

    @GetMapping("/rgb-colors/nearest-neighbours")
    public RgbColors.SevenColors nearestNeighbours(@RequestParam("r") int r, @RequestParam("g") int g, @RequestParam("b") int b) {
        var vector = RgbColors.embed(r,g,b).embedding();
        return rgbColors.nearestNeighbours(vector[0], vector[1], vector[2]);
    }

}