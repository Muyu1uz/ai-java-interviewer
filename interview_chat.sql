create table if not exists interview_chat
(
    chat_id      bigserial
        primary key,
    user_id      bigint                                                       not null,
    resume_id    varchar(255)                                                 not null,
    status       varchar(32)              default 'ACTIVE'::character varying not null,
    created_time timestamp with time zone default now()                       not null,
    updated_time timestamp with time zone default now()                       not null
);

alter table interview_chat
    owner to postgres;

