package com.sibext.android.activity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sibext.android.manager.CrashCatcherManager;
import com.sibext.android.tools.CrashCatcherError;
import com.sibext.android.tools.KeyboardHelper;
import com.sibext.android.tools.ReportHelper;
import com.sibext.crashcatcher.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 * Created by santaev on 3/10/15.
 */
public abstract class NewDesignActivity extends Activity{

    public static enum ErrorType{

        CRASH(R.string.error_type_crash, 0),
        LOGIC_BUG(R.string.error_type_logic_bug, 0),
        UI_BUG(R.string.error_type_ui_bug, 0),
        FEEDBACK(R.string.error_type_feedback, 0);

        private int nameId;
        private int code;

        ErrorType(int name, int code){
            this.nameId = name;
            this.code = code;
        }

        public int getName(){
            return nameId;
        }

        public static ErrorType fromCode(int code){
            for (int i = 0; i < ErrorType.values().length; i++) {
                if (ErrorType.values()[i].code == code){
                    return ErrorType.values()[i];
                }
            }
            throw new RuntimeException("Unknown Error Type code " + code);
        }
    }

    private ImageView sliderArrow;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private Spinner spinner;
    private TextView privacyText;
    private View logo;
    private View sliderContent;
    private View header;

    private ArrayAdapter spinnerAdapter;

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

    private View yes;
    private TextView yesText;
    private View no;
    private EditText note;
    protected String currentReportId;

    private boolean isManual;

    private boolean isOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: start");
        setContentView(R.layout.new_activity);

        TextView titleText = (TextView) findViewById(R.id.titleText);
        sliderArrow = (ImageView) findViewById(R.id.slider_circle_arrow);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        spinner = (Spinner) findViewById(R.id.spinner);
        privacyText = (TextView) findViewById(R.id.privacy);
        logo = findViewById(R.id.logo);
        sliderContent = findViewById(R.id.slider_content);
        header = findViewById(R.id.header);

        slidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) {
            }

            @Override
            public void onPanelCollapsed(View view) {
                sliderArrow.setImageResource(R.drawable.circle_arrow_up);
            }

            @Override
            public void onPanelExpanded(View view) {
                sliderArrow.setImageResource(R.drawable.circle_arrow);
            }

            @Override
            public void onPanelAnchored(View view) {
            }

            @Override
            public void onPanelHidden(View view) {
            }
        });

        ErrorType[] values = ErrorType.values();
        String[] items = new String[values.length];
        for (int i = 0; i < values.length; i++){
            items[i] = getString(values[i].getName());
        }
        spinnerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, items);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        yes = findViewById(R.id.button_yes);
        yesText = (TextView) findViewById(R.id.button_yes_text);
        no = findViewById(R.id.button_no);
        note = (EditText)findViewById(R.id.editTextComment);

        isManual = getIntent().getBooleanExtra(CrashCatcherManager.MANUAL_FLAG_KEY, false);

        titleText.setText(isManual ? R.string.manual_report_message : R.string.crash_message);
        note.setHint(isManual ? R.string.com_sibext_crashcatcher_manual_message_hint
                : R.string.com_sibext_crashcatcher_message_hint);

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardHelper.hide(v.getContext());
                finish();
            }
        });
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardHelper.hide(v.getContext());
                yes.setClickable(false);
                no.setClickable(false);
                note.setEnabled(false);
                spinner.setEnabled(false);
                new CrashSendTask(isManual).execute();
            }
        });
        Log.d(TAG, "onCreate: finish");
        setListenerToRootView();

        privacyText.setPaintFlags(privacyText.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy:");
        no.setOnClickListener(null);
        yes.setOnClickListener(null);
    }

    abstract protected boolean onReportReadyForSend(String title, StringBuilder body, String resultPath, boolean isManual, ErrorType errorType);

    protected void onReportSent() {
        onReportSent(getString(R.string.com_sibext_crashcatcher_status, currentReportId));
    }

    protected void onReportSent(final String status) {
        note.post(new Runnable() {
            @Override
            public void run() {
                note.setVisibility(View.INVISIBLE);
                no.setVisibility(View.INVISIBLE);
                yesText.setText(R.string.com_sibext_crashcatcher_exit);
                yes.setOnClickListener(new View.OnClickListener() {
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
        note.post(new Runnable() {
            @Override
            public void run() {
                yes.setClickable(true);
                no.setClickable(true);
                note.setEnabled(true);
                Toast.makeText(NewDesignActivity.this, reason, Toast.LENGTH_LONG).show();
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
            ErrorType[] values = ErrorType.values();
            if ( onReportReadyForSend(title, body, getPathLog(), isManuallyMode, values[spinner.getSelectedItemPosition()]) ) {
                finish();
            }
        }
    }

    public void setListenerToRootView(){
        final View activityRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100 ) { // 99% of the time the height diff will be due to a keyboard.
                    //sliderContent.setVisibility(View.GONE);
                    if(!isOpened){
                        //Do two things, make the view top visible and the editText smaller
                        logo.setVisibility(View.GONE);
                        header.setVisibility(View.GONE);
                        privacyText.setVisibility(View.GONE);
                        slidingUpPanelLayout.setPanelHeight(0);
                    }
                    isOpened = true;
                }else if(isOpened){
                    isOpened = false;
                    logo.setVisibility(View.VISIBLE);
                    header.setVisibility(View.VISIBLE);
                    slidingUpPanelLayout.setPanelHeight((int) getResources().getDimension(R.dimen.slider_header_closed_height));
                    privacyText.setVisibility(View.VISIBLE);
                    //sliderContent.setVisibility(View.VISIBLE);
                }
            }
        });
    }

}
