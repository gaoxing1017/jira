package com.gaoxing.jira.service.impl;

import com.gaoxing.jira.dto.DeveloperDto;
import com.gaoxing.jira.service.JiraService;
import com.gaoxing.jira.utils.DateUtil;
import com.gaoxing.jira.utils.FieldType;
import com.gaoxing.jira.utils.JiraUtil;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by gaoxing on 2017/7/11.
 * jira serviceImpl
 */
@Service("JiraService")
public class JiraServiceImpl implements JiraService{


    @Value("${jira.projects}")
    private String projects;

    public void getStoryPointData(String developer, String startTime, String endTime, DeveloperDto developerDto) {
        String searchSql = "project in (" + projects + ") AND status = Closed AND resolution = 完成" +
                " AND resolved >=" + startTime +
                " AND resolved <=" + endTime +
                " AND \"Story Points\" > 0" +
                " AND 开发人员 in (" + developer + ")";
        try {
            Issue.SearchResult storyResult = JiraUtil.getJiraClient().searchIssues(searchSql);
            developerDto.setIssues(storyResult.issues.parallelStream().map(e -> e.getKey()).collect(Collectors.toList()));
            double storyPoints = storyResult.issues.stream()
                    .mapToDouble(e -> Double.parseDouble(e.getField(FieldType.STORY_POINT).toString()))
                    .sum();
            //扣除分给其余人员的子任务故事点
            List<Issue> hasSubTasks = storyResult.issues.parallelStream().filter(e -> !e.getSubtasks().isEmpty()).collect(Collectors.toList());
            for (Issue issue : hasSubTasks) {
                for (Issue subIssue : issue.getSubtasks()) {
                    developerDto.getSubTasks().add(subIssue.getKey());
                    Issue subTask = JiraUtil.getJiraClient().getIssue(subIssue.getKey());
                    if (!developer.equals(subTask.getField(FieldType.DEVELOPER).toString())) {
                        storyPoints -= Double.parseDouble(subTask.getField(FieldType.STORY_POINT).toString());
                    }
                }
            }
            developerDto.setStoryPoints(storyPoints);
        } catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
    }

    public void getBugData(String developer,String startTime,String endTime,DeveloperDto developerDto) {
        String searchSql = "project in (" + projects + ") AND issuetype = Bug" +
                " AND created >=" + startTime +
                " AND created <=" + endTime +
                " AND 开发人员 in (" + developer + ")" +
                " ORDER BY priority DESC";
        try {
            //计算bug数量(普通、重要、线上)
            Issue.SearchResult bugResult = JiraUtil.getJiraClient().searchIssues(searchSql);
            List<String> bugTasks = bugResult.issues.parallelStream().map(e -> e.getKey()).collect(Collectors.toList());
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
        }catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
    }

    public void getReturnData(String developer,String startTime,String endTime,DeveloperDto developerDto) {
        String searchSql = "project in (" + projects + ")" +
                " AND resolved >=" + startTime +
                " AND resolved <=" + endTime +
                " AND 开发人员 in (" + developer + ")" +
                " AND 退回次数 >= 1";
        try {
            Issue.SearchResult returnIssues = JiraUtil.getJiraClient().searchIssues(searchSql);
            List<String> returnList = returnIssues.issues.parallelStream().map(e -> e.getKey()).collect(Collectors.toList());
            Double returnTime = returnIssues.issues.parallelStream().mapToDouble(e -> Double.parseDouble(e.getField(FieldType.RETURN_TIME).toString())).sum();
            developerDto.setReturnList(returnList);
            developerDto.setReturnTime(returnTime);
        }catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
    }

    public void getDelayData(String developer,String startTime,String endTime, DeveloperDto developerDto) {
        String searchSql = "project in (" + projects + ") AND status = Closed AND resolution = 完成" +
                " AND due >=" + startTime +
                " AND due <=" + endTime +
                " AND 开发人员 in (" + developer + ")";
        try {
            Issue.SearchResult delayIssueResult = JiraUtil.getJiraClient().searchIssues(searchSql);
            List<Issue> delayIssues = delayIssueResult.issues.parallelStream()
                    .filter(e -> (e.getResolutionDate().getTime() > (e.getDueDate().getTime())))
                    .collect(Collectors.toList());
            List<String> delayList = delayIssues.parallelStream().map(e -> e.getKey()).collect(Collectors.toList());
            List<String> delay7days = delayIssues.parallelStream()
                    .filter(e -> (e.getResolutionDate().getTime() - (e.getDueDate().getTime()) > 7 * DateUtil.oneDay))
                    .map(e -> e.getKey())
                    .collect(Collectors.toList());
            Long delayDay = delayIssues.parallelStream()
                    .mapToLong(e -> (e.getResolutionDate().getTime() - (e.getDueDate().getTime())))
                    .sum();
            developerDto.setDelayList(delayList);
            developerDto.setDelayTaskCount(delayList.size());
            developerDto.setDelay7days(delay7days);
            developerDto.setDelay7dayCount(delay7days.size());
            developerDto.setDelayDays(delayDay * 1.0d / DateUtil.oneDay);
        }catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
    }
}
