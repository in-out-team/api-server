create table if not exists sentences
(
    id                 bigserial primary key,
    created_at         timestamp(9) with time zone,
    updated_at         timestamp(9) with time zone,
    word_definition_id bigint not null,
    type               varchar(255),
    content            varchar(255),
    translation        varchar(255),
    lexical_categories jsonb
);

create index if not exists idx_sentences_word_definition_id
    on sentences (word_definition_id);
