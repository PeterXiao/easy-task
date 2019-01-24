/*
 *
 */
package com.cehome.task.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.cehome.task.TimeTaskFactory;
import com.cehome.task.dao.TimeTaskDao;
import com.cehome.task.domain.TimeTask;
import com.cehome.task.service.ConfigService;
import com.cehome.task.service.MachineBaseService;

import jsharp.util.Convert;

/**
 * 机器容灾切换服务
 * 
 * @author ruixiang.mrx -- （频率2秒）更新 服务器机器列表。 放入tair的hashmap（ip,时间）
 */
public class MachineSwitchService extends MachineBaseService implements InitializingBean {

    protected static final Logger logger = LoggerFactory.getLogger(MachineSwitchService.class);
    @Autowired
    TimeTaskDao timeTaskDao;
    @Autowired
    protected ConfigService configService;
    @Autowired
    private MachineListService machineListService;
    @Autowired
    private TimeTaskConsole timeTaskConsole;
    // protected long SWITCH_TIME_SPAN = Constants.CLIENT_HEART_BEAT_INTERVAL+ 10;

    @Override
    public void afterPropertiesSet() throws Exception {
	TimeTaskFactory.scheduleWithFixedDelay(new Runnable() {
	    @Override
	    public void run() {
		try {
		    schedule();
		} catch (final Exception e) {
		    logger.error("switch run error", e);
		}
	    }
	}, timeTaskConsole.getHeartBeatCheckInterval(), timeTaskConsole.getHeartBeatCheckInterval());
    }

    /*
     * private Set<String> getAllMachines(){ List<TimeTask> list=
     * timeTaskDao.listValid(); Set<String> set=new HashSet<>(list.size());
     * for(TimeTask timeTask:list){
     * set.add(timeTask.getAppName()+","+timeTask.getTargetIp()); } return set; }
     */

    // @Scheduled(fixedDelay = Constants.MONITOR_CHECK_INTERVAL)
    public void schedule() {
	logger.info("\r\n\r\n");
	// 注：监控处理：本部分逻辑其实最好让另一个应用（两台机器就行）执行比较稳妥;同一个应用可能会在处理过程中机器发布重启。
	logger.info("try to lock before get machine list");
	if (configService.simpleLock(getClusterName() + KEY_LOCK, 60)) {
	    try {
		logger.info("lock successfully , try to get machine list");

		final Set<String> appNames = configService.smembers(getClusterName() + KEY_APPS);
		for (final String appName : appNames) {
		    logger.info("do with app " + appName);
		    doWithApp(appName);
		}

	    } finally {
		configService.simpleUnlock(getClusterName() + KEY_LOCK);
	    }

	} else {
	    logger.info("lock fail");
	}
    }

    private void doWithApp(final String appName) {
	final long now = configService.getTime();// System.currentTimeMillis();
	final Map<String, String> map = configService.hgetAll(getClusterName() + KEY_MACHINES + appName);
	if (map != null) {
	    // Set<String> allMachines=getAllMachines();
	    final List<String> connecteds = new ArrayList<>();
	    final List<String> onlines = new ArrayList<>();
	    final List<String> offlines = new ArrayList<>();

	    // 刚上线 在线 离线
	    for (final Map.Entry<String, String> e : map.entrySet()) {
		final String hostInfo = e.getKey();
		final long time = Convert.toLong(e.getValue(), 0);
		if (now - time <= timeTaskConsole.getHeartBeatFailSwitchTime()) {
		    onlines.add(hostInfo);

		    if (configService.hexists(getClusterName() + KEY_MACHINES_START + appName, hostInfo)) {
			connecteds.add(hostInfo);
		    }

		} else {
		    offlines.add(hostInfo);
		}

	    }
	    logger.info("online machine count=" + onlines.size() + ","
		    + StringUtils.substring(JSON.toJSONString(onlines), 0, 100) + "...");
	    logger.info("offline machine , need to remove. moveip size=" + offlines.size() + ","
		    + JSON.toJSONString(offlines), 0, 100);
	    if (onlines.size() == 0) {
		logger.error("no online machines !!!");
	    } else {
		for (final String ip : offlines) {
		    if (switchIP(appName, ip, onlines)) {
			configService.hdel(getClusterName() + KEY_MACHINES + appName, ip);
		    }

		}
	    }

	    logger.info(connecteds.size() == 0 ? "No machines need to recover"
		    : "just online and need to recover count=" + connecteds.size());
	    for (final String machine : connecteds) {
		recoverIP(appName, machine);
		configService.hdel(getClusterName() + KEY_MACHINES_START + appName, machine);

	    }

	}
    }

    private boolean switchIP(final String appName, final String ipInfo, final List<String> onlines) {
	// 日常关闭此功能
	// if(!DefaultConfigClient.isPro()) return true;
	// 加入一个心跳判断？ http://100.69.166.160:7001/checkpreload.htm

	// String port = DefaultConfigClient.isDev() && getLocalMachine().equals(ipInfo)
	// ? "8080" : "7001";
	// String url = "http://" + ipInfo + ":" + port + "/checkpreload.htm";

	logger.info("begin to switch machine " + ipInfo);
	final String[] info = extractMachine(ipInfo);
	final List<String> machines = filterMachines(info[0], onlines);
	if (machines.size() == 0) {
	    logger.warn("no machine need to switch");
	    return false;
	}

	try {
	    final ClientServiceProxy clientServiceProxy = new ClientServiceProxy();
	    final String url = machineListService.getServiceUrl(appName, ipInfo);
	    clientServiceProxy.setBaseUrl(url);
	    final String response = clientServiceProxy.status(null);
	    if ("0".equals(response)) {
		logger.info("maichine url " + url + "... check ok，then ignore switch");
		return false;
	    } else {
		logger.info("Connect to " + url + "... response fail :" + response);
	    }
	} catch (final Exception e) {
	    logger.info("Not Connect", e);
	}

	final List<TimeTask> list = timeTaskDao.listByIp(appName, ipInfo);
	logger.info("task count need to switch: " + list.size());
	int i = 0;
	for (final TimeTask timeTask : list) {
	    timeTask.setTargetIp(machines.get(i++));
	    if (i >= machines.size()) {
		i = 0;
	    }
	    timeTask.setLastTargetIp(ipInfo);
	    if (timeTask.sqlValueMap() != null) {
		timeTask.sqlValueMap().remove("operTime");
	    }
	    timeTaskDao.save(timeTask);
	}
	return true;

    }

    private boolean recoverIP(final String appName, final String machine) {
	// 日常关闭此功能
	// if(!DefaultConfigClient.isPro()) return true;
	logger.info("begin to recover machine " + machine);
	// String[] info=ipInfo.split(",");
	final List<TimeTask> list = timeTaskDao.listByLastIp(appName, machine);
	logger.info("task count need to switch" + list.size());
	for (final TimeTask timeTask : list) {
	    timeTask.setTargetIp(machine);
	    timeTask.setLastTargetIp("");
	    if (timeTask.sqlValueMap() != null) {
		timeTask.sqlValueMap().remove("operTime");
	    }
	    timeTaskDao.save(timeTask);
	}
	return true;

    }

}