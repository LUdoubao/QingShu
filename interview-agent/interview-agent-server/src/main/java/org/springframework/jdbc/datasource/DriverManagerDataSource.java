//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Spring 框架中一个简单的、基于 JDBC {@link DriverManager} 的数据源实现。
 * 该数据源每次调用 {@link #getConnection()} 都会通过 {@link DriverManager}
 * 创建一个新的数据库连接，不提供连接池功能。适用于测试环境或简单的应用场景，
 * 不推荐在生产环境中使用（应使用连接池，如 HikariCP、Tomcat JDBC Pool 等）。
 *
 * <p>通过设置 URL、用户名/密码或连接属性来配置数据源。也可以显式设置驱动类名，
 * 以便在运行时加载 JDBC 驱动（虽然 DriverManager 通常会自动加载，但有些环境中需要手动加载）。
 *
 * <p>继承自 {@link AbstractDriverBasedDataSource}，复用了一些通用属性（url、username、password、connectionProperties）。
 *
 * @author Juergen Hoeller
 * @since 14.03.2003
 * @see DriverManager#getConnection(String, Properties)
 * @see SimpleDriverDataSource
 */
public class DriverManagerDataSource extends AbstractDriverBasedDataSource {

	/**
	 * 无参构造器，用于允许通过 setter 方法（如 setUrl、setUsername）进行配置。
	 */
	public DriverManagerDataSource() {
	}

	/**
	 * 仅指定 URL 的构造器（使用默认的 DriverManager 行为，不提供用户名/密码）。
	 *
	 * @param url 数据库 JDBC URL
	 */
	public DriverManagerDataSource(String url) {
		this.setUrl(url);
	}

	/**
	 * 指定 URL、用户名和密码的构造器。
	 *
	 * @param url      数据库 JDBC URL
	 * @param username 数据库用户名
	 * @param password 数据库密码
	 */
	public DriverManagerDataSource(String url, String username, String password) {
		this.setUrl(url);
		this.setUsername(username);
		this.setPassword(password);
	}

	/**
	 * 指定 URL 和完整连接属性的构造器（Properties 中包含用户名、密码以及其他驱动特定参数）。
	 *
	 * @param url      数据库 JDBC URL
	 * @param conProps 连接属性（如 user、password、useSSL 等）
	 */
	public DriverManagerDataSource(String url, Properties conProps) {
		this.setUrl(url);
		this.setConnectionProperties(conProps);
	}

	/**
	 * 设置 JDBC 驱动类名，并通过当前线程的上下文类加载器加载该驱动类。
	 * 这一步通常不是必需的，因为大多数 JDBC 驱动在 DriverManager 初始化时会自动注册，
	 * 但在某些类加载器环境中（如 Servlet 容器）需要显式加载以确保驱动可用。
	 *
	 * @param driverClassName 完全限定的 JDBC 驱动类名（例如 com.mysql.cj.jdbc.Driver）
	 * @throws IllegalStateException 如果驱动类无法加载（ClassNotFoundException）
	 */
	public void setDriverClassName(String driverClassName) {
		Assert.hasText(driverClassName, "Property 'driverClassName' must not be empty");
		String driverClassNameToUse = driverClassName.trim();

		try {
			// 使用默认的类加载器加载驱动类，触发静态初始化块，从而向 DriverManager 注册驱动
			Class.forName(driverClassNameToUse, true, ClassUtils.getDefaultClassLoader());
		} catch (ClassNotFoundException ex) {
			throw new IllegalStateException("Could not load JDBC driver class [" + driverClassNameToUse + "]", ex);
		}

		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Loaded JDBC driver: " + driverClassNameToUse);
		}
	}

	/**
	 * 使用当前配置的连接属性（URL、用户名、密码等）通过 DriverManager 获取连接。
	 * 此方法根据父类 AbstractDriverBasedDataSource 的模板方法定义，实际生成连接。
	 *
	 * @param props 合并后的连接属性（包含用户名、密码以及其他自定义属性）
	 * @return 新的数据库连接
	 * @throws SQLException 如果 DriverManager 无法获取连接
	 */
	@Override
	protected Connection getConnectionFromDriver(Properties props) throws SQLException {
		String url = this.getUrl();
		Assert.state(url != null, "'url' not set");
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Creating new JDBC DriverManager Connection to [" + url + "]");
		}
		return this.getConnectionFromDriverManager(url, props);
	}

	/**
	 * 实际调用 {@link DriverManager#getConnection(String, Properties)} 获取连接。
	 * 该方法可以被子类重写，以自定义连接获取逻辑（例如添加额外的日志或监控）。
	 *
	 * @param url   数据库 JDBC URL
	 * @param props 连接属性
	 * @return 数据库连接
	 * @throws SQLException 如果连接创建失败
	 */
	protected Connection getConnectionFromDriverManager(String url, Properties props) throws SQLException {
		return DriverManager.getConnection(url, props);
	}
}