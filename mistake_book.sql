create table if not exists mistake_book
(
    id               bigint     not null
        primary key,
    user_id          bigint                                                     not null,
    question_content varchar(126)                                               not null,
    user_answer      varchar(126),
    created_at       timestamp default CURRENT_TIMESTAMP,
    updated_at       timestamp default CURRENT_TIMESTAMP
);

alter table mistake_book
    owner to postgres;

create index if not exists idx_mistake_book_user_id
    on mistake_book (user_id);

