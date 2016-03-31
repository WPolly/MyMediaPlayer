package com.xiaoshan.mymediaplayer.utils;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;

public class ServiceStatusUtils {

	public static boolean isServiceRunning(Context context, String serviceName) {

		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> runningServices = am.getRunningServices(300);
		
		if (runningServices.size() > 0) {
			for (RunningServiceInfo runningServiceInfo : runningServices) {
				String className = runningServiceInfo.service.getClassName();
				if (serviceName.equals(className)) {
					return true;
				}
			}
		}
		return false;
	}
}
