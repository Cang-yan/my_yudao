package cn.iocoder.yudao.module.system.service.logger;

import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.monitor.TracerUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.RandomUtils;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.logger.OperateLogMapper;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Import({OperateLogServiceImpl.class})
public class OperateLogServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OperateLogService operateLogServiceImpl;

    @Resource
    private OperateLogMapper operateLogMapper;

    @MockBean
    private AdminUserService userService;

    @Test
    public void testCreateOperateLogAsync() {
        String traceId = TracerUtils.getTraceId();
        OperateLogCreateReqDTO reqVO = RandomUtils.randomPojo(OperateLogCreateReqDTO.class, o -> {
            o.setTraceId(traceId);
            o.setUserId(randomLongId().toString());
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLongId()).build());
        });

        // ??????service??????
        operateLogServiceImpl.createOperateLog(reqVO);
        // ????????????????????????
        OperateLogDO sysOperateLogDO = operateLogMapper.selectOne("trace_id", traceId);
        assertPojoEquals(reqVO, sysOperateLogDO);
    }

    @Test
    public void testGetOperateLogPage() {
        // ??????????????????
        // ???????????????
        AdminUserDO user = RandomUtils.randomPojo(AdminUserDO.class, o -> {
            o.setNickname("wangkai");
            o.setSex(SexEnum.MALE.getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(userService.getUsersByNickname("wangkai")).thenReturn(Collections.singletonList(user));
        String userId = user.getId();
        // ??????????????????
        OperateLogDO sysOperateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(userId);
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setModule("order");
            o.setType(OperateTypeEnum.CREATE.getType());
            o.setStartTime(buildTime(2021, 3, 6));
            o.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLongId()).build());
        });
        operateLogMapper.insert(sysOperateLogDO);

        // ?????????????????????????????????
        // ?????? userId
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setUserId(userId + 1)));
        // module ??????
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setModule("user")));
        // type ??????
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setType(OperateTypeEnum.IMPORT.getType())));
        // createTime ??????
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setStartTime(buildTime(2021, 2, 6))));
        // resultCode ??????
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setResultCode(BAD_REQUEST.getCode())));

        // ??????????????????
        OperateLogPageReqVO reqVO = new OperateLogPageReqVO();
        reqVO.setUserNickname("wangkai");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setStartTime((new Date[]{buildTime(2021, 3, 5),buildTime(2021, 3, 7)}));
        reqVO.setSuccess(true);

        // ??????service??????
        PageResult<OperateLogDO> pageResult = operateLogServiceImpl.getOperateLogPage(reqVO);
        // ??????????????????????????????????????????
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(sysOperateLogDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetOperateLogs() {
        // ??????????????????
        // ???????????????
        AdminUserDO user = RandomUtils.randomPojo(AdminUserDO.class, o -> {
            o.setNickname("wangkai");
            o.setSex(SexEnum.MALE.getSex());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(userService.getUsersByNickname("wangkai")).thenReturn(Collections.singletonList(user));
        String userId = user.getId();
        // ??????????????????
        OperateLogDO sysOperateLogDO = RandomUtils.randomPojo(OperateLogDO.class, o -> {
            o.setUserId(userId);
            o.setUserType(randomEle(UserTypeEnum.values()).getValue());
            o.setModule("order");
            o.setType(OperateTypeEnum.CREATE.getType());
            o.setStartTime(buildTime(2021, 3, 6));
            o.setResultCode(GlobalErrorCodeConstants.SUCCESS.getCode());
            o.setExts(MapUtil.<String, Object>builder("orderId", randomLongId()).build());
        });
        operateLogMapper.insert(sysOperateLogDO);

        // ?????????????????????????????????
        // ?????? userId
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setUserId(userId + 1)));
        // module ??????
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setModule("user")));
        // type ??????
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setType(OperateTypeEnum.IMPORT.getType())));
        // createTime ??????
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setStartTime(buildTime(2021, 2, 6))));
        // resultCode ??????
        operateLogMapper.insert(ObjectUtils.cloneIgnoreId(sysOperateLogDO, logDO -> logDO.setResultCode(BAD_REQUEST.getCode())));

        // ??????????????????
        OperateLogExportReqVO reqVO = new OperateLogExportReqVO();
        reqVO.setUserNickname("wangkai");
        reqVO.setModule("order");
        reqVO.setType(OperateTypeEnum.CREATE.getType());
        reqVO.setStartTime((new Date[]{buildTime(2021, 3, 5),buildTime(2021, 3, 7)}));
        reqVO.setSuccess(true);

        // ?????? service ??????
        List<OperateLogDO> list = operateLogServiceImpl.getOperateLogs(reqVO);
        // ??????????????????????????????????????????
        assertEquals(1, list.size());
        assertPojoEquals(sysOperateLogDO, list.get(0));
    }

}
