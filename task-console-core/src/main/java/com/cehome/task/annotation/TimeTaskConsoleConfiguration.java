/*
 *
 */
package com.cehome.task.annotation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cehome.task.console.TimeTaskConsole;

/**
 * Created by coolma import org.springframework.beans.factory.annotation.Value;
 */
@Configuration
public class TimeTaskConsoleConfiguration {

    @Value("${task.heartBeatFailSwitchTime:60000}")
    private long heartBeatFailSwitchTime;
    @Value("${task.heartBeatCheckInterval:30000}")
    private long heartBeatCheckInterval;

    // -- 容灾切换
    @Value("${task.heartBeatSwitchEnable:true}")
    private final boolean switchEnable = true;

    @Bean
    public TimeTaskConsole createConsoleTimeTask() {
	final TimeTaskConsole consoleTimeTask = new TimeTaskConsole();
	consoleTimeTask.setHeartBeatCheckInterval(heartBeatCheckInterval);
	consoleTimeTask.setHeartBeatFailSwitchTime(heartBeatFailSwitchTime);
	consoleTimeTask.setSwitchEnable(switchEnable);
	return consoleTimeTask;
    }

}
