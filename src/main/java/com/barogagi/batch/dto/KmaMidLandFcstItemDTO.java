package com.barogagi.batch.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class KmaMidLandFcstItemDTO {

    private String regId;

    private String wf4Am;
    private String wf4Pm;
    private Integer rnSt4Am;
    private Integer rnSt4Pm;

    private String wf5Am;
    private String wf5Pm;
    private Integer rnSt5Am;
    private Integer rnSt5Pm;

    private String wf6Am;
    private String wf6Pm;
    private Integer rnSt6Am;
    private Integer rnSt6Pm;

    private String wf7Am;
    private String wf7Pm;
    private Integer rnSt7Am;
    private Integer rnSt7Pm;

    private String wf8;
    private Integer rnSt8;

    private String wf9;
    private Integer rnSt9;

    private String wf10;
    private Integer rnSt10;
}