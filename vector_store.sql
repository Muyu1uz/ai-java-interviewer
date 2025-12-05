create table if not exists vector_store
(
    id        text not null
        primary key,
    content   text,
    metadata  jsonb,
    embedding vector(1024)
);

alter table vector_store
    owner to postgres;

create index if not exists vector_store_embedding_idx
    on vector_store using ivfflat (embedding vector_cosine_ops);

