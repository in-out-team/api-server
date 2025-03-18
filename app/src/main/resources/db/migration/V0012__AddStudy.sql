create table if not exists studies
(
    id                 bigserial primary key,
    created_at         timestamp(9) with time zone,
    updated_at         timestamp(9) with time zone,
    user_id            bigint           not null,
    word_definition_id bigint           not null,
    state              varchar(255),
    due                timestamp(9) with time zone,
    stability          double precision not null,
    difficulty         double precision not null,
    elapsed_days       integer          not null,
    scheduled_days     integer          not null,
    reps               integer          not null,
    lapses             integer          not null,
    last_review        timestamp(9) with time zone
);

create unique index if not exists idx_studies_user_id_word_definition_id
    on studies (user_id, word_definition_id);
