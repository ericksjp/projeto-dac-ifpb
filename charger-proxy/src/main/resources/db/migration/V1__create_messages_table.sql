CREATE TABLE messages (
    id  BIGSERIAL,
    content TEXT NOT NULL,
    constraint pk_messages PRIMARY KEY (id)
);
