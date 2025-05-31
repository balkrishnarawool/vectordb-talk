package com.balarawool.vectordb.db;

public class EuclideanDistanceCalculator implements DistanceCalculator{
    @Override
    public double distance(Vector vector1, Vector vector2) {
        if (vector1.embedding().length != vector2.embedding().length) {
            throw new IllegalStateException(String.format("The sizes of two vectors being processed are not same. One is %s and other is %s.", vector1.embedding().length, vector2.embedding().length));
        }
        var diffVector = vector1.subtract(vector2);
        return diffVector.magnitude();
    }
}
