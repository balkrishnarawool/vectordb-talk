package com.balarawool.vectordb.db;

public class CosineSimilarityCalculator implements DistanceCalculator {
    public double distance(Vector vector1, Vector vector2) {
        if (vector1.embedding().length != vector2.embedding().length) {
            throw new IllegalStateException(String.format("The sizes of two vectors being processed are not same. One is %s and other is %s.", vector1.embedding().length, vector2.embedding().length));
        }
        return vector1.dotProduct(vector2) / (vector1.magnitude() * vector2.magnitude());
    }
}