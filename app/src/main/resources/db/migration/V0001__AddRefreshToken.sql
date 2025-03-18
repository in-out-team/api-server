create table if not exists refresh_tokens
(
    id         bigserial primary key,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    expires_at timestamp(6) with time zone,
    token      varchar(255) unique,
    user_id    bigint not null
);

create index if not exists idx_refresh_tokens_user_id
    on refresh_tokens (user_id);
