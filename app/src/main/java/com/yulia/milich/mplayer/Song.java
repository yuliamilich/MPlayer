package com.yulia.milich.mplayer;

public class Song {
    private long ID;
    private  String title;
    private String DateAdded;

    public Song(long ID, String title, String DateAdded)
    {
        this.ID = ID;
        this.title = title;
        this.DateAdded = DateAdded;
    }

    public long getId()
    {return this.ID;}

    public String getTitle()
    {
        return this.title;
    }

    public String getDateAdded()
    {
        return this.DateAdded;
    }

    public String toSring()
    {
        return (this.ID + ", "+this.title);
    }
}
