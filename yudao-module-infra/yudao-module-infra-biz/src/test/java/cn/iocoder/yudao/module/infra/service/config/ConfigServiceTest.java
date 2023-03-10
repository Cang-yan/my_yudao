package cn.iocoder.yudao.module.infra.service.config;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigCreateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigExportReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigPageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.config.vo.ConfigUpdateReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.config.ConfigDO;
import cn.iocoder.yudao.module.infra.dal.mysql.config.ConfigMapper;
import cn.iocoder.yudao.module.infra.enums.config.ConfigTypeEnum;
import cn.iocoder.yudao.module.infra.mq.producer.config.ConfigProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Import(ConfigServiceImpl.class)
public class ConfigServiceTest extends BaseDbUnitTest {

    @Resource
    private ConfigServiceImpl configService;

    @Resource
    private ConfigMapper configMapper;
    @MockBean
    private ConfigProducer configProducer;

    @Test
    public void testCreateConfig_success() {
        // ????????????
        ConfigCreateReqVO reqVO = randomPojo(ConfigCreateReqVO.class);

        // ??????
        String configId = configService.createConfig(reqVO);
        // ??????
        assertNotNull(configId);
        // ?????????????????????????????????
        ConfigDO config = configMapper.selectById(configId);
        assertPojoEquals(reqVO, config);
        Assertions.assertEquals(ConfigTypeEnum.CUSTOM.getType(), config.getType());
        // ????????????
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    @Test
    public void testUpdateConfig_success() {
        // mock ??????
        ConfigDO dbConfig = randomConfigDO();
        configMapper.insert(dbConfig);// @Sql: ?????????????????????????????????
        // ????????????
        ConfigUpdateReqVO reqVO = randomPojo(ConfigUpdateReqVO.class, o -> {
            o.setId(dbConfig.getId()); // ??????????????? ID
        });

        // ??????
        configService.updateConfig(reqVO);
        // ????????????????????????
        ConfigDO config = configMapper.selectById(reqVO.getId()); // ???????????????
        assertPojoEquals(reqVO, config);
        // ????????????
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    @Test
    public void testDeleteConfig_success() {
        // mock ??????
        ConfigDO dbConfig = randomConfigDO(o -> {
            o.setType(ConfigTypeEnum.CUSTOM.getType()); // ???????????? CUSTOM ??????
        });
        configMapper.insert(dbConfig);// @Sql: ?????????????????????????????????
        // ????????????
        String id = dbConfig.getId();

        // ??????
        configService.deleteConfig(id);
        // ????????????????????????
        assertNull(configMapper.selectById(id));
        // ????????????
        verify(configProducer, times(1)).sendConfigRefreshMessage();
    }

    @Test
    public void testDeleteConfig_canNotDeleteSystemType() {
        // mock ??????
        ConfigDO dbConfig = randomConfigDO(o -> {
            o.setType(ConfigTypeEnum.SYSTEM.getType()); // SYSTEM ???????????????
        });
        configMapper.insert(dbConfig);// @Sql: ?????????????????????????????????
        // ????????????
        String id = dbConfig.getId();

        // ??????, ???????????????
        assertServiceException(() -> configService.deleteConfig(id), CONFIG_CAN_NOT_DELETE_SYSTEM_TYPE);
    }

    @Test
    public void testCheckConfigExists_success() {
        // mock ??????
        ConfigDO dbConfigDO = randomConfigDO();
        configMapper.insert(dbConfigDO);// @Sql: ?????????????????????????????????

        // ????????????
        configService.checkConfigExists(dbConfigDO.getId());
    }

    @Test
    public void testCheckConfigExist_notExists() {
        assertServiceException(() -> configService.checkConfigExists(randomLongId().toString()), CONFIG_NOT_EXISTS);
    }

    @Test
    public void testCheckConfigKeyUnique_success() {
        // ???????????????
        configService.checkConfigKeyUnique(randomLongId().toString(), randomString());
    }

    @Test
    public void testCheckConfigKeyUnique_keyDuplicateForCreate() {
        // ????????????
        String key = randomString();
        // mock ??????
        configMapper.insert(randomConfigDO(o -> o.setConfigKey(key)));

        // ?????????????????????
        assertServiceException(() -> configService.checkConfigKeyUnique(null, key),
                CONFIG_KEY_DUPLICATE);
    }

    @Test
    public void testCheckConfigKeyUnique_keyDuplicateForUpdate() {
        // ????????????
        String id = randomLongId().toString();
        String key = randomString();
        // mock ??????
        configMapper.insert(randomConfigDO(o -> o.setConfigKey(key)));

        // ?????????????????????
        assertServiceException(() -> configService.checkConfigKeyUnique(id, key),
                CONFIG_KEY_DUPLICATE);
    }

    @Test
    public void testGetConfigPage() {
        // mock ??????
        ConfigDO dbConfig = randomConfigDO(o -> { // ???????????????
            o.setName("??????");
            o.setConfigKey("yunai");
            o.setType(ConfigTypeEnum.SYSTEM.getType());
            o.setCreateTime(buildTime(2021, 2, 1));
        });
        configMapper.insert(dbConfig);
        // ?????? name ?????????
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setName("??????")));
        // ?????? key ?????????
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setConfigKey("tudou")));
        // ?????? type ?????????
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setType(ConfigTypeEnum.CUSTOM.getType())));
        // ?????? createTime ?????????
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setCreateTime(buildTime(2021, 1, 1))));
        // ????????????
        ConfigPageReqVO reqVO = new ConfigPageReqVO();
        reqVO.setName("???");
        reqVO.setKey("nai");
        reqVO.setType(ConfigTypeEnum.SYSTEM.getType());
        reqVO.setCreateTime((new Date[]{buildTime(2021, 1, 15),buildTime(2021, 2, 15)}));

        // ??????
        PageResult<ConfigDO> pageResult = configService.getConfigPage(reqVO);
        // ??????
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbConfig, pageResult.getList().get(0));
    }

    @Test
    public void testGetConfigList() {
        // mock ??????
        ConfigDO dbConfig = randomConfigDO(o -> { // ???????????????
            o.setName("??????");
            o.setConfigKey("yunai");
            o.setType(ConfigTypeEnum.SYSTEM.getType());
            o.setCreateTime(buildTime(2021, 2, 1));
        });
        configMapper.insert(dbConfig);
        // ?????? name ?????????
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setName("??????")));
        // ?????? key ?????????
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setConfigKey("tudou")));
        // ?????? type ?????????
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setType(ConfigTypeEnum.CUSTOM.getType())));
        // ?????? createTime ?????????
        configMapper.insert(ObjectUtils.cloneIgnoreId(dbConfig, o -> o.setCreateTime(buildTime(2021, 1, 1))));
        // ????????????
        ConfigExportReqVO reqVO = new ConfigExportReqVO();
        reqVO.setName("???");
        reqVO.setKey("nai");
        reqVO.setType(ConfigTypeEnum.SYSTEM.getType());
        reqVO.setCreateTime((new Date[]{buildTime(2021, 1, 15),buildTime(2021, 2, 15)}));

        // ??????
        List<ConfigDO> list = configService.getConfigList(reqVO);
        // ??????
        assertEquals(1, list.size());
        assertPojoEquals(dbConfig, list.get(0));
    }

    @Test
    public void testGetConfigByKey() {
        // mock ??????
        ConfigDO dbConfig = randomConfigDO();
        configMapper.insert(dbConfig);// @Sql: ?????????????????????????????????
        // ????????????
        String key = dbConfig.getConfigKey();

        // ??????
        ConfigDO config = configService.getConfigByKey(key);
        // ??????
        assertNotNull(config);
        assertPojoEquals(dbConfig, config);
    }

    // ========== ???????????? ==========

    @SafeVarargs
    private static ConfigDO randomConfigDO(Consumer<ConfigDO>... consumers) {
        Consumer<ConfigDO> consumer = (o) -> {
            o.setType(randomEle(ConfigTypeEnum.values()).getType()); // ?????? key ?????????
        };
        return RandomUtils.randomPojo(ConfigDO.class, ArrayUtils.append(consumer, consumers));
    }

}
