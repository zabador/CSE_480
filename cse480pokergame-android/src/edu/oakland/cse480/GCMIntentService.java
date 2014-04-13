/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.oakland.cse480;

import java.util.StringTokenizer;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GCMIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GCMIntentService() {
        super("GCMIntentService");
    }
    public static final String TAG = "poker";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        sendCustNotification((String)extras.get("message"));
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, Gameplay.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }
    
    public void sendCustNotification(String incomingMsg) {
        Log.i("incomingMsg = ", ""+incomingMsg);
        int msgCode;
        try {
            msgCode = Character.getNumericValue(incomingMsg.charAt(0));
        }catch(Exception e) {
            msgCode = 0;
        }
        String msg;
    	//String[] separated = incomingMsg.split("|");
    	//separated[0] = separated[0]; //discard
    	//separated[1] = separated[1] + ""; 
    	//separated[2] = separated[2] + ""; //Additional message with "" to negate a null
    	
        boolean showNotification = true;

        Intent intent;
    	
    	switch (msgCode){
    	case 1:
            msg = "A new player joined the game";
            showNotification = false;
            intent = new Intent("UpdateGameLobby");
            intent.putExtra("GAMESTARTED", false);
            this.sendBroadcast(intent);
    		break;
    	case 2:
    		msg = "The game has started";
            intent = new Intent("UpdateGameLobby");
            intent.putExtra("GAMESTARTED", true);
            this.sendBroadcast(intent);
    		break;
    	case 3:
    		msg = "It is your turn to bet";
            intent = new Intent("UpdateGamePlay");
            intent.putExtra("WINNER", "No Previous winner");
            this.sendBroadcast(intent);
    		//Stuff
    		break;
    	case 4:
    		msg = "Flop goes";
    		break;
    	case 5:
    		msg = "A card has been dealt";
    		//Stuff
    		break;
    	case 6:
    		msg = "The river card, has been dealt";
    		//Stuff
    		break;
    	case 7:
    		msg = "Hand is over. Winner was " + incomingMsg.substring(1);
            intent = new Intent("UpdateGamePlay");
            intent.putExtra("WINNER", "Winner of last hand: "+incomingMsg.substring(1));
            this.sendBroadcast(intent);
    		//Stuff
    		break;
    	default:
    		msg = "Switch case isn't working";
            showNotification = false;
    		//Some default stuff
    		break;
    	}
    	
        if (showNotification) {
            mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, Gameplay.class), 0);

            NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Poker Notification")
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(msg))
                .setContentText(msg);

            mBuilder.setContentIntent(contentIntent);
            Notification notification = mBuilder.build();
            notification.defaults |= Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE;
            notification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;

            mNotificationManager.notify(NOTIFICATION_ID, notification);

        }
    }
}
