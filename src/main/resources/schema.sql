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
    parent_id       BINARY(16)      NULL,
    depth           INTEGER         NOT NULL,
    path            VARCHAR(255)    NOT NULL,
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL
);
ALTER TABLE book_category ADD CONSTRAINT pk_book_category_id PRIMARY KEY (id);
ALTER TABLE book_category ADD CONSTRAINT fk_book_category_parent_id FOREIGN KEY (parent_id) REFERENCES book_category(id);

CREATE TABLE book (
    id              BINARY(16)      NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    author          VARCHAR(50)     NOT NULL,
    description     VARCHAR(1000)   NULL,
    publisher       VARCHAR(50)     NOT NULL,
    published_date  DATE            NULL,
    isbn            VARCHAR(20)     NOT NULL UNIQUE,
    thumbnail_url   VARCHAR(100)    NULL,
    review_count    BIGINT          NULL DEFAULT 0,
    rating          DOUBLE          NULL DEFAULT 0,
    book_category_id
                    BINARY(16)      NOT NULL,
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
    attachment_url  VARCHAR(100)    NULL,
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

CREATE TABLE popular_book (
id                  BINARY(16)      NOT NULL,
    book_id         BINARY(16)      NOT NULL,
    period          ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME') NOT NULL,
    ranking         INT UNSIGNED NOT NULL,
    book_title      VARCHAR(255)    NOT NULL,
    author          VARCHAR(50)     NOT NULL,
    thumbnail_url   VARCHAR(300)    NULL,
    score           DECIMAL(10,2)   NOT NULL,
    review_count    INT UNSIGNED    NOT NULL,
    like_count      INT UNSIGNED    NOT NULL,
    comment_count   INT UNSIGNED    NOT NULL,
    average_rating  DECIMAL(3,2)    NOT NULL,
    batch_date      DATE            NOT NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
ALTER TABLE popular_book ADD CONSTRAINT pk_popular_book_id PRIMARY KEY (id);
ALTER TABLE popular_book ADD CONSTRAINT uq_popular_book_date UNIQUE (period, book_id, batch_date);
ALTER TABLE popular_book ADD CONSTRAINT uq_popular_book_ranking UNIQUE (period, batch_date, ranking);
ALTER TABLE popular_book ADD CONSTRAINT chk_popular_book_ranking CHECK (ranking BETWEEN 1 AND 50);
ALTER TABLE popular_book ADD CONSTRAINT chk_popular_book_avg_rating CHECK (average_rating BETWEEN 0.00 AND 5.00);
ALTER TABLE popular_book ADD CONSTRAINT chk_popular_book_score CHECK (score >= 0);

CREATE TABLE popular_review (
    id              BINARY(16)      NOT NULL,
    review_id       BINARY(16)      NOT NULL,
    `period`        ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME') NOT NULL,
    ranking         INT UNSIGNED NOT NULL,
    book_title      VARCHAR(255)    NOT NULL,
    book_author     VARCHAR(50)     NOT NULL,
    thumbnail_url   VARCHAR(300)    NULL,
    user_nickname   VARCHAR(20)     NOT NULL,
    review_content  VARCHAR(1000)   NOT NULL,
    review_rating   INT UNSIGNED NOT NULL,
    score           DECIMAL(10,2)   NOT NULL,
    like_count      INT UNSIGNED    NOT NULL,
    comment_count   INT UNSIGNED    NOT NULL,
    batch_date      DATE            NOT NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
ALTER TABLE popular_review ADD CONSTRAINT pk_popular_review_id PRIMARY KEY (id);
ALTER TABLE popular_review ADD CONSTRAINT uq_popular_review_date UNIQUE (period, review_id, batch_date);
ALTER TABLE popular_review ADD CONSTRAINT uq_popular_review_ranking UNIQUE (period, batch_date, ranking);
ALTER TABLE popular_review ADD CONSTRAINT chk_popular_review_ranking CHECK (ranking BETWEEN 1 AND 50);
ALTER TABLE popular_review ADD CONSTRAINT chk_popular_review_rating CHECK (review_rating BETWEEN 0 AND 5);
ALTER TABLE popular_review ADD CONSTRAINT chk_popular_review_score CHECK (score >= 0);

CREATE TABLE power_user (
    id              BINARY(16)      NOT NULL,
    user_id         BINARY(16)      NOT NULL,
    `period`        ENUM('DAILY', 'WEEKLY', 'MONTHLY', 'ALL_TIME') NOT NULL,
    ranking         INT UNSIGNED NOT NULL,
    nickname        VARCHAR(20)     NOT NULL,
    score           DECIMAL(10,2)   NOT NULL,
    like_count      INT UNSIGNED    NOT NULL,
    comment_count   INT UNSIGNED    NOT NULL,
    batch_date      DATE            NOT NULL,
    created_at      DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
);
ALTER TABLE power_user ADD CONSTRAINT pk_power_user_id PRIMARY KEY (id);
ALTER TABLE power_user ADD CONSTRAINT uq_power_user_date UNIQUE (period, user_id, batch_date);
ALTER TABLE power_user ADD CONSTRAINT uq_power_user_ranking UNIQUE (period, batch_date, ranking);
ALTER TABLE power_user ADD CONSTRAINT chk_power_user_ranking CHECK (ranking BETWEEN 1 AND 10);
ALTER TABLE power_user ADD CONSTRAINT chk_power_user_score CHECK (score >= 0);

CREATE TABLE trending_keyword_snapshot (
    snapshot_id     BIGINT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    calculated_at   DATETIME        NOT NULL, -- (6) 정밀도를 제거하여 5바이트로 최적화
    UNIQUE KEY uq_trending_keyword_snapshot_time(calculated_at)
);
-- ALTER TABLE trending_keyword_snapshot ADD CONSTRAINT pk_trending_keyword_snapshot_id PRIMARY KEY (snapshot_id);
-- ALTER TABLE trending_keyword_snapshot ADD CONSTRAINT uq_trending_keyword_snapshot_time UNIQUE (calculated_at);

CREATE TABLE trending_keyword (
    snapshot_id     BIGINT          NOT NULL,
    ranking         INT UNSIGNED NOT NULL,
    keyword         VARCHAR(50)     NOT NULL,
    score           DECIMAL(8,2)    NOT NULL,
    -- 대리키 id 제거 후 복합 PK 지정 (snapshot_id 별로 데이터가 물리적으로 뭉쳐서 저장됨)
    PRIMARY KEY (snapshot_id, ranking)
);
ALTER TABLE trending_keyword ADD CONSTRAINT fk_trending_keyword_snapshot_id FOREIGN KEY (snapshot_id) REFERENCES trending_keyword_snapshot (snapshot_id) ON DELETE CASCADE;
ALTER TABLE trending_keyword ADD CONSTRAINT chk_trending_keyword_ranking CHECK (ranking BETWEEN 1 AND 10);
ALTER TABLE trending_keyword ADD CONSTRAINT chk_trending_keyword_score CHECK (score >= 0);