/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;

public class ApplicationSettings extends SettingsPreferenceFragment implements
        DialogInterface.OnClickListener {
    
    private static final String KEY_TOGGLE_INSTALL_APPLICATIONS = "toggle_install_applications";
    private static final String KEY_TOGGLE_ADVANCED_SETTINGS = "toggle_advanced_settings";
    private static final String KEY_APP_INSTALL_LOCATION = "app_install_location";

    // App installation location. Default is ask the user.
    private static final int APP_INSTALL_AUTO = 0;
    private static final int APP_INSTALL_DEVICE = 1;
    private static final int APP_INSTALL_SDCARD = 2;
    
    private static final String APP_INSTALL_DEVICE_ID = "device";
    private static final String APP_INSTALL_SDCARD_ID = "sdcard";
    private static final String APP_INSTALL_AUTO_ID = "auto";
    
    private CheckBoxPreference mToggleAppInstallation;
    private CheckBoxPreference mToggleAdvancedSettings;
    private ListPreference mInstallLocation;
    private DialogInterface mWarnInstallApps;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.application_settings);

        mToggleAppInstallation = (CheckBoxPreference)findPreference(
                KEY_TOGGLE_INSTALL_APPLICATIONS);
        mToggleAppInstallation.setChecked(isNonMarketAppsAllowed());

        mToggleAdvancedSettings = (CheckBoxPreference)findPreference(
                KEY_TOGGLE_ADVANCED_SETTINGS);
        mToggleAdvancedSettings.setChecked(isAdvancedSettingsEnabled());
        getPreferenceScreen().removePreference(mToggleAdvancedSettings);

        // not ready for prime time yet
        if (false) {
            getPreferenceScreen().removePreference(mInstallLocation);
        }

        mInstallLocation = (ListPreference) findPreference(KEY_APP_INSTALL_LOCATION);
        // Is app default install location set?
        boolean userSetInstLocation = (Settings.System.getInt(getContentResolver(),
                Settings.Secure.SET_INSTALL_LOCATION, 0) != 0);
        if (!userSetInstLocation) {
            getPreferenceScreen().removePreference(mInstallLocation);
        } else {
            mInstallLocation.setValue(getAppInstallLocation());
            mInstallLocation.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    String value = (String) newValue;
                    handleUpdateAppInstallLocation(value);
                    return false;
                }
            });
        }
    }

    protected void handleUpdateAppInstallLocation(final String value) {
        if(APP_INSTALL_DEVICE_ID.equals(value)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_DEVICE);
        } else if (APP_INSTALL_SDCARD_ID.equals(value)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_SDCARD);
        } else if (APP_INSTALL_AUTO_ID.equals(value)) {
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        } else {
            // Should not happen, default to prompt...
            Settings.System.putInt(getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        }
        mInstallLocation.setValue(value);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mWarnInstallApps != null) {
            mWarnInstallApps.dismiss();
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mToggleAppInstallation) {
            if (mToggleAppInstallation.isChecked()) {
                mToggleAppInstallation.setChecked(false);
                warnAppInstallation();
            } else {
                setNonMarketAppsAllowed(false);
            }
        } else if (preference == mToggleAdvancedSettings) {
            boolean value = mToggleAdvancedSettings.isChecked();
            setAdvancedSettingsEnabled(value);
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void onClick(DialogInterface dialog, int which) {
        if (dialog == mWarnInstallApps && which == DialogInterface.BUTTON_POSITIVE) {
            setNonMarketAppsAllowed(true);
            mToggleAppInstallation.setChecked(true);
        }
    }

    private void setNonMarketAppsAllowed(boolean enabled) {
        // Change the system setting
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.INSTALL_NON_MARKET_APPS, 
                                enabled ? 1 : 0);
    }

    private boolean isAdvancedSettingsEnabled() {
        return Settings.System.getInt(getContentResolver(), 
                                      Settings.System.ADVANCED_SETTINGS,
                                      Settings.System.ADVANCED_SETTINGS_DEFAULT) > 0;
    }

    private void setAdvancedSettingsEnabled(boolean enabled) {
        int value = enabled ? 1 : 0;
        // Change the system setting
        Settings.Secure.putInt(getContentResolver(), Settings.System.ADVANCED_SETTINGS, value);
        // TODO: the settings thing should broadcast this for thread safety purposes.
        Intent intent = new Intent(Intent.ACTION_ADVANCED_SETTINGS_CHANGED);
        intent.putExtra("state", value);
        getActivity().sendBroadcast(intent);
    }

    private boolean isNonMarketAppsAllowed() {
        return Settings.Secure.getInt(getContentResolver(), 
                                      Settings.Secure.INSTALL_NON_MARKET_APPS, 0) > 0;
    }

    private String getAppInstallLocation() {
        int selectedLocation = Settings.System.getInt(getContentResolver(),
                Settings.Secure.DEFAULT_INSTALL_LOCATION, APP_INSTALL_AUTO);
        if (selectedLocation == APP_INSTALL_DEVICE) {
            return APP_INSTALL_DEVICE_ID;
        } else if (selectedLocation == APP_INSTALL_SDCARD) {
            return APP_INSTALL_SDCARD_ID;
        } else  if (selectedLocation == APP_INSTALL_AUTO) {
            return APP_INSTALL_AUTO_ID;
        } else {
            // Default value, should not happen.
            return APP_INSTALL_AUTO_ID;
        }
    }

    private void warnAppInstallation() {
        // TODO: DialogFragment?
        mWarnInstallApps = new AlertDialog.Builder(getActivity()).setTitle(
                getResources().getString(R.string.error_title))
                .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                .setMessage(getResources().getString(R.string.install_all_warning))
                .setPositiveButton(android.R.string.yes, this)
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
