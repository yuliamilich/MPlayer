package com.yulia.milich.mplayer;

public class Song {
    private long ID;
    private  String title;

    public Song(long ID, String title)
    {
        this.ID = ID;
        this.title = title;
    }

    public long getId()
    {return this.ID;}

    public String getTitle()
    {
        return this.title;
    }

    public String toSring()
    {
        return (this.ID + ", "+this.title);
    }
}
