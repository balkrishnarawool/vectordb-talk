package com.balarawool.vectordb.db;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/// Simple Vector Database implementation.
/// Stores data <-> embedding mapping in an in-memory store.
/// Provides functions to
/// - create a new database (table),
/// - insert new entry
/// - select all entries
/// - select specific entry
/// - select nearest neighbours tuples for a given vector
/// - update/delete an existing entry TODO: To be added
///
/// @param <D> - Type of data
/// @param <T> - Type of vector
public class VectorDB<D, T> {

    private Map<T, D> db = new HashMap<>();

    private VectorDB() {
    }

    public static <D, T> VectorDB<D, T> create() {
        return new VectorDB<>();
    }

    /// Make sure the embeddings/vectors are unique, otherwise they are overwritten.
    public void insert(D data, T embedding) {
        db.put(embedding, data);
    }

    public Set<Map.Entry<T, D>> selectAll() {
        return db.entrySet();
    }

    public Map.Entry<T, D> selectByVector(T vector) {
        return db.entrySet().stream()
                .filter(e -> isEqual(e.getKey(), vector))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Entry not found for vector %s", Arrays.toString((Object[])vector))));
    }

    public Map.Entry<T, D> selectByData(D data) {
        return db.entrySet().stream()
                .filter(e -> e.getValue().equals(data))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Entry not found for data %s", data)));
    }

    private boolean isEqual(T key, T vector) {
        if (key instanceof int[] ka && vector instanceof int[] va)
            return Arrays.equals(ka, va);
        if (key instanceof double[] ka && vector instanceof double[] va)
            return Arrays.equals(ka, va);
        throw new IllegalStateException("Unsupported vector type. Only supported type of vectors are int[] and double[]");
    }

    public record Tuple<T, D>(Map.Entry<T, D> entry, double distance) { }
    public List<Tuple<T, D>> kNearestNeighbours(T vector, int k, DistanceCalculator<T> distanceCalculator){
        return db.entrySet().stream()
                .map(e -> new Tuple<>(e, distance(vector, e.getKey(), distanceCalculator)))
                .sorted(sort(distanceCalculator))
                .limit(k)
                .toList();
    }

    private Comparator<? super Tuple<T,D>> sort(DistanceCalculator<T> distanceCalculator) {
        // This is added because OneDimensionDistanceCalculator values range from 0 to infinity when vectors are most to least similar.
        // For others, they range from 1 to -1 when vectors are most to least similar.
        return distanceCalculator instanceof OneDimensionDistanceCalculator<T>
                ? Comparator.comparing(Tuple::distance)
                : Collections.reverseOrder(Comparator.comparing(Tuple::distance));
    }

    private double distance(T vector1, T vector2, DistanceCalculator<T> distanceCalculator) {
        return distanceCalculator.distance(vector1, vector2);
    }
}
