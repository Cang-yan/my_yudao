package cn.iocoder.yudao.module.system.api.permission;

import cn.iocoder.yudao.module.system.api.permission.dto.DeptDataPermissionRespDTO;
import cn.iocoder.yudao.module.system.service.permission.PermissionService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Set;

/**
 * 权限 API 实现类
 *
 * @author 芋道源码
 */
@Service
public class PermissionApiImpl implements PermissionApi {

    @Resource
    private PermissionService permissionService;

    @Override
    public Set<String> getUserRoleIdListByRoleIds(Collection<String> roleIds) {
        return permissionService.getUserRoleIdListByRoleIds(roleIds);
    }

    @Override
    public boolean hasAnyPermissions(String userId, String... permissions) {
        return permissionService.hasAnyPermissions(userId, permissions);
    }

    @Override
    public boolean hasAnyRoles(String userId, String... roles) {
        return permissionService.hasAnyRoles(userId, roles);
    }

    @Override
    public DeptDataPermissionRespDTO getDeptDataPermission(String userId) {
        return permissionService.getDeptDataPermission(userId);
    }

}
