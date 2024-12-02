package com.balarawool.vectordb.example1;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorSpecies;

public interface VectorUtil {

    static int[] subtract(int[] a, int[] b){
        if (a.length != b.length) throw new IllegalStateException(String.format("The sizes of two vectors being processed are not same. One is %s and other is %s.", a.length, b.length));
        int[] result = new int[a.length];

        final VectorSpecies<Integer> species = IntVector.SPECIES_PREFERRED;
        int length = species.loopBound(result.length);
        for (int i = 0; i < length; i += species.length()) {
            IntVector va = IntVector.fromArray(species, a, i);
            IntVector vb = IntVector.fromArray(species, b, i);
            IntVector vc = va.sub(vb);
            vc.intoArray(result, i);
        }
        // Handle remaining elements
        for (int i = length; i < result.length; i++) {
            result[i] = a[i] - b[i];
        }

        return result;
    }
}
