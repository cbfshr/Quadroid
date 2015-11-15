package com.example.mediaplayerpreferences;

import android.content.Intent;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MediaPreferences extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_media_preferences);
		getFragmentManager().beginTransaction().replace(
				android.R.id.content,
				new MediaPreferencesFragment()
		).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_media_preferences, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		} else if(id == R.id.apply_media_preferences) {
			Intent intent = new Intent(getApplicationContext(), MyMediaPlayerActivity.class);
			setResult(RESULT_OK, intent);

			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	public static class MediaPreferencesFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Make sure default values are applied
			PreferenceManager.setDefaultValues(
				getActivity(),
				R.xml.media_preferences,
				false
			);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.media_preferences);
		}
	}

}
