package com.hotel.controller;

import com.hotel.model.*;
import com.hotel.model.enums.Gender;
import com.hotel.model.enums.MemberLevel;
import com.hotel.model.enums.RoomType;
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
@RequestMapping("/admin")
public class AdminController {

    private final CustomerService customerService;
    private final RoomService roomService;
    private final ServiceInfoService serviceInfoService;
    private final MemberInfoService memberInfoService;
    private final CheckInService checkInService;

    @Autowired
    public AdminController(CustomerService customerService, RoomService roomService,
                           ServiceInfoService serviceInfoService, MemberInfoService memberInfoService,
                           CheckInService checkInService) {
        this.customerService = customerService;
        this.roomService = roomService;
        this.serviceInfoService = serviceInfoService;
        this.memberInfoService = memberInfoService;
        this.checkInService = checkInService;
    }

    @GetMapping("")
    public String adminDashboard(Model model) {
        model.addAttribute("activeCheckInsCount", checkInService.getAllActiveCheckIns().size());
        model.addAttribute("availableRoomsCount", roomService.getAvailableRooms().size());
        model.addAttribute("totalCustomersCount", customerService.getAllCustomers().size());
        model.addAttribute("totalMembersCount", memberInfoService.getAllMemberInfos().size());
        return "admin/admin-dashboard";
    }

    // --- 客户管理 ---
    @GetMapping("/customers")
    public String listCustomers(Model model) {
        model.addAttribute("customers", customerService.getAllCustomers());
        return "admin/manage-customers";
    }

    @GetMapping("/customers/new")
    public String showCreateCustomerForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("genders", Gender.values());
        return "admin/form-customer"; // 一个通用的客户表单页
    }

    @PostMapping("/customers/save")
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        try {
            if (customer.getId() == null || customer.getId().isEmpty()) {
                if (customerService.idCardExists(customer.getIdCardNumber())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "创建失败：身份证号 " + customer.getIdCardNumber() + " 已存在。");
                    // 需要返回表单并填充数据，或重定向到new并提示
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
    public String showEditCustomerForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
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
    public String deleteCustomer(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            // 检查是否有活跃入住记录
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
    public String listRooms(Model model) {
        model.addAttribute("rooms", roomService.getAllRooms());
        return "admin/manage-rooms";
    }

    @GetMapping("/rooms/new")
    public String showCreateRoomForm(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", RoomType.values());
        return "admin/form-room";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@ModelAttribute Room room, RedirectAttributes redirectAttributes) {
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
    public String showEditRoomForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
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
    public String deleteRoom(@PathVariable String id, RedirectAttributes redirectAttributes) {
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
    public String listServices(Model model) {
        model.addAttribute("services", serviceInfoService.getAllServiceInfos());
        return "admin/manage-services";
    }

    @GetMapping("/services/new")
    public String showCreateServiceForm(Model model) {
        model.addAttribute("serviceInfo", new ServiceInfo());
        return "admin/form-service";
    }

    @PostMapping("/services/save")
    public String saveService(@ModelAttribute ServiceInfo serviceInfo, RedirectAttributes redirectAttributes) {
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
    public String showEditServiceForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
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
    public String deleteService(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            // TODO: 检查是否有入住记录正在使用此服务
            serviceInfoService.deleteServiceInfo(id);
            redirectAttributes.addFlashAttribute("successMessage", "服务删除成功！");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/services";
    }

    // --- 会员管理 ---
    @GetMapping("/members")
    public String listMembers(Model model) {
        model.addAttribute("members", memberInfoService.getAllMemberInfos());
        // 为了在列表页显示客户姓名，可能需要额外查询或在MemberInfo中冗余
        // 或者在模板中处理，根据idCardNumber查找Customer
        model.addAttribute("customerService", customerService); // 传递服务以便模板调用
        return "admin/manage-members";
    }

    @GetMapping("/members/new")
    public String showCreateMemberForm(Model model) {
        model.addAttribute("memberInfo", new MemberInfo());
        model.addAttribute("customers", customerService.getAllCustomers().stream().filter(c -> !c.isMember()).toList()); // 只显示非会员客户
        model.addAttribute("memberLevels", MemberLevel.values());
        return "admin/form-member";
    }

    @PostMapping("/members/register")
    public String registerMember(@RequestParam String customerIdCardNumber, @RequestParam MemberLevel memberLevel, RedirectAttributes redirectAttributes) {
        try {
            memberInfoService.registerOrUpdateMember(customerIdCardNumber, memberLevel);
            redirectAttributes.addFlashAttribute("successMessage", "会员注册/更新成功！");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/members";
    }


    @PostMapping("/members/save")
    public String saveMember(@ModelAttribute MemberInfo memberInfo, RedirectAttributes redirectAttributes) {
        try {
            if (memberInfo.getId() == null || memberInfo.getId().isEmpty()) {
                // 创建会员通常通过 registerOrUpdateMember 方法，这里简化处理
                // 需要确保 customer 表的 isMember 状态同步
                memberInfoService.createMemberInfo(memberInfo); // 此方法需要customer已存在
                // 更新 customer 的 isMember 状态
                customerService.getCustomerByIdCardNumber(memberInfo.getIdCardNumber()).ifPresent(c -> {
                    c.setMember(true);
                    customerService.updateCustomer(c.getId(), c);
                });
                redirectAttributes.addFlashAttribute("successMessage", "会员信息创建成功！");
            } else {
                memberInfoService.updateMemberInfo(memberInfo.getId(), memberInfo);
                // 更新 customer 的 isMember 状态
                customerService.getCustomerByIdCardNumber(memberInfo.getIdCardNumber()).ifPresent(c -> {
                    c.setMember(memberInfo.getMemberLevel() != MemberLevel.NONE); // 假设NONE为非会员
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
    public String showEditMemberForm(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<MemberInfo> memberOpt = memberInfoService.getMemberInfoById(id);
        if (memberOpt.isPresent()) {
            MemberInfo memberInfo = memberOpt.get();
            model.addAttribute("memberInfo", memberInfo);
            model.addAttribute("memberLevels", MemberLevel.values());
            // 为了显示客户姓名等，可以传递客户信息
            customerService.getCustomerByIdCardNumber(memberInfo.getIdCardNumber())
                    .ifPresent(c -> model.addAttribute("customerName", c.getName()));
            return "admin/form-member-edit"; // 一个专门的编辑表单
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "未找到会员ID: " + id);
            return "redirect:/admin/members";
        }
    }

    @GetMapping("/members/delete/{id}")
    public String deleteMember(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            memberInfoService.deleteMemberInfo(id); // 该方法内部会更新customer的isMember状态
            redirectAttributes.addFlashAttribute("successMessage", "会员资格已移除！");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "操作失败: " + e.getMessage());
        }
        return "redirect:/admin/members";
    }


    // --- 入住记录管理 ---
    @GetMapping("/checkins")
    public String listCheckIns(@RequestParam(required = false, defaultValue = "active") String viewType, Model model) {
        List<CheckIn> checkIns;
        if ("history".equalsIgnoreCase(viewType)) {
            checkIns = checkInService.getAllCheckInHistory(); // 或者只显示已退房的
            model.addAttribute("showingHistory", true);
        } else {
            checkIns = checkInService.getAllActiveCheckIns();
            model.addAttribute("showingHistory", false);
        }
        model.addAttribute("checkIns", checkIns);
        return "admin/view-checkins";
    }

    @GetMapping("/checkins/details/{id}")
    public String viewCheckInDetails(@PathVariable String id, Model model, RedirectAttributes redirectAttributes) {
        Optional<CheckIn> checkInOpt = checkInService.getCheckInById(id); // 查看历史也用这个
        if (checkInOpt.isPresent()) {
            model.addAttribute("checkIn", checkInOpt.get());
            // 可以加入客户和房间的详细信息
            customerService.getCustomerById(checkInOpt.get().getCustomerId()).ifPresent(c -> model.addAttribute("customer", c));
            roomService.getRoomById(checkInOpt.get().getRoomId()).ifPresent(r -> model.addAttribute("room", r));
            return "admin/details-checkin"; // 新建一个详细信息页面
        }
        redirectAttributes.addFlashAttribute("errorMessage", "未找到入住记录ID: " + id);
        return "redirect:/admin/checkins";
    }


    @PostMapping("/checkins/checkout/{checkInId}")
    public String processCheckOut(@PathVariable String checkInId, RedirectAttributes redirectAttributes) {
        try {
            CheckIn checkedOut = checkInService.processCheckOut(checkInId);
            redirectAttributes.addFlashAttribute("successMessage", "房间 " + checkedOut.getRoomNumber() + " 退房成功！总金额：" + String.format("%.2f", checkedOut.getTotalAmount()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "退房失败: " + e.getMessage());
        }
        return "redirect:/admin/checkins"; // 返回到活跃入住列表
    }

    // 后台也可以为活跃的入住记录添加服务
    @GetMapping("/checkins/addservice/{checkInId}")
    public String showAdminAddServiceToCheckInForm(@PathVariable String checkInId, Model model, RedirectAttributes redirectAttributes) {
        Optional<CheckIn> checkInOpt = checkInService.getActiveCheckInById(checkInId);
        if (checkInOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "未找到有效的入住记录，或该入住已结束。");
            return "redirect:/admin/checkins";
        }
        CheckIn checkIn = checkInOpt.get();
        List<ServiceInfo> allServices = serviceInfoService.getAllServiceInfos();

        model.addAttribute("checkIn", checkIn);
        model.addAttribute("allServices", allServices);
        return "admin/form-addservice-to-checkin"; // 后台添加服务的表单
    }

    @PostMapping("/checkins/addservice/{checkInId}")
    public String adminAddServiceToCheckIn(@PathVariable String checkInId,
                                           @RequestParam String serviceInfoId,
                                           @RequestParam int quantity,
                                           RedirectAttributes redirectAttributes) {
        try {
            if (quantity <= 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "服务数量必须大于0。");
                return "redirect:/admin/checkins/addservice/" + checkInId;
            }
            com.hotel.model.ServiceInfo serviceInfoModel = serviceInfoService.getServiceInfoById(serviceInfoId)
                    .orElseThrow(() -> new IllegalArgumentException("无效的服务ID: " + serviceInfoId));

            checkInService.addServiceToCheckIn(checkInId, serviceInfoModel, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "服务添加成功!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "添加服务失败: " + e.getMessage());
        }
        // 返回到入住详情页或服务添加页
        return "redirect:/admin/checkins/details/" + checkInId;
    }

}