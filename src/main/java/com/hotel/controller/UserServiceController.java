package com.hotel.controller;

import com.hotel.model.CheckIn;
import com.hotel.model.ServiceInfo;
import com.hotel.model.ServiceRequest;
import com.hotel.service.CheckInService;
import com.hotel.service.ServiceInfoService;
import com.hotel.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user-service")
public class UserServiceController {

    @Autowired
    private ServiceInfoService serviceInfoService;
    @Autowired
    private ServiceRequestService serviceRequestService;
    @Autowired
    private CheckInService checkInService;

    // 服务申请界面首页（输入房间号）
    @GetMapping("")
    public String serviceHome() {
        return "user/user-service";
    }

    // 通过房间号查找入住记录
    @GetMapping("/login")
    public String loginByRoom(@RequestParam String roomNumber, Model model) {
        List<CheckIn> actives = checkInService.getActiveCheckInsByRoom(roomNumber);
        if (actives.isEmpty()) {
            model.addAttribute("roomNumber", roomNumber);
            model.addAttribute("checkIn", null);
            return "user/user-service";
        }
        // 如有多条可让用户选择，这里默认取第一条
        CheckIn checkIn = actives.get(0);
        List<ServiceRequest> myRequests = serviceRequestService.findByCheckInId(checkIn.getId());
        model.addAttribute("roomNumber", roomNumber);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("allServices", serviceInfoService.getAllServiceInfos());
        model.addAttribute("myRequests", myRequests);
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
        serviceRequestService.applyService(req);

        redirectAttributes.addAttribute("roomNumber", checkIn.getRoomNumber());
        return "redirect:/user-service/login";
    }
}