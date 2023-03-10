package cn.iocoder.yudao.module.system.service.user;

import cn.hutool.core.util.RandomUtil;
import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.ArrayUtils;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileUpdatePasswordReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.profile.UserProfileUpdateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.user.vo.user.*;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.PostDO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.UserPostDO;
import cn.iocoder.yudao.module.system.dal.dataobject.tenant.TenantDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.dept.UserPostMapper;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import cn.iocoder.yudao.module.system.service.dept.PostService;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import cn.iocoder.yudao.module.system.service.tenant.TenantService;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomBytes;
import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.buildTime;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static java.util.Collections.singleton;
import static org.assertj.core.util.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Import(AdminUserServiceImpl.class)
public class AdminUserServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AdminUserServiceImpl userService;

    @Resource
    private AdminUserMapper userMapper;
    @Resource
    private UserPostMapper userPostMapper;

    @MockBean
    private DeptService deptService;
    @MockBean
    private PostService postService;
    @MockBean
    private PermissionService permissionService;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private TenantService tenantService;
    @MockBean
    private FileApi fileApi;

    @Test
    public void testCreatUser_success() {
        // ????????????
        UserCreateReqVO reqVO = randomPojo(UserCreateReqVO.class, o -> {
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setMobile(randomString());
            o.setPostIds(asSet("1", "2"));
        });
        // mock ??????????????????
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setAccountCount(1));
        doNothing().when(tenantService).handleTenantInfo(argThat(handler -> {
            handler.handle(tenant);
            return true;
        }));
        // mock deptService ?????????
        DeptDO dept = randomPojo(DeptDO.class, o -> {
            o.setId(reqVO.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);
        // mock postService ?????????
        List<PostDO> posts = CollectionUtils.convertList(reqVO.getPostIds(), postId ->
                randomPojo(PostDO.class, o -> {
                    o.setId(postId);
                    o.setStatus(CommonStatusEnum.ENABLE.getStatus());
                }));
        when(postService.getPosts(eq(reqVO.getPostIds()), isNull())).thenReturn(posts);
        // mock passwordEncoder ?????????
        when(passwordEncoder.encode(eq(reqVO.getPassword()))).thenReturn("yudaoyuanma");

        // ??????
        String userId = userService.createUser(reqVO);
        // ??????
        AdminUserDO user = userMapper.selectById(userId);
        assertPojoEquals(reqVO, user, "password");
        assertEquals("yudaoyuanma", user.getPassword());
        assertEquals(CommonStatusEnum.ENABLE.getStatus(), user.getStatus());
        // ??????????????????
        List<UserPostDO> userPosts = userPostMapper.selectListByUserId(user.getId());
        assertEquals("1", userPosts.get(0).getPostId());
        assertEquals("2", userPosts.get(1).getPostId());
    }

    @Test
    public void testCreatUser_max() {
        // ????????????
        UserCreateReqVO reqVO = randomPojo(UserCreateReqVO.class);
        // mock ??????????????????
        TenantDO tenant = randomPojo(TenantDO.class, o -> o.setAccountCount(-1));
        doNothing().when(tenantService).handleTenantInfo(argThat(handler -> {
            handler.handle(tenant);
            return true;
        }));

        // ????????????????????????
        assertServiceException(() -> userService.createUser(reqVO), USER_COUNT_MAX, -1);
    }

    @Test
    public void testUpdateUser_success() {
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO(o -> o.setPostIds(asSet("1", "2")));
        userMapper.insert(dbUser);
        userPostMapper.insert(new UserPostDO().setUserId(dbUser.getId()).setPostId("1"));
        userPostMapper.insert(new UserPostDO().setUserId(dbUser.getId()).setPostId("2"));
        // ????????????
        UserUpdateReqVO reqVO = randomPojo(UserUpdateReqVO.class, o -> {
            o.setId(dbUser.getId());
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
            o.setMobile(randomString());
            o.setPostIds(asSet("2", "3"));
        });
        // mock deptService ?????????
        DeptDO dept = randomPojo(DeptDO.class, o -> {
            o.setId(reqVO.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);
        // mock postService ?????????
        List<PostDO> posts = CollectionUtils.convertList(reqVO.getPostIds(), postId ->
                randomPojo(PostDO.class, o -> {
                    o.setId(postId);
                    o.setStatus(CommonStatusEnum.ENABLE.getStatus());
                }));
        when(postService.getPosts(eq(reqVO.getPostIds()), isNull())).thenReturn(posts);

        // ??????
        userService.updateUser(reqVO);
        // ??????
        AdminUserDO user = userMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, user);
        // ??????????????????
        List<UserPostDO> userPosts = userPostMapper.selectListByUserId(user.getId());
        assertEquals("2", userPosts.get(0).getPostId());
        assertEquals("3", userPosts.get(1).getPostId());
    }

    @Test
    public void testUpdateUserProfile_success() {
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO();
        userMapper.insert(dbUser);
        // ????????????
        String userId = dbUser.getId();
        UserProfileUpdateReqVO reqVO = randomPojo(UserProfileUpdateReqVO.class, o -> {
            o.setMobile(randomString());
            o.setSex(RandomUtil.randomEle(SexEnum.values()).getSex());
        });

        // ??????
        userService.updateUserProfile(userId, reqVO);
        // ??????
        AdminUserDO user = userMapper.selectById(userId);
        assertPojoEquals(reqVO, user);
    }

    @Test
    public void testUpdateUserPassword_success() {
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO(o -> o.setPassword("encode:tudou"));
        userMapper.insert(dbUser);
        // ????????????
        String userId = dbUser.getId();
        UserProfileUpdatePasswordReqVO reqVO = randomPojo(UserProfileUpdatePasswordReqVO.class, o -> {
            o.setOldPassword("tudou");
            o.setNewPassword("yuanma");
        });
        // mock ??????
        when(passwordEncoder.encode(anyString())).then(
                (Answer<String>) invocationOnMock -> "encode:" + invocationOnMock.getArgument(0));
        when(passwordEncoder.matches(eq(reqVO.getOldPassword()), eq(dbUser.getPassword()))).thenReturn(true);

        // ??????
        userService.updateUserPassword(userId, reqVO);
        // ??????
        AdminUserDO user = userMapper.selectById(userId);
        assertEquals("encode:yuanma", user.getPassword());
    }

    @Test
    public void testUpdateUserAvatar_success() throws Exception {
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO();
        userMapper.insert(dbUser);
        // ????????????
        String userId = dbUser.getId();
        byte[] avatarFileBytes = randomBytes(10);
        ByteArrayInputStream avatarFile = new ByteArrayInputStream(avatarFileBytes);
        // mock ??????
        String avatar = randomString();
        when(fileApi.createFile(eq( avatarFileBytes))).thenReturn(avatar);

        // ??????
        userService.updateUserAvatar(userId, avatarFile);
        // ??????
        AdminUserDO user = userMapper.selectById(userId);
        assertEquals(avatar, user.getAvatar());
    }

    @Test
    public void testUpdateUserPassword02_success() {
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO();
        userMapper.insert(dbUser);
        // ????????????
        String userId = dbUser.getId();
        String password = "yudao";
        // mock ??????
        when(passwordEncoder.encode(anyString())).then(
                (Answer<String>) invocationOnMock -> "encode:" + invocationOnMock.getArgument(0));

        // ??????
        userService.updateUserPassword(userId, password);
        // ??????
        AdminUserDO user = userMapper.selectById(userId);
        assertEquals("encode:" + password, user.getPassword());
    }

    @Test
    public void testUpdateUserStatus() {
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO();
        userMapper.insert(dbUser);
        // ????????????
        String userId = dbUser.getId();
        Integer status = randomCommonStatus();

        // ??????
        userService.updateUserStatus(userId, status);
        // ??????
        AdminUserDO user = userMapper.selectById(userId);
        assertEquals(status, user.getStatus());
    }

    @Test
    public void testDeleteUser_success(){
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO();
        userMapper.insert(dbUser);
        // ????????????
        String userId = dbUser.getId();

        // ????????????
        userService.deleteUser(userId);
        // ????????????
        assertNull(userMapper.selectById(userId));
        // ??????????????????
        verify(permissionService, times(1)).processUserDeleted(eq(userId));
    }

    @Test
    public void testGetUserPage() {
        // mock ??????
        AdminUserDO dbUser = initGetUserPageData();
        // ????????????
        UserPageReqVO reqVO = new UserPageReqVO();
        reqVO.setUsername("tu");
        reqVO.setMobile("1560");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime((new Date[]{buildTime(2020, 12, 1),buildTime(2020, 12, 24)}));
        reqVO.setDeptId("1"); // ?????????"1" ??? "2" ????????????
        // mock ??????
        List<DeptDO> deptList = newArrayList(randomPojo(DeptDO.class, o -> o.setId("2")));
        when(deptService.getDeptsByParentIdFromCache(eq(reqVO.getDeptId()), eq(true))).thenReturn(deptList);

        // ??????
        PageResult<AdminUserDO> pageResult = userService.getUserPage(reqVO);
        // ??????
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbUser, pageResult.getList().get(0));
    }

    @Test
    public void testGetUsers() {
        // mock ??????
        AdminUserDO dbUser = initGetUserPageData();
        // ????????????
        UserExportReqVO reqVO = new UserExportReqVO();
        reqVO.setUsername("tu");
        reqVO.setMobile("1560");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        reqVO.setCreateTime((new Date[]{buildTime(2020, 12, 1),buildTime(2020, 12, 24)}));
        reqVO.setDeptId("1"); // ?????????1L ??? "2" ????????????
        // mock ??????
        List<DeptDO> deptList = newArrayList(randomPojo(DeptDO.class, o -> o.setId("2")));
        when(deptService.getDeptsByParentIdFromCache(eq(reqVO.getDeptId()), eq(true))).thenReturn(deptList);

        // ??????
        List<AdminUserDO> list = userService.getUsers(reqVO);
        // ??????
        assertEquals(1, list.size());
        assertPojoEquals(dbUser, list.get(0));
    }

    /**
     * ????????? getUserPage ?????????????????????
     */
    private AdminUserDO initGetUserPageData() {
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO(o -> { // ???????????????
            o.setUsername("tudou");
            o.setMobile("15601691300");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setCreateTime(buildTime(2020, 12, 12));
            o.setDeptId("2");
        });
        userMapper.insert(dbUser);
        // ?????? username ?????????
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setUsername("dou")));
        // ?????? mobile ?????????
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setMobile("18818260888")));
        // ?????? status ?????????
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // ?????? createTime ?????????
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setCreateTime(buildTime(2020, 11, 11))));
        // ?????? dept ?????????
        userMapper.insert(ObjectUtils.cloneIgnoreId(dbUser, o -> o.setDeptId("0")));
        return dbUser;
    }

    /**
     * ????????????????????????????????????????????????
     */
    @Test
    public void testImportUsers_01() {
        // ????????????
        UserImportExcelVO importUser = randomPojo(UserImportExcelVO.class, o -> {
        });
        // mock ?????????????????????
        doThrow(new ServiceException(DEPT_NOT_FOUND)).when(deptService).validDepts(any());

        // ??????
        UserImportRespVO respVO = userService.importUsers(newArrayList(importUser), true);
        // ??????
        assertEquals(0, respVO.getCreateUsernames().size());
        assertEquals(0, respVO.getUpdateUsernames().size());
        assertEquals(1, respVO.getFailureUsernames().size());
        assertEquals(DEPT_NOT_FOUND.getMsg(), respVO.getFailureUsernames().get(importUser.getUsername()));
    }

    /**
     * ????????????????????????????????????
     */
    @Test
    public void testImportUsers_02() {
        // ????????????
        UserImportExcelVO importUser = randomPojo(UserImportExcelVO.class, o -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // ?????? status ?????????
            o.setSex(randomEle(SexEnum.values()).getSex()); // ?????? sex ?????????
        });
        // mock deptService ?????????
        DeptDO dept = randomPojo(DeptDO.class, o -> {
            o.setId(importUser.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);
        // mock passwordEncoder ?????????
        when(passwordEncoder.encode(eq("yudaoyuanma"))).thenReturn("java");

        // ??????
        UserImportRespVO respVO = userService.importUsers(newArrayList(importUser), true);
        // ??????
        assertEquals(1, respVO.getCreateUsernames().size());
        AdminUserDO user = userMapper.selectByUsername(respVO.getCreateUsernames().get(0));
        assertPojoEquals(importUser, user);
        assertEquals("java", user.getPassword());
        assertEquals(0, respVO.getUpdateUsernames().size());
        assertEquals(0, respVO.getFailureUsernames().size());
    }

    /**
     * ??????????????????????????????????????????
     */
    @Test
    public void testImportUsers_03() {
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO();
        userMapper.insert(dbUser);
        // ????????????
        UserImportExcelVO importUser = randomPojo(UserImportExcelVO.class, o -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // ?????? status ?????????
            o.setSex(randomEle(SexEnum.values()).getSex()); // ?????? sex ?????????
            o.setUsername(dbUser.getUsername());
        });
        // mock deptService ?????????
        DeptDO dept = randomPojo(DeptDO.class, o -> {
            o.setId(importUser.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);

        // ??????
        UserImportRespVO respVO = userService.importUsers(newArrayList(importUser), false);
        // ??????
        assertEquals(0, respVO.getCreateUsernames().size());
        assertEquals(0, respVO.getUpdateUsernames().size());
        assertEquals(1, respVO.getFailureUsernames().size());
        assertEquals(USER_USERNAME_EXISTS.getMsg(), respVO.getFailureUsernames().get(importUser.getUsername()));
    }

    /**
     * ?????????????????????????????????
     */
    @Test
    public void testImportUsers_04() {
        // mock ??????
        AdminUserDO dbUser = randomAdminUserDO();
        userMapper.insert(dbUser);
        // ????????????
        UserImportExcelVO importUser = randomPojo(UserImportExcelVO.class, o -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // ?????? status ?????????
            o.setSex(randomEle(SexEnum.values()).getSex()); // ?????? sex ?????????
            o.setUsername(dbUser.getUsername());
        });
        // mock deptService ?????????
        DeptDO dept = randomPojo(DeptDO.class, o -> {
            o.setId(importUser.getDeptId());
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        when(deptService.getDept(eq(dept.getId()))).thenReturn(dept);

        // ??????
        UserImportRespVO respVO = userService.importUsers(newArrayList(importUser), true);
        // ??????
        assertEquals(0, respVO.getCreateUsernames().size());
        assertEquals(1, respVO.getUpdateUsernames().size());
        AdminUserDO user = userMapper.selectByUsername(respVO.getUpdateUsernames().get(0));
        assertPojoEquals(importUser, user);
        assertEquals(0, respVO.getFailureUsernames().size());
    }

    @Test
    public void testCheckUserExists_notExists() {
        assertServiceException(() -> userService.checkUserExists(randomLongId().toString()), USER_NOT_EXISTS);
    }

    @Test
    public void testCheckUsernameUnique_usernameExistsForCreate() {
        // ????????????
        String username = randomString();
        // mock ??????
        userMapper.insert(randomAdminUserDO(o -> o.setUsername(username)));

        // ?????????????????????
        assertServiceException(() -> userService.checkUsernameUnique(null, username),
                USER_USERNAME_EXISTS);
    }

    @Test
    public void testCheckUsernameUnique_usernameExistsForUpdate() {
        // ????????????
        String id = randomLongId().toString();
        String username = randomString();
        // mock ??????
        userMapper.insert(randomAdminUserDO(o -> o.setUsername(username)));

        // ?????????????????????
        assertServiceException(() -> userService.checkUsernameUnique(id, username),
                USER_USERNAME_EXISTS);
    }

    @Test
    public void testCheckEmailUnique_emailExistsForCreate() {
        // ????????????
        String email = randomString();
        // mock ??????
        userMapper.insert(randomAdminUserDO(o -> o.setEmail(email)));

        // ?????????????????????
        assertServiceException(() -> userService.checkEmailUnique(null, email),
                USER_EMAIL_EXISTS);
    }

    @Test
    public void testCheckEmailUnique_emailExistsForUpdate() {
        // ????????????
        String id = randomLongId().toString();
        String email = randomString();
        // mock ??????
        userMapper.insert(randomAdminUserDO(o -> o.setEmail(email)));

        // ?????????????????????
        assertServiceException(() -> userService.checkEmailUnique(id, email),
                USER_EMAIL_EXISTS);
    }

    @Test
    public void testCheckMobileUnique_mobileExistsForCreate() {
        // ????????????
        String mobile = randomString();
        // mock ??????
        userMapper.insert(randomAdminUserDO(o -> o.setMobile(mobile)));

        // ?????????????????????
        assertServiceException(() -> userService.checkMobileUnique(null, mobile),
                USER_MOBILE_EXISTS);
    }

    @Test
    public void testCheckMobileUnique_mobileExistsForUpdate() {
        // ????????????
        String id = randomLongId().toString();
        String mobile = randomString();
        // mock ??????
        userMapper.insert(randomAdminUserDO(o -> o.setMobile(mobile)));

        // ?????????????????????
        assertServiceException(() -> userService.checkMobileUnique(id, mobile),
                USER_MOBILE_EXISTS);
    }

    @Test
    public void testCheckOldPassword_notExists() {
        assertServiceException(() -> userService.checkOldPassword(randomLongId().toString(), randomString()),
                USER_NOT_EXISTS);
    }

    @Test
    public void testCheckOldPassword_passwordFailed() {
        // mock ??????
        AdminUserDO user = randomAdminUserDO();
        userMapper.insert(user);
        // ????????????
        String id = user.getId();
        String oldPassword = user.getPassword();

        // ?????????????????????
        assertServiceException(() -> userService.checkOldPassword(id, oldPassword),
                USER_PASSWORD_FAILED);
        // ????????????
        verify(passwordEncoder, times(1)).matches(eq(oldPassword), eq(user.getPassword()));
    }

    @Test
    public void testUsersByPostIds() {
        // ????????????
        Collection<String> postIds = asSet("10", "20");
        // mock user1 ??????
        AdminUserDO user1 = randomAdminUserDO(o -> o.setPostIds(asSet("10", "30")));
        userMapper.insert(user1);
        userPostMapper.insert(new UserPostDO().setUserId(user1.getId()).setPostId("10"));
        userPostMapper.insert(new UserPostDO().setUserId(user1.getId()).setPostId("30"));
        // mock user2 ??????
        AdminUserDO user2 = randomAdminUserDO(o -> o.setPostIds(singleton("100")));
        userMapper.insert(user2);
        userPostMapper.insert(new UserPostDO().setUserId(user2.getId()).setPostId("100"));

        // ??????
        List<AdminUserDO> result = userService.getUsersByPostIds(postIds);
        // ??????
        assertEquals(1, result.size());
        assertEquals(user1, result.get(0));
    }

    // ========== ???????????? ==========

    @SafeVarargs
    private static AdminUserDO randomAdminUserDO(Consumer<AdminUserDO>... consumers) {
        Consumer<AdminUserDO> consumer = (o) -> {
            o.setStatus(randomEle(CommonStatusEnum.values()).getStatus()); // ?????? status ?????????
            o.setSex(randomEle(SexEnum.values()).getSex()); // ?????? sex ?????????
        };
        return randomPojo(AdminUserDO.class, ArrayUtils.append(consumer, consumers));
    }

}
