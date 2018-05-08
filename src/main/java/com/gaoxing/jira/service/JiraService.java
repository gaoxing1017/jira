package com.gaoxing.jira.service;

import com.gaoxing.jira.dto.BugDto;
import com.gaoxing.jira.dto.DeveloperDto;
import com.gaoxing.jira.dto.TestTimeDto;

import java.util.List;
import java.util.Map;

/**
 * Created by gaoxing on 2017/7/11.
 *
 */
public interface JiraService {

    /**
     * 获取某个开发人员故事点数据
     * @param developer
     * @param startTime
     * @param endTime
     * @param developerDto
     */
    void getStoryPointData(String developer, String startTime, String endTime, DeveloperDto developerDto);

    /**
     * 获取某个开发人员bug数据
     * @param developer
     * @param startTime
     * @param endTime
     * @param developerDto
     */
    void getBugData(String developer, String startTime, String endTime, DeveloperDto developerDto);

    /**
     * 获取某个开发人员被退回数据
     * @param developer
     * @param startTime
     * @param endTime
     * @param developerDto
     */
    void getReturnData(String developer, String startTime, String endTime, DeveloperDto developerDto);

    /**
     * 获取某个开发人员任务延期数据
     * @param developer
     * @param startTime
     * @param endTime
     * @param developerDto
     */
    void getDelayData(String developer, String startTime, String endTime, DeveloperDto developerDto);

    /**
     * 获取测试人员退回数据
     */
    Map<String,List<String>> getTesterReturnData(String startTime, String endTime);

    /**
     * 获取测试用例数据
     * @param startTime
     * @param endTime
     * @return
     */
    Map<String,List<String>> getTestCaseData(String startTime, String endTime);

    /**
     * 获取自动化用例数据
     * @param startTime
     * @param endTime
     * @return
     */
    Map<String,List<String>> getAutoTestCase(String startTime, String endTime);

    /**
     * 获取测试人员发现bug数
     * @param startTime
     * @param endTime
     * @return
     */
//    Map<String, List<String>> getBugData(String startTime, String endTime);


    Map<String, BugDto> getBugData(String startTime, String endTime);
    /**
     * 获取测试人员线上bug数
     * @param startTime
     * @param endTime
     * @return
     */
    Map<String,List<String>> getTesterOnlineBugData(String startTime, String endTime);

    /**
     * 获取测试用例执行数据
     * @param startTime
     * @param endTime
     * @return
     */
    Map<String,List<String>> getTestCaseExecuteData(String startTime,String endTime);

    /**
     * 获取测试时间数据
     * @param startTime
     * @param endTime
     * @return
     */
    List<TestTimeDto> getTestTimeData(String startTime, String endTime);

}
