package com.grabbill.server.controller;

import com.grabbill.server.GrabbillServerErrorCode;
import com.grabbill.server.dto.GrabbillAdminUserDetails;
import com.grabbill.server.exception.GrabbillServerException;

/**
 * @author michaellow
 */
public class BaseManagementController {

    protected void checkStatus(final GrabbillAdminUserDetails userDetails) {
        if (userDetails == null) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB0012,
                    "Unauthorized access to admin endpoint!"
            );
        }

        if (!userDetails.getAdminUser().isActive()) {
            throw new GrabbillServerException(
                    GrabbillServerErrorCode.GRB7002,
                    "Admin User [" + userDetails.getUsername() + "] is inactive!"
            );
        }
    }

}
