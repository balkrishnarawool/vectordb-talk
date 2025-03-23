package com.balarawool.vectordb.db;

import java.util.Arrays;

public sealed interface Vector {
    record Int(int[] embedding) implements Vector {
        @Override public String toString() { return Arrays.toString(embedding); }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Int(int[] embedding)) return Arrays.equals(this.embedding, embedding);
            return false;
        }
    }

    record Double(double[] embedding) implements Vector {
        @Override public String toString() { return Arrays.toString(embedding); }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Double(double[] embedding)) return Arrays.equals(this.embedding, embedding);
            return false;
        }
    }
}
