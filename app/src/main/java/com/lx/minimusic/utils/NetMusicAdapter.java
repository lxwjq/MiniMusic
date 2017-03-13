package com.lx.minimusic.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lx.minimusic.R;
import com.lx.minimusic.bean.SeachResult;

import java.util.ArrayList;

/**
 * Created by 李祥 on 2017/3/9.
 */

public class NetMusicAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<SeachResult> mSeachResults;

    public NetMusicAdapter(Context context, ArrayList<SeachResult> seachResults) {
        mContext = context;
        mSeachResults = seachResults;
    }

    public ArrayList<SeachResult> getSeachResults() {
        return mSeachResults;
    }

    public void setSeachResults(ArrayList<SeachResult> seachResults) {
        mSeachResults = seachResults;
    }

    @Override
    public int getCount() {
        return mSeachResults.size();
    }

    @Override
    public SeachResult getItem(int position) {
        return mSeachResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHodler hodler = null;
        if(convertView == null){
            hodler = new ViewHodler();

            convertView = View.inflate(mContext , R.layout.list_item_net_music , null);
            hodler.tv_net_singer = (TextView) convertView.findViewById(R.id.tv_net_singer);
            hodler.tv_net_title = (TextView) convertView.findViewById(R.id.tv_net_title);

            convertView.setTag(hodler);
        }else{
            hodler = (ViewHodler) convertView.getTag();
        }

        hodler.tv_net_singer.setText(getItem(position).getArtist());
        hodler.tv_net_title.setText(getItem(position).getMusicName());
        return convertView;
    }

    static class ViewHodler {
        public TextView tv_net_title;
        public TextView tv_net_singer;
    }
}
