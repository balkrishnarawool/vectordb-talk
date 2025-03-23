package com.balarawool.vectordb.db;

public interface DistanceCalculator<V extends Vector> {
    double distance(V vector1, V vector2);
}
