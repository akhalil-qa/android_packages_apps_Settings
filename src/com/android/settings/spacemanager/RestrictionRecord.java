package com.android.settings.spacemanager;

public class RestrictionRecord {

    private String enforcer;
    private String permission;
    private String appId;

    // constructor
    public RestrictionRecord(String enforcer, String permission, String appId) {
        this.enforcer = enforcer;
        this.permission = permission;
        this.appId = appId;
    }

    // get enforcer
    public String getEnforcer() {
        return this.enforcer;
    }

    // get permission type
    public String getPermission() {
        return this.permission;
    }

    // get app id
    public String getAppId() {
        return this.appId;
    }
}
