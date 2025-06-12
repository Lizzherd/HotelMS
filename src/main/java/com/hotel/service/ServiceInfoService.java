package com.hotel.service;

import com.hotel.model.ServiceInfo;
import java.util.List;
import java.util.Optional;

public interface ServiceInfoService {
    ServiceInfo createServiceInfo(ServiceInfo serviceInfo);
    Optional<ServiceInfo> getServiceInfoById(String id);
    Optional<ServiceInfo> getServiceInfoByName(String name);
    List<ServiceInfo> getAllServiceInfos();
    ServiceInfo updateServiceInfo(String id, ServiceInfo serviceInfoDetails);
    void deleteServiceInfo(String id);
    boolean serviceNameExists(String serviceName);
    void deleteAllServiceInfos();
}