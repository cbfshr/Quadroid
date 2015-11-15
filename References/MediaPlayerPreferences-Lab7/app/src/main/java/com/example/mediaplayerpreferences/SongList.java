package com.example.mediaplayerpreferences;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SongList extends ListActivity {
	
	// Songs list
	public ArrayList<SongObject> songsList = new ArrayList<SongObject>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.song_list);

		// get all songs from MainActivity's query
		this.songsList = MyMediaPlayerActivity.getSongsList();

		// Adding song_list_items to ListView
		ListAdapter adapter = new SongListAdapter(this, R.layout.song_list_item, this.songsList);

		setListAdapter(adapter);

		ListView lv = getListView();
		// listening to single song_list_item click
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// getting song_list_item index
				int songIndex = position;
				
				// Starting new intent
				Intent intent = new Intent(getApplicationContext(), MyMediaPlayerActivity.class);
				
				// Sending songIndex to PlayerActivity
				intent.putExtra("songIndex", songIndex);
				setResult(RESULT_OK, intent);
				
				// Closing SongListView
				finish();
			}
		});

	}
}

