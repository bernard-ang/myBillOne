package com.grabbill.server.service;

import com.grabbill.core.entity.BaseActivity;
import com.grabbill.core.entity.Job;
import com.grabbill.server.controller.response.payload.JobPayload;

/**
 * @author michaellow
 */
public interface JobControllerService {

    BaseActivity getActivity(Job targetJob);

    void copyToPayload(JobPayload jobPayload, BaseActivity activity);

}
