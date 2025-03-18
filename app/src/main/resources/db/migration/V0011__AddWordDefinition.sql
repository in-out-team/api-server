create table if not exists word_definitions
(
    id               bigserial primary key,
    created_at       timestamp(9) with time zone,
    updated_at       timestamp(9) with time zone,
    word_id          bigint,
    lexical_category varchar(255),
    meaning          varchar(255),
    pre_context      varchar(255)
);

alter table word_definitions
    add constraint fk_word_definitions_word_id
        foreign key (word_id) references words (id);
