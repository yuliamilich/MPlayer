package com.yulia.milich.mplayer;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class MusicService  extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {


    private static MediaPlayer player; //media player
    private ArrayList<Song> valuesList; //songs List
    private int songPosn;  //current position
    private int positionPausedInSong; // position in the paused song
    private final IBinder musicBind = new MusicBinder();
    private boolean isStopped; //state of the player
    private Intent playIntent;
    public static boolean prepered = false;
//    private MusicList m;

    @Override
    public void onCreate() {
        //create music Service

        super.onCreate();
        songPosn = 0;
        player = new MediaPlayer();
        valuesList = new ArrayList<>();

        //default music
        player = MediaPlayer.create(this, R.raw.themetune);
        player.setLooping(true);
        player.start();

        initMusicPlayer();

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);

        //getSongs();
//        m = new MusicList();
//        valuesList = m.getSongList();

    }

    public void pauseMusic() {
        player.pause();
        //stopService(new Intent(this));
        isStopped = true;
        positionPausedInSong = player.getCurrentPosition();
    }

    public int getCurrentPosition(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public void getSongs() {
        //enter to list of songs from phone storege
        ContentResolver cr = getContentResolver();       //--allows access to the the phone
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;      //--songUri is the address to the music files in the phone
        Cursor songs = cr.query(songUri, null, null, null, null);
        if (songs != null && songs.moveToFirst()) {
            int songTitle = songs.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songID = songs.getColumnIndex(MediaStore.Audio.Media._ID);
            int songDateAdded = songs.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
            int songData = songs.getColumnIndex(MediaStore.Audio.Media.DATA);

            Song song;

            while (songs.moveToNext()) {
                long longSongID = songs.getLong(songID);
                String currentTitle = songs.getString(songTitle);
                String DateAdded = songs.getString(songDateAdded);
                String data = songs.getString(songData);
                song = new Song(longSongID, currentTitle, DateAdded, data);
                valuesList.add(song);
            }
        }
    }


    public void setSongPosn(int pos) {
        this.songPosn = pos;
    }

    public void seekTo(int position){
        player.seekTo(position);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return false;
    }

    public static void stopPlayMusic() { //stop the music
        player.stop();
        player.release();
    }

    public ArrayList<Song> getValuesList() {
        return this.valuesList;
    }

    public void initMusicPlayer() {
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        isStopped = false;
    }

    public void playSong(ArrayList <Song> songList) {
        // play song from the list
        if (player != null) //אם נוצר כבר
            player.reset();

        Song songToPlay = songList.get(songPosn);
        long songId = songToPlay.getId();

        Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                songId);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }
        player.prepareAsync();

        player.setOnPreparedListener((new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {

                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        }));
        isStopped = false;

    }

    public void setList(ArrayList<Song> theSongs) {
        valuesList = theSongs;
    }

    public class MusicBinder extends Binder implements IBinder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public void setSong(int songIndex) {
        songPosn = songIndex;
    }

    public void pause() {
        if (player != null)
            player.pause();
    }

    public boolean isPlaying() {
        // is music player playing. return true if it is else false
        return player.isPlaying();
    }

    public void release(){
        player.release();
    }

    public void resume() {
        if (player != null)
            player.start();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // TODO Auto-generated method stub
        mp.start();
        prepered = true;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // TODO Auto-generated method stub
        return false;
    }

}
