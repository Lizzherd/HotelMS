package com.hotel.model;

import com.hotel.model.enums.MemberLevel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "member_info")
public class MemberInfo {
    @Id
    private String id;

    @Indexed(unique = true)
    private String idCardNumber; // 身份证号

    private LocalDate registrationDate; // 注册日期
    private MemberLevel memberLevel; // 会员等级

    public MemberInfo() {
    }

    public MemberInfo(String idCardNumber, LocalDate registrationDate, MemberLevel memberLevel) {
        this.idCardNumber = idCardNumber;
        this.registrationDate = registrationDate;
        this.memberLevel = memberLevel;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public MemberLevel getMemberLevel() {
        return memberLevel;
    }

    public void setMemberLevel(MemberLevel memberLevel) {
        this.memberLevel = memberLevel;
    }

    @Override
    public String toString() {
        return "MemberInfo{" +
                "id='" + id + '\'' +
                ", idCardNumber='" + idCardNumber + '\'' +
                ", registrationDate=" + registrationDate +
                ", memberLevel=" + memberLevel +
                '}';
    }
}