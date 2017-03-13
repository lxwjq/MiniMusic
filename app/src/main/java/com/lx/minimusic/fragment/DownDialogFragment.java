package com.lx.minimusic.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.lx.minimusic.MainActivity;
import com.lx.minimusic.bean.SeachResult;
import com.lx.minimusic.utils.DownLoadUtils;
import com.lx.minimusic.utils.ToastUtils;

import java.io.File;

/**
 * Created by 李祥 on 2017/3/12.
 */

public class DownDialogFragment extends DialogFragment {
    private SeachResult mSeachResult;
    private MainActivity mContext;
    private String[] items;

    public static DownDialogFragment newInstance(SeachResult seachResult) {
        DownDialogFragment downDialogFragment = new DownDialogFragment();
        downDialogFragment.mSeachResult = seachResult;
        return downDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (MainActivity) getActivity();
        items = new String[]{"下载", "取消"};
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //创建对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        //下载
                        downLoadMusic();
                        break;

                    case 1:
                        dialog.dismiss();
                        break;
                }
            }
        });
        return builder.show();
    }

    /**
     * 下载音乐
     */
    private void downLoadMusic() {
        ToastUtils.showToast(mContext, "正在下载:" + mSeachResult.getMusicName());

        DownLoadUtils.getInstance().setListener(new DownLoadUtils.onDownloadListener() {
            @Override
            public void onDownload(String mp3Url) {
                ToastUtils.showToast(mContext, "下载成功");
                Uri contentUri = Uri.fromFile(new File(mp3Url));

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
                getContext().sendBroadcast(mediaScanIntent);
            }

            @Override
            public void onFailed(String error) {
                ToastUtils.showToast(mContext, error);
            }
        }).download(mSeachResult);
    }
}
