package org.morphone.mmonitor;

import org.morphone.mmonitor.misc.FileHandler;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class MonitorService extends Service {

	private static final String TAG = "org.morphone.mmonitor";
	
	private static final int SLEEP_SECONDS = 10;
	private PowerManager powerManager = null;
	private PowerManager.WakeLock wakelock = null;
	private Handler timeoutHandler = null;
	
	private PlugIntentReceiver plugIntentReceiver;
	private BatteryIntentReceiver batteryIntentReceiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate(){
		Log.i(TAG, "MonitorService created!");

		// Wait for a plug in event
		plugIntentReceiver = new PlugIntentReceiver(getApplicationContext());
		
		IntentFilter plugIntent = new IntentFilter();
		plugIntent.addAction(Intent.ACTION_POWER_CONNECTED);
		registerReceiver(plugIntentReceiver, plugIntent);
		
		// Wait for a battery in event
		batteryIntentReceiver = new BatteryIntentReceiver(getApplicationContext());
		
		IntentFilter batteryIntent = new IntentFilter();
		batteryIntent.addAction(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryIntentReceiver, batteryIntent);
		
		// Used for wake locks
		// powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		// Set alarm for the first time
		// timeoutHandler = new Handler();
		// timeoutHandler.removeCallbacks(logTask);
		// timeoutHandler.postDelayed(logTask, 0); 
	}

	 @Override
     public void onDestroy(){
		 unregisterReceiver(plugIntentReceiver);
		 unregisterReceiver(batteryIntentReceiver);
		 Log.i(TAG, "MonitorService destroyed!");
     }
	 

	// NOT USED IN THIS IMPLEMENTATION
	private Runnable logTask = new Runnable() {
		public void run() {
			try {
				// Wake locking this operation
				wakelock = powerManager.newWakeLock(
							PowerManager.PARTIAL_WAKE_LOCK, "Logging wakelock");
				wakelock.acquire();
				
				// TODO: get here battery level
				log(-1, null);
		
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				wakelock.release();
		    	timeoutHandler.postDelayed(this, SLEEP_SECONDS*1000);
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
		}
	};

}
