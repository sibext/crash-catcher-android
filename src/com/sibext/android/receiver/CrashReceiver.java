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

package com.sibext.android.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sibext.android.tools.CatchActivity;

public class CrashReceiver extends BroadcastReceiver {
    private static final String TAG = "[Sibext] CrashReceiver";
    public static final String EXTRA_REPORTER_CLASS = TAG + "EXTRA_REPORTER_CLASS";
    public static final String EXTRA_TRACE_INFO = TAG + "TRACE_INFO";
    public static final String ACTION_CATCHED_CRASH = "com.sibext.android.CATCHED_CRASH_ACTION";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, "onReceive new crash");
        final Intent crashedIntent = new Intent(context.getApplicationContext(),
                (Class<?>) intent.getSerializableExtra(EXTRA_REPORTER_CLASS));
        crashedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        crashedIntent.putExtra(CatchActivity.TRACE_INFO, intent.getStringExtra(EXTRA_TRACE_INFO));
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                context.startActivity(crashedIntent);
            }
        });
    }

}
