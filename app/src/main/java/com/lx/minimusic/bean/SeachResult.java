package com.lx.minimusic.bean;

/**
 * Created by 李祥 on 2017/3/8.
 * 搜索音乐对象的实体类
 */
public class SeachResult {

    private String musicName;  //歌名
    private String url;   // 地址
    private String artist;   //歌手
    private String album;  //专辑

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    @Override
    public String toString() {
        return "SeachResult{" +
                "musicName='" + musicName + '\'' +
                ", url='" + url + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                '}';
    }
}
