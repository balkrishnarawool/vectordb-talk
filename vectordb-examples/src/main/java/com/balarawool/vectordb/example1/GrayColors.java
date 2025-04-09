package com.balarawool.vectordb.example1;

import com.balarawool.vectordb.db.EuclideanDistanceCalculator;
import com.balarawool.vectordb.db.Vector;
import com.balarawool.vectordb.db.VectorDB;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GrayColors {
    private static int K = 3;
    private static List<String> COLORS = List.of("ffffff", "eeeeee", "dddddd", "cccccc", "aaaaaa",
                                                 "999999", "777777", "555555", "333333", "000000");
    private VectorDB<String> vdb = null;

    public GrayColors() {
        vdb = VectorDB.create();
        for (var color: COLORS) {
            vdb.insert(color, embed(color));
        }
    }

    private static Vector embed(String hexColor){
        for (int i = 0; i < COLORS.size(); i++) {
            if (COLORS.get(i).equals(hexColor)) return new Vector(new double[]{i});
        }
        throw new IllegalStateException(String.format("Embedding for this hex color-code %s is not supported", hexColor));
    }

    public record ThreeColors(Color similar1, Color similar2, Color similar3) { }
    public record Color(String code, String vector, double distance) { }

    public ThreeColors nearestNeighbours(String color){
        var list = vdb.kNearestNeighbours(embed(color), K, new EuclideanDistanceCalculator());
        return new ThreeColors(
                new Color(list.get(0).entry().getValue(), vectorToString(list.get(0)), list.get(0).d()),
                new Color(list.get(1).entry().getValue(), vectorToString(list.get(1)), list.get(1).d()),
                new Color(list.get(2).entry().getValue(), vectorToString(list.get(2)), list.get(2).d())
        );
    }

    private String vectorToString(VectorDB.Tuple<String> tuple) {
        return Arrays.toString(toIntArray(tuple.entry().getKey().embedding()));
    }

    private int[] toIntArray(double[] doubleArray) {
        final int[] intArray = new int[doubleArray.length];
        for (int i=0; i<intArray.length; ++i)
            intArray[i] = (int) doubleArray[i];
        return intArray;
    }
}
