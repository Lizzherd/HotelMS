// src/main/java/com/hotel/controller/AdminController.java
package com.hotel.controller;

import com.hotel.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private HotelService hotelService;

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        double occupancyRate = hotelService.calculateOccupancyRate();
        model.addAttribute("occupancyRate", String.format("%.2f", occupancyRate));
        return "admin/dashboard";
    }
}