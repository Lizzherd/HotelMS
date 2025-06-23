package com.hotel.model;

import com.hotel.model.enums.Gender;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customers")
public class Customer {
    @Id
    private String id;

    private String name; // 姓名
    private Gender gender; // 性别

    @Indexed(unique = true)
    private String idCardNumber; // 身份证号

    private String contactInfo; // 联系方式
    private boolean isMember;   // 是否为会员

    public Customer() {
    }

    public Customer(String name, Gender gender, String idCardNumber, String contactInfo) {
        this.name = name;
        this.gender = gender;
        this.idCardNumber = idCardNumber;
        this.contactInfo = contactInfo;
        this.isMember = false; // 默认为非会员
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setMember(boolean member) {
        isMember = member;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", idCardNumber='" + idCardNumber + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", isMember=" + isMember +
                '}';
    }
}