package org.doubao.recommend.service.domain;

import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.mall.common.util.DoubaoUtils;
import org.doubao.mall.common.vo.TagVo;
import org.doubao.mall.common.vo.TopicNameVo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 内容特征对象
 * 用于描述推荐系统中内容的各项特征属性，包括基本信息、分类信息、评分信息等
 * 这些特征将用于推荐算法的召回、排序等阶段
 */
public class ContentFeature {
    /**
     * 内容 ID
     * 唯一标识一个内容的标识符
     */
    private Long contentId;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 内容正文
     */
    private String content;
    
    /**
     * 作者名称
     */
    private String author;
    
    /**
     * 内容来源
     * 标识内容的出处或来源渠道
     */
    private String source;
    
    /**
     * 朝代信息
     * 主要用于古诗词等内容类型
     */
    private String dynasty;
    
    /**
     * 诗词分类
     * 用于古诗词的分类，如：唐诗、宋词等
     */
    private String poetryCategory;
    
    /**
     * 作者 ID
     * 关联到作者的 unique identifier
     */
    private Long authorId;

    /**
     * 标签名称列表
     */
    private List<TagVo> tagVos;

    /**
     * 话题名称列表
     */
    private List<TopicNameVo> topicNameVos;
    
    /**
     * 创建时间
     * 内容发布的时间戳
     */
    private LocalDateTime createdTime;
    /**
     * 原创
     */
    private int original;

    /**
     * 用户信息
     */
    private UserInfoDes userInfo;

    /**
     * 是否关注
     */
    private boolean follow;

    /**
     * 热度分数，默认值为 0.0
     * 基于内容的浏览量、互动量等计算的实时热度
     */
    private Double hotScore = 0.0;
    
    /**
     * 质量分数，默认值为 0.0
     * 基于内容完整性、规范性等评估的质量得分
     */
    private Double qualityScore = 0.0;
    
    /**
     * 新鲜度分数，默认值为 0.0
     * 基于内容发布时间计算的新鲜度得分，越新分数越高
     */
    private Double freshnessScore = 0.0;
    
    /**
     * 匹配分数，默认值为 0.0
     * 用于评估内容与用户兴趣的匹配程度
     */
    private Double matchScore = 0.0;

    public UserInfoDes getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoDes userInfo) {
        this.userInfo = userInfo;
    }

    public boolean isFollow() {
        return follow;
    }

    public void setFollow(boolean follow) {
        this.follow = follow;
    }

    public int getOriginal() {
        return original;
    }

    public void setOriginal(int original) {
        this.original = original;
    }

    /**
     * 获取内容正文
     * @return 内容正文
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置内容正文
     * @param content 内容正文
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 获取内容标题
     * @return 内容标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置内容标题
     * @param title 内容标题
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取内容 ID
     * @return 内容 ID
     */
    public Long getContentId() {
        return contentId;
    }

    /**
     * 设置内容 ID
     * @param contentId 内容 ID
     */
    public void setContentId(Long contentId) {
        this.contentId = contentId;
    }

    /**
     * 获取作者名称
     * @return 作者名称
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 设置作者名称
     * @param author 作者名称
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 获取内容来源
     * @return 内容来源
     */
    public String getSource() {
        return source;
    }

    /**
     * 设置内容来源
     * @param source 内容来源
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * 获取朝代信息
     * @return 朝代信息
     */
    public String getDynasty() {
        return dynasty;
    }

    /**
     * 设置朝代信息
     * @param dynasty 朝代信息
     */
    public void setDynasty(String dynasty) {
        this.dynasty = dynasty;
    }

    /**
     * 获取诗词分类
     * @return 诗词分类
     */
    public String getPoetryCategory() {
        return poetryCategory;
    }

    /**
     * 设置诗词分类
     * @param poetryCategory 诗词分类
     */
    public void setPoetryCategory(String poetryCategory) {
        this.poetryCategory = poetryCategory;
    }

    /**
     * 获取作者 ID
     * @return 作者 ID
     */
    public Long getAuthorId() {
        return authorId;
    }

    /**
     * 设置作者 ID
     * @param authorId 作者 ID
     */
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public List<TagVo> getTagVos() {
        return tagVos;
    }

    public void setTagVos(List<TagVo> tagVos) {
        this.tagVos = tagVos;
    }

    public List<TopicNameVo> getTopicNameVos() {
        return topicNameVos;
    }

    public void setTopicNameVos(List<TopicNameVo> topicNameVos) {
        this.topicNameVos = topicNameVos;
    }

    /**
     * 获取创建时间
     * @return 创建时间
     */
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    /**
     * 设置创建时间
     * @param createdTime 创建时间
     */
    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * 获取热度分数
     * @return 热度分数
     */
    public Double getHotScore() {
        return hotScore;
    }

    /**
     * 设置热度分数
     * @param hotScore 热度分数
     */
    public void setHotScore(Double hotScore) {
        this.hotScore = hotScore;
    }

    /**
     * 获取新鲜度分数
     * @return 新鲜度分数
     */
    public Double getFreshnessScore() {
        return freshnessScore;
    }

    /**
     * 设置新鲜度分数
     * @param freshnessScore 新鲜度分数
     */
    public void setFreshnessScore(Double freshnessScore) {
        this.freshnessScore = freshnessScore;
    }

    /**
     * 获取质量分数
     * @return 质量分数
     */
    public Double getQualityScore() {
        return qualityScore;
    }

    /**
     * 设置质量分数
     * @param qualityScore 质量分数
     */
    public void setQualityScore(Double qualityScore) {
        this.qualityScore = qualityScore;
    }

    /**
     * 获取匹配分数
     * @return 匹配分数
     */
    public Double getMatchScore() {
        return matchScore;
    }

    /**
     * 设置匹配分数
     * @param matchScore 匹配分数
     */
    public void setMatchScore(Double matchScore) {
        this.matchScore = matchScore;
    }


    public List<String> tagList() {
        if (DoubaoUtils.isEmpty(tagVos)) {
            return Collections.emptyList();
        }
        return tagVos.stream().map(TagVo::getName).collect(Collectors.toList());
    }

    public List<Long> topicIdList() {
        if (DoubaoUtils.isEmpty(topicNameVos)) {
            return Collections.emptyList();
        }
        return topicNameVos.stream().map(TopicNameVo::getId).collect(Collectors.toList());
    }
}
