package com.itrihua.caritas.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itrihua.caritas.annotaion.AuthCheck;
import com.itrihua.caritas.common.BaseResponse;
import com.itrihua.caritas.common.DeleteRequest;
import com.itrihua.caritas.common.ResultUtils;
import com.itrihua.caritas.constant.UserConstant;
import com.itrihua.caritas.exception.BusinessException;
import com.itrihua.caritas.exception.ErrorCode;
import com.itrihua.caritas.exception.ThrowUtils;
import com.itrihua.caritas.manager.auth.model.SpaceUserPermission;
import com.itrihua.caritas.manager.auth.model.SpaceUserPermissionConstant;
import com.itrihua.caritas.manager.upload.MultipartFilePictureUpload;
import com.itrihua.caritas.manager.upload.PictureUploadTemplate;
import com.itrihua.caritas.model.dto.file.UploadPictureResult;
import com.itrihua.caritas.model.dto.user.*;
import com.itrihua.caritas.model.entity.Picture;
import com.itrihua.caritas.model.entity.User;
import com.itrihua.caritas.model.vo.picture.PictureVO;
import com.itrihua.caritas.model.vo.user.LoginUserVO;
import com.itrihua.caritas.model.vo.user.UserVO;
import com.itrihua.caritas.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Resource
    private UserService userService;

    @Resource
    private MultipartFilePictureUpload multipartFilePictureUpload;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount(); // userAccount即用户账号,用户邮箱
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String tempCode = userRegisterRequest.getTempCode();
        long result = userService.userRegister(userAccount, userPassword, checkPassword, tempCode);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        log.info("用户登录");
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    /**
     * 获取当前登录用户
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        log.info("用户注销");
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 创建用户
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        log.info("创建用户");
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 复制参数
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        if (!user.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            user.setUserRole(UserConstant.DEFAULT_ROLE);
        }
        // 默认密码 12345678 ,并加密
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        // 保存用户
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建失败");
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        log.info("根据id获取用户");
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        log.info("根据id获取脱敏用户");
        BaseResponse<User> response = getUserById(id);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 删除用户
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        log.info("删除用户");
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null || userUpdateRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        // 复制参数
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 分页获取用户封装列表（仅管理员）
     *
     * @param userQueryRequest 查询请求参数
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        log.info("分页获取用户封装列表");
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<User> userPage = userService.page(
                new Page<>(userQueryRequest.getCurrent(), userQueryRequest.getPageSize()),
                userService.getQueryWrapper(userQueryRequest));
        // 获取封装列表
        List<UserVO> userVOList = userService.getUserVOList(userPage.getRecords());
        // 封装列表Page
        Page<UserVO> userVOPage = new Page<>(userQueryRequest.getCurrent(), userQueryRequest.getPageSize(), userPage.getTotal());
        userVOPage.setRecords(userVOList);

        return ResultUtils.success(userVOPage);
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/upload/avator")
    public BaseResponse<PictureVO> uploadAvator(@RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        ThrowUtils.throwIf(multipartFile == null,ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        PictureUploadTemplate pictureUploadTemplate = multipartFilePictureUpload;
        String uploadPathprefix = String.format("avator/%s", loginUser.getId());
        UploadPictureResult uploadPictureResult = pictureUploadTemplate.uploadPicture(multipartFile, uploadPathprefix);
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(uploadPathprefix,pictureVO);
        String avatorUrl = uploadPictureResult.getUrl();

        User oldUser = userService.getById(loginUser.getId());
        oldUser.setUserAvatar(avatorUrl);
        boolean save = userService.updateById(oldUser);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR);
        return ResultUtils.success(pictureVO);
    }

}
