package com.grabbill.server.controller;

import com.grabbill.core.entity.Privilege;
import com.grabbill.core.entity.Role;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.model.PrivilegeType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.RoleService;
import com.grabbill.core.service.UserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.UserRequest;
import com.grabbill.server.controller.response.ApiMessage;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.SearchResultPayload;
import com.grabbill.server.controller.response.payload.UserPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import com.grabbill.server.service.UserAccountEmailService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    AuditLogService auditLogService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAccountEmailService UserAccountEmailService;

    @Transactional
    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getUsers(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @RequestParam(required = false) String name,
            @PageableDefault(sort = {"name"}, direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<User> page = StringUtils.hasLength(name) ?
                userService.getAllByName(userDetails.getUser(), name, pageable) :
                userService.getAll(userDetails.getUser(), pageable);

        SearchResultPayload<UserPayload> searchResultPayload =
                SearchResultPayload.<UserPayload>builder()
                        .items(page.get().map(UserPayload::from).collect(Collectors.toList()))
                        .totalItems(page.getTotalElements())
                        .totalPages(page.getTotalPages())
                        .build();

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        searchResultPayload
                )
        );
    }

    @Transactional
    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> newUser(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody UserRequest request
    ) {
        Optional<Role> role = roleService.getByName(request.getRole());
        if (role.isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1005,
                    "Role [" + request.getRole() + "] is not found!"
            );
        }
        if (userService.getByEmail(request.getEmail()).isPresent()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1001,
                    "Email already associated with another user account."
            );
        }

        User newInstance = new User();
        request.to(newInstance, role.get());
        String randomPassword = generateRandomPassword();
        newInstance.setPassword(passwordEncoder.encode(randomPassword));
        newInstance.setAccount(userDetails.getUser().getAccount());
        newInstance.setVerified(true);
        newInstance.setActive(true);
        newInstance.setAccountActive(true);
        newInstance.setVerificationCode(RandomStringUtils.randomAlphanumeric(15));

        User savedInstance = userService.save(newInstance);
        UserAccountEmailService.sendNewUserWelcomeEmail(savedInstance, randomPassword);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(newInstance.getId()),
                getDomainType(),
                ActionType.CREATE,
                newInstance.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        UserPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{id}")
    public ResponseEntity<GrabbillApiResponse> updateUser(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody UserRequest request
    ) {
        Role currentUserRole = roleService.getById(userDetails.getUser().getRole().getId()).orElseThrow();
        if (currentUserRole.getPrivileges().stream()
                .map(Privilege::getName)
                .noneMatch(PrivilegeType.MANAGE_USER::equals)) {
            throw new GrabbillServerException(GrabbillServerErrorCode.GRB0017, "User does not have permission for this operation.");
        }

        Optional<Role> roleOptional = roleService.getByName(request.getRole());
        if (roleOptional.isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1005,
                    "Role [" + request.getRole() + "] is not found!"
            );
        }


        Optional<User> userOptional = userService.getByIdAndAccountId(id, userDetails.getUser().getAccount().getId());
        if (userOptional.isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1002,
                    "User [" + id + "] is not found!"
            );
        }

        User currentUser = userOptional.get();

        Optional<User> emailUserOptional = userService.getByEmail(request.getEmail());
        if (emailUserOptional.isPresent() && !emailUserOptional.get().getId().equals(currentUser.getId())) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1001,
                    "Email already associated with another user account."
            );
        }

        request.to(currentUser, roleOptional.get());
        User savedInstance = userService.save(currentUser);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(savedInstance.getId()),
                getDomainType(),
                ActionType.UPDATE,
                savedInstance.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        UserPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{id}/activate")
    public ResponseEntity<GrabbillApiResponse> activateUser(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer id
    ) {
        Optional<User> userOptional = userService.getByIdAndAccountId(id, userDetails.getUser().getAccount().getId());
        if (userOptional.isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1002,
                    "User [" + id + "] is not found!"
            );
        }

        User currentUser = userOptional.get();
        currentUser.setActive(true);
        User savedInstance = userService.save(currentUser);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(savedInstance.getId()),
                getDomainType(),
                ActionType.ACTIVATE,
                savedInstance.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        UserPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{id}/deactivate")
    public ResponseEntity<GrabbillApiResponse> deactivateUser(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer id
    ) {
        Optional<User> userOptional = userService.getByIdAndAccountId(id, userDetails.getUser().getAccount().getId());
        if (userOptional.isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1002,
                    "User [" + id + "] is not found!"
            );
        }

        User currentUser = userOptional.get();
        currentUser.setActive(false);
        User savedInstance = userService.save(currentUser);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(savedInstance.getId()),
                getDomainType(),
                ActionType.DEACTIVATE,
                savedInstance.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        UserPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @PutMapping(value = "/{id}/reset-password")
    public ResponseEntity<GrabbillApiResponse> resetPassword(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer id
    ) {
        Optional<User> userOptional = userService.getByIdAndAccountId(id, userDetails.getUser().getAccount().getId());
        if (userOptional.isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1002,
                    "User [" + id + "] is not found!"
            );
        }

        User currentUser = userOptional.get();
        String randomPassword = generateRandomPassword();
        currentUser.setPassword(passwordEncoder.encode(randomPassword));
        User savedInstance = userService.save(currentUser);
        UserAccountEmailService.sendPasswordResetEmail(savedInstance, randomPassword);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(savedInstance.getId()),
                getDomainType(),
                ActionType.RESET_USER_PASSWORD,
                savedInstance.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        UserPayload.from(savedInstance)
                )
        );
    }

    @Transactional
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<GrabbillApiResponse> deleteUser(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @PathVariable Integer id
    ) {
        Optional<User> userOptional = userService.getByIdAndAccountId(id, userDetails.getUser().getAccount().getId());
        if (userOptional.isEmpty()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB1002,
                    "User [" + id + "] is not found!"
            );
        }

        User user = userOptional.get();

        userService.delete(user);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(user.getId()),
                getDomainType(),
                ActionType.DELETE,
                user.getEmail(),
                userDetails.getUsername()
        );

        return ResponseEntity.ok().body(
                new GrabbillApiResponse(
                        GrabbillServerApiVersion.V1.getVersion(),
                        new ApiMessage("User with id [" + id + "] removed successfully.")
                )
        );
    }

    private String generateRandomPassword() {
        // ASCII range – alphanumeric (0-9, a-z, A-Z)
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";

        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();

        // each iteration of the loop randomly chooses a character from the given
        // ASCII range and appends it to the `StringBuilder` instance

        for (int i = 0; i < 8; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }

        return sb.toString();
    }

    public DomainType getDomainType() {
        return DomainType.USER;
    }
}
