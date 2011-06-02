package cgf.android.sandbox;

import java.net.URL;

import android.app.Activity;
import android.os.Bundle;

public class sandbox extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try
    	{
    		super.onCreate(savedInstanceState);
    		setContentView(R.layout.main);
    		URL url = new URL("ftp://hotline.rimg.net");
    	}
        catch(Exception ex)
        {
        }
    }
}