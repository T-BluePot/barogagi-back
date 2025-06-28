package com.barogagi.region.command.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Builder(toBuilder = true)
@Table(name = "REGION")
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REGION_NUM")
    private Integer regionNum;

    @Column(name = "REGION_NM", nullable = false, length = 100)
    private String regionNm;
}
