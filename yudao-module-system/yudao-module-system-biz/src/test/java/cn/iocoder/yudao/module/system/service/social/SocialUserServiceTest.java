package cn.iocoder.yudao.module.system.service.social;

import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbAndRedisUnitTest;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserBindDO;
import cn.iocoder.yudao.module.system.dal.dataobject.social.SocialUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserBindMapper;
import cn.iocoder.yudao.module.system.dal.mysql.social.SocialUserMapper;
import cn.iocoder.yudao.module.system.enums.social.SocialTypeEnum;
import com.xkcoding.justauth.AuthRequestFactory;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomLong;
import static cn.hutool.core.util.RandomUtil.randomString;
import static cn.iocoder.yudao.framework.common.util.json.JsonUtils.toJsonString;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SOCIAL_USER_AUTH_FAILURE;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.SOCIAL_USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Import(SocialUserServiceImpl.class)
public class SocialUserServiceTest extends BaseDbAndRedisUnitTest {

    @Resource
    private SocialUserServiceImpl socialUserService;

    @Resource
    private SocialUserMapper socialUserMapper;
    @Resource
    private SocialUserBindMapper socialUserBindMapper;

    @MockBean
    private AuthRequestFactory authRequestFactory;

    @Test
    public void testGetAuthorizeUrl() {
        try (MockedStatic<AuthStateUtils> authStateUtilsMock = mockStatic(AuthStateUtils.class)) {
            // ????????????
            Integer type = SocialTypeEnum.WECHAT_MP.getType();
            String redirectUri = "sss";
            // mock ??????????????? AuthRequest ??????
            AuthRequest authRequest = mock(AuthRequest.class);
            when(authRequestFactory.get(eq("WECHAT_MP"))).thenReturn(authRequest);
            // mock ??????
            authStateUtilsMock.when(AuthStateUtils::createState).thenReturn("aoteman");
            when(authRequest.authorize(eq("aoteman"))).thenReturn("https://www.iocoder.cn?redirect_uri=yyy");

            // ??????
            String url = socialUserService.getAuthorizeUrl(type, redirectUri);
            // ??????
            assertEquals("https://www.iocoder.cn/?redirect_uri=sss", url);
        }
    }

    @Test
    public void testAuthSocialUser_exists() {
        // ????????????
        Integer type = SocialTypeEnum.GITEE.getType();
        String code = "tudou";
        String state = "yuanma";
        // mock ??????
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(type).setCode(code).setState(state);
        socialUserMapper.insert(socialUser);

        // ??????
        SocialUserDO result = socialUserService.authSocialUser(type, code, state);
        // ??????
        assertPojoEquals(socialUser, result);
    }

    @Test
    public void testAuthSocialUser_authFailure() {
        // ????????????
        Integer type = SocialTypeEnum.GITEE.getType();
        // mock ??????
        AuthRequest authRequest = mock(AuthRequest.class);
        when(authRequestFactory.get(anyString())).thenReturn(authRequest);
        AuthResponse<?> authResponse = new AuthResponse<>(0, "????????????", null);
        when(authRequest.login(any(AuthCallback.class))).thenReturn(authResponse);

        // ???????????????
        assertServiceException(
                () -> socialUserService.authSocialUser(type, randomString(10), randomString(10)),
                SOCIAL_USER_AUTH_FAILURE, "????????????");
    }

    @Test
    public void testAuthSocialUser_insert() {
        // ????????????
        Integer type = SocialTypeEnum.GITEE.getType();
        String code = "tudou";
        String state = "yuanma";
        // mock ??????
        AuthRequest authRequest = mock(AuthRequest.class);
        when(authRequestFactory.get(eq(SocialTypeEnum.GITEE.getSource()))).thenReturn(authRequest);
        AuthUser authUser = randomPojo(AuthUser.class);
        AuthResponse<AuthUser> authResponse = new AuthResponse<>(AuthResponseStatus.SUCCESS.getCode(), null, authUser);
        when(authRequest.login(any(AuthCallback.class))).thenReturn(authResponse);

        // ??????
        SocialUserDO result = socialUserService.authSocialUser(type, code, state);
        // ??????
        assertBindSocialUser(type, result, authResponse.getData());
        assertEquals(code, result.getCode());
        assertEquals(state, result.getState());
    }

    @Test
    public void testAuthSocialUser_update() {
        // ????????????
        Integer type = SocialTypeEnum.GITEE.getType();
        String code = "tudou";
        String state = "yuanma";
        // mock ??????
        socialUserMapper.insert(randomPojo(SocialUserDO.class).setType(type).setOpenid("test_openid"));
        // mock ??????
        AuthRequest authRequest = mock(AuthRequest.class);
        when(authRequestFactory.get(eq(SocialTypeEnum.GITEE.getSource()))).thenReturn(authRequest);
        AuthUser authUser = randomPojo(AuthUser.class);
        authUser.getToken().setOpenId("test_openid");
        AuthResponse<AuthUser> authResponse = new AuthResponse<>(AuthResponseStatus.SUCCESS.getCode(), null, authUser);
        when(authRequest.login(any(AuthCallback.class))).thenReturn(authResponse);

        // ??????
        SocialUserDO result = socialUserService.authSocialUser(type, code, state);
        // ??????
        assertBindSocialUser(type, result, authResponse.getData());
        assertEquals(code, result.getCode());
        assertEquals(state, result.getState());
    }

    private void assertBindSocialUser(Integer type, SocialUserDO socialUser, AuthUser authUser) {
        assertEquals(authUser.getToken().getAccessToken(), socialUser.getToken());
        assertEquals(toJsonString(authUser.getToken()), socialUser.getRawTokenInfo());
        assertEquals(authUser.getNickname(), socialUser.getNickname());
        assertEquals(authUser.getAvatar(), socialUser.getAvatar());
        assertEquals(toJsonString(authUser.getRawUserInfo()), socialUser.getRawUserInfo());
        assertEquals(type, socialUser.getType());
        assertEquals(authUser.getUuid(), socialUser.getOpenid());
    }

    @Test
    public void testGetSocialUserList() {
        String userId = "1";
        Integer userType = UserTypeEnum.ADMIN.getValue();
        // mock ??????????????????
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(SocialTypeEnum.GITEE.getType());
        socialUserMapper.insert(socialUser); // ????????????
        socialUserMapper.insert(randomPojo(SocialUserDO.class)); // ???????????????
        // mock ????????????
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class) // ???????????????
                .setUserId(userId).setUserType(userType).setSocialType(SocialTypeEnum.GITEE.getType())
                .setSocialUserId(socialUser.getId()));
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class) // ??????????????????
                .setUserId("2").setUserType(userType).setSocialType(SocialTypeEnum.DINGTALK.getType()));

        // ??????
        List<SocialUserDO> result = socialUserService.getSocialUserList(userId, userType);
        // ??????
        assertEquals(1, result.size());
        assertPojoEquals(socialUser, result.get(0));
    }

    @Test
    public void testBindSocialUser() {
        // ????????????
        SocialUserBindReqDTO reqDTO = new SocialUserBindReqDTO()
                .setUserId("1").setUserType(UserTypeEnum.ADMIN.getValue())
                .setType(SocialTypeEnum.GITEE.getType()).setCode("test_code").setState("test_state");
        // mock ???????????????????????????
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(reqDTO.getType())
                .setCode(reqDTO.getCode()).setState(reqDTO.getState());
        socialUserMapper.insert(socialUser);
        // mock ?????????????????????????????????????????????????????????
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class).setUserId("1").setUserType(UserTypeEnum.ADMIN.getValue())
                .setSocialType(SocialTypeEnum.GITEE.getType()).setSocialUserId("-1"));
        // mock ??????????????????????????????????????????????????????
        socialUserBindMapper.insert(randomPojo(SocialUserBindDO.class).setUserType(UserTypeEnum.ADMIN.getValue())
                .setSocialType(SocialTypeEnum.GITEE.getType()).setSocialUserId(socialUser.getId()));

        // ??????
        socialUserService.bindSocialUser(reqDTO);
        // ??????
        List<SocialUserBindDO> socialUserBinds = socialUserBindMapper.selectList();
        assertEquals(1, socialUserBinds.size());
    }

    @Test
    public void testUnbindSocialUser_success() {
        // ????????????
        String userId = "1";
        Integer userType = UserTypeEnum.ADMIN.getValue();
        Integer type = SocialTypeEnum.GITEE.getType();
        String openid = "test_openid";
        // mock ?????????????????????
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(type).setOpenid(openid);
        socialUserMapper.insert(socialUser);
        // mock ???????????????????????????
        SocialUserBindDO socialUserBind = randomPojo(SocialUserBindDO.class).setUserType(userType)
                .setUserId(userId).setSocialType(type);
        socialUserBindMapper.insert(socialUserBind);

        // ??????
        socialUserService.unbindSocialUser(userId, userType, type, openid);
        // ??????
        assertEquals(0, socialUserBindMapper.selectCount(null).intValue());
    }

    @Test
    public void testUnbindSocialUser_notFound() {
        // ??????????????????
        assertServiceException(
                () -> socialUserService.unbindSocialUser(String.valueOf(randomLong()), UserTypeEnum.ADMIN.getValue(),
                        SocialTypeEnum.GITEE.getType(), "test_openid"),
                SOCIAL_USER_NOT_FOUND);
    }

    @Test
    public void testGetBindUserId() {
        // ????????????
        Integer userType = UserTypeEnum.ADMIN.getValue();
        Integer type = SocialTypeEnum.GITEE.getType();
        String code = "tudou";
        String state = "yuanma";
        // mock ????????????
        SocialUserDO socialUser = randomPojo(SocialUserDO.class).setType(type).setCode(code).setState(state);
        socialUserMapper.insert(socialUser);
        // mock ?????????????????????
        String userId = String.valueOf(randomLong());
        SocialUserBindDO socialUserBind = randomPojo(SocialUserBindDO.class).setUserType(userType).setUserId(userId)
                .setSocialType(type).setSocialUserId(socialUser.getId());
        socialUserBindMapper.insert(socialUserBind);

        // ??????
        String result = socialUserService.getBindUserId(userType, type, code, state);
        // ??????
        assertEquals(userId, result);
    }

}
