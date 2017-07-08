package com.gaoxing.jira.controller;

import com.gaoxing.jira.utils.FieldType;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    @RequestMapping(value = "/v1/jira", method = RequestMethod.GET)
    public Callable<String> getStoryPoint(@RequestParam String startTime,
                                          @RequestParam String endTime) {
        return () -> {
            BasicCredentials creds = new BasicCredentials("gaoxing", "happy891017");
            JiraClient jira = new JiraClient("http://j.quyiyuan.com/", creds);

            for(String developer : developers.split(",")){
                String searchSql= "project in ("+projects+") AND status = Closed AND resolution = 完成"+
                        " AND resolved >="+startTime+
                        " AND resolved <="+endTime+
                        " AND \"Story Points\" > 0"+
                        " AND 开发人员 in ("+developer+")";
                Issue.SearchResult searchResult = jira.searchIssues(searchSql);

                double sum =searchResult.issues.stream()
                                   .mapToDouble(e->Double.parseDouble(e.getField(FieldType.STORY_POINT).toString()))
                                   .sum();
                List<Issue> hasSubTasks = searchResult.issues.stream().filter(e-> !e.getSubtasks().isEmpty()).collect(Collectors.toList());
                for(Issue issue : hasSubTasks){
                    Issue subTask =jira.getIssue(issue.getKey());
                    if(!developer.equals(subTask.getField(FieldType.DEVELOPER).toString())){
                        sum-=Double.parseDouble(subTask.getField(FieldType.STORY_POINT).toString());
                    }
                }
            }


            return "test";
        };
    }

}
