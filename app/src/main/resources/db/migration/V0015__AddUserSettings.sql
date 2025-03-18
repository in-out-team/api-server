alter table users
    add column study_language varchar(255) default 'ENGLISH';

alter table users
    add column native_language varchar(255) default 'KOREAN';

alter table users
    add column study_per_day int default 5;

alter table users
    add column timezone varchar(255) default 'Asia/Seoul';
