package com.balarawool.vectordb.db;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;

/// A vector of double-values
/// TODO: See if it should be a record
/// because we have overridden equals(), hashCode() and toString().
/// The only value it adds by being a record is constructor.
/// Also the data being an array is prone to external mutations.
/// We could avoid this by cloning the array but it's not done currently
/// because it would make the code bulky
/// and vectors are only used read-only in this codebase anyways.
public record Vector(double[] embedding) {
    @Override
    public String toString() {
        return Arrays.toString(embedding);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector(double[] embedding)) return Arrays.equals(this.embedding, embedding);
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(embedding);
    }

    public Vector add(Vector toAdd) {
        var a = this.embedding;
        var b = toAdd.embedding();

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

        return new Vector(result);
    }

    public Vector subtract(Vector toSubtract) {
        var a = this.embedding;
        var b = toSubtract.embedding();

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

        return new Vector(result);
    }

    public double dotProduct(Vector toProduct) {
        var a = this.embedding;
        var b = toProduct.embedding();

        if (a.length != b.length) throw new IllegalStateException(String.format("The sizes of two vectors being processed are not same. One is %s and other is %s.", a.length, b.length));
        double[] result = new double[a.length];

        final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
        int length = species.loopBound(result.length);
        for (int i = 0; i < length; i += species.length()) {
            DoubleVector va = DoubleVector.fromArray(species, a, i);
            DoubleVector vb = DoubleVector.fromArray(species, b, i);
            DoubleVector vc = va.mul(vb);
            vc.intoArray(result, i);
        }
        // Handle remaining elements
        for (int i = length; i < result.length; i++) {
            result[i] = a[i] * b[i];
        }
        return Arrays.stream(result).sum();
    }

    public double magnitude() {
        double[] result = new double[embedding.length];

        final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
        int length = species.loopBound(result.length);
        for (int i = 0; i < length; i += species.length()) {
            DoubleVector va = DoubleVector.fromArray(species, embedding, i);
            DoubleVector vc = va.mul(va);
            vc.intoArray(result, i);
        }
        // Handle remaining elements
        for (int i = length; i < result.length; i++) {
            result[i] = embedding[i] * embedding[i];
        }
        return Math.sqrt(Arrays.stream(result).sum());
    }

    public Vector normalize() {
        var v = magnitude();

        double[] result = new double[embedding.length];
        final VectorSpecies<Double> species = DoubleVector.SPECIES_PREFERRED;
        int length = species.loopBound(result.length);
        for (int i = 0; i < length; i += species.length()) {
            DoubleVector va = DoubleVector.fromArray(species, embedding, i);
            DoubleVector vc = va.div(v);
            vc.intoArray(result, i);
        }
        // Handle remaining elements
        for (int i = length; i < result.length; i++) {
            result[i] = embedding[i] / v;
        }

        return new Vector(result);
    }

}
