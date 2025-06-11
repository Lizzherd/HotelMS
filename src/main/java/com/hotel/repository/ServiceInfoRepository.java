package com.hotel.repository;

import com.hotel.model.ServiceInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ServiceInfoRepository extends MongoRepository<ServiceInfo, String> {
    Optional<ServiceInfo> findByServiceName(String serviceName);
    boolean existsByServiceName(String serviceName);
}