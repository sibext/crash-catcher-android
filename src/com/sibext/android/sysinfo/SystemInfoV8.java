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

import android.annotation.TargetApi;

@SuppressWarnings("deprecation")
@TargetApi(8)
public class SystemInfoV8 extends AbstractSystemInfo {
    @Override
	public String build() {
		StringBuilder sb = new StringBuilder("--SYSTEM V8--").append(DELIMITER);
		add(sb, "BOARD", android.os.Build.BOARD);
		add(sb, "BOOTLOADER", android.os.Build.BOOTLOADER);
		add(sb, "BRAND", android.os.Build.BRAND);
		add(sb, "CPU_ABI", android.os.Build.CPU_ABI);
		add(sb, "CPU_ABI2", android.os.Build.CPU_ABI2);
		add(sb, "DEVICE", android.os.Build.DEVICE);
		add(sb, "DISPLAY", android.os.Build.DISPLAY);
		add(sb, "FINGERPRINT", android.os.Build.FINGERPRINT);
		add(sb, "HARDWARE", android.os.Build.HARDWARE);
		add(sb, "HOST", android.os.Build.HOST);
		add(sb, "ID", android.os.Build.ID);
		add(sb, "MANUFACTURER", android.os.Build.MANUFACTURER);
		add(sb, "MODEL", android.os.Build.MODEL);
		add(sb, "PRODUCT", android.os.Build.PRODUCT);
		add(sb, "Radio firmware", android.os.Build.RADIO);
		add(sb, "TAGS", android.os.Build.TAGS);
		add(sb, "TYPE", android.os.Build.TYPE);
		add(sb, "UNKNOWN", android.os.Build.UNKNOWN);
		add(sb, "USER", android.os.Build.USER);
		add(sb, "TIME", String.valueOf(android.os.Build.TIME));

		return sb.toString();
	}

}
