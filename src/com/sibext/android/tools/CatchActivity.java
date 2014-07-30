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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.sibext.android.manager.CrashCatcherManager;
import com.sibext.crashcatcher.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 * The Simple crash catcher activity for automatic send email report.
 * <p/>
 * <p>
 * </p>
 *
 * @author Nikolay Moskvin <moskvin@sibext.com>
 * @author Mike Osipov <osipov@sibext.com>
 */
public abstract class CatchActivity extends Activity {

    private static final String TAG = "[CCL] CrashCatcherActivity";

    private static final String DEFAULT_CRASH_SUBJECT = "Crash report";
    private static final String DEFAULT_SUBJECT = "MANUAL Report";

    public static final String TRACE_INFO = "TRACE_INFO";
    public static final String RESULT_EXTRA_TEXT = "RESULT_EXTRA_TEXT";

    private final static String STORAGE_DIRECTORY = Environment.getExternalStorageDirectory()
                                                               .toString();
    private final static String SETTINGS_DIR_PROJECT = STORAGE_DIRECTORY + "/.settings";
    final static String SETTINGS_DIR_LOG = STORAGE_DIRECTORY + "/.logcat";
    private final static String PATH_TO_LOG = SETTINGS_DIR_LOG + "/logcat.txt";
    private final static String PATH_TO_RESULT = SETTINGS_DIR_PROJECT + "/result.jpg";

    private ProgressBar progressBar;
    private TextView statusText;
    private Button yes;
    private Button no;
    private EditText note;
    protected String currentReportId;

    private boolean isManual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: start");
        setTitle(getString(R.string.crash_catcher));
        setContentView(R.layout.com_sibext_crashcatcher_activity_crash_with_form);
        progressBar = (ProgressBar)findViewById(R.id.com_sibext_crashcatcher_crash_progress);
        statusText = (TextView)findViewById(R.id.com_sibext_crashcatcher_crash_status);
        final TextView titleText = (TextView)findViewById(R.id.com_sibext_crashcatcher_crash_error);

        yes = (Button)findViewById(R.id.com_sibext_crashcatcher_yes);
        no = (Button)findViewById(R.id.com_sibext_crashcatcher_no);
        note = (EditText)findViewById(R.id.com_sibext_crashcatcher_note);

        isManual = getIntent().getBooleanExtra(CrashCatcherManager.MANUAL_FLAG_KEY, false);

        titleText.setText(isManual ? R.string.manual_report_message : R.string.crash_message);
        note.setHint(isManual ? R.string.com_sibext_crashcatcher_manual_message_hint
                             : R.string.com_sibext_crashcatcher_message_hint);

        no.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardHelper.hide(v.getContext());
                finish();
            }
        });
        yes.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardHelper.hide(v.getContext());
                yes.setClickable(false);
                no.setClickable(false);
                note.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                new CrashSendTask(isManual).execute();
            }
        });
        Log.d(TAG, "onCreate: finish");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy:");
        no.setOnClickListener(null);
        yes.setOnClickListener(null);
    }

    abstract protected boolean onReportReadyForSend(String title, StringBuilder body, String resultPath, boolean isManual);

    protected void onReportSent() {
        onReportSent(getString(R.string.com_sibext_crashcatcher_status, currentReportId));
    }

    protected void onReportSent(final String status) {
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                statusText.setVisibility(View.VISIBLE);
                note.setVisibility(View.INVISIBLE);
                statusText.setText(status);
                no.setVisibility(View.INVISIBLE);
                yes.setText(R.string.com_sibext_crashcatcher_exit);
                yes.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
                yes.setClickable(true);
            }
        });
    }

    protected void onReportUnSent() {
        onReportUnSent(getString(R.string.com_sibext_crashcatcher_error));
    }

    protected void onReportUnSent(final String reason) {
        progressBar.post(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.INVISIBLE);
                yes.setClickable(true);
                no.setClickable(true);
                note.setEnabled(true);
                Toast.makeText(CatchActivity.this, reason, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected String getNotes() {
        return note.getText().toString();
    }

    protected boolean hasNotes() {
        return getNotes().trim().length() != 0;
    }

    private String getPathResult() {
        return PATH_TO_RESULT;
    }

    private String getSubject() {
        return DEFAULT_SUBJECT;
    }

    private String getCrashSubject() {
        return DEFAULT_CRASH_SUBJECT;
    }

    private String getPathLog() {
        return PATH_TO_LOG;
    }

    private String getPathDirLog() {
        return SETTINGS_DIR_LOG;
    }

    private void captureLog() {
        final StringBuilder log = ReportHelper.getLog();
        saveLogToFile(log);
    }

    private void saveLogToFile(StringBuilder builder) {
        File outputFile = new File(getPathLog());
        if ( outputFile.exists() ) {
            outputFile.delete();
        }
        Writer writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            writer.write(builder.toString());
        } catch (Exception e) {
            Log.e(TAG, "saveLogToFile failed", e);
            throw new CrashCatcherError("Error writing file on device");
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }

    private String getFinalSubject(boolean isManuallyMode) {
        currentReportId = ReportHelper.generateReportID();
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            return "[" + getPackageName() + " v" + versionName + "] " + ( isManuallyMode ? getSubject() : getCrashSubject() ) + " " + currentReportId;
        } catch (Exception e) {
            return "[" + getPackageName() + " NO VERSION] " + getSubject() + " " + currentReportId;
        }
    }

    private String getExtraText() {
        return getIntent().getStringExtra(RESULT_EXTRA_TEXT);
    }

    private boolean hasExtraText() {
        return getIntent().getStringExtra(RESULT_EXTRA_TEXT) != null;
    }

    private class CrashSendTask extends AsyncTask<Void, Void, Boolean> {
        private final boolean isManuallyMode;
        private StringBuilder body = new StringBuilder("");

        public CrashSendTask(boolean isManuallyMode) {
            this.isManuallyMode = isManuallyMode;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                Log.i(TAG, "Capture log...");
                captureLog();
            } catch (CrashCatcherError e) {
                Log.e(TAG, "ERROR when capture log!");
                body.append(e.getMessage()).append("\n");
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if ( isCancelled() ) {
                return;
            }
            if ( hasExtraText() ) {
                body.append(getExtraText());
            }
            body.append("\n");
            if ( !isManuallyMode ) {
                body.append(" Error: ").append(getIntent().getStringExtra(TRACE_INFO));
            } else {
                body.append("Note: Manually sending");
            }

            final String title = getFinalSubject(isManuallyMode);
            if ( onReportReadyForSend(title, body, getPathLog(), isManuallyMode) ) {
                finish();
            }
        }
    }

}
