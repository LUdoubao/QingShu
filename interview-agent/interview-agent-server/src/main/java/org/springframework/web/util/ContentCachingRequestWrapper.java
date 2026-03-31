//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.springframework.web.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

/**
 * Spring Web 提供的 HttpServletRequest 包装器，用于缓存请求的输入内容。
 * 该类可以缓存 ServletInputStream 或 Reader 读取的数据，以及 POST 表单的参数内容，
 * 从而允许在请求处理过程中多次读取原始请求体。
 *
 * <p>使用场景：过滤器（Filter）或拦截器需要读取请求体内容（例如用于日志记录、验签、参数校验等），
 * 同时后续的处理器（如 Controller）仍能正常读取请求参数。默认情况下，HttpServletRequest 的输入流
 * 只能被读取一次，通过此包装器可以将数据缓存在内存中，后续调用 getInputStream() 或 getReader()
 * 时，会从缓存中重复读取。
 *
 * <p>注意：缓存的内容默认限制为 1024 字节（或通过构造参数指定），超出限制时会截断并调用
 * {@link #handleContentOverflow(int)} 方法，子类可重写该方法处理溢出逻辑。
 *
 * <p>对于 POST 表单（Content-Type = application/x-www-form-urlencoded），当第一次调用参数相关方法
 * （如 getParameter）时，会主动将参数键值对以 URL 编码形式写入缓存，以便同时缓存表单数据。
 *
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @author Rossen Stoyanchev
 * @since 3.0
 * @see #getContentAsByteArray()
 * @see #handleContentOverflow(int)
 */
public class ContentCachingRequestWrapper extends HttpServletRequestWrapper {

	/** 表单内容类型标识 */
	private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded";

	/** 缓存请求内容的字节输出流，所有读取的数据都会写入此流中 */
	private final ByteArrayOutputStream cachedContent;

	/** 内容缓存的最大字节数限制，为 null 表示不限制（默认限制为 1024 或请求 Content-Length） */
	@Nullable
	private final Integer contentCacheLimit;

	/** 包装后的 ServletInputStream，用于从底层请求读取并同时写入缓存 */
	@Nullable
	private ServletInputStream inputStream;

	/** 包装后的 BufferedReader，用于从底层请求读取字符数据并同时写入缓存 */
	@Nullable
	private BufferedReader reader;

	/**
	 * 创建一个新的 ContentCachingRequestWrapper，使用默认的缓存大小限制。
	 * 缓存大小初始化为请求的 Content-Length（如果有）或默认 1024 字节。
	 *
	 * @param request 原始的 HttpServletRequest
	 */
	public ContentCachingRequestWrapper(HttpServletRequest request) {
		super(request);
		int contentLength = request.getContentLength();
		// 缓存容量：如果请求有 Content-Length 则使用该值，否则使用 1024
		this.cachedContent = new ByteArrayOutputStream(contentLength >= 0 ? contentLength : 1024);
		this.contentCacheLimit = null; // 不设置额外限制，使用构造时的容量即可
	}

	/**
	 * 创建一个新的 ContentCachingRequestWrapper，并指定内容缓存的最大字节数限制。
	 * 超过限制后，后续的数据将不再缓存，并调用 {@link #handleContentOverflow(int)}。
	 *
	 * @param request           原始的 HttpServletRequest
	 * @param contentCacheLimit 缓存的最大字节数（必须为正数）
	 */
	public ContentCachingRequestWrapper(HttpServletRequest request, int contentCacheLimit) {
		super(request);
		this.cachedContent = new ByteArrayOutputStream(contentCacheLimit);
		this.contentCacheLimit = contentCacheLimit;
	}

	/**
	 * 返回包装后的 ServletInputStream，该输入流会同时将读取的数据写入缓存。
	 * 如果尚未创建，则创建 ContentCachingInputStream 实例并缓存。
	 *
	 * @return ServletInputStream 实例
	 * @throws IOException 如果获取原始输入流失败
	 */
	@Override
	public ServletInputStream getInputStream() throws IOException {
		if (this.inputStream == null) {
			// 使用原始请求的输入流创建缓存输入流
			this.inputStream = new ContentCachingInputStream(this.getRequest().getInputStream());
		}
		return this.inputStream;
	}

	/**
	 * 获取请求的字符编码，如果原始请求未指定编码，则默认返回 "ISO-8859-1"。
	 *
	 * @return 字符编码名称
	 */
	@Override
	public String getCharacterEncoding() {
		String enc = super.getCharacterEncoding();
		return (enc != null ? enc : "ISO-8859-1");
	}

	/**
	 * 返回包装后的 BufferedReader，用于读取字符数据并同时缓存。
	 *
	 * @return BufferedReader 实例
	 * @throws IOException 如果获取输入流失败
	 */
	@Override
	public BufferedReader getReader() throws IOException {
		if (this.reader == null) {
			// 通过缓存输入流构建 Reader，确保读取的数据也被缓存
			this.reader = new BufferedReader(new InputStreamReader(this.getInputStream(), this.getCharacterEncoding()));
		}
		return this.reader;
	}

	/**
	 * 获取单个参数值。
	 * 如果当前缓存为空且请求为 POST 表单，则先将表单参数写入缓存。
	 *
	 * @param name 参数名
	 * @return 参数值
	 */
	@Override
	public String getParameter(String name) {
		if (this.cachedContent.size() == 0 && this.isFormPost()) {
			this.writeRequestParametersToCachedContent();
		}
		return super.getParameter(name);
	}

	/**
	 * 获取所有参数的映射。
	 * 如果当前缓存为空且请求为 POST 表单，则先将表单参数写入缓存。
	 *
	 * @return 参数名到参数值数组的映射
	 */
	@Override
	public Map<String, String[]> getParameterMap() {
		if (this.cachedContent.size() == 0 && this.isFormPost()) {
			this.writeRequestParametersToCachedContent();
		}
		return super.getParameterMap();
	}

	/**
	 * 获取所有参数名的枚举。
	 * 如果当前缓存为空且请求为 POST 表单，则先将表单参数写入缓存。
	 *
	 * @return 参数名枚举
	 */
	@Override
	public Enumeration<String> getParameterNames() {
		if (this.cachedContent.size() == 0 && this.isFormPost()) {
			this.writeRequestParametersToCachedContent();
		}
		return super.getParameterNames();
	}

	/**
	 * 获取指定参数的所有值。
	 * 如果当前缓存为空且请求为 POST 表单，则先将表单参数写入缓存。
	 *
	 * @param name 参数名
	 * @return 参数值数组
	 */
	@Override
	public String[] getParameterValues(String name) {
		if (this.cachedContent.size() == 0 && this.isFormPost()) {
			this.writeRequestParametersToCachedContent();
		}
		return super.getParameterValues(name);
	}

	/**
	 * 判断当前请求是否为 POST 表单请求（Content-Type 为 application/x-www-form-urlencoded 且 HTTP 方法为 POST）。
	 *
	 * @return true 表示是 POST 表单请求
	 */
	private boolean isFormPost() {
		String contentType = this.getContentType();
		return (contentType != null && contentType.contains(FORM_CONTENT_TYPE) &&
				HttpMethod.POST.matches(this.getMethod()));
	}

	/**
	 * 将请求参数（键值对）以 URL 编码的形式写入缓存。
	 * 此方法仅在缓存尚未有内容且当前请求为 POST 表单时调用，目的是确保缓存中也能包含表单数据。
	 * 写入的格式与标准的 application/x-www-form-urlencoded 请求体一致，例如 "name1=value1&name2=value2"。
	 */
	private void writeRequestParametersToCachedContent() {
		try {
			if (this.cachedContent.size() == 0) {
				String requestEncoding = this.getCharacterEncoding();
				Map<String, String[]> form = super.getParameterMap();
				Iterator<String> nameIterator = form.keySet().iterator();
				while (nameIterator.hasNext()) {
					String name = nameIterator.next();
					List<String> values = Arrays.asList(form.get(name));
					Iterator<String> valueIterator = values.iterator();
					while (valueIterator.hasNext()) {
						String value = valueIterator.next();
						// 写入参数名（URL 编码）
						this.cachedContent.write(URLEncoder.encode(name, requestEncoding).getBytes());
						if (value != null) {
							this.cachedContent.write('=');
							// 写入参数值（URL 编码）
							this.cachedContent.write(URLEncoder.encode(value, requestEncoding).getBytes());
							if (valueIterator.hasNext()) {
								this.cachedContent.write('&');
							}
						}
					}
					if (nameIterator.hasNext()) {
						this.cachedContent.write('&');
					}
				}
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to write request parameters to cached content", ex);
		}
	}

	/**
	 * 返回当前缓存的内容字节数组。
	 * 注意：如果缓存内容超过限制，数组长度不会超过限制大小。
	 *
	 * @return 缓存的字节数组
	 */
	public byte[] getContentAsByteArray() {
		return this.cachedContent.toByteArray();
	}

	/**
	 * 处理内容缓存溢出的回调方法。
	 * 当读取的请求内容超过设定的 contentCacheLimit 时，会调用此方法。
	 * 子类可以重写此方法以执行额外操作（例如记录警告、丢弃后续内容等）。
	 * 默认实现为空。
	 *
	 * @param contentCacheLimit 缓存限制大小（字节）
	 */
	protected void handleContentOverflow(int contentCacheLimit) {
		// 默认空实现，子类可按需覆盖
	}

	/**
	 * 内部类：实现了 ServletInputStream，用于包装原始的输入流，
	 * 在读取数据的同时将数据写入缓存（ByteArrayOutputStream）。
	 * 支持字节读取、批量读取、readLine 等操作，并处理缓存溢出逻辑。
	 */
	private class ContentCachingInputStream extends ServletInputStream {

		/** 原始的 ServletInputStream */
		private final ServletInputStream is;

		/** 标志位，表示是否已经发生了缓存溢出（超过限制） */
		private boolean overflow = false;

		/**
		 * 构造器，包装原始输入流。
		 *
		 * @param is 原始 ServletInputStream
		 */
		public ContentCachingInputStream(ServletInputStream is) {
			this.is = is;
		}

		/**
		 * 读取一个字节。将读取的字节（如果未溢出）写入缓存。
		 *
		 * @return 读取的字节（0-255），如果到达流末尾则返回 -1
		 * @throws IOException 读取失败时抛出
		 */
		@Override
		public int read() throws IOException {
			int ch = this.is.read();
			if (ch != -1 && !this.overflow) {
				// 如果设置了缓存限制，并且当前缓存大小已经达到限制，则标记溢出并停止缓存
				if (ContentCachingRequestWrapper.this.contentCacheLimit != null &&
						ContentCachingRequestWrapper.this.cachedContent.size() == ContentCachingRequestWrapper.this.contentCacheLimit) {
					this.overflow = true;
					ContentCachingRequestWrapper.this.handleContentOverflow(ContentCachingRequestWrapper.this.contentCacheLimit);
				} else {
					ContentCachingRequestWrapper.this.cachedContent.write(ch);
				}
			}
			return ch;
		}

		/**
		 * 读取一批字节到目标数组 b 中。
		 *
		 * @param b 目标字节数组
		 * @return 实际读取的字节数，-1 表示结束
		 * @throws IOException 读取失败时抛出
		 */
		@Override
		public int read(byte[] b) throws IOException {
			int count = this.is.read(b);
			writeToCache(b, 0, count);
			return count;
		}

		/**
		 * 将读取的字节块写入缓存（如果未溢出且 count>0）。
		 *
		 * @param b     字节数组
		 * @param off   起始偏移量
		 * @param count 实际读取的字节数
		 */
		private void writeToCache(final byte[] b, final int off, int count) {
			if (!this.overflow && count > 0) {
				if (ContentCachingRequestWrapper.this.contentCacheLimit != null &&
						count + ContentCachingRequestWrapper.this.cachedContent.size() > ContentCachingRequestWrapper.this.contentCacheLimit) {
					// 本次读取会导致溢出：只写入能容纳的部分，然后标记溢出
					this.overflow = true;
					int remaining = ContentCachingRequestWrapper.this.contentCacheLimit - ContentCachingRequestWrapper.this.cachedContent.size();
					ContentCachingRequestWrapper.this.cachedContent.write(b, off, remaining);
					ContentCachingRequestWrapper.this.handleContentOverflow(ContentCachingRequestWrapper.this.contentCacheLimit);
					return;
				}
				ContentCachingRequestWrapper.this.cachedContent.write(b, off, count);
			}
		}

		/**
		 * 读取指定长度 len 的字节到数组 b 的 off 位置。
		 *
		 * @param b   目标数组
		 * @param off 起始偏移量
		 * @param len 最大读取长度
		 * @return 实际读取的字节数
		 * @throws IOException 读取失败时抛出
		 */
		@Override
		public int read(final byte[] b, final int off, final int len) throws IOException {
			int count = this.is.read(b, off, len);
			writeToCache(b, off, count);
			return count;
		}

		/**
		 * 读取一行数据。ServletInputStream 的 readLine 方法通常用于读取以 \n 结尾的行。
		 *
		 * @param b   目标数组
		 * @param off 起始偏移量
		 * @param len 最大长度
		 * @return 实际读取的字节数（不包含行终止符），-1 表示结束
		 * @throws IOException 读取失败时抛出
		 */
		@Override
		public int readLine(final byte[] b, final int off, final int len) throws IOException {
			int count = this.is.readLine(b, off, len);
			writeToCache(b, off, count);
			return count;
		}

		/**
		 * 判断是否所有数据都已读取完毕（非阻塞模式）。
		 *
		 * @return true 表示没有更多数据可读
		 */
		@Override
		public boolean isFinished() {
			return this.is.isFinished();
		}

		/**
		 * 判断是否可以进行非阻塞读取（ready）。
		 *
		 * @return true 表示可以立即读取数据而不阻塞
		 */
		@Override
		public boolean isReady() {
			return this.is.isReady();
		}

		/**
		 * 设置 ReadListener（用于异步非阻塞处理）。
		 *
		 * @param readListener 读取监听器
		 */
		@Override
		public void setReadListener(ReadListener readListener) {
			this.is.setReadListener(readListener);
		}
	}
}