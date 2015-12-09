package com.calfisher.quadroid;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
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
 * {@link StandardControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StandardControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StandardControlFragment extends Fragment {
	private static final String ARG_TITLE = "fragment_title";
	private int title;

	// The maximum amount of touches allowed on the screen
	private int MAX_TOUCHES = 2;

	private OnFragmentInteractionListener mListener;

	private ImageView leftJoystick = null;
	private ImageView rightJoystick = null;
	private TextView leftJoystickValue = null;
	private TextView rightJoystickValue = null;

	// Variables for the touch indicators on the joysticks (red circles)
	private ImageView leftJoystickIndicator = null;
	private ImageView rightJoystickIndicator = null;
	private float[] xTouchPosition = new float[MAX_TOUCHES];
	private float[] yTouchPosition = new float[MAX_TOUCHES];

	private boolean leftJoystickPositionSet = false;
	private boolean rightJoystickPositionSet = false;

	private float leftJoystickPositionX = 0;
	private float leftJoystickPositionY = 0;
	private float rightJoystickPositionX = 0;
	private float rightJoystickPositionY = 0;

	// Variables to keep track of the joysticks' positions on the screen
	int[] leftJoystickCoordinates = new int[2];
	private int leftJoystickTop = 0;
	private int leftJoystickLeft = 0;
	int[] rightJoystickCoordinates = new int[2];
	private int rightJoystickTop = 0;
	private int rightJoystickLeft = 0;

	// Ensures that touches do not get confused
	private int leftTouchIndex = -1;
	private int rightTouchIndex = -1;

	// The output stream (communication with the Arduino)
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

	// Required empty public constructor
	public StandardControlFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			title = getArguments().getInt(ARG_TITLE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_standard_control, container, false);

		leftJoystickValue = (TextView)view.findViewById(R.id.left_joystick_value);
		rightJoystickValue = (TextView)view.findViewById(R.id.right_joystick_value);

		leftJoystick = (ImageView)view.findViewById(R.id.left_joystick);
		rightJoystick = (ImageView)view.findViewById(R.id.right_joystick);

		leftJoystickIndicator = (ImageView)view.findViewById(R.id.left_joystick_indicator);
		rightJoystickIndicator = (ImageView)view.findViewById(R.id.right_joystick_indicator);

		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Amount of touches on the screen
				int pointerCount = event.getPointerCount();
				// The type of action
				int maskedAction = event.getActionMasked();

				// If a finger (pointer) has been taken off, determine which one and
				// reset the settings for that joystick
				if ((maskedAction == MotionEvent.ACTION_UP ||
						maskedAction == MotionEvent.ACTION_POINTER_UP)) {
					if (pointerCount == 2) {
						if (event.getActionIndex() == leftTouchIndex) {
							leftJoystickPositionSet = false;
							leftTouchIndex = -1;
							resetLeftJoystickPosition(false);

							if (rightTouchIndex == 1) {
								rightTouchIndex = 0;
							}
						}
						if (event.getActionIndex() == rightTouchIndex) {
							rightJoystickPositionSet = false;
							rightTouchIndex = -1;
							resetRightJoystickPosition();

							if (leftTouchIndex == 1) {
								leftTouchIndex = 0;
							}
						}
					} else {
						leftJoystickPositionSet = false;
						leftTouchIndex = -1;
						resetLeftJoystickPosition(false);

						rightJoystickPositionSet = false;
						rightTouchIndex = -1;
						resetRightJoystickPosition();
					}
				} else {
					// Where are the bounds of the joysticks?
					getJoystickPositions();

					// Loop through the amount of touches on the screen to determine where they are located
					for (int i = 0; i < pointerCount && i < MAX_TOUCHES; i++) {
						xTouchPosition[i] = MotionEventCompat.getX(event, i);
						yTouchPosition[i] = MotionEventCompat.getY(event, i);

						// If the right joystick position was unknown, check if the touch is within the joystick
						if (rightJoystickPositionSet == false) {
							if (xTouchPosition[i] > rightJoystickLeft &&
									xTouchPosition[i] < rightJoystick.getWidth() + rightJoystickLeft &&
									yTouchPosition[i] > rightJoystickTop &&
									yTouchPosition[i] < rightJoystick.getBottom() + rightJoystickTop) {
								rightJoystickPositionX = xTouchPosition[i];
								rightJoystickPositionY = yTouchPosition[i];

								rightJoystickPositionSet = true;
								rightTouchIndex = i;
							}
						} else {
							rightJoystickPositionX = xTouchPosition[rightTouchIndex];
							rightJoystickPositionY = yTouchPosition[rightTouchIndex];

							// If the touch point is beyond the bounds, put them at the extremes.
							if (rightJoystickPositionX < rightJoystickLeft) {
								rightJoystickPositionX = rightJoystickLeft;
							} else if (rightJoystickPositionX > rightJoystick.getWidth() + rightJoystickLeft) {
								rightJoystickPositionX = rightJoystick.getWidth() + rightJoystickLeft;
							}

							if (rightJoystickPositionY < rightJoystickTop) {
								rightJoystickPositionY = rightJoystickTop;
							} else if (rightJoystickPositionY > rightJoystick.getBottom() + rightJoystickTop) {
								rightJoystickPositionY = rightJoystick.getBottom() + rightJoystickTop;
							}
						}

						// If the left joystick position was unknown, check if the touch is within the joystick
						if (leftJoystickPositionSet == false) {
							if (xTouchPosition[i] > leftJoystickLeft &&
									xTouchPosition[i] < leftJoystick.getWidth() + leftJoystickLeft &&
									yTouchPosition[i] > leftJoystickTop &&
									yTouchPosition[i] < leftJoystick.getBottom() + leftJoystickTop) {

								leftJoystickPositionX = xTouchPosition[i];
								leftJoystickPositionY = yTouchPosition[i];

								leftJoystickPositionSet = true;
								leftTouchIndex = i;
							}
						} else {
							//
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
							} else if (leftJoystickPositionY > leftJoystick.getBottom() + leftJoystickTop) {
								leftJoystickPositionY = leftJoystick.getBottom() + leftJoystickTop;
							}
						}
					}

					if (leftJoystickPositionSet == true) {
						RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) leftJoystickIndicator.getLayoutParams();
						lp.setMargins(
							(int) leftJoystickPositionX - (leftJoystickIndicator.getWidth() / 2),
							(int) leftJoystickPositionY - (leftJoystickIndicator.getWidth() / 2),
							0,
							0
						);
						leftJoystickIndicator.setLayoutParams(lp);
						leftJoystickValue.setText(String.format(
								"Left Joystick: (Yaw: %f, Thrust: %f)",
								1 - (leftJoystickPositionX - leftJoystickLeft) / leftJoystick.getWidth(),
								1 - (leftJoystickPositionY - leftJoystickTop) / leftJoystick.getHeight()
						));
					} else {
						resetLeftJoystickPosition(false);
					}

					// If we already know the righ joystick position,
					if (rightJoystickPositionSet == true) {
						// Set the position of the right joystick indicator on the screen
						RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) rightJoystickIndicator.getLayoutParams();
						lp.setMargins(
							(int) rightJoystickPositionX - (rightJoystickIndicator.getWidth() / 2),
							(int) rightJoystickPositionY - (rightJoystickIndicator.getWidth() / 2),
							0,
							0
						);
						rightJoystickIndicator.setLayoutParams(lp);

						rightJoystickValue.setText(String.format(
								"Right Joystick: (Roll: %f, Pitch: %f)",
								1 - (rightJoystickPositionX - rightJoystickLeft) / rightJoystick.getWidth(),
								1 - (rightJoystickPositionY - rightJoystickTop) / rightJoystick.getHeight()
						));
					} else {
						resetRightJoystickPosition();
					}
				}

				// Send the calculated values to the Arduino
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

	// Left joystick: Center X, No Change Y
	// If resetPositionY is true, then the Y value (thrust) will be reset to 0
	private void resetLeftJoystickPosition(boolean resetPositionY) {
		// Recalculate the center of the left joystick (leave the thrust [up-down] as-is)
		leftJoystickPositionX = leftJoystickLeft + leftJoystick.getWidth() / 2;
		if(resetPositionY) {
			leftJoystickPositionY = leftJoystickTop + leftJoystick.getHeight();
		}

		// Display the red circle at the original position
		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) leftJoystickIndicator.getLayoutParams();
		lp.setMargins(
				(int) leftJoystickPositionX - (leftJoystickIndicator.getWidth() / 2),
				(int) leftJoystickPositionY - (leftJoystickIndicator.getWidth() / 2),
				0,
				0
		);
		leftJoystickIndicator.setLayoutParams(lp);

		leftJoystickValue.setText(String.format(
				"Left Joystick: (Yaw: %f, Thrust: %f)",
				1 - (leftJoystickPositionX - leftJoystickLeft) / leftJoystick.getWidth(),
				1 - (leftJoystickPositionY - leftJoystickTop) / leftJoystick.getHeight()
		));
	}

	// Right Joystick: Center X, Center Y
	private void resetRightJoystickPosition() {
		// Recalculate the center of the right joystick
		rightJoystickPositionX = rightJoystickLeft + rightJoystick.getWidth() / 2;
		rightJoystickPositionY = rightJoystickTop + rightJoystick.getWidth() / 2;

		// Display the red circle at the original position
		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) rightJoystickIndicator.getLayoutParams();
		lp.setMargins(
				(int) rightJoystickPositionX - (rightJoystickIndicator.getWidth() / 2),
				(int) rightJoystickPositionY - (rightJoystickIndicator.getWidth() / 2),
				0,
				0
		);
		rightJoystickIndicator.setLayoutParams(lp);

		rightJoystickValue.setText(String.format(
				"Right Joystick: (Roll: %f, Pitch: %f)",
				1 - (rightJoystickPositionX - rightJoystickLeft) / rightJoystick.getWidth(),
				1 - (rightJoystickPositionY - rightJoystickTop) / rightJoystick.getHeight()
		));
	}

	// Where are the joysticks on the screen?
	private void getJoystickPositions() {
		leftJoystick.getLocationOnScreen(leftJoystickCoordinates);
		leftJoystickLeft = leftJoystickCoordinates[0];								// X coordinate
		leftJoystickTop = leftJoystickCoordinates[1] - calculateActivityTop();		// Y coordinate

		rightJoystick.getLocationOnScreen(rightJoystickCoordinates);
		rightJoystickLeft = rightJoystickCoordinates[0];							// X coordinate
		rightJoystickTop = rightJoystickCoordinates[1] - calculateActivityTop();	// Y coordinate
	}

	// Sends the joystick values in the form of a byte array to the Arduino.
	// The values are between 0-100 (Ratio value * 100)
	private void sendControlValues(int a, int b, int c, int d) {
		byte[] signal = new byte[6];
		signal[0] = (byte)a;
		signal[1] = (byte)b;
		signal[2] = (byte)c;
		signal[3] = (byte)d;
		signal[4] = (byte)101;
		signal[5] = (byte)101;

		try {
			if(mOutputStream != null) {
				mOutputStream.write(signal);
			}
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

		/*try {
			mListener = (OnFragmentInteractionListener) getActivity();
		} catch (ClassCastException e) {
			throw new ClassCastException(getActivity().toString() + " must implement OnFragmentInteractionListener");
		}*/
	}

	@Override
	public void onResume() {
		super.onResume();

		MainActivity mainActivity = (MainActivity)getActivity();
		mOutputStream = mainActivity.getOutputStream();
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
