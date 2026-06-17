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


# Pre-requisites and setup:
This project has a collection of examples and each comes with a set of dependencies.
All these need to be managed properly to ensure that the examples run fine.
It primarily uses:
- Java 23
- Spring Framework 7
- SpringBoot 4.0
- Spring AI 2.0

And the various examples use:
- Postgres with pgvector extension (run as docker container)
- Weavite with img2vec-neural vectorization module (run as docker container)
- Local or cloud LLM (Currently using llama-3.1-8b-instant via Groq)

### Docker containers:
There are three docker compose files:
- one for Postgres and its admin console
- another one for Weaviate and its vectorization module
- and another one for Elasticsearch

### Setup Postgres
- Ensure you have the two containers running for postgres using docker compose (go to the directory and run `docker compose up -d`)
- Then go to Postgres Admin Console and create a Server. This would automatically create a database called 'vector_store'.
- Run the ddl.sql (located at /resources/sql). This will create necessary tables.

### Setup Weaviate
- Ensure you have the two containers running for Weaviate using docker compose (go to the directory and run `docker compose up -d`)
- No need for any setup after that.
- When you run the app, it should create the necessary classes with fields.

### Setup Elasticsearch
- Ensure you have a container running for Elasticsearch using docker compose (go to the directory and run `docker compose up -d`)


### Data
- Various examples use a lot of data. Mostly images (jpeg, png files).
- If you look at [DemoApplication](vectordb-examples/src/main/java/com/balarawool/vectordb/DemoApplication.java), you can see that these need to be in a specific structure. 
- As this is large binary data, I did not add it to the git repo. Please reach out to me via the socials (Bluesky or Twitter) if you need this data.
- Also, various methods in [DemoApplication](vectordb-examples/src/main/java/com/balarawool/vectordb/DemoApplication.java) make use of `initializeDb` fields to indicate if you want to extract vectors are store them in database.
So change them to `true` when running for the first time and subsequently to `false`.

### Python
- The last example can be run in two ways: Weaviate or a local-Python setup
- If you decide to use the Python-way (This is what is currently enabled. The Weaviate part of code is commented out.) then you need some more setup.
Check [FaceVectorCalculator.java](vectordb-examples/src/main/java/com/balarawool/vectordb/example7/FaceVectorCalculator.java) for details.


