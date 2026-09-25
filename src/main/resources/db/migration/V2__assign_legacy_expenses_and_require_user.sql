UPDATE expense
SET user_id = 2
WHERE user_id IS NULL;

ALTER TABLE expense
    ALTER COLUMN user_id SET NOT NULL;