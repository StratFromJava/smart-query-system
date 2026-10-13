package com.example.sqs.core.service;

import com.example.sqs.core.entity.SysUser;
import com.example.sqs.core.vo.UserVO;

/**
 * 用户领域服务。
 */
public interface SysUserService {

    /**
     * 按用户名查询用户（未找到返回 null）。
     */
    SysUser getByUsername(String username);

    /**
     * 按 ID 查询用户。
     */
    SysUser getById(Long id);

    /**
     * 注册新用户，密码需已加密。
     *
     * @return 新建用户 ID
     */
    Long register(String username, String encodedPassword, String nickname);

    /**
     * 判断用户名是否已存在。
     */
    boolean existsUsername(String username);

    /**
     * 转换为视图对象。
     */
    UserVO toVO(SysUser user);
}
