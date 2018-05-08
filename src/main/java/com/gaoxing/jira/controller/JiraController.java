package com.gaoxing.jira.controller;

import com.gaoxing.jira.dto.BugDto;
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

    @Value("${jira.developersAndroid}")
    private String androidDevelopers;

    @Value("${jira.developersBackend}")
    private String backendDevelopers;

    @Value("${jira.testers}")
    private String testers;

    @Value("${jira.projects}")
    private String projects;

    @Autowired
    private JiraService jiraService;

    @RequestMapping(value = "/v1/developer", method = RequestMethod.GET)
    public Callable<Response<List<DeveloperDto>> > getStoryPoint(@RequestParam String startTime,
                                                                 @RequestParam String endTime,
                                                                 @RequestParam(required = false) Integer type) {
        return () -> {

            List<DeveloperDto> results = new ArrayList<>();
            Response<List<DeveloperDto>> response = new Response<>(ResponseStatus.SUCCESS, "执行成功", null);

            String members = developers;
            switch (type){
                case 1:  //android端
                    members = androidDevelopers;
                    break;
                case 2:  //服务端
                    members = backendDevelopers;
                    break;
                case 0:
                    //fall though
                default: //徐汇
                    break;
            }
            for (String developer : members.split(",")) {
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
            //TODO 统计测试时间数据
//            List<TestTimeDto> testTimeData =jiraService.getTestTimeData(startTime,endTime);
            //统计测试人员退回次数
//            Map<String,List<String>> returnData=jiraService.getTesterReturnData(startTime,endTime);
            //统计测试用例
//            Map<String,List<String>> testCaseData=jiraService.getTestCaseData(startTime,endTime);
            //发现bug数
            Map<String,BugDto> bugData=jiraService.getBugData(startTime,endTime);
            //统计线上bug数
//            Map<String,List<String>> onlineBugData=jiraService.getTesterOnlineBugData(startTime,endTime);
            //统计关联测试用例数据
//            Map<String,List<String>> executeData=jiraService.getTestCaseExecuteData(startTime,endTime);


            //TODO 统计自动化测试用例

            //填充统计数据
            results.forEach(testerDto -> {

                BugDto bugDto = bugData.get(testerDto.getName());
                if (bugDto != null) {
                    testerDto.setFindBugCount(bugDto.getBugList().size());
                    testerDto.setBugList(bugDto.getBugList());

                    testerDto.setFeatureBugCount(bugDto.getFeatureBugList().size());
                    testerDto.setFeatureBugList(bugDto.getFeatureBugList());

                    testerDto.setDevBugCount(bugDto.getDevBugList().size());
                    testerDto.setDevBugList(bugDto.getDevBugList());

                    testerDto.setTestBugCount(bugDto.getTestBugList().size());
                    testerDto.setTestBugList(bugDto.getTestBugList());

                    testerDto.setOnlineBugCount(bugDto.getOnlineBugList().size());
                    testerDto.setOnlineBugList(bugDto.getOnlineBugList());

                    testerDto.setOtherBugCount(bugDto.getOtherBugList().size());
                    testerDto.setOtherBugList(bugDto.getOtherBugList());
                }
            });
            response.setData(results);
            return response;
        };
    }





}
