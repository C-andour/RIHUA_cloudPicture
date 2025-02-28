package com.itrihua.caritas.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itrihua.caritas.constant.UserConstant;
import com.itrihua.caritas.exception.BusinessException;
import com.itrihua.caritas.exception.ErrorCode;
import com.itrihua.caritas.exception.ThrowUtils;
import com.itrihua.caritas.manager.auth.StpKit;
import com.itrihua.caritas.manager.mail.EmailService;
import com.itrihua.caritas.model.dto.user.UserQueryRequest;
import com.itrihua.caritas.model.entity.User;
import com.itrihua.caritas.model.enums.SpaceRoleEnum;
import com.itrihua.caritas.model.enums.UserRoleEnum;
import com.itrihua.caritas.model.vo.user.LoginUserVO;
import com.itrihua.caritas.model.vo.user.UserVO;
import com.itrihua.caritas.service.UserService;
import com.itrihua.caritas.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.itrihua.caritas.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 86147
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-01-09 16:35:58
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private EmailService emailService;

    /**
     * 用户注册
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String tempCode) {
        // 1. 校验 账号/密码/验证码是否为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword, tempCode), ErrorCode.PARAMS_ERROR, "账号或密码为空");
        // 2. 校验账号/密码过短
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        // 3. 验证两次密码一致
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        // 验证已有验证码是否正确
        emailService.verifiyTempCode(userAccount, tempCode, "register");
        // 4. 检查是否存在重复账号
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(); //定义查询构造类(mybatis-plus)
        queryWrapper.eq("userAccount", userAccount);
        long count = this.baseMapper.selectCount(queryWrapper);
        ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "账号重复");
        // 5. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 6. 插入数据
        User user = new User();
        user.setUserAvatar("https://rihua-caritas-1325753913.cos.ap-guangzhou.myqcloud.com/avator/1877324709656350722/2025-02-26_7Sj3wgXmAbn6eYGX.webp");
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue()); // 默认角色
        boolean saveResult = this.save(user);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "注册失败,数据库发生错误");
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword), ErrorCode.PARAMS_ERROR, "账号/密码为空");
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "账号错误");
        // 2. 加密
        String encryptPassword = getEncryptPassword(userPassword);
        // 3. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount)
                .eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或账号密码错误");
        // 4. 记录用户的登录态 (自定义aop)
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 5. 记录用户登录态到 Sa-token,以便于空间鉴权时使用,同时,需要保证与上述的springSession过期时间一致
        StpKit.SPACE.login(user.getId(),StpKit.SPACE_TYPE);  //登录与登录状态
        StpKit.SPACE.getSession().set(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 用户重置密码
     */
    @Override
    public Long userResetPw(String userAccount, String userPassword, String checkPassword, String resetCode) {
        //  校验 账号/密码/验证码是否为空
        ThrowUtils.throwIf(StrUtil.hasBlank(userAccount, userPassword, checkPassword, resetCode), ErrorCode.PARAMS_ERROR, "账号密码为空");
        //  校验账号/密码过短
        ThrowUtils.throwIf(userAccount.length() < 4, ErrorCode.PARAMS_ERROR, "用户账号过短");
        //  验证两次密码一致
        ThrowUtils.throwIf(!userPassword.equals(checkPassword), ErrorCode.PARAMS_ERROR, "两次密码不一致");
        //  检查是否存在重复账号
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(); //定义查询构造类(mybatis-plus)
        queryWrapper.eq("userAccount", userAccount);
        User oldUser = this.baseMapper.selectOne(queryWrapper);
        ThrowUtils.throwIf(oldUser == null, ErrorCode.PARAMS_ERROR, "重置的账号不存在");
        // 验证已有验证码是否正确
        emailService.verifiyTempCode(userAccount, resetCode, "reset");
        //  加密
        String encryptPassword = getEncryptPassword(userPassword);
        //  插入数据
        User user = new User();
        user.setId(oldUser.getId());
        user.setUserAvatar("https://rihua-caritas-1325753913.cos.ap-guangzhou.myqcloud.com/avator/1877324709656350722/2025-02-26_7Sj3wgXmAbn6eYGX.webp");
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUserName(userAccount);
        user.setUserRole(UserRoleEnum.USER.getValue()); // 默认角色
        boolean updated = this.updateById(user);
        ThrowUtils.throwIf(!updated, ErrorCode.SYSTEM_ERROR, "重置密码,数据库发生错误");
        return user.getId();
    }

    /**
     * 加密
     */
    @Override
    public String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "rihua";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

    /**
     * 获取当前登录用户封装类
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }


    /**
     * 获取当前登录用户（不允许未登录）
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        ThrowUtils.throwIf(currentUser == null || currentUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        // 从数据库查询（追求性能的话可以注释，直接返回上述结果）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        ThrowUtils.throwIf(currentUser == null, ErrorCode.NOT_FOUND_ERROR, "该用户不存在");
        return currentUser;
    }

    /**
     * 用户注销
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        ThrowUtils.throwIf(userObj == null, ErrorCode.OPERATION_ERROR, "未登录");
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取脱敏用户信息
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 将用户列表 转为 用户脱敏列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollUtil.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 构造分页查询条件
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null,ErrorCode.PARAMS_ERROR);
        //获取参数
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        //构造查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id)
                .eq(StrUtil.isNotBlank(userRole), "userRole", userRole)
                .like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount)
                .like(StrUtil.isNotBlank(userName), "userName", userName)
                .like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile)
                .orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 是否为管理员
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }


}




