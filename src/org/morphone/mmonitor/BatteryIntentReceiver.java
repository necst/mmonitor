package org.morphone.mmonitor;

import org.morphone.mmonitor.misc.EmailSender;
import org.morphone.mmonitor.misc.FileHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryIntentReceiver extends BroadcastReceiver {

	private static final String TAG = "org.morphone.mmonitor";
	
	private boolean testRunning = false;
	private boolean emailDeviceReadySent = false;
	private int lastPercentageLogged = -1;
	
	private EmailSender emailSender = null;
	SharedPreferences.Editor prefsEditor = null;

	
	// Use two different threshold, in order to know when it's at the middle of the test
	private static final int THRESHOLD_FULL = 99;
	private static final int THRESHOLD_1 = 75;
	private static final int THRESHOLD_2 = 50;

	
	public BatteryIntentReceiver(Context context){
		// Retrieve values (if any)
		SharedPreferences prefs = context.getSharedPreferences("mmonitor", Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();

		testRunning = prefs.getBoolean("testRunning", false);
		emailDeviceReadySent = prefs.getBoolean("emailDeviceReadySent", false);
		lastPercentageLogged = prefs.getInt("lastPercentageLogged", -1);
		
		prefsEditor = prefs.edit();
		emailSender = new EmailSender(context);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Current battery level, from 0 to EXTRA_SCALE.
		int batteryPercentage = -1;
		int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (rawlevel>=0 && scale>0)
        	batteryPercentage = (rawlevel*100)/scale;

        // Are we charging / charged?
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                             (status == BatteryManager.BATTERY_STATUS_FULL);
        
		if(batteryPercentage < 0){
			Log.i(TAG, "Battery has changed, but something went wrong... ");
			log(batteryPercentage, "ERROR");
			return;
		}else{
			if(isCharging){													// CHARGING - Warn me when ready!	
				
				// Reset the state
				testRunning = false;
				prefsEditor.putBoolean("testRunning", false);
				lastPercentageLogged = -1;
				prefsEditor.putInt("lastPercentageLogged", lastPercentageLogged);
				prefsEditor.apply();
				
				if(batteryPercentage >= THRESHOLD_FULL && !emailDeviceReadySent){		
					// Async execute: emailSender.sendDeviceReadyMail();
					// Otherwise: android.os.NetworkOnMainThreadException in Android API > 9
					EmailSendingDeviceReadyTask deviceReadyTask = new EmailSendingDeviceReadyTask();
					deviceReadyTask.execute(null, null, null);
					
					emailDeviceReadySent = true;
					prefsEditor.putBoolean("emailDeviceReadySent", true);
					prefsEditor.apply();
					
					Log.i(TAG, "Battery full: device ready.");
				}
				return;
			}else{															// NOT CHARGING
				if(!testRunning && batteryPercentage >= THRESHOLD_FULL){	// 100% battery - START TEST
					log(batteryPercentage, "START TEST");
					
					testRunning = true;
					prefsEditor.putBoolean("testRunning", true);
					
					emailDeviceReadySent = false;
					prefsEditor.putBoolean("emailDeviceReadySent", false);
					prefsEditor.apply();
					
					Log.i(TAG, "Disconnected: test started.");
					
					return;
				}
				
				// Test running and percentage not already logged
				if(testRunning && batteryPercentage != lastPercentageLogged){
					if(batteryPercentage > THRESHOLD_1){				// TESTING CONDITION
						log(batteryPercentage, null);
						return;
					}else if(batteryPercentage == THRESHOLD_1){			// FIRST STEP - Warn me with file!
						log(batteryPercentage, "THRESHOLD_1");
						
						// Async execute: emailSender.sendTestByMail(batteryPercentage);
						// Otherwise: android.os.NetworkOnMainThreadException in Android API > 9
						EmailSendingTestTask sendTask = new EmailSendingTestTask(batteryPercentage);
						sendTask.execute(null, null, null);
						
						return;
					}else if(batteryPercentage < THRESHOLD_1 && 
							 batteryPercentage > THRESHOLD_2){			// TESTING CONDITION
						log(batteryPercentage, null);
						return;
					}else if(batteryPercentage == THRESHOLD_2){			// SECOND STEP - Warn me with file!
						log(batteryPercentage, "THRESHOLD_2");
						
						// Async execute: emailSender.sendTestByMail(batteryPercentage);
						// Otherwise: android.os.NetworkOnMainThreadException in Android API > 9
						EmailSendingTestTask sendTask = new EmailSendingTestTask(batteryPercentage);
						sendTask.execute(null, null, null);
						
						testRunning = false;
						prefsEditor.putBoolean("testRunning", false);
						prefsEditor.apply();
						
						Log.i(TAG, "Test completed.");
						
						return;
					}
				}
			}
		}
	}
	
	private void log(int batteryLevel, String action){
		
		long timestamp = System.currentTimeMillis();
		String logString = "";
		
		if(action != null)
			logString = timestamp + "," + batteryLevel + "," + action + "\n";
		else
			logString = timestamp + "," + batteryLevel + "\n";
			
		// Write the string to file		
		FileHandler.writeLogEntry(logString);
		
		lastPercentageLogged = batteryLevel;
		prefsEditor.putInt("lastPercentageLogged", lastPercentageLogged);
		prefsEditor.apply();
	}
	
	
	
	
    /**
     * This class encapsulates the email sending logic using an AsyncTask.
     * 
     * @author Matteo Ferroni
     */
    private class EmailSendingDeviceReadyTask extends AsyncTask<Void, Void, Void>
									implements DialogInterface.OnCancelListener
	{
    	/**
    	 * Executes time consuming stuff.
    	 */
    	protected Void doInBackground(Void... unused)
    	{
    		// Application specific
    		emailSender.sendDeviceReadyMail();
    		return null;
    	}

    	public void onCancel(DialogInterface dialog)
    	{
    		cancel(true);
    	}
	}
    
    
    
    /**
     * This class encapsulates the email sending logic using an AsyncTask.
     * 
     * @author Matteo Ferroni
     */
    private class EmailSendingTestTask extends AsyncTask<Void, Void, Void>
									implements DialogInterface.OnCancelListener
	{
    	private int batteryPercentage = -1;
    	
    	public EmailSendingTestTask(int batteryPercentage){
    		this.batteryPercentage = batteryPercentage;
    	}
    	
    	protected Void doInBackground(Void... unused)
    	{
    		// Application specific
    		emailSender.sendTestByMail(batteryPercentage);
    		return null;
    	}

    	public void onCancel(DialogInterface dialog)
    	{
    		cancel(true);
    	}

	}

}
