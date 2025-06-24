package com.hotel.controller;

import com.hotel.model.*;
import com.hotel.model.enums.Gender;
import com.hotel.model.enums.RoomType;
import com.hotel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user-checkin")
public class UserCheckinController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final CheckInService checkInService;

    @Autowired
    public UserCheckinController(CustomerService customerService, RoomService roomService,
                                 CheckInService checkInService) {
        this.customerService = customerService;
        this.roomService = roomService;
        this.checkInService = checkInService;
    }

    // 显示入住登记表单
    @GetMapping("")
    public String showCheckInForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("genders", Gender.values());

        // 添加房间类型及其可用数量
        Map<RoomType, Long> roomTypeCounts = Arrays.stream(RoomType.values())
                .collect(Collectors.toMap(
                        type -> type,
                        type -> roomService.countAvailableRoomsByType(type)
                ));


        model.addAttribute("roomTypeCounts", roomTypeCounts);
        return "user/user-checkin";
    }

    // 处理入住登记
    @PostMapping("")
    public String processCheckInForm(@ModelAttribute Customer customer,
                                     @RequestParam String roomTypeId, // 修改为接收roomTypeId
                                     @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate expectedCheckOutDate,
                                     Model model) {
        model.addAttribute("genders", Gender.values());

        try {
            // 1. 处理客户信息
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

            // 2. 获取一个可用的房间
            RoomType roomType = RoomType.valueOf(roomTypeId);
            Optional<Room> availableRoom = roomService.findFirstAvailableRoomByType(roomType);

            if (!availableRoom.isPresent()) {
                model.addAttribute("errorMessage", "所选房间类型已无空房，请选择其他类型。");
                model.addAttribute("customer", customer);
                return "user/user-checkin";
            }

            Room room = availableRoom.get();

            // 3. 验证离店日期
            if (expectedCheckOutDate == null || expectedCheckOutDate.isBefore(LocalDate.now().plusDays(1))) {
                model.addAttribute("errorMessage", "预计离店日期必须是明天或之后。");
                model.addAttribute("customer", customer);
                return "user/user-checkin";
            }

            // 4. 处理入住
            CheckIn checkIn = checkInService.processCheckIn(currentCustomer, room, expectedCheckOutDate);
            model.addAttribute("successMessage", "入住登记成功！房间号：" + room.getRoomNumber());
            model.addAttribute("customer", new Customer());

            // 重新加载房间类型数据
            Map<RoomType, Long> roomTypeCounts = Arrays.stream(RoomType.values())
                    .collect(Collectors.toMap(
                            type -> type,
                            type -> roomService.countAvailableRoomsByType(type)
                    ));
            model.addAttribute("roomTypeCounts", roomTypeCounts);

            return "user/user-checkin";

        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("errorMessage", "入住登记失败: " + e.getMessage());
            model.addAttribute("customer", customer);
            return "user/user-checkin";
        }
    }

    // 根据身份证号获取客户信息
    @GetMapping("/api/customer/{idCardNumber}")
    @ResponseBody
    public Customer getCustomerByIdCard(@PathVariable String idCardNumber) {
        return customerService.getCustomerByIdCardNumber(idCardNumber).orElse(null);
    }
}