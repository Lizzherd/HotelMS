package com.hotel.controller;

import com.hotel.model.ServiceRequest;
import com.hotel.service.ServiceRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/service-requests")
public class AdminServiceRequestController {

    @Autowired
    private ServiceRequestService serviceRequestService;

    // 管理所有服务申请
    @GetMapping("")
    public String manageRequests(Model model) {
        model.addAttribute("allRequests", serviceRequestService.findAll());
        return "admin/admin-manage-service-requests";
    }

    // 标记为完成
    @PostMapping("/complete")
    public String completeRequest(@RequestParam String requestId,
                                  @RequestParam(required = false) String adminNote,
                                  RedirectAttributes redirectAttributes) {
        serviceRequestService.markCompleted(requestId, adminNote);
        redirectAttributes.addFlashAttribute("successMessage", "服务申请已标记为完成！");
        return "redirect:/admin/service-requests";
    }

    // 拒绝申请
    @PostMapping("/reject")
    public String rejectRequest(@RequestParam String requestId,
                                @RequestParam(required = false) String adminNote,
                                RedirectAttributes redirectAttributes) {
        serviceRequestService.markRejected(requestId, adminNote);
        redirectAttributes.addFlashAttribute("successMessage", "服务申请已拒绝！");
        return "redirect:/admin/service-requests";
    }
}