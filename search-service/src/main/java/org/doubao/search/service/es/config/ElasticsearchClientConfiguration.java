// package org.doubao.search.service.es.config;
//
// import org.apache.http.HttpHost;
// import org.elasticsearch.client.RestClient;
// import org.elasticsearch.client.RestClientBuilder;
// import org.elasticsearch.client.RestHighLevelClient;
// import org.springframework.boot.context.properties.ConfigurationProperties;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.apache.http.auth.AuthScope;
// import org.apache.http.auth.UsernamePasswordCredentials;
// import org.apache.http.client.CredentialsProvider;
// import org.apache.http.impl.client.BasicCredentialsProvider;
// import org.springframework.util.StringUtils;
// import javax.annotation.PreDestroy;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.List;
//
// /**
//  * Elasticsearch RestHighLevelClient 配置类
//  * 提供弹性的Elasticsearch客户端配置，支持多节点、认证和超时设置
//  */
// @Configuration
// @ConfigurationProperties(prefix = "spring.elasticsearch.rest")
// public class ElasticsearchClientConfiguration {
//
//     /**
//      * Elasticsearch集群节点地址，格式为host:port，多个地址用逗号分隔
//      */
//     private String uris;
//
//     /**
//      * 访问Elasticsearch的用户名
//      */
//     private String username;
//
//     /**
//      * 访问Elasticsearch的密码
//      */
//     private String password;
//
//     /**
//      * 连接超时时间(毫秒)
//      */
//     private int connectionTimeout = 5000;
//
//     /**
//      *  socket超时时间(毫秒)
//      */
//     private int socketTimeout = 3000;
//
//     /**
//      * RestHighLevelClient实例
//      */
//     private RestHighLevelClient restHighLevelClient;
//
//     /**
//      * 创建RestHighLevelClient实例
//      * 支持多节点配置、身份验证和超时设置
//      *
//      * @return RestHighLevelClient实例
//      */
//     @Bean
//     public RestHighLevelClient elasticsearchClient() {
//         // 解析集群节点地址
//         List<HttpHost> httpHosts = parseHttpHosts();
//
//         // 构建RestClient builder
//         RestClient.builder(httpHosts.toArray(new HttpHost[0]))
//                 .setRequestConfigCallback(requestConfigBuilder -> {
//                     // 配置超时设置
//                     requestConfigBuilder.setConnectTimeout(connectionTimeout);
//                     requestConfigBuilder.setSocketTimeout(socketTimeout);
//                     return requestConfigBuilder;
//                 });
//
//         // 如果配置了用户名和密码，添加身份验证
//         RestClientBuilder builder = configureAuth(httpHosts);
//
//         // 创建客户端实例
//         restHighLevelClient = new RestHighLevelClient(builder);
//         return restHighLevelClient;
//     }
//
//     /**
//      * 解析URI字符串为HttpHost列表
//      *
//      * @return HttpHost列表
//      */
//     private List<HttpHost> parseHttpHosts() {
//         List<HttpHost> httpHosts = new ArrayList<>();
//
//         if (StringUtils.hasText(uris)) {
//             String[] uriArray = uris.split(",");
//             for (String uri : uriArray) {
//                 String trimmedUri = uri.trim();
//                 String[] hostAndPort = trimmedUri.split(":");
//
//                 String host = hostAndPort[0];
//                 int port = hostAndPort.length > 1 ? Integer.parseInt(hostAndPort[1]) : 9200;
//
//                 httpHosts.add(new HttpHost(host, port, "http"));
//             }
//         } else {
//             // 默认节点
//             httpHosts.add(new HttpHost("localhost", 9200, "http"));
//         }
//
//         return httpHosts;
//     }
//
//     /**
//      * 配置身份验证（如果有用户名和密码）
//      *
//      * @param httpHosts 节点列表
//      * @return 配置好的RestClientBuilder
//      */
//     private RestClientBuilder configureAuth(List<HttpHost> httpHosts) {
//         RestClientBuilder builder = RestClient.builder(httpHosts.toArray(new HttpHost[0]));
//
//         if (StringUtils.hasText(username) && StringUtils.hasText(password)) {
//             final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//             credentialsProvider.setCredentials(
//                     AuthScope.ANY,
//                     new UsernamePasswordCredentials(username, password)
//             );
//
//             builder.setHttpClientConfigCallback(httpClientBuilder ->
//                     httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
//             );
//         }
//
//         return builder;
//     }
//
//     /**
//      * 应用关闭时关闭客户端，释放资源
//      *
//      * @throws IOException 关闭客户端可能抛出的异常
//      */
//     @PreDestroy
//     public void close() throws IOException {
//         if (restHighLevelClient != null) {
//             restHighLevelClient.close();
//         }
//     }
//
//     // getter和setter方法
//     public String getUris() {
//         return uris;
//     }
//
//     public void setUris(String uris) {
//         this.uris = uris;
//     }
//
//     public String getUsername() {
//         return username;
//     }
//
//     public void setUsername(String username) {
//         this.username = username;
//     }
//
//     public String getPassword() {
//         return password;
//     }
//
//     public void setPassword(String password) {
//         this.password = password;
//     }
//
//     public int getConnectionTimeout() {
//         return connectionTimeout;
//     }
//
//     public void setConnectionTimeout(int connectionTimeout) {
//         this.connectionTimeout = connectionTimeout;
//     }
//
//     public int getSocketTimeout() {
//         return socketTimeout;
//     }
//
//     public void setSocketTimeout(int socketTimeout) {
//         this.socketTimeout = socketTimeout;
//     }
// }
//
