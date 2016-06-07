package me.nereo.multi_media_selector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import me.nereo.multi_media_selector.bean.MediaItem;

/**
 * 多图选择
 * Created by Nereo on 2015/4/7.
 * Updated by nereo on 2016/1/19.
 */
public class MultiMediaSelectorActivity extends AppCompatActivity implements MultiMediaSelectorFragment.Callback {
    private ArrayList<MediaItem> resultList = new ArrayList<>();
    private TextView mSubmitButton;
    private MediaOptions mMediaOptions;


    public static void open(Activity activity, int requestCode,
                            MediaOptions options) {
        Intent intent = new Intent(activity, MultiMediaSelectorActivity.class);
        intent.putExtra(MultiMediaSelectorFragment.EXTRA_MEDIA_OPTIONS, options);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.NO_ACTIONBAR);
        setContentView(me.nereo.multi_media_selector.R.layout.activity_default);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null){
            setSupportActionBar(toolbar);
        }

        if (savedInstanceState != null) {
            mMediaOptions = savedInstanceState
                    .getParcelable(MultiMediaSelectorFragment.EXTRA_MEDIA_OPTIONS);
//            mPhotoFileCapture = (File) savedInstanceState
//                    .getSerializable(KEY_PHOTOFILE_CAPTURE);
        } else {
            mMediaOptions = getIntent().getParcelableExtra(MultiMediaSelectorFragment.EXTRA_MEDIA_OPTIONS);
//            if (mMediaOptions == null) {
//                throw new IllegalArgumentException(
//                        "MediaOptions must be not null, you should use MediaPickerActivity.open(Activity activity, int requestCode,MediaOptions options) method instead.");
//            }
        }

        Intent intent = getIntent();
        int mediaListModel = mMediaOptions.getmMediaType();
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mediaListModel == MultiMediaSelectorFragment.LIST_IMAGE?R.string.image_picker: mediaListModel == MultiMediaSelectorFragment.LIST_VIDEO ? R.string.video_picker : R.string.picker);

        }

        if (mMediaOptions.getmMode() == MultiMediaSelectorFragment.MODE_MULTI && mMediaOptions.getMediaListSelected().size()>0) {
            resultList = mMediaOptions.getMediaListSelected();
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable(MultiMediaSelectorFragment.EXTRA_MEDIA_OPTIONS, mMediaOptions);
        getSupportFragmentManager().beginTransaction()
                .add(me.nereo.multi_media_selector.R.id.image_grid, Fragment.instantiate(this, MultiMediaSelectorFragment.class.getName(), bundle))
                .commit();

        // 完成按钮
        mSubmitButton = (TextView) findViewById(me.nereo.multi_media_selector.R.id.commit);
        if (mMediaOptions.getmMode() == MultiMediaSelectorFragment.MODE_MULTI) {
            if (resultList == null || resultList.size() <= 0) {
                mSubmitButton.setText(me.nereo.multi_media_selector.R.string.action_done);
                mSubmitButton.setEnabled(false);
            } else {
                updateDoneText();
                mSubmitButton.setEnabled(true);
            }
            mSubmitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (resultList != null && resultList.size() > 0) {
                        // 返回已选择的图片数据
                        Intent data = new Intent();
                        data.putParcelableArrayListExtra(MultiMediaSelectorFragment.EXTRA_RESULT, resultList);
                        setResult(RESULT_OK, data);
                        finish();
                    }
                }
            });
        } else {
            mSubmitButton.setVisibility(View.GONE);
        }

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MultiMediaSelectorFragment.EXTRA_MEDIA_OPTIONS, mMediaOptions);
//        outState.putSerializable(KEY_PHOTOFILE_CAPTURE, mPhotoFileCapture);
    }






    /**
     * Update done button by select image data
     */
    private void updateDoneText(){
        int size = 0;
        if(resultList == null || resultList.size()<=0){
            mSubmitButton.setText(R.string.action_done);
            mSubmitButton.setEnabled(false);
        }else{
            size = resultList.size();
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setText(getString(R.string.action_button_string,
                getString(R.string.action_done), size, mMediaOptions.getmMaxCount()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSingleImageSelected(MediaItem path) {
        Intent data = new Intent();
        resultList.add(path);
        data.putParcelableArrayListExtra(MultiMediaSelectorFragment.EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onImageSelected(MediaItem item) {
        if (!resultList.contains(item)) {
            resultList.add(item);
        }
        // 有图片之后，改变按钮状态
        if (resultList.size() > 0) {
            updateDoneText();
            if (!mSubmitButton.isEnabled()) {
                mSubmitButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onImageUnselected(MediaItem item) {
        if (resultList.contains(item)) {
            resultList.remove(item);
        }
        updateDoneText();
        // 当为选择图片时候的状态
        if (resultList.size() == 0) {
            mSubmitButton.setText(me.nereo.multi_media_selector.R.string.action_done);
            mSubmitButton.setEnabled(false);
        }
    }

    @Override
    public void onCameraShot(MediaItem item) {
        if (item != null) {
            // notify system
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(item.getPath()))));
            Intent data = new Intent();
            resultList.add(item);
            data.putParcelableArrayListExtra(MultiMediaSelectorFragment.EXTRA_RESULT, resultList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public void onRecordShot(MediaItem item) {
        Intent data = new Intent();
        resultList.add(item);
        data.putParcelableArrayListExtra(MultiMediaSelectorFragment.EXTRA_RESULT, resultList);
        setResult(RESULT_OK, data);
        finish();
    }
}
