package com.balarawool.vectordb.example1;

import com.balarawool.vectordb.db.EuclideanDistanceCalculator;
import com.balarawool.vectordb.db.Vector;
import com.balarawool.vectordb.db.VectorDB;
import com.balarawool.vectordb.db.VectorDB.Tuple;
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

    // This implementation loops over the list twice, but it is clear.
    // It makes it clear that the embedding is dependent on the index of the color in te list.
    // So keeping it this way.
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
                getColorFromTuple(list.get(0)),
                getColorFromTuple(list.get(1)),
                getColorFromTuple(list.get(2))
        );
    }

    private Color getColorFromTuple(Tuple<String> tuple) {
        return new Color(tuple.entry().getValue(), vectorToString(tuple), tuple.d());
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
