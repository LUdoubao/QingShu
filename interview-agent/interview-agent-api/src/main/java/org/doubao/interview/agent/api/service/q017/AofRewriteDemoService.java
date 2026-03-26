package org.doubao.interview.agent.api.service.q017;

import org.doubao.interview.agent.api.dto.q017.AofRewriteRequest;
import org.doubao.interview.agent.api.dto.q017.AofRewriteResponse;

/** 问题017：AOF rewrite 服务接口。 */
public interface AofRewriteDemoService {
    AofRewriteResponse rewrite(AofRewriteRequest request);
}
