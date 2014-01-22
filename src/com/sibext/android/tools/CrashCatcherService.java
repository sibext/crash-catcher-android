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

package com.sibext.android.tools;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.sibext.android.manager.CrashCatcherManager;

public class CrashCatcherService extends Service {
    private CrashCatcherManager catcherManager;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onCreate() {
        catcherManager = new CrashCatcherManager();
        catcherManager.register(this);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (catcherManager != null) {
            catcherManager.unRegister();
            catcherManager = null;
        }
        super.onDestroy();
    }
}
