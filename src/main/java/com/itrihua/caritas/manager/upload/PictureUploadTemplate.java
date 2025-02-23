package com.itrihua.caritas.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.itrihua.caritas.config.CosClientConfig;
import com.itrihua.caritas.exception.BusinessException;
import com.itrihua.caritas.exception.ErrorCode;
import com.itrihua.caritas.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;


@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource      源文件来源
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 校验图片
        validPicture(inputSource);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginName(inputSource);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        //临时文件
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null);
            //将文件写入临时文件
            downloadAndSaveTempFile(inputSource, file);
            // 上传图片 (此处上传的为处理后的图片,但返回的结果为原始图片数据,不符合,因此需要分支,返回处理后数据)
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            // 原图数据
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            List<CIObject> objectList = putObjectResult.getCiUploadResult().getProcessResults().getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                // 压缩图数据
                CIObject compressedObject = objectList.get(0);
                // 缩率图数据 (当且仅当存在,否则 压缩图 即 缩率图)
                CIObject thumbnailObject = compressedObject;
                if (objectList.size() > 1){
                    thumbnailObject = objectList.get(1);
                }
                //封装压缩图返回结果
                return getUploadPictureResult(originFilename, compressedObject, thumbnailObject, imageInfo);
            }
            //封装原图返回结果
            return getUploadPictureResult(imageInfo, originFilename, file, uploadPath);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            this.deleteTempFile(file);
        }
    }

    /**
     * 获取文件原始名称
     *
     * @param inputSource
     * @return
     */
    protected abstract String getOriginName(Object inputSource);

    /**
     * 校验文件
     *
     * @param inputSource 图片文件 / url
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 处理文件下载,将其保存为临时文件
     *
     * @param inputSource 源文件来源
     * @param file        临时文件
     */
    protected abstract void downloadAndSaveTempFile(Object inputSource, File file);

    /**
     * 封装原图返回结果
     *
     * @param imageInfo      图片信息
     * @param originFilename 原始文件名称
     * @param file           临时文件
     * @param uploadPath     文件路径
     * @return
     */
    private UploadPictureResult getUploadPictureResult(ImageInfo imageInfo, String originFilename, File file, String uploadPath) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicColor(imageInfo.getAve());
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        return uploadPictureResult;
    }

    /**
     * 封装返回结果(通过处理后的图片)
     * @param originFilename
     * @param compressedCiObject
     * @return
     */
    private UploadPictureResult getUploadPictureResult(String originFilename, CIObject compressedCiObject, CIObject thumbnailObject, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicColor(imageInfo.getAve());
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        // 设置图片为压缩后的地址
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        // 设置缩率图
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailObject.getKey());
        return uploadPictureResult;

    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

}
