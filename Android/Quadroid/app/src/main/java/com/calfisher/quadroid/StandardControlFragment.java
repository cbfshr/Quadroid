package com.calfisher.quadroid;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StandardControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StandardControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StandardControlFragment extends Fragment {
	private static final String ARG_TITLE = "fragment_title";
	private int title;
	private int MAX_TOUCHES = 2;
	private OnFragmentInteractionListener mListener;

	private TextView standardControlTouchPosition = null;
	private ImageView leftJoystick = null;
	private ImageView rightJoystick = null;
	private TextView leftJoystickPosition = null;
	private TextView rightJoystickPosition = null;
	private TextView leftJoystickValue = null;
	private TextView rightJoystickValue = null;

	private ImageView leftJoystickIndicator = null;
	private float leftJoystickIndicatorX = 0;
	private float leftJoystickIndicatorY = 0;
	private ImageView rightJoystickIndicator = null;
	private float rightJoystickIndicatorX = 0;
	private float rightJoystickIndicatorY = 0;
	private float[] xTouchPosition = new float[MAX_TOUCHES];
	private float[] yTouchPosition = new float[MAX_TOUCHES];

	private boolean leftJoystickPositionSet = false;
	private boolean rightJoystickPositionSet = false;

	private float leftJoystickPositionX = 0;
	private float leftJoystickPositionY = 0;
	private float rightJoystickPositionX = 0;
	private float rightJoystickPositionY = 0;


	int[] leftJoystickCoordinates = new int[2];
	private int leftJoystickTop = 0;
	private int leftJoystickLeft = 0;
	int[] rightJoystickCoordinates = new int[2];
	private int rightJoystickTop = 0;
	private int rightJoystickLeft = 0;

	// ADK Variables
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

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param title Title of the Fragment
	 * @return A new instance of fragment StandardControlFragment.
	 */
	public static StandardControlFragment newInstance(int title) {
		StandardControlFragment fragment = new StandardControlFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TITLE, title);
		fragment.setArguments(args);
		return fragment;
	}

	public StandardControlFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			title = getArguments().getInt(ARG_TITLE);
		}

		mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
		mPermissionIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		getActivity().registerReceiver(mUsbReceiver, filter);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_standard_control, container, false);

		//standardControlTouchPosition = (TextView)view.findViewById(R.id.standard_control_touch_position);
		leftJoystickPosition = (TextView)view.findViewById(R.id.left_joystick_position);
		rightJoystickPosition = (TextView)view.findViewById(R.id.right_joystick_position);
		leftJoystickValue = (TextView)view.findViewById(R.id.left_joystick_value);
		rightJoystickValue = (TextView)view.findViewById(R.id.right_joystick_value);


		leftJoystick = (ImageView)view.findViewById(R.id.left_joystick);
		rightJoystick = (ImageView)view.findViewById(R.id.right_joystick);

		leftJoystickIndicator = (ImageView)view.findViewById(R.id.left_joystick_indicator);
		rightJoystickIndicator = (ImageView)view.findViewById(R.id.right_joystick_indicator);

		//initializeJoystickPositions();

		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int pointerCount = event.getPointerCount();
				int maskedAction = event.getActionMasked();

				if (maskedAction == MotionEvent.ACTION_UP || maskedAction == MotionEvent.ACTION_POINTER_DOWN) {
					//standardControlTouchPosition.setText("Touch the screen!");
					// Reset the left and right joysticks to where they automatically go
					// Left joystick: Center X, No Change Y
					leftJoystickPositionX = leftJoystickLeft + leftJoystick.getWidth() / 2;

					leftJoystickPosition.setText(String.format("%f, %f", leftJoystickPositionX, leftJoystickPositionY));
					RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) leftJoystickIndicator.getLayoutParams();
					lp.setMargins(
							(int) leftJoystickPositionX - (leftJoystickIndicator.getWidth() / 2),
							(int) leftJoystickPositionY - (leftJoystickIndicator.getWidth() / 2),
							0,
							0
					);
					leftJoystickIndicator.setLayoutParams(lp);
					leftJoystickValue.setText(String.format(
							"(%f, %f)",
							1 - (leftJoystickPositionX - leftJoystickLeft) / leftJoystick.getWidth(),
							1 - (leftJoystickPositionY - leftJoystickTop) / leftJoystick.getHeight()
					));

					// Right Joystick: Center X, Center Y
					rightJoystickPositionX = rightJoystickLeft + rightJoystick.getWidth() / 2;
					rightJoystickPositionY = rightJoystickTop + rightJoystick.getWidth() / 2;

					rightJoystickPosition.setText(String.format("%f, %f", rightJoystickPositionX, rightJoystickPositionY));
					lp = (android.widget.RelativeLayout.LayoutParams) rightJoystickIndicator.getLayoutParams();
					lp.setMargins(
							(int) rightJoystickPositionX - (rightJoystickIndicator.getWidth() / 2),
							(int) rightJoystickPositionY - (rightJoystickIndicator.getWidth() / 2),
							0,
							0
					);
					rightJoystickIndicator.setLayoutParams(lp);
					rightJoystickValue.setText(String.format(
							"(%f, %f)",
							1 - (rightJoystickPositionX - rightJoystickLeft) / rightJoystick.getWidth(),
							1 - (rightJoystickPositionY - rightJoystickTop) / rightJoystick.getHeight()
					));
				} else {
					leftJoystick.getLocationOnScreen(leftJoystickCoordinates);
					leftJoystickLeft = leftJoystickCoordinates[0];    // X coordinate
					leftJoystickTop = leftJoystickCoordinates[1] - calculateActivityTop();        // Y coordinate

					rightJoystick.getLocationOnScreen(rightJoystickCoordinates);
					rightJoystickLeft = rightJoystickCoordinates[0];    // X coordinate
					rightJoystickTop = rightJoystickCoordinates[1] - calculateActivityTop();        // Y coordinate

					for (int i = 0; i < pointerCount && i < MAX_TOUCHES; i++) {
						xTouchPosition[i] = MotionEventCompat.getX(event, i);
						yTouchPosition[i] = MotionEventCompat.getY(event, i);

						if (xTouchPosition[i] > leftJoystickLeft &&
								xTouchPosition[i] < leftJoystick.getWidth() + leftJoystickLeft &&
								yTouchPosition[i] > leftJoystickTop &&
								yTouchPosition[i] < leftJoystick.getBottom() + leftJoystickTop &&
								leftJoystickPositionSet == false) {
							Log.v("Within Left Joystick:", "True");

							leftJoystickPositionX = xTouchPosition[i];
							leftJoystickPositionY = yTouchPosition[i];

							leftJoystickPositionSet = true;
						} else if (xTouchPosition[i] > rightJoystickLeft &&
								xTouchPosition[i] < rightJoystick.getWidth() + rightJoystickLeft &&
								yTouchPosition[i] > rightJoystickTop &&
								yTouchPosition[i] < rightJoystick.getBottom() + rightJoystickTop &&
								rightJoystickPositionSet == false) {
							Log.v("Within Right Joystick:", "True");

							rightJoystickPositionX = xTouchPosition[i];
							rightJoystickPositionY = yTouchPosition[i];

							rightJoystickPositionSet = true;
						}
					}

					if (leftJoystickPositionSet == true) {
						leftJoystickPosition.setText(String.format("%f, %f", leftJoystickPositionX, leftJoystickPositionY));
						RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) leftJoystickIndicator.getLayoutParams();
						lp.setMargins(
								(int) leftJoystickPositionX - (leftJoystickIndicator.getWidth() / 2),
								(int) leftJoystickPositionY - (leftJoystickIndicator.getWidth() / 2),
								0,
								0
						);
						leftJoystickIndicator.setLayoutParams(lp);
						leftJoystickValue.setText(String.format(
								"(%f, %f)",
								1 - (leftJoystickPositionX - leftJoystickLeft) / leftJoystick.getWidth(),
								1 - (leftJoystickPositionY - leftJoystickTop) / leftJoystick.getHeight()
						));
					} else {
						// Reset the left and right joysticks to where they automatically go
						// Left joystick: Center X, No Change Y
						leftJoystickPositionX = leftJoystickLeft + leftJoystick.getWidth() / 2;

						leftJoystickPosition.setText(String.format("%f, %f", leftJoystickPositionX, leftJoystickPositionY));
						RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) leftJoystickIndicator.getLayoutParams();
						lp.setMargins(
								(int) leftJoystickPositionX - (leftJoystickIndicator.getWidth() / 2),
								(int) leftJoystickPositionY - (leftJoystickIndicator.getWidth() / 2),
								0,
								0
						);
						leftJoystickIndicator.setLayoutParams(lp);
						leftJoystickValue.setText(String.format(
								"(%f, %f)",
								1 - (leftJoystickPositionX - leftJoystickLeft) / leftJoystick.getWidth(),
								1 - (leftJoystickPositionY - leftJoystickTop) / leftJoystick.getHeight()
						));
					}

					if (rightJoystickPositionSet == true) {
						rightJoystickPosition.setText(String.format("%f, %f", rightJoystickPositionX, rightJoystickPositionY));
						RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) rightJoystickIndicator.getLayoutParams();
						lp.setMargins(
								(int) rightJoystickPositionX - (rightJoystickIndicator.getWidth() / 2),
								(int) rightJoystickPositionY - (rightJoystickIndicator.getWidth() / 2),
								0,
								0
						);
						rightJoystickIndicator.setLayoutParams(lp);
						rightJoystickValue.setText(String.format(
								"(%f, %f)",
								1 - (rightJoystickPositionX - rightJoystickLeft) / rightJoystick.getWidth(),
								1 - (rightJoystickPositionY - rightJoystickTop) / rightJoystick.getHeight()
						));
					} else {
						// Right Joystick: Center X, Center Y
						rightJoystickPositionX = rightJoystickLeft + rightJoystick.getWidth() / 2;
						rightJoystickPositionY = rightJoystickTop + rightJoystick.getWidth() / 2;

						rightJoystickPosition.setText(String.format("%f, %f", rightJoystickPositionX, rightJoystickPositionY));
						RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) rightJoystickIndicator.getLayoutParams();
						lp.setMargins(
								(int) rightJoystickPositionX - (rightJoystickIndicator.getWidth() / 2),
								(int) rightJoystickPositionY - (rightJoystickIndicator.getWidth() / 2),
								0,
								0
						);
						rightJoystickIndicator.setLayoutParams(lp);
						rightJoystickValue.setText(String.format(
								"(%f, %f)",
								1 - (rightJoystickPositionX - rightJoystickLeft) / rightJoystick.getWidth(),
								1 - (rightJoystickPositionY - rightJoystickTop) / rightJoystick.getHeight()
						));
					}

					leftJoystickPositionSet = false;
					rightJoystickPositionSet = false;
				}

				sendControlValues(
					(int) ((1 - ((leftJoystickPositionX - leftJoystickLeft) / leftJoystick.getWidth())) * 100),
					(int) ((1 - ((leftJoystickPositionY - leftJoystickTop) / leftJoystick.getHeight())) * 100),
					(int) ((1 - ((rightJoystickPositionX - rightJoystickLeft) / rightJoystick.getWidth())) * 100),
					(int) ((1 - ((rightJoystickPositionY - rightJoystickTop) / rightJoystick.getHeight())) * 100)
				);
				return true;
			}
		});

		return view;
	}

	private void sendControlValues(int a, int b, int c, int d) {
		byte[] signal = new byte[6];
		signal[0] = (byte)a;
		//signal[0] = (byte)50;
		signal[1] = (byte)b;
		signal[2] = (byte)c;
		signal[3] = (byte)d;
		signal[4] = (byte)1;
		signal[5] = (byte)1;

		try {
			mOutputStream.write(signal);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// TODO: Rename method, update argument and hook method into UI event
	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		((MainActivity) getActivity()).onSectionAttached(getArguments().getInt(ARG_TITLE));
	}

	@Override
	public void onStart() {
		super.onStart();

		initializeJoystickPositions();

		/*try {
			mListener = (OnFragmentInteractionListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString() + " must implement OnFragmentInteractionListener");
		}*/
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
						mUsbManager.requestPermission(accessory, mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		public void onFragmentInteraction(Uri uri);
	}

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory " + accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};

	private void openAccessory(UsbAccessory accessory) {
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

	// This doesn't work? Is the view not ready for this yet?
	private void initializeJoystickPositions() {
		leftJoystick.getLocationOnScreen(leftJoystickCoordinates);
		leftJoystickLeft = leftJoystickCoordinates[0];	// X coordinate
		leftJoystickTop = leftJoystickCoordinates[1] - calculateActivityTop();		// Y coordinate

		rightJoystick.getLocationOnScreen(rightJoystickCoordinates);
		rightJoystickLeft = rightJoystickCoordinates[0];	// X coordinate
		rightJoystickTop = rightJoystickCoordinates[1] - calculateActivityTop();		// Y coordinate

		leftJoystickPositionX = leftJoystickLeft + leftJoystick.getWidth() / 2;
		leftJoystickPositionY = leftJoystickTop + leftJoystick.getHeight();

		leftJoystickPosition.setText(String.format("%f, %f", leftJoystickPositionX, leftJoystickPositionY));
		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) leftJoystickIndicator.getLayoutParams();
		lp.setMargins(
				(int) leftJoystickPositionX - (leftJoystickIndicator.getWidth() / 2),
				(int) leftJoystickPositionY - (leftJoystickIndicator.getWidth() / 2),
				0,
				0
		);
		leftJoystickIndicator.setLayoutParams(lp);

		// Right Joystick: Center X, Center Y
		rightJoystickPositionX = rightJoystickLeft + rightJoystick.getWidth() / 2;
		rightJoystickPositionY = rightJoystickTop + rightJoystick.getWidth() / 2;

		rightJoystickPosition.setText(String.format("%f, %f", rightJoystickPositionX, rightJoystickPositionY));
		lp = (android.widget.RelativeLayout.LayoutParams) rightJoystickIndicator.getLayoutParams();
		lp.setMargins(
				(int) rightJoystickPositionX - (rightJoystickIndicator.getWidth() / 2),
				(int) rightJoystickPositionY - (rightJoystickIndicator.getWidth() / 2),
				0,
				0
		);
		rightJoystickIndicator.setLayoutParams(lp);
	}

	/*
	 * This method calculates the top of the application on the screen
	 * to be used as an offset when determining the top of the grid.
	 */
	private int calculateActivityTop() {
		int top = 0;
		final TypedArray styledAttributes = getActivity().getApplicationContext().getTheme().obtainStyledAttributes(
				new int[]{android.R.attr.actionBarSize});
		top = (int) styledAttributes.getDimension(0, 0);
		styledAttributes.recycle();

		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			top += getResources().getDimensionPixelSize(resourceId);
		}
		return top;
	}
}
