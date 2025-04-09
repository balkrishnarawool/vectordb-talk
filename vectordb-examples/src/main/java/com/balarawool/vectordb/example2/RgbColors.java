package com.balarawool.vectordb.example2;

import com.balarawool.vectordb.db.CosineSimilarityCalculator;
import com.balarawool.vectordb.db.Vector;
import com.balarawool.vectordb.db.VectorDB;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class RgbColors {
    private static int K = 7;

    private VectorDB<String> vdb = null;

    public RgbColors() {
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

    private static Vector embed(double r, double g, double b){
        // double v = Math.sqrt(r*r + g*g + b*b);
        // return new Vector(new double[]{r/v, g/v, b/v});
        return new Vector(new double[]{r, g, b}).normalize();
    }

    public record SevenColors(Color similar1, Color similar2, Color similar3, Color similar4, Color similar5, Color similar6, Color similar7) { }
    public record Color(String code, String vector, double distance) { }

    public SevenColors nearestNeighbours(double r, double g, double b){
        var vector = RgbColors.embed(r,g,b);

        var list = vdb.kNearestNeighbours(vector, K, new CosineSimilarityCalculator());
        return new SevenColors(
                new Color(list.get(0).entry().getValue(), Arrays.toString(list.get(0).entry().getKey().embedding()), list.get(0).d()),
                new Color(list.get(1).entry().getValue(), Arrays.toString(list.get(1).entry().getKey().embedding()), list.get(1).d()),
                new Color(list.get(2).entry().getValue(), Arrays.toString(list.get(2).entry().getKey().embedding()), list.get(2).d()),
                new Color(list.get(3).entry().getValue(), Arrays.toString(list.get(3).entry().getKey().embedding()), list.get(3).d()),
                new Color(list.get(4).entry().getValue(), Arrays.toString(list.get(4).entry().getKey().embedding()), list.get(4).d()),
                new Color(list.get(5).entry().getValue(), Arrays.toString(list.get(5).entry().getKey().embedding()), list.get(5).d()),
                new Color(list.get(6).entry().getValue(), Arrays.toString(list.get(6).entry().getKey().embedding()), list.get(6).d())
        );
    }
}
