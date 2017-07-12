package com.gaoxing.jira.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by gaoxing on 2017/7/11.
 *
 */
@Data
public class TesterDto {

    /**
     * 姓名
     */
    String name;

    /**
     * 测试用例执行次数
     */
    Integer testCaseExecuteTime;

    /**
     * 编写测试用例数
     */
    Integer testCase;

    /**
     * 任务打回次数
     */
    Integer returnTime;

    /**
     * 线上bug数
     */
    Integer onlineBugCount;

    /**
     * 发现bug数
     */
    Integer findBugCount;

    /**
     * bug列表
     */
    List<String> bugList;

    /**
     * 线上bug列表
     */
    List<String> onlineBugList;

    /**
     * 打回任务列表
     */
    List<String> returnList;

    /**
     * 编写测试用例列表
     */
    List<String> testCaseList;

    /**
     * 测试用例执行列表
     */
    List<String> testCaseExecuteList;


}
