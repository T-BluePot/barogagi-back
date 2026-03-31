package com.barogagi.batch.entity;

import com.barogagi.batch.enums.Channel;
import com.barogagi.batch.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "MESSAGE_OUTBOX_ARCHIVE")
@Getter
@NoArgsConstructor
public class MessageOutboxArchive {

    @Id
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
}
