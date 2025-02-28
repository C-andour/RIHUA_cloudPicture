package com.itrihua.caritas.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * RABC(role-based access control)
 *  用户 - 角色 - 权限
 *  从 resources - biz - spaceUserAuthConfig.json 文件中读取权限角色列表
 */
@Data
public class SpaceUserAuthConfig implements Serializable {

    /**
     * 权限列表
     */
    private List<SpaceUserPermission> permissions;

    /**
     * 角色列表
     */
    private List<SpaceUserRole> roles;

    private static final long serialVersionUID = 1L;
}
