# Introduction
This is source repo for may talk on vector databases: Making Sense of Vector Databases

This talk creates a tiny Vector Database using Java's Vector API. [Tiny Vector DB](vectordb-examples/src/main/java/com/balarawool/vectordb/db/VectorDB.java)

It introduces vectors with their mathematical representation and adds several concepts to it.
While doing that it shows various examples with increasing complexity which include these concpets.

Here's a list of examples discussed:
- Example 1: [Simple vectors: Grayscale colors](vectordb-examples/src/main/java/com/balarawool/vectordb/example1/GrayController.java)
- Example 2: [Cosine Similarity: RGB colors](vectordb-examples/src/main/java/com/balarawool/vectordb/example2/RgbController.java)
- Example 3: [word2vec Embeddings](vectordb-examples/src/main/java/com/balarawool/vectordb/example3/WikiController.java)
- Example 4: [HNSW Indexing](vectordb-examples/src/main/java/com/balarawool/vectordb/example4/BigWikiController.java)
- Example 5: [RAG](vectordb-examples/src/main/java/com/balarawool/vectordb/example5/EpicComicController.java)
- Example 6: [img2vec Embeddings](vectordb-examples/src/main/java/com/balarawool/vectordb/example6/FroogleSearchController.java)
- Example 7: [Surprise!](vectordb-examples/src/main/java/com/balarawool/vectordb/example7/CelebritySearchController.java) 

For any questions reach out to me here: [@balarawool.bsky.social](https://bsky.app/profile/balarawool.bsky.social)

Note:
The project uses GraphQL plugin from IntelliJ: https://plugins.jetbrains.com/plugin/8097-graphql
So, if you want to run the .graphql file, do make sure it is installed.
