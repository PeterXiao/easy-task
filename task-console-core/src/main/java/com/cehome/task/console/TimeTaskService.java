/*
 *
 */
package com.cehome.task.console;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;

import com.alibaba.fastjson.JSONObject;
import com.cehome.task.TimeTaskFactory;
import com.cehome.task.client.RemoteLogService;
import com.cehome.task.dao.TimeTaskDao;
import com.cehome.task.domain.TimeTask;
import com.cehome.task.util.TimeTaskUtil;

import jsharp.util.Convert;

//@Service
public class TimeTaskService implements InitializingBean, DisposableBean {
    // public static String APP_NAME = GlobalService.TIME_TASK_APP_NAME;
    protected static final Logger logger = LoggerFactory.getLogger(TimeTaskService.class);
    public static int CAT_SIZE = 4;
    @Resource
    TimeTaskDao timetaskDao;
    @Autowired
    private MachineListService machineListService;

    @Autowired
    TimeTaskFactory timeTaskFactory;

    Map<String, Map<String, Set<String>>> propsMaps = new java.util.concurrent.ConcurrentHashMap<>();
    long catLastModified = 0;
    long dbLastModified = 0;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    /**
     * 添加和修改
     *
     * @param timeTask
     * @return
     */
    public int save(final TimeTask timeTask) {
	return timetaskDao.save(timeTask);
    }

    /**
     * 获取任务
     *
     * @param id
     * @return
     */
    public TimeTask get(final long id) {
	return timetaskDao.get(id);
    }

    /**
     * 更新状态 0：停止 1：运行 2：删除
     *
     * @param id
     * @param toStatus
     */
    public void updateStatus(final long id, final int toStatus, final String operUser) {
	final TimeTask timeTask = timetaskDao.createObject();
	timeTask.setId(id);
	timeTask.setStatus(toStatus);
	timeTask.setOperUser(operUser);
	save(timeTask);
    }

    /**
     * 删除
     *
     * @param id
     * @param real true真删 false做标记
     */
    public void delete(final long id, final boolean real, final String operUser) {
	if (real) {
	    timetaskDao.deleteById(id);
	} else {
	    updateStatus(id, 2, operUser);
	}
    }

    /**
     * 优先级 9是高危
     *
     * @param id
     * @param priority
     */
    public void updatePriority(final int id, final int priority, final String operUser) {

	final TimeTask timeTask = timetaskDao.createObject();
	timeTask.setId(id);
	timeTask.setPriority(priority);
	timeTask.setOperUser(operUser);
	save(timeTask);

    }

    public int batchSwitchIP(final List<Long> ids, final String appName, final String ip, final String operUser) {
	// String[] ipInfo=ip.split(",");
	final String where = " id in (" + Convert.toString(ids, ",", null) + " ) and {appName}=? ";
	final TimeTask timeTask = timetaskDao.createObject();
	timeTask.setTargetIp(ip);
	timeTask.setLastTargetIp("");
	timeTask.setOperUser(operUser);
	return timetaskDao.updateByWhere(timeTask, where, appName);
    }

    private Object getTimeTaskList(final String words, final Map<String, Object> params, final int state, final int pn,
	    final int ps, final boolean count) throws Exception {

	String sql = "select " + (count ? "count(*)" : "*") + " from " + timetaskDao.getTableName() + " ";
	String where = "";
	final List<String> wordList = splitWords(words);
	final List queryParams = new ArrayList();
	if (wordList != null && wordList.size() > 0) {
	    final String[] fields = { "name", "category1", "category2", "category3", "category4", "{appName}",
		    "{targetIp}", "{operUser}", "{createUser}" };
	    for (final String word : wordList) {
		String ww = "";
		for (final String field : fields) {
		    if (ww.length() > 0) {
			ww += " or ";
		    }
		    ww += field + " like ? ";
		    queryParams.add("%" + word + "%");
		}

		where += " and ( " + ww + " ) ";
	    }
	}

	if (params.get("category1") != null) {
	    where += " and ( category1 = ? ) ";
	    queryParams.add(params.get("category1"));
	}
	if (params.get("category2") != null) {
	    where += " and ( category2 = ? ) ";
	    queryParams.add(params.get("category2"));
	}
	if (params.get("category3") != null) {
	    where += " and ( category3 = ? ) ";
	    queryParams.add(params.get("category3"));
	}
	if (params.get("category4") != null) {
	    where += " and ( category4 = ? ) ";
	    queryParams.add(params.get("category4"));
	}
	if (params.get("ip") != null) {
	    where += " and ( target_ip = ? ) ";
	    queryParams.add(params.get("ip"));
	}

	/*
	 * if (params.get("scheduler") != null) { where += " and ( scheduler = ? ) ";
	 * queryParams.add(params.get("scheduler")); }
	 */

	if (params.get("priority") != null) {
	    where += " and ( priority = ? ) ";
	    queryParams.add(params.get("priority"));
	}

	if (params.get("appName") != null) {
	    where += " and ( {appName} = ? ) ";
	    queryParams.add(params.get("appName"));
	}
	final int taskType = Convert.toInt(params.get("taskType"), -1);
	if (taskType > -1) {
	    where += " and ( {taskType} = ? ) ";
	    queryParams.add(params.get("taskType"));
	}

	if (params.get("userId") != null) {
	    where += " and ( {userId} = ? ) ";
	    queryParams.add(params.get("userId"));
	}

	if (params.get("appInfo") != null) {
	    where += " and ( concat(',',{category4},',')  like ? ) ";
	    queryParams.add("%," + params.get("appInfo") + ",%");
	}

	if (params.get("createUser") != null) {
	    where += " and ( {createUser} = ? ) ";
	    queryParams.add(params.get("createUser"));
	}

	if (params.get("id") != null) {
	    where += " and ( {id} = ? ) ";
	    queryParams.add(params.get("id"));
	}

	if (state == -1) {
	    where += " and ( {status} in (0,1) ) ";
	} else {
	    where += " and ( {status} =? ) ";
	    queryParams.add(state);
	}

	if (where.length() > 0) {
	    where = where.trim().substring(3);
	    sql += " where " + where;
	}

	if (!count) {
	    sql += " order by {id} desc ";
	}

	if (!count) {
	    sql += " limit ?,? ";
	    queryParams.add((pn - 1) * ps);
	    queryParams.add(ps);
	}

	return count ? timetaskDao.queryValue(sql, queryParams).getInt(0) : timetaskDao.queryList(sql, queryParams);

    }

    public int getCount(final String words, final Map<String, Object> params, final int state, final int pn,
	    final int ps) throws Exception {
	return (Integer) getTimeTaskList(words, params, state, pn, ps, true);
    }

    public List<TimeTask> getList(final String words, final Map<String, Object> params, final int state, final int pn,
	    final int ps) throws Exception {
	return (List<TimeTask>) getTimeTaskList(words, params, state, pn, ps, false);
    }

    public static List<String> splitWords(String s) {
	if (s == null || s.trim().length() == 0) {
	    return null;
	}

	final List<String> result = new ArrayList<>();
	int i = 0;
	int begin = 0; // "addd eeee 总共 cddddd"
	s = s + " ";
	while (i < s.length()) {
	    final char c = s.charAt(i);
	    if (c <= 32) {
		if (i > begin) {
		    result.add(s.substring(begin, i));
		}
		begin = i + 1;
	    } else if (c >= 128) {
		if (i > begin) {
		    result.add(s.substring(begin, i));
		}
		result.add(s.substring(i, i + 1));
		begin = i + 1;
	    }
	    i++;
	}
	return result;

    }

    private static String formatWords(String s) {
	if (s == null || s.trim().length() == 0) {
	    return s;
	}

	final StringBuilder sb = new StringBuilder(s.length());
	int i = 0;
	int begin = 0; // "addd eeee 总共 cddddd"
	s = s + " ";
	while (i < s.length()) {
	    final char c = s.charAt(i);
	    if (c <= 32) {
		if (i > begin) {
		    sb.append(" AND \"" + s.substring(begin, i) + "\"");
		}
		begin = i + 1;
	    } else if (c >= 128) {
		if (i > begin) {
		    sb.append(" AND \"" + s.substring(begin, i) + "\"");
		}
		sb.append(" AND \"" + s.substring(i, i + 1) + "\"");
		begin = i + 1;
	    }
	    i++;
	}
	String res = sb.toString().trim();
	if (res.startsWith("AND")) {
	    res = res.substring(3).trim();
	}
	return res;

    }

    public static void main(final String[] args) {
	System.out.println(formatWords("  1addd eeee   我10.10. 我  总共 cddddd"));
    }

    @Override
    public void destroy() throws Exception {
    }

    public RemoteLogService getRemoteLogService(final String host, final String port) {
	RmiProxyFactoryBean factory = null;
	factory = new RmiProxyFactoryBean();
	factory.setServiceInterface(RemoteLogService.class);
	factory.setServiceUrl("rmi://" + host + ":" + port + "/remoteLogService");
	factory.afterPropertiesSet();
	return (RemoteLogService) factory.getObject();
    }

    public ClientServiceProxy getClientServiceProxy(final TimeTask timeTask) {
	final ClientServiceProxy clientServiceProxy = new ClientServiceProxy();
	final String url = machineListService.getServiceUrl(timeTask.getAppName(), timeTask.getTargetIp());
	if (url == null) {
	    throw new RuntimeException("can not find target url for app=" + timeTask.getAppName() + ", ip="
		    + timeTask.getTargetIp() + ". Ensure it is online");
	}
	clientServiceProxy.setBaseUrl(url);
	return clientServiceProxy;

    }

    public Map<String, Set<String>> getPropsMap(final int taskType, final long userId) {
	final String key = taskType + "_" + userId;
	Map<String, Set<String>> propsMap = propsMaps.get(key);
	if (propsMap == null || System.currentTimeMillis() - catLastModified > 1000 * 10) {
	    synchronized (this) {
		propsMap = propsMaps.get(key);
		if (propsMap == null || System.currentTimeMillis() - catLastModified > 1000 * 10) {

		    final Date date = getLastModified();
		    final long time = date == null ? 0 : date.getTime();
		    final boolean changed = propsMap == null || dbLastModified != time;
		    if (changed) {
			final List<TimeTask> configs = timetaskDao.listValid(taskType, userId);
			final Map<String, Set<String>> map = new HashMap<>();

			// Set<String> targetIpSet = new
			// TreeSet<String>(machineListService.getMachines(null));
			// map.put("targetIp",targetIpSet);

			final Set<String>[] catSets = new TreeSet[CAT_SIZE + 1];
			for (int i = 1; i < CAT_SIZE + 1; i++) {
			    catSets[i] = new TreeSet<>();
			    map.put("cat" + i, catSets[i]);
			}

			final Set<String> beanNameSet = new TreeSet<>();
			map.put("beanName", beanNameSet);
			for (final TimeTask timeTask : configs) {

			    final String[] categories = timeTask.getCategories();
			    for (int i = 0; i < categories.length; i++) {
				if (!StringUtils.isBlank(categories[i])) {
				    catSets[i + 1].add(categories[i]);
				}
			    }

			    final JSONObject json = TimeTaskUtil.getJSON(timeTask.getConfig());
			    final String beanName = json.getString("bean");
			    if (!StringUtils.isBlank(beanName)) {
				beanNameSet.add(beanName);
			    }

			}
			catLastModified = System.currentTimeMillis();
			dbLastModified = time;
			propsMap = map;
			propsMaps.put(key, propsMap);
		    }
		}

	    }

	}
	return propsMap;
    }

    /**
     * 合并并返还新的map
     *
     * @param maps
     * @return
     */
    public Map<String, Set<String>> combinMapSet(final Map<String, Set<String>>... maps) {
	final Map<String, Set<String>> map1 = maps[0];
	final Map<String, Set<String>> propsMap = new HashMap<>(map1.size());
	for (final String key : map1.keySet()) {
	    propsMap.put(key, new HashSet<>(map1.get(key)));
	}
	for (int i = 1; i < maps.length; i++) {
	    final Map<String, Set<String>> map2 = maps[i];
	    for (final String key : map2.keySet()) {
		final Set<String> set = propsMap.get(key);
		if (set == null) {
		    propsMap.put(key, new HashSet<>(map2.get(key)));
		} else {
		    set.addAll(map2.get(key));
		}
	    }
	}
	return propsMap;
    }

    /**
     * 分类排序
     *
     * @param propsMap
     * @return
     */
    public Map<String, Set<String>> sortCats(final Map<String, Set<String>> propsMap) {

	propsMap.put("cat4", fixChannels(propsMap.get("cat4")));
	for (int i = 0; i < TimeTaskService.CAT_SIZE + 1; i++) {
	    propsMap.put("cat" + i, sortSet(propsMap.get("cat" + i)));
	}
	return propsMap;
    }

    private Set<String> sortSet(final Set<String> set) {
	final String[] ss = set.toArray(new String[0]);
	final Comparator com = Collator.getInstance(Locale.CHINA);
	Arrays.sort(ss, com);
	final Set<String> set2 = new LinkedHashSet<>(Arrays.asList(ss));
	return set2;

    }

    private Set<String> fixChannels(final Set<String> set) {

	final Set<String> set2 = new LinkedHashSet<>(set.size());
	for (final String s : set) {
	    final String[] ss = s.split("[,;]+");
	    for (final String a : ss) {
		if (a.length() > 0) {
		    set2.add(a);
		}
	    }
	}
	return set2;
    }

    public Date getLastModified() {
	return timetaskDao.getLastModified();
    }

}
