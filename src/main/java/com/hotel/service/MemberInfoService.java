package com.hotel.service;

import com.hotel.model.MemberInfo;
import com.hotel.model.enums.MemberLevel;
import java.util.List;
import java.util.Optional;

public interface MemberInfoService {
    MemberInfo createMemberInfo(MemberInfo memberInfo);
    Optional<MemberInfo> getMemberInfoById(String id);
    Optional<MemberInfo> getMemberInfoByIdCardNumber(String idCardNumber);
    List<MemberInfo> getAllMemberInfos();
    MemberInfo updateMemberInfo(String id, MemberInfo memberInfoDetails);
    void deleteMemberInfo(String id);
    MemberInfo registerOrUpdateMember(String customerIdCardNumber, MemberLevel level);
}