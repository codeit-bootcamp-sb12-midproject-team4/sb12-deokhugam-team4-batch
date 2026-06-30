package com.codeit.deokhugambatch.domain.review;

import com.codeit.deokhugambatch.domain.book.Book;
import com.codeit.deokhugambatch.domain.common.SoftDeletableEntity;
import com.codeit.deokhugambatch.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Table(
    name = "review",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_reviews_book_user",
            columnNames = {"book_id", "user_id"}
        )
    })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE review SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Review extends SoftDeletableEntity {

  @Column(name = "content", length = 1000, nullable = false)
  private String content;

  @Column(name = "attachment_url", length = 100)
  private String attachmentUrl;

  @Column(name = "rating", nullable = false)
  private Integer rating;

  @Column(name = "like_count", nullable = false)
  private Long likeCount = 0L;

  @Column(name = "comment_count", nullable = false)
  private Long commentCount = 0L;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "book_id", nullable = false)
  private Book book;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  public void update(String content, String attachmentUrl, Integer rating) {
    this.content = content;
    this.attachmentUrl = attachmentUrl;
    this.rating = rating;
  }

  /// ?숈떆???댁뒋 諛쒖깮吏?? -> Repository Layer?먯꽌 @OptimisticLock?쇰줈 泥섎━
  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    if (likeCount > 0) {
      this.likeCount--;
    }
  }

  public void increaseCommentCount() {
    this.commentCount++;
  }

  public void decreaseCommentCount() {
    if (commentCount > 0) {
      this.commentCount--;
    }
  }

  public boolean isOwnedBy(UUID userId) {
    return this.user.getId().equals(userId);
  }
}