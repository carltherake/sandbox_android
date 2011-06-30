package cgf.android.sandbox;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;

import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Calendar;
import java.text.SimpleDateFormat;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

public class sandbox extends Activity {
    /** Called when the activity is first created. */
	private static final int CAMERA_PIC_REQUEST = 4975;
	private File TEMP_PHOTO_FILE = new File(Environment.getExternalStorageDirectory(), "temp_photo.jpg");
	private Context mainCont;
	
	public String strTechnician = "";
	public String strLocation = "";
	
	protected boolean _taken = true;
	
	protected static final String PHOTO_TAKEN = "photo_taken";
	
    @Override
     public void onCreate(Bundle savedInstanceState) {
    	try
    	{
    		super.onCreate(savedInstanceState);
    		setContentView(R.layout.main);
    		mainCont = this;

    		Button btnSave = (Button)findViewById(R.id.btnSave);
    		btnSave.setOnClickListener(new OnClickListener()
    		{
    			public void onClick(View view)
    			{
    				try
    				{
	    				EditText techBox = (EditText)findViewById(R.id.etTech);
	    				EditText locBox = (EditText)findViewById(R.id.etLocation);

	    				String techName = techBox.getText().toString();
	    				String locName = locBox.getText().toString();
	    				
//	    				upload(techName, locName, TEMP_PHOTO_FILE);
	    				sendFtp(TEMP_PHOTO_FILE);
    				}
//    				catch (IOException e) {
//    					alertbox("btnSave.onClick", "IOException: " + e.getMessage());
//    					Toast.makeText(mainCont, "btnSave.onClick: IOException", Toast.LENGTH_LONG);
//    				}
    				catch (Exception e) {
    					alertbox("btnSave.onClick:", "Exception");
    					Toast.makeText(mainCont, "btnSave.onClick: Exception", Toast.LENGTH_LONG);
    				}
    			}
    		});

    		Button btnPicture = (Button)findViewById(R.id.btnPicture);
    		btnPicture.setOnClickListener(new OnClickListener()
    		{
    			public void onClick(View view)
    			{
    				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//    				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TEMP_PHOTO_FILE));
    				startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
    			}
    		});
    		
			Toast.makeText(mainCont, "on click, d00d", Toast.LENGTH_SHORT).show();
    	}
        catch(Exception ex)  {
        	finish();
        	Toast.makeText(this, "onCreate: Error occured.", Toast.LENGTH_SHORT).show();
        }
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
//    	if ((requestCode == CAMERA_PIC_REQUEST) && (resultCode == Activity.RESULT_OK)) {
//    		if (data == null) {
//    			Toast.makeText(mainCont, "Photo Taken.  data is NULL", Toast.LENGTH_SHORT);
//    		}
//    	}
    	
//     *********   First Attempt.  Creates image in /mnt/sdcard/temp_photo.jpg   ***************
    	if (requestCode == CAMERA_PIC_REQUEST)
    	{
    		try {
	    		FileOutputStream out = new FileOutputStream(TEMP_PHOTO_FILE);
	    		
	    		Bitmap thumbnail = (Bitmap)data.getExtras().get("data");
	    		ImageView image = (ImageView)findViewById(R.id.photo);
	    		image.setImageBitmap(thumbnail);
	    		thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, out);
	    		alertbox("File Created", TEMP_PHOTO_FILE.getAbsolutePath());
    		}
    		catch(Exception e)
    		{
    			alertbox("onActivityResult:", "Failed to create file.");
    		}
    	}
    }
    
	public void upload(String techName, String locName, File source) throws MalformedURLException, IOException {
//		String DATE_FORMAT_NOW = "yyyymmdd_hhmm";
//		Calendar cal = Calendar.getInstance();
//		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
//		String fileName = sdf.format(cal.getTime()) + "_" + techName + "_" + locName + ".jpg";
		String fileName = "temp_photo.jpg";
		
		StringBuffer sb = new StringBuffer("ftp://");
		sb.append(getString(R.string.username));
		sb.append(':');
		sb.append(getString(R.string.password));
		sb.append('@');
		sb.append(getString(R.string.server));
		sb.append('/');
		sb.append(fileName);
		sb.append(";type=i");

		alertbox("what", "thefuck? "  + sb.toString());
		
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		
		try
		{
			URL url = new URL(sb.toString());
			URLConnection urlc = url.openConnection();
			urlc.setDoInput(true);
			
			bis = new BufferedInputStream(new FileInputStream(source));
			bos = new BufferedOutputStream(urlc.getOutputStream());
			
			int i;
			while ((i = bis.read()) != -1)
			{
				bos.write(i);
			}
		}
		finally
		{
			if (bis!=null)
				try { bis.close(); 	alertbox("Info: ", "Input Stream Closed."); }
				catch (IOException ioe) { ioe.printStackTrace(); }
			if (bos != null)
				try { bos.close(); alertbox("Info: ", "Output Stream Closed."); }
				catch (IOException ioe) { ioe.printStackTrace(); }
		}
	}
	
	public void alertbox(String title, String mymessage) {
		new AlertDialog.Builder(this)
			.setMessage(mymessage)
			.setTitle(title)
			.setCancelable(true)
			.setNeutralButton(android.R.string.cancel,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			}).show();
	}
	
	public void sendFtp(File source) {
		FTPClient ftp = new FTPClient();
		try
		{
			int reply;
			ftp.connect("ftp.rimg.net");
			alertbox("FTP", "Connected to ftp.rimg.net");
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply))
			{
				ftp.disconnect();
				alertbox("Error", "FTP server refused connection.");
			}
		}
		catch (IOException e)
		{
			if (ftp.isConnected())
			{
				try
				{
					ftp.disconnect();
				}
				catch (IOException f)
				{
					// do nothing
				}
			}
		}

__main:
		try
		{
			if (!ftp.login("Android", "4ndr01d"))
			{
				ftp.logout();
				alertbox("Error", "Unable to login");
				break __main;
			} else {
				alertbox("Message", "Login Successful");
			}
			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			ftp.enterLocalPassiveMode();
//			InputStream input = new FileInputStream(TEMP_PHOTO_FILE.getAbsolutePath());
//			FileInputStream input = new FileInputStream(Environment.getExternalStorageDirectory() + "/" + "temp_photo.jpg");
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(source));
			alertbox("File Open", "available: " + input.toString());
			ftp.storeFile("temp_photo.jpg", input);
			input.close();
		} catch (FTPConnectionClosedException e) {
			alertbox("Error", "Server closed connection.");
		} catch (IOException e) {
			alertbox("sendFtp: Error", "IO Exception: " + e.getMessage().toString());
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
					// do nothing
				}
			}
		}
	}
	
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState)
//	{
//		if (savedInstanceState.getBoolean(sandbox.PHOTO_TAKEN)) {
//			_taken = true;
//		}
//	}

//	@Override
//	protected void onSaveInstanceState(Bundle outState)
//	{
//		outState.putBoolean(sandbox.PHOTO_TAKEN, _taken);
//	}
	
//	public void StoreImage(Context mContext, Uri imageLoc, File imageDir)
//	{
//		Bitmap bm = null;
//		try {
//			bm = Media.getBitmap(mContext.getContentResolver(), imageLoc);
//			FileOutputStream out = new FileOutputStream(imageDir);
//			bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
//			bm.recycle();
//		} catch (FileNotFoundException e) {
//			Toast.makeText(this, "onSaveInstanceState: FileNotFoundException", Toast.LENGTH_SHORT);
////			e.printStackTrace();
//		} catch (IOException e) {
//			Toast.makeText(this, "onSaveInstanceState: FileNotFoundException", Toast.LENGTH_SHORT);
////			e.printStackTrace();
//		} catch (Exception e) {
//			Toast.makeText(this, "onSaveInstanceState: FileNotFoundException", Toast.LENGTH_SHORT);
////			e.printStackTrace();
//		}
//	}
}