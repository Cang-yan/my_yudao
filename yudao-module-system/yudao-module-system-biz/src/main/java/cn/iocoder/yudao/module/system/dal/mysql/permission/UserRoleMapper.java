package cn.iocoder.yudao.module.system.dal.mysql.permission;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.UserRoleDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Mapper
public interface UserRoleMapper extends BaseMapperX<UserRoleDO> {

    default List<UserRoleDO> selectListByUserId(String userId) {
        return selectList(new QueryWrapper<UserRoleDO>().eq("user_id", userId));
    }

    default List<UserRoleDO> selectListByRoleId(String roleId) {
        return selectList(new QueryWrapper<UserRoleDO>().eq("role_id", roleId));
    }

    default void deleteListByUserIdAndRoleIdIds(String userId, Collection<String> roleIds) {
        delete(new QueryWrapper<UserRoleDO>().eq("user_id", userId)
                .in("role_id", roleIds));
    }

    default void deleteListByUserId(String userId) {
        delete(new QueryWrapper<UserRoleDO>().eq("user_id", userId));
    }

    default void deleteListByRoleId(String roleId) {
        delete(new QueryWrapper<UserRoleDO>().eq("role_id", roleId));
    }


    default List<UserRoleDO> selectListByRoleIds(Collection<String> roleIds) {
        return selectList(UserRoleDO::getRoleId, roleIds);
    }

    @Select("SELECT COUNT(*) FROM system_user_role WHERE update_time > #{maxUpdateTime}")
    Long selectCountByUpdateTimeGt(Date maxUpdateTime);

}
