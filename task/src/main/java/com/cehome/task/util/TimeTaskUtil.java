/*
 *
 */
package com.cehome.task.util;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;

import com.alibaba.fastjson.JSONObject;

public class TimeTaskUtil {
    public static JSONObject getJSON(final String s) {
	if (s != null && s.trim().length() > 0) {
	    try {
		return JSONObject.parseObject(s);
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	}
	return new JSONObject();
    }

    public static <T> T getJSON(final String s, final Class<T> clazz) {
	if (s != null && s.trim().length() > 0) {
	    try {
		return JSONObject.parseObject(s, clazz);
	    } catch (final Exception e) {
		e.printStackTrace();
	    }
	}
	try {
	    return clazz.newInstance();
	} catch (final Exception e) {
	    throw new RuntimeException(e);
	}
    }

    public static String getFullUrl(final HttpServletRequest request) throws IOException {

	String url = request.getServletPath();
	if (!request.getContextPath().equals("/")) {
	    url = request.getContextPath() + url;
	}
	if (request.getQueryString() != null) {
	    url += "?" + request.getQueryString();
	}
	return url;

    }

    public static String removeParam(final String query, final String name) {
	int n = query.indexOf('?');
	if (n == -1) {
	    return query + "?";
	}
	n = query.indexOf(name + "=", n + 1);
	if (n >= 0) {
	    final int begin = n > 0 && query.charAt(n - 1) == '&' ? n - 1 : n;
	    final int end = query.indexOf('&', begin + 1);
	    if (end == -1) {
		return query.substring(0, begin);
	    } else {
		return query.substring(0, begin) + query.substring(end);
	    }
	}
	return query;
    }

    public static Object registerBean(final AbstractAutowireCapableBeanFactory factory, final String name,
	    final Object bean) {

	factory.autowireBean(bean);
	factory.registerSingleton(name, bean);
	factory.initializeBean(bean, name);
	return bean;
    }

}