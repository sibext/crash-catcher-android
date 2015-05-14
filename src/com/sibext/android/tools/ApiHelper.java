package com.sibext.android.tools;

import android.graphics.Bitmap;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import ch.boye.httpclientandroidlib.entity.mime.HttpMultipartMode;
import ch.boye.httpclientandroidlib.entity.mime.MultipartEntity;
import ch.boye.httpclientandroidlib.entity.mime.content.FileBody;

/**
 * Created by santaev on 3/31/15.
 */
public class ApiHelper {

    private static final String TAG = "[CCL] ApiHelper";

    public static final int CLIENT_INTERNET_ERROR =  -1;
    public static final int SERVER_STATUS_CODE_OK =  200;
    public static final int SERVER_STATUS_BAD_REQUEST =  400;
    public static final int SERVER_STATUS_UNAVAILABLE =  503;

    public static class UploadFileResponse{
        public int code;
        public String json;
    }

    public static class SendIssueResponse{
        public int code;
        public String id;
        public String trackerNumber;
        public String json;
        public boolean isDuplicated;

    }

    public static ApiHelper.UploadFileResponse doFilePost(String url, String fileName, String api)
            throws UnsupportedEncodingException {
        ch.boye.httpclientandroidlib.client.HttpClient httpClient = new ch.boye.httpclientandroidlib.impl.client.DefaultHttpClient();
        ch.boye.httpclientandroidlib.client.methods.HttpPost httpPost = new ch.boye.httpclientandroidlib.client.methods.HttpPost(url);
        httpPost.setHeader("Authorization", "Token token=" + api);
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        FileBody fb = new FileBody(new File(fileName), "text/plain");
        reqEntity.addPart("file[file]", fb);
        httpPost.setEntity(reqEntity);
        ch.boye.httpclientandroidlib.HttpResponse response;
        ApiHelper.UploadFileResponse sendIssueResponse = new ApiHelper.UploadFileResponse();
        try {
            response = httpClient.execute(httpPost);
            Log.d(TAG, "doPost status code : " + response.getStatusLine().getStatusCode());
            sendIssueResponse.code = response.getStatusLine().getStatusCode();
            sendIssueResponse.json = IOUtils.toString(response.getEntity().getContent());
            Log.d(TAG, "doPost url and json: " + url + " \n" + sendIssueResponse.json);
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return sendIssueResponse;
    }

    public static SendIssueResponse doPost(String url, String json, String api) throws UnsupportedEncodingException {
        HttpClient httpClient = getHttpClient();
        HttpContext localContext = new BasicHttpContext();
        HttpPost httpPost = getHttpPost(url, api);
        byte[] bytes = json.getBytes();
        HttpEntity httpEntity = new ByteArrayEntity(bytes);
        httpPost.setEntity(httpEntity);
        HttpResponse response;
        SendIssueResponse sendIssueResponse = new SendIssueResponse();
        try {
            response = httpClient.execute(httpPost, localContext);
            sendIssueResponse.code = response.getStatusLine().getStatusCode();
            sendIssueResponse.json = IOUtils.toString(response.getEntity().getContent());
            Log.d(TAG, "doPost: " + url + " \n" + json + " " + sendIssueResponse.code);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            sendIssueResponse.code = CLIENT_INTERNET_ERROR;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return sendIssueResponse;
    }



    private static HttpPost getHttpPost(String url, String api){
        HttpPost httpPost = new HttpPost(url);
        httpPost.setParams(getTimeOutParams());
        httpPost.setHeader("Authorization", "Token token=" + api);
        httpPost.setHeader("Content-Type", "application/json");
        return httpPost;
    }

    private static HttpClient getHttpClient(){
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
}
