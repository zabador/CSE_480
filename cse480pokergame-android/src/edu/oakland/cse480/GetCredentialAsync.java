package edu.oakland.cse480;

import com.appspot.testmavenagain.myendpoint.Myendpoint;
import com.appspot.testmavenagain.myendpoint.model.MyRequest;
import com.appspot.testmavenagain.myendpoint.model.MyResult;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

public class GetCredentialAsync extends AsyncTask<Void, Void, MyResult> {

    private Myendpoint endpoint;
    private GoogleCloudMessaging gcm;
    private ProgressDialog dialog;
    private String regId;
    private Context mContext;
    private OnUpdateFinish listener;
    private boolean startGame;

    public GetCredentialAsync(Context mContext, OnUpdateFinish listener, Myendpoint endpoint, GoogleCloudMessaging gcm, String regId, boolean startGame) {
        this.endpoint = endpoint;
        this.gcm = gcm;
        this.dialog = new ProgressDialog(mContext);
        this.mContext = mContext;
        this.regId = regId;
        this.listener = listener;
        this.startGame = startGame;
    }


    /**
     *
     * Displays the progress dialog box
     *
     */
    @Override
    protected void onPreExecute()
    {
        dialog.setMessage("Logging in");
        dialog.show();
    } 

    @Override
    protected MyResult doInBackground(Void... params) {

        try {
            if (!startGame) {
                MyRequest r = new MyRequest();
                r.setRegId(regId);
                return endpoint.authenticate(r).execute();
            }
            else {
                MyRequest r = new MyRequest();
                r.setFirstRound(true);
                return endpoint.startGame(r).execute();

            }
        } catch (IOException e) {
            e.printStackTrace();
            MyResult r = new MyResult();
            Log.e("error = ", e.getMessage(), e);
            r.setValue("EXCEPTION");
            return r;
        }
    }


    @Override
    protected void onPostExecute(MyResult r) {
        if(dialog.isShowing()) {
            dialog.dismiss();
        }
        listener.updateGameLobby();
    }
}
