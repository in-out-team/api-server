create table if not exists ai_audios
(
    id            bigserial primary key,
    created_at    timestamp(9) with time zone,
    updated_at    timestamp(9) with time zone,
    language      varchar(255),
    voice_type    varchar(255),
    content       varchar(255),
    directory     varchar(255)
);

create unique index if not exists idx_ai_audios_language_voice_type_content
    on ai_audios (language, voice_type, content);
