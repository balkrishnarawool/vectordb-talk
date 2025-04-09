package com.balarawool.vectordb.db;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparing;

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

    public record Tuple<D>(Map.Entry<Vector, D> entry, double d) { }

    /// Returns k number of nearest neighbours of a given vector using given similarity-calculator.
    public List<Tuple<D>> kNearestNeighbours(Vector vector, int k, SimilarityCalculator similarityCalculator){
        return db.entrySet().stream()
                .map(e -> new Tuple<>(e, similarityCalculator.similarity(vector, e.getKey())))
                .sorted(reverseOrder(comparing(Tuple::d)))
                .limit(k)
                .toList();
    }

    /// Returns k number of nearest neighbours of a given vector using given distance-calculator.
    public List<Tuple<D>> kNearestNeighbours(Vector vector, int k, DistanceCalculator distanceCalculator){
        return db.entrySet().stream()
                .map(e -> new Tuple<>(e, distanceCalculator.distance(vector, e.getKey())))
                .sorted(comparing(Tuple::d))
                .limit(k)
                .toList();
    }
}
