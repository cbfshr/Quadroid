package com.example.mediaplayerpreferences;

import android.graphics.drawable.Drawable;

/**
 * This object represents a song in the form of the title and the file path for the audio file.
 */
public class SongObject {

	/**
	 * The title of the audio file
	 */
	private String title;
	/**
	 * The artist name of the audio file
	 */
	private String artistName;
	
	/**
	 * The file path of the audio file 
	 */
	private String filePath;

	private Drawable albumCover;

	public SongObject(String title, String artistName, Drawable albumCover, String filePath){
		super();
		this.title = title;
		this.artistName = artistName;
		this.albumCover = albumCover;
		this.filePath = filePath;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) { this.title = title; }

	public String getArtistName() {
		return artistName;
	}

	public Drawable getAlbumCover() {
		return albumCover;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
}
