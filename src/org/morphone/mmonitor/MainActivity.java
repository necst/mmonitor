package org.morphone.mmonitor;

import org.morphone.mmonitor.misc.EmailSender;
import org.morphone.mmonitor.misc.FileHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

	private TextView mainTextView = null;
	private Button refreshButton = null;
	private Button sendMailButton = null;
	private Button abortTestButton = null;
	private String fileContent = "File not loaded by the asyncTask";
	
	private EmailSender emailSender = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		// Start service for the first time
		Thread th = new Thread() {
		@Override
			public void run() {
				Intent serviceIntent = new Intent(getApplicationContext(), MonitorService.class);
				startService(serviceIntent);
			}
		};
		th.start();
		
		// Create an EmailSender: it needs the context
		emailSender = new EmailSender(getApplicationContext());
		
		// Layout stuff
		setContentView(R.layout.activity_main);
		
		mainTextView = (TextView) findViewById(R.id.MainTextView);
		refreshButton = (Button) findViewById(R.id.MainButton);
		sendMailButton = (Button) findViewById(R.id.SendMailButton);
		abortTestButton = (Button) findViewById(R.id.AbortTestButton);
		
		refreshButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Async update
				AsyncLoadTask updateUI = new AsyncLoadTask();
				updateUI.execute(null, null, null);
			}
		});
		
		sendMailButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Async send mail
				EmailSendingTask emailSending = new EmailSendingTask();
				emailSending.execute(null, null, null);
			}
		});
		
		abortTestButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Async abort test
				AbortTestTask abortTest = new AbortTestTask();
				abortTest.execute(null, null, null);
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onResume(){
		super.onResume();
		
		// Async update
		AsyncLoadTask updateUI = new AsyncLoadTask();
		updateUI.execute(null, null, null);
	}
	
	private void updateInterface() {
		// Application specific
		mainTextView.setText(fileContent);
	}
	
	
	
    /**
     * This class encapsulates the interface updating logic using an AsyncTask.
     * 
     * @author Matteo Ferroni
     */
    private class AsyncLoadTask extends AsyncTask<Void, Void, Void>
									implements DialogInterface.OnCancelListener
	{
    	private ProgressDialog dialog;

    	/**
    	 * Shows the dialog in order to notify the user that the application is loading.
    	 */
    	protected void onPreExecute()
    	{	
    		dialog = ProgressDialog.show(MainActivity.this, "Please wait", "Loading...", true);
    	}


    	/**
    	 * Executes time consuming stuff.
    	 */
    	protected Void doInBackground(Void... unused)
    	{
    		// Application specific
    		fileContent = FileHandler.readFileContent();
    		return null;
    	}

    	/**
    	 * After the execution, the interface is updated.
    	 */
    	protected void onPostExecute(Void unused)
    	{
    		updateInterface();			// Update the interface
    		try{
    			dialog.dismiss();
    		}catch(IllegalArgumentException e){
    			Log.e("MainActivity", "Exception while dismissing a dialog: " + e.getMessage());
    		}
    	}

    	/**
    	 * If the user aborts the operation, the application context is cleaned and the interface is updated.
    	 */
    	public void onCancel(DialogInterface dialog)
    	{
    		cancel(true);
    		updateInterface();			// Update the interface
    		try{
    			dialog.dismiss();
    		}catch(IllegalArgumentException e){
    			Log.e("MainActivity", "Exception while dismissing a dialog: " + e.getMessage());
    		}
    	}

	}
    
    
    
    /**
     * This class encapsulates the email sending logic using an AsyncTask.
     * 
     * @author Matteo Ferroni
     */
    private class EmailSendingTask extends AsyncTask<Void, Void, Void>
									implements DialogInterface.OnCancelListener
	{
    	private ProgressDialog dialog;

    	/**
    	 * Shows the dialog in order to notify the user that the application is loading.
    	 */
    	protected void onPreExecute()
    	{	
    		dialog = ProgressDialog.show(MainActivity.this, "Please wait", "Email sending...", true);
    	}


    	/**
    	 * Executes time consuming stuff.
    	 */
    	protected Void doInBackground(Void... unused)
    	{
    		// Application specific
    		emailSender.sendTestOnManualButton();
    		return null;
    	}

    	/**
    	 * After the execution, the email is sent.
    	 */
    	protected void onPostExecute(Void unused)
    	{
    		updateInterface();			// Update the interface
    		try{
    			dialog.dismiss();
    		}catch(IllegalArgumentException e){
    			Log.e("MainActivity", "Exception while dismissing a dialog: " + e.getMessage());
    		}
    	}

    	/**
    	 * If the user aborts the operation, the application context is cleaned and the interface is updated.
    	 */
    	public void onCancel(DialogInterface dialog)
    	{
    		cancel(true);
    		updateInterface();			// Update the interface
    		try{
    			dialog.dismiss();
    		}catch(IllegalArgumentException e){
    			Log.e("MainActivity", "Exception while dismissing a dialog: " + e.getMessage());
    		}
    	}

	}
    
    
    
    /**
     * This class encapsulates the abort test logic using an AsyncTask.
     * 
     * @author Matteo Ferroni
     */
    private class AbortTestTask extends AsyncTask<Void, Void, Void>
									implements DialogInterface.OnCancelListener
	{
    	private ProgressDialog dialog;

    	/**
    	 * Shows the dialog in order to notify the user that the application is loading.
    	 */
    	protected void onPreExecute()
    	{	
    		dialog = ProgressDialog.show(MainActivity.this, "Please wait", "Abort test...", true);
    	}


    	/**
    	 * Executes time consuming stuff.
    	 */
    	protected Void doInBackground(Void... unused)
    	{
    		// Application specific
    		emailSender.sendTestOnAbortButton();
    		FileHandler.removeLogFile();
    		return null;
    	}

    	/**
    	 * After the execution, the test is aborted.
    	 */
    	protected void onPostExecute(Void unused)
    	{
    		updateInterface();			// Update the interface
    		try{
    			dialog.dismiss();
    		}catch(IllegalArgumentException e){
    			Log.e("MainActivity", "Exception while dismissing a dialog: " + e.getMessage());
    		}
    	}

    	/**
    	 * If the user aborts the operation, the application context is cleaned and the interface is updated.
    	 */
    	public void onCancel(DialogInterface dialog)
    	{
    		cancel(true);
    		updateInterface();			// Update the interface
    		try{
    			dialog.dismiss();
    		}catch(IllegalArgumentException e){
    			Log.e("MainActivity", "Exception while dismissing a dialog: " + e.getMessage());
    		}
    	}

	}


}
