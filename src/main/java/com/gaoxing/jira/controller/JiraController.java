package com.gaoxing.jira.controller;

import com.gaoxing.jira.dto.DeveloperDto;
import com.gaoxing.jira.utils.DateUtil;
import com.gaoxing.jira.utils.FieldType;
import com.gaoxing.jira.utils.Response;
import com.gaoxing.jira.utils.ResponseStatus;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Created by gaoxing on 2017/7/8.
 */
@RestController
public class JiraController {

    @Value("${jira.projects}")
    private String projects;

    @Value("${jira.developers}")
    private String developers;

    @Value("${jira.testers}")
    private String testers;


//    private DeveloperDto getDeveloperData(String developer,String startTime,String endTime, List<DeveloperDto> results){
//        return
//    }

    @RequestMapping(value = "/v1/developer", method = RequestMethod.GET)
    public Callable<Response<List<DeveloperDto>> > getStoryPoint(@RequestParam String startTime,
                                                   @RequestParam String endTime) {
        return () -> {
            //
            List<DeveloperDto> results = new ArrayList<>();
            Response<List<DeveloperDto>> response = new Response<>(ResponseStatus.SUCCESS, "执行成功", null);
            BasicCredentials creds = new BasicCredentials("gaoxing", "happy891017");
            JiraClient jira = new JiraClient("http://j.quyiyuan.com/", creds);

//            Stream.of(developers.split(",")).forEach(e-> getDeveloperData(e,startTime,endTime,results));
            for (String developer : developers.split(",")) {
                DeveloperDto developerDto = new DeveloperDto();
                developerDto.setSubTasks(new ArrayList<>());
                developerDto.setName(developer);
                String searchSql = "project in (" + projects + ") AND status = Closed AND resolution = 完成" +
                        " AND resolved >=" + startTime +
                        " AND resolved <=" + endTime +
                        " AND \"Story Points\" > 0" +
                        " AND 开发人员 in (" + developer + ")";
                Issue.SearchResult storyResult = jira.searchIssues(searchSql);
                developerDto.setIssues(storyResult.issues.stream().map(e -> e.getKey()).collect(Collectors.toList()));
                double storyPoints = storyResult.issues.stream()
                        .mapToDouble(e -> Double.parseDouble(e.getField(FieldType.STORY_POINT).toString()))
                        .sum();
                //扣除分给其余人员的子任务故事点
                List<Issue> hasSubTasks = storyResult.issues.stream().filter(e -> !e.getSubtasks().isEmpty()).collect(Collectors.toList());
                for (Issue issue : hasSubTasks) {
                    for (Issue subIssue : issue.getSubtasks()) {
                        developerDto.getSubTasks().add(subIssue.getKey());
                        Issue subTask = jira.getIssue(subIssue.getKey());
                        if (!developer.equals(subTask.getField(FieldType.DEVELOPER).toString())) {
                            storyPoints -= Double.parseDouble(subTask.getField(FieldType.STORY_POINT).toString());
                        }
                    }
                }
                developerDto.setStoryPoints(storyPoints);
                searchSql = "project in (" + projects + ") AND issuetype = Bug" +
                        " AND created >=" + startTime +
                        " AND created <=" + endTime +
                        " AND 开发人员 in (" + developer + ")" +
                        " ORDER BY priority DESC";

                //计算bug数量(普通、重要、线上)
                Issue.SearchResult bugResult = jira.searchIssues(searchSql);
                List<String> bugTasks = bugResult.issues.stream().map(e -> e.getKey()).collect(Collectors.toList());
                developerDto.setBugs(bugTasks);
                developerDto.setBugCount(bugTasks.size());
                List<String> mainBugs = new ArrayList<>();
                List<String> onlineBugs = new ArrayList<>();
                for (Issue bugIssue : bugResult.issues) {
                    //优先级由紧急->微小: 1->5
                    if (3 >= Integer.parseInt(bugIssue.getPriority().getId())) {
                        mainBugs.add(bugIssue.getKey());
                    }
                    if ("线上".equals(((Map) bugIssue.getField(FieldType.IS_ONLINE_BUG)).get("value").toString())) {
                        onlineBugs.add(bugIssue.getKey());
                    }
                }
                developerDto.setMainBugs(mainBugs);
                developerDto.setMainBugCount(mainBugs.size());
                developerDto.setOnlineBugs(onlineBugs);
                developerDto.setOnlineBugCount(onlineBugs.size());
                // 计算任务退回次数
                searchSql = "project in (" + projects + ")" +
                        " AND resolved >=" + startTime +
                        " AND resolved <=" + endTime +
                        " AND 开发人员 in (" + developer + ")" +
                        " AND 退回次数 >= 1";
                Issue.SearchResult returnIssues = jira.searchIssues(searchSql);
                List<String> returnList = returnIssues.issues.stream().map(e -> e.getKey()).collect(Collectors.toList());
                Double returnTime = returnIssues.issues.stream().mapToDouble(e -> Double.parseDouble(e.getField(FieldType.RETURN_TIME).toString())).sum();
                developerDto.setReturnList(returnList);
                developerDto.setReturnTime(returnTime);
                //计算任务延期次数及延期天数
                searchSql = "project in (" + projects + ") AND status = Closed AND resolution = 完成" +
                        " AND due >=" + startTime +
                        " AND due <=" + endTime +
                        " AND 开发人员 in (" + developer + ")";
                Issue.SearchResult delayIssueResult = jira.searchIssues(searchSql);
                List<Issue> delayIssues = delayIssueResult.issues.stream()
                                                          .filter(e -> (e.getResolutionDate().getTime() > (e.getDueDate().getTime())))
                                                          .collect(Collectors.toList());
                List<String> delayList = delayIssues.stream().map(e -> e.getKey()).collect(Collectors.toList());
                List<String> delay7days = delayIssues.stream()
                        .filter(e -> (e.getResolutionDate().getTime() - (e.getDueDate().getTime()) > 7 * DateUtil.oneDay))
                        .map(e -> e.getKey())
                        .collect(Collectors.toList());
                Long delayDay = delayIssues.stream()
                        .mapToLong(e -> (e.getResolutionDate().getTime() - (e.getDueDate().getTime())))
                        .sum();
                developerDto.setDelayList(delayList);
                developerDto.setDelayTaskCount(delayList.size());
                developerDto.setDelay7days(delay7days);
                developerDto.setDelay7dayCount(delay7days.size());
                developerDto.setDelayDays(delayDay * 1.0d / DateUtil.oneDay);
                results.add(developerDto);
            }
            response.setData(results);
            return response;
        };
    }
}
