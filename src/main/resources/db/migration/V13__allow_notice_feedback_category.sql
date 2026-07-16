-- Existing production schemas were created before Flyway was introduced.
-- Hibernate can create a CHECK constraint for EnumType.STRING columns, so the
-- pre-NOTICE constraint must be replaced explicitly rather than relying on
-- ddl-auto=update.
DO $$
DECLARE
    category_check RECORD;
BEGIN
    -- A fresh database is created by Hibernate after Flyway runs. In that case
    -- there is no legacy constraint to repair.
    IF to_regclass('public.feedback_posts') IS NULL THEN
        RETURN;
    END IF;

    -- PostgreSQL generates the name for Hibernate-created enum checks, so do
    -- not assume a particular legacy constraint name.
    FOR category_check IN
        SELECT constraint_row.conname
        FROM pg_constraint AS constraint_row
        INNER JOIN pg_class AS table_row ON table_row.oid = constraint_row.conrelid
        INNER JOIN pg_namespace AS schema_row ON schema_row.oid = table_row.relnamespace
        INNER JOIN pg_attribute AS column_row
            ON column_row.attrelid = table_row.oid
            AND column_row.attnum = ANY (constraint_row.conkey)
        WHERE constraint_row.contype = 'c'
          AND schema_row.nspname = 'public'
          AND table_row.relname = 'feedback_posts'
          AND column_row.attname = 'category'
    LOOP
        EXECUTE format(
            'ALTER TABLE public.feedback_posts DROP CONSTRAINT %I',
            category_check.conname
        );
    END LOOP;

    ALTER TABLE public.feedback_posts
        ADD CONSTRAINT feedback_posts_category_check
        CHECK (category IN ('ERROR', 'SUGGESTION', 'QUESTION', 'OTHER', 'NOTICE'));
END;
$$;
