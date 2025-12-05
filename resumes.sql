create table if not exists resumes
(
    id                     bigint       not null
        primary key,
    resume_id              varchar(255) not null,
    professional_knowledge text,
    project_experience     text,
    internship_experience  text,
    create_time            timestamp default CURRENT_TIMESTAMP,
    update_time            timestamp default CURRENT_TIMESTAMP
);

comment on table resumes is '简历信息表';

comment on column resumes.id is '主键ID';

comment on column resumes.resume_id is '简历ID';

comment on column resumes.professional_knowledge is '专业知识';

comment on column resumes.project_experience is '项目经验';

comment on column resumes.internship_experience is '实习经验';

comment on column resumes.create_time is '创建时间';

comment on column resumes.update_time is '更新时间';

alter table resumes
    owner to postgres;

create index if not exists idx_resumes_resume_id
    on resumes (resume_id);

create index if not exists idx_resumes_create_time
    on resumes (create_time);

