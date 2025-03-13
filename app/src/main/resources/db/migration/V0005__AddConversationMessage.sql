create table if not exists conversation_messages
(
    id              bigserial primary key,
    created_at      timestamp(6) with time zone,
    updated_at      timestamp(6) with time zone,
    content         varchar(255),
    sender          varchar(255),
    audio_id        bigint,
    conversation_id bigint
);

alter table conversation_messages
    add constraint fk_conversation_messages_conversation_id
        foreign key (conversation_id) references conversations (id);
