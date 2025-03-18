create table if not exists sentence_feedbacks
(
    id                bigserial primary key,
    created_at        timestamp(9) with time zone,
    updated_at        timestamp(9) with time zone,
    feedback          varchar(255),
    sentence_id       bigint not null,
    submitted_content varchar(255)
);

create index if not exists idx_sentence_feedbacks_sentence_id
    on sentence_feedbacks (sentence_id);
