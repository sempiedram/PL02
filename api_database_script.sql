

-- Code for dropping database with active connections taken from:
-- https://stackoverflow.com/questions/5408156/how-to-drop-a-postgresql-database-if-there-are-active-connections-to-it
SELECT pg_terminate_backend(pg_stat_activity.pid)
FROM pg_stat_activity
WHERE pg_stat_activity.datname = 'PL02DB'
  AND pid <> pg_backend_pid();
  
DROP DATABASE "PL02DB";

-- Database: PL02DB
-- DROP DATABASE "PL02DB";

CREATE DATABASE "PL02DB"
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'English_United States.1252'
    LC_CTYPE = 'English_United States.1252'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE "PL02DB"
    IS 'PL02 User information database';


-- SCHEMA: users
-- DROP SCHEMA users ;

CREATE SCHEMA users
    AUTHORIZATION postgres;

COMMENT ON SCHEMA users
    IS 'Users and sessions information.';

GRANT ALL ON SCHEMA users TO postgres WITH GRANT OPTION;


-- Table: users."user"
-- DROP TABLE users."user";

CREATE TABLE users."user"
(
    username text CONSTRAINT user_username_nn NOT NULL,
    password_hash text CONSTRAINT user_password_hash_nn NOT NULL,
    display_name text CONSTRAINT user_display_name_nn NOT NULL,
    email text CONSTRAINT user_email_nn NOT NULL,
    registration_date TIMESTAMP WITH TIME ZONE CONSTRAINT user_registration_date_nn NOT NULL,
    CONSTRAINT "user_pk" PRIMARY KEY (username)
);

ALTER TABLE users."user"
    OWNER to postgres;

COMMENT ON TABLE users."user"
    IS 'Users general information.';


-- Table: users.user_session
-- DROP TABLE users.user_session;

CREATE TABLE users.user_session
(
    session_token uuid CONSTRAINT user_session_session_nn NOT NULL,
    username text CONSTRAINT user_session_user_nn NOT NULL,
    session_start TIMESTAMP WITH TIME ZONE CONSTRAINT user_session_start_nn NOT NULL,
    valid boolean CONSTRAINT user_session_valid_df DEFAULT TRUE CONSTRAINT user_session_valid_nn,
    CONSTRAINT user_session_pk PRIMARY KEY (session_token),
    CONSTRAINT user_session_user_fk FOREIGN KEY (username)
        REFERENCES users."user" (username) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

ALTER TABLE users.user_session
    OWNER to postgres;

COMMENT ON TABLE users.user_session
    IS 'Session tokens for each user.';


-- PROCEDURE: users.sp_add_user_session(text, uuid)
-- DROP PROCEDURE users.sp_add_user_session(text, uuid);

CREATE OR REPLACE PROCEDURE users.add_user_session_sp(
	username_ text,
	session_token_ uuid)
LANGUAGE 'plpgsql'

AS $BODY$
BEGIN
    INSERT INTO users.user_session(session_token, username, session_start)
    VALUES (session_token_, username_, now());
END;
$BODY$;


-- PROCEDURE: users.sp_logout_session(uuid)
-- DROP PROCEDURE users.sp_logout_session(uuid);

CREATE OR REPLACE PROCEDURE users.logout_session_sp(
	session_token_ uuid)
LANGUAGE 'plpgsql'

AS $BODY$
BEGIN
    UPDATE users.user_session s
        SET valid = FALSE
        WHERE s.session_token = session_token_;
END;
$BODY$;


-- PROCEDURE: users.sp_register_user(text, text, text, text)
-- DROP PROCEDURE users.sp_register_user(text, text, text, text);

CREATE OR REPLACE PROCEDURE users.register_user_sp(
	username_ text,
	password_hash_ text,
	display_name_ text,
	email_ text)
LANGUAGE 'plpgsql'

AS $BODY$ BEGIN
    INSERT INTO users.user (username, password_hash, display_name, email, registration_date)
    VALUES (username_,
            password_hash_,
            display_name_,
            email_,
            now());
END; $BODY$;
