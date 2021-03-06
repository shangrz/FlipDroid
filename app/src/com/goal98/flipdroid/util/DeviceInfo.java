package com.goal98.flipdroid.util;

import android.app.Activity;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.Window;
import com.goal98.flipdroid.activity.FlipdroidApplications;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 5/26/11
 * Time: 6:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeviceInfo {
    private int displayWidth;
    private int displayHeight;
    private int width;
    private int height;
    private static DeviceInfo deviceInfo;
    private float density;

    private DeviceInfo() {

    }

    public static synchronized DeviceInfo getInstance(Activity activity) {
        if (deviceInfo == null) {
            Rect rect = new Rect();
            Window window = activity.getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rect);
            int statusBarHeight = rect.top;
            int contentViewTop =
                    window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
            int titleBarHeight = contentViewTop - statusBarHeight;
            System.out.println("creating new device info");
            deviceInfo = new DeviceInfo();
            deviceInfo.setDisplayHeight((int) ((activity.getWindowManager().getDefaultDisplay().getHeight())*5/6));
            deviceInfo.setDisplayWidth((activity.getWindowManager().getDefaultDisplay().getWidth()) - 20);
            deviceInfo.setWidth(activity.getWindowManager().getDefaultDisplay().getWidth());
            deviceInfo.setHeight(activity.getWindowManager().getDefaultDisplay().getHeight());
            deviceInfo.setDensityScale(activity.getResources().getDisplayMetrics().density);
            FlipdroidApplications application = (FlipdroidApplications) activity.getApplication();
            application.setDeviceInfo(deviceInfo);
            System.out.println("creating new device info done" + deviceInfo);
        }
        return deviceInfo;

    }

    private void setDensityScale(float density) {
        this.density = density;
    }

    public float getDensity() {
        return DisplayMetrics.DENSITY_DEFAULT*density;
    }

    public int getDisplayHeight() {
        return displayHeight;
    }

    public void setDisplayHeight(int displayHeight) {
        this.displayHeight = displayHeight;
    }

    public int getDisplayWidth() {
        return displayWidth;
    }

    public void setDisplayWidth(int displayWidth) {
        this.displayWidth = displayWidth;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }


    public boolean isLargeScreen() {
        return height >= 800;
    }

    public boolean isSmallScreen() {
        return height == 320;
    }

    public int getDipFromPixel(int px) {
        return (int) (px*160/ this.getDensity());
    }

    @Override
    public String toString() {
        return "display:" + displayWidth + "," + displayHeight+", actual:"+width +"," + height+",scale:" + density;
    }
}
