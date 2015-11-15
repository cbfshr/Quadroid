package com.calfisher.quadroid;

import android.app.Activity;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This is a base class for handling swipes in the application.
 */
public class CustomGestureListener extends Activity implements OnGestureListener{
	/*
	 * These variables store activity specific values.
	 */
	private GestureDetector gesture = null;
	private Class<?> leftActivity = null;
	private Class<?> rightActivity = null;

	private static int FLING_DISTANCE = 250;
	private static int FLING_VELOCITY = 100;

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		if (gesture != null)
			return gesture.onTouchEvent(me);
		else
			return false;
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		if(event.getAction() == MotionEvent.ACTION_UP) {
//			TextView textView = (TextView) findViewById(R.id.name);
//			textView.setText("Touch the Screen!");
//		} else {
//			float x = event.getX();
//			float y = event.getY();
//
//			TextView textView = (TextView) findViewById(R.id.name);
//			textView.setText(String.format("%f, %f", x, y));
//		}
//		return true;
//	}

	@Override
	public boolean onDown(MotionEvent e) {
		//insert code here
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		float startX = e1.getX();
		float startY = e1.getY();
		float endX = e2.getX();
		float endY = e2.getY();

		//TextView textView = (TextView)findViewById(R.id.main_screen_text);
		//textView.setText(String.format("Starting X Position:%f\nEnding X Position: %f", startX, endX));

		Toast toast = Toast.makeText(getApplicationContext(), "No Movement", Toast.LENGTH_SHORT);
		//toast.setDuration(Toast.LENGTH_SHORT);
		//toast.setText(">>> No movement <<<");

		if(Math.abs(startX - endX) > FLING_DISTANCE && Math.abs(velocityX) > FLING_VELOCITY) {
			if(startX > endX) {
				//Swipe Left
				toast.setText("Left Activity Starting...");

				//Intent leftActivityIntent = new Intent(this, leftActivity);
				//startActivity(leftActivityIntent);
			} else {
				toast.setText("Right Activity Starting...");

				//Intent rightActivityIntent = new Intent(this, rightActivity);
				//startActivity(rightActivityIntent);
			}
		}

		toast.show();

		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
							float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Sets the gesture detector for the activity
	 * @param gesture the gesture detector specific to the activity
	 */
	public void setGestureDetector(GestureDetector gesture){
		this.gesture = gesture;
	}

	/**
	 * Sets the left and right activity classes which are swiped to
	 * @param leftActivity	The class for the left Activity
	 * @param rightActivity The class for the right Activity
	 */
	public void setLeftRight(Class<?> leftActivity, Class<?> rightActivity){
		this.leftActivity = leftActivity;
		this.rightActivity = rightActivity;
	}
}
