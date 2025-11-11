package com.barogagi.member.join.dto;

import com.barogagi.config.vo.DefaultVO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserIdCheckDTO extends DefaultVO {
    @NotBlank(message = "사용자 ID는 필수 입력값입니다.")
    @Size(min = 4, max = 20, message = "사용자 ID는 4자 이상 20자 이하여야 합니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "사용자 ID는 영문과 숫자만 사용 가능합니다.")
    private String userId = "";
}
