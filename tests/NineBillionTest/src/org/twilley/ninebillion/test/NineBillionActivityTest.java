package org.twilley.ninebillion.test;

import org.twilley.android.ninebillion.NineBillionActivity;
import org.twilley.android.ninebillion.R;

import android.test.ActivityInstrumentationTestCase2;
import android.test.MoreAsserts;
import android.widget.TextView;

public class NineBillionActivityTest extends ActivityInstrumentationTestCase2<NineBillionActivity> {
    private NineBillionActivity mActivity;  // the activity under test
    private TextView mTextView; // the textview I'm testing
    
    public NineBillionActivityTest() {
      super("org.twilley.android.ninebillion", NineBillionActivity.class);
    }
    
    protected void setUp() throws Exception {
    	super.setUp();
    	mActivity = this.getActivity();
        mTextView = (TextView) mActivity.findViewById(R.id.nametext);       
    }
    
    // test update name view
    // set it to something and read it back
    public void testUpdateNameView1() throws Throwable {
    	final byte[] have = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'B', 'A'};
    	final byte[] want = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'B', 'A'};
    	runTestOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    	    	mActivity.updateNameView(have);    			
    		}
    	});
    	byte[] get = ((String) mTextView.getText()).getBytes();
    	MoreAsserts.assertEquals(want, get);
    }

    // test update name view
    // set it to something new and read it back
    public void testUpdateNameView2() throws Throwable {
    	final byte[] have = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'B', 'B'};
    	final byte[] want = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'B', 'B'};
    	runTestOnUiThread(new Runnable() {
    		@Override
    		public void run() {
    	    	mActivity.updateNameView(have);    			
    		}
    	});
    	byte[] get = ((String) mTextView.getText()).getBytes();
    	MoreAsserts.assertEquals(want, get);
    }
    
}