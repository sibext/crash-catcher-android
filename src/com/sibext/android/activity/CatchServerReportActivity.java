package com.sibext.android.activity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.sibext.android.tools.ApiHelper;
import com.sibext.crashcatcher.R;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;
import ch.boye.httpclientandroidlib.entity.mime.content.StringBody;

/**
 * Created by santaev on 3/31/15.
 */
public class CatchServerReportActivity extends NewDesignActivity{

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
    private String fileName;

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
        this.fileName = "filefile.txt";
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
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Can't get init params", e);
        }
    }

    private SendIssueResponse doPost(String url, String json) throws UnsupportedEncodingException {
        HttpClient httpClient = getHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpPost httpPost = getHttpPost(url);
        byte[] bytes = json.getBytes();
        HttpEntity httpEntity = new ByteArrayEntity(bytes);
        httpPost.setEntity(httpEntity);
        HttpResponse response;
        SendIssueResponse sendIssueResponse = new SendIssueResponse();
        try {
            response = httpClient.execute(httpPost, localContext);
            sendIssueResponse.code = response.getStatusLine().getStatusCode();
            sendIssueResponse.json = IOUtils.toString(response.getEntity().getContent());
            Log.d(TAG, "doPost: " + url + " \n" + json);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return sendIssueResponse;
    }

    private ApiHelper.UploadFileResponse doPost(String url, String fileName, InputStream inputStream, long length)
            throws UnsupportedEncodingException {
        HttpClient httpClient = getHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpPost httpPost = getHttpPost(url);
        HttpEntity httpEntity = new ;//new InputStreamEntity(inputStream, length);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        //reqEntity.addPart("user[name]", new StringBody(name, Charset.forName("UTF-8")));
        FileBody fb = new FileBody(new File(fileName), "text/plain");
        reqEntity.addPart("file[file]", fb);
        httpPost.setEntity(reqEntity);
        httpPost.setHeader("file[file]", fileName);
        HttpResponse response;
        ApiHelper.UploadFileResponse sendIssueResponse = new ApiHelper.UploadFileResponse();
        try {
            response = httpClient.execute(httpPost, localContext);
            sendIssueResponse.code = response.getStatusLine().getStatusCode();
            sendIssueResponse.json = IOUtils.toString(response.getEntity().getContent());
            Log.d(TAG, "doPost: " + url + " \n" + sendIssueResponse.json);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return sendIssueResponse;
    }

    private HttpPost getHttpPost(String url){
        HttpPost httpPost = new HttpPost(url);
        httpPost.setParams(getTimeOutParams());
        httpPost.setHeader("Authorization", "Token token=" + api);
        httpPost.setHeader("Content-Type", "application/json");
        return httpPost;
    }

    private HttpClient getHttpClient(){
        HttpClient httpClient = new DefaultHttpClient();
        httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "Android");
        return httpClient;
    }

    private static HttpParams getTimeOutParams(){
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 30000);
        HttpConnectionParams.setSoTimeout(httpParams, 50000);
        return httpParams;
    }

    private SendIssueResponse sentReport(String title, String desc, String build, String appPackage, String category){
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonIssue = new JSONObject();
        try {
            jsonIssue.put(getString(R.string.json_key_title), title);
            jsonIssue.put(getString(R.string.json_key_description), desc);
            jsonIssue.put(getString(R.string.json_key_build), build);
            jsonIssue.put(getString(R.string.json_key_package), appPackage);
            jsonIssue.put(getString(R.string.json_key_category), category);
            jsonObject.put(getString(R.string.json_key_issue), jsonIssue);
            SendIssueResponse sendIssueResponse = doPost(url + ISSUE_URL, jsonObject.toString());
            Log.d(TAG,"Do Post " + sendIssueResponse.json);
            if (200 == sendIssueResponse.code) {
                JSONObject jsonResponse = new JSONObject(sendIssueResponse.json);
                JSONObject issueResponse = jsonResponse.getJSONObject(getString(R.string.json_key_issue));
                sendIssueResponse.id = issueResponse.getString(getString(R.string.json_key_id));
                sendIssueResponse.trackerNumber = issueResponse.getString(getString(R.string.json_key_track_number));
            }
            return sendIssueResponse;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ApiHelper.UploadFileResponse uploadFile(String path, String fileName){
        Log.d(TAG,"Uploading file " + path + " " + url + String.format(UPLOAD_FILE_URL, id));
        try {
            return ApiHelper.doPost(path, fileName, url + String.format(UPLOAD_FILE_URL, id), api);
        } catch (Exception e) {
            return null;
        }
    }

    class ReportAsyncTask extends AsyncTask<Void, Void, SendIssueResponse>{

        @Override
        protected SendIssueResponse doInBackground(Void... params) {
            return sentReport(title, desc, build, appPackage, category);
        }

        @Override
        protected void onPostExecute(SendIssueResponse res) {
            super.onPostExecute(res);
            if (null == res){
                onReportUnSent();
            } else if (200 == res.code){
                id = res.id;
                trackerNumber = res.trackerNumber;
                if (null != path){
                    new UploadAsyncTask().execute();
                } else {
                    onReportSent(String.format(getString(R.string.com_sibext_crashcatcher_status), res.trackerNumber));
                }
            } else if (503 == res.code){
                onReportSent(getString(R.string.com_sibext_crashcatcher_status_server_unavailable));
            } else if (400 == res.code){
                onReportSent("Bad request + " + res.json);
            }
        }
    }

    class UploadAsyncTask extends AsyncTask<Void, Void, ApiHelper.UploadFileResponse>{

        @Override
        protected ApiHelper.UploadFileResponse doInBackground(Void... params) {
            return uploadFile(path, fileName);
        }

        @Override
        protected void onPostExecute(ApiHelper.UploadFileResponse res) {
            super.onPostExecute(res);
            if (null == res){
                onReportUnSent();
            } else if (200 == res.code){
                onReportSent(String.format(getString(R.string.com_sibext_crashcatcher_status), trackerNumber));
            } else if (503 == res.code){
                onReportSent(getString(R.string.com_sibext_crashcatcher_status_server_unavailable));
            } else if (400 == res.code){
                onReportSent("Bad request + " + res.json);
            }
        }
    }

    class SendIssueResponse{
        int code;
        String id;
        String trackerNumber;
        String json;
    }
}
