package com.sibext.android.tools;

import android.app.Activity;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.sibext.android.manager.CrashCatcherManager;
import com.sibext.crashcatcher.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;

/**
 * Created by santaev on 3/10/15.
 */
public abstract class CatchActivity extends Activity{

    public static enum ErrorType{

        CRASH(R.string.error_type_crash, 0, R.string.json_error_type_value_crash),
        LOGIC_BUG(R.string.error_type_logic_bug, 1, R.string.json_error_type_value_logic),
        UI_BUG(R.string.error_type_ui_bug, 2, R.string.json_error_type_value_ui),
        FEEDBACK(R.string.error_type_feedback, 3, R.string.json_error_type_value_feedback);

        private int nameId;
        private int code;
        private int jsonKeyId;

        ErrorType(int name, int code, int jsonKeyId){
            this.nameId = name;
            this.code = code;
            this.jsonKeyId = jsonKeyId;
        }

        public int getName(){
            return nameId;
        }

        public int getJsonKeyId() {
            return jsonKeyId;
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
    private View noteLayout;
    private View header;
    private View buttons;
    private LinearLayout panel;

    private ArrayAdapter spinnerAdapter;

    private static final String TAG = "[CCL] CrashCatcherActivity";

    private static final String DEFAULT_CRASH_SUBJECT = "Crash report";
    private static final String DEFAULT_SUBJECT = "MANUAL Report";

    private static final String KEY_REPORT_IS_SENT = "KEY_REPORT_SENT";
    private static final String KEY_REPORT_STATUS = "KEY_REPORT_STATUS";

    public static final String TRACE_INFO = "TRACE_INFO";
    public static final String RESULT_EXTRA_TEXT = "RESULT_EXTRA_TEXT";

    private final static String STORAGE_DIRECTORY = Environment.getExternalStorageDirectory()
            .toString();
    private final static String SETTINGS_DIR_PROJECT = STORAGE_DIRECTORY + "/.settings";
    final static String SETTINGS_DIR_LOG = STORAGE_DIRECTORY + "/.logcat";
    private final static String PATH_TO_LOG = SETTINGS_DIR_LOG + "/logcat.txt";
    private final static String PATH_TO_RESULT = SETTINGS_DIR_PROJECT + "/result.jpg";

    private View yes;
    private View no;
    private EditText note;
    private TextView status;
    protected String currentReportId;

    private boolean isManual;

    private boolean isOpened = false;

    private boolean reportIsSended = false;
    private String reportStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: start");
        setContentView(R.layout.new_activity);

        TextView titleText = (TextView) findViewById(R.id.com_sibext_crashcatcher_titleText);
        sliderArrow = (ImageView) findViewById(R.id.slider_circle_arrow);
        slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.com_sibext_crashcatcher_sliding_layout);
        spinner = (Spinner) findViewById(R.id.com_sibext_crashcatcher_spinner);
        privacyText = (TextView) findViewById(R.id.com_sibext_crashcatcher_privacy);
        buttons = findViewById(R.id.com_sibext_crashcatcher_buttons_layout);
        logo = findViewById(R.id.com_sibext_crashcatcher_logo);
        panel = (LinearLayout) findViewById(R.id.com_sibext_crashcatcher_panel);
        sliderContent = findViewById(R.id.com_sibext_crashcatcher_slider_content);
        header = findViewById(R.id.com_sibext_crashcatcher_header);
        noteLayout = findViewById(R.id.com_sibext_crashcatcher_edit_text_layout);

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

        yes = findViewById(R.id.com_sibext_crashcatcher_button_yes);
        no = findViewById(R.id.com_sibext_crashcatcher_button_no);
        note = (EditText)findViewById(R.id.com_sibext_crashcatcher_edit_text_comment);

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

        if (null != savedInstanceState){
            reportIsSended = savedInstanceState.getBoolean(KEY_REPORT_IS_SENT);
            String status = savedInstanceState.getString(KEY_REPORT_STATUS);
            if (reportIsSended){
                showReportInfo(status);
                this.status.setText(status);
            }
        }
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
               showReportInfo(status);
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_REPORT_IS_SENT, reportIsSended);
        outState.putString(KEY_REPORT_STATUS, reportStatus);
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

    private void showReportInfo(final String status){
        header.setVisibility(View.GONE);
        panel.removeAllViewsInLayout();
        View view = View.inflate(CatchActivity.this, R.layout.report_status_layout, panel);
        TextView statusTextView = (TextView) view.findViewById(R.id.com_sibext_crashcatcher_status);
        final View exitButton = findViewById(R.id.com_sibext_crashcatcher_button_close);
        this.status = statusTextView;
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        statusTextView.setText(status);
        buttons.setVisibility(View.INVISIBLE);
        ((LinearLayout.LayoutParams) buttons.getLayoutParams()).weight = 0.6f;
        reportIsSended = true;
        reportStatus = status;
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
            if ( onReportReadyForSend(note.getText().toString(), body, getPathLog(), isManuallyMode, values[spinner.getSelectedItemPosition()]) ) {
                finish();
            }
        }
    }

    private void onKeyboardOpened(){
        logo.setVisibility(View.GONE);
        header.setVisibility(View.GONE);
        LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams) panel.getLayoutParams());
        lp.weight = 1;
        lp.height = 0;
        panel.setLayoutParams(lp);
        lp = ((LinearLayout.LayoutParams) noteLayout.getLayoutParams());
        lp.weight = 1;
        lp.height = 0;
        noteLayout.setLayoutParams(lp);
    }

    private void onKeyboardClosed(){
        logo.setVisibility(View.VISIBLE);
        header.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams lp = ((LinearLayout.LayoutParams) panel.getLayoutParams());
        lp.weight = 0;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        panel.setLayoutParams(lp);
        lp = ((LinearLayout.LayoutParams) noteLayout.getLayoutParams());
        lp.weight = 0;
        lp.height = (int) getResources().getDimension(R.dimen.comments_height);
        noteLayout.setLayoutParams(lp);
    }

    public void setListenerToRootView(){
        final View activityRootView = getWindow().getDecorView().findViewById(android.R.id.content);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
                if (heightDiff > 100 ) { // 99% of the time the height diff will be due to a keyboard.
                    if(!isOpened){
                        //Do two things, make the view top visible and the editText smaller
                        onKeyboardOpened();
                    }
                    isOpened = true;
                }else if(isOpened){
                    onKeyboardClosed();
                    isOpened = false;

                }
            }
        });
    }

}
