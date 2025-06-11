package com.hotel.repository;

import com.hotel.model.MemberInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MemberInfoRepository extends MongoRepository<MemberInfo, String> {
    Optional<MemberInfo> findByIdCardNumber(String idCardNumber);
}