// src/main/java/com/hotel/model/Room.java
package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Document(collection = "rooms")
public class Room {
    @Id
    private String id;
    private String roomNumber;
    private BigDecimal price;
    private boolean occupied;
    private String type;

    // 构造器、getter、setter...
}