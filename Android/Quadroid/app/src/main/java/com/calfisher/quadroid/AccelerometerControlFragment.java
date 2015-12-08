package com.calfisher.quadroid;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccelerometerControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccelerometerControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccelerometerControlFragment extends Fragment implements SensorEventListener {
	private static final String ARG_TITLE = "fragment_title";
	private int title;
	private int MAX_TOUCHES = 1;

	private ImageView leftJoystick = null;
	private TextView leftJoystickPosition = null;
	private TextView leftJoystickValue = null;
	private ImageView leftJoystickIndicator = null;
	private float[] xTouchPosition = new float[MAX_TOUCHES];
	private float[] yTouchPosition = new float[MAX_TOUCHES];
	private boolean leftJoystickPositionSet = false;
	private float leftJoystickPositionX = 0;
	private float leftJoystickPositionY = 0;
	int[] leftJoystickCoordinates = new int[2];
	private int leftJoystickTop = 0;
	private int leftJoystickLeft = 0;
	private int leftTouchIndex = -1;

	private SensorManager sensorManager;

	private TextView axVal;
	private TextView ayVal;
	private ImageView xAccelerometerIndicator = null;
	private ImageView yAccelerometerIndicator = null;
	private float xAccelerometerValue = 50;
	private float yAccelerometerValue = 50;

	private OnFragmentInteractionListener mListener;

	FileOutputStream mOutputStream;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param title Title of the Fragment
	 * @return A new instance of fragment AccelerometerControlFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static AccelerometerControlFragment newInstance(int title) {
		AccelerometerControlFragment fragment = new AccelerometerControlFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TITLE, title);
		fragment.setArguments(args);
		return fragment;
	}

	public AccelerometerControlFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			title = getArguments().getInt(ARG_TITLE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_accelerometer_control, container, false);

		// JOYSTICK
		leftJoystickPosition = (TextView)view.findViewById(R.id.left_joystick_position);
		leftJoystickValue = (TextView)view.findViewById(R.id.left_joystick_value);
		leftJoystick = (ImageView)view.findViewById(R.id.left_joystick);
		leftJoystickIndicator = (ImageView)view.findViewById(R.id.left_joystick_indicator);

		// ACCELEROMETER
		//Get TextViews to output values
		axVal = (TextView) view.findViewById(R.id.AxValue);
		ayVal = (TextView) view.findViewById(R.id.AyValue);

		xAccelerometerIndicator = (ImageView)view.findViewById(R.id.x_accelerometer_indicator);
		yAccelerometerIndicator = (ImageView)view.findViewById(R.id.y_accelerometer_indicator);

		//Set up Sensor
		sensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int pointerCount = event.getPointerCount();
				int maskedAction = event.getActionMasked();

				if ((maskedAction == MotionEvent.ACTION_UP ||
						maskedAction == MotionEvent.ACTION_POINTER_UP)) {
					if (pointerCount == 1) {
						if (event.getActionIndex() == leftTouchIndex) {
							Log.v("Index Up1: ", String.format("%d", event.getActionIndex()));
							leftJoystickPositionSet = false;
							leftTouchIndex = -1;
							resetLeftJoystickPosition(false);
						}
					}/* else {
						leftJoystickPositionSet = false;
						leftTouchIndex = -1;
						resetLeftJoystickPosition(false);
					}*/
				} else {
					getJoystickPositions();

					for (int i = 0; i < pointerCount && i < MAX_TOUCHES; i++) {
						xTouchPosition[i] = MotionEventCompat.getX(event, i);
						yTouchPosition[i] = MotionEventCompat.getY(event, i);

						if (leftJoystickPositionSet == false) {
							if (xTouchPosition[i] > leftJoystickLeft &&
									xTouchPosition[i] < leftJoystick.getWidth() + leftJoystickLeft &&
									yTouchPosition[i] > leftJoystickTop &&
									yTouchPosition[i] < leftJoystick.getBottom() + leftJoystickTop) {
								Log.v("Within Left Joystick:", "True");

								leftJoystickPositionX = xTouchPosition[i];
								leftJoystickPositionY = yTouchPosition[i];

								leftJoystickPositionSet = true;
								leftTouchIndex = i;
							}
						} else {
							leftJoystickPositionX = xTouchPosition[leftTouchIndex];
							leftJoystickPositionY = yTouchPosition[leftTouchIndex];

							// If the touch point is beyond the bounds, put them at the bounds.
							if (leftJoystickPositionX < leftJoystickLeft) {
								leftJoystickPositionX = leftJoystickLeft;
							} else if (leftJoystickPositionX > leftJoystick.getWidth() + leftJoystickLeft) {
								leftJoystickPositionX = leftJoystick.getWidth() + leftJoystickLeft;
							}

							if (leftJoystickPositionY < leftJoystickTop) {
								leftJoystickPositionY = leftJoystickTop;
							} else if (leftJoystickPositionY > leftJoystick.getBottom() + leftJoystickTop - calculateActivityTop()) {
								leftJoystickPositionY = leftJoystick.getBottom() + leftJoystickTop - calculateActivityTop();
							}
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
						resetLeftJoystickPosition(false);
					}


					//leftJoystickPositionSet = false;
					//rightJoystickPositionSet = false;
				}


				Log.e("X Accel Value0", String.format("%f", xAccelerometerValue));
				Log.e("Y Accel Value0", String.format("%f", yAccelerometerValue));
				sendControlValues(
					(int) ((1 - ((leftJoystickPositionX - leftJoystickLeft) / leftJoystick.getWidth())) * 100),
					(int) ((1 - ((leftJoystickPositionY - leftJoystickTop) / leftJoystick.getHeight())) * 100),
					(int) xAccelerometerValue,
					100 - (int) yAccelerometerValue
				);
				return true;
			}
		});

		return view;
	}

	// Left joystick: Center X, No Change Y
	// If resetPositionY is true, then the Y value (thrust) will be reset to 0
	private void resetLeftJoystickPosition(boolean resetPositionY) {
		leftJoystickPositionX = leftJoystickLeft + leftJoystick.getWidth() / 2;
		if(resetPositionY) {
			leftJoystickPositionY = leftJoystickTop + leftJoystick.getHeight();
		}

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

	private void getJoystickPositions() {
		leftJoystick.getLocationOnScreen(leftJoystickCoordinates);
		leftJoystickLeft = leftJoystickCoordinates[0];                                // X coordinate
		leftJoystickTop = leftJoystickCoordinates[1] - calculateActivityTop();        // Y coordinate
	}

		@Override
	public void onResume() {
		super.onResume();

		MainActivity mainActivity = (MainActivity)getActivity();
		mOutputStream = mainActivity.getOutputStream();
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
		/*try {
			mListener = (OnFragmentInteractionListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString() + " must implement OnFragmentInteractionListener");
		}*/
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//Check that sensor event is accelerometer
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			//convert range to a 0-100
			//float xValue = (event.values[0] + 10) * 5;
			//float yValue = (event.values[1] + 10) * 5;
			xAccelerometerValue = (event.values[0] + 10) * 5;
			yAccelerometerValue = (event.values[1] + 10) * 5;

			axVal.setText(String.valueOf(xAccelerometerValue));  //left-right Roll
			ayVal.setText(String.valueOf(100 - yAccelerometerValue)); //front-back Pitch

			DisplayMetrics displayMetrics = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) xAccelerometerIndicator.getLayoutParams();
			lp.setMargins(
					(int) (((100 - xAccelerometerValue) * displayMetrics.widthPixels) / 100.0) - (xAccelerometerIndicator.getWidth() / 2),
					(displayMetrics.heightPixels - calculateActivityTop() - (yAccelerometerIndicator.getHeight() / 2)) / 2,
					0,
					0
			);
			xAccelerometerIndicator.setLayoutParams(lp);

			RelativeLayout.LayoutParams lp2 = (android.widget.RelativeLayout.LayoutParams) yAccelerometerIndicator.getLayoutParams();
			lp2.setMargins(
					(displayMetrics.widthPixels - xAccelerometerIndicator.getWidth()) / 2,
					(int) ((yAccelerometerValue * (displayMetrics.heightPixels - calculateActivityTop() - (yAccelerometerIndicator.getHeight() / 2))) / 100.0),
					0,
					0
			);
			yAccelerometerIndicator.setLayoutParams(lp2);


			//Log.e("X Accel Value1", String.format("%f", xAccelerometerValue));
			//Log.e("Y Accel Value1", String.format("%f", yAccelerometerValue));
			/*sendControlValues(
					50,
					0,
					(int) xAccelerometerValue,
					(int) yAccelerometerValue
			);*/
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	// Sends the joystick values in the form of a byte array to the Arduino.
	// The values are between 0-100 (Ratio value * 100)
	private void sendControlValues(int a, int b, int c, int d) {
		byte[] signal = new byte[6];
		signal[0] = (byte)a;
		signal[1] = (byte)b;
		signal[2] = (byte)101;
		signal[3] = (byte)101;
		//Log.e("X Accel Value2", String.format("%d", c));
		signal[4] = (byte)c;
		//Log.e("Y Accel Value2", String.format("%d", d));
		signal[5] = (byte)d;

		try {
			if(mOutputStream != null) {
				mOutputStream.write(signal);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
