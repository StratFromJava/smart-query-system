package com.example.sqs.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.sqs.core.entity.SysUser;
import com.example.sqs.core.mapper.SysUserMapper;
import com.example.sqs.core.service.SysUserService;
import com.example.sqs.core.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现。
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public SysUser getByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
    }

    @Override
    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(String username, String encodedPassword, String nickname) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setNickname(nickname == null || nickname.isBlank() ? username : nickname);
        user.setStatus(1);
        sysUserMapper.insert(user);
        return user.getId();
    }

    @Override
    public boolean existsUsername(String username) {
        return sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) > 0;
    }

    @Override
    public UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
