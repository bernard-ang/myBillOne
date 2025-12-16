package com.grabbill.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author michaellow
 */
@RestController
@RequestMapping("/build-info")
public class BuildInfoController {

    private static final String SERVER_NAME = "grabbill-server";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${git.build.time}")
    private String buildDateTime;

    @Value("${git.commit.id.abbrev}")
    private String gitCommitIdAbbrev;


    @GetMapping
    public String getBuildInfo() {
        LocalDate localDate = LocalDate.parse(buildDateTime.substring(0, 10), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return SERVER_NAME + ":" + DATE_TIME_FORMATTER.format(localDate) + ":" + gitCommitIdAbbrev;
    }

}
