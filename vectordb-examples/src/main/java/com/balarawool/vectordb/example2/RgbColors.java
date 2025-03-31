package com.balarawool.vectordb.example2;

import com.balarawool.vectordb.db.CosineSimilarityCalculator;
import com.balarawool.vectordb.db.Vector;
import com.balarawool.vectordb.db.VectorDB;

import java.util.Arrays;

public class RgbColors {
    private static int K = 7;

    private static VectorDB<String> vdb = null;

    private static void initializeDb() {
        vdb = VectorDB.create();
        for (int i = -1; i<256; i+=16) {
            for (int j = -1; j<256; j+=16) {
                for (int k = -1; k<256; k+=16) {
                    int r = i==-1?1:i;
                    int g = j==-1?1:j;
                    int b = k==-1?1:k;
                    String hex = String.format("#%02x%02x%02x", r, g, b);
                    vdb.insert(hex, embed(r, g, b));
                }
            }
        }
    }

    public static Vector embed(double r, double g, double b){
        // normalize the vector
        double v = Math.sqrt(r*r + g*g + b*b);
        return new Vector(new double[]{r/v, g/v, b/v});
    }

    public record SevenColors(Color similar1, Color similar2, Color similar3, Color similar4, Color similar5, Color similar6, Color similar7) { }
    public record Color(String code, String vector, double distance) { }
    public static SevenColors nearestNeighbours(double r, double g, double b){
        if (vdb == null) {
            initializeDb();
        }
        var list = vdb.kNearestNeighbours(embed(r/256d,g/256d,b/256d), K, new CosineSimilarityCalculator());
        return new SevenColors(
                new Color(list.get(0).entry().getValue(), Arrays.toString(list.get(0).entry().getKey().embedding()), list.get(0).distance()),
                new Color(list.get(1).entry().getValue(), Arrays.toString(list.get(1).entry().getKey().embedding()), list.get(1).distance()),
                new Color(list.get(2).entry().getValue(), Arrays.toString(list.get(2).entry().getKey().embedding()), list.get(2).distance()),
                new Color(list.get(3).entry().getValue(), Arrays.toString(list.get(3).entry().getKey().embedding()), list.get(3).distance()),
                new Color(list.get(4).entry().getValue(), Arrays.toString(list.get(4).entry().getKey().embedding()), list.get(4).distance()),
                new Color(list.get(5).entry().getValue(), Arrays.toString(list.get(5).entry().getKey().embedding()), list.get(5).distance()),
                new Color(list.get(6).entry().getValue(), Arrays.toString(list.get(6).entry().getKey().embedding()), list.get(6).distance())
        );
    }
}
