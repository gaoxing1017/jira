# jira
用于jira任务统计,spring boot项目,集成jira-client.


###开发人员绩效数据统计
- 完成故事点数
- 产生bug数
- 退回次数
- 重要以上bug数
- 任务逾期次数
- 任务逾期天数


#接口访问示例
http://localhost:8989/ms-jira/v1/developer?startTime=2017-06-21&endTime=2017-07-20

### 测试人员绩效数据统计
- 编写测试用例数
- 自动化测试任务数
- 执行测试用例数
- 发现bug数
- 线上bug数
- 任务退回次数

#接口访问示例
http://localhost:8989/ms-jira/v1/tester?startTime=2017-06-21&endTime=2017-07-20