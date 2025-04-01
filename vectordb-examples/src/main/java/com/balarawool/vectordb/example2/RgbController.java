package com.balarawool.vectordb.example2;

import com.balarawool.vectordb.db.Vector;
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
        return rgbColors.nearestNeighbours(r, g, b);
    }

}