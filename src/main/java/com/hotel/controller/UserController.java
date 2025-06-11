package com.hotel.controller;

import com.hotel.model.*;
import com.hotel.model.enums.Gender;
import com.hotel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final CheckInService checkInService;
    private final ServiceInfoService serviceInfoService;

    @Autowired
    public UserController(CustomerService customerService, RoomService roomService,
                          CheckInService checkInService, ServiceInfoService serviceInfoService) {
        this.customerService = customerService;
        this.roomService = roomService;
        this.checkInService = checkInService;
        this.serviceInfoService = serviceInfoService;
    }

    // 用户首页或引导页
    @GetMapping("")
    public String userIndex() {
        return "redirect:/user/checkin"; // 直接重定向到入住登记
    }

    // 显示入住登记表单
    @GetMapping("/checkin")
    public String showCheckInForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("availableRooms", roomService.getAvailableRooms());
        model.addAttribute("genders", Gender.values());
        return "user/user-checkin";
    }

    // 处理入住登记
    @PostMapping("/checkin")
    public String processCheckInForm(@ModelAttribute Customer customer,
                                     @RequestParam String roomId,
                                     @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate expectedCheckOutDate,
                                     RedirectAttributes redirectAttributes, Model model) {
        try {
            // 检查或创建客户
            Optional<Customer> existingCustomerOpt = customerService.getCustomerByIdCardNumber(customer.getIdCardNumber());
            Customer currentCustomer;
            if (existingCustomerOpt.isPresent()) {
                currentCustomer = existingCustomerOpt.get();
                // 可以选择更新客户信息
                currentCustomer.setName(customer.getName());
                currentCustomer.setGender(customer.getGender());
                currentCustomer.setContactInfo(customer.getContactInfo());
                customerService.updateCustomer(currentCustomer.getId(), currentCustomer);
            } else {
                if (customer.getName() == null || customer.getName().trim().isEmpty() ||
                        customer.getIdCardNumber() == null || customer.getIdCardNumber().trim().isEmpty() ||
                        customer.getContactInfo() == null || customer.getContactInfo().trim().isEmpty() ||
                        customer.getGender() == null) {
                    redirectAttributes.addFlashAttribute("errorMessage", "客户信息不完整，请填写所有必填项。");
                    return "redirect:/user/checkin";
                }
                currentCustomer = customerService.createCustomer(customer);
            }

            Room room = roomService.getRoomById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("无效的房间ID: " + roomId));

            if (room.isOccupied()) {
                redirectAttributes.addFlashAttribute("errorMessage", "房间 " + room.getRoomNumber() + " 刚刚已被预订，请选择其他房间。");
                return "redirect:/user/checkin";
            }

            if (expectedCheckOutDate == null || expectedCheckOutDate.isBefore(LocalDate.now().plusDays(1))) {
                redirectAttributes.addFlashAttribute("errorMessage", "预计离店日期必须是明天或之后。");
                return "redirect:/user/checkin";
            }


            CheckIn checkIn = checkInService.processCheckIn(currentCustomer, room, expectedCheckOutDate);
            redirectAttributes.addFlashAttribute("successMessage", "入住登记成功！房间号：" + room.getRoomNumber());
            // 重定向到为该入住选择服务的页面
            return "redirect:/user/services/" + checkIn.getId();
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "入住登记失败: " + e.getMessage());
            // 为了用户体验，可以考虑将已填写的customer信息和可选房间列表再次传回表单
            // model.addAttribute("customer", customer); // 如果表单支持回填
            // model.addAttribute("availableRooms", roomService.getAvailableRooms());
            // model.addAttribute("genders", Gender.values());
            return "redirect:/user/checkin";
        }
    }

    // 显示为特定入住选择服务的页面
    @GetMapping("/services/{checkInId}")
    public String showSelectServicesForm(@PathVariable String checkInId, Model model, RedirectAttributes redirectAttributes) {
        Optional<CheckIn> checkInOpt = checkInService.getActiveCheckInById(checkInId);
        if (checkInOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "未找到有效的入住记录，或该入住已结束。");
            return "redirect:/user/checkin";
        }
        CheckIn checkIn = checkInOpt.get();
        List<ServiceInfo> allServices = serviceInfoService.getAllServiceInfos();

        model.addAttribute("checkIn", checkIn);
        model.addAttribute("allServices", allServices);
        model.addAttribute("serviceItem", new CheckInServiceItem()); // 用于表单绑定
        return "user/user-select-services";
    }

    // 为特定入住添加服务
    @PostMapping("/services/{checkInId}/add")
    public String addServiceToCheckIn(@PathVariable String checkInId,
                                      @RequestParam String serviceInfoId,
                                      @RequestParam int quantity,
                                      RedirectAttributes redirectAttributes) {
        try {
            if (quantity <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "服务数量必须大于0。");
                return "redirect:/user/services/" + checkInId;
            }
            com.hotel.model.ServiceInfo serviceInfoModel = serviceInfoService.getServiceInfoById(serviceInfoId)
                    .orElseThrow(() -> new IllegalArgumentException("无效的服务ID: " + serviceInfoId));

            checkInService.addServiceToCheckIn(checkInId, serviceInfoModel, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "服务添加成功!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "添加服务失败: " + e.getMessage());
        }
        return "redirect:/user/services/" + checkInId;
    }

    // 用户完成服务选择（或跳过）
    @PostMapping("/services/{checkInId}/complete")
    public String completeServiceSelection(@PathVariable String checkInId, RedirectAttributes redirectAttributes) {
        Optional<CheckIn> checkInOpt = checkInService.getActiveCheckInById(checkInId);
        if (checkInOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作的入住记录无效。");
            return "redirect:/user/checkin";
        }
        // 此处可以添加一些完成选择后的逻辑，比如打印账单预览等
        redirectAttributes.addFlashAttribute("successMessage", "入住流程完成！祝您入住愉快。");
        // 可以重定向到用户状态页面或首页
        return "redirect:/"; // 或者一个用户仪表盘页面
    }


    // AJAX端点：根据身份证号获取客户信息 (用于入住登记表单自动填充)
    @GetMapping("/api/customer/{idCardNumber}")
    @ResponseBody
    public Customer getCustomerByIdCard(@PathVariable String idCardNumber) {
        return customerService.getCustomerByIdCardNumber(idCardNumber).orElse(null);
    }
}