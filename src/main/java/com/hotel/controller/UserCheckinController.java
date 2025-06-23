package com.hotel.controller;

import com.hotel.model.*;
import com.hotel.model.enums.Gender;
import com.hotel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/user-checkin")
public class UserCheckinController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final CheckInService checkInService;

    @Autowired
    public UserCheckinController(CustomerService customerService, RoomService roomService,
                                 CheckInService checkInService, ServiceInfoService serviceInfoService) {
        this.customerService = customerService;
        this.roomService = roomService;
        this.checkInService = checkInService;
    }

    // 显示入住登记表单
    @GetMapping("")
    public String showCheckInForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("availableRooms", roomService.getAvailableRooms());
        model.addAttribute("genders", Gender.values());
        return "user/user-checkin";
    }

    // 处理入住登记
    @PostMapping("")
    public String processCheckInForm(@ModelAttribute Customer customer,
                                     @RequestParam String roomId,
                                     @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate expectedCheckOutDate,
                                     Model model) {
        model.addAttribute("availableRooms", roomService.getAvailableRooms());
        model.addAttribute("genders", Gender.values());
        try {
            Optional<Customer> existingCustomerOpt = customerService.getCustomerByIdCardNumber(customer.getIdCardNumber());
            Customer currentCustomer;
            if (existingCustomerOpt.isPresent()) {
                currentCustomer = existingCustomerOpt.get();
                currentCustomer.setName(customer.getName());
                currentCustomer.setGender(customer.getGender());
                currentCustomer.setContactInfo(customer.getContactInfo());
                customerService.updateCustomer(currentCustomer.getId(), currentCustomer);
            } else {
                if (customer.getName() == null || customer.getName().trim().isEmpty() ||
                        customer.getIdCardNumber() == null || customer.getIdCardNumber().trim().isEmpty() ||
                        customer.getContactInfo() == null || customer.getContactInfo().trim().isEmpty() ||
                        customer.getGender() == null) {
                    model.addAttribute("errorMessage", "客户信息不完整，请填写所有必填项。");
                    model.addAttribute("customer", customer);
                    return "user/user-checkin";
                }
                currentCustomer = customerService.createCustomer(customer);
            }

            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("无效的房间ID: " + roomId));

            if (room.isOccupied()) {
                model.addAttribute("errorMessage", "房间 " + room.getRoomNumber() + " 刚刚已被预订，请选择其他房间。");
                model.addAttribute("customer", customer);
                return "user/user-checkin";
            }

            if (expectedCheckOutDate == null || expectedCheckOutDate.isBefore(LocalDate.now().plusDays(1))) {
                model.addAttribute("errorMessage", "预计离店日期必须是明天或之后。");
                model.addAttribute("customer", customer);
                return "user/user-checkin";
            }

            CheckIn checkIn = checkInService.processCheckIn(currentCustomer, room, expectedCheckOutDate);
            model.addAttribute("successMessage", "入住登记成功！房间号：" + room.getRoomNumber());
            // 清空表单
            model.addAttribute("customer", new Customer());
            return "user/user-checkin";

        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", "入住登记失败: " + e.getMessage());
            model.addAttribute("customer", customer);
            return "user/user-checkin";
        }
    }

    //根据身份证号获取客户信息
    @GetMapping("/api/customer/{idCardNumber}")
    @ResponseBody
    public Customer getCustomerByIdCard(@PathVariable String idCardNumber) {
        return customerService.getCustomerByIdCardNumber(idCardNumber).orElse(null);
    }
}