create table if not exists daily_study_sets
(
    id         bigserial primary key,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    user_id    bigint   not null,
    date       date,
    study_ids  bigint[] not null
);

create unique index if not exists idx_daily_study_sets_user_id_date
    on daily_study_sets (user_id, date);
