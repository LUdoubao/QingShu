package org.doubao.mall.common.util;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将quote表中的author、title、content字段从繁体转换为简体并更新
 */
public class TraditionalToSimplifiedUpdater {
	// 日志对象，方便查看运行状态
	private static final Logger logger = LoggerFactory.getLogger(TraditionalToSimplifiedUpdater.class);

	// 数据库连接参数（请根据你的环境修改）
	private static final String DB_URL = "jdbc:mysql://*:3306/qingshu?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "Lg101799.";

	// 分批处理大小（避免一次性查询/更新过多数据导致内存溢出）
	private static final int BATCH_SIZE = 1000;
	// 异体字/生僻字映射表（可根据需求扩展）
	private static final Map<String, String> SPECIAL_CHAR_MAP = new HashMap<String, String>() {{
		put("妬", "妒");
		put("裏", "里");
		put("衹", "只");
		put("祇", "只");
		put("丼", "井");
		put("麪", "面");
		put("醜", "丑");
		put("牀", "床");
		put("鞏", "巩");
		put("祕", "秘");
		put("傑", "杰");
		put("捲", "卷");
		put("齒", "齿");
		put("鬚", "须");
		put("髮", "发");
		// 可继续添加更多异体字映射
	}};


	public static void main(String[] args) {
		Connection conn = null;
		try {
			// 1. 建立数据库连接
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			// 关闭自动提交，可手动控制事务（可选，批量更新建议开启）
			conn.setAutoCommit(false);

			// 2. 获取总数据量，用于计算分页次数
			int totalCount = getTotalCount(conn);
			logger.info("待处理的总数据量：{}", totalCount);

			if (totalCount == 0) {
				logger.info("无数据需要处理，程序结束");
				return;
			}

			// 3. 分批查询并更新数据
			int totalPages = (totalCount + BATCH_SIZE - 1) / BATCH_SIZE;
			for (int page = 0; page < totalPages; page++) {
				int offset = page * BATCH_SIZE;
				logger.info("开始处理第 {} 页，偏移量：{}", page + 1, offset);

				// 查询当前页数据
				List<QuoteData> quoteList = queryQuoteData(conn, offset, BATCH_SIZE);
				if (quoteList.isEmpty()) {
					logger.info("第 {} 页无数据，跳过", page + 1);
					continue;
				}

				// 批量更新转换后的数据
				batchUpdateQuoteData(conn, quoteList);

				// 提交当前批次的事务
				conn.commit();
				logger.info("第 {} 页处理完成，共处理 {} 条数据", page + 1, quoteList.size());
			}

			logger.info("所有数据转换并更新完成！");

		} catch (SQLException e) {
			logger.error("数据库操作异常", e);
			// 发生异常时回滚事务
			if (conn != null) {
				try {
					conn.rollback();
					logger.info("事务已回滚");
				} catch (SQLException ex) {
					logger.error("事务回滚失败", ex);
				}
			}
		} finally {
			// 关闭数据库连接
			if (conn != null) {
				try {
					conn.close();
					logger.info("数据库连接已关闭");
				} catch (SQLException e) {
					logger.error("关闭数据库连接失败", e);
				}
			}
		}
	}

	/**
	 * 获取quote表的总数据量（仅统计未逻辑删除的数据）
	 */
	private static int getTotalCount(Connection conn) throws SQLException {
		String sql = "SELECT COUNT(*) FROM quote WHERE deleted = 0";
		try (PreparedStatement pstmt = conn.prepareStatement(sql);
			 ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		}
	}

	/**
	 * 分页查询quote表数据（仅查询需要转换的字段）
	 */
	private static List<QuoteData> queryQuoteData(Connection conn, int offset, int limit) throws SQLException {
		String sql = "SELECT id, author, title, content FROM quote WHERE deleted = 0 LIMIT ? OFFSET ?";
		List<QuoteData> quoteList = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, limit);
			pstmt.setInt(2, offset);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					QuoteData data = new QuoteData();
					data.setId(rs.getLong("id"));
					// 处理NULL值，避免转换时空指针
					data.setAuthor(rs.getString("author") == null ? "" : rs.getString("author"));
					data.setTitle(rs.getString("title") == null ? "" : rs.getString("title"));
					data.setContent(rs.getString("content") == null ? "" : rs.getString("content"));
					quoteList.add(data);
				}
			}
		}
		return quoteList;
	}
	/**
	 * 核心：繁体转简体 + 异体字修正
	 */
	private static String convertToSimplified(String traditionalText) {
		if (traditionalText == null || traditionalText.isEmpty()) {
			return traditionalText;
		}
		// 第一步：基础繁转简
		String simplified = ZhConverterUtil.toSimple(traditionalText);
		// 第二步：替换异体字/生僻字
		for (Map.Entry<String, String> entry : SPECIAL_CHAR_MAP.entrySet()) {
			simplified = simplified.replace(entry.getKey(), entry.getValue());
		}
		return simplified;
	}

	/**
	 * 批量更新转换后的简体字数据
	 */
	private static void batchUpdateQuoteData(Connection conn, List<QuoteData> quoteList) throws SQLException {
		String updateSql = "UPDATE quote SET author = ?, title = ?, content = ? WHERE id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
			for (QuoteData data : quoteList) {
				// 使用新增的转换方法（含异体字修正）
				String simplifiedAuthor = convertToSimplified(data.getAuthor());
				String simplifiedTitle = convertToSimplified(data.getTitle());
				String simplifiedContent = convertToSimplified(data.getContent());

				// 设置更新参数
				pstmt.setString(1, simplifiedAuthor.isEmpty() ? "" : simplifiedAuthor);
				pstmt.setString(2, simplifiedTitle.isEmpty() ? "" : simplifiedTitle);
				pstmt.setString(3, simplifiedContent.isEmpty() ? "" : simplifiedContent);
				pstmt.setLong(4, data.getId());

				// 添加到批量更新队列
				pstmt.addBatch();
			}
			// 执行批量更新
			int[] updateCounts = pstmt.executeBatch();
			logger.info("本次批量更新影响行数：{}", updateCounts.length);
		}
	}

	/**
	 * 封装quote表的查询数据（仅包含需要转换的字段）
	 */
	static class QuoteData {
		private long id;
		private String author;
		private String title;
		private String content;

		// Getter 和 Setter 方法
		public long getId() { return id; }
		public void setId(long id) { this.id = id; }
		public String getAuthor() { return author; }
		public void setAuthor(String author) { this.author = author; }
		public String getTitle() { return title; }
		public void setTitle(String title) { this.title = title; }

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}
}