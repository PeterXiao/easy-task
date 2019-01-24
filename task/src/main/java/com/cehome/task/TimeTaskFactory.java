/*
 *
 */
package com.cehome.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.cehome.task.dao.TimeTaskCacheDao;
import com.cehome.task.dao.TimeTaskDao;
import com.cehome.task.domain.TimeTask;
import com.cehome.task.domain.TimeTaskCache;
import com.cehome.task.service.ConfigService;
import com.cehome.task.service.DatabaseConfigService;
import com.cehome.task.service.RedisConfigService;
import com.cehome.task.util.IpAddressUtil;
import com.cehome.task.util.TimeTaskUtil;

import jsharp.sql.ObjectSessionFactory;
import jsharp.sql.SessionFactory;
import jsharp.support.BeanAnn;

public class TimeTaskFactory implements ApplicationContextAware, InitializingBean, BeanPostProcessor {
    protected static final Logger logger = LoggerFactory.getLogger(TimeTaskFactory.class);
    // 唯一标识、对应表名
    @Value("${task.factory.name}")
    private String name;
    // -- 应用
    @Value("${task.factory.appName}")
    private String appName;
    // -- 自动切换时用到
    @Value("${task.factory.appEnv:}")
    private String appEnv;

    @Value("${task.factory.redis.host:}")
    private String redisHost;
    @Value("${task.factory.redis.port:6379}")
    private int redisPort;

    @Value(Constants.CONFIG_DRIVER)
    private String driverClassName;

    // private String rmiPort="1189";
    private DataSource dataSource;
    // private String tableName="time_task";

    // -- 单机；集群
    private boolean clusterMode = true;

    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
    protected String localIP;
    protected String localHostName;
    ConfigService configService;
    ApplicationContext applicationContext;
    private SessionFactory sessionFactory;

    public ConfigService getConfigService() {
	return configService;
    }

    public SessionFactory getSessionFactory() {
	return sessionFactory;
    }

    protected SessionFactory createSessionFactory() {
	final SessionFactory sessionFactory = new ObjectSessionFactory();
	sessionFactory.setDataSource(dataSource);
	return sessionFactory;
    }

    protected TimeTaskDao createTimeTaskDao() {
	final BeanAnn beanAnn = BeanAnn.getBeanAnn(TimeTask.class);
	beanAnn.setTable(getName());
	final TimeTaskDao timeTaskDao = new TimeTaskDao();
	timeTaskDao.setTableName(getName());
	timeTaskDao.setSessionFactory(getSessionFactory());
	return timeTaskDao;
    }

    protected TimeTaskCacheDao createTimeTaskCacheDao() {
	final BeanAnn beanAnn = BeanAnn.getBeanAnn(TimeTaskCache.class);
	final String tableName = getName() + "_cache";
	beanAnn.setTable(tableName);
	final TimeTaskCacheDao timeTaskCacheDao = new TimeTaskCacheDao();
	timeTaskCacheDao.setTableName(tableName);
	timeTaskCacheDao.setSessionFactory(getSessionFactory());
	return timeTaskCacheDao;
    }

    protected ConfigService createRedisService() {

	ConfigService configService = null;
	if (StringUtils.isNotBlank(redisHost)) {
	    configService = new RedisConfigService(redisHost, redisPort);
	} else {
	    configService = new DatabaseConfigService(driverClassName.indexOf("com.mysql.jdbc.Driver") >= 0);
	}

	return configService;
    }

    // public abstract void createBeans(ApplicationContext applicationContext)
    // throws BeansException ;

    @Override
    public void afterPropertiesSet() throws Exception {
	if (getName() == null) {
	    throw new RuntimeException("factory name can not be null");
	}

	final DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext
		.getAutowireCapableBeanFactory();

	sessionFactory = createSessionFactory();
	factory.registerSingleton("timeTaskDao", createTimeTaskDao());

	if (isClusterMode()) {
	    logger.info("time task init with cluster mode");
	    if (StringUtils.isBlank(redisHost)) {
		factory.registerSingleton("timeTaskCacheDao", createTimeTaskCacheDao());
	    }
	    configService = createRedisService();
	    TimeTaskUtil.registerBean(factory, "configService", configService);
	} else {
	    logger.info("time task init with standalone mode");
	}

	localIP = IpAddressUtil.getLocalHostAddress();
	localHostName = IpAddressUtil.getLocalHostName();
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
	this.applicationContext = applicationContext;

	/*
	 * String env =getAppEnv(); if (StringUtils.isNotBlank(env)) { env += SPLIT;
	 * }else { env=""; } if (isUseHostName()) { localMachine =env+ localHostName; }
	 * else { localMachine =env+ localIP; }
	 */

    }

    public String getName() {
	return name;
    }

    public void setName(final String name) {
	this.name = name;
    }

    public boolean isClusterMode() {
	return clusterMode;
    }

    public void setClusterMode(final boolean clusterMode) {
	this.clusterMode = clusterMode;
    }

    public String getRedisHost() {
	return redisHost;
    }

    public void setRedisHost(final String redisHost) {
	this.redisHost = redisHost;
    }

    public int getRedisPort() {
	return redisPort;
    }

    public void setRedisPort(final int redisPort) {
	this.redisPort = redisPort;
    }

    public String getAppEnv() {
	return appEnv;
    }

    public void setAppEnv(final String appEnv) {
	this.appEnv = appEnv;
    }

    public DataSource getDataSource() {
	return dataSource;
    }

    public void setDataSource(final DataSource dataSource) {
	this.dataSource = dataSource;
    }

    public String getAppName() {
	return appName;
    }

    public void setAppName(final String appName) {
	this.appName = appName;
    }
    /*
     * public String getTableName() { return tableName; }
     *
     * public void setTableName(String tableName) { this.tableName = tableName; }
     */

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
	return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
	return bean;
    }

    public static void scheduleWithFixedDelay(final Runnable runnable, final long first, final long interval) {
	scheduledExecutorService.scheduleWithFixedDelay(runnable, first, interval, TimeUnit.MILLISECONDS);
    }

    public String getLocalIP() {
	return localIP;
    }

    public String getLocalHostName() {
	return localHostName;
    }

}
