SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `book`;
DROP TABLE IF EXISTS `review`;
DROP TABLE IF EXISTS `review_like`;
DROP TABLE IF EXISTS `book_status`;
DROP TABLE IF EXISTS `book_recommendation`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `book_category`;
DROP TABLE IF EXISTS `popular_book`;
DROP TABLE IF EXISTS `popular_review`;
DROP TABLE IF EXISTS `power_user`;
DROP TABLE IF EXISTS `trending_keyword_snapshot`;
DROP TABLE IF EXISTS `trending_keyword`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `users` (
    id              BINARY(16)      NOT NULL,
    email           VARCHAR(320)    NOT NULL,
    nickname        VARCHAR(20)     NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    deleted_at      DATETIME(6)     NULL,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL
);
ALTER TABLE `users` ADD CONSTRAINT pk_user_id PRIMARY KEY (id);
ALTER TABLE `users` ADD CONSTRAINT uk_user_email UNIQUE (email);

CREATE TABLE book_category (
    id              BINARY(16)      NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    depth           INTEGER         NOT NULL,
    path            VARCHAR(255)    NOT NULL UNIQUE,
    parent_id       BINARY(16)      NULL,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL
);
ALTER TABLE book_category ADD CONSTRAINT pk_book_category_id PRIMARY KEY (id);
ALTER TABLE book_category ADD CONSTRAINT fk_book_category_parent_id FOREIGN KEY (parent_id) REFERENCES book_category(id);
ALTER TABLE book_category ADD CONSTRAINT uk_book_category_parent_id_name UNIQUE (name, parent_id);
ALTER TABLE book_category ADD CONSTRAINT uk_book_category_path UNIQUE (path);

CREATE TABLE book (
    id              BINARY(16)      NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    author          VARCHAR(50)     NOT NULL,
    description     TEXT            NULL,
    publisher       VARCHAR(50)     NOT NULL,
    published_date  DATE            NULL,
    isbn            VARCHAR(20)     NULL UNIQUE,
    thumbnail_key   VARCHAR(255)    NULL,
    review_count    BIGINT          NOT NULL DEFAULT 0,
    rating          DOUBLE          NOT NULL DEFAULT 0,
    book_category_id
                    BINARY(16)      NULL,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    deleted_at      DATETIME(6)     NULL
);
ALTER TABLE book ADD CONSTRAINT pk_book_id PRIMARY KEY (id);
ALTER TABLE book ADD CONSTRAINT uk_book_isbn UNIQUE (isbn);
ALTER TABLE book ADD CONSTRAINT fk_book_book_category FOREIGN KEY (book_category_id) REFERENCES book_category(id);

CREATE TABLE review (
    id              BINARY(16)      NOT NULL,
    book_id         BINARY(16)      NOT NULL,
    user_id         BINARY(16)      NOT NULL,
    content         VARCHAR(1000)   NOT NULL,
    attachment_url  VARCHAR(255)    NULL,
    rating          INTEGER         NOT NULL CHECK (rating BETWEEN 1 AND 5),
    like_count      BIGINT          NOT NULL DEFAULT 0,
    comment_count   BIGINT          NOT NULL DEFAULT 0,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    deleted_at      DATETIME(6)     NULL
);
ALTER TABLE review ADD CONSTRAINT pk_review_id PRIMARY KEY (id);
ALTER TABLE review ADD CONSTRAINT uk_review_book_user UNIQUE (book_id, user_id);
ALTER TABLE review ADD CONSTRAINT fk_review_book FOREIGN KEY (book_id) REFERENCES book(id);
ALTER TABLE review ADD CONSTRAINT fk_review_user FOREIGN KEY (user_id) REFERENCES `users`(id);

CREATE TABLE review_like (
    id              BINARY(16) NOT NULL,
    review_id       BINARY(16) NOT NULL,
    user_id         BINARY(16) NOT NULL,
    created_at      DATETIME(6) NOT NULL
);
ALTER TABLE review_like ADD CONSTRAINT pk_review_like_id PRIMARY KEY (id);
ALTER TABLE review_like ADD CONSTRAINT fk_review_like_review FOREIGN KEY (review_id) REFERENCES review (id);
ALTER TABLE review_like ADD CONSTRAINT fk_review_like_user FOREIGN KEY (user_id) REFERENCES `users` (id);
ALTER TABLE review_like ADD CONSTRAINT uk_review_like_review_user UNIQUE (review_id, user_id);

CREATE TABLE comment (
    id              BINARY(16)       NOT NULL,
    user_id         BINARY(16)       NOT NULL,
    review_id       BINARY(16)       NOT NULL,
    content         VARCHAR(500)     NOT NULL,
    deleted_at      DATETIME(6)      NULL,
    created_at      DATETIME(6)      NOT NULL,
    updated_at      DATETIME(6)      NOT NULL
);
ALTER TABLE comment ADD CONSTRAINT pk_comment_id PRIMARY KEY (id);
ALTER TABLE comment ADD CONSTRAINT uk_comment_user_review UNIQUE (user_id, review_id);
ALTER TABLE comment ADD CONSTRAINT fk_comment_user FOREIGN KEY (user_id) REFERENCES `users`(id);
ALTER TABLE comment ADD CONSTRAINT fk_comment_review FOREIGN KEY (review_id) REFERENCES review(id);

CREATE TABLE book_status (
    id              BINARY(16)      NOT NULL,
    user_id         BINARY(16)      NOT NULL,
    book_id         BINARY(16)      NOT NULL,
    status          VARCHAR(10)     NOT NULL,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL
);
ALTER TABLE book_status ADD CONSTRAINT pk_book_status_id PRIMARY KEY (id);
ALTER TABLE book_status ADD CONSTRAINT uk_book_status_user_book UNIQUE (user_id, book_id);
ALTER TABLE book_status ADD CONSTRAINT fk_book_status_user FOREIGN KEY (user_id) REFERENCES `users`(id);
ALTER TABLE book_status ADD CONSTRAINT fk_book_status_book FOREIGN KEY (book_id) REFERENCES book(id);

CREATE TABLE book_recommendation (
    id              BINARY(16)      NOT NULL,
    user_id         BINARY(16)      NOT NULL,
    book_id         BINARY(16)      NOT NULL,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL
);
ALTER TABLE book_recommendation ADD CONSTRAINT pk_book_recommendation_id PRIMARY KEY (id);
ALTER TABLE book_recommendation ADD CONSTRAINT uk_book_recommendation_user_book UNIQUE (user_id, book_id);
ALTER TABLE book_recommendation ADD CONSTRAINT fk_book_recommendation_user FOREIGN KEY (user_id) REFERENCES `users`(id);
ALTER TABLE book_recommendation ADD CONSTRAINT fk_book_recommendation_book FOREIGN KEY (book_id) REFERENCES  book(id);

CREATE TABLE notification
(
    id              BINARY(16)      NOT NULL,
    user_id         BINARY(16)      NOT NULL,
    review_id       BINARY(16)      NOT NULL,
    message         VARCHAR(1000)   NULL,
    confirmed       BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL
);
ALTER TABLE notification ADD CONSTRAINT pk_notification_id PRIMARY KEY (id);
ALTER TABLE notification ADD CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES `users`(id);
ALTER TABLE notification ADD CONSTRAINT fk_notifications_review FOREIGN KEY (review_id) REFERENCES review(id);

CREATE TABLE batch_metadata
(
    metadata_type ENUM(
        'POPULAR_BOOK',
        'POPULAR_REVIEW',
        'POWER_USER',
        'TRENDING_KEYWORD'
    ) NOT NULL,
    period ENUM(
        'REALTIME',
        'DAILY',
        'WEEKLY',
        'MONTHLY',
        'ALL_TIME'
        ) NOT NULL,
    dataset_id    BIGINT NOT NULL,
    batch_date    DATE NULL,
    updated_at    DATETIME(6)
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT pk_batch_metadata
        PRIMARY KEY (metadata_type, period),
    CONSTRAINT uq_batch_metadata_dataset_id
        UNIQUE (dataset_id)
);

CREATE TABLE popular_book
(
    id             BINARY(16)      NOT NULL,
    dataset_id     BIGINT          NOT NULL,
    book_id        BINARY(16)      NOT NULL,
    period ENUM('DAILY','WEEKLY','MONTHLY','ALL_TIME') NOT NULL,
    batch_date     DATE            NOT NULL,
    ranking        INT UNSIGNED    NOT NULL,
    book_title     VARCHAR(255)    NOT NULL,
    author         VARCHAR(50)     NOT NULL,
    thumbnail_url  VARCHAR(300)    NULL,
    score          DECIMAL(10, 2)  NOT NULL,
    review_count   INT UNSIGNED    NOT NULL,
    like_count     INT UNSIGNED    NOT NULL,
    comment_count  INT UNSIGNED    NOT NULL,
    average_rating DECIMAL(3, 2)   NOT NULL,
    created_at     DATETIME(6)     NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_popular_book_dataset_book UNIQUE (dataset_id, book_id),
    CONSTRAINT uq_popular_book_dataset_ranking UNIQUE (dataset_id, ranking),
    CONSTRAINT chk_popular_book_ranking CHECK (ranking BETWEEN 1 AND 50),
    CONSTRAINT chk_popular_book_avg_rating CHECK (average_rating BETWEEN 0.00 AND 5.00),
    CONSTRAINT chk_popular_book_score CHECK (score >= 0)
);

CREATE TABLE popular_review
(
    id             BINARY(16)      NOT NULL,
    dataset_id     BIGINT          NOT NULL,
    review_id      BINARY(16)      NOT NULL,
    period ENUM('DAILY','WEEKLY','MONTHLY','ALL_TIME') NOT NULL,
    batch_date     DATE            NOT NULL,
    ranking        INT UNSIGNED    NOT NULL,
    book_title     VARCHAR(255)    NOT NULL,
    book_author    VARCHAR(50)     NOT NULL,
    thumbnail_url  VARCHAR(300)    NULL,
    user_nickname  VARCHAR(20)     NOT NULL,
    review_summary VARCHAR(300)    NOT NULL,
    review_rating  INT UNSIGNED    NOT NULL,
    score          DECIMAL(10, 2)  NOT NULL,
    like_count     INT UNSIGNED    NOT NULL,
    comment_count  INT UNSIGNED    NOT NULL,
    created_at     DATETIME(6)     NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_popular_review_dataset_review UNIQUE (dataset_id, review_id),
    CONSTRAINT uq_popular_review_dataset_ranking UNIQUE (dataset_id, ranking),
    CONSTRAINT chk_popular_review_ranking CHECK (ranking BETWEEN 1 AND 50),
    CONSTRAINT chk_popular_review_rating CHECK (review_rating BETWEEN 0 AND 5),
    CONSTRAINT chk_popular_review_score CHECK (score >= 0)
);

CREATE TABLE power_user
(
    id            BINARY(16)      NOT NULL,
    dataset_id    BIGINT          NOT NULL,
    user_id       BINARY(16)      NOT NULL,
    period ENUM('DAILY','WEEKLY','MONTHLY','ALL_TIME') NOT NULL,
    batch_date    DATE            NOT NULL,
    ranking       INT UNSIGNED    NOT NULL,
    nickname      VARCHAR(20)     NOT NULL,
    score         DECIMAL(10, 2)  NOT NULL,
    like_count    INT UNSIGNED    NOT NULL,
    comment_count INT UNSIGNED    NOT NULL,
    created_at    DATETIME(6)     NOT NULL
                        DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uq_power_user_dataset_user UNIQUE (dataset_id, user_id),
    CONSTRAINT uq_power_user_dataset_ranking UNIQUE (dataset_id, ranking),
    CONSTRAINT chk_power_user_ranking CHECK (ranking BETWEEN 1 AND 10),
    CONSTRAINT chk_power_user_score CHECK (score >= 0)
);

CREATE TABLE trending_keyword_snapshot
(
    dataset_id    BIGINT   NOT NULL AUTO_INCREMENT,
    calculated_at DATETIME NOT NULL,
    PRIMARY KEY (dataset_id),
    CONSTRAINT uq_trending_keyword_snapshot_time UNIQUE (calculated_at)
);

CREATE TABLE trending_keyword
(
    dataset_id BIGINT        NOT NULL,
    ranking    INT UNSIGNED  NOT NULL,
    keyword    VARCHAR(50)   NOT NULL,
    score      DECIMAL(8, 2) NOT NULL,
    PRIMARY KEY (dataset_id, ranking),
    CONSTRAINT fk_trending_keyword_dataset
        FOREIGN KEY (dataset_id)
            REFERENCES trending_keyword_snapshot (dataset_id)
            ON DELETE CASCADE,
    CONSTRAINT chk_trending_keyword_ranking CHECK (ranking BETWEEN 1 AND 10),
    CONSTRAINT chk_trending_keyword_score CHECK (score >= 0)
);

CREATE TABLE `oauth_kakao` (
   id              BINARY(16)      NOT NULL,
   user_id         BINARY(16)      NOT NULL,
   kakao_id         VARCHAR(50)     NOT NULL,
   email           VARCHAR(320)    NOT NULL,
   nickname        VARCHAR(20)     NOT NULL,
   deleted_at      DATETIME(6)     NULL,
   created_at      DATETIME(6)     NOT NULL,
   updated_at      DATETIME(6)     NOT NULL
);
ALTER TABLE `oauth_kakao` ADD CONSTRAINT pk_oauth_kakao_id PRIMARY KEY (id);
ALTER TABLE `oauth_kakao` ADD CONSTRAINT uk_oauth_kakao_email UNIQUE (email);
ALTER TABLE `oauth_kakao` ADD CONSTRAINT fk_oauth_kakao_user FOREIGN KEY (user_id) REFERENCES `users`(id);

CREATE TABLE comment_report (
                                id              BINARY(16)      NOT NULL,
                                comment_id      BINARY(16)      NOT NULL,
                                user_id         BINARY(16)      NOT NULL,
                                reason          VARCHAR(500)    NULL,
                                created_at      DATETIME(6)     NOT NULL,
                                updated_at      DATETIME(6)     NOT NULL
);
ALTER TABLE comment_report ADD CONSTRAINT pk_comment_report_id PRIMARY KEY (id);
ALTER TABLE comment_report ADD CONSTRAINT fk_comment_report_comment FOREIGN KEY (comment_id) REFERENCES comment(id);
ALTER TABLE comment_report ADD CONSTRAINT fk_comment_report_user FOREIGN KEY (user_id) REFERENCES `users`(id);