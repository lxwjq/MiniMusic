package com.lx.minimusic.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.lx.minimusic.bean.SeachResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by 李祥 on 2017/3/12.
 */

public class DownLoadUtils {

    public static final String DOWNLOAD_URL = "/download?__o=%2Fsearch%2Fsong";
    public static final int SUCCESS_LRC = 1;  //下载歌词成功
    public static final int FAILED_LRC = 2;  //下载歌词失败
    private static final int SUCCESS_MP3 = 3;//下载歌曲成功
    private static final int FAILED_MP3 = 4;//下载歌曲失败

    private static final int GET_MP3_URL = 5;  //获取mp3的url成功
    private static final int GET_FAILED_MP3_URL = 6; //获取mp3的url失败
    private static final int MUSIC_EXISTS = 7;  //音乐已经存在


    private static DownLoadUtils sInstance;
    private onDownloadListener mOnDownloadListener;
    private ExecutorService mThreadPool;

    /**
     * 设置回调的监听对象
     *
     * @param listener
     * @return
     */
    public DownLoadUtils setListener(onDownloadListener listener) {
        mOnDownloadListener = listener;
        return this;
    }

    /**
     * 单例模式获取DownLoadUtils对象
     *
     * @return 单一的DownLoadUtils对象
     */
    public synchronized static DownLoadUtils getInstance() {
        if (sInstance == null) {
            sInstance = new DownLoadUtils();
        }
        return sInstance;
    }

    /**
     * 执行下载操作业务操作
     *
     * @param seachResult 下载的对象
     */
    public void download(final SeachResult seachResult) {
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch (msg.what) {
                    case SUCCESS_LRC:
                        if (mOnDownloadListener != null) {
                            mOnDownloadListener.onDownload("歌词下载成功");
                        }
                        break;
                    case FAILED_LRC:
                        if (mOnDownloadListener != null) {
                            mOnDownloadListener.onDownload("歌词下载失败");
                        }
                        break;
                    case GET_MP3_URL:
                        downloadMusic(seachResult, (String) msg.obj, this);
                        break;
                    case GET_FAILED_MP3_URL:
                        if (mOnDownloadListener != null) {
                            mOnDownloadListener.onFailed("下载失败，该歌曲是收费类型");
                        }
                        break;
                    case SUCCESS_MP3:
                        if (mOnDownloadListener != null) {
                            mOnDownloadListener.onDownload(seachResult.getMusicName() + "已下载成功");
                        }

                        String url = Constant.BAIDU_SERVICE + seachResult.getUrl();

                        break;

                    case FAILED_MP3:
                        if (mOnDownloadListener != null) {
                            mOnDownloadListener.onFailed(seachResult.getMusicName() + "下载失败");
                        }
                        break;
                    case MUSIC_EXISTS:
                        if (mOnDownloadListener != null) {
                            mOnDownloadListener.onFailed("音乐已存在");
                        }
                        break;
                }
            }
        };

        getDownloadMusicURL(seachResult, handler);
    }

    /**
     * 下载mp3的具体方法
     *
     * @param seachResult
     * @param url
     * @param handler
     */
    private void downloadMusic(final SeachResult seachResult, final String url, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_MUSIC);
                if (!musicDirFile.exists()) {
                    musicDirFile.mkdirs();
                }

                String mp3Url = Constant.BAIDU_SERVICE + url;
                String target = musicDirFile + "/" + seachResult.getMusicName() + ".mp3";

                File fileTaget = new File(target);

                if (!fileTaget.exists()) {
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                    return;
                } else {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(mp3Url).build();

                    try {
                        Response response = client.newCall(request).execute();

                        if (response.isSuccessful()) {
                            PrintStream ps = new PrintStream(fileTaget);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_MP3
                        ).sendToTarget();
                    }

                }
            }
        });
    }

    /**
     * 获取歌曲的url地址
     *
     * @param seachResult 歌曲
     * @param handler
     */
    private void getDownloadMusicURL(final SeachResult seachResult, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //获取歌曲的url
                    String url = Constant.BAIDU_SERVICE
                            + "/song/" + seachResult.getUrl().
                            substring(seachResult.getUrl().lastIndexOf("/") + 1) + DOWNLOAD_URL;


                    Document document = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();

                    Elements targetElements = document.select("a[data-btndata]");

                    if (targetElements.size() <= 0) {

                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                        return;
                    }
                    for (Element e : targetElements) {
                        if (e.attr("href").contains(".mp3")) {
                            String result = e.attr("href");
                            Message message = handler.obtainMessage(GET_MP3_URL, result);

                            message.sendToTarget();
                            return;
                        }

                        if (e.attr("href").startsWith("/vip")) {
                            targetElements.remove();
                        }
                    }

                    if (targetElements.size() <= 0) {
                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                        return;
                    }
                    String result = targetElements.get(0).attr("href");
                    Message msg = handler.obtainMessage(GET_MP3_URL, result);
                    msg.sendToTarget();

                } catch (Exception e) {
                    e.printStackTrace();
                    //获取url失败
                    handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                }
            }
        });
    }

    /**
     * 下载歌词的具体方法
     *
     * @param url
     * @param musicName 歌曲名字
     * @param handle
     */
    public void downloadLRC(final String url, final String musicName, final Handler handle) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6000).get();
                    Elements lrcTag = doc.select("div.lyric-content");
                    String lrcURL = lrcTag.attr("data-lrclink");
                    File lrcDriFile = new File(Environment.getExternalStorageDirectory() + Constant.DIR_LRC);

                    if (!lrcDriFile.exists()) {
                        lrcDriFile.mkdirs();
                    }


                    String target = lrcDriFile + "/" + musicName + ".lrc";

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(lrcURL).build();

                    Response response = client.newCall(request).execute();

                    if (response.isSuccessful()) {
                        PrintStream ps = new PrintStream(new File(target));
                        byte[] bytes = response.body().bytes();
                        ps.write(bytes, 0, bytes.length);
                        ps.close();
                        handle.obtainMessage(SUCCESS_LRC, target).sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handle.obtainMessage(FAILED_LRC).sendToTarget();
                }
            }
        });
    }


    private DownLoadUtils() {
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    /**
     * 自定义下载事件的监听器
     */
    public interface onDownloadListener {
        /**
         * 下载
         *
         * @param mp3Url 下载mp3的url
         */
        public void onDownload(String mp3Url);

        /**
         * 下载失败
         *
         * @param error
         */
        public void onFailed(String error);
    }

}
