package me.nereo.multi_media_selector;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 播放视频页面
 *
 * @author Martin
 */
public class VideoFragment extends Fragment implements View.OnClickListener {

    private ScalableVideoView mScalableVideoView;
    private ImageView mPlayImageView;
    private ImageView mThumbnailImageView;
    String path;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.activity_play_video, null);
        initArgs();
        initView(view);
        return view;
    }

    private void initView(View view) {
        mScalableVideoView = (ScalableVideoView)view. findViewById(R.id.video_view);
        try {
            // 这个调用是为了初始化mediaplayer并让它能及时和surface绑定
            mScalableVideoView.setDataSource("");
        } catch (IOException e) {
            e.printStackTrace();
        }

        mPlayImageView = (ImageView) view.findViewById(R.id.playImageView);
        mThumbnailImageView = (ImageView)view. findViewById(R.id.thumbnailImageView);
        mThumbnailImageView.setImageBitmap(getVideoThumbnail(path));
        mScalableVideoView.setOnClickListener(this);
        mPlayImageView.setOnClickListener(this);

        if (TextUtils.isEmpty(path)) {
            Toast.makeText(getActivity(), "视频路径错误", Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
            return;
        }
        mHasLoadedOnce=true;
    }

    private void initArgs() {
        Bundle bundle = getArguments();
        path = bundle.getString("path");
    }

    /**
     * 获取视频缩略图（这里获取第一帧）
     *
     * @param filePath
     * @return
     */
    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(TimeUnit.MILLISECONDS.toMicros(1));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    private boolean mHasLoadedOnce = false;

    //失去焦点暂停视频
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isVisibleToUser && mHasLoadedOnce) {
               stopPlay();
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setUserVisibleHint(true);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.video_view) {
            stopPlay();
        } else if (i == R.id.playImageView) {
            startPlay();

        }
    }

    private void stopPlay() {
        mScalableVideoView.stop();
        mPlayImageView.setVisibility(View.VISIBLE);
        mThumbnailImageView.setVisibility(View.VISIBLE);
    }

    private void startPlay() {
        try {
            mScalableVideoView.setDataSource(path);
            mScalableVideoView.setLooping(true);
            mScalableVideoView.prepare();
            mScalableVideoView.start();
            mPlayImageView.setVisibility(View.GONE);
            mThumbnailImageView.setVisibility(View.GONE);
        } catch (IOException e) {
            Toast.makeText(getActivity(), "播放视频异常", Toast.LENGTH_SHORT).show();
        }
    }

}
