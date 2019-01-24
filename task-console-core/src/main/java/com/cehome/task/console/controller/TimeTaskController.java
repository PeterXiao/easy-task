/*
 *
 */
package com.cehome.task.console.controller;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.WebUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cehome.task.Constants;
import com.cehome.task.TimeTaskFactory;
import com.cehome.task.console.ClientServiceProxy;
import com.cehome.task.console.GlobalService;
import com.cehome.task.console.MachineListService;
import com.cehome.task.console.TaskTypeCustom;
import com.cehome.task.console.TimeTaskService;
import com.cehome.task.domain.TimeTask;
import com.cehome.task.domain.TimeTaskSearch;
import com.cehome.task.util.TimeTaskUtil;

import jsharp.util.Convert;
import jsharp.util.EntityUtils;
import jsharp.util.WebKit;

public class TimeTaskController implements ApplicationContextAware {
    protected static final Logger logger = LoggerFactory.getLogger(TimeTaskController.class);
    // public static String APP_NAME = GlobalService.TIME_TASK_APP_NAME;
    public static int AREA_TASK = 0;
    public static int AREA_CRAWLER = 1;
    public static int AREA_RSS = 2;
    // private static String P="4659729871798381715";

    protected ApplicationContext context;
    @Resource
    protected TimeTaskService timeTaskService;
    @Resource
    protected MachineListService machineListService;
    // @Resource
    // protected RemoteLogService remoteLogService;
    @Autowired
    protected GlobalService globalService;

    @Autowired
    TimeTaskFactory timeTaskFactory;

    @RequestMapping("index.htm")
    public void index(final HttpServletRequest request, final HttpServletResponse response, final Model model)
	    throws Exception {
	response.sendRedirect("list.htm");
    }

    @RequestMapping("list.htm")
    public String list(final TimeTaskSearch timeTaskSearch, final HttpServletRequest request,
	    final HttpServletResponse response, final Model model) throws Exception {
	final Map<String, Object> params = new HashMap<>();

	final int taskType = timeTaskSearch.getTaskType();
	final Long userId = timeTaskSearch.getUserId();
	final int status = timeTaskSearch.getStatus();
	final String words = timeTaskSearch.getWords();
	final int danger = timeTaskSearch.getDanger();
	final String createUser = timeTaskSearch.getCreateUser();
	final int scroll = timeTaskSearch.getScroll();
	final Long timeTaskId = timeTaskSearch.getTimeTaskId();
	final int ps = timeTaskSearch.getPs();
	int pn = timeTaskSearch.getPn();
	final String targetIp = timeTaskSearch.getTargetIp();

	String appName = timeTaskSearch.getAppName();
	final Collection<String> appNames = machineListService.getAppNames();
	// set default appNames
	if (appNames.size() == 1 && StringUtils.isBlank(appName)) {
	    appName = appNames.iterator().next();
	}

	if (StringUtils.isNotBlank(appName)) {
	    params.put("appName", appName);
	}
	params.put("taskType", taskType);

	// params.put("type", 4);
	// --普通用户
	/*
	 * int admin=1; if(admin==0) { userId= globalService.getLoginUserId(request);
	 * params.put("userId", userId);
	 *
	 * Map<String, Set<String>> propsMap
	 * =timeTaskService.combinMapSet(timeTaskService.getPropsMap(taskType,userId));
	 * timeTaskService.sortCats(propsMap); model.addAttribute("propsMap", propsMap);
	 * } //--管理员 else{ if(!globalService.isAdminUser(request)) { throw new
	 * Exception("not admin"); } if(userId!=null) { params.put("userId", userId); }
	 *
	 * }
	 */
	// if (globalService.getLoginUserId(request) == 59311) {
	// params.remove("userId");
	// }
	for (int i = 1; i <= TimeTaskService.CAT_SIZE; i++) {

	    String cat = request.getParameter("cat" + i);
	    if (cat == null) {
		cat = "";
	    }
	    if (cat.length() > 0) {
		params.put("category" + i, cat);
	    }
	    model.addAttribute("cat" + i, cat);

	}

	if (StringUtils.isNotBlank(createUser)) {
	    params.put("createUser", createUser);
	}

	if (targetIp.length() > 0) {
	    params.put("ip", targetIp);
	}
	if (danger > 0) {
	    params.put("priority", 9);
	}

	if (timeTaskId != null) {
	    params.put("id", timeTaskId);
	}
	final int totalCount = timeTaskService.getCount(words, params, status, pn, ps);
	final int pageCount = totalCount == 0 ? 0 : (totalCount - 1) / ps + 1;
	if (pn == -2 || pn > pageCount) {
	    pn = pageCount;
	}
	if (pn < 1) {
	    pn = 1;
	}
	final List<TimeTask> list = timeTaskService.getList(words, params, status, pn, ps);

	/*
	 * Map<Long,Object> taskConfigs=new HashMap<Long,Object>(); for(TimeTask
	 * timeTask:list){ JSONObject json= JSON.parseObject( timeTask.getTaskConfig());
	 * taskConfigs.put(timeTask.getId(),json); } model.addAttribute("taskConfigs",
	 * taskConfigs);
	 */

	String url = TimeTaskUtil.getFullUrl(request);
	url = TimeTaskUtil.removeParam(url, "scroll");
	final String basePageUrl = TimeTaskUtil.removeParam(url, "pn");

	model.addAttribute("list", list);
	model.addAttribute("words", words);
	model.addAttribute("taskType", taskType);
	model.addAttribute("status", status);
	model.addAttribute("targetIp", targetIp);
	model.addAttribute("danger", danger);
	model.addAttribute("appName", appName);
	model.addAttribute("createUser", createUser);
	model.addAttribute("userId", userId);
	model.addAttribute("timeTaskId", timeTaskId);
	// model.addAttribute("admin", admin);

	model.addAttribute("url", url);
	model.addAttribute("basePageUrl", basePageUrl);
	model.addAttribute("pn", pn);
	model.addAttribute("ps", ps);
	model.addAttribute("scroll", scroll);
	model.addAttribute("pageCount", pageCount);
	model.addAttribute("totalCount", totalCount);

	model.addAttribute("appNames", appNames);

	// 定制的字段
	// model.addAttribute("appInfos",dispatchChannelService.getDispatchChannes());
	// rss 定制

	model.addAttribute("areaCustom", TaskTypeCustom.getTaskTypeCustom(taskType));
	// model.addAttribute("online", DefaultConfigClient.isPro());

	// model.addAttribute("machines", machineListService.getMachines());

	if (StringUtils.isNotBlank(appName)) {
	    model.addAttribute("machines", machineListService.getMachines(appName));
	    model.addAttribute("switchMachines", machineListService.getMachines(appName, true));
	} else {
	    model.addAttribute("machines", new HashSet<String>());
	    model.addAttribute("switchMachines", machineListService.getMachines(true));
	}

	return "timeTask/list";

    }

    /*
     * private String extractIP(String ipInfo){ String[] ss=ipInfo.split(",");
     * if(ss.length>1) return ss[1]; return ipInfo; }
     */

    // id=5&pn=dd ==> id=5

    @RequestMapping("edit.htm")
    public String edit(@RequestParam(value = "taskType", required = false, defaultValue = "0") final int taskType,
	    final HttpServletRequest request, final HttpServletResponse response, final Model model) throws Exception {
	final int id = Convert.toInt(request.getParameter("id"), 0);
	TimeTask timeTask = null;
	final long userId = globalService.getLoginUserId(request);
	final Map<String, Set<String>> propsMap = timeTaskService.getPropsMap(taskType, userId);
	// Collection<String> ipSet=timeTaskMachineService.getMachines();
	final Collection<String> appNames = machineListService.getAppNames();
	// copy (no use anymore)
	if (id < 0) {
	    timeTask = timeTaskService.get(-id);
	    final TimeTask newTimeTask = EntityUtils.create(TimeTask.class);
	    EntityUtils.copyExclude(timeTask, newTimeTask, "id", "taskResult", "lastStartTime", "lastEndTime");

	    newTimeTask.setStatus(0);
	    final String randomIp = machineListService.getRandomMachine(timeTask.getAppName());
	    if (randomIp != null) {
		newTimeTask.setTargetIp(randomIp);
	    }
	    timeTaskService.save(newTimeTask);
	    timeTask = newTimeTask;

	} else if (id == 0) { // new
	    timeTask = new TimeTask();
	    timeTask.setTaskType(taskType);
	    timeTask.setCronType(0);
	    timeTask.setCron("60m");//
	    timeTask.setPriority(1);
	    if (appNames.size() == 1) {
		timeTask.setAppName(appNames.iterator().next());
		final String randomIp = machineListService.getRandomMachine(timeTask.getAppName());
		if (randomIp != null) {
		    timeTask.setTargetIp(randomIp);
		}

	    }
	    // String randomIp= machineListService.getRandomMachine(null);
	    // if(randomIp!=null) timeTask.setTargetIp(randomIp);
	} else {
	    timeTask = timeTaskService.get(id);
	}
	final JSONObject areaCustom = TaskTypeCustom.getTaskTypeCustom(timeTask.getTaskType());
	final JSONObject beanObject = areaCustom.getJSONObject("bean");
	int fixed = 0;
	if (beanObject != null) {
	    fixed = beanObject.getIntValue("fixed");
	}

	model.addAttribute("cats", timeTask.getCategories());
	model.addAttribute("entity", timeTask);
	final JSONObject invoker = TimeTaskUtil.getJSON(timeTask.getConfig());
	final JSONObject args = invoker.getJSONObject("args");
	invoker.put("args", args == null ? "{}" : args.toJSONString());
	model.addAttribute("invoker", invoker);

	model.addAttribute("propsMap", propsMap);
	model.addAttribute("areaCustom", TaskTypeCustom.getTaskTypeCustom(taskType));
	model.addAttribute("fixed", fixed);
	model.addAttribute("appNames", appNames);
	if (timeTask.getAppName() != null) {
	    final Collection<String> machines = machineListService.getMachines(timeTask.getAppName());
	    model.addAttribute("machines", machines);
	}
	return "timeTask/edit";
    }

    @ResponseBody
    @RequestMapping("copy.htm")
    public String copy(final long id, final HttpServletRequest request, final HttpServletResponse response,
	    final Model model) throws Exception {
	final TimeTask timeTask = timeTaskService.get(id);
	final TimeTask newTimeTask = EntityUtils.create(TimeTask.class);
	EntityUtils.copyExclude(timeTask, newTimeTask, "id", "taskResult", "lastStartTime", "lastEndTime");

	newTimeTask.setStatus(0);
	final String randomIp = machineListService.getRandomMachine(timeTask.getAppName());
	if (randomIp != null) {
	    newTimeTask.setTargetIp(randomIp);
	}
	timeTaskService.save(newTimeTask);
	return "" + newTimeTask.getId();
    }

    @ResponseBody
    @RequestMapping("save.htm")
    public String save(final HttpServletRequest request, final HttpServletResponse response, final Model model)
	    throws Exception {
	final TimeTask timeTask = EntityUtils.create(TimeTask.class, request);

	// String[] ipInfo=
	// machineListService.extractMachine(timeTask.getTargetIp().trim());
	// timeTask.setTargetIp(ipInfo[1]);

	// timeTask.setJobType(4);// 基于数据库的任务实例
	timeTask.setCategory1(request.getParameter("cat1"));
	timeTask.setCategory2(request.getParameter("cat2"));
	timeTask.setCategory3(request.getParameter("cat3"));
	// 用作分发渠道了，这里不配置
	// timeTask.setCategory4(request.getParameter("cat4"));
	timeTask.setOperUser(globalService.getLoginUsername(request));

	if (timeTask.getCron().trim().indexOf(' ') > 0) {
	    timeTask.setCronType(1); // cron
	} else {
	    timeTask.setCronType(0);// interval
	    // timeTask.setMonFlag(Convert.toInt(request.getParameter("monFlag"),0));
	}

	final JSONObject areaCustom = TaskTypeCustom.getTaskTypeCustom(timeTask.getTaskType());
	final JSONObject beanObject = areaCustom.getJSONObject("bean");
	// -- 正常设置参数
	if (beanObject == null || beanObject.getIntValue("fixed") == 0) {
	    final JSONObject args = TimeTaskUtil.getJSON(request.getParameter("args"));
	    final String bean = request.getParameter("bean");
	    final String method = request.getParameter("method");
	    final String stopMethod = request.getParameter("stopMethod");
	    final JSONObject json = new JSONObject();
	    json.put("bean", bean);
	    json.put("method", method);
	    json.put("stopMethod", stopMethod);
	    json.put("args", args);
	    timeTask.setConfig(json.toJSONString());
	} else {
	    // --参数固定，则插入时候指定
	    if (timeTask.getId() == 0) {
		final JSONObject json = new JSONObject();
		json.put("bean", beanObject.getString("bean"));
		json.put("method", beanObject.getString("method"));
		json.put("stopMethod", beanObject.getString("stopMethod"));
		json.put("args", beanObject.getJSONObject("args"));
		timeTask.setConfig(json.toJSONString());
	    } else {
		checkPass(timeTask.getId(), request);

	    }
	}

	if (timeTask.getId() == 0) {
	    timeTask.setUserId(globalService.getLoginUserId(request));
	    timeTask.setCreateUser(globalService.getLoginUsername(request));
	}

	beforeSave(timeTask, request);
	timeTaskService.save(timeTask);
	afterSave(timeTask, request);

	return "" + timeTask.getId();

    }

    protected void beforeSave(final TimeTask timeTask, final HttpServletRequest request) {

    }

    protected void afterSave(final TimeTask timeTask, final HttpServletRequest request) {

    }

    @ResponseBody
    @RequestMapping("getIpList.htm")
    public String getIpList(@RequestParam(value = "appName") final String appName) throws Exception {
	final JSONArray array = new JSONArray();
	final Collection<String> collection = machineListService.getMachines(appName);
	for (final String s : collection) {
	    array.add(s);
	}
	return array.toJSONString();
    }

    protected void checkPass(final long id, final HttpServletRequest request) throws Exception {
	if (globalService.isAdminUser(request)) {
	    return;
	}

    }

    @RequestMapping("delete.htm")
    public void delete(@RequestParam(value = "ids") final String[] ids, final HttpServletRequest request,
	    final HttpServletResponse response, final Model model) throws Exception {
	for (final String id : ids) {
	    checkPass(Long.parseLong(id), request);
	    timeTaskService.updateStatus(Long.parseLong(id), 2, globalService.getLoginUsername(request));
	}

    }

    @RequestMapping("rank.htm")
    public void rank(@RequestParam(value = "ids") final String[] ids, @RequestParam(value = "rank") final int rank,
	    final HttpServletRequest request, final HttpServletResponse response, final Model model) throws Exception {
	// 高危9 普通

	for (final String id : ids) {
	    checkPass(Long.parseLong(id), request);
	    timeTaskService.updatePriority(Integer.parseInt(id), rank, globalService.getLoginUsername(request));
	}

    }

    @RequestMapping("switchIp.htm")
    public void switchIp(@RequestParam(value = "ids") final long[] ids,
	    // @RequestParam(value = "appName") String appName,
	    @RequestParam(value = "ip") String ip, final HttpServletRequest request, final HttpServletResponse response,
	    final Model model) throws Exception {
	/*
	 * if(globalService.isOuterDomain(request)){ throw new
	 * Exception("access denied"); }
	 */
	final List<Long> list = new ArrayList();
	for (final long id : ids) {
	    list.add(id);
	}
	final int n = ip.indexOf(Constants.MACHINE_SPLIT);
	final String appName = ip.substring(0, n);
	ip = ip.substring(n + Constants.MACHINE_SPLIT.length());
	timeTaskService.batchSwitchIP(list, appName, ip, globalService.getLoginUsername(request));
    }

    @RequestMapping("exportTasks.htm")
    public void exportTasks(@RequestParam(value = "ids") final long[] ids, final HttpServletRequest request,
	    final HttpServletResponse response, final Model model) throws Exception {
	final List<TimeTask> array = new ArrayList<>();
	for (final long id : ids) {
	    final TimeTask timeTask = timeTaskService.get(id);
	    timeTask.setTaskResult(null);
	    // timeTask.setId(0);
	    timeTask.setStatus(0);
	    array.add(timeTask);
	}
	final String data = JSON.toJSONString(array);
	WebKit.sendFile(response, new ByteArrayInputStream(data.getBytes("UTF-8")), "text/json", true,
		"task_" + ids[0] + ".conf");
    }

    @RequestMapping("importTasks.htm")
    public void importTasks(@RequestParam(value = "file1") final MultipartFile file, final HttpServletRequest request,
	    final HttpServletResponse response, final Model model) throws Exception {

	if (!file.isEmpty()) {

	    final String data = new String(file.getBytes(), "UTF-8");
	    final JSONArray array = JSON.parseArray(data);

	    final Map<String, String[]> appMachines = new HashMap<>();
	    for (int i = 0; i < array.size(); i++) {
		final JSONObject obj = array.getJSONObject(i);
		final TimeTask timeTask = JSON.toJavaObject(obj, TimeTask.class);
		timeTask.setId(0);
		timeTask.setUserId(globalService.getLoginUserId(request));
		timeTask.setCreateUser(globalService.getLoginUsername(request));
		timeTask.setOperUser(globalService.getLoginUsername(request));

		String[] ips = appMachines.get(timeTask.getAppName());
		if (ips == null) {
		    // Map<String, Set<String>> propsMap= timeTaskService.getPropsMap(
		    // timeTask.getTaskType(),globalService.getLoginUserId(request));
		    final Collection<String> ipSet = machineListService.getMachines(timeTask.getAppName());// propsMap.get("targetIp");
		    if (ipSet.size() > 0) {
			ips = ipSet.toArray(new String[0]);
		    } else {
			ips = new String[0];
		    }
		    appMachines.put(timeTask.getAppName(), ips);
		}
		if (ArrayUtils.isNotEmpty(ips)) {
		    final int n = new Random().nextInt(ips.length);
		    final String randomIp = ips[n];
		    if (StringUtils.isNotBlank(randomIp)) {
			timeTask.setTargetIp(randomIp);
		    }
		}
		timeTaskService.save(timeTask);

	    }
	}

    }

    @RequestMapping("status.htm")
    public void status(@RequestParam(value = "ids") final long[] ids, @RequestParam(value = "status") final int status,
	    final HttpServletRequest request, final HttpServletResponse response, final Model model) throws Exception {
	for (final long id : ids) {
	    checkPass(id, request);
	    /*
	     * if (status == 1) status = 0; else status = 1;
	     */
	    timeTaskService.updateStatus(id, status, globalService.getLoginUsername(request));
	    if (status == 1 || status == 2) {
		// --可能存在则删除
		// TairService.getInstance().prefixDelete(ConstUtil.TAIR_PREFIX_TASK_NEXT_FIRE_TIME,
		// id + "");

	    }
	}
    }

    /*
     * @RequestMapping("deleteData.htm") public void deleteData(@RequestParam(value
     * = "ids") long[] ids,
     *
     * @RequestParam(value = "redo",required = false,defaultValue = "0") int redo,
     *
     * @RequestParam(value = "time",required = false) String time,
     *
     * @RequestParam(value = "time",required = false ,defaultValue = "0") int
     * cancel, HttpServletRequest request, HttpServletResponse response, Model
     * model) throws Exception { short namespace = (short)
     * EnvUtil.getInt("tair.rdb.namespace", 0); Date date=time==null?new Date():
     * DateParser.parse(time); for (long id : ids) { checkPass(id,request);
     * JSONObject json=new JSONObject(); json.put("date",date);
     * json.put("redo",redo); if(cancel==0){ tairManagerExt.hset(namespace,
     * ConstUtil.TAIR_EXT_PREFIX_DEL_DATA, id, json, (short) 0,
     * ConstUtil.SECONDS_OF_DAY * 30);} else{ tairManagerExt.hdel(namespace,
     * ConstUtil.TAIR_EXT_PREFIX_DEL_DATA, id, (short) 0, 0); } } }
     */

    @RequestMapping("getLog.htm")
    public String getLog(@RequestParam(value = "id") final long id,
	    @RequestParam(value = "pn", defaultValue = "1") long pn,
	    // @RequestParam(value = "ps",defaultValue="1024") long ps,
	    final HttpServletRequest request, final HttpServletResponse response, final Model model) throws Exception {
	String clientLogUrl = "";
	String logName = "";
	try {
	    checkPass(id, request);
	    final Cookie cookie = WebUtils.getCookie(request, "pageSize");
	    final long pageSize = cookie == null ? 5 : Convert.toLong(cookie.getValue(), 5);
	    final long ps = pageSize * 1024;
	    final TimeTask timeTask = timeTaskService.get(id);
	    // String host = timeTask.getTargetIp();
	    // String port = timeTaskFactory.getRmiPort();
	    logName = "task/" + timeTask.getId() + "-*.log";
	    final ClientServiceProxy clientServiceProxy = timeTaskService.getClientServiceProxy(timeTask);
	    clientLogUrl = clientServiceProxy.getBaseUrl() + Constants.CLIENT_SERVICE_URL_GET_LOG;
	    model.addAttribute("clientLogUrl", clientLogUrl);
	    model.addAttribute("logName", logName);
	    final long ts = Convert.toLong(clientServiceProxy.getLogSize(logName), 0);
	    final long totalSize = ts / 1024;
	    final long pageCount = (ts - 1) / ps + 1;
	    if (pn == -2 || pn > pageCount) {
		pn = pageCount;
	    }
	    if (pn < 1) {
		pn = 1;
	    }
	    final String log = clientServiceProxy.getLog(logName, pn, ps);
	    model.addAttribute("taskName", timeTask.getId() + "[" + timeTask.getName() + "]");
	    model.addAttribute("log", log);
	    model.addAttribute("pn", pn);
	    model.addAttribute("pageSize", pageSize);
	    model.addAttribute("pageCount", pageCount);
	    model.addAttribute("totalSize", totalSize);
	    return "timeTask/getLog";
	} catch (final Exception e) {
	    logger.error("getLog " + clientLogUrl, e);
	    model.addAttribute("message", "url=" + clientLogUrl + "<br>logName=" + logName + "<br>exception=" + e);
	    return "timeTask/message";
	}

    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
	context = applicationContext;
    }

    @RequestMapping("removeAppName.htm")
    public void removeAppName(final String appName, final HttpServletRequest request,
	    final HttpServletResponse response, final Model model) throws Exception {
	machineListService.removeAppName(appName);

    }

    @ResponseBody
    @RequestMapping("test.htm")
    public String test() throws Exception {
	final WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
	// webApplicationContext.get
	final String path = "";// webApplicationContext.getServletContext().getContextPath();
	return path + ":" + getServerPort(true);
    }

    private static String getServerPort(final boolean secure) throws Exception {
	MBeanServer mBeanServer = null;
	if (MBeanServerFactory.findMBeanServer(null).size() > 0) {
	    mBeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
	}

	if (mBeanServer == null) {
	    logger.debug("调用findMBeanServer查询到的结果为null");
	    return "";
	}

	Set<ObjectName> names = null;
	try {
	    names = mBeanServer.queryNames(new ObjectName("Catalina:type=Connector,*"), null);
	} catch (final Exception e) {
	    return "";
	}
	final Iterator<ObjectName> it = names.iterator();
	ObjectName oname = null;
	while (it.hasNext()) {
	    oname = it.next();
	    final String protocol = (String) mBeanServer.getAttribute(oname, "protocol");
	    String scheme = (String) mBeanServer.getAttribute(oname, "scheme");
	    Boolean secureValue = (Boolean) mBeanServer.getAttribute(oname, "secure");
	    final Boolean SSLEnabled = (Boolean) mBeanServer.getAttribute(oname, "SSLEnabled");
	    if (SSLEnabled != null && SSLEnabled) {// tomcat6开始用SSLEnabled
		secureValue = true;// SSLEnabled=true但secure未配置的情况
		scheme = "https";
	    }
	    if (protocol != null && ("HTTP/1.1".equals(protocol) || protocol.contains("http"))) {
		if (secure && "https".equals(scheme) && secureValue) {
		    return ((Integer) mBeanServer.getAttribute(oname, "port")).toString();
		} else if (!secure && !"https".equals(scheme) && !secureValue) {
		    return ((Integer) mBeanServer.getAttribute(oname, "port")).toString();
		}
	    }
	}
	return "";
    }

}
