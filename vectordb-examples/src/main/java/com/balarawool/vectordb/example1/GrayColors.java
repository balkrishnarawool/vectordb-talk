package com.balarawool.vectordb.example1;

import com.balarawool.vectordb.db.ScalarDistanceCalculator;
import com.balarawool.vectordb.db.Vector;
import com.balarawool.vectordb.db.VectorDB;

import java.util.Arrays;
import java.util.List;

public class GrayColors {
    private static int K = 3;
    private static List<String> COLORS = List.of("ffffff", "eeeeee", "dddddd", "cccccc", "aaaaaa",
                                                 "999999", "777777", "555555", "333333", "000000");
    private static VectorDB<String> vdb = null;

    private static void initializeDb() {
        vdb = VectorDB.create();
        for (var color: COLORS) {
            vdb.insert(color, embed(color));
        }
    }

    public static Vector embed(String hexColor){
        for (int i = 0; i < COLORS.size(); i++) {
            if (COLORS.get(i).equals(hexColor)) return new Vector(new double[]{i});
        }
        throw new IllegalStateException(String.format("Embedding for this hex color-code %s is not supported", hexColor));
    }

    public record ThreeColors(Color similar1, Color similar2, Color similar3) { }
    public record Color(String code, String vector, double distance) { }
    public static ThreeColors nearestNeighbours(String color){
        if (vdb == null) {
            initializeDb();
        }
        var list = vdb.kNearestNeighbours(embed(color), K, new ScalarDistanceCalculator());
        return new ThreeColors(
                new Color(list.get(0).entry().getValue(), Arrays.toString(list.get(0).entry().getKey().embedding()), list.get(0).distance()),
                new Color(list.get(1).entry().getValue(), Arrays.toString(list.get(1).entry().getKey().embedding()), list.get(1).distance()),
                new Color(list.get(2).entry().getValue(), Arrays.toString(list.get(2).entry().getKey().embedding()), list.get(2).distance())
        );
    }
}
