package com.yulia.milich.mplayer;

public class Song {
    private long ID;
    private String title;
    private String DateAdded;
    private String path;

    public Song(long ID, String title, String DateAdded, String path)
    {
        this.ID = ID;
        this.title = title;
        this.DateAdded = DateAdded;
        this.path = path;
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

    public String getPath() {
        return path;
    }

    public String toSring()
    {
        return (this.ID + ", "+this.title);
    }
}
