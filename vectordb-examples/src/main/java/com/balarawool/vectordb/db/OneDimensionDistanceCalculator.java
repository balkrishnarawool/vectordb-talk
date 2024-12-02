package com.balarawool.vectordb.db;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;

public class OneDimensionDistanceCalculator<T> implements DistanceCalculator<T>{
    @Override
    public double distance(T vector1, T vector2) {
        if (vector1 instanceof int[] v1 && vector2 instanceof int[] v2) {
            if (v1.length != 1 || v2.length != 1) {
                throw new IllegalStateException(String.format("The sizes of two vectors being processed by OneDimensionDistanceCalculator has to be 1." +
                        "Sizes of them are %s and %s.", v1.length, v2.length));
            }
            return Math.abs(v1[0] - v2[0]);
        } else {
            if (vector1 instanceof double[] v1 && vector2 instanceof double[] v2) {
                if (v1.length != 1 || v2.length != 1) {
                    throw new IllegalStateException(String.format("The sizes of two vectors being processed by OneDimensionDistanceCalculator has to be 1." +
                            "Sizes of them are %s and %s.", v1.length, v2.length));
                }
                return Math.abs(v1[0] - v2[0]);
            }
        }
        throw new IllegalStateException("Unsupported vector type. Only supported type of vectors are int[] and double[]");
    }
}
