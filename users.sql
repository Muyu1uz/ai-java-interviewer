create table if not exists users
(
    id          bigint       not null
        primary key,
    username    varchar(255) not null
        unique,
    password    varchar(255) not null,
    useraccount varchar(255) not null
        constraint users_pk_useraccount
            unique,
    resume_id   varchar(255)
);

comment on table users is '用户信息表';

comment on column users.id is '用户主键ID（雪花ID）';

comment on column users.username is '用户名';

comment on column users.password is '密码';

comment on column users.useraccount is '用户账号';

comment on column users.resume_id is '关联简历ID';

alter table users
    owner to postgres;

create unique index if not exists idx_users_username
    on users (username);

create index if not exists idx_users_resume_id
    on users (resume_id);

create index if not exists users_useraccount_index
    on users (useraccount);

