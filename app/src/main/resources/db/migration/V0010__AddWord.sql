create table if not exists words
(
    id            bigserial primary key,
    created_at    timestamp(6) with time zone,
    updated_at    timestamp(6) with time zone,
    from_language varchar(255),
    name          varchar(255),
    to_language   varchar(255)
);

create unique index if not exists idx_words_name_from_language_to_language
    on words (name, from_language, to_language);
