package com.balarawool.vectordb.db;

import java.util.Arrays;

public record Vector(double[] embedding) {
    @Override public String toString() { return Arrays.toString(embedding); }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Vector(double[] embedding)) return Arrays.equals(this.embedding, embedding);
        return false;
    }
}
