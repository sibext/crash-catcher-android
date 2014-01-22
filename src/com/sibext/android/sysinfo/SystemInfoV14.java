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

@TargetApi(14)
public class SystemInfoV14 extends SystemInfoV9 {
    @Override
	public String build() {
		StringBuilder sb = new StringBuilder("--SYSTEM V14--").append(DELIMITER);
		sb.append(getInfoFromBuildClass());
		add(sb, "Radio firmware", android.os.Build.getRadioVersion());
		return sb.toString();
	}

}
