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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sibext.android.sysinfo.SystemInfoBuilder;
import com.sibext.crashcatcher.R;

/**
 * The Simple crash catcher activity for automatic send email report.
 * 
 * <p>
 * </p>
 * 
 * @author Nikolay Moskvin <moskvin@sibext.com>
 * @author Mike Osipov <osipov@sibext.com>
 * 
 */
public abstract class CatchActivity extends Activity {
	private static final String TAG = "CrashCatcherActivity";

	private static final String DEFAULT_CRASH_SUBJECT = "Crash report";
	private static final String DEFAULT_SUBJECT = "Report";

	public static final String TRACE_INFO = "TRACE_INFO";
    public static final String RESULT_EXTRA_TEXT = "RESULT_EXTRA_TEXT";

	private final static String STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().toString();
	private final static String SETTINGS_DIR_PROJECT = STORAGE_DIRECTORY + "/.settings";
	private final static String SETTINGS_DIR_LOG = STORAGE_DIRECTORY
			+ "/.logcat";
	private final static String PATH_TO_LOG = SETTINGS_DIR_LOG + "/logcat.txt";
	private final static String PATH_TO_RESULT = SETTINGS_DIR_PROJECT
			+ "/result.jpg";

	private ProgressBar progressBar;
	private TextView statusText;
	private Button yes;
	private Button no;
	private EditText note;
	protected String currentReportId;

	public class CrashCatcherError extends Error {

		public CrashCatcherError(String detailMessage) {
			super(detailMessage);
		}

		public CrashCatcherError(Throwable throwable) {
			super(throwable);
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
        setTitle(getString(R.string.crash_catcher));
        setContentView(R.layout.com_sibext_crashcatcher_activity_crash_with_form);
        progressBar = (ProgressBar)findViewById(R.id.com_sibext_crashcatcher_crash_progress);
        statusText = (TextView)findViewById(R.id.com_sibext_crashcatcher_crash_status);
        yes = (Button)findViewById(R.id.com_sibext_crashcatcher_yes);
        no = (Button)findViewById(R.id.com_sibext_crashcatcher_no);
        note = (EditText)findViewById(R.id.com_sibext_crashcatcher_note);

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
                new CrashSendTask(false).execute();
            }
        });

		super.onCreate(savedInstanceState);
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        no.setOnClickListener(null);
        yes.setOnClickListener(null);
    }

    abstract protected boolean onReportReadyForSend(String title, StringBuilder body, String resultPath, boolean isMonuallyMode);

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
		Process LogcatProc = null;
		BufferedReader reader = null;
		StringBuilder log = new StringBuilder();

		initFolder();

		try {
			LogcatProc = Runtime.getRuntime().exec(
					new String[] { "logcat", "-d" });

			reader = new BufferedReader(new InputStreamReader(
					LogcatProc.getInputStream()));

			String line;
			long time = System.currentTimeMillis();
			Log.d(TAG, System.currentTimeMillis() + "");
			while ((line = reader.readLine()) != null) {
				log.append(line).append(System.getProperty("line.separator"));
			}
			Log.d(TAG, (System.currentTimeMillis() - time) + "");
			log.append(new SystemInfoBuilder().build());
		} catch (Exception e) {
			throw new CrashCatcherError("Get logcat failed");
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
		saveLogToFile(log);
	}

	private void saveLogToFile(StringBuilder builder) {
		File outputFile = new File(getPathLog());
		if (outputFile.exists()) {
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

	private String getFinalSubject(boolean isMonuallyMode) {
	    currentReportId = ReportHelper.generateReportID();
		try {
			String versionName = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionName;
			return "[" + getPackageName() + " v" + versionName + "] "
					+ (isMonuallyMode ? getSubject() : getCrashSubject()) + " " + currentReportId;
		} catch (Exception e) {
			return "[" + getPackageName() + " NO VERSION] " + getSubject() + " " + currentReportId;
		}
	}

	private void initFolder() {
		File tempDir = new File(getPathDirLog());
		if (!tempDir.exists()) {
			tempDir.mkdirs();
		}
	}

    private String getExtraText() {
        return getIntent().getStringExtra(RESULT_EXTRA_TEXT);
    }

    private boolean hasExtraText() {
        return getIntent().getStringExtra(RESULT_EXTRA_TEXT) != null;
    }

    private class CrashSendTask extends AsyncTask<Void, Void, Boolean> {
        private final boolean isMonuallyMode;
        private StringBuilder body = new StringBuilder("");

        public CrashSendTask(boolean isMonuallyMode) {
            this.isMonuallyMode = isMonuallyMode;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                captureLog();
            } catch (CrashCatcherError e) {
                body.append(e.getMessage()).append("\n");
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (isCancelled()) {
                return;
            }
            if (hasExtraText()) {
                body.append(getExtraText());
            }
            if (!isMonuallyMode) {
                body.append("\n").append(" Error: ").append(getIntent().getStringExtra(TRACE_INFO));
            } else {
                body.append("\n").append("Note: Manually sending");
            }

            if (onReportReadyForSend(getFinalSubject(isMonuallyMode), body, getPathLog(), isMonuallyMode)) {
                finish();
            }

        }
    }

}
