package com.gaoxing.jira.service.impl;

import com.gaoxing.jira.dto.DeveloperDto;
import com.gaoxing.jira.service.JiraService;
import com.gaoxing.jira.utils.DateUtil;
import com.gaoxing.jira.utils.FieldType;
import com.gaoxing.jira.utils.JiraUtil;
import net.rcarz.jiraclient.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by gaoxing on 2017/7/11.
 * jira serviceImpl
 */
@Service("JiraService")
public class JiraServiceImpl implements JiraService {


    @Value("${jira.projects}")
    private String projects;

    @Value("${jira.testers}")
    private String testers;

    @Value("${jira.testProjects}")
    private String testProjects;

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
                        if(!"null".equals(subTask.getField(FieldType.STORY_POINT).toString())) {
                            storyPoints -= Double.parseDouble(subTask.getField(FieldType.STORY_POINT).toString());
                        }
                    }
                }
            }
            developerDto.setStoryPoints(storyPoints);
        } catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
    }

    public void getBugData(String developer, String startTime, String endTime, DeveloperDto developerDto) {
        String searchSql = "project in (" + projects + ") AND issuetype = Bug" +
                " AND created >=" + startTime +
                " AND created <=" + endTime +
                " AND status != \"reject closed\""+
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
                String storyPoint = bugIssue.getField(FieldType.STORY_POINT).toString();
                if(!"null".equals(storyPoint) && Double.parseDouble(storyPoint)>0){
                    continue;
                }
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
        } catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
    }

    public void getReturnData(String developer, String startTime, String endTime, DeveloperDto developerDto) {
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
        } catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
    }

    public void getDelayData(String developer, String startTime, String endTime, DeveloperDto developerDto) {
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
        } catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
    }

    public Map<String, List<String>> getTesterReturnData(String startTime, String endTime) {
        String searchSql = "project in (" + projects + ")" +
                " AND resolved >=" + startTime +
                " AND resolved <=" + endTime +
                " AND 退回次数 >= 1";
        Map<String, List<String>> results = new HashMap<>();
        Issue.SearchResult returnIssues = new Issue.SearchResult();
        try {
            returnIssues = JiraUtil.getJiraClient().searchIssues(searchSql, FieldType.REPORTER, 1000);
        } catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
        returnIssues.issues.forEach(issue -> {
            try {
                Issue issueDetail = JiraUtil.getJiraClient().getIssue(issue.getKey(), FieldType.REPORTER, "changelog");
                List<ChangeLogEntry> changeLogEntry = issueDetail.getChangeLog().getEntries();
                changeLogEntry.forEach(changeLog -> {
                    changeLog.getItems().forEach(item -> {
                        if ("退回次数".equals(item.getField())) {
                            List<String> returnList = results.get(changeLog.getAuthor().getName());
                            if (returnList == null) {
                                List<String> value = new ArrayList<>();
                                value.add(issueDetail.getKey());
                                results.put(changeLog.getAuthor().getName(), value);
                            } else {
                                returnList.add(issueDetail.getKey());
                            }
                        }
                    });
                });
            } catch (JiraException jiraException) {
                System.console().printf(jiraException.toString());
            }
        });

        return results;
    }

    public Map<String, List<String>> getTestCaseData(String startTime, String endTime) {
        String searchSql = "project in (" + testProjects + ")" +
                " AND issuetype = 测试" +
                " AND created >=" + startTime +
                " AND created <=" + endTime;
        return getIssueListByReporter(executeSearchSql(searchSql,FieldType.REPORTER));
    }

    public Map<String, List<String>> getAutoTestCase(String startTime, String endTime) {
        return new HashMap<>();
    }

    public Map<String, List<String>> getTesterOnlineBugData(String startTime, String endTime) {
        String searchSql = "project in (" + projects + ")" +
                " AND issuetype = Bug" +
                " AND 测试环境 in (STG环境,灰度环境,生产环境,STG)"+
                " AND status !=\"reject closed\""+
                " AND created >=" + startTime +
                " AND created <=" + endTime;
        return getIssueListByProject(executeSearchSql(searchSql,FieldType.PROJECT));
    }

    public Map<String, List<String>> getBugData(String startTime, String endTime) {
        String searchSql = "project in (" + projects + ")" +
                " AND issuetype = Bug" +
                " AND status !=\"reject closed\""+
                " AND created >=" + startTime +
                " AND created <=" + endTime;
        return getIssueListByReporter(executeSearchSql(searchSql,FieldType.REPORTER));
    }

    public Map<String,List<String>> getTestCaseExecuteData(String startTime,String endTime) {
        String searchSql = "project in (" + projects + ") AND status = Closed AND resolution = 完成" +
                " AND resolved >=" + startTime +
                " AND resolved <=" + endTime;
        Issue.SearchResult testCaseExecute = executeSearchSql(searchSql, FieldType.ISSUE_LINKS);

        Map<String,List<String>> issueMap= new HashMap<>();
        for(String tester : testers.split(",")){
            issueMap.put(tester,new ArrayList<>());
        }
        testCaseExecute.issues.forEach(issue -> {
            Issue issueDetail = getIssue(issue.getKey(), FieldType.ISSUE_LINKS);
            if (issueDetail != null) {
                List<ChangeLogEntry> changeLogs = issueDetail.getChangeLog().getEntries();
                changeLogs.forEach(changeLog -> {
                    changeLog.getItems().forEach(item -> {
                        if ("status".equals(item.getField()) && "Testing".equals(item.getFromString())) {
                            issueDetail.getIssueLinks().forEach(issueLink -> {
                                if (issueLink.getOutwardIssue() != null &&
                                    FieldType.TEST_LINK_ISSUE.equals(issueLink.getOutwardIssue().getIssueType().getId())) {
                                    issueMap.get(changeLog.getAuthor().getName()).add(issueLink.getOutwardIssue().getKey());
                                }
                            });
                        }
                    });
                });
            }
        });
        return issueMap;
    }

    /**
     * 执行jira搜索语句
     * @param searchSql
     * @return
     */
    private Issue.SearchResult executeSearchSql(String searchSql,String includedFields){
        Issue.SearchResult searchResult=new Issue.SearchResult();
        try {
            searchResult = JiraUtil.getJiraClient().searchIssues(searchSql,includedFields,1000);
        } catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
        return searchResult;
    }

    /**
     * 获取issue明细
     * @param key
     * @param includedFields
     * @return
     */
    private Issue getIssue(String key,String includedFields){
        Issue issue=null;
        try {
            issue = JiraUtil.getJiraClient().getIssue(key,includedFields,"changelog");
        } catch (JiraException jiraException) {
            System.console().printf(jiraException.toString());
        }
        return issue;
    }

    /**
     * 根据报告人进行任务统计
     * @param searchResult
     * @return
     */
    private Map<String, List<String>> getIssueListByReporter(Issue.SearchResult searchResult){
        Map<String, List<String>> results = new HashMap<>();
        searchResult.issues.forEach(issue -> {
            List<String> valueList = results.get(issue.getReporter().getName());
            if (valueList == null) {
                List<String> value = new ArrayList<>();
                value.add(issue.getKey());
                results.put(issue.getReporter().getName(), value);
            } else {
                valueList.add(issue.getKey());
            }
        });
        return results;
    }

    /**
     * 根据项目进行任务统计
     * @param searchResult
     * @return
     */
    private Map<String, List<String>> getIssueListByProject(Issue.SearchResult searchResult){
        Map<String, List<String>> results = new HashMap<>();
        searchResult.issues.forEach(issue -> {
            List<String> valueList = results.get(issue.getProject().getKey());
            if (valueList == null) {
                List<String> value = new ArrayList<>();
                value.add(issue.getKey());
                results.put(issue.getProject().getKey(), value);
            } else {
                valueList.add(issue.getKey());
            }
        });
        return results;
    }
}
