package com.balarawool.vectordb.db;

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
/// @param <V> - Type of vector
public class VectorDB<D, V extends Vector> {

    private Map<V, D> db = new HashMap<>();

    private VectorDB() {
    }

    public static <D, T extends Vector> VectorDB<D, T> create() {
        return new VectorDB<>();
    }

    /// Make sure the embeddings/vectors are unique, otherwise they are overwritten.
    public void insert(D data, V embedding) {
        db.put(embedding, data);
    }

    public Set<Map.Entry<V, D>> selectAll() {
        return db.entrySet();
    }

    public Map.Entry<V, D> selectByVector(V vector) {
        return db.entrySet().stream()
                .filter(e -> e.getKey().equals(vector))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Entry not found for vector %s", vector)));
    }

    public Map.Entry<V, D> selectByData(D data) {
        return db.entrySet().stream()
                .filter(e -> e.getValue().equals(data))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Entry not found for data %s", data)));
    }

    public record Tuple<T extends Vector, D>(Map.Entry<T, D> entry, double distance) { }
    public List<Tuple<V, D>> kNearestNeighbours(V vector, int k, DistanceCalculator<V> distanceCalculator){
        return db.entrySet().stream()
                .map(e -> new Tuple<>(e, distance(vector, e.getKey(), distanceCalculator)))
                .sorted(sort(distanceCalculator))
                .limit(k)
                .toList();
    }

    private Comparator<? super Tuple<V,D>> sort(DistanceCalculator<V> distanceCalculator) {
        // This is added because OneDimensionDistanceCalculator values range from 0 to infinity when vectors are most to least similar.
        // For others, they range from 1 to -1 when vectors are most to least similar.
        return distanceCalculator instanceof OneDimensionDistanceCalculator<V>
                ? Comparator.comparing(Tuple::distance)
                : Collections.reverseOrder(Comparator.comparing(Tuple::distance));
    }

    private double distance(V vector1, V vector2, DistanceCalculator<V> distanceCalculator) {
        return distanceCalculator.distance(vector1, vector2);
    }
}
