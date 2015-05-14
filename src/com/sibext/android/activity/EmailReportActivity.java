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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.sibext.android.tools.CatchActivity;
import com.sibext.crashcatcher.R;

import java.io.File;
import java.util.ArrayList;

public class EmailReportActivity extends CatchActivity {
    private static final String TAG = "[CCL] EmailReportActivity";

    private String recipient;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected boolean onReportReadyForSend(String title, StringBuilder body, String path, boolean isManual, ErrorType errorType) {
        Intent i = new Intent(android.content.Intent.ACTION_SEND_MULTIPLE);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL, new String[] { getRecipient() });
        i.putExtra(Intent.EXTRA_SUBJECT, title);

        ArrayList<Uri> uris = new ArrayList<Uri>();
        ArrayList<String> filePaths = new ArrayList<String>();

        File logCatFile = new File(path);
        if (logCatFile.exists()) {
            filePaths.add(path);
        }

        attachFiles(filePaths, getAttachFileDir());

        for (String files : filePaths) {
            File fileIn = new File(files);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }

        if (uris.size() > 0) {
            Log.e(TAG, uris.size() + " ");
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }

        try {
            StringBuilder extraText = new StringBuilder();
            if (hasNotes()) {
                extraText.append("Note:\n");
                extraText.append(getNotes());
                extraText.append("\n");
            }
            extraText.append(body.toString());
            i.putExtra(Intent.EXTRA_TEXT, extraText.toString());
            startActivity(Intent.createChooser(i, getString(R.string.com_sibext_crashcatcher_select_transfer)));
            onReportSent();
            return true;
        } catch (ActivityNotFoundException ex) {
            onReportUnSent();
            Log.e(TAG, "Can't send report", ex);
        }
        return !isManual;
    }

    protected String getAttachFileDir() {
        return null;
    }

    private void attachFiles(ArrayList<String> attachList, String dir) {
        Log.d(TAG, "attachFiles, dir: " + dir);
        if (dir == null) {
            return;
        }
        File directory = new File(dir);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    Log.d(TAG, "attachFiles, file: " + file.getPath());
                    attachList.add(file.getPath());
                }
            }
        }
    }

    private void init() {
        ApplicationInfo ai = null;
        try {
            ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if(ai != null) {
                recipient = ai.metaData.getString(getString(R.string.metadata_recipient_key));
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Can't get init params", e);
        } 
    }

    protected String getRecipient() {
        return recipient;
    }

}
