/*
 *
 */
package com.cehome.task.domain;

import java.util.Date;

import javax.persistence.Id;
import javax.persistence.Transient;

import jsharp.sql.BaseDO;
import jsharp.sql.anno.Table;

@Table(name = "temp_time_task")
public class TimeTask extends BaseDO {
    @Id
    private long id; // 主键
    private String name; // 时间任务描述
    private String appName; // 时间程序所属应用

    private int cronType;// 时间程序类型 0: inteval 1:cron
    private String cron; // 时间程序周期表达式
    private String targetIp; // 允许运行该程序的服务器IP
    // -- bean config
    private String config;

    // -- custom user interface by taskType
    private int taskType;
    // -- custom config
    private String taskConfig;//
    private String taskResult;
    private int status; // 该时间程序当前状态 1:启动 2:停止
    private String lastTargetIp; // 最后一次运行该程序的服务器IP
    private Date lastStartTime; // 最后一次运行改程序的开始时间
    private Date lastEndTime; // 最后一次运行该程序的结束时间

    private long userId;// 保留
    private Date operTime;

    private String category1; // 类别 组别
    private String category2; // 类别 网站
    private String category3; // 类别 频道
    private String category4; // 类别
    private int priority; // 时间程序所在poolName

    private String operUser;
    private String createUser;

    public TimeTask() {
	super();
	setSqlValue("operTime", "now()");
    }

    public String getCron() {
	return cron;
    }

    public void setCron(final String cron) {
	this.cron = cron;
    }

    public void setId(final long id) {
	this.id = id;
    }

    public long getId() {
	return id;
    }

    public String getTargetIp() {
	return targetIp;
    }

    public void setTargetIp(final String targetIp) {
	this.targetIp = targetIp;
    }

    public int getStatus() {
	return status;
    }

    public void setStatus(final int status) {
	this.status = status;
    }

    public String getLastTargetIp() {
	return lastTargetIp;
    }

    public void setLastTargetIp(final String lastTargetIp) {
	this.lastTargetIp = lastTargetIp;
    }

    public Date getLastStartTime() {
	return lastStartTime;
    }

    public void setLastStartTime(final Date lastStartTime) {
	this.lastStartTime = lastStartTime;
    }

    public Date getLastEndTime() {
	return lastEndTime;
    }

    public void setLastEndTime(final Date lastEndTime) {
	this.lastEndTime = lastEndTime;
    }

    public String getName() {
	return name;
    }

    public void setName(final String name) {
	this.name = name;
    }

    public String getAppName() {
	return appName;
    }

    public void setAppName(final String appName) {
	this.appName = appName;
    }

    public int getCronType() {
	return cronType;
    }

    public void setCronType(final int cronType) {
	this.cronType = cronType;
    }

    @Transient
    public String[] getCategories() {
	return new String[] { category1, category2, category3, category4 };
    }

    public String getCategory1() {
	return category1;
    }

    public void setCategory1(final String category1) {
	this.category1 = category1;
    }

    public String getCategory2() {
	return category2;
    }

    public void setCategory2(final String category2) {
	this.category2 = category2;
    }

    public String getCategory3() {
	return category3;
    }

    public void setCategory3(final String category3) {
	this.category3 = category3;
    }

    public String getCategory4() {
	return category4;
    }

    public void setCategory4(final String category4) {
	this.category4 = category4;
    }

    public long getUserId() {
	return userId;
    }

    public void setUserId(final long userId) {
	this.userId = userId;
    }

    public String getConfig() {
	return config;
    }

    public void setConfig(final String config) {
	this.config = config;
    }

    public int getPriority() {
	return priority;
    }

    public void setPriority(final int priority) {
	this.priority = priority;
    }

    public Date getOperTime() {
	return operTime;
    }

    public void setOperTime(final Date operTime) {
	this.operTime = operTime;
    }

    public int getTaskType() {
	return taskType;
    }

    public void setTaskType(final int taskType) {
	this.taskType = taskType;
    }

    public String getTaskConfig() {
	return taskConfig;
    }

    public void setTaskConfig(final String taskConfig) {
	this.taskConfig = taskConfig;
    }

    public String getOperUser() {
	return operUser;
    }

    public void setOperUser(final String operUser) {
	this.operUser = operUser;
    }

    public String getCreateUser() {
	return createUser;
    }

    public void setCreateUser(final String createUser) {
	this.createUser = createUser;
    }

    public String getTaskResult() {
	return taskResult;
    }

    public void setTaskResult(final String taskResult) {
	this.taskResult = taskResult;
    }
}
