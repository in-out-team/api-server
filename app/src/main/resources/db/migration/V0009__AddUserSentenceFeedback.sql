create table if not exists user_sentence_feedbacks
(
    id                   bigserial primary key,
    created_at           timestamp(6) with time zone,
    updated_at           timestamp(6) with time zone,
    sentence_feedback_id bigint not null,
    user_sentence_id     bigint not null
);

create index if not exists idx_user_sentence_feedbacks_user_sentence_id
    on user_sentence_feedbacks (user_sentence_id);
