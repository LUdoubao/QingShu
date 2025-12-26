package org.doubao.quote.service.duplicate.check;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class MongoConfig {

	@Value("${spring.data.mongodb.uri:}")
	private String mongoUri;

	@Value("${spring.data.mongodb.host:localhost}")
	private String host;

	@Value("${spring.data.mongodb.port:27017}")
	private int port;

	@Value("${spring.data.mongodb.database:quote_db}")
	private String database;

	@Value("${spring.data.mongodb.username:}")
	private String username;

	@Value("${spring.data.mongodb.password:}")
	private String password;

	@Value("${spring.data.mongodb.authentication-database:admin}")
	private String authDatabase;

	@Bean
	public MongoClient mongoClient() throws UnsupportedEncodingException {
		// 优先使用URI连接
		if (!mongoUri.isEmpty()) {
			return MongoClients.create(mongoUri);
		}

		// 使用分项配置创建连接
		String connectionString;
		if (!username.isEmpty() && !password.isEmpty()) {
			// 对用户名和密码进行 URL 编码
			String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.toString());
			String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8.toString());

			connectionString = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
					encodedUsername, encodedPassword, host, port, database, authDatabase);
		} else {
			connectionString = String.format("mongodb://%s:%d/%s", host, port, database);
		}

		return MongoClients.create(connectionString);
	}

	@Bean
	public MongoTemplate mongoTemplate() throws UnsupportedEncodingException {
		MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), database);
		createIndexes(mongoTemplate);
		return mongoTemplate;
	}

	private void createIndexes(MongoTemplate mongoTemplate) {
		IndexOperations indexOps = mongoTemplate.indexOps("quote_db");

		// 创建单字段索引
		indexOps.ensureIndex(new Index().on("contentSimHash", Sort.Direction.ASC));
		indexOps.ensureIndex(new Index().on("author", Sort.Direction.ASC));
		indexOps.ensureIndex(new Index().on("isOriginal", Sort.Direction.ASC));

		// 创建复合索引
		indexOps.ensureIndex(new Index().on("contentSimHash", Sort.Direction.ASC)
				.on("author", Sort.Direction.ASC)
				.on("isOriginal", Sort.Direction.ASC));

		// 创建文本索引
		indexOps.ensureIndex(new Index().on("content",  Sort.Direction.ASC));
	}
}

