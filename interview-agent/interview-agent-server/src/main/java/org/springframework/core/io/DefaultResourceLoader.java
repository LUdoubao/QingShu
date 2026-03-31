//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.core.io;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * Spring 框架中 {@link ResourceLoader} 接口的默认实现。
 * 该类负责根据资源路径字符串解析为 {@link Resource} 对象，支持以下资源类型：
 * <ul>
 *   <li><b>类路径资源</b>：以 {@code classpath:} 为前缀，例如 {@code classpath:config.xml}</li>
 *   <li><b>URL 资源</b>：标准的 URL，例如 {@code file:/path/to/file}、{@code https://example.com/data}</li>
 *   <li><b>相对路径资源</b>：以 {@code /} 开头的路径，会被视为相对于类路径根目录的资源</li>
 *   <li><b>无前缀路径</b>：尝试作为 URL 解析，失败后作为类路径资源处理</li>
 * </ul>
 *
 * <p>该类还支持可插拔的协议解析器（{@link ProtocolResolver}），允许扩展自定义的资源协议。
 * 同时提供了资源缓存功能，用于缓存特定类型的资源，提高重复访问的性能。
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 * @see #getResource(String)
 * @see ProtocolResolver
 * @see ClassPathResource
 * @see UrlResource
 */
public class DefaultResourceLoader implements ResourceLoader {

	/** 当前使用的类加载器，用于加载类路径资源 */
	@Nullable
	private ClassLoader classLoader;

	/** 可插拔的协议解析器集合，用于解析自定义资源协议（如 vfs:、jar: 等） */
	private final Set<ProtocolResolver> protocolResolvers = new LinkedHashSet<>(4);

	/** 资源缓存映射：值类型 -> 资源到缓存值的映射。用于缓存经过转换或处理后的资源对象 */
	private final Map<Class<?>, Map<Resource, ?>> resourceCaches = new ConcurrentHashMap<>(4);

	/**
	 * 默认构造器，使用默认的类加载器（通常为线程上下文类加载器）。
	 */
	public DefaultResourceLoader() {
	}

	/**
	 * 构造器，指定特定的类加载器。
	 *
	 * @param classLoader 要使用的类加载器，可以为 null（表示使用默认类加载器）
	 */
	public DefaultResourceLoader(@Nullable ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * 设置当前资源加载器使用的类加载器。
	 *
	 * @param classLoader 类加载器（可为 null）
	 */
	public void setClassLoader(@Nullable ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/**
	 * 获取当前使用的类加载器。如果未显式设置，则返回默认的类加载器。
	 *
	 * @return 类加载器（永远不为 null）
	 */
	@Nullable
	@Override
	public ClassLoader getClassLoader() {
		return (this.classLoader != null ? this.classLoader : ClassUtils.getDefaultClassLoader());
	}

	/**
	 * 添加一个协议解析器，用于处理自定义的资源协议。
	 * 协议解析器会按照添加顺序依次尝试解析资源，第一个成功解析的将返回 Resource。
	 *
	 * @param resolver 协议解析器
	 */
	public void addProtocolResolver(ProtocolResolver resolver) {
		Assert.notNull(resolver, "ProtocolResolver must not be null");
		this.protocolResolvers.add(resolver);
	}

	/**
	 * 获取当前注册的所有协议解析器。
	 *
	 * @return 协议解析器集合（不可修改的直接引用，但建议只读遍历）
	 */
	public Collection<ProtocolResolver> getProtocolResolvers() {
		return this.protocolResolvers;
	}

	/**
	 * 获取指定值类型的资源缓存映射。
	 * 如果该类型尚未有缓存，则自动创建一个新的 ConcurrentHashMap。
	 * 缓存可用于存储将 Resource 转换为特定类型对象的中间结果（例如 Resource 对应的 File 或 InputStream 等）。
	 *
	 * @param valueType 缓存值的类型
	 * @param <T>       缓存值的泛型类型
	 * @return 资源到缓存值的映射
	 */
	@SuppressWarnings("unchecked")
	public <T> Map<Resource, T> getResourceCache(Class<T> valueType) {
		return (Map<Resource, T>) this.resourceCaches.computeIfAbsent(valueType, key -> new ConcurrentHashMap<>());
	}

	/**
	 * 清除所有资源缓存。
	 */
	public void clearResourceCaches() {
		this.resourceCaches.clear();
	}

	/**
	 * 核心方法：根据给定的位置字符串解析为 Resource 对象。
	 * 解析策略按优先级从高到低：
	 * <ol>
	 *   <li>使用所有注册的 {@link ProtocolResolver} 尝试解析</li>
	 *   <li>如果位置以 "/" 开头，作为类路径下的相对路径处理（相对于类路径根）</li>
	 *   <li>如果位置以 "classpath:" 开头，解析为 {@link ClassPathResource}</li>
	 *   <li>尝试将位置当作 URL 解析，如果是 file: 协议则返回 {@link FileUrlResource}，否则返回 {@link UrlResource}</li>
	 *   <li>如果 URL 解析失败，回退到类路径资源（作为路径处理）</li>
	 * </ol>
	 *
	 * @param location 资源路径（不能为 null）
	 * @return Resource 实例
	 * @throws IllegalArgumentException 如果 location 为 null
	 */
	@Override
	public Resource getResource(String location) {
		Assert.notNull(location, "Location must not be null");

		// 1. 使用自定义协议解析器尝试解析
		for (ProtocolResolver protocolResolver : getProtocolResolvers()) {
			Resource resource = protocolResolver.resolve(location, this);
			if (resource != null) {
				return resource;
			}
		}

		// 2. 以 "/" 开头的路径，视为相对于类路径根目录的资源
		if (location.startsWith("/")) {
			return getResourceByPath(location);
		}

		// 3. 以 "classpath:" 为前缀，创建 ClassPathResource
		if (location.startsWith(CLASSPATH_URL_PREFIX)) {
			return new ClassPathResource(location.substring(CLASSPATH_URL_PREFIX.length()), getClassLoader());
		}

		// 4. 尝试作为 URL 解析
		try {
			URL url = new URL(location);
			// 如果是 file: 协议，使用 FileUrlResource（支持文件系统特定操作）
			return (ResourceUtils.isFileURL(url) ? new FileUrlResource(url) : new UrlResource(url));
		} catch (MalformedURLException ex) {
			// 5. URL 解析失败，最后回退到类路径资源（相对路径）
			return getResourceByPath(location);
		}
	}

	/**
	 * 根据相对路径获取类路径资源。
	 * 子类可以覆盖此方法以自定义相对路径资源的行为（例如使用不同的 Resource 实现）。
	 *
	 * @param path 相对路径（通常以 "/" 开头，但此方法不要求）
	 * @return 类路径资源
	 */
	protected Resource getResourceByPath(String path) {
		// 使用内部类 ClassPathContextResource，它实现了 ContextResource 接口，
		// 能够提供相对于上下文根的路径信息。
		return new ClassPathContextResource(path, getClassLoader());
	}

	/**
	 * 内部类：表示一个位于类路径中且可感知上下文路径的资源。
	 * 实现了 ContextResource 接口，可以返回相对于上下文根的路径。
	 * 主要用于支持 ServletContextResource 等需要上下文路径的场景，
	 * 但此处作为类路径资源的基础实现。
	 */
	protected static class ClassPathContextResource extends ClassPathResource implements ContextResource {

		/**
		 * 构造器。
		 *
		 * @param path        类路径下的相对路径
		 * @param classLoader 类加载器
		 */
		public ClassPathContextResource(String path, @Nullable ClassLoader classLoader) {
			super(path, classLoader);
		}

		/**
		 * 返回资源相对于上下文根的路径。
		 * 对于类路径资源，直接返回资源的内部路径（即构造时的 path）。
		 *
		 * @return 路径字符串
		 */
		@Override
		public String getPathWithinContext() {
			return getPath();
		}

		/**
		 * 创建基于当前资源相对路径的新资源。
		 *
		 * @param relativePath 相对路径
		 * @return 新的 ClassPathContextResource 实例
		 */
		@Override
		public Resource createRelative(String relativePath) {
			String pathToUse = StringUtils.applyRelativePath(getPath(), relativePath);
			return new ClassPathContextResource(pathToUse, getClassLoader());
		}
	}
}