

-- SCHEMA: users
-- DROP SCHEMA users ;
CREATE SCHEMA users AUTHORIZATION postgres;
COMMENT ON SCHEMA users IS 'Users and sessions information.';
GRANT ALL ON SCHEMA users TO postgres WITH GRANT OPTION;


-- Table: users."user"
-- DROP TABLE users."user";
CREATE TABLE users."user"
(
    username text COLLATE pg_catalog."default" NOT NULL,
    password_hash text COLLATE pg_catalog."default" NOT NULL,
    display_name text COLLATE pg_catalog."default" NOT NULL,
    email text COLLATE pg_catalog."default" NOT NULL,
    registration_date timestamp with time zone NOT NULL,
    CONSTRAINT user_pk PRIMARY KEY (username)
);
ALTER TABLE users."user" OWNER to postgres;
COMMENT ON TABLE users."user" IS 'Users general information.';


-- Table: users.user_session
-- DROP TABLE users.user_session;
CREATE TABLE users.user_session
(
    session_token uuid NOT NULL,
    username text COLLATE pg_catalog."default" NOT NULL,
    session_start timestamp with time zone NOT NULL,
    valid boolean NOT NULL DEFAULT true,
    CONSTRAINT user_session_pk PRIMARY KEY (session_token),
    CONSTRAINT user_session_user_fk FOREIGN KEY (username)
        REFERENCES users."user" (username) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
ALTER TABLE users.user_session OWNER to postgres;
COMMENT ON TABLE users.user_session IS 'Session tokens for each user.';


-- FUNCTION: users.check_session_token_sp(uuid)
-- DROP FUNCTION users.check_session_token_sp(uuid);
CREATE OR REPLACE FUNCTION users.check_session_token_sp(
	session_token_ uuid)
    RETURNS boolean
AS $$
    SELECT EXISTS(
        SELECT * FROM users.user_session ses
            WHERE ses.session_token = session_token_
                AND ses.valid
    );
$$
LANGUAGE 'sql';
ALTER FUNCTION users.check_session_token_sp(uuid) OWNER TO postgres;


-- PROCEDURE: users.logout_session_sp(uuid)
-- DROP PROCEDURE users.logout_session_sp(uuid);
CREATE OR REPLACE PROCEDURE users.logout_session_sp(
	session_token_ uuid)
AS $$
BEGIN
    UPDATE users.user_session s
        SET valid = FALSE
        WHERE s.session_token = session_token_;
END;
$$
LANGUAGE 'plpgsql';


-- PROCEDURE: users.register_user_sp(text, text, text, text)
-- DROP PROCEDURE users.register_user_sp(text, text, text, text);
CREATE OR REPLACE PROCEDURE users.register_user_sp(
	username_ text,
	password_hash_ text,
	display_name_ text,
	email_ text)
AS $$ BEGIN
    INSERT INTO users.user (username, password_hash, display_name, email, registration_date)
    VALUES (username_,
            password_hash_,
            display_name_,
            email_,
            now());
END; $$
LANGUAGE 'plpgsql';


-- PROCEDURE: users.add_user_session_sp(text, uuid)
-- DROP PROCEDURE users.add_user_session_sp(text, uuid);
CREATE OR REPLACE PROCEDURE users.add_user_session_sp(
	username_ text,
	session_token_ uuid)
AS $$
BEGIN
    INSERT INTO users.user_session(session_token, username, session_start)
    VALUES (session_token_, username_, now());
END;
$$
LANGUAGE 'plpgsql';


