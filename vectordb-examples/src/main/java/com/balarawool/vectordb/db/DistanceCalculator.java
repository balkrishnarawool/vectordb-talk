package com.balarawool.vectordb.db;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;

public interface DistanceCalculator<T> {
    double distance(T vector1, T vector2);
}
