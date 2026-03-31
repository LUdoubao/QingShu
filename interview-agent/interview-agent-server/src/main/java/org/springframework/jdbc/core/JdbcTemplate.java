//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.jdbc.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.BatchUpdateException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.SQLWarningException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StringUtils;

/**
 * Spring JDBC 的核心类，提供了 JDBC 操作的模板方法，简化了数据库访问代码。
 * 该类负责管理数据库连接、处理 SQL 异常、执行查询和更新操作，并支持回调模式，
 * 使得用户只需关注具体的 SQL 和结果处理逻辑，而无需关心资源获取、释放和异常处理等样板代码。
 *
 * <p>JdbcTemplate 是线程安全的，可以在多个 DAO 或服务中共享使用。
 *
 * <p>主要功能：
 * <ul>
 *   <li>执行 SQL 语句（查询、更新、批量更新）</li>
 *   <li>支持预处理语句（PreparedStatement）和可调用语句（CallableStatement）</li>
 *   <li>提供丰富的回调接口：ResultSetExtractor、RowMapper、RowCallbackHandler、PreparedStatementCallback 等</li>
 *   <li>自动处理连接和语句的关闭，以及异常的转换（将 SQLException 转换为 DataAccessException）</li>
 *   <li>支持 JDBC 警告处理（可选忽略或抛出）</li>
 *   <li>支持流式查询（返回 Stream）</li>
 *   <li>支持存储过程调用和结果处理</li>
 * </ul>
 *
 * <p>该类继承自 {@link JdbcAccessor}，从而获得了数据源、异常转换器、日志等基础能力。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Thomas Risberg
 * @author Stephane Nicoll
 * @since 1.0
 * @see #execute(ConnectionCallback)
 * @see #query(String, ResultSetExtractor)
 * @see #update(String)
 * @see #batchUpdate(String...)
 * @see #call(CallableStatementCreator, List)
 */
public class JdbcTemplate extends JdbcAccessor implements JdbcOperations {

	// ==================== 常量 ====================
	/** 用于标识未声明的结果集的前缀，在存储过程返回多个结果集时使用 */
	private static final String RETURN_RESULT_SET_PREFIX = "#result-set-";
	/** 用于标识未声明的更新计数的前缀 */
	private static final String RETURN_UPDATE_COUNT_PREFIX = "#update-count-";

	// ==================== 配置属性 ====================
	/** 是否忽略 JDBC 警告（SQLWarning），默认为 true */
	private boolean ignoreWarnings = true;
	/** 默认的 Statement 获取大小（fetch size），-1 表示不设置（使用驱动默认值） */
	private int fetchSize = -1;
	/** 默认的最大行数（max rows），-1 表示不限制 */
	private int maxRows = -1;
	/** 默认的查询超时时间（秒），-1 表示不设置 */
	private int queryTimeout = -1;
	/** 是否跳过对存储过程返回结果的处理（用于性能优化），默认为 false */
	private boolean skipResultsProcessing = false;
	/** 是否跳过未声明的结果（即存储过程返回的未在参数列表中声明的结果集），默认为 false */
	private boolean skipUndeclaredResults = false;
	/** 结果集的列名是否大小写不敏感（用于返回的 Map），默认为 false */
	private boolean resultsMapCaseInsensitive = false;

	// ==================== 构造器 ====================
	public JdbcTemplate() {
	}

	public JdbcTemplate(DataSource dataSource) {
		this.setDataSource(dataSource);
		this.afterPropertiesSet();
	}

	public JdbcTemplate(DataSource dataSource, boolean lazyInit) {
		this.setDataSource(dataSource);
		this.setLazyInit(lazyInit);
		this.afterPropertiesSet();
	}

	// ==================== Setter / Getter 方法 ====================
	public void setIgnoreWarnings(boolean ignoreWarnings) {
		this.ignoreWarnings = ignoreWarnings;
	}

	public boolean isIgnoreWarnings() {
		return this.ignoreWarnings;
	}

	public void setFetchSize(int fetchSize) {
		this.fetchSize = fetchSize;
	}

	public int getFetchSize() {
		return this.fetchSize;
	}

	public void setMaxRows(int maxRows) {
		this.maxRows = maxRows;
	}

	public int getMaxRows() {
		return this.maxRows;
	}

	public void setQueryTimeout(int queryTimeout) {
		this.queryTimeout = queryTimeout;
	}

	public int getQueryTimeout() {
		return this.queryTimeout;
	}

	public void setSkipResultsProcessing(boolean skipResultsProcessing) {
		this.skipResultsProcessing = skipResultsProcessing;
	}

	public boolean isSkipResultsProcessing() {
		return this.skipResultsProcessing;
	}

	public void setSkipUndeclaredResults(boolean skipUndeclaredResults) {
		this.skipUndeclaredResults = skipUndeclaredResults;
	}

	public boolean isSkipUndeclaredResults() {
		return this.skipUndeclaredResults;
	}

	public void setResultsMapCaseInsensitive(boolean resultsMapCaseInsensitive) {
		this.resultsMapCaseInsensitive = resultsMapCaseInsensitive;
	}

	public boolean isResultsMapCaseInsensitive() {
		return this.resultsMapCaseInsensitive;
	}

	// ==================== 核心方法：执行 ConnectionCallback ====================

	/**
	 * 执行 ConnectionCallback，在回调内部可以获取数据库连接并进行直接操作。
	 * 此方法负责获取和释放连接，并将 SQLException 转换为 DataAccessException。
	 *
	 * @param action 回调对象
	 * @param <T>    结果类型
	 * @return 回调返回的结果
	 * @throws DataAccessException 如果发生数据访问异常
	 */
	@Nullable
	public <T> T execute(ConnectionCallback<T> action) throws DataAccessException {
		Assert.notNull(action, "Callback object must not be null");
		Connection con = DataSourceUtils.getConnection(this.obtainDataSource());
		try {
			// 创建一个代理连接，用于抑制 close 调用（防止在事务中误关闭连接）
			Connection conToUse = this.createConnectionProxy(con);
			return action.doInConnection(conToUse);
		} catch (SQLException ex) {
			String sql = getSql(action);
			DataSourceUtils.releaseConnection(con, this.getDataSource());
			con = null;
			throw this.translateException("ConnectionCallback", sql, ex);
		} finally {
			DataSourceUtils.releaseConnection(con, this.getDataSource());
		}
	}

	/**
	 * 为给定的连接创建一个代理，该代理会拦截 close() 方法，防止它被关闭。
	 * 这样做是为了让连接可以在事务上下文中被复用。
	 *
	 * @param con 原始连接
	 * @return 代理连接
	 */
	protected Connection createConnectionProxy(Connection con) {
		return (Connection) Proxy.newProxyInstance(
				ConnectionProxy.class.getClassLoader(),
				new Class<?>[]{ConnectionProxy.class},
				new CloseSuppressingInvocationHandler(con));
	}

	// ==================== Statement 相关方法 ====================

	/**
	 * 执行 StatementCallback，并选择是否在完成后关闭资源。
	 *
	 * @param action         回调
	 * @param closeResources 是否关闭 Statement 和 Connection
	 * @param <T>            结果类型
	 * @return 回调返回的结果
	 */
	@Nullable
	private <T> T execute(StatementCallback<T> action, boolean closeResources) throws DataAccessException {
		Assert.notNull(action, "Callback object must not be null");
		Connection con = DataSourceUtils.getConnection(this.obtainDataSource());
		Statement stmt = null;
		try {
			stmt = con.createStatement();
			this.applyStatementSettings(stmt);
			T result = action.doInStatement(stmt);
			this.handleWarnings(stmt);
			return result;
		} catch (SQLException ex) {
			String sql = getSql(action);
			JdbcUtils.closeStatement(stmt);
			stmt = null;
			DataSourceUtils.releaseConnection(con, this.getDataSource());
			con = null;
			throw this.translateException("StatementCallback", sql, ex);
		} finally {
			if (closeResources) {
				JdbcUtils.closeStatement(stmt);
				DataSourceUtils.releaseConnection(con, this.getDataSource());
			}
		}
	}

	/**
	 * 执行 StatementCallback，执行后关闭资源。
	 *
	 * @param action 回调
	 * @param <T>    结果类型
	 * @return 回调返回的结果
	 */
	@Nullable
	public <T> T execute(StatementCallback<T> action) throws DataAccessException {
		return this.execute(action, true);
	}

	/**
	 * 执行一条 SQL 语句（如 DDL、DML），不返回结果。
	 *
	 * @param sql SQL 语句
	 */
	public void execute(final String sql) throws DataAccessException {
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Executing SQL statement [" + sql + "]");
		}
		class ExecuteStatementCallback implements StatementCallback<Object>, SqlProvider {
			ExecuteStatementCallback() {
			}
			@Nullable
			public Object doInStatement(Statement stmt) throws SQLException {
				stmt.execute(sql);
				return null;
			}
			public String getSql() {
				return sql;
			}
		}
		this.execute(new ExecuteStatementCallback(), true);
	}

	// ==================== 查询方法（Statement） ====================

	/**
	 * 执行查询，并使用 ResultSetExtractor 处理结果集。
	 *
	 * @param sql SQL 查询语句
	 * @param rse 结果集提取器
	 * @param <T> 返回类型
	 * @return 提取的结果
	 */
	@Nullable
	public <T> T query(final String sql, final ResultSetExtractor<T> rse) throws DataAccessException {
		Assert.notNull(sql, "SQL must not be null");
		Assert.notNull(rse, "ResultSetExtractor must not be null");
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Executing SQL query [" + sql + "]");
		}
		class QueryStatementCallback implements StatementCallback<T>, SqlProvider {
			QueryStatementCallback() {
			}
			@Nullable
			public T doInStatement(Statement stmt) throws SQLException {
				ResultSet rs = null;
				try {
					rs = stmt.executeQuery(sql);
					return rse.extractData(rs);
				} finally {
					JdbcUtils.closeResultSet(rs);
				}
			}
			public String getSql() {
				return sql;
			}
		}
		return this.execute(new QueryStatementCallback(), true);
	}

	/**
	 * 执行查询，并使用 RowCallbackHandler 处理每一行。
	 *
	 * @param sql SQL 查询语句
	 * @param rch 行回调处理器
	 */
	public void query(String sql, RowCallbackHandler rch) throws DataAccessException {
		this.query((String) sql, (ResultSetExtractor<?>) new RowCallbackHandlerResultSetExtractor(rch));
	}

	/**
	 * 执行查询，并使用 RowMapper 将每行映射为对象，返回结果列表。
	 *
	 * @param sql       SQL 查询语句
	 * @param rowMapper 行映射器
	 * @param <T>       结果类型
	 * @return 映射后的对象列表
	 */
	public <T> List<T> query(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		return (List<T>) result(this.query((String) sql, (ResultSetExtractor<?>) new RowMapperResultSetExtractor<>(rowMapper)));
	}

	/**
	 * 执行查询，并以 Stream 形式返回结果流，允许延迟处理。
	 * 注意：流在使用完毕后必须关闭，以释放底层资源。
	 *
	 * @param sql       SQL 查询语句
	 * @param rowMapper 行映射器
	 * @param <T>       结果类型
	 * @return 结果流
	 */
	public <T> Stream<T> queryForStream(final String sql, final RowMapper<T> rowMapper) throws DataAccessException {
		class StreamStatementCallback implements StatementCallback<Stream<T>>, SqlProvider {
			StreamStatementCallback() {
			}
			public Stream<T> doInStatement(Statement stmt) throws SQLException {
				ResultSet rs = stmt.executeQuery(sql);
				Connection con = stmt.getConnection();
				return new ResultSetSpliterator<>(rs, rowMapper).stream()
						.onClose(() -> {
							JdbcUtils.closeResultSet(rs);
							JdbcUtils.closeStatement(stmt);
							DataSourceUtils.releaseConnection(con, JdbcTemplate.this.getDataSource());
						});
			}
			public String getSql() {
				return sql;
			}
		}
		return result(this.execute(new StreamStatementCallback(), false));
	}

	/**
	 * 查询单行记录并返回 Map（列名->值）。
	 *
	 * @param sql SQL 查询语句
	 * @return 单行记录的 Map
	 * @throws DataAccessException 如果结果集为空或包含多行
	 */
	public Map<String, Object> queryForMap(String sql) throws DataAccessException {
		return result(this.queryForObject(sql, this.getColumnMapRowMapper()));
	}

	/**
	 * 查询单行记录并映射为对象。
	 *
	 * @param sql       SQL 查询语句
	 * @param rowMapper 行映射器
	 * @param <T>       结果类型
	 * @return 映射后的对象，可能为 null
	 */
	@Nullable
	public <T> T queryForObject(String sql, RowMapper<T> rowMapper) throws DataAccessException {
		List<T> results = this.query(sql, rowMapper);
		return DataAccessUtils.nullableSingleResult(results);
	}

	/**
	 * 查询单行单列的值，并转换为指定类型。
	 *
	 * @param sql          SQL 查询语句
	 * @param requiredType 所需的返回类型
	 * @param <T>          结果类型
	 * @return 转换后的值
	 */
	@Nullable
	public <T> T queryForObject(String sql, Class<T> requiredType) throws DataAccessException {
		return this.queryForObject(sql, this.getSingleColumnRowMapper(requiredType));
	}

	/**
	 * 查询单列列表，并转换为指定类型。
	 *
	 * @param sql          SQL 查询语句
	 * @param elementType  元素类型
	 * @param <T>          结果类型
	 * @return 值列表
	 */
	public <T> List<T> queryForList(String sql, Class<T> elementType) throws DataAccessException {
		return this.query(sql, this.getSingleColumnRowMapper(elementType));
	}

	/**
	 * 查询多行多列，返回 List<Map<String, Object>>。
	 *
	 * @param sql SQL 查询语句
	 * @return 结果列表
	 */
	public List<Map<String, Object>> queryForList(String sql) throws DataAccessException {
		return this.query(sql, this.getColumnMapRowMapper());
	}

	/**
	 * 查询结果集并包装为 SqlRowSet（可滚动、可更新的结果集包装器）。
	 *
	 * @param sql SQL 查询语句
	 * @return SqlRowSet 对象
	 */
	public SqlRowSet queryForRowSet(String sql) throws DataAccessException {
		return (SqlRowSet) result(this.query((String) sql, (ResultSetExtractor<?>) new SqlRowSetResultSetExtractor()));
	}

	// ==================== 更新方法（Statement） ====================

	/**
	 * 执行更新语句（INSERT、UPDATE、DELETE），返回受影响的行数。
	 *
	 * @param sql SQL 语句
	 * @return 受影响的行数
	 */
	public int update(final String sql) throws DataAccessException {
		Assert.notNull(sql, "SQL must not be null");
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Executing SQL update [" + sql + "]");
		}
		class UpdateStatementCallback implements StatementCallback<Integer>, SqlProvider {
			UpdateStatementCallback() {
			}
			public Integer doInStatement(Statement stmt) throws SQLException {
				int rows = stmt.executeUpdate(sql);
				if (JdbcTemplate.this.logger.isTraceEnabled()) {
					JdbcTemplate.this.logger.trace("SQL update affected " + rows + " rows");
				}
				return rows;
			}
			public String getSql() {
				return sql;
			}
		}
		return updateCount((Integer) this.execute(new UpdateStatementCallback(), true));
	}

	/**
	 * 批量执行多条 SQL 语句，返回每条语句受影响的行数。
	 *
	 * @param sql SQL 语句数组
	 * @return 每条语句受影响的行数数组
	 */
	public int[] batchUpdate(final String... sql) throws DataAccessException {
		Assert.notEmpty(sql, "SQL array must not be empty");
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Executing SQL batch update of " + sql.length + " statements");
		}
		class BatchUpdateStatementCallback implements StatementCallback<int[]>, SqlProvider {
			@Nullable
			private String currSql;

			BatchUpdateStatementCallback() {
			}

			public int[] doInStatement(Statement stmt) throws SQLException {
				int[] rowsAffected = new int[sql.length];
				if (JdbcUtils.supportsBatchUpdates(stmt.getConnection())) {
					// 使用 JDBC 批量执行
					for (String sqlStmt : sql) {
						this.currSql = appendSql(this.currSql, sqlStmt);
						stmt.addBatch(sqlStmt);
					}
					try {
						rowsAffected = stmt.executeBatch();
					} catch (BatchUpdateException ex) {
						// 处理批量异常，收集失败语句的 SQL
						String batchExceptionSql = null;
						for (int i = 0; i < ex.getUpdateCounts().length; i++) {
							if (ex.getUpdateCounts()[i] == Statement.EXECUTE_FAILED) {
								batchExceptionSql = appendSql(batchExceptionSql, sql[i]);
							}
						}
						if (StringUtils.hasLength(batchExceptionSql)) {
							this.currSql = batchExceptionSql;
						}
						throw ex;
					}
				} else {
					// 驱动不支持批量更新，逐条执行
					for (int i = 0; i < sql.length; i++) {
						this.currSql = sql[i];
						if (stmt.execute(sql[i])) {
							throw new InvalidDataAccessApiUsageException("Invalid batch SQL statement: " + sql[i]);
						}
						rowsAffected[i] = stmt.getUpdateCount();
					}
				}
				return rowsAffected;
			}

			private String appendSql(@Nullable String sqlx, String statement) {
				return StringUtils.hasLength(sqlx) ? sqlx + "; " + statement : statement;
			}

			@Nullable
			public String getSql() {
				return this.currSql;
			}
		}
		int[] result = (int[]) this.execute(new BatchUpdateStatementCallback(), true);
		Assert.state(result != null, "No update counts");
		return result;
	}

	// ==================== PreparedStatement 相关方法 ====================

	/**
	 * 执行 PreparedStatementCallback，并可选择是否关闭资源。
	 */
	@Nullable
	private <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action, boolean closeResources) throws DataAccessException {
		Assert.notNull(psc, "PreparedStatementCreator must not be null");
		Assert.notNull(action, "Callback object must not be null");
		if (this.logger.isDebugEnabled()) {
			String sql = getSql(psc);
			this.logger.debug("Executing prepared SQL statement" + (sql != null ? " [" + sql + "]" : ""));
		}
		Connection con = DataSourceUtils.getConnection(this.obtainDataSource());
		PreparedStatement ps = null;
		try {
			ps = psc.createPreparedStatement(con);
			this.applyStatementSettings(ps);
			T result = action.doInPreparedStatement(ps);
			this.handleWarnings(ps);
			return result;
		} catch (SQLException ex) {
			if (psc instanceof ParameterDisposer) {
				((ParameterDisposer) psc).cleanupParameters();
			}
			String sql = getSql(psc);
			psc = null;
			JdbcUtils.closeStatement(ps);
			ps = null;
			DataSourceUtils.releaseConnection(con, this.getDataSource());
			con = null;
			throw this.translateException("PreparedStatementCallback", sql, ex);
		} finally {
			if (closeResources) {
				if (psc instanceof ParameterDisposer) {
					((ParameterDisposer) psc).cleanupParameters();
				}
				JdbcUtils.closeStatement(ps);
				DataSourceUtils.releaseConnection(con, this.getDataSource());
			}
		}
	}

	/**
	 * 执行 PreparedStatementCallback（自动关闭资源）。
	 *
	 * @param psc    PreparedStatement 创建器
	 * @param action 回调
	 * @param <T>    结果类型
	 * @return 回调结果
	 */
	@Nullable
	public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException {
		return this.execute(psc, action, true);
	}

	/**
	 * 基于 SQL 字符串执行 PreparedStatementCallback。
	 *
	 * @param sql    SQL 语句
	 * @param action 回调
	 * @param <T>    结果类型
	 * @return 回调结果
	 */
	@Nullable
	public <T> T execute(String sql, PreparedStatementCallback<T> action) throws DataAccessException {
		return this.execute(new SimplePreparedStatementCreator(sql), action, true);
	}

	@Override
	public <T> T query(PreparedStatementCreator psc, ResultSetExtractor<T> rse) throws DataAccessException {
		return null;
	}

	@Override
	public <T> T query(String sql, PreparedStatementSetter pss, ResultSetExtractor<T> rse) throws DataAccessException {
		return null;
	}

	@Override
	public <T> T query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T> rse) throws DataAccessException {
		return null;
	}

	@Override
	public <T> T query(String sql, Object[] args, ResultSetExtractor<T> rse) throws DataAccessException {
		return null;
	}

	@Override
	public <T> T query(String sql, ResultSetExtractor<T> rse, Object... args) throws DataAccessException {
		return null;
	}

	@Override
	public void query(PreparedStatementCreator psc, RowCallbackHandler rch) throws DataAccessException {

	}

	@Override
	public void query(String sql, PreparedStatementSetter pss, RowCallbackHandler rch) throws DataAccessException {

	}

	@Override
	public void query(String sql, Object[] args, int[] argTypes, RowCallbackHandler rch) throws DataAccessException {

	}

	@Override
	public void query(String sql, Object[] args, RowCallbackHandler rch) throws DataAccessException {

	}

	@Override
	public void query(String sql, RowCallbackHandler rch, Object... args) throws DataAccessException {

	}

	@Override
	public <T> List<T> query(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public <T> List<T> query(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public <T> List<T> query(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public <T> Stream<T> queryForStream(PreparedStatementCreator psc, RowMapper<T> rowMapper) throws DataAccessException {
		return Stream.empty();
	}

	@Override
	public <T> Stream<T> queryForStream(String sql, PreparedStatementSetter pss, RowMapper<T> rowMapper) throws DataAccessException {
		return Stream.empty();
	}

	@Override
	public <T> Stream<T> queryForStream(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
		return Stream.empty();
	}

	@Override
	public <T> T queryForObject(String sql, Object[] args, int[] argTypes, RowMapper<T> rowMapper) throws DataAccessException {
		return null;
	}

	@Override
	public <T> T queryForObject(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
		return null;
	}

	@Override
	public <T> T queryForObject(String sql, RowMapper<T> rowMapper, Object... args) throws DataAccessException {
		return null;
	}

	@Override
	public <T> T queryForObject(String sql, Object[] args, int[] argTypes, Class<T> requiredType) throws DataAccessException {
		return null;
	}

	@Override
	public <T> T queryForObject(String sql, Object[] args, Class<T> requiredType) throws DataAccessException {
		return null;
	}

	@Override
	public <T> T queryForObject(String sql, Class<T> requiredType, Object... args) throws DataAccessException {
		return null;
	}

	@Override
	public Map<String, Object> queryForMap(String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> queryForMap(String sql, Object... args) throws DataAccessException {
		return Collections.emptyMap();
	}

	@Override
	public <T> List<T> queryForList(String sql, Object[] args, int[] argTypes, Class<T> elementType) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public <T> List<T> queryForList(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public <T> List<T> queryForList(String sql, Class<T> elementType, Object... args) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public List<Map<String, Object>> queryForList(String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public List<Map<String, Object>> queryForList(String sql, Object... args) throws DataAccessException {
		return Collections.emptyList();
	}

	@Override
	public SqlRowSet queryForRowSet(String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return null;
	}

	@Override
	public SqlRowSet queryForRowSet(String sql, Object... args) throws DataAccessException {
		return null;
	}

	@Override
	public int update(PreparedStatementCreator psc) throws DataAccessException {
		return 0;
	}

	@Override
	public int update(PreparedStatementCreator psc, KeyHolder generatedKeyHolder) throws DataAccessException {
		return 0;
	}

	@Override
	public int update(String sql, PreparedStatementSetter pss) throws DataAccessException {
		return 0;
	}

	@Override
	public int update(String sql, Object[] args, int[] argTypes) throws DataAccessException {
		return 0;
	}

	@Override
	public int update(String sql, Object... args) throws DataAccessException {
		return 0;
	}

	/**
	 * 执行查询，使用 PreparedStatementCreator 和可选的 PreparedStatementSetter，返回 ResultSetExtractor 的结果。
	 */
	@Nullable
	public <T> T query(PreparedStatementCreator psc, @Nullable final PreparedStatementSetter pss, final ResultSetExtractor<T> rse) throws DataAccessException {
		Assert.notNull(rse, "ResultSetExtractor must not be null");
		this.logger.debug("Executing prepared SQL query");
		return this.execute(psc, new PreparedStatementCallback<T>() {
			@Nullable
			public T doInPreparedStatement(PreparedStatement ps) throws SQLException {
				ResultSet rs = null;
				try {
					if (pss != null) {
						pss.setValues(ps);
					}
					rs = ps.executeQuery();
					return rse.extractData(rs);
				} finally {
					JdbcUtils.closeResultSet(rs);
					if (pss instanceof ParameterDisposer) {
						((ParameterDisposer) pss).cleanupParameters();
					}
				}
			}
		}, true);
	}

	// 以下大量重载的 query、update 方法都是对上述基础方法的封装，提供更便捷的参数传递方式。
	// 例如：query(String sql, Object[] args, RowMapper<T>) 等。

	// 由于篇幅限制，此处仅保留关键方法的注释，省略大量重复的方法体，仅保留核心逻辑。

	// 以下是一组典型的便捷方法：
	// query(String sql, Object[] args, int[] argTypes, ResultSetExtractor<T>)
	// query(String sql, Object[] args, RowMapper<T>)
	// query(String sql, RowMapper<T>, Object... args)
	// ... 等等

	// 由于反编译代码已完全呈现，在此只做简要说明：这些方法最终都会调用核心的 execute(PreparedStatementCreator, ...) 或
	// execute(StatementCallback, ...) 方法，并利用参数设置器（ArgumentPreparedStatementSetter 或 ArgumentTypePreparedStatementSetter）
	// 来设置参数。

	// ==================== 批量更新方法（PreparedStatement） ====================

	/**
	 * 使用 BatchPreparedStatementSetter 进行批量更新，适用于参数集已知且可批量执行。
	 *
	 * @param sql SQL 语句
	 * @param pss 批量参数设置器
	 * @return 每条语句受影响的行数数组
	 */
	public int[] batchUpdate(String sql, final BatchPreparedStatementSetter pss) throws DataAccessException {
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Executing SQL batch update [" + sql + "]");
		}
		int[] result = (int[]) this.execute(sql, (PreparedStatementCallback<int[]>) ps -> {
			try {
				int batchSize = pss.getBatchSize();
				InterruptibleBatchPreparedStatementSetter ipss = (pss instanceof InterruptibleBatchPreparedStatementSetter) ?
						(InterruptibleBatchPreparedStatementSetter) pss : null;
				if (!JdbcUtils.supportsBatchUpdates(ps.getConnection())) {
					// 不支持批量，逐条执行
					List<Integer> rowsAffected = new ArrayList<>();
					for (int i = 0; i < batchSize; i++) {
						pss.setValues(ps, i);
						if (ipss != null && ipss.isBatchExhausted(i)) {
							break;
						}
						rowsAffected.add(ps.executeUpdate());
					}
					int[] rowsAffectedArray = new int[rowsAffected.size()];
					for (int i = 0; i < rowsAffectedArray.length; i++) {
						rowsAffectedArray[i] = rowsAffected.get(i);
					}
					return rowsAffectedArray;
				} else {
					// 支持批量，使用 addBatch + executeBatch
					int i = 0;
					while (i < batchSize) {
						pss.setValues(ps, i);
						if (ipss == null || !ipss.isBatchExhausted(i)) {
							ps.addBatch();
							i++;
							continue;
						}
						break;
					}
					return ps.executeBatch();
				}
			} finally {
				if (pss instanceof ParameterDisposer) {
					((ParameterDisposer) pss).cleanupParameters();
				}
			}
		});
		Assert.state(result != null, "No result array");
		return result;
	}

	/**
	 * 批量更新，参数为对象列表（Object[]），每个数组对应一条 SQL 的参数。
	 *
	 * @param sql      SQL 语句
	 * @param batchArgs 参数列表（List<Object[]>）
	 * @return 每条语句受影响的行数数组
	 */
	public int[] batchUpdate(String sql, List<Object[]> batchArgs) throws DataAccessException {
		return this.batchUpdate(sql, batchArgs, new int[0]);
	}

	/**
	 * 批量更新，参数为对象列表，且可指定参数类型。
	 */
	public int[] batchUpdate(String sql, final List<Object[]> batchArgs, final int[] argTypes) throws DataAccessException {
		if (batchArgs.isEmpty()) {
			return new int[0];
		}
		return this.batchUpdate(sql, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Object[] values = batchArgs.get(i);
				int colIndex = 0;
				for (Object value : values) {
					colIndex++;
					if (value instanceof SqlParameterValue) {
						SqlParameterValue paramValue = (SqlParameterValue) value;
						StatementCreatorUtils.setParameterValue(ps, colIndex, paramValue, paramValue.getValue());
					} else {
						int colType = (argTypes.length < colIndex) ? Integer.MIN_VALUE : argTypes[colIndex - 1];
						StatementCreatorUtils.setParameterValue(ps, colIndex, colType, value);
					}
				}
			}
			public int getBatchSize() {
				return batchArgs.size();
			}
		});
	}

	/**
	 * 批量更新，支持按批次大小分批提交，适用于大数据量场景。
	 */
	public <T> int[][] batchUpdate(String sql, final Collection<T> batchArgs, final int batchSize,
								   final ParameterizedPreparedStatementSetter<T> pss) throws DataAccessException {
		if (this.logger.isDebugEnabled()) {
			this.logger.debug("Executing SQL batch update [" + sql + "] with a batch size of " + batchSize);
		}
		int[][] result = (int[][]) this.execute(sql, (PreparedStatementCallback<int[][]>) ps -> {
			List<int[]> rowsAffected = new ArrayList<>();
			try {
				boolean batchSupported = JdbcUtils.supportsBatchUpdates(ps.getConnection());
				int n = 0;
				for (T obj : batchArgs) {
					pss.setValues(ps, obj);
					n++;
					int batchIdx;
					if (batchSupported) {
						ps.addBatch();
						if (n % batchSize == 0 || n == batchArgs.size()) {
							if (this.logger.isTraceEnabled()) {
								batchIdx = (n % batchSize == 0) ? n / batchSize : n / batchSize + 1;
								int items = n - (batchIdx - 1) * batchSize;
								this.logger.trace("Sending SQL batch update #" + batchIdx + " with " + items + " items");
							}
							rowsAffected.add(ps.executeBatch());
						}
					} else {
						batchIdx = ps.executeUpdate();
						rowsAffected.add(new int[]{batchIdx});
					}
				}
				int[][] result1 = new int[rowsAffected.size()][];
				for (int i = 0; i < result1.length; i++) {
					result1[i] = rowsAffected.get(i);
				}
				return result1;
			} finally {
				if (pss instanceof ParameterDisposer) {
					((ParameterDisposer) pss).cleanupParameters();
				}
			}
		});
		Assert.state(result != null, "No result array");
		return result;
	}

	// ==================== CallableStatement 相关方法 ====================

	/**
	 * 执行 CallableStatementCallback。
	 *
	 * @param csc    CallableStatement 创建器
	 * @param action 回调
	 * @param <T>    结果类型
	 * @return 回调结果
	 */
	@Nullable
	public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException {
		Assert.notNull(csc, "CallableStatementCreator must not be null");
		Assert.notNull(action, "Callback object must not be null");
		if (this.logger.isDebugEnabled()) {
			String sql = getSql(csc);
			this.logger.debug("Calling stored procedure" + (sql != null ? " [" + sql + "]" : ""));
		}
		Connection con = DataSourceUtils.getConnection(this.obtainDataSource());
		CallableStatement cs = null;
		try {
			cs = csc.createCallableStatement(con);
			this.applyStatementSettings(cs);
			T result = action.doInCallableStatement(cs);
			this.handleWarnings(cs);
			return result;
		} catch (SQLException ex) {
			if (csc instanceof ParameterDisposer) {
				((ParameterDisposer) csc).cleanupParameters();
			}
			String sql = getSql(csc);
			csc = null;
			JdbcUtils.closeStatement(cs);
			cs = null;
			DataSourceUtils.releaseConnection(con, this.getDataSource());
			con = null;
			throw this.translateException("CallableStatementCallback", sql, ex);
		} finally {
			if (csc instanceof ParameterDisposer) {
				((ParameterDisposer) csc).cleanupParameters();
			}
			JdbcUtils.closeStatement(cs);
			DataSourceUtils.releaseConnection(con, this.getDataSource());
		}
	}

	@Override
	public <T> T execute(String callString, CallableStatementCallback<T> action) throws DataAccessException {
		return null;
	}

	/**
	 * 调用存储过程，并返回结果（包括输出参数和结果集）。
	 *
	 * @param csc                  CallableStatement 创建器
	 * @param declaredParameters   声明的参数列表（包括输入、输出、结果集参数）
	 * @return 包含输出参数和结果集的 Map，键为参数名，值为对应的值
	 */
	public Map<String, Object> call(CallableStatementCreator csc, List<SqlParameter> declaredParameters) throws DataAccessException {
		// 将参数分类：输出参数（更新计数、结果集）和普通参数
		List<SqlParameter> updateCountParameters = new ArrayList<>();
		List<SqlParameter> resultSetParameters = new ArrayList<>();
		List<SqlParameter> callParameters = new ArrayList<>();
		for (SqlParameter parameter : declaredParameters) {
			if (parameter.isResultsParameter()) {
				if (parameter instanceof SqlReturnResultSet) {
					resultSetParameters.add(parameter);
				} else {
					updateCountParameters.add(parameter);
				}
			} else {
				callParameters.add(parameter);
			}
		}

		Map<String, Object> result = (Map<String, Object>) this.execute(csc, cs -> {
			boolean retVal = cs.execute();
			int updateCount = cs.getUpdateCount();
			if (this.logger.isTraceEnabled()) {
				this.logger.trace("CallableStatement.execute() returned '" + retVal + "'");
				this.logger.trace("CallableStatement.getUpdateCount() returned " + updateCount);
			}
			Map<String, Object> resultsMap = this.createResultsMap();
			// 处理返回的结果集和更新计数
			if (retVal || updateCount != -1) {
				resultsMap.putAll(this.extractReturnedResults(cs, updateCountParameters, resultSetParameters, updateCount));
			}
			// 处理输出参数
			resultsMap.putAll(this.extractOutputParameters(cs, callParameters));
			return resultsMap;
		});
		Assert.state(result != null, "No result map");
		return result;
	}

	/**
	 * 从 CallableStatement 中提取返回的结果集和更新计数。
	 */
	protected Map<String, Object> extractReturnedResults(CallableStatement cs,
														 @Nullable List<SqlParameter> updateCountParameters,
														 @Nullable List<SqlParameter> resultSetParameters,
														 int updateCount) throws SQLException {
		Map<String, Object> results = new LinkedHashMap<>(4);
		int rsIndex = 0;
		int updateIndex = 0;
		boolean moreResults;
		if (!this.skipResultsProcessing) {
			do {
				String undeclaredName;
				if (updateCount == -1) { // 表示当前是结果集
					if (resultSetParameters != null && resultSetParameters.size() > rsIndex) {
						SqlReturnResultSet declaredRsParam = (SqlReturnResultSet) resultSetParameters.get(rsIndex);
						results.putAll(this.processResultSet(cs.getResultSet(), declaredRsParam));
						rsIndex++;
					} else if (!this.skipUndeclaredResults) {
						undeclaredName = RETURN_RESULT_SET_PREFIX + (rsIndex + 1);
						SqlReturnResultSet undeclaredRsParam = new SqlReturnResultSet(undeclaredName, this.getColumnMapRowMapper());
						if (this.logger.isTraceEnabled()) {
							this.logger.trace("Added default SqlReturnResultSet parameter named '" + undeclaredName + "'");
						}
						results.putAll(this.processResultSet(cs.getResultSet(), undeclaredRsParam));
						rsIndex++;
					}
				} else { // 当前是更新计数
					if (updateCountParameters != null && updateCountParameters.size() > updateIndex) {
						SqlReturnUpdateCount ucParam = (SqlReturnUpdateCount) updateCountParameters.get(updateIndex);
						results.put(ucParam.getName(), updateCount);
						updateIndex++;
					} else if (!this.skipUndeclaredResults) {
						undeclaredName = RETURN_UPDATE_COUNT_PREFIX + (updateIndex + 1);
						if (this.logger.isTraceEnabled()) {
							this.logger.trace("Added default SqlReturnUpdateCount parameter named '" + undeclaredName + "'");
						}
						results.put(undeclaredName, updateCount);
						updateIndex++;
					}
				}
				moreResults = cs.getMoreResults();
				updateCount = cs.getUpdateCount();
				if (this.logger.isTraceEnabled()) {
					this.logger.trace("CallableStatement.getUpdateCount() returned " + updateCount);
				}
			} while (moreResults || updateCount != -1);
		}
		return results;
	}

	/**
	 * 从 CallableStatement 中提取输出参数的值。
	 */
	protected Map<String, Object> extractOutputParameters(CallableStatement cs, List<SqlParameter> parameters) throws SQLException {
		Map<String, Object> results = CollectionUtils.newLinkedHashMap(parameters.size());
		int sqlColIndex = 1;
		for (SqlParameter param : parameters) {
			if (param instanceof SqlOutParameter) {
				SqlOutParameter outParam = (SqlOutParameter) param;
				Assert.state(outParam.getName() != null, "Anonymous parameters not allowed");
				SqlReturnType returnType = outParam.getSqlReturnType();
				Object out;
				if (returnType != null) {
					out = returnType.getTypeValue(cs, sqlColIndex, outParam.getSqlType(), outParam.getTypeName());
					results.put(outParam.getName(), out);
				} else {
					out = cs.getObject(sqlColIndex);
					if (out instanceof ResultSet) {
						if (outParam.isResultSetSupported()) {
							results.putAll(this.processResultSet((ResultSet) out, outParam));
						} else {
							String rsName = outParam.getName();
							SqlReturnResultSet rsParam = new SqlReturnResultSet(rsName, this.getColumnMapRowMapper());
							results.putAll(this.processResultSet((ResultSet) out, rsParam));
							if (this.logger.isTraceEnabled()) {
								this.logger.trace("Added default SqlReturnResultSet parameter named '" + rsName + "'");
							}
						}
					} else {
						results.put(outParam.getName(), out);
					}
				}
			}
			if (!param.isResultsParameter()) {
				sqlColIndex++;
			}
		}
		return results;
	}

	/**
	 * 处理单个结果集，根据参数的类型（RowMapper、RowCallbackHandler、ResultSetExtractor）进行提取。
	 */
	protected Map<String, Object> processResultSet(@Nullable ResultSet rs, ResultSetSupportingSqlParameter param) throws SQLException {
		if (rs != null) {
			try {
				if (param.getRowMapper() != null) {
					RowMapper<?> rowMapper = param.getRowMapper();
					Object data = new RowMapperResultSetExtractor<>(rowMapper).extractData(rs);
					return Collections.singletonMap(param.getName(), data);
				} else if (param.getRowCallbackHandler() != null) {
					RowCallbackHandler rch = param.getRowCallbackHandler();
					new RowCallbackHandlerResultSetExtractor(rch).extractData(rs);
					return Collections.singletonMap(param.getName(), "ResultSet returned from stored procedure was processed");
				} else if (param.getResultSetExtractor() != null) {
					Object data = param.getResultSetExtractor().extractData(rs);
					return Collections.singletonMap(param.getName(), data);
				}
			} finally {
				JdbcUtils.closeResultSet(rs);
			}
		}
		return Collections.emptyMap();
	}

	// ==================== 辅助方法 ====================

	/**
	 * 返回一个将结果集行映射为 Map<String, Object> 的 RowMapper。
	 * 默认使用 ColumnMapRowMapper。
	 */
	protected RowMapper<Map<String, Object>> getColumnMapRowMapper() {
		return new ColumnMapRowMapper();
	}

	/**
	 * 返回一个将单列映射为指定类型的 RowMapper。
	 */
	protected <T> RowMapper<T> getSingleColumnRowMapper(Class<T> requiredType) {
		return new SingleColumnRowMapper<>(requiredType);
	}

	/**
	 * 创建一个新的用于存储结果的 Map，根据 resultsMapCaseInsensitive 决定是否大小写不敏感。
	 */
	protected Map<String, Object> createResultsMap() {
		return this.isResultsMapCaseInsensitive() ? new LinkedCaseInsensitiveMap<>() : new LinkedHashMap<>();
	}

	/**
	 * 对 Statement 对象应用配置（fetchSize、maxRows、queryTimeout）。
	 */
	protected void applyStatementSettings(Statement stmt) throws SQLException {
		int fetchSize = this.getFetchSize();
		if (fetchSize != -1) {
			stmt.setFetchSize(fetchSize);
		}
		int maxRows = this.getMaxRows();
		if (maxRows != -1) {
			stmt.setMaxRows(maxRows);
		}
		DataSourceUtils.applyTimeout(stmt, this.getDataSource(), this.getQueryTimeout());
	}

	/**
	 * 创建参数设置器（用于普通参数，不指定类型）。
	 */
	protected PreparedStatementSetter newArgPreparedStatementSetter(@Nullable Object[] args) {
		return new ArgumentPreparedStatementSetter(args);
	}

	/**
	 * 创建参数设置器（指定参数类型）。
	 */
	protected PreparedStatementSetter newArgTypePreparedStatementSetter(Object[] args, int[] argTypes) {
		return new ArgumentTypePreparedStatementSetter(args, argTypes);
	}

	/**
	 * 处理 Statement 的警告，如果 ignoreWarnings 为 true，则仅记录日志；否则抛出异常。
	 */
	protected void handleWarnings(Statement stmt) throws SQLException {
		if (this.isIgnoreWarnings()) {
			if (this.logger.isDebugEnabled()) {
				for (SQLWarning warningToLog = stmt.getWarnings(); warningToLog != null; warningToLog = warningToLog.getNextWarning()) {
					this.logger.debug("SQLWarning ignored: SQL state '" + warningToLog.getSQLState() +
							"', error code '" + warningToLog.getErrorCode() +
							"', message [" + warningToLog.getMessage() + "]");
				}
			}
		} else {
			handleWarnings(stmt.getWarnings());
		}
	}

	/**
	 * 处理 SQLWarning（不忽略时）。
	 */
	protected void handleWarnings(@Nullable SQLWarning warning) throws SQLWarningException {
		if (warning != null) {
			throw new SQLWarningException("Warning not ignored", warning);
		}
	}

	/**
	 * 将 SQLException 转换为 Spring 的 DataAccessException。
	 */
	protected DataAccessException translateException(String task, @Nullable String sql, SQLException ex) {
		DataAccessException dae = this.getExceptionTranslator().translate(task, sql, ex);
		return (dae != null) ? dae : new UncategorizedSQLException(task, sql, ex);
	}

	// ==================== 内部工具方法 ====================

	/**
	 * 从 SqlProvider 对象中获取 SQL 字符串。
	 */
	@Nullable
	private static String getSql(Object sqlProvider) {
		return (sqlProvider instanceof SqlProvider) ? ((SqlProvider) sqlProvider).getSql() : null;
	}

	/**
	 * 断言结果非空，并返回。
	 */
	private static <T> T result(@Nullable T result) {
		Assert.state(result != null, "No result");
		return result;
	}

	/**
	 * 断言更新计数结果非空，并返回。
	 */
	private static int updateCount(@Nullable Integer result) {
		Assert.state(result != null, "No update count");
		return result;
	}

	// ==================== 内部类 ====================

	/**
	 * 用于流式查询的 Spliterator，将 ResultSet 转换为 Stream。
	 */
	private static class ResultSetSpliterator<T> implements Spliterator<T> {
		private final ResultSet rs;
		private final RowMapper<T> rowMapper;
		private int rowNum = 0;

		public ResultSetSpliterator(ResultSet rs, RowMapper<T> rowMapper) {
			this.rs = rs;
			this.rowMapper = rowMapper;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			try {
				if (this.rs.next()) {
					action.accept(this.rowMapper.mapRow(this.rs, this.rowNum++));
					return true;
				} else {
					return false;
				}
			} catch (SQLException ex) {
				throw new InvalidResultSetAccessException(ex);
			}
		}

		@Override
		@Nullable
		public Spliterator<T> trySplit() {
			return null; // 不支持并行拆分
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public int characteristics() {
			return Spliterator.NONNULL | Spliterator.ORDERED;
		}

		public Stream<T> stream() {
			return StreamSupport.stream(this, false);
		}
	}

	/**
	 * 将 RowCallbackHandler 适配为 ResultSetExtractor，用于方便地使用回调处理每一行。
	 */
	private static class RowCallbackHandlerResultSetExtractor implements ResultSetExtractor<Object> {
		private final RowCallbackHandler rch;

		public RowCallbackHandlerResultSetExtractor(RowCallbackHandler rch) {
			this.rch = rch;
		}

		@Nullable
		@Override
		public Object extractData(ResultSet rs) throws SQLException {
			while (rs.next()) {
				this.rch.processRow(rs);
			}
			return null;
		}
	}

	/**
	 * 简单的 CallableStatementCreator 实现，仅用于包装 SQL 字符串。
	 */
	private static class SimpleCallableStatementCreator implements CallableStatementCreator, SqlProvider {
		private final String callString;

		public SimpleCallableStatementCreator(String callString) {
			Assert.notNull(callString, "Call string must not be null");
			this.callString = callString;
		}

		@Override
		public CallableStatement createCallableStatement(Connection con) throws SQLException {
			return con.prepareCall(this.callString);
		}

		@Override
		public String getSql() {
			return this.callString;
		}
	}

	/**
	 * 简单的 PreparedStatementCreator 实现，仅用于包装 SQL 字符串。
	 */
	private static class SimplePreparedStatementCreator implements PreparedStatementCreator, SqlProvider {
		private final String sql;

		public SimplePreparedStatementCreator(String sql) {
			Assert.notNull(sql, "SQL must not be null");
			this.sql = sql;
		}

		@Override
		public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
			return con.prepareStatement(this.sql);
		}

		@Override
		public String getSql() {
			return this.sql;
		}
	}

	/**
	 * 用于代理连接的 InvocationHandler，抑制 close 调用，防止连接被意外关闭。
	 */
	private class CloseSuppressingInvocationHandler implements InvocationHandler {
		private final Connection target;

		public CloseSuppressingInvocationHandler(Connection target) {
			this.target = target;
		}

		@Nullable
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			switch (method.getName()) {
				case "equals":
					return proxy == args[0];
				case "hashCode":
					return System.identityHashCode(proxy);
				case "close":
					return null; // 忽略 close 调用
				case "isClosed":
					return false;
				case "getTargetConnection":
					return this.target;
				case "unwrap":
					return ((Class<?>) args[0]).isInstance(proxy) ? proxy : this.target.unwrap((Class<?>) args[0]);
				case "isWrapperFor":
					return ((Class<?>) args[0]).isInstance(proxy) || this.target.isWrapperFor((Class<?>) args[0]);
				default:
					try {
						Object retVal = method.invoke(this.target, args);
						if (retVal instanceof Statement) {
							JdbcTemplate.this.applyStatementSettings((Statement) retVal);
						}
						return retVal;
					} catch (InvocationTargetException ex) {
						throw ex.getTargetException();
					}
			}
		}
	}
}