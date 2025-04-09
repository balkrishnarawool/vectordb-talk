package com.balarawool.vectordb.db;

public class EuclideanDistanceCalculator implements DistanceCalculator{
    @Override
    public double distance(Vector vector1, Vector vector2) {
        return vector1.euclideanDistance(vector2);
    }
}
