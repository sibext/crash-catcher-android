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

import java.util.Random;

public class ReportHelper {
    private static final double SIZE_REPORT_ID = 6;

    public static String generateReportID() {
        int n = new Random().nextInt((int) Math.pow(10.0, SIZE_REPORT_ID));
        String key = String.valueOf(n);
        StringBuilder res = new StringBuilder("#");
        for (int i = 0; i < (SIZE_REPORT_ID - key.length()); i++) {
            res.append("0");
        }
        res.append(key);
        return res.toString();
    }
}
