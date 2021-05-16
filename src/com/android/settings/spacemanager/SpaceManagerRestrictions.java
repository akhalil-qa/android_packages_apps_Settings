package com.android.settings.spacemanager;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;
import android.app.settings.SettingsEnums;
import android.app.Fragment;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.SettingsActivity;
import androidx.annotation.VisibleForTesting;
import android.app.Activity;
import android.content.Intent;
import android.widget.FrameLayout;
import android.content.res.Resources;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.ServiceManager;
import android.os.ISpaceManager;
import android.os.SpaceManager;
import java.util.List;
import android.os.IBinder;
import android.os.ServiceManager.ServiceNotFoundException;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.util.Log;

public class SpaceManagerRestrictions extends InstrumentedFragment {

    private View mRootView;
    PackageManager packageManager;
    SpaceManager spaceManager;
    ArrayList<SpaceManagerRowData> rowsData;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_APP_NOTIF_CATEGORY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // get screen title
        Resources resource = getContext().getResources();
        String screenTitle = resource.getString(R.string.space_manager);

        // set screen title
        final Activity activity = getActivity();
        activity.setTitle(screenTitle);

        // get pacakge manager
        packageManager = getContext().getPackageManager();


        // initialize rows data array
        rowsData = new ArrayList<SpaceManagerRowData>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.space_manager_main_list, null);

        // get restriction records from SpaceManager
        ArrayList<RestrictionRecord> restrictionRecords = new ArrayList<RestrictionRecord>();
        try {       
            spaceManager = SpaceManager.getInstance();
            List<String> list = spaceManager.getRestrictionRecords();
            for (String str : list) {
                String[] splitString = str.split("-", 0);
                restrictionRecords.add(new RestrictionRecord(splitString[0], splitString[1], splitString[2]));
            }      
        } catch (Exception e) {
            Log.d("TSM", "error while trying to call android.os.SpaceManager.java instance or its methods: " + e);
        }

        // display restrictions list
        displayRestrictionsList(restrictionRecords);

        return mRootView;
    }

    // get app icon
    private Drawable getAppIcon(String packageName) {
        try {
            return packageManager.getApplicationIcon(packageName);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    // get app name
    private String getAppName(String packageName) {
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return (String)packageManager.getApplicationLabel(info);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    // display restriction records in the list screen
    private void displayRestrictionsList(ArrayList<RestrictionRecord> restrictionRecords) {

        // if restrction record is not empty or null
        if(!restrictionRecords.isEmpty() || restrictionRecords != null) {
            for (RestrictionRecord restrictionRecord : restrictionRecords) {
                // read the required data to be presented
                Drawable appIcon;
                String appName;
                if (restrictionRecord.getAppId().equals("*")) {
                    Resources resource = getContext().getResources();
                    appIcon = resource.getDrawable(R.drawable.ic_homepage_space_manager, null);
                    appName = "[" + restrictionRecord.getPermission() + "]";
                } else {
                    appIcon = getAppIcon(restrictionRecord.getAppId());
                    appName = getAppName(restrictionRecord.getAppId());
                }
            
                // add the data to the row representation
                rowsData.add(new SpaceManagerRowData(appIcon, appName, restrictionRecord));
            }
        }
        initRecyclerView(); 
    }

    private void initRecyclerView() {

        if ((rowsData == null) || (rowsData.isEmpty()) ) {
            TextView noRestrictions = mRootView.findViewById(R.id.empty);
            noRestrictions.setVisibility(View.GONE); 
        }

        RecyclerView recyclerView = mRootView.findViewById(R.id.apps_list);

        ApplicationsAdapter adapter = new ApplicationsAdapter(rowsData, getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder> {

        ArrayList<SpaceManagerRowData> mData;
        private Context mContext;

        public ApplicationsAdapter(ArrayList<SpaceManagerRowData> mData, Context mContext) {
            this.mData = mData;
            this.mContext = mContext;
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.space_manager_row_main_list, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull final ApplicationsAdapter.ViewHolder holder, final int position) {

            // show the app icon, app name, and restriction text
            holder.appIcon.setImageDrawable(mData.get(position).getAppIcon());
            holder.appName.setText(mData.get(position).getAppName());
            holder.restrictionText.setText(mData.get(position).getRestrictionText());

            // when a row is clicked
            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // show an alert dialog with restriction details
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Applied Restriction");
                    builder.setMessage(mData.get(position).getDetailedText());
                    builder.setCancelable(true);
                    builder.show();

                    /*
                    // show an alert dialog with list of items
                    String[] list = {"item1", "item2", "item2", "item4"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Title");
                    builder.setItems(list, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {}
                    });
                    builder.show();
                    */
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        // representation of a row
        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView appIcon;
            TextView appName;
            TextView restrictionText;
            LinearLayout parentLayout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                appIcon = itemView.findViewById(R.id.app_icon);
                appName = itemView.findViewById(R.id.app_name);
                restrictionText = itemView.findViewById(R.id.restriction_text);
                parentLayout = itemView.findViewById(R.id.parent_layout);
            }
        }
    }
}
