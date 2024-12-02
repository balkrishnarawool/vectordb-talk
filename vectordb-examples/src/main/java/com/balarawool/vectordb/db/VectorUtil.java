package com.balarawool.vectordb.db;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

public interface VectorUtil {

    static double[] add(double[] a, double[] b){
        if (a.length != b.length) throw new IllegalStateException(String.format("The sizes of two vectors being processed are not same. One is %s and other is %s.", a.length, b.length));
        double[] result = new double[a.length];

        final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
        int length = species.loopBound(result.length);
        for (int i = 0; i < length; i += species.length()) {
            DoubleVector va = DoubleVector.fromArray(species, a, i);
            DoubleVector vb = DoubleVector.fromArray(species, b, i);
            DoubleVector vc = va.add(vb);
            vc.intoArray(result, i);
        }
        // Handle remaining elements
        for (int i = length; i < result.length; i++) {
            result[i] = a[i] - b[i];
        }

        return result;
    }

    static double[] subtract(double[] a, double[] b){
        if (a.length != b.length) throw new IllegalStateException(String.format("The sizes of two vectors being processed are not same. One is %s and other is %s.", a.length, b.length));
        double[] result = new double[a.length];

        final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
        int length = species.loopBound(result.length);
        for (int i = 0; i < length; i += species.length()) {
            DoubleVector va = DoubleVector.fromArray(species, a, i);
            DoubleVector vb = DoubleVector.fromArray(species, b, i);
            DoubleVector vc = va.sub(vb);
            vc.intoArray(result, i);
        }
        // Handle remaining elements
        for (int i = length; i < result.length; i++) {
            result[i] = a[i] - b[i];
        }

        return result;
    }
}
