package com.yulia.milich.mplayer;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.ListView;

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
    public static final int mPrem = 1; //for premition request

    public static MusicService musicService; // music player
    private Intent playIntent;
    public static boolean isPlaying = true; // true if music is working else false
    private boolean musicBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

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

        songsNames = new ArrayList<String>();
        lvSongs = (ListView) findViewById(R.id.lvSongs);
        songList = new ArrayList<Song>();

//        if (ContextCompat.checkSelfPermission(MusicList.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(MusicList.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                ActivityCompat.requestPermissions(MusicList.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, mPrem);
//            } else {
//                ActivityCompat.requestPermissions(MusicList.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, mPrem);
//            }
//
//        } else {
//            //todo enter to the list
//        }


        getSongs();
        //getDirSongs();
        listSongs();


    }



    public void getSongs() {
        songList.clear();
        songsNames.clear();

        ContentResolver cr = getContentResolver();       //--allows access to the the phone
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;      //--songUri is the address to the music files in the phone
        Cursor songs = cr.query(songUri, null, null, null, null);
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
                    songsNames.add(m.group(2));
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

    public void getDirSongs(String path){
        songList.clear();
        songsNames.clear();

        ContentResolver cr = getContentResolver();       //--allows access to the the phone
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;      //--songUri is the address to the music files in the phone
        Cursor songs = cr.query(songUri, null, MediaStore.Audio.Media.DATA + " like ?", new String[] {"%" + path + "%"}, null);
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
                    songsNames.add(m.group(2));
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
                MusicList.musicService.playSong();
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
        musicService = null;
        super.onDestroy();
        System.exit(0);
    }
}
