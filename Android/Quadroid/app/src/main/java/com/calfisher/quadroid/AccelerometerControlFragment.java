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
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
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

	private SensorManager sensorManager;

	private TextView axVal;
	private TextView ayVal;
	//private TextView azVal;

	private ImageView xAccelerometerIndicator = null;
	private ImageView yAccelerometerIndicator = null;

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
		// Inflate the layout for this fragment
		//return inflater.inflate(R.layout.fragment_accelerometer_control, container, false);

		View view = inflater.inflate(R.layout.fragment_accelerometer_control, container, false);

		//Get TextViews to output values
		axVal = (TextView) view.findViewById(R.id.AxValue);
		ayVal = (TextView) view.findViewById(R.id.AyValue);
		//azVal = (TextView) view.findViewById(R.id.AzValue);

		xAccelerometerIndicator = (ImageView)view.findViewById(R.id.x_accelerometer_indicator);
		yAccelerometerIndicator = (ImageView)view.findViewById(R.id.y_accelerometer_indicator);

		//Set up Sensor
		sensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

		return view;
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
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			//convert range to a 0-100
			float xValue = (event.values[0] + 10) * 5;
			float yValue = (event.values[1] + 10) * 5;

			axVal.setText(String.valueOf((event.values[0] + 10) * 5));  //left-right Roll
			ayVal.setText(String.valueOf((event.values[1] + 10) * 5)); //front-back Pitch
			//azVal.setText(String.valueOf(event.values[2]));

			DisplayMetrics displayMetrics = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

			RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) xAccelerometerIndicator.getLayoutParams();
			lp.setMargins(
					(int) (((100 - xValue) * displayMetrics.widthPixels) / 100.0) - (xAccelerometerIndicator.getWidth() / 2),
					(int) ((displayMetrics.heightPixels - calculateActivityTop() - (yAccelerometerIndicator.getHeight() / 2)) / 2),
					0,
					0
			);
			xAccelerometerIndicator.setLayoutParams(lp);

			RelativeLayout.LayoutParams lp2 = (android.widget.RelativeLayout.LayoutParams) yAccelerometerIndicator.getLayoutParams();
			lp2.setMargins(
					(int) (displayMetrics.widthPixels - yAccelerometerIndicator.getWidth()) / 2,
					(int) ((yValue * (displayMetrics.heightPixels - calculateActivityTop() - (yAccelerometerIndicator.getHeight() / 2))) / 100.0),
					0,
					0
			);
			yAccelerometerIndicator.setLayoutParams(lp2);

			sendControlValues(50, 0, (int)xValue, (int)yValue);
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
		signal[4] = (byte)c;
		signal[5] = (byte)d;

		try {
			mOutputStream.write(signal);
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
