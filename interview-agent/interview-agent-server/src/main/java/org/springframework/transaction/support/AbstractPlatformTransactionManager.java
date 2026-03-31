//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.transaction.support;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.Constants;
import org.springframework.lang.Nullable;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.InvalidTimeoutException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionSuspensionNotSupportedException;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * Spring 事务管理的抽象基类，实现了 {@link PlatformTransactionManager} 接口。
 * 该类遵循模板方法设计模式，定义了事务创建、提交、回滚的标准流程，
 * 并将与具体事务资源（如 JDBC、JTA、JPA）相关的操作留给子类实现。
 *
 * <p>该类提供了丰富的事务配置属性：
 * <ul>
 *   <li><b>事务同步</b>：控制是否在事务中开启同步机制（如资源绑定、同步回调）</li>
 *   <li><b>默认超时</b>：为没有指定超时的事务提供默认超时秒数</li>
 *   <li><b>嵌套事务支持</b>：是否允许嵌套事务（通过保存点）</li>
 *   <li><b>验证现有事务</b>：参与已有事务时是否验证隔离级别、只读属性等</li>
 *   <li><b>参与失败回滚全局</b>：当参与的事务失败时是否将全局事务标记为回滚</li>
 *   <li><b>提前回滚标志检查</b>：在提交时如果发现全局回滚标志是否尽早抛出异常</li>
 *   <li><b>提交失败时回滚</b>：提交过程中发生异常时是否执行回滚</li>
 * </ul>
 *
 * <p>子类需要实现的核心方法：
 * <ul>
 *   <li>{@link #doGetTransaction()}：获取当前事务对象</li>
 *   <li>{@link #isExistingTransaction(Object)}：判断是否存在事务</li>
 *   <li>{@link #doBegin(Object, TransactionDefinition)}：开始新事务</li>
 *   <li>{@link #doCommit(DefaultTransactionStatus)}：提交事务</li>
 *   <li>{@link #doRollback(DefaultTransactionStatus)}：回滚事务</li>
 *   <li>{@link #doSuspend(Object)} 和 {@link #doResume(Object, Object)}：挂起/恢复事务（可选）</li>
 *   <li>{@link #doSetRollbackOnly(DefaultTransactionStatus)}：设置回滚标志（参与事务时）</li>
 * </ul>
 *
 * @author Juergen Hoeller
 * @since 1.1
 * @see PlatformTransactionManager
 * @see DefaultTransactionStatus
 * @see TransactionSynchronizationManager
 */
public abstract class AbstractPlatformTransactionManager implements PlatformTransactionManager, Serializable {

	// ==================== 常量定义 ====================

	/** 总是开启事务同步（无论是否存在事务） */
	public static final int SYNCHRONIZATION_ALWAYS = 0;
	/** 仅当存在实际事务时才开启同步 */
	public static final int SYNCHRONIZATION_ON_ACTUAL_TRANSACTION = 1;
	/** 永不开启事务同步 */
	public static final int SYNCHRONIZATION_NEVER = 2;

	/** 用于解析常量名称的工具类 */
	private static final Constants constants = new Constants(AbstractPlatformTransactionManager.class);

	// ==================== 日志与配置属性 ====================

	/** 日志记录器（transient，避免序列化） */
	protected transient Log logger = LogFactory.getLog(getClass());

	/** 事务同步模式，默认为 SYNCHRONIZATION_ALWAYS */
	private int transactionSynchronization = SYNCHRONIZATION_ALWAYS;

	/** 默认事务超时时间（秒），-1 表示使用底层事务系统的默认值 */
	private int defaultTimeout = -1;

	/** 是否允许嵌套事务，默认 false */
	private boolean nestedTransactionAllowed = false;

	/** 参与已有事务时是否验证定义的一致性（隔离级别、只读等），默认 false */
	private boolean validateExistingTransaction = false;

	/** 当参与的事务失败时是否将全局事务标记为回滚，默认 true */
	private boolean globalRollbackOnParticipationFailure = true;

	/** 如果全局事务被标记为回滚，是否在提交时尽早抛出异常，默认 false */
	private boolean failEarlyOnGlobalRollbackOnly = false;

	/** 提交失败时是否执行回滚，默认 false */
	private boolean rollbackOnCommitFailure = false;

	// ==================== 构造器 ====================

	public AbstractPlatformTransactionManager() {
	}

	// ==================== Setter / Getter 方法 ====================

	/**
	 * 通过常量名称设置事务同步模式。
	 *
	 * @param constantName 常量名称，如 "SYNCHRONIZATION_ALWAYS"
	 */
	public final void setTransactionSynchronizationName(String constantName) {
		setTransactionSynchronization(constants.asNumber(constantName).intValue());
	}

	/**
	 * 设置事务同步模式。
	 *
	 * @param transactionSynchronization 模式值，必须是 SYNCHRONIZATION_ALWAYS、SYNCHRONIZATION_ON_ACTUAL_TRANSACTION 或 SYNCHRONIZATION_NEVER
	 */
	public final void setTransactionSynchronization(int transactionSynchronization) {
		this.transactionSynchronization = transactionSynchronization;
	}

	public final int getTransactionSynchronization() {
		return this.transactionSynchronization;
	}

	/**
	 * 设置默认事务超时时间（秒）。
	 *
	 * @param defaultTimeout 超时秒数，-1 表示使用底层事务系统的默认值
	 * @throws InvalidTimeoutException 如果值小于 -1
	 */
	public final void setDefaultTimeout(int defaultTimeout) {
		if (defaultTimeout < -1) {
			throw new InvalidTimeoutException("Invalid default timeout", defaultTimeout);
		}
		this.defaultTimeout = defaultTimeout;
	}

	public final int getDefaultTimeout() {
		return this.defaultTimeout;
	}

	/**
	 * 设置是否允许嵌套事务。
	 *
	 * @param nestedTransactionAllowed true 表示允许
	 */
	public final void setNestedTransactionAllowed(boolean nestedTransactionAllowed) {
		this.nestedTransactionAllowed = nestedTransactionAllowed;
	}

	public final boolean isNestedTransactionAllowed() {
		return this.nestedTransactionAllowed;
	}

	/**
	 * 设置参与已有事务时是否验证事务定义的一致性。
	 * 如果启用，会检查隔离级别和只读属性是否与现有事务匹配，不匹配则抛出异常。
	 *
	 * @param validateExistingTransaction true 表示验证
	 */
	public final void setValidateExistingTransaction(boolean validateExistingTransaction) {
		this.validateExistingTransaction = validateExistingTransaction;
	}

	public final boolean isValidateExistingTransaction() {
		return this.validateExistingTransaction;
	}

	/**
	 * 设置当参与的事务失败时是否将全局事务标记为回滚。
	 * 默认为 true，这样参与事务的失败会导致整个事务回滚。
	 *
	 * @param globalRollbackOnParticipationFailure true 表示标记全局回滚
	 */
	public final void setGlobalRollbackOnParticipationFailure(boolean globalRollbackOnParticipationFailure) {
		this.globalRollbackOnParticipationFailure = globalRollbackOnParticipationFailure;
	}

	public final boolean isGlobalRollbackOnParticipationFailure() {
		return this.globalRollbackOnParticipationFailure;
	}

	/**
	 * 设置当全局事务被标记为回滚时，是否在提交时尽早抛出异常。
	 * 默认为 false，即会尝试提交但最终由底层事务管理器抛出异常。
	 * 如果设置为 true，会在检测到回滚标志时立即抛出 UnexpectedRollbackException。
	 *
	 * @param failEarlyOnGlobalRollbackOnly true 表示尽早失败
	 */
	public final void setFailEarlyOnGlobalRollbackOnly(boolean failEarlyOnGlobalRollbackOnly) {
		this.failEarlyOnGlobalRollbackOnly = failEarlyOnGlobalRollbackOnly;
	}

	public final boolean isFailEarlyOnGlobalRollbackOnly() {
		return this.failEarlyOnGlobalRollbackOnly;
	}

	/**
	 * 设置提交失败时是否执行回滚。
	 * 默认为 false，即提交异常直接抛出，不回滚。
	 * 设置为 true 时，会先尝试回滚事务，然后再抛出异常。
	 *
	 * @param rollbackOnCommitFailure true 表示提交失败时回滚
	 */
	public final void setRollbackOnCommitFailure(boolean rollbackOnCommitFailure) {
		this.rollbackOnCommitFailure = rollbackOnCommitFailure;
	}

	public final boolean isRollbackOnCommitFailure() {
		return this.rollbackOnCommitFailure;
	}

	// ==================== 核心公共方法 ====================

	/**
	 * 根据给定的 TransactionDefinition 获取事务状态。
	 * 这是 PlatformTransactionManager 接口的主要实现方法。
	 * 它会根据传播行为（Propagation）决定是创建新事务、挂起当前事务，还是参与已有事务。
	 *
	 * @param definition 事务定义（可为 null，此时使用默认定义）
	 * @return 事务状态对象
	 * @throws TransactionException 如果获取事务失败
	 */
	@Override
	public final TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException {
		// 如果没有提供定义，使用默认定义（PROPAGATION_REQUIRED, ISOLATION_DEFAULT 等）
		TransactionDefinition def = (definition != null ? definition : TransactionDefinition.withDefaults());

		// 获取底层事务对象（由子类实现）
		Object transaction = doGetTransaction();
		boolean debugEnabled = logger.isDebugEnabled();

		// 如果已经存在事务，则根据传播行为处理
		if (isExistingTransaction(transaction)) {
			return handleExistingTransaction(def, transaction, debugEnabled);
		}

		// 检查超时值的有效性
		if (def.getTimeout() < -1) {
			throw new InvalidTimeoutException("Invalid transaction timeout", def.getTimeout());
		}

		// 传播行为为 MANDATORY 但没有现有事务 -> 抛出异常
		if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_MANDATORY) {
			throw new IllegalTransactionStateException(
					"No existing transaction found for transaction marked with propagation 'mandatory'");
		}
		// 传播行为为 REQUIRED、REQUIRES_NEW、NESTED 时，需要创建新事务
		else if (def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRED ||
				def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW ||
				def.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {

			// 对于 REQUIRES_NEW 或 NESTED，需要先挂起当前不存在的事务（实际上为 null）
			SuspendedResourcesHolder suspendedResources = suspend(null);
			if (debugEnabled) {
				logger.debug("Creating new transaction with name [" + def.getName() + "]: " + def);
			}
			try {
				// 启动新事务
				return startTransaction(def, transaction, debugEnabled, suspendedResources);
			} catch (RuntimeException | Error ex) {
				// 启动失败，恢复挂起资源
				resume(null, suspendedResources);
				throw ex;
			}
		}
		// 传播行为为 NEVER、SUPPORTS、NOT_SUPPORTED 等情况，没有实际事务
		else {
			// 如果指定了隔离级别但未启动实际事务，发出警告（隔离级别将被忽略）
			if (def.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT && logger.isWarnEnabled()) {
				logger.warn("Custom isolation level specified but no actual transaction initiated; " +
						"isolation level will effectively be ignored: " + def);
			}
			// 决定是否开启同步（仅在 SYNCHRONIZATION_ALWAYS 时开启）
			boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
			// 准备事务状态（没有实际事务，newTransaction=false）
			return prepareTransactionStatus(def, null, true, newSynchronization, debugEnabled, null);
		}
	}

	/**
	 * 启动一个新事务。
	 *
	 * @param definition         事务定义
	 * @param transaction        底层事务对象
	 * @param debugEnabled       是否开启调试日志
	 * @param suspendedResources 挂起的资源（来自 suspend）
	 * @return 事务状态
	 */
	private TransactionStatus startTransaction(TransactionDefinition definition, Object transaction,
											   boolean debugEnabled, @Nullable SuspendedResourcesHolder suspendedResources) {
		// 根据配置决定是否开启同步（SYNCHRONIZATION_NEVER 时不开启）
		boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
		DefaultTransactionStatus status = newTransactionStatus(definition, transaction, true,
				newSynchronization, debugEnabled, suspendedResources);
		// 子类实现：开始实际事务
		doBegin(transaction, definition);
		// 准备同步（如果开启了同步）
		prepareSynchronization(status, definition);
		return status;
	}

	/**
	 * 处理存在已有事务的情况，根据传播行为决定行为。
	 *
	 * @param definition    事务定义
	 * @param transaction   现有事务对象
	 * @param debugEnabled  是否开启调试日志
	 * @return 事务状态
	 * @throws TransactionException
	 */
	private TransactionStatus handleExistingTransaction(TransactionDefinition definition, Object transaction,
														boolean debugEnabled) throws TransactionException {
		// 传播行为 NEVER：不允许存在事务，抛出异常
		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NEVER) {
			throw new IllegalTransactionStateException(
					"Existing transaction found for transaction marked with propagation 'never'");
		}

		// 传播行为 NOT_SUPPORTED：挂起当前事务，然后以无事务方式运行
		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NOT_SUPPORTED) {
			if (debugEnabled) {
				logger.debug("Suspending current transaction");
			}
			SuspendedResourcesHolder suspendedResources = suspend(transaction);
			boolean newSynchronization = (getTransactionSynchronization() == SYNCHRONIZATION_ALWAYS);
			return prepareTransactionStatus(definition, null, false, newSynchronization, debugEnabled, suspendedResources);
		}

		// 传播行为 REQUIRES_NEW：挂起当前事务，创建新事务
		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_REQUIRES_NEW) {
			if (debugEnabled) {
				logger.debug("Suspending current transaction, creating new transaction with name [" +
						definition.getName() + "]");
			}
			SuspendedResourcesHolder suspendedResources = suspend(transaction);
			try {
				return startTransaction(definition, transaction, debugEnabled, suspendedResources);
			} catch (RuntimeException | Error ex) {
				// 启动失败后恢复挂起的事务
				resumeAfterBeginException(transaction, suspendedResources, ex);
				throw ex;
			}
		}

		// 传播行为 NESTED：嵌套事务
		if (definition.getPropagationBehavior() == TransactionDefinition.PROPAGATION_NESTED) {
			if (!isNestedTransactionAllowed()) {
				throw new NestedTransactionNotSupportedException(
						"Transaction manager does not allow nested transactions by default - " +
								"specify 'nestedTransactionAllowed' property with value 'true'");
			}
			if (debugEnabled) {
				logger.debug("Creating nested transaction with name [" + definition.getName() + "]");
			}
			// 是否使用保存点实现嵌套事务（通常为 true）
			if (useSavepointForNestedTransaction()) {
				// 创建事务状态，不标记为新事务，但会创建保存点
				DefaultTransactionStatus status = prepareTransactionStatus(definition, transaction, false,
						false, debugEnabled, null);
				status.createAndHoldSavepoint();
				return status;
			} else {
				// 如果不支持保存点，则使用 REQUIRES_NEW 方式（创建新事务）
				return startTransaction(definition, transaction, debugEnabled, null);
			}
		}

		// 传播行为 SUPPORTS、REQUIRED、MANDATORY 等：参与已有事务
		if (debugEnabled) {
			logger.debug("Participating in existing transaction");
		}

		// 如果需要验证现有事务
		if (isValidateExistingTransaction()) {
			// 验证隔离级别
			if (definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT) {
				Integer currentIsolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
				if (currentIsolationLevel == null || currentIsolationLevel != definition.getIsolationLevel()) {
					Constants isoConstants = DefaultTransactionDefinition.constants;
					throw new IllegalTransactionStateException("Participating transaction with definition [" +
							definition + "] specifies isolation level which is incompatible with existing transaction: " +
							(currentIsolationLevel != null ?
									isoConstants.toCode(currentIsolationLevel, DefaultTransactionDefinition.PREFIX_ISOLATION) :
									"(unknown)"));
				}
			}
			// 验证只读属性：如果现有事务是只读，而当前定义要求非只读，则抛出异常
			if (!definition.isReadOnly() && TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
				throw new IllegalTransactionStateException("Participating transaction with definition [" +
						definition + "] is not marked as read-only but existing transaction is");
			}
		}

		// 决定是否开启同步
		boolean newSynchronization = (getTransactionSynchronization() != SYNCHRONIZATION_NEVER);
		return prepareTransactionStatus(definition, transaction, false, newSynchronization, debugEnabled, null);
	}

	/**
	 * 准备事务状态，并初始化同步（如果需要）。
	 * 这是一个便捷方法，供子类或内部使用。
	 *
	 * @param definition         事务定义
	 * @param transaction        事务对象
	 * @param newTransaction     是否是新事务
	 * @param newSynchronization 是否是新同步（即之前没有活动同步）
	 * @param debug              是否调试模式
	 * @param suspendedResources 挂起的资源
	 * @return 事务状态
	 */
	protected final DefaultTransactionStatus prepareTransactionStatus(TransactionDefinition definition,
																	  @Nullable Object transaction,
																	  boolean newTransaction,
																	  boolean newSynchronization,
																	  boolean debug,
																	  @Nullable Object suspendedResources) {
		DefaultTransactionStatus status = newTransactionStatus(definition, transaction, newTransaction,
				newSynchronization, debug, suspendedResources);
		prepareSynchronization(status, definition);
		return status;
	}

	/**
	 * 创建新的事务状态对象。
	 * 子类可以重写此方法以返回自定义的 TransactionStatus 实现。
	 *
	 * @param definition         事务定义
	 * @param transaction        事务对象
	 * @param newTransaction     是否是新事务
	 * @param newSynchronization 是否是新同步
	 * @param debug              调试标志
	 * @param suspendedResources 挂起的资源
	 * @return DefaultTransactionStatus 实例
	 */
	protected DefaultTransactionStatus newTransactionStatus(TransactionDefinition definition,
															@Nullable Object transaction,
															boolean newTransaction,
															boolean newSynchronization,
															boolean debug,
															@Nullable Object suspendedResources) {
		boolean actualNewSynchronization = newSynchronization &&
				!TransactionSynchronizationManager.isSynchronizationActive();
		return new DefaultTransactionStatus(transaction, newTransaction, actualNewSynchronization,
				definition.isReadOnly(), debug, suspendedResources);
	}

	/**
	 * 准备事务同步：如果状态表明是新同步，则在 TransactionSynchronizationManager 中注册当前事务的属性。
	 *
	 * @param status     事务状态
	 * @param definition 事务定义
	 */
	protected void prepareSynchronization(DefaultTransactionStatus status, TransactionDefinition definition) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.setActualTransactionActive(status.hasTransaction());
			TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(
					definition.getIsolationLevel() != TransactionDefinition.ISOLATION_DEFAULT ?
							definition.getIsolationLevel() : null);
			TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());
			TransactionSynchronizationManager.setCurrentTransactionName(definition.getName());
			TransactionSynchronizationManager.initSynchronization();
		}
	}

	/**
	 * 确定实际使用的事务超时时间。
	 * 优先使用定义中的超时，如果没有则使用默认超时。
	 *
	 * @param definition 事务定义
	 * @return 超时秒数
	 */
	protected int determineTimeout(TransactionDefinition definition) {
		return (definition.getTimeout() != TransactionDefinition.TIMEOUT_DEFAULT ?
				definition.getTimeout() : getDefaultTimeout());
	}

	// ==================== 挂起 / 恢复 ====================

	/**
	 * 挂起当前事务（如果存在），并返回挂起的资源。
	 *
	 * @param transaction 当前事务对象（可能为 null）
	 * @return 挂起资源持有者，如果没有挂起则返回 null
	 * @throws TransactionException 如果挂起失败
	 */
	@Nullable
	protected final SuspendedResourcesHolder suspend(@Nullable Object transaction) throws TransactionException {
		// 如果同步处于活动状态，挂起所有注册的同步
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			List<TransactionSynchronization> suspendedSynchronizations = doSuspendSynchronization();
			try {
				Object suspendedResources = null;
				if (transaction != null) {
					suspendedResources = doSuspend(transaction);
				}
				// 保存当前事务同步管理器中的属性
				String name = TransactionSynchronizationManager.getCurrentTransactionName();
				TransactionSynchronizationManager.setCurrentTransactionName(null);
				boolean readOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
				TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);
				Integer isolationLevel = TransactionSynchronizationManager.getCurrentTransactionIsolationLevel();
				TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(null);
				boolean wasActive = TransactionSynchronizationManager.isActualTransactionActive();
				TransactionSynchronizationManager.setActualTransactionActive(false);
				return new SuspendedResourcesHolder(suspendedResources, suspendedSynchronizations,
						name, readOnly, isolationLevel, wasActive);
			} catch (RuntimeException | Error ex) {
				// 挂起失败，恢复同步
				doResumeSynchronization(suspendedSynchronizations);
				throw ex;
			}
		} else if (transaction != null) {
			// 没有同步活动，但需要挂起资源（仅限事务资源）
			Object suspendedResources = doSuspend(transaction);
			return new SuspendedResourcesHolder(suspendedResources);
		} else {
			return null;
		}
	}

	/**
	 * 恢复之前挂起的事务。
	 *
	 * @param transaction     当前事务对象（可能为 null）
	 * @param resourcesHolder 挂起资源持有者
	 * @throws TransactionException 如果恢复失败
	 */
	protected final void resume(@Nullable Object transaction, @Nullable SuspendedResourcesHolder resourcesHolder)
			throws TransactionException {
		if (resourcesHolder != null) {
			// 恢复事务资源
			Object suspendedResources = resourcesHolder.suspendedResources;
			if (suspendedResources != null) {
				doResume(transaction, suspendedResources);
			}
			// 恢复同步和属性
			List<TransactionSynchronization> suspendedSynchronizations = resourcesHolder.suspendedSynchronizations;
			if (suspendedSynchronizations != null) {
				TransactionSynchronizationManager.setActualTransactionActive(resourcesHolder.wasActive);
				TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(resourcesHolder.isolationLevel);
				TransactionSynchronizationManager.setCurrentTransactionReadOnly(resourcesHolder.readOnly);
				TransactionSynchronizationManager.setCurrentTransactionName(resourcesHolder.name);
				doResumeSynchronization(suspendedSynchronizations);
			}
		}
	}

	/**
	 * 在开始新事务失败后恢复挂起的事务。
	 *
	 * @param transaction       事务对象
	 * @param suspendedResources 挂起的资源
	 * @param beginEx           开始事务时抛出的异常
	 */
	private void resumeAfterBeginException(Object transaction, @Nullable SuspendedResourcesHolder suspendedResources,
										   Throwable beginEx) {
		try {
			resume(transaction, suspendedResources);
		} catch (RuntimeException | Error ex) {
			String exMessage = "Inner transaction begin exception overridden by outer transaction resume exception";
			logger.error(exMessage, beginEx);
			throw ex;
		}
	}

	/**
	 * 挂起当前所有的 TransactionSynchronization，返回它们的列表。
	 */
	private List<TransactionSynchronization> doSuspendSynchronization() {
		List<TransactionSynchronization> suspendedSynchronizations =
				TransactionSynchronizationManager.getSynchronizations();
		for (TransactionSynchronization synchronization : suspendedSynchronizations) {
			synchronization.suspend();
		}
		TransactionSynchronizationManager.clearSynchronization();
		return suspendedSynchronizations;
	}

	/**
	 * 恢复挂起的同步，并重新注册。
	 */
	private void doResumeSynchronization(List<TransactionSynchronization> suspendedSynchronizations) {
		TransactionSynchronizationManager.initSynchronization();
		for (TransactionSynchronization synchronization : suspendedSynchronizations) {
			synchronization.resume();
			TransactionSynchronizationManager.registerSynchronization(synchronization);
		}
	}

	// ==================== 提交 / 回滚 ====================

	/**
	 * 提交事务。这是 PlatformTransactionManager 接口的方法。
	 *
	 * @param status 事务状态（必须未完成）
	 * @throws TransactionException 如果提交失败
	 */
	@Override
	public final void commit(TransactionStatus status) throws TransactionException {
		if (status.isCompleted()) {
			throw new IllegalTransactionStateException(
					"Transaction is already completed - do not call commit or rollback more than once per transaction");
		}
		DefaultTransactionStatus defStatus = (DefaultTransactionStatus) status;

		// 如果本地代码标记了回滚（例如通过 TransactionStatus.setRollbackOnly()），则执行回滚
		if (defStatus.isLocalRollbackOnly()) {
			if (defStatus.isDebug()) {
				logger.debug("Transactional code has requested rollback");
			}
			processRollback(defStatus, false);
			return;
		}

		// 如果全局事务被标记为回滚，但 shouldCommitOnGlobalRollbackOnly 为 false，则执行回滚
		if (!shouldCommitOnGlobalRollbackOnly() && defStatus.isGlobalRollbackOnly()) {
			if (defStatus.isDebug()) {
				logger.debug("Global transaction is marked as rollback-only but transactional code requested commit");
			}
			processRollback(defStatus, true);
			return;
		}

		// 正常提交
		processCommit(defStatus);
	}

	/**
	 * 执行提交逻辑。
	 *
	 * @param status 事务状态
	 */
	private void processCommit(DefaultTransactionStatus status) throws TransactionException {
		try {
			boolean beforeCompletionInvoked = false;
			try {
				boolean unexpectedRollback = false;
				// 准备提交（子类可覆盖，例如刷新会话）
				prepareForCommit(status);
				// 触发 beforeCommit 同步回调
				triggerBeforeCommit(status);
				// 触发 beforeCompletion 同步回调
				triggerBeforeCompletion(status);
				beforeCompletionInvoked = true;

				// 处理保存点（嵌套事务）
				if (status.hasSavepoint()) {
					if (status.isDebug()) {
						logger.debug("Releasing transaction savepoint");
					}
					unexpectedRollback = status.isGlobalRollbackOnly();
					status.releaseHeldSavepoint();
				}
				// 如果是新事务，执行真正的提交
				else if (status.isNewTransaction()) {
					if (status.isDebug()) {
						logger.debug("Initiating transaction commit");
					}
					unexpectedRollback = status.isGlobalRollbackOnly();
					doCommit(status);
				}
				// 对于参与的事务，检查全局回滚标志（如果设置了 failEarly）
				else if (isFailEarlyOnGlobalRollbackOnly()) {
					unexpectedRollback = status.isGlobalRollbackOnly();
				}

				// 如果检测到意外的回滚（全局回滚标志为 true），抛出异常
				if (unexpectedRollback) {
					throw new UnexpectedRollbackException(
							"Transaction silently rolled back because it has been marked as rollback-only");
				}
			} catch (UnexpectedRollbackException ex) {
				// 触发 afterCompletion 状态为 STATUS_ROLLED_BACK
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
				throw ex;
			} catch (TransactionException ex) {
				// 提交异常：根据 rollbackOnCommitFailure 决定是否回滚
				if (isRollbackOnCommitFailure()) {
					doRollbackOnCommitException(status, ex);
				} else {
					triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
				}
				throw ex;
			} catch (RuntimeException | Error ex) {
				// 未预期的异常，如果没有触发 beforeCompletion，则先触发它
				if (!beforeCompletionInvoked) {
					triggerBeforeCompletion(status);
				}
				doRollbackOnCommitException(status, ex);
				throw ex;
			}

			// 提交成功，触发 afterCommit 和 afterCompletion
			try {
				triggerAfterCommit(status);
			} finally {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_COMMITTED);
			}
		} finally {
			// 清理资源
			cleanupAfterCompletion(status);
		}
	}

	/**
	 * 执行回滚逻辑。
	 *
	 * @param status     事务状态
	 * @param unexpected 是否是意外的回滚（由全局回滚标志引起）
	 */
	private void processRollback(DefaultTransactionStatus status, boolean unexpected) {
		try {
			boolean unexpectedRollback = unexpected;
			try {
				// 触发 beforeCompletion 同步回调
				triggerBeforeCompletion(status);

				// 如果有保存点，回滚到保存点
				if (status.hasSavepoint()) {
					if (status.isDebug()) {
						logger.debug("Rolling back transaction to savepoint");
					}
					status.rollbackToHeldSavepoint();
				}
				// 如果是新事务，执行真正的回滚
				else if (status.isNewTransaction()) {
					if (status.isDebug()) {
						logger.debug("Initiating transaction rollback");
					}
					doRollback(status);
				}
				// 对于参与的事务，根据配置决定是否标记回滚
				else if (status.hasTransaction()) {
					// 如果本地没有要求回滚，且全局参与失败时不需要标记回滚，则什么也不做
					if (!status.isLocalRollbackOnly() && !isGlobalRollbackOnParticipationFailure()) {
						if (status.isDebug()) {
							logger.debug("Participating transaction failed - letting transaction originator decide on rollback");
						}
					} else {
						if (status.isDebug()) {
							logger.debug("Participating transaction failed - marking existing transaction as rollback-only");
						}
						doSetRollbackOnly(status);
					}
					// 如果设置了 failEarlyOnGlobalRollbackOnly，且全局回滚标志为 true，则抛出异常
					if (!isFailEarlyOnGlobalRollbackOnly()) {
						unexpectedRollback = false;
					}
				} else {
					logger.debug("Should roll back transaction but cannot - no transaction available");
				}
			} catch (RuntimeException | Error ex) {
				triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
				throw ex;
			}

			// 触发 afterCompletion，状态为 STATUS_ROLLED_BACK
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
			if (unexpectedRollback) {
				throw new UnexpectedRollbackException(
						"Transaction rolled back because it has been marked as rollback-only");
			}
		} finally {
			cleanupAfterCompletion(status);
		}
	}

	/**
	 * 提交失败时执行回滚（根据 rollbackOnCommitFailure 配置）。
	 *
	 * @param status 事务状态
	 * @param ex     提交时抛出的异常
	 */
	private void doRollbackOnCommitException(DefaultTransactionStatus status, Throwable ex) throws TransactionException {
		try {
			if (status.isNewTransaction()) {
				if (status.isDebug()) {
					logger.debug("Initiating transaction rollback after commit exception", ex);
				}
				doRollback(status);
			} else if (status.hasTransaction() && isGlobalRollbackOnParticipationFailure()) {
				if (status.isDebug()) {
					logger.debug("Marking existing transaction as rollback-only after commit exception", ex);
				}
				doSetRollbackOnly(status);
			}
		} catch (RuntimeException | Error rbex) {
			logger.error("Commit exception overridden by rollback exception", ex);
			triggerAfterCompletion(status, TransactionSynchronization.STATUS_UNKNOWN);
			throw rbex;
		}
		triggerAfterCompletion(status, TransactionSynchronization.STATUS_ROLLED_BACK);
	}

	/**
	 * 触发 beforeCommit 同步回调。
	 */
	protected final void triggerBeforeCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationUtils.triggerBeforeCommit(status.isReadOnly());
		}
	}

	/**
	 * 触发 beforeCompletion 同步回调。
	 */
	protected final void triggerBeforeCompletion(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationUtils.triggerBeforeCompletion();
		}
	}

	/**
	 * 触发 afterCommit 同步回调。
	 */
	private void triggerAfterCommit(DefaultTransactionStatus status) {
		if (status.isNewSynchronization()) {
			TransactionSynchronizationUtils.triggerAfterCommit();
		}
	}

	/**
	 * 触发 afterCompletion 同步回调，并清理同步。
	 *
	 * @param status           事务状态
	 * @param completionStatus 完成状态（STATUS_COMMITTED, STATUS_ROLLED_BACK, STATUS_UNKNOWN）
	 */
	private void triggerAfterCompletion(DefaultTransactionStatus status, int completionStatus) {
		if (status.isNewSynchronization()) {
			List<TransactionSynchronization> synchronizations =
					TransactionSynchronizationManager.getSynchronizations();
			TransactionSynchronizationManager.clearSynchronization();
			if (status.hasTransaction() && !status.isNewTransaction()) {
				// 对于参与的事务，不能立即调用 afterCompletion，而是注册到现有事务中
				if (!synchronizations.isEmpty()) {
					registerAfterCompletionWithExistingTransaction(status.getTransaction(), synchronizations);
				}
			} else {
				invokeAfterCompletion(synchronizations, completionStatus);
			}
		}
	}

	/**
	 * 调用 afterCompletion 回调。
	 *
	 * @param synchronizations 同步列表
	 * @param completionStatus 完成状态
	 */
	protected final void invokeAfterCompletion(List<TransactionSynchronization> synchronizations, int completionStatus) {
		TransactionSynchronizationUtils.invokeAfterCompletion(synchronizations, completionStatus);
	}

	/**
	 * 清理事务完成后的资源。
	 *
	 * @param status 事务状态
	 */
	private void cleanupAfterCompletion(DefaultTransactionStatus status) {
		status.setCompleted();
		if (status.isNewSynchronization()) {
			TransactionSynchronizationManager.clear();
		}
		if (status.isNewTransaction()) {
			doCleanupAfterCompletion(status.getTransaction());
		}
		// 恢复挂起的事务
		if (status.getSuspendedResources() != null) {
			if (status.isDebug()) {
				logger.debug("Resuming suspended transaction after completion of inner transaction");
			}
			Object transaction = (status.hasTransaction() ? status.getTransaction() : null);
			resume(transaction, (SuspendedResourcesHolder) status.getSuspendedResources());
		}
	}

	// ==================== 抽象方法（子类必须实现） ====================

	/**
	 * 获取当前的事务对象。返回的对象类型由子类决定（如 DataSourceTransactionObject、JtaTransactionObject 等）。
	 *
	 * @return 事务对象
	 * @throws TransactionException 如果获取失败
	 */
	protected abstract Object doGetTransaction() throws TransactionException;

	/**
	 * 判断给定的事务对象是否表示一个已存在的事务。
	 * 默认实现返回 false（即不支持检测已有事务）。支持事务同步的管理器需要重写此方法。
	 *
	 * @param transaction 事务对象
	 * @return 是否存在事务
	 * @throws TransactionException
	 */
	protected boolean isExistingTransaction(Object transaction) throws TransactionException {
		return false;
	}

	/**
	 * 是否使用保存点实现嵌套事务。默认返回 true。
	 * 如果返回 false，则嵌套事务会作为独立的事务（REQUIRES_NEW）处理。
	 *
	 * @return true 表示使用保存点
	 */
	protected boolean useSavepointForNestedTransaction() {
		return true;
	}

	/**
	 * 开始一个新事务。
	 *
	 * @param transaction 事务对象
	 * @param definition  事务定义
	 * @throws TransactionException
	 */
	protected abstract void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException;

	/**
	 * 挂起当前事务。默认实现抛出异常，表明不支持事务挂起。
	 * 支持 REQUIRES_NEW 和 NOT_SUPPORTED 传播行为的子类必须实现此方法。
	 *
	 * @param transaction 事务对象
	 * @return 挂起的资源对象（将在 resume 时使用）
	 * @throws TransactionException
	 */
	protected Object doSuspend(Object transaction) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	/**
	 * 恢复挂起的事务。
	 *
	 * @param transaction      当前事务对象
	 * @param suspendedResources 挂起的资源
	 * @throws TransactionException
	 */
	protected void doResume(@Nullable Object transaction, Object suspendedResources) throws TransactionException {
		throw new TransactionSuspensionNotSupportedException(
				"Transaction manager [" + getClass().getName() + "] does not support transaction suspension");
	}

	/**
	 * 判断是否应该提交一个全局标记为回滚的事务。
	 * 默认返回 false（即遇到全局回滚标志时，执行回滚而非提交）。
	 * 某些事务管理器（如 JTA）可能允许提交只读事务即使被标记了回滚，这时可以重写返回 true。
	 *
	 * @return 是否提交
	 */
	protected boolean shouldCommitOnGlobalRollbackOnly() {
		return false;
	}

	/**
	 * 准备提交。在 beforeCommit 同步之前调用，子类可覆盖以执行刷新等操作。
	 *
	 * @param status 事务状态
	 * @throws TransactionException
	 */
	protected void prepareForCommit(DefaultTransactionStatus status) {
	}

	/**
	 * 提交事务。
	 *
	 * @param status 事务状态
	 * @throws TransactionException
	 */
	protected abstract void doCommit(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * 回滚事务。
	 *
	 * @param status 事务状态
	 * @throws TransactionException
	 */
	protected abstract void doRollback(DefaultTransactionStatus status) throws TransactionException;

	/**
	 * 将现有事务标记为仅回滚。
	 * 默认实现抛出异常，要求子类在支持参与事务时提供实现。
	 *
	 * @param status 事务状态
	 * @throws TransactionException
	 */
	protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
		throw new IllegalTransactionStateException(
				"Participating in existing transactions is not supported - " +
						"when 'isExistingTransaction' returns true, appropriate 'doSetRollbackOnly' behavior must be provided");
	}

	/**
	 * 将 afterCompletion 同步注册到现有事务中。
	 * 默认实现立即调用 afterCompletion（状态为 STATUS_UNKNOWN），
	 * 支持在事务完成后才调用同步的管理器需要重写此方法。
	 *
	 * @param transaction      事务对象
	 * @param synchronizations 同步列表
	 * @throws TransactionException
	 */
	protected void registerAfterCompletionWithExistingTransaction(Object transaction,
																  List<TransactionSynchronization> synchronizations) throws TransactionException {
		logger.debug("Cannot register Spring after-completion synchronization with existing transaction - " +
				"processing Spring after-completion callbacks immediately, with outcome status 'unknown'");
		invokeAfterCompletion(synchronizations, TransactionSynchronization.STATUS_UNKNOWN);
	}

	/**
	 * 清理事务完成后的资源。子类可覆盖以释放特定资源。
	 *
	 * @param transaction 事务对象
	 */
	protected void doCleanupAfterCompletion(Object transaction) {
	}

	// ==================== 序列化支持 ====================

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		// 恢复 transient 的 logger
		this.logger = LogFactory.getLog(getClass());
	}

	// ==================== 内部类：挂起资源持有者 ====================

	/**
	 * 挂起资源的持有者，用于保存被挂起的事务的所有相关状态。
	 */
	protected static final class SuspendedResourcesHolder {

		/** 挂起的事务资源（由子类 doSuspend 返回） */
		@Nullable
		private final Object suspendedResources;

		/** 挂起的同步列表 */
		@Nullable
		private List<TransactionSynchronization> suspendedSynchronizations;

		/** 挂起的事务名称 */
		@Nullable
		private String name;

		/** 挂起的事务是否只读 */
		private boolean readOnly;

		/** 挂起的事务隔离级别 */
		@Nullable
		private Integer isolationLevel;

		/** 挂起时实际事务是否活动 */
		private boolean wasActive;

		/**
		 * 仅包含事务资源的构造器（没有同步）。
		 */
		private SuspendedResourcesHolder(Object suspendedResources) {
			this.suspendedResources = suspendedResources;
		}

		/**
		 * 完整构造器。
		 */
		private SuspendedResourcesHolder(@Nullable Object suspendedResources,
										 List<TransactionSynchronization> suspendedSynchronizations,
										 @Nullable String name, boolean readOnly,
										 @Nullable Integer isolationLevel, boolean wasActive) {
			this.suspendedResources = suspendedResources;
			this.suspendedSynchronizations = suspendedSynchronizations;
			this.name = name;
			this.readOnly = readOnly;
			this.isolationLevel = isolationLevel;
			this.wasActive = wasActive;
		}
	}
}