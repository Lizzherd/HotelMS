package com.hotel.controller;

import com.hotel.model.*;
import com.hotel.model.enums.Gender;
import com.hotel.model.enums.MemberLevel;
import com.hotel.model.enums.RoomType;
import com.hotel.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final ServiceInfoService serviceInfoService;
    private final MemberInfoService memberInfoService;
    private final CheckInService checkInService;
    private final ServiceRequestService serviceRequestService;

    @Value("${admin.secret-key}")
    private String adminSecretKey;

    private static final String ADMIN_SESSION_ATTRIBUTE = "isAdminLoggedIn";

    @Autowired
    public AdminController(CustomerService customerService, RoomService roomService,
                           ServiceInfoService serviceInfoService, MemberInfoService memberInfoService,
                           CheckInService checkInService,
                           ServiceRequestService serviceRequestService) {
        this.customerService = customerService;
        this.roomService = roomService;
        this.serviceInfoService = serviceInfoService;
        this.memberInfoService = memberInfoService;
        this.checkInService = checkInService;
        this.serviceRequestService = serviceRequestService;
    }

    private boolean isAdminLoggedIn(HttpSession session) {
        return session != null && Boolean.TRUE.equals(session.getAttribute(ADMIN_SESSION_ATTRIBUTE));
    }

    @GetMapping("/login")
    public String adminLoginPage(HttpSession session) {
        if (isAdminLoggedIn(session)) {
            return "redirect:/admin";
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String processAdminLogin(@RequestParam String secretKey, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (adminSecretKey.equals(secretKey)) {
            request.getSession().setAttribute(ADMIN_SESSION_ATTRIBUTE, true);
            return "redirect:/admin";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "密钥无效，请重试。");
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/logout")
    public String adminLogout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(ADMIN_SESSION_ATTRIBUTE);
            session.invalidate();
        }
        redirectAttributes.addFlashAttribute("logoutMessage", "您已成功退出。");
        return "redirect:/admin/login";
    }

    @GetMapping("")
    public String adminDashboard(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录以访问管理区域。");
            return "redirect:/admin/login";
        }
        model.addAttribute("activeCheckInsCount", checkInService.getAllActiveCheckIns().size());
        model.addAttribute("availableRoomsCount", roomService.getAvailableRooms().size());
        model.addAttribute("totalCustomersCount", customerService.getAllCustomers().size());
        model.addAttribute("totalMembersCount", memberInfoService.getAllMemberInfos().size());
        return "admin/admin-dashboard";
    }

    @GetMapping("/perform-reset-all-data")
    public String performResetAllData(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        try {
            checkInService.deleteAllCheckIns();
            memberInfoService.deleteAllMemberInfos();
            customerService.deleteAllCustomers();
            roomService.deleteAllRooms();
            serviceInfoService.deleteAllServiceInfos();
            redirectAttributes.addFlashAttribute("successMessage", "所有系统记录已成功重置！");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "重置数据时发生错误：" + e.getMessage());
        }
        return "redirect:/admin";
    }

    // --- 客户管理 ---
    @GetMapping("/customers")
    public String listCustomers(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        model.addAttribute("customers", customerService.getAllCustomers());
        return "admin/manage-customers";
    }

    @GetMapping("/customers/new")
    public String showCreateCustomerForm(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        model.addAttribute("customer", new Customer());
        model.addAttribute("genders", Gender.values());
        return "admin/form-customer";
    }

    @PostMapping("/customers/save")
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            if (customer.getId() == null || customer.getId().isEmpty()) {
                if (customerService.idCardExists(customer.getIdCardNumber())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "创建失败：身份证号 " + customer.getIdCardNumber() + " 已存在。");
                    return "redirect:/admin/customers/new";
                }
                customerService.createCustomer(customer);
                redirectAttributes.addFlashAttribute("successMessage", "客户创建成功！");
            } else {
                customerService.updateCustomer(customer.getId(), customer);
                redirectAttributes.addFlashAttribute("successMessage", "客户更新成功！");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败: " + e.getMessage());
            return (customer.getId() == null || customer.getId().isEmpty()) ? "redirect:/admin/customers/new" : "redirect:/admin/customers/edit/" + customer.getId();
        }
        return "redirect:/admin/customers";
    }

    @GetMapping("/customers/edit/{id}")
    public String showEditCustomerForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        Optional<Customer> customerOpt = customerService.getCustomerById(id);
        if (customerOpt.isPresent()) {
            model.addAttribute("customer", customerOpt.get());
            model.addAttribute("genders", Gender.values());
            return "admin/form-customer";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "未找到客户ID: " + id);
            return "redirect:/admin/customers";
        }
    }

    @GetMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable String id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            Customer customer = customerService.getCustomerById(id)
                    .orElseThrow(() -> new IllegalArgumentException("客户不存在"));
            if (!checkInService.getActiveCheckInsByCustomer(customer.getIdCardNumber()).isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "删除失败：该客户尚有未结账的入住记录。");
                return "redirect:/admin/customers";
            }
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("successMessage", "客户删除成功！");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/customers";
    }

    // --- 客房管理 ---
    @GetMapping("/rooms")
    public String listRooms(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/manage-rooms";
    }

    @GetMapping("/rooms/new")
    public String showCreateRoomForm(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", RoomType.values());
        return "admin/form-room";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute Room room, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            if (room.getId() == null || room.getId().isEmpty()) {
                if (roomService.roomNumberExists(room.getRoomNumber())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "创建失败：房间号 " + room.getRoomNumber() + " 已存在。");
                    return "redirect:/admin/rooms/new";
                }
                roomService.createRoom(room);
                redirectAttributes.addFlashAttribute("successMessage", "客房创建成功！");
            } else {
                roomService.updateRoom(room.getId(), room);
                redirectAttributes.addFlashAttribute("successMessage", "客房更新成功！");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败: " + e.getMessage());
            return (room.getId() == null || room.getId().isEmpty()) ? "redirect:/admin/rooms/new" : "redirect:/admin/rooms/edit/" + room.getId();
        }
        return "redirect:/admin/rooms";
    }

    @GetMapping("/rooms/edit/{id}")
    public String showEditRoomForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        Optional<Room> roomOpt = roomService.getRoomById(id);
        if (roomOpt.isPresent()) {
            model.addAttribute("room", roomOpt.get());
            model.addAttribute("roomTypes", RoomType.values());
            return "admin/form-room";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "未找到客房ID: " + id);
            return "redirect:/admin/rooms";
        }
    }

    @GetMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable String id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "客房删除成功！");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    // --- 服务管理 ---
    @GetMapping("/services")
    public String listServices(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        model.addAttribute("services", serviceInfoService.getAllServiceInfos());
        return "admin/manage-services";
    }

    @GetMapping("/services/new")
    public String showCreateServiceForm(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        model.addAttribute("serviceInfo", new ServiceInfo());
        return "admin/form-service";
    }

    @PostMapping("/services/save")
    public String saveService(@ModelAttribute ServiceInfo serviceInfo, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            if (serviceInfo.getId() == null || serviceInfo.getId().isEmpty()) {
                if (serviceInfoService.serviceNameExists(serviceInfo.getServiceName())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "创建失败：服务名称 '" + serviceInfo.getServiceName() + "' 已存在。");
                    return "redirect:/admin/services/new";
                }
                serviceInfoService.createServiceInfo(serviceInfo);
                redirectAttributes.addFlashAttribute("successMessage", "服务创建成功！");
            } else {
                serviceInfoService.updateServiceInfo(serviceInfo.getId(), serviceInfo);
                redirectAttributes.addFlashAttribute("successMessage", "服务更新成功！");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败: " + e.getMessage());
            return (serviceInfo.getId() == null || serviceInfo.getId().isEmpty()) ? "redirect:/admin/services/new" : "redirect:/admin/services/edit/" + serviceInfo.getId();
        }
        return "redirect:/admin/services";
    }

    @GetMapping("/services/edit/{id}")
    public String showEditServiceForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        Optional<ServiceInfo> serviceOpt = serviceInfoService.getServiceInfoById(id);
        if (serviceOpt.isPresent()) {
            model.addAttribute("serviceInfo", serviceOpt.get());
            return "admin/form-service";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "未找到服务ID: " + id);
            return "redirect:/admin/services";
        }
    }

    @GetMapping("/services/delete/{id}")
    public String deleteService(@PathVariable String id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            serviceInfoService.deleteServiceInfo(id);
            redirectAttributes.addFlashAttribute("successMessage", "服务删除成功！");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    // --- 会员管理 ---
    @GetMapping("/members")
    public String listMembers(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        model.addAttribute("members", memberInfoService.getAllMemberInfos());
        model.addAttribute("customerService", customerService);
        return "admin/manage-members";
    }

    @GetMapping("/members/new")
    public String showCreateMemberForm(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        model.addAttribute("memberInfo", new MemberInfo());
        model.addAttribute("customers", customerService.getAllCustomers().stream().filter(c -> !c.isMember()).toList());
        model.addAttribute("memberLevels", MemberLevel.values());
        return "admin/form-member";
    }

    @PostMapping("/members/register")
    public String registerMember(@RequestParam String customerIdCardNumber, @RequestParam MemberLevel memberLevel, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            memberInfoService.registerOrUpdateMember(customerIdCardNumber, memberLevel);
            redirectAttributes.addFlashAttribute("successMessage", "会员注册/更新成功！");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/members";
    }

    @PostMapping("/members/save")
    public String saveMember(@ModelAttribute MemberInfo memberInfo, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            if (memberInfo.getId() == null || memberInfo.getId().isEmpty()) {
                memberInfoService.createMemberInfo(memberInfo);
                customerService.getCustomerByIdCardNumber(memberInfo.getIdCardNumber()).ifPresent(c -> {
                    c.setMember(true);
                    customerService.updateCustomer(c.getId(), c);
                });
                redirectAttributes.addFlashAttribute("successMessage", "会员信息创建成功！");
            } else {
                memberInfoService.updateMemberInfo(memberInfo.getId(), memberInfo);
                customerService.getCustomerByIdCardNumber(memberInfo.getIdCardNumber()).ifPresent(c -> {
                    c.setMember(memberInfo.getMemberLevel() != MemberLevel.NONE);
                    customerService.updateCustomer(c.getId(), c);
                });
                redirectAttributes.addFlashAttribute("successMessage", "会员信息更新成功！");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败: " + e.getMessage());
            return (memberInfo.getId() == null || memberInfo.getId().isEmpty()) ? "redirect:/admin/members/new" : "redirect:/admin/members/edit/" + memberInfo.getId();
        }
        return "redirect:/admin/members";
    }

    @GetMapping("/members/edit/{id}")
    public String showEditMemberForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        Optional<MemberInfo> memberOpt = memberInfoService.getMemberInfoById(id);
        if (memberOpt.isPresent()) {
            MemberInfo memberInfo = memberOpt.get();
            model.addAttribute("memberInfo", memberInfo);
            model.addAttribute("memberLevels", MemberLevel.values());
            customerService.getCustomerByIdCardNumber(memberInfo.getIdCardNumber())
                    .ifPresent(c -> model.addAttribute("customerName", c.getName()));
            return "admin/form-member-edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "未找到会员ID: " + id);
            return "redirect:/admin/members";
        }
    }

    @GetMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable String id, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            memberInfoService.deleteMemberInfo(id);
            redirectAttributes.addFlashAttribute("successMessage", "会员资格已移除！");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/members";
    }

    // --- 入住记录管理 ---
    @GetMapping("/checkins")
    public String listCheckIns(@RequestParam(required = false, defaultValue = "active") String viewType, Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        List<CheckIn> checkIns;
        if ("history".equalsIgnoreCase(viewType)) {
            checkIns = checkInService.getAllCheckInHistory();
            model.addAttribute("showingHistory", true);
        } else {
            checkIns = checkInService.getAllActiveCheckIns();
            model.addAttribute("showingHistory", false);
        }
        model.addAttribute("checkIns", checkIns);
        return "admin/view-checkins";
    }

    @GetMapping("/checkins/details/{id}")
    public String viewCheckInDetails(@PathVariable String id, Model model, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "请先登录。");
            return "redirect:/admin/login";
        }
        Optional<CheckIn> checkInOpt = checkInService.getCheckInById(id);
        if (checkInOpt.isPresent()) {
            CheckIn checkIn = checkInOpt.get();
            model.addAttribute("checkIn", checkIn);

            // 服务申请处理数据
            List<ServiceRequest> allRequests = serviceRequestService.findByCheckInId(checkIn.getId());
            model.addAttribute("allRequests", allRequests);

            List<ServiceRequest> completedRequests = allRequests.stream()
                    .filter(req -> "COMPLETED".equals(req.getStatus()))
                    .collect(Collectors.toList());
            model.addAttribute("completedRequests", completedRequests);

            BigDecimal completedRequestsTotal = completedRequests.stream()
                    .map(ServiceRequest::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("completedRequestsTotal", completedRequestsTotal);

            customerService.getCustomerById(checkIn.getCustomerId()).ifPresent(c -> model.addAttribute("customer", c));
            roomService.getRoomById(checkIn.getRoomId()).ifPresent(r -> model.addAttribute("room", r));
            return "admin/details-checkin";
        }
        redirectAttributes.addFlashAttribute("errorMessage", "未找到入住记录ID: " + id);
        return "redirect:/admin/checkins";
    }

    @PostMapping("/checkins/checkout/{checkInId}")
    public String processCheckOut(@PathVariable String checkInId, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (!isAdminLoggedIn(session)) {
            redirectAttributes.addFlashAttribute("errorMessage", "会话已过期，请重新登录。");
            return "redirect:/admin/login";
        }
        try {
            CheckIn checkedOut = checkInService.processCheckOut(checkInId);
            redirectAttributes.addFlashAttribute("successMessage", "房间 " + checkedOut.getRoomNumber() + " 退房成功！总金额：" + String.format("%.2f", checkedOut.getTotalAmount()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "退房失败: " + e.getMessage());
        }
        return "redirect:/admin/checkins";
    }

    // --- 服务申请处理区 ---
    @PostMapping("/service-requests/complete")
    public String completeServiceRequest(@RequestParam String requestId,
                                         @RequestParam(required = false) String adminNote,
                                         @RequestParam(required = false) String redirectCheckInId,
                                         Model model,
                                         RedirectAttributes redirectAttributes,
                                         HttpServletRequest request) {
        serviceRequestService.markCompleted(requestId, adminNote);
        model.addAttribute("successMessage", "服务申请已标记为完成！");

        // 仍然留在入住详情页，重新查一遍该入住信息及相关数据
        if (redirectCheckInId != null && !redirectCheckInId.isEmpty()) {
            Optional<CheckIn> checkInOpt = checkInService.getCheckInById(redirectCheckInId);
            if (checkInOpt.isPresent()) {
                CheckIn checkIn = checkInOpt.get();
                model.addAttribute("checkIn", checkIn);

                List<ServiceRequest> allRequests = serviceRequestService.findByCheckInId(checkIn.getId());
                model.addAttribute("allRequests", allRequests);

                List<ServiceRequest> completedRequests = allRequests.stream()
                        .filter(req -> "COMPLETED".equals(req.getStatus()))
                        .collect(Collectors.toList());
                model.addAttribute("completedRequests", completedRequests);

                BigDecimal completedRequestsTotal = completedRequests.stream()
                        .map(ServiceRequest::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("completedRequestsTotal", completedRequestsTotal);

                customerService.getCustomerById(checkIn.getCustomerId()).ifPresent(c -> model.addAttribute("customer", c));
                roomService.getRoomById(checkIn.getRoomId()).ifPresent(r -> model.addAttribute("room", r));
                return "admin/details-checkin";
            }
            model.addAttribute("errorMessage", "未找到入住记录ID: " + redirectCheckInId);
            return "admin/view-checkins";
        }
        // 如果没有 redirectCheckInId，回到入住列表页
        redirectAttributes.addFlashAttribute("successMessage", "服务申请已标记为完成！");
        return "redirect:/admin/checkins";
    }

    @PostMapping("/service-requests/reject")
    public String rejectServiceRequest(@RequestParam String requestId,
                                       @RequestParam(required = false) String adminNote,
                                       @RequestParam(required = false) String redirectCheckInId,
                                       Model model,
                                       RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) {
        serviceRequestService.markRejected(requestId, adminNote);
        model.addAttribute("successMessage", "服务申请已拒绝！");

        // 仍然留在入住详情页，重新查一遍该入住信息及相关数据
        if (redirectCheckInId != null && !redirectCheckInId.isEmpty()) {
            Optional<CheckIn> checkInOpt = checkInService.getCheckInById(redirectCheckInId);
            if (checkInOpt.isPresent()) {
                CheckIn checkIn = checkInOpt.get();
                model.addAttribute("checkIn", checkIn);

                List<ServiceRequest> allRequests = serviceRequestService.findByCheckInId(checkIn.getId());
                model.addAttribute("allRequests", allRequests);

                List<ServiceRequest> completedRequests = allRequests.stream()
                        .filter(req -> "COMPLETED".equals(req.getStatus()))
                        .collect(Collectors.toList());
                model.addAttribute("completedRequests", completedRequests);

                BigDecimal completedRequestsTotal = completedRequests.stream()
                        .map(ServiceRequest::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                model.addAttribute("completedRequestsTotal", completedRequestsTotal);

                customerService.getCustomerById(checkIn.getCustomerId()).ifPresent(c -> model.addAttribute("customer", c));
                roomService.getRoomById(checkIn.getRoomId()).ifPresent(r -> model.addAttribute("room", r));
                return "admin/details-checkin";
            }
            model.addAttribute("errorMessage", "未找到入住记录ID: " + redirectCheckInId);
            return "admin/view-checkins";
        }
        // 如果没有 redirectCheckInId，回到入住列表页
        redirectAttributes.addFlashAttribute("successMessage", "服务申请已拒绝！");
        return "redirect:/admin/checkins";
    }
}