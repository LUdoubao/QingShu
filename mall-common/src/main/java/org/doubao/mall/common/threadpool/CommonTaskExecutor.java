package org.doubao.mall.common.threadpool;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;


@Component
public class CommonTaskExecutor implements Executor {
	private final ThreadPoolTaskExecutor delegate;

	public CommonTaskExecutor(ThreadPoolTaskExecutor delegate) {
		this.delegate = delegate;
	}

	public <T> CompletableFuture<T> asyncExecute(Callable<T> task) {
		CompletableFuture<T> future = new CompletableFuture<>();
		delegate.execute(() -> {
			try {
				future.complete(task.call());
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
		});
		return future;
	}

	// 实现Executor接口的execute方法
	@Override
	public void execute(Runnable command) {
		delegate.execute(command);
	}
}