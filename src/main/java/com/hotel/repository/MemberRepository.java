// src/main/java/com/hotel/repository/MemberRepository.java
package com.hotel.repository;

import com.hotel.model.Member;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberRepository extends MongoRepository<Member, String> {
    Member findByIdCard(String idCard);
}