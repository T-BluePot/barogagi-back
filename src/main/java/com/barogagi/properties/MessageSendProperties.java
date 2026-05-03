package com.barogagi.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "service")
public class MessageSendProperties {

    // service.name
    private String name = "핏플(fitpl)";

    // service.kor-name
    private String korName = "핏플";

    // service.eng-name
    private String engName;

    // service.company.email
    private String companyEmail;

    // service.company.kor-name
    private String companyKorName = "푸른핫가마";

    // service.company.eng-name
    private String companyEngName;

    // service.company.tel
    private String companyTel;

    // service.business.biz.number
    private String businessBizNumber;

    // service.company.ceo-name
    private String companyCeoName = "정은우";
}
