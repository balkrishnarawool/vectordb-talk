package com.balarawool.vectordb.example7;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// TODO Improve face-image-to-vector conversion:
//  - Either wrap python script inside Java
//  - or run the model inside JVM
//  - or run the conversion as an independent application and expose as an api
///
/// Calculates vector for an image containing face
/// Uses Python3 and python-libraries deepface, tf-keras
/// Make sure Python3 is installed --> brew install python (and verify with python3 --version)
/// and that deepface and tf-keras are installed --> python3 -m pip install deepface tf-keras
public class FaceVectorCalculator {
    // TODO Use relative path instead of absolute path
    private static final String FACE_VECTOR_PY = "/Users/balkrishna/dev/java/github/vectordb-talk/vectordb-examples/src/main/python/face_vector.py";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<Double> calculateFaceVector(String imagePath) {
        List<Double> vectorResult = new ArrayList<>();

        try {
            // Build the exact execution command
            var json = getVectorAsJson(imagePath);
            // Parse returned JSON string data directly back to a native Java double array
            JsonNode rootNode = objectMapper.readTree(json);
            if (rootNode.has("error")) {
                throw new RuntimeException("DeepFace Error: " + rootNode.get("error").asText());
            }

            JsonNode vectorNode = rootNode.get("vector").get(0).get("embedding");
            if (vectorNode.isArray()) {
                for (JsonNode node : vectorNode) {
                    vectorResult.add(node.asDouble());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vectorResult;
    }

    private static String getVectorAsJson(String imagePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("python3", FACE_VECTOR_PY, imagePath);
        processBuilder.redirectErrorStream(true);

        // Execute process natively inside the OS runtime layer
        Process process = processBuilder.start();

        // Read output stream buffer lines from the script process
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python process execution failed. Output: " + output);
        }

        var i = output.indexOf("{");
        var json = output.substring(i);
        return json;
    }
}
