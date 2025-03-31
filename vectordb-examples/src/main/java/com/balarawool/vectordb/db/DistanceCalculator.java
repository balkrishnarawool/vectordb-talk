package com.balarawool.vectordb.db;

public interface DistanceCalculator {
    double distance(Vector vector1, Vector vector2);
}
