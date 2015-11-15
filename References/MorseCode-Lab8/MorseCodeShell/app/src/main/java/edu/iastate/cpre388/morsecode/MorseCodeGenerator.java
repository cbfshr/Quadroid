package edu.iastate.cpre388.morsecode;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;
 

 
public class MorseCodeGenerator extends Activity {
 
	private static final byte SHORT = (byte) 0;
	private static final byte LONG = (byte) 1;
	private static final byte LETTER = (byte) 2;
	private static final byte WORD = (byte) 3;
	private static final byte STOP = (byte) 4;
	
	// TAG is used to debug in Android logcat console
	private static final String TAG = "ArduinoAccessory";
 
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
 
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
		
	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
 
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};
 
 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
 
		setContentView(R.layout.activity_morse_code_generator);
		
	}
 
 
	@Override
	public void onResume() {
		super.onResume();
 
		if (mInputStream != null && mOutputStream != null) {
			return;
		}
 
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
	}
 
	@Override
	public void onPause() {
		super.onPause();
		closeAccessory();
	}
 
	@Override
	public void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}
 
	private void openAccessory(UsbAccessory accessory) {
	    // TODO: Complete this method following instructions in lab manual.
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Log.d(TAG, "accessory opened");
        } else {
            Log.d(TAG, "accessory open fail");
        }
	}
 
 
	private void closeAccessory() {
		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}
	}
 
	public void blinkLED(View v) {
		// TODO: Encode your message and transmit it to the Arduino to be displayed. (See BlinkApp for ideas on how to do this)

        TextView morseCodeMessage = (TextView)findViewById(R.id.morseCodeMessage);

        try {
            mOutputStream.write(encodeMessage(morseCodeMessage.getText().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
 
	private byte[] encodeMessage(String msg) {
		byte[] encoded = new byte[125];
		Scanner s = new Scanner(msg.toUpperCase());
		
		int index = 0;
		while(s.hasNext()) {
			String word = s.next();
			for (int i = 0; i < word.length(); i++) {
				char c = word.charAt(i);
				switch(c) {
				case 'A':
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					break;
				case 'B':
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					break;
				case 'C': 
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					break;
				case 'D':
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					break;
				case 'E':
					encoded[index++] = SHORT;
					break;
				case 'F': 
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					break;
				case 'G': 
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					break;
				case 'H':
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					break;
				case 'I':
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					break;
				case 'J':
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					break;
				case 'K':
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					break;
				case 'L':
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					break;
				case 'M':
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					break;
				case 'N':
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					break;
				case 'O':
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					break;
				case 'P':
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					break;
				case 'Q':
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					break;
				case 'R':
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					break;
				case 'S':
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					break;
				case 'T':
					encoded[index++] = LONG;
					break;
				case 'U':
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					break;
				case 'V':
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					break;
				case 'W':
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					break;
				case 'X':
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					break;
				case 'Y':
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					break;
				case 'Z':
					encoded[index++] = LONG;
					encoded[index++] = LONG;
					encoded[index++] = SHORT;
					encoded[index++] = SHORT;
					break;
				default: 
					break;
				}
				
				if (i != word.length() - 1) {
					encoded[index++] = LETTER;
				}
			}
			encoded[index++] = WORD;
		}
		encoded[index] = STOP;
		return encoded;
	}
	
}
