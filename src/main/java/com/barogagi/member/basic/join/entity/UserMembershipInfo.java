package com.barogagi.member.basic.join.entity;

import com.barogagi.config.vo.DefaultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "USER_MEMBERSHIP_INFO")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserMembershipInfo extends DefaultVO {

    @Schema(description = "회원번호", example = "D18513EW81")
    @Id
    @Column(name = "MEMBERSHIP_NO")
    private String membershipNo = "";

    @Schema(description = "아이디", example = "abc123")
    @Column(name = "USER_ID")
    private String userId = "";

    @Schema(description = "비밀번호", example = "qwer12#$")
    @Column(name = "PASSWORD")
    private String password = "";

    @Schema(description = "이메일 주소", example = "abc123@naver.com")
    @Column(name = "EMAIL")
    private String email = "";

    @Schema(description = "생년월일 (YYYYMMDD)", example = "20010101")
    @Column(name = "BIRTH")
    private String birth = "";

    @Schema(description = "휴대폰 번호", example = "01012345678")
    @Column(name = "TEL")
    private String tel = "";

    @Schema(description = "성별 (M : 남 / W : 여)", example = "M")
    @Column(name = "GENDER")
    private String gender = "";

    @Schema(description = "닉네임", example = "가나다")
    @Column(name = "NICKNAME")
    private String nickName = "";

    @Schema(description = "회원가입 종류(BASIC : 기본 / GOOGLE : 구글 / KAKAO : 카카오톡 / NAVER : 네이버)", example = "BASIC")
    @Column(name = "JOIN_TYPE")
    private String joinType = "";
}
