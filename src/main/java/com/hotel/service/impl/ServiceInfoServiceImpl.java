package com.hotel.service.impl;

import com.hotel.model.ServiceInfo;
import com.hotel.repository.ServiceInfoRepository;
import com.hotel.service.ServiceInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ServiceInfoServiceImpl implements ServiceInfoService {

    private final ServiceInfoRepository serviceInfoRepository;

    @Autowired
    public ServiceInfoServiceImpl(ServiceInfoRepository serviceInfoRepository) {
        this.serviceInfoRepository = serviceInfoRepository;
    }

    @Override
    public ServiceInfo createServiceInfo(ServiceInfo serviceInfo) {
        if (serviceInfoRepository.existsByServiceName(serviceInfo.getServiceName())) {
            throw new IllegalArgumentException("服务名称已存在: " + serviceInfo.getServiceName());
        }
        return serviceInfoRepository.save(serviceInfo);
    }

    @Override
    public Optional<ServiceInfo> getServiceInfoById(String id) {
        return serviceInfoRepository.findById(id);
    }

    @Override
    public Optional<ServiceInfo> getServiceInfoByName(String name) {
        return serviceInfoRepository.findByServiceName(name);
    }

    @Override
    public List<ServiceInfo> getAllServiceInfos() {
        return serviceInfoRepository.findAll();
    }

    @Override
    public ServiceInfo updateServiceInfo(String id, ServiceInfo serviceInfoDetails) {
        ServiceInfo serviceInfo = serviceInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到服务ID: " + id));

        if (serviceInfoDetails.getServiceName() != null && !serviceInfoDetails.getServiceName().equals(serviceInfo.getServiceName())) {
            if (serviceInfoRepository.existsByServiceName(serviceInfoDetails.getServiceName())) {
                throw new IllegalArgumentException("更新的服务名称已存在: " + serviceInfoDetails.getServiceName());
            }
            serviceInfo.setServiceName(serviceInfoDetails.getServiceName());
        }
        if (serviceInfoDetails.getServicePrice() >= 0) {
            serviceInfo.setServicePrice(serviceInfoDetails.getServicePrice());
        }
        return serviceInfoRepository.save(serviceInfo);
    }

    @Override
    public void deleteServiceInfo(String id) {
        if (!serviceInfoRepository.existsById(id)) {
            throw new IllegalArgumentException("未找到服务ID: " + id);
        }
        // 注意：删除服务前可能需要检查是否有入住记录关联此服务
        serviceInfoRepository.deleteById(id);
    }

    @Override
    public boolean serviceNameExists(String serviceName) {
        return serviceInfoRepository.existsByServiceName(serviceName);
    }

    @Override
    @Transactional
    public void deleteAllServiceInfos() {
        serviceInfoRepository.deleteAll();
    }
}