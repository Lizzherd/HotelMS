package com.hotel.controller;

import com.hotel.model.CheckIn;
import com.hotel.model.ServiceInfo;
import com.hotel.model.ServiceRequest;
import com.hotel.model.MemberInfo;
import com.hotel.service.CheckInService;
import com.hotel.service.ServiceInfoService;
import com.hotel.service.ServiceRequestService;
import com.hotel.service.MemberInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;



@Controller
@RequestMapping("/user-service")
public class UserServiceController {

    @Autowired
    private ServiceInfoService serviceInfoService;
    @Autowired
    private ServiceRequestService serviceRequestService;
    @Autowired
    private CheckInService checkInService;

    @Autowired
    private MemberInfoService memberInfoService;


    // 服务申请界面首页（输入房间号），一定要补全所有模板变量，防止初次访问时报错
    @GetMapping("")
    public String serviceHome(Model model) {
        model.addAttribute("roomNumber", null);
        model.addAttribute("checkIn", null);
        model.addAttribute("allServices", Collections.emptyList());
        model.addAttribute("myRequests", Collections.emptyList());
        return "user/user-service";
    }

    @GetMapping("/login")
    public String loginByRoom(@RequestParam String roomNumber, Model model) {

        // 先默认折扣为1（无折扣）
        BigDecimal discountRate = BigDecimal.ONE;
        // 获取所有服务
        List<ServiceInfo> allServices = serviceInfoService.getAllServiceInfos();
        // 初始化折扣价列表
        List<BigDecimal> final_prices = new ArrayList<>();

        List<CheckIn> actives = checkInService.getActiveCheckInsByRoom(roomNumber);

        model.addAttribute("roomNumber", roomNumber);
        model.addAttribute("checkIn", null);
        model.addAttribute("allServices", allServices);
        model.addAttribute("myRequests", Collections.emptyList());
        model.addAttribute("discountRate", discountRate);

        if (!actives.isEmpty()) {
            CheckIn checkIn = actives.get(0);
            List<ServiceRequest> myRequests = serviceRequestService.findByCheckInId(checkIn.getId());
            model.addAttribute("roomNumber", roomNumber);
            model.addAttribute("checkIn", checkIn);
            model.addAttribute("allServices", serviceInfoService.getAllServiceInfos());
            model.addAttribute("myRequests", myRequests);

            // ✅ 查询会员信息
            Optional<MemberInfo> memberInfoOpt = memberInfoService.getMemberInfoByIdCardNumber(checkIn.getCustomerIdCardNumber());
            if (memberInfoOpt.isPresent()) {
                MemberInfo memberInfo = memberInfoOpt.get();
                model.addAttribute("memberInfo", memberInfo);
                discountRate = getDiscountRate(memberInfo);
            }
            model.addAttribute("discountRate", discountRate);

            // 遍历服务列表，计算折后价并赋值
            for (ServiceInfo service : allServices) {
                BigDecimal price = BigDecimal.valueOf(service.getServicePrice());
                BigDecimal finalPrice = price.multiply(discountRate)
                        .setScale(1, RoundingMode.HALF_UP); // 保留1位小数，四舍五入
                final_prices.add(finalPrice);
            }
            model.addAttribute("final_prices", final_prices);

        }

        return "user/user-service";
    }

    // 申请服务
    @PostMapping("/apply")
    public String applyService(@RequestParam String checkInId,
                               @RequestParam String serviceInfoId,
                               @RequestParam int quantity,
                               RedirectAttributes redirectAttributes) {
        CheckIn checkIn = checkInService.getCheckInById(checkInId).orElseThrow();
        ServiceInfo info = serviceInfoService.getServiceInfoById(serviceInfoId).orElseThrow();
        ServiceRequest req = new ServiceRequest();
        req.setCheckInId(checkInId);
        req.setCustomerIdCard(checkIn.getCustomerIdCardNumber());
        req.setServiceInfoId(serviceInfoId);
        req.setServiceName(info.getServiceName());
        req.setRoomNumber(checkIn.getRoomNumber());
        req.setQuantity(quantity);

        // 计算折后价
        BigDecimal originalPrice = BigDecimal.valueOf(info.getServicePrice());
        BigDecimal discountRate = getMemberDiscountRate(checkIn.getCustomerIdCardNumber()); // 获取会员折扣
        BigDecimal finalPrice = originalPrice.multiply(discountRate)
                .setScale(1, RoundingMode.HALF_UP); // 保留1位小数
        req.setUnitPrice(finalPrice); // 关键：赋值最终单价

        serviceRequestService.applyService(req);

        redirectAttributes.addAttribute("roomNumber", checkIn.getRoomNumber());
        return "redirect:/user-service/login";
    }

    private BigDecimal getDiscountRate(MemberInfo memberInfo) {
        switch (memberInfo.getMemberLevel()) {
            case BRONZE:
                return new BigDecimal("0.95");
            case SILVER:
                return new BigDecimal("0.90");
            case GOLD:
                return new BigDecimal("0.85");
            case PLATINUM:
                return new BigDecimal("0.80");
            default:
                return BigDecimal.ONE; // 非会员无折扣
        }
    }
    // 根据身份证号获取会员折扣率
    private BigDecimal getMemberDiscountRate(String idCardNumber) {
        Optional<MemberInfo> memberInfoOpt = memberInfoService.getMemberInfoByIdCardNumber(idCardNumber);
        return memberInfoOpt.map(this::getDiscountRate).orElse(BigDecimal.ONE); // 非会员默认无折扣
    }

}
