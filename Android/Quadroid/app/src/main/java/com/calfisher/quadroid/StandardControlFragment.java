package com.calfisher.quadroid;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


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

	private OnFragmentInteractionListener mListener;

	private TextView standardControlTouchPosition = null;
	private ImageView leftJoystick = null;
	private ImageView rightJoystick = null;
	private TextView leftJoystickPosition = null;
	private TextView rightJoystickPosition = null;

	private float xTouchPosition = 0;
	private float yTouchPosition = 0;
	private float xTouchPosition0 = 0;
	private float yTouchPosition0 = 0;
	private float xTouchPosition1 = 0;
	private float yTouchPosition1 = 0;


	private float leftJoystickPositionX = 0;
	private float leftJoystickPositionY = 0;
	private float rightJoystickPositionX = 0;
	private float rightJoystickPositionY = 0;

	private int leftJoystickTop = 0;
	private int leftJoystickLeft = 0;
	private float rightJoystickTop = 0;
	private float rightJoystickLeft = 0;

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param title Title of the Fragment
	 * @return A new instance of fragment StandardControlFragment.
	 */
	// TODO: Rename and change types and number of parameters
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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_standard_control, container, false);
		standardControlTouchPosition = (TextView)view.findViewById(R.id.standard_control_touch_position);
		leftJoystickPosition = (TextView)view.findViewById(R.id.left_joystick_position);
		rightJoystickPosition = (TextView)view.findViewById(R.id.right_joystick_position);

		leftJoystick = (ImageView)view.findViewById(R.id.left_joystick);
		rightJoystick = (ImageView)view.findViewById(R.id.right_joystick);

		/*int[] leftJoystickCoordinates = new int[2];
		leftJoystick.getLocationOnScreen(leftJoystickCoordinates);
		leftJoystickLeft = leftJoystickCoordinates[0];	// X coordinate
		leftJoystickTop = leftJoystickCoordinates[1];	// Y coordinate

		int[] rightJoystickCoordinates = new int[2];
		rightJoystick.getLocationOnScreen(rightJoystickCoordinates);
		rightJoystickLeft = rightJoystickCoordinates[0];	// X coordinate
		rightJoystickTop = rightJoystickCoordinates[1];	// Y coordinate*/


		/*final GestureDetector gestureDetector = new GestureDetector(getActivity(),
			new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onDown(MotionEvent e) {
					return true;
				}

				@Override
				public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
					Toast.makeText(getContext(), "BYYEEE", Toast.LENGTH_SHORT).show();

					return true;
				}
			}
		);*/

		view.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				/*if (event.getAction() == MotionEvent.ACTION_MOVE) {
					Toast.makeText(getContext(), "Hello", Toast.LENGTH_SHORT).show();
				}*/
				int pointerCount = event.getPointerCount();
				int maskedAction = event.getActionMasked();

				if(maskedAction == MotionEvent.ACTION_UP || maskedAction == MotionEvent.ACTION_POINTER_DOWN) {
					standardControlTouchPosition.setText("Touch the screen!");
				} else {
					//standardControlTouchPosition.setText(String.format("%f, %f", v.getLeft(), v.getTop()));

					if(pointerCount == 1) {
						int leftJoystickIndex = -1;
						int rightJoystickIndex = -1;
						xTouchPosition0 = MotionEventCompat.getX(event, 0);
						yTouchPosition0 = MotionEventCompat.getY(event, 0);

						leftJoystickIndex = checkLeftJoystick(xTouchPosition0, yTouchPosition0, -1, -1);
						if(leftJoystickIndex == 0) {
							leftJoystickPosition.setText(String.format("%f, %f", xTouchPosition0, yTouchPosition0));
							return true;
						}

						rightJoystickIndex = checkRightJoystick(xTouchPosition0, yTouchPosition0, -1, -1);
						if(rightJoystickIndex == 0) {
							rightJoystickPosition.setText(String.format("%f, %f", xTouchPosition0, yTouchPosition0));
							return true;
						}

						/*xTouchPosition = (int)MotionEventCompat.getX(event, 1);
						yTouchPosition = (int)MotionEventCompat.getY(event, 1);
						rightJoystickPosition.setText(String.format("%f, %f", xTouchPosition, yTouchPosition));*/
					} else if(pointerCount == 2) {
						int leftJoystickIndex = -1;
						int rightJoystickIndex = -1;
						xTouchPosition0 = MotionEventCompat.getX(event, 0);
						yTouchPosition0 = MotionEventCompat.getY(event, 0);
						xTouchPosition1 = MotionEventCompat.getX(event, 1);
						yTouchPosition1 = MotionEventCompat.getY(event, 1);

						leftJoystickIndex = checkLeftJoystick(xTouchPosition0, yTouchPosition0, xTouchPosition1, yTouchPosition1);
						if(leftJoystickIndex == 0) {
							leftJoystickPosition.setText(String.format("%f, %f", xTouchPosition0, yTouchPosition0));
						} else if(leftJoystickIndex == 1) {
							leftJoystickPosition.setText(String.format("%f, %f", xTouchPosition1, yTouchPosition1));
						}

						rightJoystickIndex = checkRightJoystick(xTouchPosition0, yTouchPosition0, xTouchPosition1, yTouchPosition1);
						if(rightJoystickIndex != -1) {
							if(leftJoystickIndex == -1) {
								rightJoystickIndex = 0;
							} else if(leftJoystickIndex == 0) {
								rightJoystickIndex = 1;
							} else {
								rightJoystickIndex = 0;
							}
							if(rightJoystickIndex == 0) {
								rightJoystickPosition.setText(String.format("%f, %f", xTouchPosition0, yTouchPosition0));
								return true;
							}
							if (rightJoystickIndex == 1) {
								rightJoystickPosition.setText(String.format("%f, %f", xTouchPosition1, yTouchPosition1));
							}
						}
					}
				}

				//return gestureDetector.onTouchEvent(event);
				return true;
			}

			private int checkLeftJoystick(float x0, float y0, float x1, float y1) {
				int[] leftJoystickCoordinates = new int[2];
				leftJoystick.getLocationOnScreen(leftJoystickCoordinates);
				leftJoystickLeft = leftJoystickCoordinates[0];	// X coordinate
				leftJoystickTop = leftJoystickCoordinates[1];	// Y coordinate

				if(x0 > leftJoystickLeft &&
						x0 < leftJoystick.getWidth() &&
						y0 > leftJoystickTop &&
						y0 < leftJoystick.getBottom() + leftJoystickTop ) {
					//Log.v("Yo Momma", "Within left joystick");
					return 0;
				} else if(x1 > leftJoystickLeft &&
						x1 < leftJoystick.getWidth() &&
						y1 > leftJoystickTop &&
						y1 < leftJoystick.getBottom() + leftJoystickTop) {
					return 1;
				}

				return -1;
			}

			private int checkRightJoystick(float x0, float y0, float x1, float y1) {
				int[] rightJoystickCoordinates = new int[2];
				rightJoystick.getLocationOnScreen(rightJoystickCoordinates);
				rightJoystickLeft = rightJoystickCoordinates[0];	// X coordinate
				rightJoystickTop = rightJoystickCoordinates[1];	// Y coordinate


				/*Log.v("Yo Momma", String.format("(%f, %f) (%f, %f)", x0, y0, x1, y1));
				Log.v("Yo Momma", String.format("(%f, %f)", rightJoystickLeft, rightJoystickTop));
				Log.v("Yo Momma", String.format("%d", rightJoystick.getWidth()));
				Log.v("Yo Momma", String.format("%d", rightJoystick.getBottom() + (int)rightJoystickTop));*/

				if(x0 > rightJoystickLeft &&
						x0 < rightJoystick.getWidth() + rightJoystickLeft &&
						y0 > rightJoystickTop &&
						y0 < rightJoystick.getBottom() + rightJoystickTop ) {
					//Log.v("Yo Momma", "Within left joystick");
					return 0;
				} else if(x1 > rightJoystickLeft &&
						x1 < rightJoystick.getWidth() &&
						y1 > rightJoystickTop &&
						y1 < rightJoystick.getBottom() + rightJoystickTop) {
					return 1;
				}

				return -1;
			}
		});

		return view;
		//return inflater.inflate(R.layout.fragment_standard_control, container, false);
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
