alter table users
    add column roles jsonb not null default '["USER"]'::jsonb;

