-- V4: Student-specific details (one-to-one with usr_user)
CREATE TABLE usr_student (
    user_id          VARCHAR(36)  PRIMARY KEY REFERENCES usr_user(id) ON DELETE CASCADE,
    academic_id      VARCHAR(50)  NOT NULL,
    fname            VARCHAR(20)  NOT NULL,
    lname            VARCHAR(20)  NOT NULL,
    gender           VARCHAR(20)  NOT NULL DEFAULT 'NOT_SET'
        CONSTRAINT chk_student_gender CHECK (gender IN ('MALE', 'FEMALE', 'OTHER', 'PREFER_NOT', 'NOT_SET')),
    whatsapp_number  VARCHAR(15),
    school           VARCHAR(150),
    guardian_name    VARCHAR(100),
    guardian_mobile  VARCHAR(15),
    usr_batch_id     VARCHAR(36)  REFERENCES usr_batch(id) ON DELETE SET NULL,
    updated_at       TIMESTAMPTZ  DEFAULT CURRENT_TIMESTAMP,
    updated_by       VARCHAR(36),
    CONSTRAINT uq_student_academic_id UNIQUE (academic_id)
);

CREATE INDEX idx_student_academic_id ON usr_student(academic_id);
CREATE INDEX idx_student_batch_id    ON usr_student(usr_batch_id);
