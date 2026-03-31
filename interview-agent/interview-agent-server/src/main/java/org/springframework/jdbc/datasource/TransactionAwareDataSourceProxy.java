//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.jdbc.datasource;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务感知的数据源代理。
 * 该代理包装了一个目标 {@link DataSource}，并对其返回的 {@link Connection} 进行增强，
 * 使其能够感知 Spring 的事务同步管理器（TransactionSynchronizationManager）。
 * 主要功能：
 * <ul>
 *   <li>当处于 Spring 管理的事务中时，返回与当前事务绑定的连接（同一个连接），
 *       避免每次获取连接都新建，从而保证事务内操作使用同一连接。</li>
 *   <li>对 {@link Connection#close()} 方法的调用不会真正关闭物理连接，而是将其释放回事务同步管理器，
 *       由事务管理器在事务完成时统一关闭。</li>
 *   <li>对 {@link Statement} 对象设置事务超时时间（通过 {@link DataSourceUtils#applyTransactionTimeout}）。</li>
 * </ul>
 *
 * <p>适用于需要将非事务性数据源（如简单的 DriverManagerDataSource）与 Spring 事务管理集成，
 * 或者希望现有代码无感知地使用 Spring 事务管理的场景。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see DataSourceUtils#doGetConnection
 * @see DataSourceUtils#doReleaseConnection
 * @see TransactionSynchronizationManager
 */
public class TransactionAwareDataSourceProxy extends DelegatingDataSource {

	/**
	 * 是否在每次获取连接时都重新获取事务绑定的连接。
	 * 默认为 false：如果当前存在事务同步，则固定使用第一次获取的事务连接（同一个连接）；
	 * 设置为 true 时，每次调用 getConnection 都会重新获取当前事务绑定的连接，
	 * 适用于事务内需要切换连接的特殊场景（例如分布式事务中的重连）。
	 */
	private boolean reobtainTransactionalConnections = false;

	/**
	 * 无参构造器，用于允许通过 setter 方法注入目标 DataSource。
	 */
	public TransactionAwareDataSourceProxy() {
	}

	/**
	 * 使用指定的目标 DataSource 创建代理。
	 *
	 * @param targetDataSource 目标数据源
	 */
	public TransactionAwareDataSourceProxy(DataSource targetDataSource) {
		super(targetDataSource);
	}

	/**
	 * 设置是否重新获取事务绑定的连接。
	 *
	 * @param reobtainTransactionalConnections true 表示每次 getConnection 都重新获取
	 */
	public void setReobtainTransactionalConnections(boolean reobtainTransactionalConnections) {
		this.reobtainTransactionalConnections = reobtainTransactionalConnections;
	}

	/**
	 * 获取一个事务感知的数据库连接代理。
	 * 如果当前存在活动的事务同步，则返回与事务绑定的连接（同一个物理连接）；
	 * 否则，返回一个普通的连接（但依然会被代理，以便 close 方法等被增强）。
	 *
	 * @return 连接代理对象（实现了 {@link ConnectionProxy} 接口）
	 * @throws SQLException 如果无法获取连接
	 */
	@Override
	public Connection getConnection() throws SQLException {
		// 获取目标数据源（可能是原始数据源，也可能是重新包装的）
		DataSource targetDataSource = this.obtainTargetDataSource();
		return getTransactionAwareConnectionProxy(targetDataSource);
	}

	/**
	 * 为目标数据源创建事务感知的连接代理。
	 *
	 * @param targetDataSource 目标数据源
	 * @return 连接代理
	 */
	protected Connection getTransactionAwareConnectionProxy(DataSource targetDataSource) {
		// 创建动态代理，实现 ConnectionProxy 接口（间接继承 Connection）
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class<?>[]{ConnectionProxy.class},
				new TransactionAwareInvocationHandler(targetDataSource));
	}

	/**
	 * 判断是否应该固定使用同一个连接（即不重新获取）。
	 * 当不存在事务同步时，返回 true（因为没有事务上下文，无需重新获取）；
	 * 当存在事务同步且 reobtainTransactionalConnections = false 时，也返回 true（固定使用第一次获取的连接）；
	 * 只有当存在事务同步且 reobtainTransactionalConnections = true 时，返回 false（每次重新获取）。
	 *
	 * @param targetDataSource 目标数据源（此处未使用，但保留以便子类扩展）
	 * @return true 表示应固定使用同一个连接，false 表示应每次都重新获取当前事务绑定的连接
	 */
	protected boolean shouldObtainFixedConnection(DataSource targetDataSource) {
		return !TransactionSynchronizationManager.isSynchronizationActive() || !this.reobtainTransactionalConnections;
	}

	/**
	 * 连接代理的 InvocationHandler 实现。
	 * 负责拦截对 Connection 接口方法的调用，并根据事务上下文调整行为。
	 */
	private class TransactionAwareInvocationHandler implements InvocationHandler {

		/** 目标数据源（用于获取真正的连接） */
		private final DataSource targetDataSource;

		/** 当前代理持有的固定连接（当 shouldObtainFixedConnection 为 true 时使用） */
		@Nullable
		private Connection target;

		/** 连接是否已被关闭（代理标记） */
		private boolean closed = false;

		/**
		 * 构造器。
		 *
		 * @param targetDataSource 目标数据源
		 */
		public TransactionAwareInvocationHandler(DataSource targetDataSource) {
			this.targetDataSource = targetDataSource;
		}

		/**
		 * 拦截代理连接的所有方法调用。
		 *
		 * @param proxy  代理对象本身
		 * @param method 被调用的方法
		 * @param args   方法参数
		 * @return 方法执行结果
		 * @throws Throwable 可能抛出的异常
		 */
		@Nullable
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// 处理方法名以进行特殊逻辑
			switch (method.getName()) {
				case "equals":
					// 对于 equals 方法，比较代理对象本身
					return proxy == args[0];
				case "hashCode":
					// 对于 hashCode 方法，返回代理对象的标识哈希码
					return System.identityHashCode(proxy);
				case "toString":
					// toString 方法：返回包含目标连接或数据源信息的字符串
					StringBuilder sb = new StringBuilder("Transaction-aware proxy for target Connection ");
					if (this.target != null) {
						sb.append('[').append(this.target.toString()).append(']');
					} else {
						sb.append(" from DataSource [").append(this.targetDataSource).append(']');
					}
					return sb.toString();
				case "close":
					// close 方法：不是真正关闭物理连接，而是释放回事务同步管理器
					DataSourceUtils.doReleaseConnection(this.target, this.targetDataSource);
					this.closed = true;
					return null;
				case "isClosed":
					// 返回代理标记的关闭状态
					return this.closed;
				case "unwrap":
					// 如果请求的接口是代理对象能实现的，返回代理自身
					if (((Class<?>) args[0]).isInstance(proxy)) {
						return proxy;
					}
					break;
				case "isWrapperFor":
					if (((Class<?>) args[0]).isInstance(proxy)) {
						return true;
					}
					break;
				// 其他方法继续往下处理
			}

			// 以下为常规方法拦截

			// 如果 target 连接尚未初始化
			if (this.target == null) {
				// 对于 getWarnings 和 clearWarnings 方法，如果还没有实际连接，直接返回 null
				if (method.getName().equals("getWarnings") || method.getName().equals("clearWarnings")) {
					return null;
				}
				// 如果代理已经被标记关闭，抛出异常
				if (this.closed) {
					throw new SQLException("Connection handle already closed");
				}
				// 根据配置决定是否固定使用同一个连接
				if (TransactionAwareDataSourceProxy.this.shouldObtainFixedConnection(this.targetDataSource)) {
					// 固定模式：获取一个连接并缓存
					this.target = DataSourceUtils.doGetConnection(this.targetDataSource);
				}
			}

			// 确定本次调用的实际目标连接
			Connection actualTarget = this.target;
			if (actualTarget == null) {
				// 非固定模式：每次都重新获取当前事务绑定的连接
				actualTarget = DataSourceUtils.doGetConnection(this.targetDataSource);
			}

			// 特殊处理：如果调用了 getTargetConnection 方法（ConnectionProxy 接口定义），返回真正的物理连接
			if (method.getName().equals("getTargetConnection")) {
				return actualTarget;
			}

			try {
				// 在目标连接上执行方法
				Object retVal = method.invoke(actualTarget, args);
				// 如果返回的是 Statement 对象，为其设置事务超时时间（如果存在事务）
				if (retVal instanceof Statement) {
					DataSourceUtils.applyTransactionTimeout((Statement) retVal, this.targetDataSource);
				}
				return retVal;
			} catch (InvocationTargetException ex) {
				// 反射调用抛出的目标异常，直接抛出原始异常
				throw ex.getTargetException();
			} finally {
				// 如果本次使用的是临时获取的连接（非固定缓存的那个），则立即释放（因为不会缓存）
				if (actualTarget != this.target) {
					DataSourceUtils.doReleaseConnection(actualTarget, this.targetDataSource);
				}
			}
		}
	}
}