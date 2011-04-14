package org.twilley.ninebillion.test;

import org.twilley.android.ninebillion.LastTrumpException;
import org.twilley.android.ninebillion.NineBillionService;

import android.content.Intent;
import android.os.IBinder;
import android.test.MoreAsserts;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

public class NineBillionServiceTest extends ServiceTestCase<NineBillionService> {

	private NineBillionService mService; // service under test
	
    public NineBillionServiceTest() {
        super(NineBillionService.class);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mService = this.getService();
    }

    /**
     * Test basic startup/shutdown of Service
     */
    @SmallTest
    public void testStartable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), NineBillionService.class);
        startService(startIntent); 
    }

    /**
     * Test binding to service
     */
    @MediumTest
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), NineBillionService.class);
        @SuppressWarnings("unused")
		IBinder service = bindService(startIntent); 
    }

    // All of the tests below this point fail with null pointer exceptions.
    // I have no idea why.
    // When I put the same code in the activity and the activity tests, they succeed.
    
    // increment the last letter
	// ABABABABA -> ABABABABB
    public void testNext1() throws Throwable {
    	final byte[] have = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'B', 'A'};
    	final byte[] want = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'B', 'B'};
    	int index = 8;
    	byte[] get = mService.next(have, index);
    	MoreAsserts.assertEquals(want, get);
    }

    // handle final Z correctly
    // ABABABABZ -> ABABABACA
    public void testNext2() throws Throwable {
    	final byte[] have = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'B', 'Z'};
    	final byte[] want = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'C', 'A'};
    	int index = 8;
    	byte[] get = mService.next(have, index);
    	MoreAsserts.assertEquals(want, get);
    }

    // handle non-final Z correctly
    // ABABABAZZ -> ABABABBAA
    public void testNext3() throws Throwable {
    	byte[] have = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'Z', 'Z'};
    	byte[] want = {'A', 'B', 'A', 'B', 'A', 'B', 'B', 'A', 'A'};
    	int index = 8;
    	byte[] get = mService.next(have, index);
    	MoreAsserts.assertEquals(want, get);
    }

    // handle ultimate Z correctly
    // throw exception
    // ZZZZZZZZZ -> kaboom
    public void testNext4() throws Throwable {
    	byte[] have = {'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z', 'Z'};
    	int index = 8;
    	try {
    		mService.next(have, index);
    		// no fail?!
    		fail("next() should have thrown LastTrumpException");
    	} catch (LastTrumpException expected) {
    		// this is what we wanted
    	}
    }
    
    // test if a name is valid
    // a known good name
    public void testIsValid1() throws Throwable {
        final byte[] have = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'B', 'A'};
    	final int want = -1;
    	int get = mService.isValid(have);
    	assertEquals(want, get);
    }
    
    // a known good name ending with two consecutive letters
    public void testIsValid2() throws Throwable {
    	final byte[] have = {'A', 'B', 'A', 'B', 'A', 'B', 'A', 'B', 'B'};
    	final int want = -1;
    	int get = mService.isValid(have);
    	assertEquals(want, get);
    }
    
    // a known bad name with the last four letters repeating
    public void testIsValid3() throws Throwable {
    	final byte[] have = {'A', 'B', 'A', 'B', 'A', 'B', 'B', 'B', 'B'};
    	final int want = 8;
    	int get = mService.isValid(have);
    	assertEquals(want, get);
    }

    // a known bad name with all letters repeating
    public void testIsValid4() throws Throwable {
    	final byte[] have = {'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A', 'A'};
    	final int want = 3;
    	int get = mService.isValid(have);
    	assertEquals(want, get);
    }
}
