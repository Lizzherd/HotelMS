// src/main/java/com/hotel/model/CheckInRecord.java
package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "checkin_records")
public class CheckInRecord {
    @Id
    private String id;
    private String customerId;
    private String roomId;
    private LocalDateTime checkInTime;
    private List<ServiceItem> services;

    // 构造器、getter、setter...

    public java.lang.String getId() {
        return id;
    }
}