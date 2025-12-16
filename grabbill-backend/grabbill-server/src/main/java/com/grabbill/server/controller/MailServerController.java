package com.grabbill.server.controller;

import com.grabbill.core.entity.MailServer;
import com.grabbill.core.entity.User;
import com.grabbill.core.model.ActionType;
import com.grabbill.core.model.DomainType;
import com.grabbill.core.service.AuditLogService;
import com.grabbill.core.service.MailServerService;
import com.grabbill.core.service.UserService;
import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.controller.request.MailServerUpdateRequest;
import com.grabbill.server.controller.response.GrabbillApiResponse;
import com.grabbill.server.controller.response.GrabbillServerApiVersion;
import com.grabbill.server.controller.response.payload.MailServerPayload;
import com.grabbill.server.dto.GrabbillUserDetails;
import com.grabbill.server.exception.GrabbillServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/mail-server")
public class MailServerController {
    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailServerService mailServerService;


    @GetMapping()
    public ResponseEntity<GrabbillApiResponse> getMailServerSettings(
            @AuthenticationPrincipal GrabbillUserDetails userDetails
    ) {
        User user = getUser(userDetails);
        Optional<MailServer> mailServerOptional = mailServerService.getByAccount(user.getAccount());

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                mailServerOptional.map(MailServerPayload::from).orElseGet(() -> MailServerPayload.builder().customServer(false).build())
        );

        return ResponseEntity.ok().body(response);
    }

    @PostMapping()
    public ResponseEntity<GrabbillApiResponse> updateMailServerSettings(
            @AuthenticationPrincipal GrabbillUserDetails userDetails,
            @Valid @RequestBody MailServerUpdateRequest request
    ) {
        User user = getUser(userDetails);
        MailServer mailServer = mailServerService.getByAccount(user.getAccount()).orElse(new MailServer());
        mailServer.setAccount(user.getAccount());

        mailServer.setCustomServer(request.getCustomServer());
        mailServer.setSmtpHost(request.getSmtpHost());
        mailServer.setSmtpPort(request.getSmtpPort());
        mailServer.setSmtpEncryption(request.getSmtpEncryption());
        mailServer.setSmtpUsername(request.getSmtpUsername());
        mailServer.setSmtpPassword(request.getSmtpPassword());
        mailServer.setSmtpFrom(request.getSmtpFrom());
        mailServer.setSmtpFromName(request.getSmtpFromName());
        mailServer.setImapHost(request.getImapHost());
        mailServer.setImapPort(request.getImapPort());
        mailServer.setImapEncryption(request.getImapEncryption());
        mailServer.setImapUsername(request.getImapUsername());
        mailServer.setImapPassword(request.getImapPassword());

        MailServer updatedMailServer = mailServerService.save(mailServer);

        auditLogService.log(
                userDetails.getUser().getAccount().getId(),
                Optional.empty(),
                Long.valueOf(updatedMailServer.getId()),
                getDomainType(),
                ActionType.UPDATE,
                user.getAccount().getCompanyName(),
                userDetails.getUsername()
        );

        GrabbillApiResponse response = new GrabbillApiResponse(
                GrabbillServerApiVersion.V1.getVersion(),
                MailServerPayload.from(updatedMailServer)
        );

        return ResponseEntity.ok().body(response);
    }

    private User getUser(final GrabbillUserDetails userDetails) {
        return userService.getByEmail(userDetails.getUsername()).orElseThrow(
                () -> new GrabbillServerException(
                        GrabbillServerErrorCode.GRB1002,
                        "User [" + userDetails.getUsername() + "] is not found!"
                )
        );
    }

    public DomainType getDomainType() {
        return DomainType.MAIL_SERVER;
    }

}
