package com.gaoxing.jira.service;

import com.gaoxing.jira.dto.DeveloperDto;

/**
 * Created by gaoxing on 2017/7/11.
 *
 */
public interface JiraService {

    void getStoryPointData(String developer, String startTime, String endTime, DeveloperDto developerDto);

    void getBugData(String developer, String startTime, String endTime, DeveloperDto developerDto);

    void getReturnData(String developer, String startTime, String endTime, DeveloperDto developerDto);

    void getDelayData(String developer, String startTime, String endTime, DeveloperDto developerDto);

}
