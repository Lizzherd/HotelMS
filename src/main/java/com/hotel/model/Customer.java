// src/main/java/com/hotel/model/Customer.java
package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "customers")
public class Customer {
    @Id
    private String id;
    private String name;
    private String gender;
    private String idCard;
    private String phone;
    private boolean isMember;

    // 构造器、getter、setter
    public Customer() {}

    // 全参构造器
    public Customer(String name, String gender, String idCard, String phone, boolean isMember) {
        this.name = name;
        this.gender = gender;
        this.idCard = idCard;
        this.phone = phone;
        this.isMember = isMember;
    }
    // getter和setter方法...
    public java.lang.String getId() {
        return id;
    }

    public java.lang.String getIdCard() {
        return idCard;
    }
}