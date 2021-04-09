package com.android.settings.appspermission;

public class RestrictedPermissionAppList {
    private String authorityId;
    private String packageName;
    private String permissionType;

    // constructor
    public RestrictedPermissionAppList(String authorityId, String packageName, String permissionType) {
        this.authorityId = authorityId;
        this.packageName = packageName;
        this.permissionType = permissionType;
    }

    // get authority id
    public String getAuthorityId() {
        return this.authorityId;
    }

    // get permission type
    public String getPackageName() {
        return this.packageName;
    }

    // get app id
    public String getPermissionType() {
        return this.permissionType;
    }
}
