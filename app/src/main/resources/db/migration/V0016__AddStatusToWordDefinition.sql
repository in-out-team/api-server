alter table word_definitions
    add column status varchar(255) not null default 'PENDING';
