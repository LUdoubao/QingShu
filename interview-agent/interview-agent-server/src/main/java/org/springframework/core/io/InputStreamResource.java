//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.core.io;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Spring 资源抽象中基于 {@link InputStream} 的实现类。
 * 该类包装一个已经打开的 InputStream，并将其作为资源提供。
 *
 * <p><b>重要注意事项：</b>
 * <ul>
 *   <li>{@link #getInputStream()} 方法返回的流是直接返回构造时传入的 InputStream 实例，
 *       且该流只能被读取一次。第二次调用 {@code getInputStream()} 会抛出异常。</li>
 *   <li>该类不负责关闭流，需要调用方自行管理流的生命周期。</li>
 *   <li>由于流只能读一次，因此该类不适合需要多次读取的资源（如需要缓存或重新打开的资源）。</li>
 * </ul>
 *
 * <p>通常用于简单的、单次使用的资源场景，例如从 HTTP 响应、文件上传等获取的输入流。
 * 如果需要可重复读取的资源，应使用 {@link ByteArrayResource} 或 {@link FileSystemResource}。
 *
 * @author Juergen Hoeller
 * @since 1.0.1
 * @see #getInputStream()
 * @see ByteArrayResource
 * @see FileSystemResource
 */
public class InputStreamResource extends AbstractResource {

	/** 被封装的输入流对象 */
	private final InputStream inputStream;

	/** 资源的描述信息，用于日志或异常提示 */
	private final String description;

	/** 标记该输入流是否已经被读取过（即是否已经调用过 getInputStream） */
	private boolean read;

	/**
	 * 构造一个 InputStreamResource，使用默认的描述信息。
	 *
	 * @param inputStream 要封装的输入流（不能为 null）
	 */
	public InputStreamResource(InputStream inputStream) {
		this(inputStream, "resource loaded through InputStream");
	}

	/**
	 * 构造一个 InputStreamResource，使用自定义的描述信息。
	 *
	 * @param inputStream 要封装的输入流（不能为 null）
	 * @param description 描述信息，可用于日志或调试
	 */
	public InputStreamResource(InputStream inputStream, @Nullable String description) {
		this.read = false;
		Assert.notNull(inputStream, "InputStream must not be null");
		this.inputStream = inputStream;
		this.description = (description != null ? description : "");
	}

	/**
	 * 判断资源是否存在。对于 InputStreamResource，总是返回 true，
	 * 因为流已经存在（只是可能已被读取）。
	 *
	 * @return true
	 */
	@Override
	public boolean exists() {
		return true;
	}

	/**
	 * 判断资源是否表示一个打开的流。
	 * 由于 InputStream 一旦打开就无法重新打开，所以始终返回 true。
	 *
	 * @return true
	 */
	@Override
	public boolean isOpen() {
		return true;
	}

	/**
	 * 返回封装的输入流。
	 * 注意：该方法只能被调用一次，第二次调用会抛出 IllegalStateException。
	 *
	 * @return 构造时传入的 InputStream 实例
	 * @throws IllegalStateException 如果已经调用过此方法
	 * @throws IOException 如果获取输入流时发生错误（本实现不会主动抛出，但保留声明）
	 */
	@Override
	public InputStream getInputStream() throws IOException, IllegalStateException {
		if (this.read) {
			throw new IllegalStateException(
					"InputStream has already been read - do not use InputStreamResource if a stream needs to be read multiple times");
		}
		this.read = true;
		return this.inputStream;
	}

	/**
	 * 返回资源的描述信息，包含自定义描述文本。
	 *
	 * @return 描述字符串，例如 "InputStream resource [uploaded file]"
	 */
	@Override
	public String getDescription() {
		return "InputStream resource [" + this.description + "]";
	}

	/**
	 * 比较两个 InputStreamResource 是否相等。
	 * 仅当它们是同一个对象，或者封装的 InputStream 实例相等时返回 true。
	 * 注意：这通常意味着比较的是 InputStream 对象的引用，而非内容。
	 *
	 * @param other 比较对象
	 * @return 是否相等
	 */
	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || (other instanceof InputStreamResource &&
				((InputStreamResource) other).inputStream.equals(this.inputStream)));
	}

	/**
	 * 返回哈希码，基于封装的 InputStream 的哈希码。
	 *
	 * @return 哈希码值
	 */
	@Override
	public int hashCode() {
		return this.inputStream.hashCode();
	}
}