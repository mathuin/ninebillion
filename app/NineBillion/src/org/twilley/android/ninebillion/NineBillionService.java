package org.twilley.android.ninebillion;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

public class NineBillionService extends Service {
	private static final String TAG = "NineBillionService";
	private static final byte[] goodbye = {'G', 'O', 'O', 'D', ' ', 'B', 'Y', 'E', '!'};
	static final String UPDATENAME = "org.twilley.android.ninebillion.updatename";
	static final String RESETNAME = "org.twilley.android.ninebillion.resetname";
	static final String NAME = "Name";
	private byte[] name = new byte[9];
	private boolean done = false;
	private Handler mHandler;

	public class NineBillionBinder extends Binder {
		public static final int SET = 0;
		public static final int START = 1;
		public static final int STOP = 2;
		public static final String NAME = "Name";
		
		NineBillionService getService() {
			return NineBillionService.this;
		}
		
		@Override
		protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
			Bundle values;
			byte[] newName = new byte[9];
			boolean returnCode = false;
			
			switch(code) {
			case SET:
				values = data.readBundle();
				newName = values.getByteArray(NAME);
				Log.i(TAG, "request to change name from " + new String(name) + " to " + new String(newName));
				mHandler.removeCallbacks(nameIterator);
				name = newName;
				updateName(name);
				returnCode = true;
				break;
			case START:
				Log.i(TAG, "request to start iterator");
				mHandler.post(nameIterator);
				break;
			case STOP:
				Log.i(TAG, "request to stop iterator");
				mHandler.removeCallbacks(nameIterator);
				break;
			}
			return returnCode;
		}
	}
	
	final IBinder mBinder = new NineBillionBinder();
	
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.v(TAG, "entered onCreate");

		// configure handler
		mHandler = new Handler();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.v(TAG, "entered onStart");

		Log.d(TAG, "intent: " + intent + ", startId: " + startId);
	}

	@Override
	public void onDestroy() {
		Log.v(TAG, "entered onDestroy");

		// drop the mHandler stuff
		mHandler.removeCallbacks(nameIterator);
	}
	
	/** 
	 * Actually update name 
	 * @param newName  The new name
	 */
	private void updateName(byte[] newName) {
		Intent intent = new Intent(UPDATENAME);
		intent.putExtra(NAME, newName);
		Log.i(TAG, "sending broadcast" + intent);
		Log.v(TAG, "name is " + new String(newName));
		sendBroadcast(intent);
	}
	
	/** Iterates through names */
	private final Runnable nameIterator = new Runnable() {
		public void run() {
			Log.v(TAG, "entered run");

			// check the current name through
	    	if (!done) {
	    		int index = isValid(name);
	    		if (index == -1) {
	    			updateName(name);
	    			index = 8;
	    		}
	    		try {
					name = next(name, index);
				} catch (LastTrumpException e) {
					done = true;
					updateName(goodbye);
				}	
	    	}

	    	// reset mHandler for next event
			mHandler.removeCallbacks(this);
			mHandler.post(this);
		}
	};
	
	/** This method generates the next name.  
	 * @param oldname  The previous name.
	 * @param index  The index is the byte which needs incrementing. 
	 */
    public byte[] next(byte[] oldname, int index) throws LastTrumpException {
    	Log.v(TAG, "oldname is " + new String(oldname));
    	byte[] newname = {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    	for (int i = 0; i < 9; i++)
    		newname[i] = oldname[i];
    	if (newname[index] == 'Z')
    		if (index == 0)
    			throw new LastTrumpException();
    		else
    			newname = next(oldname, index-1);
    	else {
    		Log.v(TAG, "newname[index] is " + newname[index]);
    		newname[index]++;
    		for (int i = index+1; i < 9; i++)
    			newname[i] = 'A';
    	}
    	return newname;
    }
    
    /** This method checks the name for validity. 
     * @param name  The name to be checked.
     */
    public int isValid(byte[] name) {
    	// Valid names return -1, invalid names return index of offending character
		int index = 0;
		int counter = 0;
		for (index = 1; index < 9; index++)
			if (name[index-1] == name[index]) {
				counter++;
				if (counter == 3) {
					break;
				}
			} else
				counter = 0;
		return (counter != 3) ? -1 : index;
    }
}
