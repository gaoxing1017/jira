package com.gaoxing.jira.controller;

import com.gaoxing.jira.dto.DeveloperDto;
import com.gaoxing.jira.dto.TesterDto;
import com.gaoxing.jira.service.JiraService;
import com.gaoxing.jira.utils.Response;
import com.gaoxing.jira.utils.ResponseStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by gaoxing on 2017/7/8.
 *
 */
@RestController
public class JiraController {


    @Value("${jira.developers}")
    private String developers;

    @Value("${jira.testers}")
    private String testers;


    @Autowired
    private JiraService jiraService;

    @RequestMapping(value = "/v1/developer", method = RequestMethod.GET)
    public Callable<Response<List<DeveloperDto>> > getStoryPoint(@RequestParam String startTime,
                                                                 @RequestParam String endTime) {
        return () -> {

            List<DeveloperDto> results = new ArrayList<>();
            Response<List<DeveloperDto>> response = new Response<>(ResponseStatus.SUCCESS, "执行成功", null);

            for (String developer : developers.split(",")) {
                DeveloperDto developerDto = new DeveloperDto();
                developerDto.setSubTasks(new ArrayList<>());
                developerDto.setName(developer);
                //故事点统计
                jiraService.getStoryPointData(developer,startTime,endTime,developerDto);
                //bug统计
                jiraService.getBugData(developer,startTime,endTime,developerDto);
                // 计算任务退回次数
                jiraService.getReturnData(developer,startTime,endTime,developerDto);
                //计算任务延期次数及延期天数
                jiraService.getDelayData(developer,startTime,endTime,developerDto);

                results.add(developerDto);
            }
            response.setData(results);
            return response;
        };
    }

    @RequestMapping(value = "/v1/tester", method = RequestMethod.GET)
    public Callable<Response<List<TesterDto>> > getTestPoint(@RequestParam String startTime,
                                                             @RequestParam String endTime) {
        return () -> {

            List<TesterDto> results = new ArrayList<>();
            Response<List<TesterDto>> response = new Response<>(ResponseStatus.SUCCESS, "执行成功", null);

            //TODO 统计测试人员退回次数

            //TODO 统计测试用例

            //TODO 统计自动化测试用例

            response.setData(results);
            return response;
        };
    }





}
