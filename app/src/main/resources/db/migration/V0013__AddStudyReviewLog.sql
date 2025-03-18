create table if not exists study_review_logs
(
    id                bigserial primary key,
    created_at        timestamp(9) with time zone,
    updated_at        timestamp(9) with time zone,
    study_id          bigint,
    rating            varchar(255),
    state             varchar(255),
    due               timestamp(9) with time zone,
    stability         double precision not null,
    difficulty        double precision not null,
    elapsed_days      integer          not null,
    last_elapsed_days integer          not null,
    scheduled_days    integer          not null,
    review            timestamp(9) with time zone
);

alter table study_review_logs
    add constraint fk_study_review_logs_study_id
        foreign key (study_id) references studies (id);
