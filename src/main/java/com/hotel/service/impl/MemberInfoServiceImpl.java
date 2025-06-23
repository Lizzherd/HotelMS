package com.hotel.service.impl;

import com.hotel.model.Customer;
import com.hotel.model.MemberInfo;
import com.hotel.model.enums.MemberLevel;
import com.hotel.repository.CustomerRepository;
import com.hotel.repository.MemberInfoRepository;
import com.hotel.service.MemberInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MemberInfoServiceImpl implements MemberInfoService {

    private final MemberInfoRepository memberInfoRepository;
    private final CustomerRepository customerRepository;


    @Autowired
    public MemberInfoServiceImpl(MemberInfoRepository memberInfoRepository, CustomerRepository customerRepository) {
        this.memberInfoRepository = memberInfoRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public MemberInfo createMemberInfo(MemberInfo memberInfo) {
        // 确保关联的客户存在
        customerRepository.findByIdCardNumber(memberInfo.getIdCardNumber())
                .orElseThrow(() -> new IllegalArgumentException("创建会员失败：找不到身份证号为 " + memberInfo.getIdCardNumber() + " 的客户"));

        if (memberInfoRepository.findByIdCardNumber(memberInfo.getIdCardNumber()).isPresent()) {
            throw new IllegalArgumentException("该身份证号已注册会员: " + memberInfo.getIdCardNumber());
        }
        memberInfo.setRegistrationDate(LocalDate.now());
        return memberInfoRepository.save(memberInfo);
    }

    @Transactional
    @Override
    public MemberInfo registerOrUpdateMember(String customerIdCardNumber, MemberLevel level) {
        Customer customer = customerRepository.findByIdCardNumber(customerIdCardNumber)
                .orElseThrow(() -> new IllegalArgumentException("找不到客户: " + customerIdCardNumber));

        Optional<MemberInfo> existingMemberOpt = memberInfoRepository.findByIdCardNumber(customerIdCardNumber);
        MemberInfo memberInfo;
        if (existingMemberOpt.isPresent()) {
            memberInfo = existingMemberOpt.get();
            memberInfo.setMemberLevel(level);
        } else {
            memberInfo = new MemberInfo(customerIdCardNumber, LocalDate.now(), level);
        }

        customer.setMember(true);
        customerRepository.save(customer);

        return memberInfoRepository.save(memberInfo);
    }


    @Override
    public Optional<MemberInfo> getMemberInfoById(String id) {
        return memberInfoRepository.findById(id);
    }

    @Override
    public Optional<MemberInfo> getMemberInfoByIdCardNumber(String idCardNumber) {
        return memberInfoRepository.findByIdCardNumber(idCardNumber);
    }

    @Override
    public List<MemberInfo> getAllMemberInfos() {
        return memberInfoRepository.findAll();
    }

    @Override
    public MemberInfo updateMemberInfo(String id, MemberInfo memberInfoDetails) {
        MemberInfo memberInfo = memberInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到会员ID: " + id));


        if (memberInfoDetails.getMemberLevel() != null) memberInfo.setMemberLevel(memberInfoDetails.getMemberLevel());
        if (memberInfoDetails.getRegistrationDate() != null) memberInfo.setRegistrationDate(memberInfoDetails.getRegistrationDate());


        Customer customer = customerRepository.findByIdCardNumber(memberInfo.getIdCardNumber()).orElse(null);
        if (customer != null) {
            customer.setMember(memberInfo.getMemberLevel() != MemberLevel.NONE); // 假设NONE为非会员
            customerRepository.save(customer);
        }

        return memberInfoRepository.save(memberInfo);
    }

    @Override
    @Transactional
    public void deleteMemberInfo(String id) {
        MemberInfo memberInfo = memberInfoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("未找到会员ID: " + id));


        Customer customer = customerRepository.findByIdCardNumber(memberInfo.getIdCardNumber()).orElse(null);
        if (customer != null) {
            customer.setMember(false);
            customerRepository.save(customer);
        }
        memberInfoRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllMemberInfos() {
        memberInfoRepository.deleteAll();
    }
}