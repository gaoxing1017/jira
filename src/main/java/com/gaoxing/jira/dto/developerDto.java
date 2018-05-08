package com.gaoxing.jira.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by gaoxing on 2017/7/10.
 *
 */
@Data
public class DeveloperDto {

    /**
     * 姓名
     */
    String name;

    /**
     * 故事点累计
     */
    Double storyPoints;

    /**
     * bug所占故事点累计
     */
    Double bugStoryPoints;

    /**
     * bug总数
     */
    Integer  bugCount;

    /**
     * 重要以上bug数
     */
    Integer  mainBugCount;

    /**
     * 线上bug数
     */
    Integer  onlineBugCount;

    /**
     * 退回次数
     */
    Double  returnTime;

    /**
     * 延期天数
     */
    Double  delayDays;

    /**
     * 延期任务数
     */
    Integer delayTaskCount;

    /**
     * 延期7天以上任务数
     */
    Integer delay7dayCount;

    /**
     * 任务列表
     */
    List<String> issues;

    /**
     * 子任务列表(分配给其余开发,故事点扣除)
     */
    List<String> subTasks;

    /**
     * bug列表
     */
    List<String> bugs;

    /**
     * 重要bug列表
     */
    List<String> mainBugs;

    /**
     * 线上bug列表
     */
    List<String> onlineBugs;

    /**
     * 退回任务列表
     */
    List<String> returnList;

    /**
     * 延期列表
     */
    List<String> delayList;

    /**
     * 延期7天列表
     */
    List<String> delay7days;

}
