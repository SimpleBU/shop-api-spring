package com.example.shop.service;

import com.example.shop.dto.UserDto;
import com.example.shop.dto.UserRole;
import com.example.shop.exception.ResourceNotFoundException;
import com.example.shop.model.AuditEvent;
import com.example.shop.model.SequenceGenerator;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class UserService {

    private final Map<String, UserDto> users = new ConcurrentHashMap<>();
    private final List<AuditEvent> auditTrail = new CopyOnWriteArrayList<>();
    private final SequenceGenerator ids = new SequenceGenerator("usr", 10);

    public UserService() {
        seed("anna@example.com", "Anna Petrova", UserRole.CUSTOMER);
        seed("boris@example.com", "Boris Smirnov", UserRole.CUSTOMER);
        seed("clara@example.com", "Clara Ivanova", UserRole.SUPPORT);
        seed("dmitry@example.com", "Dmitry Orlov", UserRole.ADMIN);
    }

    private void seed(String email, String displayName, UserRole role) {
        String id = ids.next();
        users.put(id, new UserDto(id, email, displayName, role, false, OffsetDateTime.now().minusYears(1)));
    }

    public List<UserDto> findAll(UserRole role, Boolean blocked) {
        return users.values().stream()
                .filter(u -> role == null || u.role() == role)
                .filter(u -> blocked == null || u.blocked() == blocked)
                .sorted(Comparator.comparing(UserDto::email))
                .toList();
    }

    public UserDto findById(String id) {
        UserDto user = users.get(id);
        if (user == null) {
            throw new ResourceNotFoundException("User", id);
        }
        return user;
    }

    public UserDto changeRole(String id, UserRole role, String actor) {
        UserDto updated = findById(id).withRole(role);
        users.put(id, updated);
        auditTrail.add(AuditEvent.now(actor, "ROLE_CHANGED", id));
        return updated;
    }

    public void purge(String id, String actor) {
        if (users.remove(id) == null) {
            throw new ResourceNotFoundException("User", id);
        }
        auditTrail.add(AuditEvent.now(actor, "USER_PURGED", id));
    }

    public List<AuditEvent> auditTrail() {
        return List.copyOf(auditTrail);
    }

    public int size() {
        return users.size();
    }
}
