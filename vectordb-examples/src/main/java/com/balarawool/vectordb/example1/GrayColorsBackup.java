package com.balarawool.vectordb.example1;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GrayColorsBackup {
    private static int K = 3;
    private static List<String> COLORS = List.of("ffffff", "eeeeee", "dddddd", "cccccc", "aaaaaa",
                                                 "999999", "777777", "555555", "333333", "000000");

    private static Map<int[], String> COLOR_EMBEDDINGS = calcEmbeddings();

    private static Map<int[], String> calcEmbeddings() {
        return COLORS.stream()
                .collect(Collectors.toMap(c -> embed(c), c -> c));
    }

    public static int[] embed(String hexColor){
        for (int i = 0; i < COLORS.size(); i++) {
            if (COLORS.get(i).equals(hexColor)) return new int[]{i};
        }
        throw new IllegalStateException(String.format("Embedding for this hex color-code %s is not supported", hexColor));
    }

    public static int dist(int[] color1, int[] color2){
        var dist = VectorUtil.subtract(color1, color2);
        return Math.abs(dist[0]);
    }

    public record ThreeColors(Color exact, Color similar1, Color similar2) { }
    public record Color(String code, String vector, int distance) { }
    public static ThreeColors nearestNeighbours(String color){
        var exact = COLOR_EMBEDDINGS.keySet().stream()
                .filter(e -> dist(embed(color), e) == 0)
                .findFirst()
                .map(k -> new Color(COLOR_EMBEDDINGS.get(k), Arrays.toString(k), 0))
                .orElseThrow(() -> new IllegalStateException(String.format("No exact match found for %s", color)));
        var similar = COLOR_EMBEDDINGS.keySet().stream()
                .filter(e -> dist(embed(color), e) == 1)
                .map(k -> new Color(COLOR_EMBEDDINGS.get(k), Arrays.toString(k), 1))
                .toList();

        return similar.size() == 1 ? new ThreeColors(exact, similar.get(0), null) : new ThreeColors(exact, similar.get(0), similar.get(1));
    }
}
