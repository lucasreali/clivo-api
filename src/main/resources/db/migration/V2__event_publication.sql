CREATE TABLE event_publication (
    id                      UUID          PRIMARY KEY,
    listener_id             TEXT          NOT NULL,
    event_type              TEXT          NOT NULL,
    serialized_event        TEXT          NOT NULL,
    publication_date        TIMESTAMPTZ   NOT NULL,
    completion_date         TIMESTAMPTZ,
    last_resubmission_date  TIMESTAMPTZ,
    completion_attempts     INTEGER       NOT NULL DEFAULT 0,
    status                  VARCHAR(255)
                            CHECK (status IN ('PUBLISHED','PROCESSING','COMPLETED',
                                              'FAILED','RESUBMITTED'))
);

CREATE INDEX ix_event_publication_incomplete
    ON event_publication (completion_date) WHERE completion_date IS NULL;
