package cn.iocoder.yudao.module.pay.controller.admin.order;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.pay.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.pay.convert.order.PayOrderConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.merchant.PayMerchantDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderDO;
import cn.iocoder.yudao.module.pay.dal.dataobject.order.PayOrderExtensionDO;
import cn.iocoder.yudao.module.pay.service.merchant.PayAppService;
import cn.iocoder.yudao.module.pay.service.merchant.PayMerchantService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderExtensionService;
import cn.iocoder.yudao.module.pay.service.order.PayOrderService;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import cn.iocoder.yudao.framework.pay.core.enums.PayChannelEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.operatelog.core.enums.OperateTypeEnum.EXPORT;

@Api(tags = "???????????? - ????????????")
@RestController
@RequestMapping("/pay/order")
@Validated
public class PayOrderController {

    @Resource
    private PayOrderService orderService;
    @Resource
    private PayOrderExtensionService orderExtensionService;
    @Resource
    private PayMerchantService merchantService;
    @Resource
    private PayAppService appService;

    @GetMapping("/get")
    @ApiOperation("??????????????????")
    @ApiImplicitParam(name = "id", value = "??????", required = true, example = "1024", dataTypeClass = String.class)
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PayOrderDetailsRespVO> getOrder(@RequestParam("id") String id) {
        PayOrderDO order = orderService.getOrder(id);
        if (ObjectUtil.isNull(order)) {
            return success(new PayOrderDetailsRespVO());
        }

        PayMerchantDO merchantDO = merchantService.getMerchant(order.getMerchantId());
        PayAppDO appDO = appService.getApp(order.getAppId());
        PayChannelEnum channelEnum = PayChannelEnum.getByCode(order.getChannelCode());

        // TODO @aquan???????????????????????? format???
        PayOrderDetailsRespVO respVO = PayOrderConvert.INSTANCE.orderDetailConvert(order);
        respVO.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "????????????");
        respVO.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "????????????");
        respVO.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "????????????");

        PayOrderExtensionDO extensionDO = orderExtensionService.getOrderExtension(order.getSuccessExtensionId());
        if (ObjectUtil.isNotNull(extensionDO)) {
            respVO.setPayOrderExtension(PayOrderConvert.INSTANCE.orderDetailExtensionConvert(extensionDO));
        }

        return success(respVO);
    }

    @GetMapping("/page")
    @ApiOperation("????????????????????????")
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PageResult<PayOrderPageItemRespVO>> getOrderPage(@Valid PayOrderPageReqVO pageVO) {
        PageResult<PayOrderDO> pageResult = orderService.getOrderPage(pageVO);
        if (CollectionUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        // ????????????ID??????
        Map<String, PayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(pageResult.getList(), PayOrderDO::getMerchantId));
        // ????????????ID??????
        Map<String, PayAppDO> appMap = appService.getAppMap(
                CollectionUtils.convertList(pageResult.getList(), PayOrderDO::getAppId));

        List<PayOrderPageItemRespVO> pageList = new ArrayList<>(pageResult.getList().size());
        pageResult.getList().forEach(c -> {
            PayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            PayAppDO appDO = appMap.get(c.getAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getChannelCode());

            PayOrderPageItemRespVO orderItem = PayOrderConvert.INSTANCE.pageConvertItemPage(c);
            orderItem.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "????????????");
            orderItem.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "????????????");
            orderItem.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "????????????");
            pageList.add(orderItem);
        });
        return success(new PageResult<>(pageList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @ApiOperation("??????????????????Excel")
    @PreAuthorize("@ss.hasPermission('pay:order:export')")
    @OperateLog(type = EXPORT)
    public void exportOrderExcel(@Valid PayOrderExportReqVO exportReqVO,
            HttpServletResponse response) throws IOException {

        List<PayOrderDO> list = orderService.getOrderList(exportReqVO);
        if (CollectionUtil.isEmpty(list)) {
            ExcelUtils.write(response, "????????????.xls", "??????",
                    PayOrderExcelVO.class, new ArrayList<>());
        }

        // ????????????ID??????
        Map<String, PayMerchantDO> merchantMap = merchantService.getMerchantMap(
                CollectionUtils.convertList(list, PayOrderDO::getMerchantId));
        // ????????????ID??????
        Map<String, PayAppDO> appMap = appService.getAppMap(
                CollectionUtils.convertList(list, PayOrderDO::getAppId));
        // ????????????????????????
        Map<String, PayOrderExtensionDO> orderExtensionMap = orderExtensionService
                .getOrderExtensionMap(CollectionUtils.convertList(list, PayOrderDO::getSuccessExtensionId));

        List<PayOrderExcelVO> excelDatum = new ArrayList<>(list.size());
        list.forEach(c -> {
            PayMerchantDO merchantDO = merchantMap.get(c.getMerchantId());
            PayAppDO appDO = appMap.get(c.getAppId());
            PayChannelEnum channelEnum = PayChannelEnum.getByCode(c.getChannelCode());
            PayOrderExtensionDO orderExtensionDO = orderExtensionMap.get(c.getSuccessExtensionId());

            PayOrderExcelVO excelItem = PayOrderConvert.INSTANCE.excelConvert(c);
            excelItem.setMerchantName(ObjectUtil.isNotNull(merchantDO) ? merchantDO.getName() : "????????????");
            excelItem.setAppName(ObjectUtil.isNotNull(appDO) ? appDO.getName() : "????????????");
            excelItem.setChannelCodeName(ObjectUtil.isNotNull(channelEnum) ? channelEnum.getName() : "????????????");
            excelItem.setNo(ObjectUtil.isNotNull(orderExtensionDO) ? orderExtensionDO.getNo() : "");
            excelDatum.add(excelItem);
        });

        // ?????? Excel
        ExcelUtils.write(response, "????????????.xls", "??????", PayOrderExcelVO.class, excelDatum);
    }

}
