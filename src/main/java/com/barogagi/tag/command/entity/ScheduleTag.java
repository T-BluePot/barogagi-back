package com.barogagi.tag.command.entity;

import com.barogagi.schedule.command.entity.Schedule;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "SCHEDULE_TAG")
@EqualsAndHashCode
public class ScheduleTag {

    @EmbeddedId
    private ScheduleTagId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("scheduleNum")
    @JoinColumn(name = "SCHEDULE_NUM")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagNum")
    @JoinColumn(name = "TAG_NUM")
    private Tag tag;

}


