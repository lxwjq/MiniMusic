package com.lx.minimusic.bean;

/**
 * Created by 李祥 on 2017/3/1.
 * 音乐实体类
 */
public class Mp3Info {
    public long id;
    public long mp3InfoId;  //在收藏是用与保存原有的id
    public long playTime;   //最近播放的时间
    public int isLike;   //是否 为收藏  0为默认  1为收藏
    public String title;  //歌名
    public String artist;  //艺术家
    public String album;   //专辑
    public long albumId;
    public String duration;   //时长
    public long size;   //大小
    public String url;   //路径
    public int isMusic;   //是否存在音乐
    public int duration_copy;

    public long getMp3InfoId() {
        return mp3InfoId;
    }

    public long getPlayTime() {
        return playTime;
    }

    public void setPlayTime(long playTime) {
        this.playTime = playTime;
    }

    public int getIsLike() {
        return isLike;
    }

    public void setIsLike(int isLike) {
        this.isLike = isLike;
    }

    public void setMp3InfoId(long mp3InfoId) {
        this.mp3InfoId = mp3InfoId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIsMusic() {
        return isMusic;
    }

    public void setIsMusic(int isMusic) {
        this.isMusic = isMusic;
    }

    @Override
    public String toString() {
        return "Mp3Info{" +
                "id=" + id +
                ", mp3InfoId=" + mp3InfoId +
                ", playTime=" + playTime +
                ", isLike=" + isLike +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", albumId=" + albumId +
                ", duration='" + duration + '\'' +
                ", size=" + size +
                ", url='" + url + '\'' +
                ", isMusic=" + isMusic +
                ", duration_copy=" + duration_copy +
                '}';
    }
}
