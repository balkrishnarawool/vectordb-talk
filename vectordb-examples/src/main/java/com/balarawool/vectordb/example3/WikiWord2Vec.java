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
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class WikiWord2Vec {
    private static int K = 10;
    private static final String DATA_FILE = "src/main/resources/data/wiki_4pages.txt";
    private static final String VECTOR_FILE = "src/main/resources/data/vectors_wiki4_google_w2v.txt";
    private static final String GOOGLE_W2V_FILE = "google_w2v/GoogleNews-vectors-negative300.bin.gz";

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

    public List<Entry> nearestNeighbours(String word) {
        return vdb.kNearestNeighbours(vdb.selectByData(word), K, new CosineSimilarityCalculator())
                .stream()
                .map(t -> new Entry(t.entry().getValue(), t.d()))
                .toList();
    }

    public List<Entry> equation(String start, String toSubtract, String toAdd) throws IOException {
        var vectorStart = vdb.selectByData(start);
        var vectorToSubtract = vdb.selectByData(toSubtract);
        var vectorToAdd = vdb.selectByData(toAdd);
        var vector = vectorStart.subtract(vectorToSubtract).add(vectorToAdd);
        return vdb.kNearestNeighbours(vector, K, new CosineSimilarityCalculator())
                .stream()
                .map(t -> new Entry(t.entry().getValue(), t.d()))
                .toList();
    }

    private void initializeDb() throws IOException {
        vdb = VectorDB.create();
        var vectorFile = Path.of(VECTOR_FILE);

        if (!Files.exists(vectorFile)) {
            createAndStoreVectors2();
        }

        System.out.println(vectorFile.toAbsolutePath());
        AtomicInteger n = new AtomicInteger();
        Files.lines(vectorFile, Charset.defaultCharset()).forEach(line -> {
            var strs = line.split(" ");
            if (strs.length > 3) {
                var word = strs[0];
                var embedding = new double[strs.length - 1];
                for (int i = 1; i < strs.length; i++) {
                    embedding[i - 1] = Double.parseDouble(strs[i]);
                }
                vdb.insert(word, new Vector(embedding));
                n.getAndIncrement();
            }
        });
        System.out.println("Total words in DB: " + n.get());
    }

    // This method is for old implementation which used DL4J's own word2vec
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

    // This is new implementation which uses GoogleNewsWord2Vec
    private static void createAndStoreVectors2() throws IOException {
        AtomicReference<File> modelFile = new AtomicReference<>();
        AtomicReference<Word2Vec> vec = new AtomicReference<>();
        Thread thread = new Thread(() -> {
            try {
                modelFile.set(new ClassPathResource(GOOGLE_W2V_FILE).getFile());
                vec.set(WordVectorSerializer.readWord2VecModel(modelFile.get()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Start the background thread
        thread.start();

        while (thread.isAlive()) {
            System.out.println("Loading Google News Word2Vec model...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Done loading Google News Word2Vec model.");

        var dataFile = Path.of(DATA_FILE);//new ClassPathResource(DATA_FILE).getFile();
        var vectorFile = Path.of(VECTOR_FILE);
        var writer = Files.newBufferedWriter(vectorFile);

        Files.lines(dataFile, Charset.defaultCharset())
                .forEach(line -> {
                    AtomicInteger i = new AtomicInteger();
                        Arrays.stream(line.split(" "))
                        .forEach(s -> {
                            try {
                                if (!s.isEmpty()) {
                                    var vector = vec.get().getWordVector(s.toLowerCase());
                                    writer.write(s.toLowerCase() + " "); System.out.print(s.toLowerCase() + " ");
                                    for (var d: vector) { writer.write(d + " "); System.out.print(d + " "); }
                                    writer.newLine(); System.out.println();
                                    i.getAndIncrement();
                                    System.out.println("Words: " + i);
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                });
    }
}
