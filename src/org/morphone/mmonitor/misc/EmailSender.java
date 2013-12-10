package org.morphone.mmonitor.misc;

import org.morphone.mmonitor.mail.GMailSender;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class EmailSender {

	// Specify here the device code
	private static final String DEVICE_CODE = "MMonitor - Generic device under test";
	
	// Specify here the credentials of the source email
	private static final String emailFrom = "emailFrom@gmail.com";
	private static final String passwordEmailFrom = "emailFromPassword";
	
	// Specify here the destination of the email
	private static final String emailTo = "emailTo@provider.com";
	
	private static final int SEND_MAIL_MAX_ATTEMPTS = 10;
	
	private WakeLock wakelock = null; 
	
	public EmailSender(Context context){
		// Init the wakelock
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Send mail wakelock");
	}
	
	public boolean sendTestByMail(int level){		
		return sendMailWithFile("MMonitor - " + DEVICE_CODE + " FINISHED",
                   		"Battery level is " + level + "%",
                   		emailFrom,
                   		emailTo);
       
	}
	
	public boolean sendTestOnAbortButton(){
		return sendMailWithFile("MMonitor - " + DEVICE_CODE + " ABORTED",
        				"Manually aborted by the user",
        				emailFrom,
        				emailTo);
	}
	
	public boolean sendTestOnManualButton(){
		return sendMailWithFile("MMonitor - " + DEVICE_CODE + " MANUAL",
						"Manually sent by the user",
						emailFrom,
						emailTo);
	}
	
	public boolean sendDeviceReadyMail(){
		return sendMail("MMonitor - " + DEVICE_CODE + " READY",
        		"Device ready for test",
        		emailFrom,
        		emailTo);
	}
	
	public boolean sendDevicePluggedMail(){
		return sendMailWithFile("MMonitor - " + DEVICE_CODE + " PLUGGED",
        		"Device has been plugged right now.\nTest finished!",
        		emailFrom,
        		emailTo);
	}
	
	// Send a mail without the logFile attached
	// NOTE: attempts logic added in order to support network instability/timeout
	private boolean sendMail(String object, String text, String from, String to){
		int currentAttemp = 0;
		boolean emailSent = false;
		wakelock.acquire();
		
		do{
			currentAttemp++;
			try {
	            GMailSender sender = new GMailSender(emailFrom, passwordEmailFrom);
	            sender.sendMail(object, text, from, to);
	            emailSent = true;
	        } catch (Exception e) {   
	            Log.e("SendMail", e.getMessage(), e);   
	        }
		}while(!emailSent && currentAttemp < SEND_MAIL_MAX_ATTEMPTS);
		
		wakelock.release();
		
		return emailSent;
	}
	
	// Send a mail with the logFile attached
	// NOTE: attempts logic added in order to support network instability/timeout
	private boolean sendMailWithFile(String object, String text, String from, String to){
		int currentAttemp = 0;
		boolean emailSent = false;
		wakelock.acquire();
		
		do{
			currentAttemp++;
			try {
	            GMailSender sender = new GMailSender(emailFrom, passwordEmailFrom);
	            sender.sendMailWithFile(object, text, from, to);
	            emailSent = true;
	        } catch (Exception e) {   
	            Log.e("SendMail", e.getMessage(), e);   
	        }
		}while(!emailSent && currentAttemp < SEND_MAIL_MAX_ATTEMPTS);
		
		wakelock.release();
		
		return emailSent;
	}
	
}
