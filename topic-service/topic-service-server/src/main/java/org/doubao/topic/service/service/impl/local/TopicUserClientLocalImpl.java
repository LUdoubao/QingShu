package org.doubao.topic.service.service.impl.local;

import org.doubao.mall.common.condition.MonolithMode;
import org.doubao.mall.common.entity.Result;
import org.doubao.mall.common.entity.UserInfoDes;
import org.doubao.topic.service.feign.UserClient;
import org.doubao.user.service.service.core.UserService;
import org.doubao.user.service.service.relation.RelationService;
import org.doubao.user.service.service.relation.UserPrivacyService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@MonolithMode
public class TopicUserClientLocalImpl implements UserClient {

    @Resource
    private UserService userService;
    
    @Resource
    private RelationService relationService;
    
    @Resource
    private UserPrivacyService userPrivacyService;

}