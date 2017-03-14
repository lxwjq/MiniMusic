package com.lx.minimusic.utils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.lx.minimusic.bean.SeachResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by 李祥 on 2017/3/9.
 */

public class SearchMusicUtils {
    private static final int SIZE = 20;  //一次只查询20条
    private static final String URL = Constant.BAIDU_SERVICE + Constant.BAIDU_SEARCH;

    private static SearchMusicUtils sInstance;

    private ExecutorService mThreadPool;  //定义线程池

    private OnSearchResultListener mListener;

    public synchronized static SearchMusicUtils getInstance() {
        if (sInstance == null) {
            sInstance = new SearchMusicUtils();
        }
        return sInstance;
    }

    private SearchMusicUtils() {
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    public SearchMusicUtils setListener(OnSearchResultListener l) {
        mListener = l;
        return this;
    }

    public void search(final String key, final int page) {
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case Constant.SUCCESS:
                        if (mListener != null) {
                            mListener.onSearchResult((ArrayList<SeachResult>) msg.obj);
                        }
                        break;

                    case Constant.FAILED:
                        if (mListener != null) {
                            mListener.onSearchResult(null);
                        }
                        break;
                }
            }
        };

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<SeachResult> results = getMusicList(key, page);

                if (results == null) {
                    //失败
                    handler.sendEmptyMessage(Constant.FAILED);
                    return;
                }

                handler.obtainMessage(Constant.SUCCESS, results).sendToTarget();
            }
        });
    }

    /**
     * 使用Jsoup请求网络  解析数据
     *
     * @param key
     * @param page
     * @return
     */
    private ArrayList<SeachResult> getMusicList(final String key, final int page) {
        final String start = String.valueOf((page - 1) * SIZE);

        try {
            Document doc = Jsoup.connect(URL).
                    data("key", key, "start", start).
                    userAgent(Constant.USER_AGENT).
                    timeout(60 * 1000).get();

            Elements songTitle = doc.select("div.song-item.clearfix");

            Elements songInfos;
            ArrayList<SeachResult> seachResults = new ArrayList<SeachResult>();


            for (int i = 0; i < songTitle.size(); i++) {
                Element song;
                song = songTitle.get(i);
                songInfos = song.getElementsByTag("a");
                SeachResult seachResult = new SeachResult();

                for (Element info : songInfos) {
                    //收费歌曲
                    if (info.attr("href").startsWith("http://y.baidu.com/song/")) {
                        continue;
                    }

                    //跳转到百度音乐和的音乐
                    if (info.attr("href").equals("#") && !TextUtils.isEmpty(info.attr("data-songdata"))) {
                        continue;
                    }

                    //歌曲链接
                    if (info.attr("href").startsWith("/song")) {
                        seachResult.setMusicName(info.text());
                        seachResult.setUrl(info.attr("href"));
                    }

                    //歌手链接
                    if (info.attr("href").startsWith("/data")) {
                        seachResult.setArtist(info.text());
                    }

                    //专辑链接
                    if (info.attr("href").startsWith("/alum")) {
                        seachResult.setAlbum(info.text().replaceAll("《|》", ""));
                    }
                }

                seachResults.add(seachResult);

            }
            return seachResults;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public interface OnSearchResultListener {
        public void onSearchResult(List<SeachResult> seachResults);

    }
}
