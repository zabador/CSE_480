package edu.oakland.cse480;

import com.appspot.testmavenagain.myendpoint.Myendpoint;
import com.appspot.testmavenagain.myendpoint.model.MyResult;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

public class UpdateAsync extends AsyncTask<Void, Void, MyResult> {
	private Myendpoint endpoint;
	private GoogleCloudMessaging gcm;
	private OnUpdateFinish listener;
	private String regid;
	private Context context;
    private ProgressDialog dialog;

	public UpdateAsync(OnUpdateFinish listener, Context context, Myendpoint endpoint, GoogleCloudMessaging gcm, String accountName) {
		this.endpoint = endpoint;
		this.gcm = gcm;
		this.listener = listener;
		this.context = context;
        this.dialog = new ProgressDialog(context);
	}

   /**
     *
     * Displays the progress dialog box
     *
     */
    @Override
    protected void onPreExecute()
    {
        dialog.setMessage("Syncing...");
        dialog.show();
    } 
	@Override
	protected MyResult doInBackground(Void... params) {

        try {
            return endpoint.getGameState().execute();
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

		listener.onGetGameStateFinish(r);
	}
}
