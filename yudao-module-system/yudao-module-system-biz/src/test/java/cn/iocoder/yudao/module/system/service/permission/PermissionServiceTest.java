package cn.iocoder.yudao.module.system.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.module.system.api.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleMenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuBatchInsertMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.RoleMenuMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleBatchInsertMapper;
import cn.iocoder.yudao.module.system.dal.mysql.permission.UserRoleMapper;
import cn.iocoder.yudao.module.system.enums.permission.DataScopeEnum;
import cn.iocoder.yudao.module.system.mq.producer.permission.PermissionProducer;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import({PermissionServiceImpl.class,
        RoleMenuBatchInsertMapper.class, UserRoleBatchInsertMapper.class})
public class PermissionServiceTest extends BaseDbUnitTest {

    @Resource
    private PermissionServiceImpl permissionService;

    @Resource
    private RoleMenuMapper roleMenuMapper;
    @Resource
    private RoleMenuBatchInsertMapper roleMenuBatchInsertMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private UserRoleBatchInsertMapper userRoleBatchInsertMapper;

    @MockBean
    private RoleService roleService;
    @MockBean
    private MenuService menuService;
    @MockBean
    private DeptService deptService;
    @MockBean
    private AdminUserService userService;

    @MockBean
    private PermissionProducer permissionProducer;

    @Test
    public void testInitRoleMenuLocalCache() {
        // mock ??????
        RoleMenuDO roleMenuDO01 = randomPojo(RoleMenuDO.class, o -> o.setRoleId("1").setMenuId("10"));
        roleMenuMapper.insert(roleMenuDO01);
        RoleMenuDO roleMenuDO02 = randomPojo(RoleMenuDO.class, o -> o.setRoleId("1").setMenuId("20"));
        roleMenuMapper.insert(roleMenuDO02);

        // ??????
        permissionService.initRoleMenuLocalCache();
        // ?????? roleMenuCache ??????
        assertEquals(1, permissionService.getRoleMenuCache().keySet().size());
        assertEquals(asList("10", "20"), permissionService.getRoleMenuCache().get("1"));
        // ?????? menuRoleCache ??????
        assertEquals(2, permissionService.getMenuRoleCache().size());
        assertEquals(singletonList( "1"), permissionService.getMenuRoleCache().get("10"));
        assertEquals(singletonList( "1"), permissionService.getMenuRoleCache().get("20"));
        // ?????? maxUpdateTime ??????
        Date maxUpdateTime = permissionService.getRoleMenuMaxUpdateTime();
        assertEquals(ObjectUtils.max(roleMenuDO01.getUpdateTime(), roleMenuDO02.getUpdateTime()), maxUpdateTime);
    }

    @Test
    public void testInitUserRoleLocalCache() {
        // mock ??????
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId("1").setRoleId("10"));
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO roleMenuDO02 = randomPojo(UserRoleDO.class, o -> o.setUserId("1").setRoleId("20"));
        userRoleMapper.insert(roleMenuDO02);

        // ??????
        permissionService.initUserRoleLocalCache();
        // ?????? roleMenuCache ??????
        assertEquals(1, permissionService.getUserRoleCache().size());
        assertEquals(asSet("10", "20"), permissionService.getUserRoleCache().get("1"));
        // ?????? maxUpdateTime ??????
        Date maxUpdateTime = permissionService.getUserRoleMaxUpdateTime();
        assertEquals(ObjectUtils.max(userRoleDO01.getUpdateTime(), roleMenuDO02.getUpdateTime()), maxUpdateTime);
    }

    @Test
    public void testGetRoleMenuListFromCache_superAdmin() {
        // ????????????
        Collection<String> roleIds = singletonList("100");
        Collection<Integer> menuTypes = asList(2, 3);
        Collection<Integer> menusStatuses = asList(0, 1);
        // mock ??????
        List<RoleDO> roleList = singletonList(randomPojo(RoleDO.class, o -> o.setId("100")));
        when(roleService.getRolesFromCache(eq(roleIds))).thenReturn(roleList);
        when(roleService.hasAnySuperAdmin(same(roleList))).thenReturn(true);
        List<MenuDO> menuList = randomPojoList(MenuDO.class);
        when(menuService.getMenuListFromCache(eq(menuTypes), eq(menusStatuses))).thenReturn(menuList);

        // ??????
        List<MenuDO> result = permissionService.getRoleMenuListFromCache(roleIds, menuTypes, menusStatuses);
        // ??????
        assertSame(menuList, result);
    }

    @Test
    public void testGetRoleMenuListFromCache_normal() {
        // ????????????
        Collection<String> roleIds = asSet("100", "200");
        Collection<Integer> menuTypes = asList(2, 3);
        Collection<Integer> menusStatuses = asList(0, 1);
        // mock ??????
        Multimap<String, String> roleMenuCache = ImmutableMultimap.<String, String>builder().put("100", "1000")
                .put("200", "2000").put("200", "2001").build();
        permissionService.setRoleMenuCache(roleMenuCache);
        List<MenuDO> menuList = randomPojoList(MenuDO.class);
        when(menuService.getMenuListFromCache(eq(asList("1000", "2000", "2001")), eq(menuTypes), eq(menusStatuses))).thenReturn(menuList);

        // ??????
        List<MenuDO> result = permissionService.getRoleMenuListFromCache(roleIds, menuTypes, menusStatuses);
        // ??????
        assertSame(menuList, result);
    }

    @Test
    public void testGetUserRoleIdsFromCache() {
        // ????????????
        String userId = "1";
        Collection<Integer> roleStatuses = singleton(CommonStatusEnum.ENABLE.getStatus());
        // mock ??????
        Map<String, Set<String>> userRoleCache = MapUtil.<String, Set<String>>builder()
                .put( "1", asSet("10", "20")).build();
        permissionService.setUserRoleCache(userRoleCache);
        RoleDO roleDO01 = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq("10"))).thenReturn(roleDO01);
        RoleDO roleDO02 = randomPojo(RoleDO.class, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus()));
        when(roleService.getRoleFromCache(eq("20"))).thenReturn(roleDO02);

        // ??????
        Set<String> roleIds = permissionService.getUserRoleIdsFromCache(userId, roleStatuses);
        // ??????
        assertEquals(asSet("10"), roleIds);
    }

    @Test
    public void testGetRoleMenuIds_superAdmin() {
        // ????????????
        String roleId = "100";
        // mock ??????
        when(roleService.hasAnySuperAdmin(eq(singleton("100")))).thenReturn(true);
        List<MenuDO> menuList = singletonList(randomPojo(MenuDO.class).setId("1"));
        when(menuService.getMenus()).thenReturn(menuList);

        // ??????
        Set<String> menuIds = permissionService.getRoleMenuIds(roleId);
        // ??????
        assertEquals(singleton( "1"), menuIds);
    }

    @Test
    public void testGetRoleMenuIds_normal() {
        // ????????????
        String roleId = "100";
        // mock ??????
        RoleMenuDO roleMenu01 = randomPojo(RoleMenuDO.class).setRoleId("100").setMenuId("1");
        roleMenuMapper.insert(roleMenu01);
        RoleMenuDO roleMenu02 = randomPojo(RoleMenuDO.class).setRoleId("100").setMenuId("2");
        roleMenuMapper.insert(roleMenu02);

        // ??????
        Set<String> menuIds = permissionService.getRoleMenuIds(roleId);
        // ??????
        assertEquals(asSet( "1", "2"), menuIds);
    }

    @Test
    public void testAssignRoleMenu() {
        // ????????????
        String roleId = "1";
        Set<String> menuIds = asSet("200", "300");
        // mock ??????
        RoleMenuDO roleMenu01 = randomPojo(RoleMenuDO.class).setRoleId("1").setMenuId("100");
        roleMenuMapper.insert(roleMenu01);
        RoleMenuDO roleMenu02 = randomPojo(RoleMenuDO.class).setRoleId("1").setMenuId("200");
        roleMenuMapper.insert(roleMenu02);

        // ??????
        permissionService.assignRoleMenu(roleId, menuIds);
        // ??????
        List<RoleMenuDO> roleMenuList = roleMenuMapper.selectList();
        assertEquals(2, roleMenuList.size());
        assertEquals( "1", roleMenuList.get(0).getRoleId());
        assertEquals("200", roleMenuList.get(0).getMenuId());
        assertEquals( "1", roleMenuList.get(1).getRoleId());
        assertEquals("300", roleMenuList.get(1).getMenuId());
        verify(permissionProducer).sendRoleMenuRefreshMessage();
    }

    @Test
    public void testAssignUserRole() {
        // ????????????
        String userId =  "1";
        Set<String> roleIds = asSet("200", "300");
        // mock ??????
        UserRoleDO userRole01 = randomPojo(UserRoleDO.class).setUserId( "1").setRoleId("100");
        userRoleMapper.insert(userRole01);
        UserRoleDO userRole02 = randomPojo(UserRoleDO.class).setUserId( "1").setRoleId("200");
        userRoleMapper.insert(userRole02);

        // ??????
        permissionService.assignUserRole(userId, roleIds);
        // ??????
        List<UserRoleDO> userRoleDOList = userRoleMapper.selectList();
        assertEquals(2, userRoleDOList.size());
        assertEquals( "1", userRoleDOList.get(0).getUserId());
        assertEquals("200", userRoleDOList.get(0).getRoleId());
        assertEquals( "1", userRoleDOList.get(1).getUserId());
        assertEquals("300", userRoleDOList.get(1).getRoleId());
        verify(permissionProducer).sendUserRoleRefreshMessage();
    }

    @Test
    public void testGetUserRoleIdListByUserId() {
        // ????????????
        String userId =  "1";
        // mock ??????
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId( "1").setRoleId("10"));
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO roleMenuDO02 = randomPojo(UserRoleDO.class, o -> o.setUserId( "1").setRoleId("20"));
        userRoleMapper.insert(roleMenuDO02);

        // ??????
        Set<String> result = permissionService.getUserRoleIdListByUserId(userId);
        // ??????
        assertEquals(asSet("10", "20"), result);
    }

    @Test
    public void testGetUserRoleIdListByRoleIds() {
        // ????????????
        Collection<String> roleIds = asSet("10", "20");
        // mock ??????
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId( "1").setRoleId("10"));
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO roleMenuDO02 = randomPojo(UserRoleDO.class, o -> o.setUserId("2").setRoleId("20"));
        userRoleMapper.insert(roleMenuDO02);

        // ??????
        Set<String> result = permissionService.getUserRoleIdListByRoleIds(roleIds);
        // ??????
        assertEquals(asSet("1", "2"), result);
    }

    @Test
    public void testAssignRoleDataScope() {
        // ????????????
        String roleId = "1";
        Integer dataScope = 2;
        Set<String> dataScopeDeptIds = asSet("10", "20");

        // ??????
        permissionService.assignRoleDataScope(roleId, dataScope, dataScopeDeptIds);
        // ??????
        verify(roleService).updateRoleDataScope(eq(roleId), eq(dataScope), eq(dataScopeDeptIds));
    }

    @Test
    public void testProcessRoleDeleted() {
        // ????????????
        String roleId = randomLongId().toString();
        // mock ?????? UserRole
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setRoleId(roleId)); // ?????????
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO userRoleDO02 = randomPojo(UserRoleDO.class); // ????????????
        userRoleMapper.insert(userRoleDO02);
        // mock ?????? RoleMenu
        RoleMenuDO roleMenuDO01 = randomPojo(RoleMenuDO.class, o -> o.setRoleId(roleId)); // ?????????
        roleMenuMapper.insert(roleMenuDO01);
        RoleMenuDO roleMenuDO02 = randomPojo(RoleMenuDO.class); // ????????????
        roleMenuMapper.insert(roleMenuDO02);

        // ??????
        permissionService.processRoleDeleted(roleId);
        // ???????????? RoleMenuDO
        List<RoleMenuDO> dbRoleMenus = roleMenuMapper.selectList();
        assertEquals(1, dbRoleMenus.size());
        assertPojoEquals(dbRoleMenus.get(0), roleMenuDO02);
        // ???????????? UserRoleDO
        List<UserRoleDO> dbUserRoles = userRoleMapper.selectList();
        assertEquals(1, dbUserRoles.size());
        assertPojoEquals(dbUserRoles.get(0), userRoleDO02);
        // ????????????
        verify(permissionProducer).sendRoleMenuRefreshMessage();
        verify(permissionProducer).sendUserRoleRefreshMessage();
    }

    @Test
    public void testProcessMenuDeleted() {
        // ????????????
        String menuId = randomLongId().toString();
        // mock ??????
        RoleMenuDO roleMenuDO01 = randomPojo(RoleMenuDO.class, o -> o.setMenuId(menuId)); // ?????????
        roleMenuMapper.insert(roleMenuDO01);
        RoleMenuDO roleMenuDO02 = randomPojo(RoleMenuDO.class); // ????????????
        roleMenuMapper.insert(roleMenuDO02);

        // ??????
        permissionService.processMenuDeleted(menuId);
        // ????????????
        List<RoleMenuDO> dbRoleMenus = roleMenuMapper.selectList();
        assertEquals(1, dbRoleMenus.size());
        assertPojoEquals(dbRoleMenus.get(0), roleMenuDO02);
        // ????????????
        verify(permissionProducer).sendRoleMenuRefreshMessage();
    }

    @Test
    public void testProcessUserDeleted() {
        // ????????????
        String userId = randomLongId().toString();
        // mock ??????
        UserRoleDO userRoleDO01 = randomPojo(UserRoleDO.class, o -> o.setUserId(userId)); // ?????????
        userRoleMapper.insert(userRoleDO01);
        UserRoleDO userRoleDO02 = randomPojo(UserRoleDO.class); // ????????????
        userRoleMapper.insert(userRoleDO02);

        // ??????
        permissionService.processUserDeleted(userId);
        // ????????????
        List<UserRoleDO> dbUserRoles = userRoleMapper.selectList();
        assertEquals(1, dbUserRoles.size());
        assertPojoEquals(dbUserRoles.get(0), userRoleDO02);
        // ????????????
        verify(permissionProducer).sendUserRoleRefreshMessage();
    }

    @Test
    public void testHasAnyPermissions_superAdmin() {
        // ????????????
        String userId = "1";
        String[] roles = new String[]{"system:user:query", "system:user:create"};
        // mock ????????????????????????
        permissionService.setUserRoleCache(MapUtil.<String, Set<String>>builder().put("1", asSet("100")).build());
        RoleDO role = randomPojo(RoleDO.class, o -> o.setId("100")
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq("100"))).thenReturn(role);
        // mock ????????????
        when(roleService.hasAnySuperAdmin(eq(asSet("100")))).thenReturn(true);

        // ??????
        boolean has = permissionService.hasAnyPermissions(userId, roles);
        // ??????
        assertTrue(has);
    }

    @Test
    public void testHasAnyPermissions_normal() {
        // ????????????
        String userId = "1";
        String[] roles = new String[]{"system:user:query", "system:user:create"};
        // mock ????????????????????????
        permissionService.setUserRoleCache(MapUtil.<String, Set<String>>builder().put("1", asSet("100")).build());
        RoleDO role = randomPojo(RoleDO.class, o -> o.setId("100")
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq("100"))).thenReturn(role);
        // mock ????????????
        MenuDO menu = randomPojo(MenuDO.class, o -> o.setId("1000"));
        when(menuService.getMenuListByPermissionFromCache(eq("system:user:create"))).thenReturn(singletonList(menu));
        permissionService.setMenuRoleCache(ImmutableMultimap.<String, String>builder().put("1000", "100").build());


        // ??????
        boolean has = permissionService.hasAnyPermissions(userId, roles);
        // ??????
        assertTrue(has);
    }

    @Test
    public void testHasAnyRoles_superAdmin() {
        // ????????????
        String userId = "1";
        String[] roles = new String[]{"yunai", "tudou"};
        // mock ????????????????????????
        permissionService.setUserRoleCache(MapUtil.<String, Set<String>>builder().put("1", asSet("100")).build());
        RoleDO role = randomPojo(RoleDO.class, o -> o.setId("100")
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq("100"))).thenReturn(role);
        // mock ????????????
        when(roleService.hasAnySuperAdmin(eq(asSet("100")))).thenReturn(true);

        // ??????
        boolean has = permissionService.hasAnyRoles(userId, roles);
        // ??????
        assertTrue(has);
    }

    @Test
    public void testHasAnyRoles_normal() {
        // ????????????
        String userId = "1";
        String[] roles = new String[]{"yunai", "tudou"};
        // mock ????????????????????????
        permissionService.setUserRoleCache(MapUtil.<String, Set<String>>builder().put("1", asSet("100")).build());
        RoleDO role = randomPojo(RoleDO.class, o -> o.setId("100").setCode("yunai")
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRoleFromCache(eq("100"))).thenReturn(role);
        // mock ????????????
        when(roleService.getRolesFromCache(eq(asSet("100")))).thenReturn(singletonList(role));

        // ??????
        boolean has = permissionService.hasAnyRoles(userId, roles);
        // ??????
        assertTrue(has);
    }

    @Test
    public void testGetDeptDataPermission_All() {
        // ????????????
        String userId = "1";
        // mock ?????????????????????
        permissionService.setUserRoleCache(MapUtil.<String, Set<String>>builder().put("1", asSet("2")).build());
        // mock ?????????????????????
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.ALL.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton("2")))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq("2"))).thenReturn(roleDO);

        // ??????
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // ??????
        assertTrue(result.getAll());
        assertFalse(result.getSelf());
        assertTrue(CollUtil.isEmpty(result.getDeptIds()));
    }

    @Test
    public void testGetDeptDataPermission_DeptCustom() {
        // ????????????
        String userId = "1";
        // mock ?????????????????????
        permissionService.setUserRoleCache(MapUtil.<String, Set<String>>builder().put("1", asSet("2")).build());
        // mock ?????????????????????
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_CUSTOM.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton("2")))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq("2"))).thenReturn(roleDO);
        // mock ???????????????
        when(userService.getUser(eq("1"))).thenReturn(new AdminUserDO().setDeptId("3"), null, null); // ???????????? null ???????????????????????????????????????

        // ??????
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // ??????
        assertFalse(result.getAll());
        assertFalse(result.getSelf());
        assertEquals(roleDO.getDataScopeDeptIds().size() + 1, result.getDeptIds().size());
        assertTrue(CollUtil.containsAll(result.getDeptIds(), roleDO.getDataScopeDeptIds()));
        assertTrue(CollUtil.contains(result.getDeptIds(), "3"));
    }

    @Test
    public void testGetDeptDataPermission_DeptOnly() {
        // ????????????
        String userId = "1";
        // mock ?????????????????????
        permissionService.setUserRoleCache(MapUtil.<String, Set<String>>builder().put("1", asSet("2")).build());
        // mock ?????????????????????
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_ONLY.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton("2")))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq("2"))).thenReturn(roleDO);
        // mock ???????????????
        when(userService.getUser(eq("1"))).thenReturn(new AdminUserDO().setDeptId("3"), null, null); // ???????????? null ???????????????????????????????????????

        // ??????
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // ??????
        assertFalse(result.getAll());
        assertFalse(result.getSelf());
        assertEquals(1, result.getDeptIds().size());
        assertTrue(CollUtil.contains(result.getDeptIds(), "3"));
    }

    @Test
    public void testGetDeptDataPermission_DeptAndChild() {
        // ????????????
        String userId = "1";
        // mock ?????????????????????
        permissionService.setUserRoleCache(MapUtil.<String, Set<String>>builder().put("1", asSet("2")).build());
        // mock ?????????????????????
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.DEPT_AND_CHILD.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton("2")))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq("2"))).thenReturn(roleDO);
        // mock ???????????????
        when(userService.getUser(eq("1"))).thenReturn(new AdminUserDO().setDeptId("3"), null, null); // ???????????? null ???????????????????????????????????????
        // mock ??????????????????
        DeptDO deptDO = randomPojo(DeptDO.class);
        when(deptService.getDeptsByParentIdFromCache(eq("3"), eq(true)))
                .thenReturn(singletonList(deptDO));

        // ??????
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // ??????
        assertFalse(result.getAll());
        assertFalse(result.getSelf());
        assertEquals(2, result.getDeptIds().size());
        assertTrue(CollUtil.contains(result.getDeptIds(), deptDO.getId()));
        assertTrue(CollUtil.contains(result.getDeptIds(), "3"));
    }

    @Test
    public void testGetDeptDataPermission_Self() {
        // ????????????
        String userId = "1";
        // mock ?????????????????????
        permissionService.setUserRoleCache(MapUtil.<String, Set<String>>builder().put("1", asSet("2")).build());
        // mock ?????????????????????
        RoleDO roleDO = randomPojo(RoleDO.class, o -> o.setDataScope(DataScopeEnum.SELF.getScope())
                .setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(roleService.getRolesFromCache(eq(singleton("2")))).thenReturn(singletonList(roleDO));
        when(roleService.getRoleFromCache(eq("2"))).thenReturn(roleDO);

        // ??????
        DeptDataPermissionRespDTO result = permissionService.getDeptDataPermission(userId);
        // ??????
        assertFalse(result.getAll());
        assertTrue(result.getSelf());
        assertTrue(CollUtil.isEmpty(result.getDeptIds()));
    }

}
