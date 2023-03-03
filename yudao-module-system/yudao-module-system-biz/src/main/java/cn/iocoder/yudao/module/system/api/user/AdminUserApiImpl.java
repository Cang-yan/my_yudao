package cn.iocoder.yudao.module.system.api.user;

import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import cn.iocoder.yudao.module.system.convert.user.UserConvert;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.service.user.AdminUserService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Admin 用户 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class AdminUserApiImpl implements AdminUserApi {

    @Resource
    private AdminUserService userService;

    @Override
    public Integer getUserCount() {
        return userService.list().size();

    }

    @Override
    public AdminUserRespDTO getUser(String id) {
        AdminUserDO user = userService.getUser(id);
        return UserConvert.INSTANCE.convert4(user);
    }

    @Override
    public String getUserNickName(String id) {
        AdminUserDO user = userService.getUser(id);
        return user.getNickname();
    }

    @Override
    public List<AdminUserRespDTO> getUsers(Collection<String> ids) {
        List<AdminUserDO> users = userService.getUsers(ids);
        return UserConvert.INSTANCE.convertList4(users);
    }

    @Override
    public List<AdminUserRespDTO> getUsersByDeptIds(Collection<String> deptIds) {
        List<AdminUserDO> users = userService.getUsersByDeptIds(deptIds);
        return UserConvert.INSTANCE.convertList4(users);
    }

    @Override
    public List<AdminUserRespDTO> getUsersByPostIds(Collection<String> postIds) {
        List<AdminUserDO> users = userService.getUsersByPostIds(postIds);
        return UserConvert.INSTANCE.convertList4(users);
    }

    @Override
    public void validUsers(Set<String> ids) {
        userService.validUsers(ids);
    }

}
