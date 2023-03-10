package cn.iocoder.yudao.module.system.service.auth;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.enums.UserTypeEnum;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.framework.test.core.util.AssertUtils;
import cn.iocoder.yudao.module.system.api.sms.SmsCodeApi;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthLoginReqVO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.AuthLoginRespVO;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.enums.logger.LoginLogTypeEnum;
import cn.iocoder.yudao.module.system.enums.logger.LoginResultEnum;
import cn.iocoder.yudao.module.system.service.logger.LoginLogService;
import cn.iocoder.yudao.module.system.service.member.MemberService;
import cn.iocoder.yudao.module.system.service.oauth2.OAuth2TokenService;
import cn.iocoder.yudao.module.system.service.social.SocialUserService;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import com.anji.captcha.service.CaptchaService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;
import javax.validation.Validator;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomString;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Import(AdminAuthServiceImpl.class)
public class AdminAuthServiceImplTest extends BaseDbUnitTest {

    @Resource
    private AdminAuthServiceImpl authService;

    @MockBean
    private AdminUserService userService;
    @MockBean
    private CaptchaService captchaService;
    @MockBean
    private LoginLogService loginLogService;
    @MockBean
    private SocialUserService socialService;
    @MockBean
    private SmsCodeApi smsCodeApi;
    @MockBean
    private OAuth2TokenService oauth2TokenService;
    @MockBean
    private MemberService memberService;

    @MockBean
    private Validator validator;

    @Test
    public void testAuthenticate_success() {
        // ????????????
        String username = randomString();
        String password = randomString();
        // mock user ??????
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username)
                .setPassword(password).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(userService.getUserByUsername(eq(username))).thenReturn(user);
        // mock password ??????
        when(userService.isPasswordMatch(eq(password), eq(user.getPassword()))).thenReturn(true);

        // ??????
        AdminUserDO loginUser = authService.authenticate(username, password);
        // ??????
        assertPojoEquals(user, loginUser);
    }

    @Test
    public void testAuthenticate_userNotFound() {
        // ????????????
        String username = randomString();
        String password = randomString();

        // ??????, ???????????????
        AssertUtils.assertServiceException(() -> authService.authenticate(username, password),
                AUTH_LOGIN_BAD_CREDENTIALS);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.BAD_CREDENTIALS.getResult())
                        && o.getUserId() == null)
        );
    }

    @Test
    public void testAuthenticate_badCredentials() {
        // ????????????
        String username = randomString();
        String password = randomString();
        // mock user ??????
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username)
                .setPassword(password).setStatus(CommonStatusEnum.ENABLE.getStatus()));
        when(userService.getUserByUsername(eq(username))).thenReturn(user);

        // ??????, ???????????????
        AssertUtils.assertServiceException(() -> authService.authenticate(username, password),
                AUTH_LOGIN_BAD_CREDENTIALS);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.BAD_CREDENTIALS.getResult())
                        && o.getUserId().equals(user.getId()))
        );
    }

    @Test
    public void testAuthenticate_userDisabled() {
        // ????????????
        String username = randomString();
        String password = randomString();
        // mock user ??????
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setUsername(username)
                .setPassword(password).setStatus(CommonStatusEnum.DISABLE.getStatus()));
        when(userService.getUserByUsername(eq(username))).thenReturn(user);
        // mock password ??????
        when(userService.isPasswordMatch(eq(password), eq(user.getPassword()))).thenReturn(true);

        // ??????, ???????????????
        AssertUtils.assertServiceException(() -> authService.authenticate(username, password),
                AUTH_LOGIN_USER_DISABLED);
        verify(loginLogService).createLoginLog(
                argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
                        && o.getResult().equals(LoginResultEnum.USER_DISABLED.getResult())
                        && o.getUserId().equals(user.getId()))
        );
    }

//    @Test
//    public void testCaptcha_success() {
//        // ????????????
//        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
//
//        // mock ???????????????
//        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
//
//        // ??????
//        authService.verifyCaptcha(reqVO);
//        // ??????
//        verify(captchaService).deleteCaptchaCode(reqVO.getUuid());
//    }
//
//    @Test
//    public void testCaptcha_notFound() {
//        // ????????????
//        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
//
//        // ??????, ???????????????
//        assertServiceException(() -> authService.verifyCaptcha(reqVO), AUTH_LOGIN_CAPTCHA_NOT_FOUND);
//        // ??????????????????
//        verify(loginLogService, times(1)).createLoginLog(
//            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
//                    && o.getResult().equals(LoginResultEnum.CAPTCHA_NOT_FOUND.getResult()))
//        );
//    }

//    @Test
//    public void testCaptcha_codeError() {
//        // ????????????
//        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class);
//
//        // mock ??????????????????
//        String code = randomString();
//        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(code);
//
//        // ??????, ???????????????
//        assertServiceException(() -> authService.verifyCaptcha(reqVO), AUTH_LOGIN_CAPTCHA_CODE_ERROR);
//        // ??????????????????
//        verify(loginLogService).createLoginLog(
//            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
//                    && o.getResult().equals(LoginResultEnum.CAPTCHA_CODE_ERROR.getResult()))
//        );
//    }

//    @Test
//    public void testLogin_success() {
//        // ????????????
//        AuthLoginReqVO reqVO = randomPojo(AuthLoginReqVO.class, o ->
//                o.setUsername("test_username").setPassword("test_password"));
//
//        // mock ???????????????
//        when(captchaService.getCaptchaCode(reqVO.getUuid())).thenReturn(reqVO.getCode());
//        // mock user ??????
//        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setId(1L).setUsername("test_username")
//                .setPassword("test_password").setStatus(CommonStatusEnum.ENABLE.getStatus()));
//        when(userService.getUserByUsername(eq("test_username"))).thenReturn(user);
//        // mock password ??????
//        when(userService.isPasswordMatch(eq("test_password"), eq(user.getPassword()))).thenReturn(true);
//        // mock ????????????????????? Redis
//        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class, o -> o.setUserId(1L)
//                .setUserType(UserTypeEnum.ADMIN.getValue()));
//        when(oauth2TokenService.createAccessToken(eq(1L), eq(UserTypeEnum.ADMIN.getValue()), eq("default"), isNull()))
//                .thenReturn(accessTokenDO);
//
//        // ??????, ???????????????
//        AuthLoginRespVO loginRespVO = authService.login(reqVO);
//        assertPojoEquals(accessTokenDO, loginRespVO);
//        // ??????????????????
//        verify(loginLogService).createLoginLog(
//            argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGIN_USERNAME.getType())
//                    && o.getResult().equals(LoginResultEnum.SUCCESS.getResult())
//                    && o.getUserId().equals(user.getId()))
//        );
//    }

    @Test
    public void testLogout_success() {
        // ????????????
        String token = randomString();
        // mock
        OAuth2AccessTokenDO accessTokenDO = randomPojo(OAuth2AccessTokenDO.class, o -> o.setUserId("1")
                .setUserType(UserTypeEnum.ADMIN.getValue()));
        when(oauth2TokenService.removeAccessToken(eq(token))).thenReturn(accessTokenDO);

        // ??????
        authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        // ??????????????????
        verify(loginLogService).createLoginLog(argThat(o -> o.getLogType().equals(LoginLogTypeEnum.LOGOUT_SELF.getType())
                    && o.getResult().equals(LoginResultEnum.SUCCESS.getResult()))
        );
    }

    @Test
    public void testLogout_fail() {
        // ????????????
        String token = randomString();

        // ??????
        authService.logout(token, LoginLogTypeEnum.LOGOUT_SELF.getType());
        // ??????????????????
        verify(loginLogService, never()).createLoginLog(any());
    }

}
