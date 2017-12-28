package com.gaoxing.jira.dto;

import lombok.Data;

/**
 * Created by gaoxing on 2017/9/1.
 */
@Data
public class IssueTimeDto {

    String issueId;

    String name;

    Long startTime;

    Long endTime;

    public Long getExecuteTime(){
        if(startTime>0 && endTime > startTime ){
            return endTime-startTime;
        }
        return 0L;
    }
}
