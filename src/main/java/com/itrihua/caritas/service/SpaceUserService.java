package com.itrihua.caritas.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itrihua.caritas.model.dto.spaceuser.SpaceUserAddRequest;
import com.itrihua.caritas.model.dto.spaceuser.SpaceUserQueryRequest;
import com.itrihua.caritas.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itrihua.caritas.model.vo.spaceuser.SpaceUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 86147
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2025-02-16 20:04:57
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     *  添加空间成员
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 查询单个空间成员封装类
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     *  查询空间成员封装类列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);

    /**
     *  检验空间成员权限
     * @param add  是否为新增校验?/编辑时校验
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     *  构建查询条件
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);
}
