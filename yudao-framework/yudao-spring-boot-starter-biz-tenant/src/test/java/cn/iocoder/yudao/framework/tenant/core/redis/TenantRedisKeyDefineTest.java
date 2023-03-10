package cn.iocoder.yudao.framework.tenant.core.redis;

import cn.iocoder.yudao.framework.redis.core.RedisKeyDefine;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TenantRedisKeyDefineTest {

    @Test
    public void testFormatKey() {
        String tenantId = "30";
        TenantContextHolder.setTenantId(tenantId);
        // 准备参数
        TenantRedisKeyDefine define = new TenantRedisKeyDefine("", "user:%d:%d", RedisKeyDefine.KeyTypeEnum.HASH,
                Object.class, RedisKeyDefine.TimeoutTypeEnum.FIXED);
        String userId = "10";
        Integer userType = 1;

        // 调用
        String key = define.formatKey(userId, userType);
        // 断言
        assertEquals("user:10:1:30", key);
    }

}
