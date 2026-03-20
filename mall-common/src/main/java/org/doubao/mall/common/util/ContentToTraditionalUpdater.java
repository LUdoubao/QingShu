package org.doubao.mall.common.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 将quote表的content字段内容赋值给content_traditional字段
 * 分批处理：每500条提交一次事务，保证效率和数据安全
 */
public class ContentToTraditionalUpdater {
	// 日志对象，方便查看运行状态
	private static final Logger logger = LoggerFactory.getLogger(ContentToTraditionalUpdater.class);

	// 数据库连接参数（请根据实际环境修改）
	private static final String DB_URL = "jdbc:mysql://*:3306/chinese_poetry?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
	private static final String DB_USER = "root";
	private static final String DB_PASSWORD = "Lg101799.";

	// 每批次处理数量（一次提交500条）
	private static final int BATCH_SIZE = 500;

	public static void main(String[] args) {
		Connection conn = null;
		try {
			// 1. 加载MySQL驱动（兼容8.x版本）
			Class.forName("com.mysql.cj.jdbc.Driver");

			// 2. 建立数据库连接，关闭自动提交（手动控制事务）
			conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			conn.setAutoCommit(false);
			logger.info("数据库连接成功，开始处理数据...");

			// 3. 获取总数据量（仅处理未逻辑删除的数据）
			int totalCount = getTotalDataCount(conn);
			if (totalCount == 0) {
				logger.info("无未删除的数据需要处理，程序结束");
				return;
			}
			logger.info("待处理的总数据量：{} 条", totalCount);

			// 4. 计算总批次，分批处理
			int totalBatches = (totalCount + BATCH_SIZE - 1) / BATCH_SIZE;
			logger.info("共需处理 {} 批次，每批次 {} 条", totalBatches, BATCH_SIZE);

			for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
				int offset = batchNum * BATCH_SIZE;
				logger.info("开始处理第 {} 批次，偏移量：{}", batchNum + 1, offset);

				// 5. 查询当前批次的id和content字段
				List<QuoteData> dataList = queryBatchData(conn, offset, BATCH_SIZE);
				if (dataList.isEmpty()) {
					logger.info("第 {} 批次无数据，跳过", batchNum + 1);
					continue;
				}

				// 6. 批量更新content_traditional字段
				int updateCount = batchUpdateData(conn, dataList);

				// 7. 提交当前批次事务
				conn.commit();
				logger.info("第 {} 批次处理完成，成功更新 {} 条数据", batchNum + 1, updateCount);
			}

			logger.info("所有数据处理完成！总计更新 {} 条数据", totalCount);

		} catch (ClassNotFoundException e) {
			logger.error("加载MySQL驱动失败，请检查依赖是否正确", e);
		} catch (SQLException e) {
			logger.error("数据库操作异常", e);
			// 发生异常时回滚事务
			if (conn != null) {
				try {
					conn.rollback();
					logger.info("事务已回滚，数据未修改");
				} catch (SQLException ex) {
					logger.error("事务回滚失败", ex);
				}
			}
		} finally {
			// 8. 关闭数据库连接
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
	 * 获取未逻辑删除的数据总条数
	 */
	private static int getTotalDataCount(Connection conn) throws SQLException {
		String countSql = "SELECT COUNT(*) FROM quote WHERE deleted = 0";
		try (PreparedStatement pstmt = conn.prepareStatement(countSql);
			 ResultSet rs = pstmt.executeQuery()) {
			if (rs.next()) {
				return rs.getInt(1);
			}
			return 0;
		}
	}

	/**
	 * 分页查询当前批次的id和content字段
	 */
	private static List<QuoteData> queryBatchData(Connection conn, int offset, int limit) throws SQLException {
		String querySql = "SELECT id, content FROM quote WHERE deleted = 0 LIMIT ? OFFSET ?";
		List<QuoteData> dataList = new ArrayList<>();

		try (PreparedStatement pstmt = conn.prepareStatement(querySql)) {
			pstmt.setInt(1, limit);
			pstmt.setInt(2, offset);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					QuoteData data = new QuoteData();
					data.setId(rs.getLong("id"));
					data.setContent(rs.getString("content")); // 获取content字段值
					dataList.add(data);
				}
			}
		}
		return dataList;
	}

	/**
	 * 批量更新content_traditional字段（将content的值赋值给它）
	 */
	private static int batchUpdateData(Connection conn, List<QuoteData> dataList) throws SQLException {
		String updateSql = "UPDATE quote SET content_traditional = ? WHERE id = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
			// 批量添加更新语句
			for (QuoteData data : dataList) {
				pstmt.setString(1, data.getContent()); // content值赋值给content_traditional
				pstmt.setLong(2, data.getId());
				pstmt.addBatch();
			}
			// 执行批量更新
			int[] updateCounts = pstmt.executeBatch();
			return updateCounts.length; // 返回本次更新的条数
		}
	}

	/**
	 * 封装查询结果的实体类（仅包含id和content）
	 */
	static class QuoteData {
		private long id;
		private String content;

		// Getter & Setter
		public long getId() {
			return id;
		}

		public void setId(long id) {
			this.id = id;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	}
}