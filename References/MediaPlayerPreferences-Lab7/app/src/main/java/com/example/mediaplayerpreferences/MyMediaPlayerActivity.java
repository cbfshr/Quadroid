package com.example.mediaplayerpreferences;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * CPRE 388 - Labs
 * 
 * Copyright 2013
 */
public class MyMediaPlayerActivity extends Activity {

	/**
	 * Other view elements
	 */
	private TextView songTitleLabel;

	private ImageView albumArtwork;

	/**
	 *  media player:
	 *  http://developer.android.com/reference/android/media/MediaPlayer.html 
	 */
	private MediaPlayer mp;

	/**
	 * Index of the current song being played
	 */
	private int currentSongIndex = 0;

	/**
	 * List of Sounds that can be played in the form of SongObjects
	 */
	private static ArrayList<SongObject> songsList = new ArrayList<SongObject>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media_player_main);

		songTitleLabel = (TextView) findViewById(R.id.songTitle);
		albumArtwork = (ImageView)findViewById(R.id.albumCover);

		// Initialize the media player
		mp = new MediaPlayer();

		// Getting all songs in a list
		populateSongsList();

		// By default play first song if there is one in the list
		playSong(0);

		final Button playPauseButton = (Button)findViewById(R.id.playpausebutton);
		playPauseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(mp.isPlaying()) {
					mp.pause();
					//playPauseButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_play));
					((Button)findViewById(R.id.playpausebutton)).setBackgroundResource(R.drawable.btn_play);
				} else {
					mp.start();
					//playPauseButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.btn_pause));
					((Button)findViewById(R.id.playpausebutton)).setBackgroundResource(R.drawable.btn_pause);
				}
			}
		});

		final Button backButton = (Button)findViewById(R.id.backbutton);
		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(currentSongIndex > 0) {
					playSong(--currentSongIndex);
				} else {
					currentSongIndex = songsList.size() - 1;
					playSong(currentSongIndex);
				}
			}
		});

		final Button forwardButton = (Button)findViewById(R.id.forwardbutton);
		forwardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(currentSongIndex < songsList.size() - 1) {
					playSong(++currentSongIndex);
				} else {
					currentSongIndex = 0;
					playSong(currentSongIndex);
				}
			}
		});

		mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				if(currentSongIndex < songsList.size() - 1) {
					playSong(++currentSongIndex);
				} else {
					currentSongIndex = 0;
					playSong(currentSongIndex);
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.media_player_menu, menu);
		return true;
	} 

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_choose_song:
			// Open SongList to display a list of audio files to play
			Intent songListIntent = new Intent(this, SongList.class);
			startActivityForResult(songListIntent, 1);
			return true;
		case R.id.menu_preferences:
			// Display Settings page
			Intent mediaPreferencesIntent = new Intent(this, MediaPreferences.class);
			startActivityForResult(mediaPreferencesIntent, 2);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(1 == requestCode) {
			if(Activity.RESULT_OK == resultCode) {
				playSong(data.getExtras().getInt("songIndex"));
			}
		} else if(2 == requestCode) {
			if(Activity.RESULT_OK == resultCode) {
				populateSongsList();

				currentSongIndex = 0;
				playSong(currentSongIndex);
			}
		}
	}

	/**
	 * Helper function to play a song at a specific index of songsList
	 * @param songIndex - index of song to be played
	 */
	public void  playSong(int songIndex){
		// Play song if index is within the songsList
		if (songIndex < songsList.size() && songIndex >= 0) {
			try {
				mp.stop();
				mp.reset();
				mp.setDataSource(songsList.get(songIndex).getFilePath());
				mp.prepare();
				mp.start();
				// Displaying Song title
				String songTitle = songsList.get(songIndex).getTitle();
				Drawable albumArt = songsList.get(songIndex).getAlbumCover();
				songTitleLabel.setText(songTitle);
				albumArtwork.setBackground(albumArt);

				// Changing Button Image to pause image
				((Button)findViewById(R.id.playpausebutton)).setBackgroundResource(R.drawable.btn_pause);

				// Update song index
				currentSongIndex = songIndex;

			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} 
		} else if (songsList.size() > 0) {
			playSong(0);
		}
	}


	/** 
	 * Get list of info for all sounds to be played
	 */
	public void populateSongsList(){
		mp.stop();
		songsList = new ArrayList<SongObject>();

		Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] mProjection = {
				MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM_ID,
				MediaStore.Audio.Media.DATA
		};
		String mSelectionClause = null;
		String[] mSelectionArgs = null;
		String mSortOrder = null;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		Resources resources = getResources();

		String audio_preferences = preferences.getString(resources.getString(R.string.mp_audio_pref), "Music");
		switch(audio_preferences) {
			case("music"):
				mSelectionClause = MediaStore.Audio.Media.IS_MUSIC +" = 1";
				break;
			case("alarm"):
				mUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
				mSelectionClause = MediaStore.Audio.Media.IS_ALARM +" = 1";
				break;
			case("notification"):
				mUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
				mSelectionClause = MediaStore.Audio.Media.IS_NOTIFICATION +" = 1";
				break;
			case("ringtone"):
				mUri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
				mSelectionClause = MediaStore.Audio.Media.IS_RINGTONE +" = 1";
				break;
		}

		// Get a Cursor object from the content URI
		Cursor songs = getContentResolver().query(
			mUri,
			mProjection,
			mSelectionClause,
			mSelectionArgs,
			mSortOrder
		);

		// Use the cursor to loop through the results and add them to 
		//		the songsList as SongObjects
		while (songs.moveToNext()) {
			Log.e("Adding to songsList: ", songs.getString(songs.getColumnIndex(MediaStore.Audio.Media.TITLE)));

			Drawable albumCover = null;
			if(preferences.getBoolean(resources.getString(R.string.mp_album_artwork_pref), true) == true) {
				//Get album artwork
				Cursor album_artwork = getContentResolver().query(
						MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
						new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
						MediaStore.Audio.Media._ID + " = ?",
						new String[]{String.valueOf(songs.getInt(songs.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)))},
						null
				);

				if(album_artwork.moveToFirst()) {
					String albumArt = album_artwork.getString(album_artwork.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
					albumCover = Drawable.createFromPath(albumArt);
				}
			}

			songsList.add(
					new SongObject(
							songs.getString(songs.getColumnIndex(MediaStore.Audio.Media.TITLE)),
							songs.getString(songs.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
							albumCover,
							songs.getString(songs.getColumnIndex(MediaStore.Images.Media.DATA))
					)
			);
		}



		boolean shuffle_prefs = preferences.getBoolean(resources.getString(R.string.mp_shuffle_pref), false);
		if(shuffle_prefs == true) {
			Collections.shuffle(songsList);
		}
	}

	/**
	 * Get song list for display in ListView
	 * @return list of Songs 
	 */
	public static ArrayList<SongObject> getSongsList(){
		return songsList;
	}

}
