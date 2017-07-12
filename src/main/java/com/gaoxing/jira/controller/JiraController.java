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
import java.util.Map;
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

    @Value("${jira.projects}")
    private String projects;

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
            for(String tester : testers.split(",")){
                TesterDto testerDto=new TesterDto();
                testerDto.setName(tester);
                results.add(testerDto);
            }
            //TODO 统计测试人员退回次数
            Map<String,List<String>> returnData=jiraService.getTesterReturnData(startTime,endTime);
            //TODO 统计测试用例
            Map<String,List<String>> testCaseData=jiraService.getTestCaseData(startTime,endTime);
            //TODO 发现bug数
            Map<String,List<String>> bugData=jiraService.getBugData(startTime,endTime);
            //TODO 统计线上bug数
            Map<String,List<String>> onlineBugData=jiraService.getTesterOnlineBugData(startTime,endTime);
            //TODO 统计自动化测试用例
            results.forEach(testerDto -> {
                List<String> returnList= returnData.get(testerDto.getName());
                if(returnList==null) {
                    testerDto.setReturnTime(0);
                    testerDto.setReturnList(new ArrayList<>());
                }else{
                    testerDto.setReturnTime(returnList.size());
                    testerDto.setReturnList(returnList);
                }
                List<String> testCaseList= testCaseData.get(testerDto.getName());
                if(returnList==null) {
                    testerDto.setTestCase(0);
                    testerDto.setTestCaseList(new ArrayList<>());
                }else{
                    testerDto.setTestCase(testCaseList.size());
                    testerDto.setTestCaseList(testCaseList);
                }
                List<String> bugList=bugData.get(testerDto.getName());
                if(bugList==null) {
                    testerDto.setFindBugCount(0);
                    testerDto.setBugList(new ArrayList<>());
                }else{
                    testerDto.setFindBugCount(bugList.size());
                    testerDto.setBugList(bugList);
                }
                List<String> onlineBugList=new ArrayList<>();
                if("zhangling".equals(testerDto.getName())) {
                    onlineBugList = onlineBugData.get("HMS");
                }else if("zhangchen_dev".equals(testerDto.getName())){
                    onlineBugList = onlineBugData.get("RECON");
                }
                if(onlineBugList==null) {
                    testerDto.setOnlineBugCount(0);
                    testerDto.setOnlineBugList(new ArrayList<>());
                }else{
                    testerDto.setOnlineBugCount(onlineBugList.size());
                    testerDto.setOnlineBugList(onlineBugList);
                }
            });
            response.setData(results);
            return response;
        };
    }





}
