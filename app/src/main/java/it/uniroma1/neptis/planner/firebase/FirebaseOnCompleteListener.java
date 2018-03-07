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

/**
 * Implementation of the OnCompleteListener interface to ease reuse
 * of Firebase token management
 *
 * @see OnCompleteListener
 *
 * @author  Thomas Colleron
 * @version 1.0
 * @since   07/03/2018
 *
 */
public class FirebaseOnCompleteListener implements OnCompleteListener<GetTokenResult> {

    private JSONAsyncTask task;
    private String[] args;
    private String token;

    /**
     * Main constructor
     * @param task JSONAsyncTask object handling the server call and UI updates
     * @param args the list of needed String parameters, such as URL or data
     */
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

    /**
     * This method concatenates the input list of strings with the Firebase token
     * in order to execute the AsyncTask
     */
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
