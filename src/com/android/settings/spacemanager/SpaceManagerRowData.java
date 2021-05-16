package com.android.settings.spacemanager;

import android.graphics.drawable.Drawable;

public class SpaceManagerRowData {
    
    private Drawable appIcon;
    private String appName;
    private RestrictionRecord restrictionRecord;

    public SpaceManagerRowData(Drawable appIcon, String appName, RestrictionRecord restrictionRecord) {

        this.appIcon = appIcon;
        this.appName = appName;
        this.restrictionRecord = restrictionRecord;
    }

    // get app icon
    public Drawable getAppIcon() {
        return this.appIcon;
    }

    // get app name
    public String getAppName() {
        return this.appName;
    }

    // get restriction record
    public RestrictionRecord getRestrictionRecord() {
        return this.restrictionRecord;
    }

    // get restrictionText
    public String getRestrictionText() {
        return this.restrictionRecord.getPermission() + " | " + this.restrictionRecord.getEnforcer();
    }

    // get detailed text
    public String getDetailedText() {
        if (this.restrictionRecord.getPermission().equals("*")) {
            return this.appName + " is disabled by " + this.restrictionRecord.getEnforcer();
        }
        else if (this.restrictionRecord.getAppId().equals("*")) {
            return this.restrictionRecord.getPermission() + " is restricted on all applications by " + this.restrictionRecord.getEnforcer();
        }
        else {
            return this.restrictionRecord.getPermission() + " is restricted on " + this.appName + " by " + this.restrictionRecord.getEnforcer();
        }
    }
}
