package com.lx.minimusic.fragment;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.lx.minimusic.MainActivity;
import com.lx.minimusic.R;
import com.lx.minimusic.bean.SeachResult;
import com.lx.minimusic.utils.AppUtils;
import com.lx.minimusic.utils.Constant;
import com.lx.minimusic.utils.NetMusicAdapter;
import com.lx.minimusic.utils.SearchMusicUtils;
import com.lx.minimusic.utils.ToastUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 李祥 on 2017/3/1.
 */

public class NetMusicFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener {

    private MainActivity mContext;
    private LinearLayout mLlNetEdSeach;
    private LinearLayout mLlNetTvSeach;
    private EditText mEdSeach;
    private ImageButton mIbnSeacher;
    private ProgressBar mPbLoad;
    private LinearLayout mLlLoad;
    private ListView mLvNetList;
    private ArrayList<SeachResult> mSeachResults;

    private NetMusicAdapter mNetMusicAdapter;
    private int page = 1; // 搜索音乐的页码

    /**
     * @return 返回一个NetMusicFragment对象
     */
    public static NetMusicFragment newInstance() {
        NetMusicFragment net = new NetMusicFragment();
        return net;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View netView = inflater.inflate(R.layout.net_music_list, null);
        initUI(netView);
        initEvent();
        initData();

        return netView;
    }

    /**
     * 初始化UI
     */
    private void initUI(View view) {
        mLlNetEdSeach = (LinearLayout) view.findViewById(R.id.ll_net_ed_seach);
        mLlNetTvSeach = (LinearLayout) view.findViewById(R.id.ll_net_tv_seach);
        mEdSeach = (EditText) view.findViewById(R.id.ed_seach);
        mIbnSeacher = (ImageButton) view.findViewById(R.id.ibn_seach);
        mPbLoad = (ProgressBar) view.findViewById(R.id.pb_load);
        mLlLoad = (LinearLayout) view.findViewById(R.id.ll_load);
        mLvNetList = (ListView) view.findViewById(R.id.lv_net_list);
    }

    /**
     * 初始化数据
     */
    private void initData() {

        mSeachResults = new ArrayList<SeachResult>();
        mPbLoad.setVisibility(View.VISIBLE);

        new LoadNetDataTask().execute(Constant.BAIDU_SERVICE + Constant.BAIDU_TOP);
    }

    /**
     * 注册点击事件
     */
    private void initEvent() {
        mLlNetTvSeach.setOnClickListener(this);
        mLlNetEdSeach.setOnClickListener(this);
        mIbnSeacher.setOnClickListener(this);
        mLvNetList.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position >= mSeachResults.size() || position < 0) {
            return;
        }
        showDownloadDialog(position);
    }

    private void showDownloadDialog(int position) {
        DownDialogFragment downDialogFragment = DownDialogFragment.newInstance(mSeachResults.get(position));
        downDialogFragment.show(getFragmentManager(), "download");
    }

    class LoadNetDataTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLlLoad.setVisibility(View.VISIBLE);
            mLvNetList.setVisibility(View.GONE);

            mSeachResults.clear();  //清空已有的数据
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try {
                Document doc = Jsoup.connect(url).userAgent(Constant.USER_AGENT).timeout(6 * 1000).get();

                Elements songTitle = doc.select("span.song-title");
                Elements artists = doc.select("span.author_list");

                for (int i = 0; i < songTitle.size() - 1; i++) {
                    SeachResult seachResult = new SeachResult();
                    Elements urls = songTitle.get(i).getElementsByTag("a");
                    seachResult.setUrl(urls.get(0).attr("href"));
                    seachResult.setMusicName(urls.get(0).text());

                    Elements artistsElements = artists.get(i).getElementsByTag("a");
                    seachResult.setArtist(artistsElements.get(0).text());

                    seachResult.setAlbum("热歌榜");
                    mSeachResults.add(seachResult);
                }


            } catch (Exception e) {
                e.printStackTrace();

                return -1;
            }

            return 1;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

            if (integer == 1) {
                mLlLoad.setVisibility(View.GONE);
                mLvNetList.setVisibility(View.VISIBLE);
                //ListView填充数据
                mNetMusicAdapter = new NetMusicAdapter(mContext, mSeachResults);
                mLvNetList.setAdapter(mNetMusicAdapter);
            }
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_net_tv_seach:
                mLlNetTvSeach.setVisibility(View.GONE);
                mLlNetEdSeach.setVisibility(View.VISIBLE);
                break;

            case R.id.ll_net_ed_seach:
                break;

            case R.id.ibn_seach:
                search();
                break;
        }
    }

    /**
     * 搜索歌曲
     */
    private void search() {
        AppUtils.hideInputMethod(mEdSeach);  //隐藏输入法

        mLlNetTvSeach.setVisibility(View.VISIBLE);
        mLlNetEdSeach.setVisibility(View.GONE);

        String seachName = mEdSeach.getText().toString().trim();

        if (!TextUtils.isEmpty(seachName)) {
            mLlLoad.setVisibility(View.VISIBLE);

        } else {
            ToastUtils.showToast(mContext, "请输入歌名");
            return;
        }

        mLlLoad.setVisibility(View.VISIBLE);

        SearchMusicUtils.getInstance().setListener(new SearchMusicUtils.OnSearchResultListener() {
            @Override
            public void onSearchResult(List<SeachResult> seachResults) {
                ArrayList<SeachResult> sr = mNetMusicAdapter.getSeachResults();

                sr.clear();
                sr.addAll(seachResults);
                mNetMusicAdapter.notifyDataSetChanged();
                mLlLoad.setVisibility(View.GONE);
            }
        }).search(seachName, page);
    }
}
