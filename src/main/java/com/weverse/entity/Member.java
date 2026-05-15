package com.weverse.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    // 외부 시스템에서 발급된 ID를 PK로 사용하므로 @GeneratedValue를 사용하지 않는다.
    @Id
    private String memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipGrade membershipGrade;

    private LocalDateTime subscribeStartAt;

    private LocalDateTime subscribeEndAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void updateMembership(MembershipGrade grade, LocalDateTime startAt, LocalDateTime endAt) {
        this.membershipGrade = grade;
        this.subscribeStartAt = startAt;
        this.subscribeEndAt = endAt;
    }
}
