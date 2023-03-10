package cn.iocoder.yudao.module.pay.service.refund;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.date.DateUtils;
import cn.iocoder.yudao.framework.pay.core.client.PayClientFactory;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundExportReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.refund.vo.PayRefundPageReqVO;
import cn.iocoder.yudao.module.pay.dal.dataobject.refund.PayRefundDO;
import cn.iocoder.yudao.module.pay.dal.mysql.refund.PayRefundMapper;
import cn.iocoder.yudao.module.pay.enums.order.PayOrderNotifyStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundStatusEnum;
import cn.iocoder.yudao.module.pay.enums.refund.PayRefundTypeEnum;
import cn.iocoder.yudao.module.pay.service.merchant.PayAppService;
import cn.iocoder.yudao.module.pay.service.merchant.PayChannelService;
import cn.iocoder.yudao.module.pay.service.notify.PayNotifyService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderExtensionService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Import(PayRefundServiceImpl.class)
public class PayRefundServiceTest extends BaseDbUnitTest {

    @Resource
    private PayRefundServiceImpl refundService;

    @Resource
    private PayRefundMapper refundMapper;

    @MockBean
    private PayClientFactory payClientFactory;
    @MockBean
    private PayOrderService orderService;
    @MockBean
    private PayOrderExtensionService orderExtensionService;
    @MockBean
    private PayAppService appService;
    @MockBean
    private PayChannelService channelService;
    @MockBean
    private PayNotifyService notifyService;

    @Test
    public void testGetRefundPage() {
        // mock ??????
        PayRefundDO dbRefund = randomPojo(PayRefundDO.class, o -> { // ???????????????
            o.setMerchantId("1");
            o.setAppId("1");
            o.setChannelId("1");
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setOrderId("1");
            o.setTradeNo("OT0000001");
            o.setMerchantOrderId("MOT0000001");
            o.setMerchantRefundNo("MRF0000001");
            o.setNotifyUrl("https://www.cancanzi.com");
            o.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
            o.setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
            o.setType(PayRefundTypeEnum.SOME.getStatus());
            o.setPayAmount(100L);
            o.setRefundAmount(500L);
            o.setReason("????????????????????????????????????");
            o.setUserIp("127.0.0.1");
            o.setChannelOrderNo("CH0000001");
            o.setChannelRefundNo("CHR0000001");
            o.setChannelErrorCode("");
            o.setChannelErrorMsg("");
            o.setChannelExtras("");
            o.setExpireTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 30));
            o.setSuccessTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 15));
            o.setNotifyTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 20));
            o.setCreateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 10));
            o.setUpdateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 35));
        });
        refundMapper.insert(dbRefund);
        // ?????? merchantId ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantId("2")));
        // ?????? appId ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setAppId("2")));
        // ?????? channelCode ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // ?????? merchantRefundNo ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantRefundNo("MRF1111112")));
        // ?????? notifyStatus ?????????
        refundMapper.insert(
                cloneIgnoreId(dbRefund, o -> o.setNotifyStatus(PayOrderNotifyStatusEnum.FAILURE.getStatus())));
        // ?????? status ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setStatus(PayRefundStatusEnum.CLOSE.getStatus())));
        // ?????? type ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setType(PayRefundTypeEnum.ALL.getStatus())));
        // ?????? createTime ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o ->
                o.setCreateTime(DateUtils.buildTime(2022, 1, 1, 10, 10, 10))));
        // ????????????
        PayRefundPageReqVO reqVO = new PayRefundPageReqVO();
        reqVO.setMerchantId("1");
        reqVO.setAppId("1");
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantRefundNo("MRF0000001");
        reqVO.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
        reqVO.setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
        reqVO.setType(PayRefundTypeEnum.SOME.getStatus());
        reqVO.setCreateTime((new Date[]{buildTime(2021, 1, 1, 10, 10, 10),buildTime(2021, 1, 1, 10, 10, 12)}));

        // ??????
        PageResult<PayRefundDO> pageResult = refundService.getRefundPage(reqVO);
        // ??????
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbRefund, pageResult.getList().get(0));
    }

    @Test
    public void testGetRefundList() {
        // mock ??????
        PayRefundDO dbRefund = randomPojo(PayRefundDO.class, o -> { // ???????????????
            o.setMerchantId("1");
            o.setAppId("1");
            o.setChannelId("1");
            o.setChannelCode(PayChannelEnum.WX_PUB.getCode());
            o.setOrderId("1");
            o.setTradeNo("OT0000001");
            o.setMerchantOrderId("MOT0000001");
            o.setMerchantRefundNo("MRF0000001");
            o.setNotifyUrl("https://www.cancanzi.com");
            o.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
            o.setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
            o.setType(PayRefundTypeEnum.SOME.getStatus());
            o.setPayAmount(100L);
            o.setRefundAmount(500L);
            o.setReason("????????????????????????????????????");
            o.setUserIp("127.0.0.1");
            o.setChannelOrderNo("CH0000001");
            o.setChannelRefundNo("CHR0000001");
            o.setChannelErrorCode("");
            o.setChannelErrorMsg("");
            o.setChannelExtras("");
            o.setExpireTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 30));
            o.setSuccessTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 15));
            o.setNotifyTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 20));
            o.setCreateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 10));
            o.setUpdateTime(DateUtils.buildTime(2021, 1, 1, 10, 10, 35));
        });
        refundMapper.insert(dbRefund);
        // ?????? merchantId ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantId("2")));
        // ?????? appId ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setAppId("2")));
        // ?????? channelCode ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setChannelCode(PayChannelEnum.ALIPAY_APP.getCode())));
        // ?????? merchantRefundNo ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setMerchantRefundNo("MRF1111112")));
        // ?????? notifyStatus ?????????
        refundMapper.insert(
                cloneIgnoreId(dbRefund, o -> o.setNotifyStatus(PayOrderNotifyStatusEnum.FAILURE.getStatus())));
        // ?????? status ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setStatus(PayRefundStatusEnum.CLOSE.getStatus())));
        // ?????? type ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o -> o.setType(PayRefundTypeEnum.ALL.getStatus())));
        // ?????? createTime ?????????
        refundMapper.insert(cloneIgnoreId(dbRefund, o ->
                o.setCreateTime(DateUtils.buildTime(2022, 1, 1, 10, 10, 10))));

        // ????????????
        PayRefundExportReqVO reqVO = new PayRefundExportReqVO();
        reqVO.setMerchantId("1");
        reqVO.setAppId("1");
        reqVO.setChannelCode(PayChannelEnum.WX_PUB.getCode());
        reqVO.setMerchantRefundNo("MRF0000001");
        reqVO.setNotifyStatus(PayOrderNotifyStatusEnum.SUCCESS.getStatus());
        reqVO.setStatus(PayRefundStatusEnum.SUCCESS.getStatus());
        reqVO.setType(PayRefundTypeEnum.SOME.getStatus());
        reqVO.setCreateTime((new Date[]{buildTime(2021, 1, 1, 10, 10, 10),buildTime(2021, 1, 1, 10, 10, 12)}));

        // ??????
        List<PayRefundDO> list = refundService.getRefundList(reqVO);
        // ??????
        assertEquals(1, list.size());
        assertPojoEquals(dbRefund, list.get(0));
    }

}
