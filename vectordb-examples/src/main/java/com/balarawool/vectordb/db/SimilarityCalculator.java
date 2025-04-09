package com.balarawool.vectordb.db;

public interface SimilarityCalculator {
    double similarity(Vector vector1, Vector vector2);
}
