create table if not exists user_sentences
(
    id                 bigserial primary key,
    created_at         timestamp(6) with time zone,
    updated_at         timestamp(6) with time zone,
    user_id            bigint not null,
    word_definition_id bigint not null,
    type               varchar(255),
    sentence_id        bigint not null
);

create index idx_user_sentences_user_id_word_definition_id
    on user_sentences (user_id, word_definition_id);

create index idx_user_sentences_user_id_sentence_id
    on user_sentences (user_id, sentence_id);
