/*
 *
 */
package com.cehome.task.annotation;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cehome.task.Constants;
import com.cehome.task.TimeTaskFactory;

/**
 * Created by coolma
 *
 */
@Configuration
public class TimeTaskFactoryConfiguration {

    @Value(Constants.CONFIG_DRIVER)
    private String driverClassName;

    // @Value("${task.factory.createTable:false}")
    // private boolean createTable;

    /*
     * @Value("${task.datasource.url}") private String url;
     * 
     * @Value("${task.datasource.username}") private String username;
     * 
     * @Value("${task.datasource.password}") private String password;
     */

    @Bean
    @ConfigurationProperties(prefix = "task.datasource")
    protected PoolProperties createPoolProperties() {
	final PoolProperties poolProperties = new PoolProperties();
	poolProperties.setDriverClassName(driverClassName);
	poolProperties.setValidationQuery("select 1");
	poolProperties.setTestWhileIdle(true);
	poolProperties.setTimeBetweenEvictionRunsMillis(30 * 1000);
	return poolProperties;
    }

    // @Bean(name="taskDatasource")
    // @ConfigurationProperties(prefix = "task.datasource")
    protected DataSource createDataSource() {
	final DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource(createPoolProperties());
	/*
	 * dataSource.setDriverClassName(driverClassName); dataSource.setUrl(url);
	 * dataSource.setUsername(username); dataSource.setPassword(password);
	 * dataSource.setValidationQuery("select 1"); dataSource.setTestWhileIdle(true);
	 * dataSource.setTimeBetweenEvictionRunsMillis(30*1000);
	 */
	return dataSource;
    }

    @Bean
    public TimeTaskFactory createTimeTaskFactory() {

	final TimeTaskFactory timeTaskFactory = new TimeTaskFactory();

	// timeTaskFactory.setName(factoryName);
	// timeTaskFactory.setAppName(appName);
	/*
	 * if(DefaultConfigClient.isProPre()) { timeTaskFactory.setAppEnv("pre"); }
	 */
	timeTaskFactory.setDataSource(createDataSource());
	/*
	 * if(StringUtils.isNotBlank(appEnv)){ timeTaskFactory.setAppEnv(appEnv); }
	 * 
	 * //timeTaskFactory.setClusterMode(false);
	 * if(StringUtils.isNotBlank(redisHost)) {
	 * timeTaskFactory.setRedisHost(redisHost);
	 * timeTaskFactory.setRedisPort(redisPort); }
	 */

	return timeTaskFactory;
    }

}
