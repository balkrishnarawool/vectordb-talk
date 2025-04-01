package com.balarawool.vectordb.example3;

import com.balarawool.vectordb.db.CosineSimilarityCalculator;
import com.balarawool.vectordb.db.Vector;
import com.balarawool.vectordb.db.VectorDB;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;
import org.nd4j.common.io.ClassPathResource;
import org.nd4j.shade.guava.io.Files;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@Service
public class WikiWord2Vec {
    private static int K = 10;
    private static final String VECTOR_FILE = "/Users/TS90XD/dev/java/vectordb/vectordb-talk/vectordb-talk/simple-vectordb-sb/src/main/resources/data/vectors_wiki4_new_v2.txt";
    private static final String DATA_FILE = "data/wiki_4pages.txt";

    private VectorDB<String> vdb = null;

    public WikiWord2Vec() throws IOException {
        initializeDb();
    }

    public record Embedding(String word, double[] vector) { }

    public Embedding embedding(String word) throws IOException {
        var vector = vdb.selectByData(word);
        return new Embedding(word, vector.embedding());
    }

    public record Entry(String word, double distance) { }
    public List<Entry> nearestNeighbours(String word) throws IOException {
        return vdb.kNearestNeighbours(vdb.selectByData(word), K, new CosineSimilarityCalculator())
                .stream()
                .map(t -> new Entry(t.entry().getValue(), t.distance()))
                .toList();
    }

    public List<Entry> equation(String start, String toSubtract, String toAdd) throws IOException {
        var vectorStart = vdb.selectByData(start);
        var vectorToSubtract = vdb.selectByData(toSubtract);
        var vectorToAdd = vdb.selectByData(toAdd);
        var vector = vectorStart.subtract(vectorToSubtract).add(vectorToAdd);
        return vdb.kNearestNeighbours(vector, K, new CosineSimilarityCalculator())
                .stream()
                .map(t -> new Entry(t.entry().getValue(), t.distance()))
                .toList();
    }

    private void initializeDb() throws IOException {
        vdb = VectorDB.create();
        var vectorFile = new File(VECTOR_FILE);
        if (!vectorFile.exists()) {
            createAndStoreVectors();
        }
        for (var line: Files.readLines(vectorFile, Charset.defaultCharset())) {
            var strs = line.split(" ");
            if (strs.length > 3) {
                var word = strs[0];
                var embedding = new double[strs.length - 1];
                for (int i = 1; i < strs.length; i++) {
                    embedding[i - 1] = Double.parseDouble(strs[i]);
                }
                vdb.insert(word, new Vector(embedding));
            }
        }
    }

    private static void createAndStoreVectors() throws IOException {
        // Gets Path to Text file
        String filePath = new ClassPathResource(DATA_FILE).getFile().getAbsolutePath();

        System.out.println("Load & Vectorize Sentences....");
        // Strip white space before and after for each line
        SentenceIterator iter = new BasicLineIterator(filePath);
        // Split on white spaces in the line to get words
        TokenizerFactory t = new DefaultTokenizerFactory();

        /*
            CommonPreprocessor will apply the following regex to each token: [\d\.:,"'\(\)\[\]|/?!;]+
            So, effectively all numbers, punctuation symbols and some special symbols are stripped off.
            Additionally it forces lower case for all tokens.
         */
        t.setTokenPreProcessor(new CommonPreprocessor());

        System.out.println("Building model....");
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();

        System.out.println("Fitting Word2Vec model....");
        vec.fit();

        System.out.println("Writing word vectors to text file....");

        // Write word vectors to file
        WordVectorSerializer.writeWordVectors(vec, VECTOR_FILE);
    }
}
