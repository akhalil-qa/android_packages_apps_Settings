package com.android.settings.appspermission;

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



public class AppsPermissions extends InstrumentedFragment {

	private static final String TAG = "AppPermission";
     private View mRootView;
    PackageManager packageManager;
    ArrayList<RestrictedPermissionAppList> appPermissionDetails;
    ArrayList<BasicData> data;
    SpaceManager spaceManager;

    @Override
    public int getMetricsCategory() {
        return SettingsEnums.SETTINGS_APP_NOTIF_CATEGORY;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        final Activity activity = getActivity();
        try {
            List<String> list = new ArrayList<String>();;
            spaceManager=SpaceManager.getInstance();
            //get the List<String> passed by Service
            list=spaceManager.getRestrictionRecord();
            for (int i=0;i < list.size();i++)//DEBUG
                {
                  System.out.println("SpaceManager list: "+list.get(i));
                }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("SpaceManager "+e);
        }
        Resources res = getContext().getResources();
        String screenTitle = res.getString(R.string.space_manager);
        activity.setTitle(screenTitle);
        packageManager = getContext().getPackageManager();
        data= new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView=inflater.inflate(R.layout.manage_application_permissions, null);
        //TODO by Ahmed
        //Pass the list returned from service
        DisplayAppDetails(appPermissionDetails);
        // createHeader();
        return mRootView;
    }

      private void DisplayAppDetails(ArrayList<RestrictedPermissionAppList> appDetails) {
        //Example
        // String pName= "com.google.android.apps.messaging";
        // Drawable icon= getAppIcon(pName);
        // String appName = getAppName(pName);
        // String restrictedBy = "Restricted by Authority 1";
        // data.add(new BasicData(icon,appName,restrictedBy));
        // initRecyclerView();
        // This below example is for ArrayList<Object> and you will be using List<String>,
        // so you will have to make necessary modification to the code. The example works as 
        // expected in the case of ArrayList but require changes in case of List<String>.
       for (int j = 0; j < appDetails.size(); j++) {
           Drawable icon= getAppIcon(appDetails.get(j).getPackageName());
           String appName = getAppName(appDetails.get(j).getPackageName());
           String restrictedBy = "Restricted by "+ appDetails.get(j).getAuthorityId();
           data.add(new BasicData(icon,appName,restrictedBy));
           initRecyclerView();
       }   
    }

    private String getAppName(String packageName) {
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            String appName = (String) packageManager.getApplicationLabel(info);
            return appName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private Drawable getAppIcon(String packageName) {
        try
        {
            Drawable icon = packageManager.getApplicationIcon(packageName);
            return icon;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    private void initRecyclerView() {
        if((data==null) || (data.isEmpty())){
            TextView noApplication= mRootView.findViewById(R.id.empty);
            noApplication.setVisibility(View.GONE); 
        }
        RecyclerView recyclerView =mRootView.findViewById(R.id.apps_list);
        ApplicationsAdapter adapter = new ApplicationsAdapter(data,getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }


    private class ApplicationsAdapter extends RecyclerView.Adapter<ApplicationsAdapter.ViewHolder>{

        private static final String TAG = "RecyclerViewAdapter";

        ArrayList<BasicData> mData;
        private Context mContext;

        public ApplicationsAdapter(ArrayList<BasicData> mData, Context mContext) {
            this.mData = mData;
            this.mContext=mContext;
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.app_detail_layout_list,parent,false);
            ViewHolder holder= new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull final ApplicationsAdapter.ViewHolder holder, final int position) {
            holder.title.setText(mData.get(position).appName);
//        holder.title.setPaintFlags( holder.title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

            holder.authorityId.setText(mData.get(position).restrictedBy);

            holder.appIcon.setImageDrawable(mData.get(position).imageId);
            holder.parent_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Permission View Clicked");
                    //TODO by Ahmed 
                    // Add Permission received from service to String Array
                    // Used AlertDialog.Builder to display them on click.
                    String[] Permission = {"location", "storage", "contacts"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Permissions");
                    builder.setItems(Permission, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // the user clicked on permission[which]
                        }
                    });
                    builder.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder{

            TextView title;
            TextView authorityId;
            ImageView appIcon;
            LinearLayout parent_layout;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                title=itemView.findViewById(R.id.AppName);
                authorityId=itemView.findViewById(R.id.restrictedBy);
                appIcon=itemView.findViewById(R.id.icon);
                parent_layout=itemView.findViewById(R.id.parent_layout);
            }
        }

    }
}
