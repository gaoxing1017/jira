package com.gaoxing.jira.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaoxing on 2018/5/8.
 */
@Data
public class BugDto {

    /**
     * 姓名
     */
    String name;

    /**
     * feature缺陷列表
     */
    List<String> featureBugList;
    /**
     * dev缺陷列表
     */
    List<String> devBugList;
    /**
     * test缺陷列表
     */
    List<String> testBugList;
    /**
     * 线上bug列表
     */
    List<String> onlineBugList;

    /**
     * 其他bug列表
     */
    List<String> otherBugList;

    /**
     * bug列表
     * @param name
     */
    List<String> bugList;

    public BugDto(String name){
        this.name = name;
        this.bugList = new ArrayList<>();
        this.featureBugList = new ArrayList<>();
        this.devBugList = new ArrayList<>();
        this.testBugList = new ArrayList<>();
        this.onlineBugList = new ArrayList<>();
        this.otherBugList = new ArrayList<>();
    }
}
