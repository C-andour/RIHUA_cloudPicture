package com.itrihua.caritas.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itrihua.caritas.api.aliyunai.model.CreateOutPaintingTaskResponse;
import com.itrihua.caritas.model.dto.picture.*;
import com.itrihua.caritas.model.entity.Picture;
import com.itrihua.caritas.model.entity.User;
import com.itrihua.caritas.model.vo.picture.PictureVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 86147
* @description 针对表【picture(图片)】的数据库操作Service
* @createDate 2025-01-16 20:23:13
*/
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param inputSource 文件源 (文件,url)
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    /**
     * 分页查询图片
     * @param pictureQueryRequest
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取分页图片封装
     * @param picturePage
     * @param request
     * @return
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);

    /**
     * 获取单个图片封装
     * @param picture
     * @param request
     * @return
     */
    PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 校验图片
     * @param picture
     */
    void validPicture(Picture picture);

    /**
     * 图片审核
     *
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);

    /**
     * 内容调用,用于填充审核信息
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 清理图片文件
     * @param picture
     */
    public void clearPictureFile(Picture picture);

    /**
     * 删除图片
     * @param PictureId
     * @param loginUser
     */
    public void deletePicture(Long PictureId, User loginUser);

    /**
     * 编辑图片
     * @param pictureEditRequest
     * @param loginUser
     */
    public void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 校验图片权限(空间校验)
     * @param picture
     * @param loginUser
     */
    public void checkPictureAuth(Picture picture, User loginUser);

    /**
     * 提取URL中的KEY(存储路径)
     * @param pictureUrl
     */
    public String extractKeyFromUrl(String pictureUrl);

    /**
     * 根据颜色搜索个人空间内的图片
     * @param spaceId
     * @param picColor
     * @param loginUser
     * @return
     */
    public List<PictureVO> searchPictureByColor(Long spaceId, String picColor, User loginUser);

    /**
     * 创建扩图任务
     * @param createPictureOutPaintingTaskRequest
     * @param loginUser
     * @return
     */
    CreateOutPaintingTaskResponse createPictureOutPaintingTask(CreatePictureOutPaintingTaskRequest createPictureOutPaintingTaskRequest, User loginUser);
}
