package com.sibext.android.tools;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by santaev on 3/31/15.
 */
public class ApiHelper {

    public static class UploadFileResponse{
        public int code;
        public String json;
    }

    public static UploadFileResponse doPost(String filePath, String fileName, String urlString, String api) throws Exception {
        String response;
        HttpURLConnection conn = null;
        try {
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "---------------------------14737809831466499882746641449";

            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("uploaded_file", fileName);
            conn.setRequestProperty("Authorization", "Token token=" + api);
            //conn.setRequestProperty("Content-Disposition", "form-data; name=\"userfile\"; filename=\""+ fileName + "\"");

            OutputStream pos = conn.getOutputStream();

            DataOutputStream dos = new DataOutputStream(pos);

            dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"userfile\"; filename=\""+ fileName + "\"" + lineEnd);
            dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
            //file[file]
            dos.writeBytes(lineEnd);
            dos.writeBytes("file[file]: ");
            File file = new File(filePath);
            Log.e("FILE", "" + file.length());
            FileInputStream fileInputStream = new FileInputStream(file);
            int maxBufferSize = 1*1024*1024;
            int bytesAvailable = fileInputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            byte[] buffer = new byte[bufferSize];
            int bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            while (bytesRead > 0){
                Log.e("READED", "" + bytesRead);
                dos.write(buffer, 0, bytesRead);
                //bytesAvailable = fileInputStream.available();
                //bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }
            fileInputStream.close();

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            dos.flush();
            dos.close();
            uploadFileResponse.code = conn.getResponseCode();
            Log.e("CODE", "" + uploadFileResponse.code);
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] bytes = new byte[1024];
            while((bytesRead = is.read(bytes)) != -1) {
                baos.write(bytes, 0, bytesRead);
            }
            byte[] bytesReceived = baos.toByteArray();
            baos.close();
            is.close();

            response = new String(bytesReceived);
            uploadFileResponse.json = response;
            return uploadFileResponse;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return null;
    }
}
