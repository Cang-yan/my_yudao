package cn.iocoder.yudao.module.system.service.tenant;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.config.TenantProperties;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantExportReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantPageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.tenant.vo.tenant.TenantUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantPackageDO;
import cn.iocoder.yudao.module.system.dal.mysql.tenant.TenantMapper;
import cn.iocoder.yudao.module.system.enums.permission.RoleCodeEnum;
import cn.iocoder.yudao.module.system.enums.permission.RoleTypeEnum;
import cn.iocoder.yudao.module.system.service.permission.MenuService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.permission.RoleService;
import cn.iocoder.yudao.module.system.service.tenant.handler.TenantInfoHandler;
import cn.iocoder.yudao.module.system.service.tenant.handler.TenantMenuHandler;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.addTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO.PACKAGE_ID_SYSTEM;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * {@link TenantServiceImpl} ??????????????????
 *
 * @author ????????????
 */
@Import(TenantServiceImpl.class)
public class TenantServiceImplTest extends BaseDbUnitTest {

    @Resource
    private TenantServiceImpl tenantService;

    @Resource
    private TenantMapper tenantMapper;

    @MockBean
    private TenantProperties tenantProperties;
    @MockBean
    private TenantPackageService tenantPackageService;
    @MockBean
    private AdminUserService userService;
    @MockBean
    private RoleService roleService;
    @MockBean
    private MenuService menuService;
    @MockBean
    private PermissionService permissionService;

    @BeforeEach
    public void setUp() {
        // ?????????????????????
        TenantContextHolder.clear();
    }

    @Test
    public void testGetTenantIds() {
        // mock ??????
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setId("1"));
        tenantMapper.insert(tenant);

        // ??????????????????????????????
        List<String> result = tenantService.getTenantIds();
        assertEquals(Collections.singletonList("1"), result);
    }

    @Test
    public void testValidTenant_notExists() {
        assertServiceException(() -> tenantService.validTenant(randomLongId().toString()), TENANT_NOT_EXISTS);
    }

    @Test
    public void testValidTenant_disable() {
        // mock ??????
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setId("1").setStatus(CommonStatusEnum.DISABLE.getStatus()));
        tenantMapper.insert(tenant);

        // ??????????????????????????????
        assertServiceException(() -> tenantService.validTenant("1"), TENANT_DISABLE, tenant.getName());
    }

    @Test
    public void testValidTenant_expired() {
        // mock ??????
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setId("1").setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setExpireTime(buildTime(2020, 2, 2)));
        tenantMapper.insert(tenant);

        // ??????????????????????????????
        assertServiceException(() -> tenantService.validTenant("1"), TENANT_EXPIRE, tenant.getName());
    }

    @Test
    public void testValidTenant_success() {
        // mock ??????
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setId("1").setStatus(CommonStatusEnum.ENABLE.getStatus())
                .setExpireTime(addTime(Duration.ofDays(1))));
        tenantMapper.insert(tenant);

        // ??????????????????????????????
        tenantService.validTenant("1");
    }

    @Test
    public void testCreateTenant() {
        // mock ?????? "100"
        TenantPackageDO tenantPackage = randomPojo(TenantPackageDO.class, o -> o.setId("100"));
        when(tenantPackageService.validTenantPackage(eq("100"))).thenReturn(tenantPackage);
        // mock ?????? "200"
        when(roleService.createRole(argThat(role -> {
            assertEquals(RoleCodeEnum.TENANT_ADMIN.getName(), role.getName());
            assertEquals(RoleCodeEnum.TENANT_ADMIN.getCode(), role.getCode());
            assertEquals(0, role.getSort());
            assertEquals("??????????????????", role.getRemark());
            return true;
        }), eq(RoleTypeEnum.SYSTEM.getType()))).thenReturn("200");
        // mock ?????? "300"
        when(userService.createUser(argThat(user -> {
            assertEquals("yunai", user.getUsername());
            assertEquals("yuanma", user.getPassword());
            assertEquals("??????", user.getNickname());
            assertEquals("15601691300", user.getMobile());
            return true;
        }))).thenReturn("300");

        // ????????????
        TenantCreateReqVO reqVO = randomPojo(TenantCreateReqVO.class, o -> {
            o.setContactName("??????");
            o.setContactMobile("15601691300");
            o.setPackageId("100");
            o.setStatus(randomCommonStatus());
            o.setDomain("https://www.iocoder.cn");
            o.setUsername("yunai");
            o.setPassword("yuanma");
        });

        // ??????
        String tenantId = tenantService.createTenant(reqVO);
        // ??????
        assertNotNull(tenantId);
        // ?????????????????????????????????
        TenantDO tenant = tenantMapper.selectById(tenantId);
        assertPojoEquals(reqVO, tenant);
        assertEquals("300", tenant.getContactUserId());
        // verify ????????????
        verify(permissionService).assignRoleMenu(eq("200"), same(tenantPackage.getMenuIds()));
        // verify ????????????
        verify(permissionService).assignUserRole(eq("300"), eq(singleton("200")));
    }

    @Test
    public void testUpdateTenant_success() {
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setStatus(randomCommonStatus()));
        tenantMapper.insert(dbTenant);// @Sql: ?????????????????????????????????
        // ????????????
        TenantUpdateReqVO reqVO = randomPojo(TenantUpdateReqVO.class, o -> {
            o.setId(dbTenant.getId()); // ??????????????? ID
            o.setStatus(randomCommonStatus());
            o.setDomain(randomString());
        });

        // mock ??????
        TenantPackageDO tenantPackage = randomPojo(TenantPackageDO.class,
                o -> o.setMenuIds(asSet("200", "201")));
        when(tenantPackageService.validTenantPackage(eq(reqVO.getPackageId()))).thenReturn(tenantPackage);
        // mock ????????????
        RoleDO role100 = randomPojo(RoleDO.class, o -> o.setId("100").setCode(RoleCodeEnum.TENANT_ADMIN.getCode()));
        role100.setTenantId(dbTenant.getId());
        RoleDO role101 = randomPojo(RoleDO.class, o -> o.setId("101"));
        role101.setTenantId(dbTenant.getId());
        when(roleService.getRoles(isNull())).thenReturn(asList(role100, role101));
        // mock ?????????????????????
        when(permissionService.getRoleMenuIds(eq("101"))).thenReturn(asSet("201", "202"));

        // ??????
        tenantService.updateTenant(reqVO);
        // ????????????????????????
        TenantDO tenant = tenantMapper.selectById(reqVO.getId()); // ???????????????
        assertPojoEquals(reqVO, tenant);
        // verify ??????????????????
        verify(permissionService).assignRoleMenu(eq("100"), eq(asSet("200", "201")));
        verify(permissionService).assignRoleMenu(eq("101"), eq(asSet("201")));
    }

    @Test
    public void testUpdateTenant_notExists() {
        // ????????????
        TenantUpdateReqVO reqVO = randomPojo(TenantUpdateReqVO.class);

        // ??????, ???????????????
        assertServiceException(() -> tenantService.updateTenant(reqVO), TENANT_NOT_EXISTS);
    }

    @Test
    public void testUpdateTenant_system() {
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setPackageId(PACKAGE_ID_SYSTEM));
        tenantMapper.insert(dbTenant);// @Sql: ?????????????????????????????????
        // ????????????
        TenantUpdateReqVO reqVO = randomPojo(TenantUpdateReqVO.class, o -> {
            o.setId(dbTenant.getId()); // ??????????????? ID
        });

        // ???????????????????????????
        assertServiceException(() -> tenantService.updateTenant(reqVO), TENANT_CAN_NOT_UPDATE_SYSTEM);
    }

    @Test
    public void testDeleteTenant_success() {
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class,
                o -> o.setStatus(randomCommonStatus()));
        tenantMapper.insert(dbTenant);// @Sql: ?????????????????????????????????
        // ????????????
        String id = dbTenant.getId();

        // ??????
        tenantService.deleteTenant(id);
        // ????????????????????????
        assertNull(tenantMapper.selectById(id));
    }

    @Test
    public void testDeleteTenant_notExists() {
        // ????????????
        String id = randomLongId().toString();

        // ??????, ???????????????
        assertServiceException(() -> tenantService.deleteTenant(id), TENANT_NOT_EXISTS);
    }

    @Test
    public void testDeleteTenant_system() {
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setPackageId(PACKAGE_ID_SYSTEM));
        tenantMapper.insert(dbTenant);// @Sql: ?????????????????????????????????
        // ????????????
        String id = dbTenant.getId();

        // ??????, ???????????????
        assertServiceException(() -> tenantService.deleteTenant(id), TENANT_CAN_NOT_UPDATE_SYSTEM);
    }

    @Test
    public void testGetTenant() {
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class);
        tenantMapper.insert(dbTenant);// @Sql: ?????????????????????????????????
        // ????????????
        String id = dbTenant.getId();

        // ??????
        TenantDO result = tenantService.getTenant(id);
        // ????????????
        assertPojoEquals(result, dbTenant);
    }

    @Test
    public void testGetTenantPage() {
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> { // ???????????????
            o.setName("????????????");
            o.setContactName("??????");
            o.setContactMobile("15601691300");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2020, 12, 12));
        });
        tenantMapper.insert(dbTenant);
        // ?????? name ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setName(randomString())));
        // ?????? contactName ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setContactName(randomString())));
        // ?????? contactMobile ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setContactMobile(randomString())));
        // ?????? status ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // ?????? createTime ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setCreateTime(buildTime(2021, 12, 12))));
        // ????????????
        TenantPageReqVO reqVO = new TenantPageReqVO();
        reqVO.setName("??????");
        reqVO.setContactName("???");
        reqVO.setContactMobile("1560");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(new Date[]{buildTime(2020, 12, 1),buildTime(2020, 12, 24)});

        // ??????
        PageResult<TenantDO> pageResult = tenantService.getTenantPage(reqVO);
        // ??????
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbTenant, pageResult.getList().get(0));
    }

    @Test
    public void testGetTenantList() {
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> { // ???????????????
            o.setName("????????????");
            o.setContactName("??????");
            o.setContactMobile("15601691300");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2020, 12, 12));
        });
        tenantMapper.insert(dbTenant);
        // ?????? name ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setName(randomString())));
        // ?????? contactName ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setContactName(randomString())));
        // ?????? contactMobile ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setContactMobile(randomString())));
        // ?????? status ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // ?????? createTime ?????????
        tenantMapper.insert(cloneIgnoreId(dbTenant, o -> o.setCreateTime(buildTime(2021, 12, 12))));
        // ????????????
        TenantExportReqVO reqVO = new TenantExportReqVO();
        reqVO.setName("??????");
        reqVO.setContactName("???");
        reqVO.setContactMobile("1560");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime(new Date[]{buildTime(2020, 12, 1),buildTime(2020, 12, 24)});

        // ??????
        List<TenantDO> list = tenantService.getTenantList(reqVO);
        // ??????
        assertEquals(1, list.size());
        assertPojoEquals(dbTenant, list.get(0));
    }


    @Test
    public void testGetTenantByName() {
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setName("??????"));
        tenantMapper.insert(dbTenant);// @Sql: ?????????????????????????????????

        // ??????
        TenantDO result = tenantService.getTenantByName("??????");
        // ????????????
        assertPojoEquals(result, dbTenant);
    }

    @Test
    public void testGetTenantListByPackageId() {
        // mock ??????
        TenantDO dbTenant1 = randomPojo(TenantDO.class, o -> o.setPackageId("1"));
        tenantMapper.insert(dbTenant1);// @Sql: ?????????????????????????????????
        TenantDO dbTenant2 = randomPojo(TenantDO.class, o -> o.setPackageId("2"));
        tenantMapper.insert(dbTenant2);// @Sql: ?????????????????????????????????

        // ??????
        List<TenantDO> result = tenantService.getTenantListByPackageId("1");
        assertEquals(1, result.size());
        assertPojoEquals(dbTenant1, result.get(0));
    }

    @Test
    public void testGetTenantCountByPackageId() {
        // mock ??????
        TenantDO dbTenant1 = randomPojo(TenantDO.class, o -> o.setPackageId("1"));
        tenantMapper.insert(dbTenant1);// @Sql: ?????????????????????????????????
        TenantDO dbTenant2 = randomPojo(TenantDO.class, o -> o.setPackageId("2"));
        tenantMapper.insert(dbTenant2);// @Sql: ?????????????????????????????????

        // ??????
        Long count = tenantService.getTenantCountByPackageId("1");
        assertEquals(1, count);
    }

    @Test
    public void testHandleTenantInfo_disable() {
        // ????????????
        TenantInfoHandler handler = mock(TenantInfoHandler.class);
        // mock ??????
        when(tenantProperties.getEnable()).thenReturn(false);

        // ??????
        tenantService.handleTenantInfo(handler);
        // ??????
        verify(handler, never()).handle(any());
    }

    @Test
    public void testHandleTenantInfo_success() {
        // ????????????
        TenantInfoHandler handler = mock(TenantInfoHandler.class);
        // mock ?????????
        when(tenantProperties.getEnable()).thenReturn(true);
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class);
        tenantMapper.insert(dbTenant);// @Sql: ?????????????????????????????????
        TenantContextHolder.setTenantId(dbTenant.getId());

        // ??????
        tenantService.handleTenantInfo(handler);
        // ??????
        verify(handler).handle(argThat(argument -> {
            assertPojoEquals(dbTenant, argument);
            return true;
        }));
    }

    @Test
    public void testHandleTenantMenu_disable() {
        // ????????????
        TenantMenuHandler handler = mock(TenantMenuHandler.class);
        // mock ??????
        when(tenantProperties.getEnable()).thenReturn(false);

        // ??????
        tenantService.handleTenantMenu(handler);
        // ??????
        verify(handler, never()).handle(any());
    }

    @Test // ?????????????????????
    public void testHandleTenantMenu_system() {
        // ????????????
        TenantMenuHandler handler = mock(TenantMenuHandler.class);
        // mock ?????????
        when(tenantProperties.getEnable()).thenReturn(true);
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setPackageId(PACKAGE_ID_SYSTEM));
        tenantMapper.insert(dbTenant);// @Sql: ?????????????????????????????????
        TenantContextHolder.setTenantId(dbTenant.getId());
        // mock ??????
        when(menuService.getMenus()).thenReturn(Arrays.asList(randomPojo(MenuDO.class, o -> o.setId("100")),
                        randomPojo(MenuDO.class, o -> o.setId("101"))));

        // ??????
        tenantService.handleTenantMenu(handler);
        // ??????
        verify(handler).handle(asSet("100", "101"));
    }

    @Test // ?????????????????????
    public void testHandleTenantMenu_normal() {
        // ????????????
        TenantMenuHandler handler = mock(TenantMenuHandler.class);
        // mock ?????????
        when(tenantProperties.getEnable()).thenReturn(true);
        // mock ??????
        TenantDO dbTenant = randomPojo(TenantDO.class, o -> o.setPackageId("200"));
        tenantMapper.insert(dbTenant);// @Sql: ?????????????????????????????????
        TenantContextHolder.setTenantId(dbTenant.getId());
        // mock ??????
        when(tenantPackageService.getTenantPackage(eq("200"))).thenReturn(randomPojo(TenantPackageDO.class,
                o -> o.setMenuIds(asSet("100", "101"))));

        // ??????
        tenantService.handleTenantMenu(handler);
        // ??????
        verify(handler).handle(asSet("100", "101"));
    }
}
