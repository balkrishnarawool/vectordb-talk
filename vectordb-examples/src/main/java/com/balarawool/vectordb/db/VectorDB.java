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
/// - delete an existing entry
/// (Note that update is not a separate method, if you want to update, delete existing entry and insert a new one)
///
/// @param <D> - Type of data
public class VectorDB<D> {

    private Map<Vector, D> db = new HashMap<>();

    private VectorDB() {
    }

    public static <D> VectorDB<D> create() {
        return new VectorDB<>();
    }

    /// Make sure the embeddings/vectors are unique, otherwise they are overwritten.
    public void insert(D data, Vector embedding) {
        db.put(embedding, data);
    }

    public Set<Map.Entry<Vector, D>> selectAll() {
        return db.entrySet();
    }

    public D selectByVector(Vector vector) {
        return db.entrySet().stream()
                .filter(e -> e.getKey().equals(vector))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalStateException(String.format("Entry not found for vector %s", vector)));
    }

    public Vector selectByData(D data) {
        return db.entrySet().stream()
                .filter(e -> e.getValue().equals(data))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException(String.format("Entry not found for data %s", data)));
    }

    /// Deletes given vector and returns corresponding data.
    public D deleteByVector(Vector vector) {
        return db.remove(vector);
    }

    ///  Deletes first data element (and vector) that is same as the input and returns the removed data.
    public D deleteByData(D data) {
         var result = db.entrySet().stream()
                .filter(e -> e.getValue().equals(data))
                .findFirst();
         return result.map(e -> db.remove(e.getKey()))
                 .orElseThrow(() -> new IllegalStateException(String.format("Entry not found for data %s", data)));
    }

    public record Tuple<D>(Map.Entry<Vector, D> entry, double distance) { }

    /// Returns k number of nearest neighbours of a given vector using given distance-calculator.
    public List<Tuple<D>> kNearestNeighbours(Vector vector, int k, DistanceCalculator distanceCalculator){
        return db.entrySet().stream()
                .map(e -> new Tuple<>(e, distance(vector, e.getKey(), distanceCalculator)))
                .sorted(sort(distanceCalculator))
                .limit(k)
                .toList();
    }

    private Comparator<? super Tuple<D>> sort(DistanceCalculator distanceCalculator) {
        // This is added because OneDimensionDistanceCalculator values range from 0 to infinity when vectors are most to least similar.
        // For others, they range from 1 to -1 when vectors are most to least similar.
        return distanceCalculator instanceof ScalarDistanceCalculator
                ? Comparator.comparing(Tuple::distance)
                : Collections.reverseOrder(Comparator.comparing(Tuple::distance));
    }

    private double distance(Vector vector1, Vector vector2, DistanceCalculator distanceCalculator) {
        return distanceCalculator.distance(vector1, vector2);
    }
}
