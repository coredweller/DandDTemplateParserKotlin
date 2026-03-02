CREATE TABLE IF NOT EXISTS character_sheet_renders (
    id             VARCHAR(36)  NOT NULL,
    sheet_type     VARCHAR(20)  NOT NULL,
    character_name VARCHAR(255) NOT NULL,
    level          INT          NOT NULL,
    response_html  LONGTEXT     NOT NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT (UTC_TIMESTAMP(6)),
    PRIMARY KEY (id)
);
