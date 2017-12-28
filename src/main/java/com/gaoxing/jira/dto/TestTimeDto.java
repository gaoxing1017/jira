package com.gaoxing.jira.dto;

import lombok.Data;

import java.util.List;

/**
 * Created by gaoxing on 2017/9/1.
 */
@Data
public class TestTimeDto {
    /**
     * 姓名
     */
    String name;

    /**
     * 测试时间明细
     */
    List<IssueTimeDto> testTimeList;
}
