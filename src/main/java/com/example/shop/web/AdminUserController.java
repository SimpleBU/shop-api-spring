package com.example.shop.web;

import com.example.shop.dto.PageResponse;
import com.example.shop.dto.UserDto;
import com.example.shop.dto.UserRole;
import com.example.shop.dto.UserRoleUpdateRequest;
import com.example.shop.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<UserDto>> list(@RequestParam(required = false) UserRole role,
                                                      @RequestParam(required = false) Boolean blocked,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        List<UserDto> found = userService.findAll(role, blocked);
        return ResponseEntity.ok(PageResponse.of(found, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getOne(@PathVariable String id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDto> changeRole(@PathVariable String id,
                                              @Valid @RequestBody UserRoleUpdateRequest request,
                                              @RequestHeader(name = "X-Admin-Actor", required = false,
                                                      defaultValue = "system") String actor) {
        return ResponseEntity.ok(userService.changeRole(id, request.role(), actor));
    }

    @DeleteMapping("/{id}/purge")
    public ResponseEntity<Void> purge(@PathVariable String id,
                                      @RequestHeader(name = "X-Admin-Actor", required = false,
                                              defaultValue = "system") String actor) {
        userService.purge(id, actor);
        return ResponseEntity.noContent().build();
    }
}
