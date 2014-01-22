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

package com.sibext.android.sysinfo;

import android.util.Log;

public class SystemInfoBuilder {
    private static final String TAG = "[CCL] SystemInfoBuilder";

    private static String SYSTEM_INFO_IMPLEMENTATION[] = new String[] { 
        "Legacy", 
        "V8",
        "V9",
        "V14",
        "V19", 
    };

    public String build() {
        int index = 0;
        switch (android.os.Build.VERSION.SDK_INT) {
        case 1:
        case 2:
        case 3:
        case 4:
        case 5:
        case 6:
        case 7:
            index = 0;
            break;
        case 8:
            index = 1;
            break;
        case 9:
        case 10:
        case 11:
        case 12:
        case 13:
            index = 2;
            break;
        case 14:
            index = 3;
            break;
        case 19:
            index = 4;
            break;

        default:
            index = 3;
            break;
        }
        try {
            Class<?> impl = Class.forName("com.sibext.android.sysinfo.SystemInfo" + SYSTEM_INFO_IMPLEMENTATION[index]);
            ISystemInfo systemInfo = (ISystemInfo) impl.newInstance();
            return systemInfo.build();
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "System Info implementation is not found", e);
        } catch (InstantiationException e) {
            Log.e(TAG, "System Info implementation is not found", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "System Info implementation is not found", e);
        }
        return "EMPTY";
    }
}
