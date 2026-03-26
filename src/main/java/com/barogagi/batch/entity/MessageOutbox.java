package com.barogagi.batch.entity;

import com.barogagi.batch.enums.Channel;
import com.barogagi.batch.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "MESSAGE_OUTBOX",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_outbox", columnNames = {
                        "membershipNo", "userId", "messageType", "channel"
                })
        }
)
@Getter
@NoArgsConstructor
public class MessageOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OUTBOX_NO")
    private Long id;

    @Column(name = "MEMBERSHIP_NO")
    private String membershipNo;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "MESSAGE_TYPE")
    private String messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "CHANNEL")
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS")
    private Status status;

    @Column(name = "TRY_CNT")
    private int tryCnt;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    public void markSuccess() {
        this.status = Status.SUCCESS;
    }

    // 발송 실패 처리 + tryCnt 증가
    public void markFail() {
        this.tryCnt++;
        this.status = Status.FAIL;
    }

    // 재시도 가능 상태로 변경 + tryCnt 증가
    public void markRetry() {
        this.tryCnt++;
        this.status = Status.READY;
    }

    public static MessageOutbox create(String membershipNo, String userId, String type, Channel channel) {
        MessageOutbox o = new MessageOutbox();
        o.membershipNo = membershipNo;
        o.userId = userId;
        o.messageType = type;
        o.channel = channel;
        o.status = Status.READY;
        o.tryCnt = 0;
        o.createdAt = LocalDateTime.now();
        return o;
    }
}
