package com.mrbluyee.djautocontrol.application;
import android.app.Application;
import android.content.Context;
import com.secneo.sdk.Helper;
public class MApplication extends Application {
    private DJSDKApplication djsdkApplication;
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(MApplication.this);
        if (djsdkApplication == null) {
            djsdkApplication = new DJSDKApplication();
            djsdkApplication.setContext(this);
        }
    }
    @Override
    public void onCreate() {
        super.onCreate();
        djsdkApplication.onCreate();
    }
}