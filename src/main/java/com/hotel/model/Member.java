// src/main/java/com/hotel/model/Member.java
package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;

@Document(collection = "members")
public class Member {
    @Id
    private String id;
    private String idCard;
    private LocalDate registerDate;
    private String level;

    // 构造器、getter、setter...
}