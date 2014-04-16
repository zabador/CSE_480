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
	private OnUpdateFinish listener;
	private String regid;
	private Context context;
    private ProgressDialog dialog;
    private int bet;

	public PlaceBetAsync(OnUpdateFinish listener, Context context, Myendpoint endpoint, GoogleCloudMessaging gcm, int bet) {
		this.endpoint = endpoint;
		this.gcm = gcm;
		this.listener = listener;
		this.context = context;
        this.dialog = new ProgressDialog(context);
        this.bet = bet;
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
			MyRequest r = new MyRequest();
			r.setBet(bet);
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
