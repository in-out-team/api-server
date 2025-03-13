create table if not exists users
(
    id         bigserial primary key,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    email      varchar(255) unique,
    nickname   varchar(255),
    password   varchar(255)
);
