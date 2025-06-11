// src/main/java/com/hotel/controller/FrontController.java
package com.hotel.controller;

import com.hotel.model.*;
import com.hotel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/front")
public class FrontController {

    @Autowired
    private HotelService hotelService;

    @GetMapping("/checkin")
    public String showCheckinForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("rooms", hotelService.getAvailableRooms());
        return "front/checkin";
    }

    @PostMapping("/checkin")
    public String processCheckin(@ModelAttribute Customer customer,
                                 @RequestParam String roomNumber,
                                 Model model) {
        // 创建服务项列表（初始为空）
        List<ServiceItem> services = new ArrayList<>();
        CheckInRecord record = hotelService.checkIn(customer, roomNumber, services);

        model.addAttribute("recordId", record.getId());
        model.addAttribute("roomNumber", roomNumber);
        return "redirect:/front/services?recordId=" + record.getId();
    }

    @GetMapping("/services")
    public String showServices(@RequestParam String recordId, Model model) {
        model.addAttribute("services", hotelService.getAllServices());
        model.addAttribute("recordId", recordId);
        model.addAttribute("serviceItem", new ServiceItem());
        return "front/services";
    }

    @PostMapping("/addService")
    public String addService(@RequestParam String recordId,
                             @ModelAttribute ServiceItem serviceItem) {
        hotelService.addServiceToRecord(recordId, serviceItem);
        return "redirect:/front/services?recordId=" + recordId;
    }
}