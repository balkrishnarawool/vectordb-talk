package com.balarawool.vectordb.db;

public class OneDimensionDistanceCalculator<V extends Vector> implements DistanceCalculator<V>{
    @Override
    public double distance(V vector1, V vector2) {
        if (vector1 instanceof Vector.Int(int[] embedding1) && vector2 instanceof Vector.Int(int[] embedding2)) {
            if (embedding1.length != 1 || embedding2.length != 1) {
                throw new IllegalStateException(String.format("The sizes of two vectors being processed by OneDimensionDistanceCalculator has to be 1." +
                        "Sizes of them are %s and %s.", embedding1.length, embedding2.length));
            }
            return Math.abs(embedding1[0] - embedding2[0]);
        } else {
            if (vector1 instanceof Vector.Double(double[] embedding1) && vector2 instanceof Vector.Double(double[] embedding2) ) {
                if (embedding1.length != 1 || embedding2.length != 1) {
                    throw new IllegalStateException(String.format("The sizes of two vectors being processed by OneDimensionDistanceCalculator has to be 1." +
                            "Sizes of them are %s and %s.", embedding1.length, embedding2.length));
                }
                return Math.abs(embedding1[0] - embedding2[0]);
            }
        }
        throw new IllegalStateException("Unsupported vector type. Only supported type of vectors are of type Vector");
    }
}
