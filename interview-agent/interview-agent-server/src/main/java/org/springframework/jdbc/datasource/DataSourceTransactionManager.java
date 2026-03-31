//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.jdbc.datasource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.Nullable;
import org.springframework.transaction.*;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronizationUtils;
import org.springframework.util.Assert;

/**
 * Spring 框架中基于 JDBC 的 {@link DataSource} 事务管理器实现。
 * 该事务管理器绑定一个 JDBC Connection 到当前线程（通过 {@link TransactionSynchronizationManager}），
 * 允许每个数据源有一个线程绑定的 Connection。
 *
 * <p>支持的事务传播行为：由 {@link AbstractPlatformTransactionManager} 提供。
 * 特别地，支持嵌套事务（通过 JDBC 保存点），需要在构造时设置 {@code nestedTransactionAllowed=true}。
 *
 * <p>该事务管理器适用于使用单个 JDBC DataSource 的应用，典型的用法是通过
 * {@link org.springframework.jdbc.core.JdbcTemplate} 或 MyBatis 等框架配合使用。
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see DataSource
 * @see JdbcTransactionObjectSupport
 * @see TransactionSynchronizationManager
 */
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager
		implements ResourceTransactionManager, InitializingBean {

	/** 目标数据源，用于获取数据库连接 */
	@Nullable
	private DataSource dataSource;

	/**
	 * 是否强制将只读事务设置为数据库只读模式（通过执行 "SET TRANSACTION READ ONLY"）。
	 * 默认为 false，仅通过 JDBC Connection 的只读标志提示驱动，不执行额外 SQL。
	 */
	private boolean enforceReadOnly;

	/**
	 * 无参构造器，默认允许嵌套事务（通过保存点）。
	 */
	public DataSourceTransactionManager() {
		this.enforceReadOnly = false;
		this.setNestedTransactionAllowed(true); // 启用嵌套事务支持
	}

	/**
	 * 指定数据源的构造器。
	 *
	 * @param dataSource 目标数据源
	 */
	public DataSourceTransactionManager(DataSource dataSource) {
		this();
		this.setDataSource(dataSource);
		this.afterPropertiesSet();
	}

	/**
	 * 设置目标数据源。如果传入的是 {@link TransactionAwareDataSourceProxy}，则获取其包裹的原始数据源。
	 *
	 * @param dataSource 数据源，可为 null（但使用时必须设置）
	 */
	public void setDataSource(@Nullable DataSource dataSource) {
		if (dataSource instanceof TransactionAwareDataSourceProxy) {
			// 剥去事务感知代理，获取实际的目标数据源
			this.dataSource = ((TransactionAwareDataSourceProxy) dataSource).getTargetDataSource();
		} else {
			this.dataSource = dataSource;
		}
	}

	/**
	 * 获取当前配置的数据源。
	 *
	 * @return 数据源，可能为 null
	 */
	@Nullable
	public DataSource getDataSource() {
		return this.dataSource;
	}

	/**
	 * 获取数据源，确保非空。
	 *
	 * @return 非空的数据源
	 * @throws IllegalStateException 如果 dataSource 为 null
	 */
	protected DataSource obtainDataSource() {
		DataSource dataSource = this.getDataSource();
		Assert.state(dataSource != null, "No DataSource set");
		return dataSource;
	}

	/**
	 * 设置是否强制只读事务使用数据库级别的只读模式。
	 * 如果设置为 true，则对于只读事务会执行 "SET TRANSACTION READ ONLY" 语句。
	 *
	 * @param enforceReadOnly true 表示强制执行
	 */
	public void setEnforceReadOnly(boolean enforceReadOnly) {
		this.enforceReadOnly = enforceReadOnly;
	}

	/**
	 * 返回是否强制只读事务使用数据库只读模式。
	 */
	public boolean isEnforceReadOnly() {
		return this.enforceReadOnly;
	}

	/**
	 * Bean 初始化时检查数据源是否已设置。
	 *
	 * @throws IllegalArgumentException 如果 dataSource 为 null
	 */
	@Override
	public void afterPropertiesSet() {
		if (this.getDataSource() == null) {
			throw new IllegalArgumentException("Property 'dataSource' is required");
		}
	}

	/**
	 * 返回该事务管理器所管理的资源工厂，即数据源。
	 * 用于 {@link ResourceTransactionManager} 接口，以便通过 {@link TransactionSynchronizationManager}
	 * 绑定/解绑资源。
	 *
	 * @return 数据源实例
	 */
	@Override
	public Object getResourceFactory() {
		return this.obtainDataSource();
	}

	/**
	 * 获取当前事务对象，包装了可能存在的 ConnectionHolder。
	 *
	 * @return 事务对象（DataSourceTransactionObject 实例）
	 */
	@Override
	protected Object doGetTransaction() {
		DataSourceTransactionObject txObject = new DataSourceTransactionObject();
		// 设置是否允许保存点（用于嵌套事务）
		txObject.setSavepointAllowed(isNestedTransactionAllowed());

		// 从当前线程获取与数据源绑定的 ConnectionHolder
		ConnectionHolder conHolder = (ConnectionHolder) TransactionSynchronizationManager.getResource(obtainDataSource());
		txObject.setConnectionHolder(conHolder, false);
		return txObject;
	}

	/**
	 * 判断是否存在活动的事务。
	 *
	 * @param transaction 事务对象（DataSourceTransactionObject）
	 * @return true 如果存在 ConnectionHolder 且其中的事务处于活动状态
	 */
	@Override
	protected boolean isExistingTransaction(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		return (txObject.hasConnectionHolder() && txObject.getConnectionHolder().isTransactionActive());
	}

	/**
	 * 开始一个新事务：获取连接、设置隔离级别、只读标志、关闭自动提交等。
	 *
	 * @param transaction 事务对象
	 * @param definition  事务定义（隔离级别、超时、只读等）
	 * @throws CannotCreateTransactionException 如果不能创建事务
	 */
	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		Connection con = null;

		try {
			// 如果没有 ConnectionHolder，或者现有 Holder 尚未与事务同步，则获取新连接
			if (!txObject.hasConnectionHolder() || txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
				Connection newCon = obtainDataSource().getConnection();
				if (logger.isDebugEnabled()) {
					logger.debug("Acquired Connection [" + newCon + "] for JDBC transaction");
				}
				txObject.setConnectionHolder(new ConnectionHolder(newCon), true);
			}

			// 标记 ConnectionHolder 与事务同步
			txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
			con = txObject.getConnectionHolder().getConnection();

			// 准备连接：设置隔离级别、只读模式（如果驱动支持）
			Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
			txObject.setPreviousIsolationLevel(previousIsolationLevel);
			txObject.setReadOnly(definition.isReadOnly());

			// 如果连接当前是自动提交模式，则关闭自动提交，并标记需要恢复
			if (con.getAutoCommit()) {
				txObject.setMustRestoreAutoCommit(true);
				if (logger.isDebugEnabled()) {
					logger.debug("Switching JDBC Connection [" + con + "] to manual commit");
				}
				con.setAutoCommit(false);
			}

			// 子类扩展点：对连接进行额外准备（例如设置只读事务）
			prepareTransactionalConnection(con, definition);

			// 标记事务活动
			txObject.getConnectionHolder().setTransactionActive(true);

			// 设置超时时间
			int timeout = determineTimeout(definition);
			if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
				txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
			}

			// 如果这是一个新的 ConnectionHolder，将其绑定到当前线程
			if (txObject.isNewConnectionHolder()) {
				TransactionSynchronizationManager.bindResource(obtainDataSource(), txObject.getConnectionHolder());
			}
		} catch (Throwable ex) {
			// 异常处理：如果是新连接，释放并清除 holder
			if (txObject.isNewConnectionHolder()) {
				DataSourceUtils.releaseConnection(con, obtainDataSource());
				txObject.setConnectionHolder(null, false);
			}
			throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", ex);
		}
	}

	/**
	 * 挂起当前事务：解绑数据源与 ConnectionHolder 的绑定关系。
	 *
	 * @param transaction 当前事务对象
	 * @return 挂起的资源（即被解绑的 ConnectionHolder）
	 */
	@Override
	protected Object doSuspend(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
		// 清除事务对象中的 ConnectionHolder 引用
		txObject.setConnectionHolder(null);
		// 从线程解绑资源并返回
		return TransactionSynchronizationManager.unbindResource(obtainDataSource());
	}

	/**
	 * 恢复挂起的事务：重新绑定之前挂起的 ConnectionHolder 到当前线程。
	 *
	 * @param transaction       当前事务对象（可能为 null）
	 * @param suspendedResources 挂起时返回的资源
	 */
	@Override
	protected void doResume(@Nullable Object transaction, Object suspendedResources) {
		TransactionSynchronizationManager.bindResource(obtainDataSource(), suspendedResources);
	}

	/**
	 * 提交事务：执行 Connection.commit()。
	 *
	 * @param status 事务状态
	 * @throws TransactionSystemException 如果提交失败
	 */
	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Committing JDBC transaction on Connection [" + con + "]");
		}
		try {
			con.commit();
		} catch (SQLException ex) {
			throw translateException("JDBC commit", ex);
		}
	}

	/**
	 * 回滚事务：执行 Connection.rollback()。
	 *
	 * @param status 事务状态
	 * @throws TransactionSystemException 如果回滚失败
	 */
	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		Connection con = txObject.getConnectionHolder().getConnection();
		if (status.isDebug()) {
			logger.debug("Rolling back JDBC transaction on Connection [" + con + "]");
		}
		try {
			con.rollback();
		} catch (SQLException ex) {
			throw translateException("JDBC rollback", ex);
		}
	}

	/**
	 * 将现有事务标记为回滚仅：设置 ConnectionHolder 的回滚标志。
	 *
	 * @param status 事务状态
	 */
	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
		if (status.isDebug()) {
			logger.debug("Setting JDBC transaction [" + txObject.getConnectionHolder().getConnection() + "] rollback-only");
		}
		txObject.setRollbackOnly();
	}

	/**
	 * 事务完成后的清理工作：解绑新绑定的资源、恢复自动提交模式、重置隔离级别、释放连接等。
	 *
	 * @param transaction 事务对象
	 */
	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;

		// 如果是新绑定的 ConnectionHolder，从线程解绑
		if (txObject.isNewConnectionHolder()) {
			TransactionSynchronizationManager.unbindResource(obtainDataSource());
		}

		Connection con = txObject.getConnectionHolder().getConnection();
		try {
			// 恢复自动提交模式（如果之前被改为手动提交）
			if (txObject.isMustRestoreAutoCommit()) {
				con.setAutoCommit(true);
			}
			// 重置连接状态：恢复隔离级别、只读标志等
			DataSourceUtils.resetConnectionAfterTransaction(con, txObject.getPreviousIsolationLevel(), txObject.isReadOnly());
		} catch (Throwable ex) {
			logger.debug("Could not reset JDBC Connection after transaction", ex);
		}

		// 如果是新绑定的连接，释放它（归还连接池）
		if (txObject.isNewConnectionHolder()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Releasing JDBC Connection [" + con + "] after transaction");
			}
			DataSourceUtils.releaseConnection(con, this.dataSource);
		}

		// 清理 ConnectionHolder 的状态
		txObject.getConnectionHolder().clear();
	}

	/**
	 * 准备事务连接：如果启用了 enforceReadOnly 且当前事务为只读，则执行 "SET TRANSACTION READ ONLY"。
	 * 注意：并非所有数据库都支持该语句，使用时需注意兼容性。
	 *
	 * @param con        数据库连接
	 * @param definition 事务定义
	 * @throws SQLException 如果执行 SQL 失败
	 */
	protected void prepareTransactionalConnection(Connection con, TransactionDefinition definition) throws SQLException {
		if (isEnforceReadOnly() && definition.isReadOnly()) {
			// 使用 Statement 执行只读事务设置
			Statement stmt = con.createStatement();
			try {
				stmt.executeUpdate("SET TRANSACTION READ ONLY");
			} finally {
				stmt.close();
			}
		}
	}

	/**
	 * 将 SQLException 转换为 Spring 的事务异常。
	 *
	 * @param task 任务描述（如 "JDBC commit"）
	 * @param ex   SQLException
	 * @return TransactionSystemException 实例
	 */
	protected RuntimeException translateException(String task, SQLException ex) {
		return new TransactionSystemException(task + " failed", ex);
	}

	@Override
	public void rollback(TransactionStatus status) throws TransactionException {

	}

	/**
	 * 内部类：扩展自 {@link JdbcTransactionObjectSupport}，用于保存当前事务的 JDBC 连接状态。
	 * 增加了是否为新连接、是否需要恢复自动提交、只读标志等属性。
	 */
	private static class DataSourceTransactionObject extends JdbcTransactionObjectSupport {
		/** 是否是新创建的 ConnectionHolder（即由当前事务绑定到线程） */
		private boolean newConnectionHolder;
		/** 是否需要恢复自动提交模式（即之前将自动提交改为手动提交） */
		private boolean mustRestoreAutoCommit;

		private DataSourceTransactionObject() {
		}

		/**
		 * 设置 ConnectionHolder，并标记是否是新绑定的。
		 *
		 * @param connectionHolder   ConnectionHolder 实例（可为 null）
		 * @param newConnectionHolder 是否是新绑定的
		 */
		public void setConnectionHolder(@Nullable ConnectionHolder connectionHolder, boolean newConnectionHolder) {
			super.setConnectionHolder(connectionHolder);
			this.newConnectionHolder = newConnectionHolder;
		}

		/**
		 * 返回是否为新绑定的 ConnectionHolder。
		 */
		public boolean isNewConnectionHolder() {
			return this.newConnectionHolder;
		}

		/**
		 * 设置是否需要恢复自动提交。
		 */
		public void setMustRestoreAutoCommit(boolean mustRestoreAutoCommit) {
			this.mustRestoreAutoCommit = mustRestoreAutoCommit;
		}

		/**
		 * 返回是否需要恢复自动提交。
		 */
		public boolean isMustRestoreAutoCommit() {
			return this.mustRestoreAutoCommit;
		}

		/**
		 * 标记当前事务为回滚仅（设置 ConnectionHolder 的回滚标志）。
		 */
		public void setRollbackOnly() {
			this.getConnectionHolder().setRollbackOnly();
		}

		/**
		 * 判断当前事务是否被标记为回滚仅。
		 */
		@Override
		public boolean isRollbackOnly() {
			return this.getConnectionHolder().isRollbackOnly();
		}

		/**
		 * 刷新会话：如果当前存在同步，触发 flush 回调。
		 */
		@Override
		public void flush() {
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationUtils.triggerFlush();
			}
		}
	}
}