/*
 * Copyright (c) 2018. Thomas Collerton <tho.collerton@gmail.com>
 * This file is part of the Neptis project
 */

package it.uniroma1.neptis.planner.firebase;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;

import it.uniroma1.neptis.planner.asynctasks.JSONAsyncTask;

public class FirebaseOnCompleteListener implements OnCompleteListener<GetTokenResult> {

    private JSONAsyncTask task;
    private String[] args;
    private String token;

    public FirebaseOnCompleteListener(JSONAsyncTask task, String... args) {
        this.task = task;
        this.args = args;
    }

    @Override
    public void onComplete(@NonNull Task<GetTokenResult> task) {
        if(task.isSuccessful()) {
            token = task.getResult().getToken();
            appendToken();
            executeAsyncTask();
        } else {

        }
    }

    private void appendToken() {
        String[] out = new String[args.length + 1];
        System.arraycopy(args, 0, out, 0, args.length);
        out[out.length - 1] = token;
        args = out;
    }

    private void executeAsyncTask() {
        this.task.execute(args);
    }
}
