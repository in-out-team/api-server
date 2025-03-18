create table if not exists conversations
(
    id                 bigserial primary key,
    created_at         timestamp(6) with time zone,
    updated_at         timestamp(6) with time zone,
    user_id            bigint not null,
    word_definition_id bigint not null
);

create index if not exists idx_conversations_user_id_word_definition_id
    on conversations (user_id, word_definition_id);
