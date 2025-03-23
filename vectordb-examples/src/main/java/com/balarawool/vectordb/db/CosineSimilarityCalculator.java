package com.balarawool.vectordb.db;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;

public class CosineSimilarityCalculator<V extends Vector> implements DistanceCalculator<V>{
    public double distance(V vector1, V vector2) {
        if (vector1 instanceof Vector.Double(double[] embedding1) && vector2 instanceof Vector.Double(double[] embedding2)) {
            if (embedding1.length != embedding2.length)
                throw new IllegalStateException(String.format("The sizes of two vectors being processed are not same. One is %s and other is %s.", embedding1.length, embedding2.length));
            return dotProduct(embedding1, embedding2) / (magnitude(embedding1) * magnitude(embedding2));
        }
        throw new IllegalStateException("Unsupported vector type. Only supported type of vectors is double[]");
    }

    public static double dotProduct(double[] vector1, double[] vector2) {
        if (vector1.length != vector2.length) throw new IllegalStateException(String.format("The sizes of two vectors being processed are not same. One is %s and other is %s.", vector1.length, vector2.length));
        double[] result = new double[vector1.length];

        final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
        int length = species.loopBound(result.length);
        for (int i = 0; i < length; i += species.length()) {
            DoubleVector va = DoubleVector.fromArray(species, vector1, i);
            DoubleVector vb = DoubleVector.fromArray(species, vector2, i);
            DoubleVector vc = va.mul(vb);
            vc.intoArray(result, i);
        }
        // Handle remaining elements
        for (int i = length; i < result.length; i++) {
            result[i] = vector1[i] * vector2[i];
        }
        return Arrays.stream(result).sum();
    }

    static public double magnitude(double[] vector) {
        double[] result = new double[vector.length];

        final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
        int length = species.loopBound(result.length);
        for (int i = 0; i < length; i += species.length()) {
            DoubleVector va = DoubleVector.fromArray(species, vector, i);
            DoubleVector vc = va.mul(va);
            vc.intoArray(result, i);
        }
        // Handle remaining elements
        for (int i = length; i < result.length; i++) {
            result[i] = vector[i] * vector[i];
        }
        return Math.sqrt(Arrays.stream(result).sum());
    }
}
