package org.twilley.android.ninebillion;

import org.twilley.android.ninebillion.NineBillionService;
import org.twilley.android.ninebillion.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NineBillionActivity extends Activity {
	private static final String TAG = "NineBillionActivity";
	private static final String NAME = "Name";
	private static final String start = "AAAAAAAAA";
	private static final int MENU_INFO = 0;
	private SharedPreferences app_preferences;
	private Button startstopButton;
	private Button resetButton;
	private TextView nameView;
	protected NineBillionService mBoundService;
	private boolean mIsBound;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		// open preferences
		app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
       
		// set up text view
		nameView = (TextView) this.findViewById(R.id.nametext);
		
        // set up buttons
        startstopButton = (Button)this.findViewById(R.id.startstopbutton);
        startstopButton.setText("Start");
        startstopButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		int code;
        		if (startstopButton.getText() == "Start") {
        			startstopButton.setText("Stop");
        			code = NineBillionService.NineBillionBinder.START;
        		} else {
        			startstopButton.setText("Start");
        			code = NineBillionService.NineBillionBinder.STOP;
        		}
    	        try {
    	        	Log.d(TAG, "sending startstop transact request to service");
    				mBoundService.mBinder.transact(code, null, null, 0);
    			} catch (RemoteException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			// will stop service
        	}
        });
        resetButton = (Button)this.findViewById(R.id.resetbutton);
        resetButton.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
    			byte[] name = app_preferences.getString(NAME, start).getBytes();
    	        Bundle nameValues = new Bundle();
    	        nameValues.putByteArray(NineBillionService.NineBillionBinder.NAME, name);
    	        Parcel nameData = Parcel.obtain();
    	        nameData.writeBundle(nameValues);
    	        try {
    	        	Log.d(TAG, "sending name transact request to service");
    				mBoundService.mBinder.transact(NineBillionService.NineBillionBinder.SET, nameData, null, 0);
    			} catch (RemoteException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        	}
        });
    }

    /** Called when the activity is becoming visible to the user. */
    @Override
    protected void onStart() {
    	super.onStart();
    	Log.v(TAG, "entered onStart");
    	
    	// start service
		Log.d(TAG, "binding service");
	    doBindService();
	}
    
    /** Called after your activity has been stopped, prior to it being started again. */
    @Override
    protected void onRestart() {
    	super.onRestart();
    	Log.v(TAG, "entered onRestart");
    }

    /** Called when the activity will start interacting with the user. */
    @Override
    protected void onResume() {
    	super.onResume();
    	Log.v(TAG, "entered onResume");
    	// activity is currently on the top of the stack, with user activity
    	
		// register receiver
		registerReceiver(mIntentReceiver, new IntentFilter(NineBillionService.UPDATENAME), null, null);
    }
    
    /** Called when the system is about to start resuming a previous activity. */
    @Override
    protected void onPause() {
    	super.onPause();
    	Log.v(TAG, "entered onPause");
    	// commit unsaved changes to persistent data
    	// stop animations
    	// MUST BE VERY QUICK
    	
		unregisterReceiver(mIntentReceiver);
    }

    /** Called when the activity is no longer visible to the user. */
    @Override
    protected void onStop() {
    	super.onStop();
    	Log.v(TAG, "entered onStop");
    	
		// stop service
		Log.d(TAG, "stopping service");
	    doUnbindService();
    }

    /** The final call you receive before your activity is destroyed. */
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	Log.v(TAG, "entered onDestroy");

    	// save current name to preferences
		SharedPreferences.Editor editor = app_preferences.edit();
		editor.putString(NAME, nameView.getText().toString());
		editor.commit();		
    }

    /** This method updates the name view. 
     * @param name  The name which should be published in the view. 
     * */
    public void updateNameView(byte[] name) {
        nameView.setText(new String(name));
    }
    
    /** Listens for service messages */
	private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
	    public void onReceive(Context context, Intent intent) {
			Log.v(TAG, "entered onReceive");
			
			String action = intent.getAction();
			Bundle extras = intent.getExtras();
			
			if (action.equals(NineBillionService.UPDATENAME)) {
				Log.i(TAG, "received intent to update name");
				
				byte[] newName = extras.getByteArray(NineBillionService.NAME);
				Log.d(TAG, "name is " + new String(newName));
				
				// update the name view
				updateNameView(newName);
			} else {
				Log.d(TAG, "received unknown intent: " + action);
			}
	    }
	};
	
	/* service connection stuff */
	private ServiceConnection mConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.v(TAG, "entered onServiceConnected");
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
	        mBoundService = ((NineBillionService.NineBillionBinder)service).getService();

	        // now set the name
			byte[] name = app_preferences.getString(NAME, start).getBytes();
	        Bundle nameValues = new Bundle();
	        nameValues.putByteArray(NineBillionService.NineBillionBinder.NAME, name);
	        Parcel nameData = Parcel.obtain();
	        nameData.writeBundle(nameValues);
	        try {
	        	Log.d(TAG, "sending name transact request to service");
				mBoundService.mBinder.transact(NineBillionService.NineBillionBinder.SET, nameData, null, 0);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	    public void onServiceDisconnected(ComponentName className) {
	    	Log.v(TAG, "entered onServiceDisconnected");
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
	        mBoundService = null;
	    }
	};
	
	void doBindService() {
		Log.v(TAG, "entered doBindService");
	    // Establish a connection with the service.  We use an explicit
	    // class name because we want a specific service implementation that
	    // we know will be running in our own process (and thus won't be
	    // supporting component replacement by other applications).
	    bindService(new Intent(NineBillionActivity.this, NineBillionService.class), mConnection, Context.BIND_AUTO_CREATE);
	    mIsBound = true;
	}

	void doUnbindService() {
		Log.v(TAG, "entered doUnbindService");
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        stopService(new Intent(NineBillionActivity.this, NineBillionService.class));
	        mIsBound = false;
	    }
	}   

	/** build options menu during OnCreate */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		Log.v(TAG, "entered onCreateOptionsMenu");
		
		// add info
		menu.add(0, MENU_INFO, 0, R.string.menu_info);
		
		return true;
	}
	
	/** invoked when user selects item from menu */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Log.v(TAG, "entered onOptionsItemSelected");
		
		// display info
		switch (item.getItemId()) {
		case MENU_INFO:
			// display info
			showAbout(this);
			return true;
		default:
			// should never reach here...
			return false;
		}
	}

	/** displays "about..." page */
	private void showAbout(final Activity activity) {
		Log.v(TAG, "entered showAbout");
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		PackageManager pm = getPackageManager();
		String versionName;
		try {
			versionName = pm.getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			Log.w(TAG, "PackageManager threw NameNotFoundException");
			versionName = "";
		}

		builder.setTitle(getString(R.string.about_title) + " " + getString(R.string.app_name) + " " + versionName);
		builder.setMessage(R.string.about_body);
		builder.setPositiveButton(R.string.about_dismiss_button, 
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						;
					}
		});
		builder.create().show();
	}


}