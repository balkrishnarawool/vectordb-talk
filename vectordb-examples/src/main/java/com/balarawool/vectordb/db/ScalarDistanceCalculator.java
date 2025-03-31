package com.balarawool.vectordb.db;

public class ScalarDistanceCalculator implements DistanceCalculator{
    @Override
    public double distance(Vector vector1, Vector vector2) {
        if (vector1.embedding().length != 1 || vector2.embedding().length != 1) {
            throw new IllegalStateException(String.format("The sizes of two vectors being processed by OneDimensionDistanceCalculator has to be 1." +
                    "Sizes of them are %s and %s.", vector1.embedding().length, vector2.embedding().length));
        }
        return Math.abs(vector1.embedding()[0] - vector2.embedding()[0]);
    }
}
