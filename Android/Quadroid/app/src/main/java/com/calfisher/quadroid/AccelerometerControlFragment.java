package com.calfisher.quadroid;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


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
	private TextView azVal;

	private OnFragmentInteractionListener mListener;

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
		azVal = (TextView) view.findViewById(R.id.AzValue);

		//Set up Sensor
		sensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

		return view;
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
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		//Check that sensor event is accelerometer
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			//convert range to a 0-100
			axVal.setText(String.valueOf((event.values[0] + 10) * 5));  //left-right Roll
			ayVal.setText(String.valueOf((event.values[1] + 10) * 5)); //front-back Pitch
			azVal.setText(String.valueOf(event.values[2]));
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

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
}
