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

public class PlaceBetAsync extends AsyncTask<Void, Void, MyResult> {
	private Myendpoint endpoint;
	private GoogleCloudMessaging gcm;
	private boolean getGameState;
	private String accountName;
	private OnUpdateFinish listener;
	private String regid;
	private Context context;
    private ProgressDialog dialog;

	public PlaceBetAsync(OnUpdateFinish listener, Context context, Myendpoint endpoint, GoogleCloudMessaging gcm) {
		this.endpoint = endpoint;
		this.gcm = gcm;
		this.getGameState = getGameState;
		this.accountName = accountName;
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
        dialog.setMessage("Placing Bet");
        dialog.show();
    } 

	@Override
	protected MyResult doInBackground(Void... params) {
		try {
			if (gcm == null) {
				gcm = GoogleCloudMessaging.getInstance(context);
			}
			regid = gcm.register("699958132030");
			Log.d("regid from app = ", regid);

		} catch (IOException ex) {
		}

		try {
			MyRequest r = new MyRequest();
			r.setBet(3);
			return endpoint.placeBet(r).execute();
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
		listener.onPlaceBetFinish();
	}
}
