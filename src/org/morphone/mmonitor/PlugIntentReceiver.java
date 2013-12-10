package org.morphone.mmonitor;

import org.morphone.mmonitor.misc.EmailSender;
import org.morphone.mmonitor.misc.FileHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class PlugIntentReceiver extends BroadcastReceiver {

	private static final String TAG = "org.morphone.mmonitor";

	private EmailSender emailSender = null;
	SharedPreferences.Editor prefsEditor = null;

	public PlugIntentReceiver(Context context){
		emailSender = new EmailSender(context);
		SharedPreferences prefs = context.getSharedPreferences("mmonitor", Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// Test completed: send email
		EmailSendingTestCompletedTask task = new EmailSendingTestCompletedTask();
		task.execute(null, null, null);
		
		// prefsEditor.putBoolean("testRunning", false);
		// prefsEditor.apply();
	}
	
	
    /**
     * This class encapsulates the email sending logic using an AsyncTask.
     * 
     * @author Matteo Ferroni
     */
    private class EmailSendingTestCompletedTask extends AsyncTask<Void, Void, Void>
									implements DialogInterface.OnCancelListener
	{
    	/**
    	 * Executes time consuming stuff.
    	 */
    	protected Void doInBackground(Void... unused)
    	{
    		// Application specific
    		boolean mailSent = emailSender.sendDevicePluggedMail();
    		if(mailSent){
    			// Rename file
    			FileHandler.renameFile();
    			Log.i(TAG, "Mail sent, file renamed.");
    		}
    		return null;
    	}

    	public void onCancel(DialogInterface dialog)
    	{
    		cancel(true);
    	}
	}

}
