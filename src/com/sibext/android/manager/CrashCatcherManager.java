/**
 * This file is part of CrashCatcher library.
 * Copyright (c) 2014, Sibext Ltd. (http://www.sibext.com), 
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License 
 * for more details (http://www.gnu.org/licenses/lgpl-3.0.txt).
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */

package com.sibext.android.manager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.sibext.android.activity.EmailReportActivity;
import com.sibext.android.tools.CatchActivity;
import com.sibext.android.tools.StackTraceHelper;
import com.sibext.crashcatcher.R;

public class CrashCatcherManager {
    private static final String TAG = "[CCL] CrashCatcherManager";

    public static final String MANUAL_FLAG_KEY = "MANUAL_FLAG_KEY";
    
    private Context context;
    private Class<?> catchClass = null;

    private void init() {
        catchClass = getCatchClass();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            private volatile boolean alreadyCrashed = false;
            @Override
            public void uncaughtException(Thread paramThread, final Throwable e) {
                if (alreadyCrashed) return;
                alreadyCrashed = true;
				try{
					final String stackTrace = StackTraceHelper.getStackTrace(e);
		            Log.e(TAG, "Error: " + stackTrace);
		            sendReport(stackTrace, false);
				} catch (Throwable e1) {
                    try {
                        Log.e(TAG, "Can't handle  the crash", e1);
                    } catch (Throwable e2) {}
                }
            }

        });
    }
    
    public void manualSendReport(){
        sendReport("Manual", true);
    }
    
    public void sendReport(String stackTrace, final boolean manual){
        final Intent crashedIntent;
        if (catchClass != null) {
            crashedIntent = new Intent(context.getApplicationContext(), catchClass);
        } else {
            crashedIntent = new Intent(context.getApplicationContext(), getDefaultReporterClass());
        }
        crashedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        crashedIntent.putExtra(CatchActivity.TRACE_INFO, stackTrace);
        crashedIntent.putExtra(MANUAL_FLAG_KEY, manual);
        final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, crashedIntent, 0);
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    pendingIntent.send(context.getApplicationContext(), 0, null);
                    if(manual){
                        Log.d(TAG, "sent manual report");
                    } else {
                        Log.d(TAG, "sent new crash");
                         android.os.Process.killProcess(android.os.Process.myPid());
                         System.exit(10);
                    }
                } catch (PendingIntent.CanceledException e) {
                    Log.d(TAG, "Can't start activity", e);
                }
            }
        });
    }

    public void register(Context c) {
        this.context = c;
        init();
    }

    public void unRegister() {
//        context = null;
    }

    protected Class<?> getDefaultReporterClass() {
        return EmailReportActivity.class;
    }
}
