package com.barogagi.tag.command.entity;


import com.barogagi.plan.command.entity.Category;
import com.barogagi.tag.enums.TagType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성 방지
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "TAG")
@ToString(exclude = "category")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TAG_NUM")
    private Integer tagNum;

    @Column(name = "TAG_NM", nullable = false, length = 100)
    private String tagNm;

    @Enumerated(EnumType.STRING)
    @Column(name = "TAG_TYPE", nullable = false, length = 1)
    private TagType tagType; // ENUM('P', 'S')

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATEGORY_NUM")
    private Category category;
}
