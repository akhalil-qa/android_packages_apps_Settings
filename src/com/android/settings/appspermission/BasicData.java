package com.android.settings.appspermission;

import android.graphics.drawable.Drawable;

public class BasicData {
    public Drawable imageId;
    public String appName;
    public String restrictedBy;

    BasicData( Drawable imageId, String appName, String restrictedBy) {

        this.imageId = imageId;
        this.appName = appName;
        this.restrictedBy = restrictedBy;
    }
}
