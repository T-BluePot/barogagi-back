package com.barogagi.batch.entity;

import com.barogagi.batch.enums.Channel;
import com.barogagi.batch.enums.Status;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "MESSAGE_SEND_HISTORY"
)
@Getter
@NoArgsConstructor
public class MessageSendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEND_ID")
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
    @Column(name = "SEND_STATUS")
    private Status sendStatus;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    public MessageSendHistory(String membershipNo, String userId, String messageType,
                              Channel channel, Status sendStatus, String errorMessage) {
        this.membershipNo = membershipNo;
        this.userId = userId;
        this.messageType = messageType;
        this.channel = channel;
        this.sendStatus = sendStatus;
        this.errorMessage = errorMessage;
        this.createdAt = LocalDateTime.now();
    }
}
