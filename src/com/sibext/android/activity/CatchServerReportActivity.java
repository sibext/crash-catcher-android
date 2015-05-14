package com.sibext.android.activity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.sibext.android.tools.ApiHelper;
import com.sibext.android.tools.CatchActivity;
import com.sibext.crashcatcher.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by santaev on 3/31/15.
 */
public class CatchServerReportActivity extends CatchActivity {

    private static final String TAG = "[CCL] CatchServerReportActivity";
    private static final String ISSUE_URL =  "/v1/issues.json";
    private static final String UPLOAD_FILE_URL =  "/v1/issues/%s/uploads.json";

    private String url;
    private String api;

    private String title;
    private String desc;
    private String build;
    private String appPackage;
    private String category;
    private String path;
    private String id;
    private String trackerNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    protected boolean onReportReadyForSend(String title, StringBuilder body, String resultPath, boolean isManual, ErrorType errorType) {
        this.title = title;
        this.desc = body.toString();
        try {
            this.build = "" + getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            this.build = "VersionCode not founded";
        }
        this.appPackage = getPackageName();
        this.category = getString(errorType.getJsonKeyId());
        this.path = resultPath;
        new ReportAsyncTask().execute();
        return false;
    }

    private void init(){
        ApplicationInfo ai;
        try {
            ai = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            if(ai != null) {
                url = ai.metaData.getString(getString(R.string.metadata_catch_server_url_key));
                api = ai.metaData.getString(getString(R.string.metadata_catch_server_api_key));
                Log.d(TAG, "url = " + url + "\napi = " + api);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Can't get init params", e);
        }
    }

    private ApiHelper.SendIssueResponse sentReport(String title, String desc, String build, String appPackage, String category){
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonIssue = new JSONObject();
        try {
            jsonIssue.put(getString(R.string.json_key_title), title);
            jsonIssue.put(getString(R.string.json_key_description), desc);
            jsonIssue.put(getString(R.string.json_key_build), build);
            jsonIssue.put(getString(R.string.json_key_package), appPackage);
            jsonIssue.put(getString(R.string.json_key_category), category);
            jsonObject.put(getString(R.string.json_key_issue), jsonIssue);
            ApiHelper.SendIssueResponse sendIssueResponse = ApiHelper.doPost(url + ISSUE_URL, jsonObject.toString(), api);
            Log.d(TAG,"Do Post Json = " + sendIssueResponse.json);
            if (ApiHelper.SERVER_STATUS_CODE_OK == sendIssueResponse.code) {
                try {
                    JSONObject jsonResponse = new JSONObject(sendIssueResponse.json);
                    JSONObject issueResponse = jsonResponse.getJSONObject(getString(R.string.json_key_issue));
                    sendIssueResponse.id = issueResponse.getString(getString(R.string.json_key_id));
                    sendIssueResponse.trackerNumber = issueResponse.getString(getString(R.string.json_key_track_number));
                    sendIssueResponse.isDuplicated = issueResponse.getBoolean(getString(R.string.json_key_duplication));
                } catch (JSONException ex){
                    Log.e(TAG, ex.getMessage());
                }
            }
            return sendIssueResponse;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ApiHelper.UploadFileResponse uploadFile(String path){
        Log.d(TAG,"Uploading file " + path + " " + url + String.format(UPLOAD_FILE_URL, id));
        try {
            return ApiHelper.doFilePost(url + String.format(UPLOAD_FILE_URL, id), path, api);
        } catch (Exception e) {
            return null;
        }
    }

    class ReportAsyncTask extends AsyncTask<Void, Void, ApiHelper.SendIssueResponse>{

        @Override
        protected ApiHelper.SendIssueResponse doInBackground(Void... params) {
            return sentReport(title, desc, build, appPackage, category);
        }

        @Override
        protected void onPostExecute(ApiHelper.SendIssueResponse res) {
            super.onPostExecute(res);
            if (null == res){
                onReportUnSent();
            } else if (ApiHelper.SERVER_STATUS_CODE_OK == res.code){
                id = res.id;
                trackerNumber = res.trackerNumber;
                if (null != path){
                    new UploadAsyncTask().execute();
                } else {
                    onReportSent(String.format(getString(R.string.com_sibext_crashcatcher_status), res.trackerNumber));
                }
            } else if (ApiHelper.SERVER_STATUS_UNAVAILABLE == res.code){
                onReportSent(getString(R.string.com_sibext_crashcatcher_status_server_unavailable));
            } else if (ApiHelper.SERVER_STATUS_BAD_REQUEST == res.code){
                onReportSent("Bad request + " + res.json);
            } else if (ApiHelper.CLIENT_INTERNET_ERROR == res.code){
                onReportSent(getString(R.string.com_sibext_crashcatcher_status_net_error));
            } else {
                onReportSent(getString(R.string.com_sibext_crashcatcher_status_unknown_error));
            }
        }
    }

    class UploadAsyncTask extends AsyncTask<Void, Void, ApiHelper.UploadFileResponse>{

        @Override
        protected ApiHelper.UploadFileResponse doInBackground(Void... params) {
            return uploadFile(path);
        }

        @Override
        protected void onPostExecute(ApiHelper.UploadFileResponse res) {
            super.onPostExecute(res);
            if (null == res){
                onReportUnSent();
            } else if (ApiHelper.SERVER_STATUS_CODE_OK == res.code){
                onReportSent(String.format(getString(R.string.com_sibext_crashcatcher_status), trackerNumber));
            } else if (ApiHelper.SERVER_STATUS_UNAVAILABLE == res.code){
                onReportSent(getString(R.string.com_sibext_crashcatcher_status_server_unavailable));
            } else if (ApiHelper.SERVER_STATUS_BAD_REQUEST == res.code){
                onReportSent("Bad request + " + res.json);
            } else if (ApiHelper.CLIENT_INTERNET_ERROR == res.code){
                onReportSent(getString(R.string.com_sibext_crashcatcher_status_net_error));
            } else {
                onReportSent(getString(R.string.com_sibext_crashcatcher_status_unknown_error));
            }
        }
    }


}
