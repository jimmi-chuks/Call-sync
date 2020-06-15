CREATE TYPE calltype AS ENUM ('missed', 'incoming', 'outgoingsuccess', 'outgoingfailed', 'unknown');
--
CREATE TYPE signuptype AS ENUM ('google', 'facebook');
--Refactor enum type from varchar to enum
create TABLE users(
  uuid       UUID PRIMARY KEY,
  name       VARCHAR NOT NULL,
  sign_up_type signuptype NOT NULL,
  sign_up_id   VARCHAR NOT NULL
);

create TABLE contacts(
    uuid       UUID PRIMARY KEY,
    name VARCHAR UNIQUE NOT NULL,
    number VARCHAR NOT NULL,
    owner_id UUID NOT NULL,
    saved_name       VARCHAR NOT NULL,
    CONSTRAINT user_id_fkey FOREIGN KEY (owner_id)
    REFERENCES users (uuid) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);

create TABLE calls(
    uuid       UUID PRIMARY KEY,
    caller_id UUID NOT NULL,
    call_type calltype NOT NULL,
    number VARCHAR NOT NULL,
    call_time INT8 NOT NULL,
    device_info VARCHAR NOT NULL,
    call_duration INT8 NOT NULL,
    CONSTRAINT caller_id_fkey FOREIGN KEY (caller_id)
    REFERENCES users (uuid) MATCH SIMPLE
    ON UPDATE NO ACTION ON DELETE NO ACTION
);
