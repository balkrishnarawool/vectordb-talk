-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- You can verify that the extensions are installed by using Admin Console
-- and by going to the database vector_store and clicking Extensions.

-- Table: public.math_big_vector_store

-- DROP TABLE IF EXISTS public.math_big_vector_store;

CREATE TABLE IF NOT EXISTS public.math_big_vector_store
(
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    content text COLLATE pg_catalog."default",
    metadata json,
    embedding vector(768),
    CONSTRAINT math_big_vector_store_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.math_big_vector_store
    OWNER to postgres;
-- Index: math_big_vector_store_embedding_idx

-- DROP INDEX IF EXISTS public.math_big_vector_store_embedding_idx;

CREATE INDEX IF NOT EXISTS math_big_vector_store_embedding_idx
    ON public.math_big_vector_store USING hnsw
    (embedding vector_cosine_ops)
    TABLESPACE pg_default;


-- Table: public.epic_vector_store

-- DROP TABLE IF EXISTS public.epic_vector_store;

CREATE TABLE IF NOT EXISTS public.epic_vector_store
(
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    content text COLLATE pg_catalog."default",
    orig_content text COLLATE pg_catalog."default",
    embedding vector(768),
    CONSTRAINT epic_vector_store_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.epic_vector_store
    OWNER to postgres;
-- Index: epic_vector_store_embedding_idx

-- DROP INDEX IF EXISTS public.epic_vector_store_embedding_idx;

CREATE INDEX IF NOT EXISTS epic_vector_store_embedding_idx
    ON public.epic_vector_store USING hnsw
    (embedding vector_cosine_ops)
    TABLESPACE pg_default;


-- Table: public.celebrity

-- DROP TABLE IF EXISTS public.celebrity;

CREATE TABLE IF NOT EXISTS public.celebrity
(
    id uuid NOT NULL DEFAULT uuid_generate_v4(),
    filename text COLLATE pg_catalog."default",
    content text COLLATE pg_catalog."default",
    embedding vector(512),
    CONSTRAINT celebrity_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.celebrity
    OWNER to postgres;
-- Index: celebrity_embedding_idx

-- DROP INDEX IF EXISTS public.celebrity_embedding_idx;

CREATE INDEX IF NOT EXISTS celebrity_embedding_idx
    ON public.celebrity USING hnsw
    (embedding vector_cosine_ops)
    TABLESPACE pg_default;