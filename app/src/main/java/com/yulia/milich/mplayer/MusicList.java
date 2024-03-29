package com.yulia.milich.mplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicList extends AppCompatActivity {

    private ListView lvSongs;
    private ArrayList<Song> songList;
    private ArrayList<String> songsNames;
    private ArrayList<String> directories;
    private ArrayList<String> directoriesPath;
    private ArrayAdapter adapter;
    private TextView currentSong;
    private ImageButton pause;
    private SeekBar mseekBar;
    private Handler handler, handler2;
    private Runnable runnable, runnable2;
    public static final int mPerm = 1; //for permition request

    public static MusicService musicService; // music player
    private Intent playIntent;
    public static boolean isPlaying = true; // true if music is working else false
    private boolean musicBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        handler = new Handler();
        handler2 = new Handler();

        musicService = new MusicService();

        musicBound = false;

        if(playIntent == null)
        {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection,
                    Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        directories = new ArrayList<>();
        directoriesPath = new ArrayList<>();

        currentSong = (TextView) findViewById(R.id.currentSong);
        pause = (ImageButton) findViewById(R.id.pause);
        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicList.isPlaying) {
                    MusicList.musicService.pause();
                    pause.setImageResource(android.R.drawable.ic_media_pause);
                }
                else {
                    MusicList.musicService.resume();
                    pause.setImageResource(android.R.drawable.ic_media_play);
                }
                MusicList.isPlaying = !MusicList.isPlaying;
                playCycle();
            }
        });

        songsNames = new ArrayList<String>();
        lvSongs = (ListView) findViewById(R.id.lvSongs);
        songList = new ArrayList<Song>();

        getDirSongs("null");
        listSongs();

        mseekBar = (SeekBar) findViewById(R.id.seekBar);

        try{
            mseekBar.setMax(musicService.getDuration());
            playCycle();
        }
        catch (Exception e){
            untilPrepered();
        }

        mseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //do something when the seekbar is changed

                if (fromUser){
                    musicService.seekTo(progress);
                    mseekBar.setMax(musicService.getDuration());
                    playCycle();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
    });

    }

    public void untilPrepered(){
        if(!musicService.prepered){
            runnable2 = new Runnable() {
                @Override
                public void run() {
                    untilPrepered();
                }
            };
            handler2.postDelayed(runnable2, 1000);
        }
        else {
            mseekBar.setMax(musicService.getDuration());
            playCycle();
        }
    }

    public void playCycle(){
        mseekBar.setProgress(musicService.getCurrentPosition());

        if(MusicList.isPlaying){
            runnable = new Runnable() {
                @Override
                public void run() {
                    playCycle();
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }



//    public void getSongs() {
//        songList.clear();
//        songsNames.clear();
//
//        ContentResolver cr = getContentResolver();       //--allows access to the the phone
//        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;      //--songUri is the address to the music files in the phone
//        Cursor songs = cr.query(songUri, null, null, null, null);
//        if (songs != null && songs.moveToFirst()) {
//            int songTitle = songs.getColumnIndex(MediaStore.Audio.Media.TITLE);
//            int songID = songs.getColumnIndex(MediaStore.Audio.Media._ID);
//            int songDateAdded = songs.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
//            int songData = songs.getColumnIndex(MediaStore.Audio.Media.DATA);
//
//            Song song;
//
//            String pathPatern = ".*(/(.*)/)";
//            Pattern pathP = Pattern.compile(pathPatern);
//
//            while (songs.moveToNext()) {
//                long longSongID = songs.getLong(songID);
//                String currentTitle = songs.getString(songTitle);
//                String DateAdded = songs.getString(songDateAdded);
//                String data = songs.getString(songData);
//                //songsNames.add(currentTitle);
//                Matcher m = pathP.matcher(data);
//                if(m.find()){
//                    songsNames.add(m.group(2));
//                    if(!directories.contains(m.group(2))){
//                        directories.add(m.group(2));
//                        directoriesPath.add(data);
//                    }
//                }
//                else{
//                    songsNames.add(currentTitle);
//                }
//                //songsNames.add(m.group(0));
//                //songsNames.add(String.valueOf(m.find()));
//                song = new Song(longSongID, currentTitle, DateAdded, data);
//                songList.add(song);
//            }
//
//        }
//    }

    public void getDirSongs(String path){
        songList.clear();
        songsNames.clear();

        ContentResolver cr = getContentResolver();       //--allows access to the the phone
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;      //--songUri is the address to the music files in the phone
        Cursor songs;
        if(path == "null"){ //check if we should look for all songs or songs in a directory
            songs = cr.query(songUri, null, null, null, null);
        }
        else {
            songs = cr.query(songUri, null, MediaStore.Audio.Media.DATA + " like ?", new String[]{"%" + path + "%"}, null);
        }
        if (songs != null && songs.moveToFirst()) {
            int songTitle = songs.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songID = songs.getColumnIndex(MediaStore.Audio.Media._ID);
            int songDateAdded = songs.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
            int songData = songs.getColumnIndex(MediaStore.Audio.Media.DATA);

            Song song;

            String pathPatern = ".*(/(.*)/)";
            Pattern pathP = Pattern.compile(pathPatern);

            while (songs.moveToNext()) {
                long longSongID = songs.getLong(songID);
                String currentTitle = songs.getString(songTitle);
                String DateAdded = songs.getString(songDateAdded);
                String data = songs.getString(songData);
                //songsNames.add(currentTitle);
                Matcher m = pathP.matcher(data);
                if(m.find()){
                    songsNames.add(currentTitle);
                    if(!directories.contains(m.group(2))){
                        directories.add(m.group(2));
                        directoriesPath.add(data);
                    }
                }
                else{
                    songsNames.add(currentTitle);
                }
                //songsNames.add(m.group(0));
                //songsNames.add(String.valueOf(m.find()));
                song = new Song(longSongID, currentTitle, DateAdded, data);
                songList.add(song);
            }

        }
    }

    private void listSongs() {
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, songsNames);
        lvSongs.setAdapter(adapter);
        lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // change playing song to the chosen song
                MusicList.musicService.setSong(i);
                MusicList.musicService.playSong(songList);
                currentSong.setText(songsNames.get(i));
                mseekBar.setProgress(0);
            }
        });
    }

    public void listDirectories(){
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, directories);
        lvSongs.setAdapter(adapter);
        lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //step into directory
                String pathPatern = "(.*)/";
                Pattern pathP = Pattern.compile(pathPatern);
                Matcher m = pathP.matcher(directoriesPath.get(i));
                if(m.find()){
                    getDirSongs(m.group(1));
                }
                listSongs();
            }
        });
    }

    public ArrayList<Song> getSongList() {
        return songList;
    }

    //menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_all, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //when selcting option in the menu
        //music --> stop/play music
        int id = item.getItemId();
        Intent intent = null;

        switch (id) {
            case R.id.music:
                if (MusicList.isPlaying)
                    MusicList.musicService.pause();
                else
                    MusicList.musicService.resume();
                MusicList.isPlaying = !MusicList.isPlaying;
                break;

            case R.id.sortD:
                listDirectories();
                break;
            case R.id.sortS:
                getDirSongs("null");
                listSongs();
                break;
//            case R.id.manu_main:
//                intent = new Intent(MusicList.this, MusicList.class);
//                startActivity(intent);
//                finish();
//                break;
//
//            case R.id.call:
//                intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + ""));
//                startActivity(intent);
//                break;
            case R.id.exit:
                finish();
                //System.exit(0);
                break;
        }
        return true;
    }


    //for music service
    @Override
    public void onResume() {
        super.onResume();
        if (MusicList.isPlaying)
            MusicList.musicService.resume();
    }

    //conect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            //get service
            musicService = binder.getService();
            // pass list
            musicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            musicBound = false;
        }
    };

    @Override
    protected void onDestroy() {
        //shut down music service
        stopService(playIntent);
        musicService.release();
        musicService = null;
        handler.removeCallbacks(runnable);
        handler2.removeCallbacks(runnable2);
        super.onDestroy();
        System.exit(0);
    }
}
