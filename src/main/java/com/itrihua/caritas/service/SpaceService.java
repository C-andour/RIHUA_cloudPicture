package com.itrihua.caritas.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itrihua.caritas.model.dto.space.SpaceAddRequest;
import com.itrihua.caritas.model.dto.space.SpaceQueryRequest;
import com.itrihua.caritas.model.entity.Picture;
import com.itrihua.caritas.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.itrihua.caritas.model.entity.User;
import com.itrihua.caritas.model.vo.picture.PictureVO;
import com.itrihua.caritas.model.vo.space.SpaceVO;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86147
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-02-08 12:15:53
*/
public interface SpaceService extends IService<Space> {

    /**
     * 添加空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 根据空间等级扩容
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间
     * @param space 空间
     * @param add 是否为创建
     */
    public void validSpace(Space space, boolean add);

    /**
     * 获取空间封装类
     * @param space
     * @param request
     * @return
     */
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取分页空间封装
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 空间分页查询,Query条件拼接
     * 采用方法: getSpaceVOPage
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 空间权限校验
     * @param oldSpace
     * @param loginUser
     */
    void checkSpaceAuth(Space oldSpace, User loginUser);
}
