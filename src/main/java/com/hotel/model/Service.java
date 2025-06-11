// src/main/java/com/hotel/model/Service.java
package com.hotel.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;

@Document(collection = "services")
public class Service {
    @Id
    private String id;
    private String serviceName;
    private BigDecimal servicePrice;

    // 构造器、getter、setter...
}