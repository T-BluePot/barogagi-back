package com.barogagi.setting.service;

import com.barogagi.response.ApiResponse;
import com.barogagi.setting.dto.MemberSettingDTO;
import com.barogagi.setting.entity.MemberSetting;
import com.barogagi.setting.entity.Setting;
import com.barogagi.setting.enums.SettingType;
import com.barogagi.setting.enums.Value;
import com.barogagi.setting.mapper.SettingMapper;
import com.barogagi.setting.repository.MemberSettingRepository;
import com.barogagi.setting.repository.SettingRepository;
import com.barogagi.util.MembershipUtil;
import com.barogagi.util.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SettingService {

    private final MembershipUtil membershipUtil;

    private final MemberSettingRepository memberSettingRepository;
    private final SettingRepository settingRepository;

    private final SettingMapper settingMapper;

    // 설정 기본 세팅
    public void basicSetting(String membershipNo) {

        // 1. 설정 목록 조회 후 설정이 되지 않은 설정들은 자동으로 ON으로 설정
        List<MemberSettingDTO> autoSettingList = this.autoSetting(membershipNo);
    }

    public ApiResponse selectSettings(HttpServletRequest request) {

        // 1. 회원번호 구하기
        Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
        if(!membershipNoInfo.get("resultCode").equals("A200")) {
            return ApiResponse.error(
                    String.valueOf(membershipNoInfo.get("resultCode")),
                    String.valueOf(membershipNoInfo.get("message"))
            );
        }

        String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));
        List<MemberSettingDTO> settingList = this.autoSetting(membershipNo);

        return ApiResponse.resultData(settingList, "S200", "설정 목록 조회 성공하였습니다.");
    }

    public ApiResponse updateSetting(HttpServletRequest request, SettingType settingType, Value value) {

        // 1. 회원번호 구하기
        Map<String, Object> membershipNoInfo = membershipUtil.membershipNoService(request);
        if(!membershipNoInfo.get("resultCode").equals("A200")) {
            return ApiResponse.error(
                    String.valueOf(membershipNoInfo.get("resultCode")),
                    String.valueOf(membershipNoInfo.get("message"))
            );
        }

        String membershipNo = String.valueOf(membershipNoInfo.get("membershipNo"));

        // 2. 설정 정보 조회
        Setting settingInfo = settingRepository.selectSetting(settingType);

        // 3. 설정 수정
        int updated = memberSettingRepository.updateSetting(membershipNo, settingInfo.getId(), value, LocalDateTime.now());

        if(updated != 1) {
            return ApiResponse.result(ErrorCode.NOT_UPDATE_SETTING);
        }

        return ApiResponse.result("S202", "설정 수정 완료하였습니다.");
    }

    public List<MemberSettingDTO> selectSettings(String membershipNo) {
        return settingMapper.selectSettings(membershipNo);
    }

    public MemberSettingDTO selectMemberSettingValue(MemberSettingDTO memberSettingDTO) {
        return settingMapper.selectMemberSettingValue(memberSettingDTO);
    }

    public List<MemberSettingDTO> autoSetting(String membershipNo) {

        List<MemberSettingDTO> settingList = this.selectSettings(membershipNo);

        for(MemberSettingDTO memberSettingDTO : settingList) {
            String beforeValue = memberSettingDTO.getBeforeValue();
            if(beforeValue == null) {
                MemberSetting memberSetting = new MemberSetting();
                memberSetting.setId(memberSettingDTO.getId());
                memberSetting.setMembershipNo(membershipNo);
                memberSetting.setValue(memberSettingDTO.getValue());
                memberSetting.setDate(LocalDateTime.now());

                memberSettingRepository.save(memberSetting);
            }
        }

        return settingList;
    }
}
