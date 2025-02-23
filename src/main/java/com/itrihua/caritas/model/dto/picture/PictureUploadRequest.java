package com.itrihua.caritas.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

@Data
public class PictureUploadRequest implements Serializable {
  
    /**  
     * 图片 id（用于修改）  
     */  
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 文件地址 URL
     */
    private String fileUrl;

    private static final long serialVersionUID = 1L;  
}
