-- list of all tables in my db
SELECT *
FROM information_schema.tables
     WHERE table_type = 'BASE TABLE'
           AND table_schema = 'public'
ORDER BY table_type, table_name;
