#!/bin/bash
set -e

psql -U "$POSTGRES_USER" -d postgres -tc "
    SELECT 1
    FROM pg_database
    WHERE datname = 'in-out';
" | grep -q 1 || psql -U "$POSTGRES_USER" -d postgres -c "
    CREATE DATABASE \"in-out\";
"

psql -U "$POSTGRES_USER" -d postgres -tc "
    SELECT 1
    FROM pg_database
    WHERE datname = 'in-out-jobrunr-local';
" | grep -q 1 || psql -U "$POSTGRES_USER" -d postgres -c "
    CREATE DATABASE \"in-out-jobrunr-local\";
"
