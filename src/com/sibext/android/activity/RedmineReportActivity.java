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

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import com.sibext.android.tools.CatchActivity;
import com.sibext.android.tools.SHA1Helper;
import com.sibext.crashcatcher.R;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManager.INCLUDE;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.User;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class RedmineReportActivity extends CatchActivity {
    private static final String TAG = "[CCL] RedmineReportActivity";
    private String redmineHost = null;
    private String redmineKey = null;
    private String redmineProject = null;
    private String assigneeLogin = null;
    
    private HandlerThread thread;
    private Handler handler;
    private RedmineManager manager;

    private boolean founded;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
       tryGetArgumentsFromManifest();
        if (invalidArguments()) {
            tryGetArgumentsFromResources();
        }
        if (invalidArguments()) {
            throw new RuntimeException("Please setup readmine in your manifest or in your resources!");
        }
        thread = new HandlerThread(TAG);
        thread.start();
        handler = new Handler(thread.getLooper());
        manager = new RedmineManager(redmineHost, redmineKey);
    }

    @Override
    protected boolean onReportReadyForSend(final String title, final StringBuilder body,String resultPath, final boolean isManual) {
        Log.d(TAG, "onReportReadyForSend:");
        final Issue issueToCreate = new Issue();
        final File logFile = new File(resultPath);
        issueToCreate.setSubject(title);
        issueToCreate.setPriorityId(5);
        issueToCreate.setDescription(body.toString() + SHA1Helper.getSHA1(body.toString()));
        if (assigneeLogin != null) {
            User user = new User();
            user.setLogin(assigneeLogin);
            issueToCreate.setAssignee(user);
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    sendIssue(issueToCreate, title, body, logFile, isManual);
                } catch (RedmineException e) {
                    Log.d(TAG, "Can't create new issue", e);
                    onReportUnSent();
                } catch (IOException e) {
                    Log.d(TAG, "Can't add attach to issue", e);
                    onReportUnSent();
                } catch (Exception e) {
                    Log.d(TAG, "Can't send to redmine", e);
                    onReportUnSent();
                }
            }
        });
        return true;
    }

    @Override
    protected void onDestroy() {
        thread.quit();
        super.onDestroy();
    }

    protected boolean isIgnoreDuplicates() {
        return true;
    }

    protected boolean isDuplicate() {
        return founded;
    }

    private boolean invalidArguments() {
        return redmineHost == null || redmineKey == null || redmineProject == null;
    }

    private void tryGetArgumentsFromManifest() {
        try {
            ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(),
                                                                        PackageManager.GET_META_DATA);
            if(ai == null) {
                return;
            }
            redmineHost = ai.metaData.getString(getString(R.string.metadata_redmine_host));
            redmineKey = ai.metaData.getString(getString(R.string.metadata_redmine_key));
            redmineProject = ai.metaData.getString(getString(R.string.metadata_redmine_project));
            assigneeLogin = ai.metaData.getString(getString(R.string.metadata_redmine_assignee_login));
            Log.d(TAG, "tryGetArgumentsFromManifest");
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Can't get init params", e);
        }
    }

    private void tryGetArgumentsFromResources() {
        Log.d(TAG, "tryGetArgumentsFromResources");
        redmineHost = getString(R.string.redmine_host);
        redmineKey = getString(R.string.redmine_access_key);
        redmineProject = getString(R.string.redmine_project);
        assigneeLogin = getString(R.string.redmine_assignee_login);
    }

    private void sendIssue(final Issue issueToCreate, final String title, final StringBuilder body, final File logFile, final boolean isManual) throws RedmineException, IOException {
        Log.d(TAG, "sendIssue:");
        if (isIgnoreDuplicates() && !isManual) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("project_id", redmineProject);
            params.put("author_id", "me");
            List<Issue> foundIssues = manager.getIssues(params);

            founded = false;
            Issue sameIssue = null;
            for (Issue issue : foundIssues) {
                if (issue.getDescription().contains(SHA1Helper.getSHA1(body.toString()))) {
                    founded = true;
                    sameIssue = issue;
                    break;
                }
            }
            if (founded) {
                Log.d(TAG, "sendIssue: is duplicate");
                onReportSent(getString(R.string.com_sibext_crashcatcher_already_exist));
                // Needs reopen if already closed or resolved
                if (sameIssue.getStatusName().equalsIgnoreCase("Closed") || sameIssue.getStatusName().equalsIgnoreCase("Resolved")) {
                    Issue loadedIssueWithJournals = manager.getIssueById(sameIssue.getId(), INCLUDE.journals);
                    loadedIssueWithJournals.setStatusId(4);
                    loadedIssueWithJournals.setStatusName("Reopen");
                    loadedIssueWithJournals.setNotes(title + " has been detected again!\n" + "*Comments:* " + getNotes());
                    manager.update(loadedIssueWithJournals);
                }
                return;
            }
        }
        Issue createdIssue = manager.createIssue(redmineProject, issueToCreate);
        Log.d(TAG, "id of issue = " + createdIssue.getId());
        Log.d(TAG, "log file = " + logFile.getAbsolutePath());
        StringBuilder log = new StringBuilder("h1. LOG FILE");
        FileInputStream inputStream = new FileInputStream(logFile);
        if (hasNotes()) {
            log.append("<pre><code class=\"text\">").append(getNotes()).append("</code></pre>");
        }
        log.append("<pre><code class=\"text\">");
        try {
            log.append(IOUtils.toString(inputStream));
        } finally {
            inputStream.close();
        }
        createdIssue.setNotes(log.toString());
        //TODO: @moskvin Please implement attachment to redmine.
        //final Attachment attach = manager.uploadAttachment("log.txt", "application/octet-stream", logFile);
        //createdIssue.getAttachments().add(attach);
        //Log.d(TAG, "attach size = " + createdIssue.getAttachments().size());
        manager.update(createdIssue);
        currentReportId = "#" + createdIssue.getId();
        onReportSent();
    }
}
