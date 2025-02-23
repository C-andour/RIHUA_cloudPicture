package com.itrihua.caritas.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.itrihua.caritas.exception.BusinessException;
import com.itrihua.caritas.exception.ErrorCode;
import com.itrihua.caritas.exception.ThrowUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class URLFilePictureUpload extends PictureUploadTemplate{
    /**
     * 获取图片名称
     * @param inputSource
     * @return
     */
    @Override
    protected String getOriginName(Object inputSource) {
        String url = (String) inputSource;
        return FileUtil.mainName(url);
    }

    /***
     * 校验图片
     * @param inputSource 图片文件 / url
     */
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        ThrowUtils.throwIf(fileUrl == null, ErrorCode.PARAMS_ERROR, "文件地址不能为空");

        try {
            //验证是否为合法的URL
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        //2.校验 URL 协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ErrorCode.PARAMS_ERROR, "请求的文件必须为 HTTP 或 HTTPS 协议");

        //3.通过 HEAD 请求验证文件是否存在
        try(HttpResponse response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute()) {
            if (response.getStatus() != HttpStatus.HTTP_OK){
                return;
            }
            //文件存在, 验证文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)){
                ThrowUtils.throwIf(!contentType.startsWith("image/"), ErrorCode.PARAMS_ERROR, "请求的文件不是图片");
            }
            final List<String> allowList = Arrays.asList("image/jpeg","image/jpg","image/webp","image/png");
            ThrowUtils.throwIf(!allowList.contains(contentType), ErrorCode.PARAMS_ERROR, "请求的文件不是图片");

            //文件存在, 验证文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)){
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    final long ONE_M = 1024 * 1024L;
                    ThrowUtils.throwIf(contentLength > 5 * ONE_M, ErrorCode.PARAMS_ERROR, "文件大小不能超过 5M");
                } catch (NumberFormatException e){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 2M");
                }
            }
        }
    }

    /**
     * 下载图片到临时文件
     * @param inputSource 源文件来源
     * @param file        临时文件
     */
    @Override
    protected void downloadAndSaveTempFile(Object inputSource, File file) {
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, file);
    }
}
