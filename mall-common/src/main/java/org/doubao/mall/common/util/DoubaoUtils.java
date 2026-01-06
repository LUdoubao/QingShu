package org.doubao.mall.common.util;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Doubao 工具类
 * 提供常用的工具方法，特别是针对空值检查的工具方法
 * 该类封装了 Spring 的 ObjectUtils、CollectionUtils 和 StringUtils，
 * 提供统一的 API 来替代直接使用这些工具类的方法
 */
public class DoubaoUtils {
	
	/**
	 * 检查对象是否为 null
	 * @param obj 待检查的对象
	 * @return 如果对象为 null 则返回 true，否则返回 false
	 */
	public static boolean isNull(@Nullable Object obj) {
		return Objects.isNull(obj);
	}

	/**
	 * 检查对象是否不为 null
	 * @param obj 待检查的对象
	 * @return 如果对象不为 null 则返回 true，否则返回 false
	 */
	public static boolean notNull(@Nullable Object obj) {
		return Objects.nonNull(obj);
	}

	/**
	 * 检查对象是否为空
	 * 此方法使用 ObjectUtils.isEmpty() 来判断，它认为 null、空字符串、空数组、空集合和空Map都是空
	 * @param obj 待检查的对象
	 * @return 如果对象为空则返回 true，否则返回 false
	 */
	public static boolean isEmpty(@Nullable Object obj) {
		return ObjectUtils.isEmpty(obj);
	}

	/**
	 * 检查对象是否不为空
	 * @param obj 待检查的对象
	 * @return 如果对象不为空则返回 true，否则返回 false
	 */
	public static boolean isNotEmpty(@Nullable Object obj) {
		return !ObjectUtils.isEmpty(obj);
	}

	/**
	 * 检查数组是否为空
	 * @param array 待检查的数组
	 * @return 如果数组为空或null则返回 true，否则返回 false
	 */
	public static boolean isEmpty(@Nullable Object[] array) {
		return ObjectUtils.isEmpty(array);
	}

	/**
	 * 检查数组是否不为空
	 * @param array 待检查的数组
	 * @return 如果数组不为null且不为空则返回 true，否则返回 false
	 */
	public static boolean isNotEmpty(@Nullable Object[] array) {
		return !ObjectUtils.isEmpty(array);
	}
	
	/**
	 * 检查集合是否为空
	 * @param collection 集合
	 * @return 如果集合为空或null则返回 true，否则返回 false
	 */
	public static boolean isEmpty(@Nullable Collection<?> collection) {
		return CollectionUtils.isEmpty(collection);
	}
	
	/**
	 * 检查集合是否不为空
	 * @param collection 集合
	 * @return 如果集合不为null且不为空则返回 true，否则返回 false
	 */
	public static boolean isNotEmpty(@Nullable Collection<?> collection) {
		return !CollectionUtils.isEmpty(collection);
	}
	
	/**
	 * 检查Map是否为空
	 * @param map Map
	 * @return 如果Map为空或null则返回 true，否则返回 false
	 */
	public static boolean isEmpty(@Nullable Map<?, ?> map) {
		return CollectionUtils.isEmpty(map);
	}
	
	/**
	 * 检查Map是否不为空
	 * @param map Map
	 * @return 如果Map不为null且不为空则返回 true，否则返回 false
	 */
	public static boolean isNotEmpty(@Nullable Map<?, ?> map) {
		return !CollectionUtils.isEmpty(map);
	}
	
	/**
	 * 检查字符串是否为空
	 * @param str 字符串
	 * @return 如果字符串为空或null则返回 true，否则返回 false
	 */
	public static boolean isEmpty(@Nullable String str) {
		return StringUtils.isEmpty(str);
	}
	
	/**
	 * 检查字符串是否不为空
	 * @param str 字符串
	 * @return 如果字符串不为null且不为空则返回 true，否则返回 false
	 */
	public static boolean isNotEmpty(@Nullable String str) {
		return !StringUtils.isEmpty(str);
	}
}