package edu.iastate.gestures;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;

/**
 * Activity which represents the left view.
 */
public class ThirdActivity extends CustomGestureListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_third);
		super.setGestureDetector(new GestureDetector(this.getApplicationContext(), this));
		super.setLeftRight(SecondActivity.class, MainActivity.class);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_third, menu);
		return true;
	}
}
