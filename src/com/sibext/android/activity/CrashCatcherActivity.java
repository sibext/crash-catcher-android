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

package com.sibext.android.activity;

import android.app.Activity;
import android.os.Bundle;
import com.sibext.android.manager.CrashCatcherManager;

public class CrashCatcherActivity extends Activity {
    
    private final CrashCatcherManager manager = new CrashCatcherManager();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        manager.register(this);
        super.onCreate(savedInstanceState);
    }
    
    protected CrashCatcherManager getCrashCatcherManager(){
        return manager;
    }
    
    @Override
    protected void onDestroy() {
        manager.unRegister();
        super.onDestroy();
    }
}
