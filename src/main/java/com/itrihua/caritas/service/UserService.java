package com.itrihua.caritas.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itrihua.caritas.model.dto.user.UserQueryRequest;
import com.itrihua.caritas.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itrihua.caritas.model.vo.user.LoginUserVO;
import com.itrihua.caritas.model.vo.user.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 86147
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-01-09 16:35:58
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String tempCode);

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户重置密码
     */
    Long userResetPw(String userAccount, String userPassword, String checkPassword, String resetCode);

    /**
     * 获取加密密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获取当前登录用户封装类
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 将用户列表 转为 用户脱敏列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 构造分页查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     */
    boolean isAdmin(User user);

}


