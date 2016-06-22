package me.nereo.multi_media_selector;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import me.nereo.multi_media_selector.bean.MediaItem;


/**
 * modify by chenbc
 */
public class MediaDetailsActivity extends AppCompatActivity implements
        OnPageChangeListener {
    /**
     * 用于管理图片的滑动
     */
    private ViewPager mViewPager;
    /**
     * 显示当前图片的页数
     */
    private int imagePosition;
    private ArrayList<String> imageUrlArrayList;
    private Context mContext;
    private CheckBox mCheckBox;
    private TextView mSubmitButton;

    // 结果数据
    private ArrayList<String> mMediaSelectedList = new ArrayList<>();
    private int mMaxPick = 9;

    /**
     * 查看图片大图list，（position ==0 ，videoURL maybe）
     *
     * @param index
     * @param urls
     */
    public static void showMediaUrls(Fragment fragment, int index, ArrayList<String> urls, ArrayList<String> selectUrls, int requestCode) {
        Intent intent = new Intent(fragment.getActivity(), MediaDetailsActivity.class);
        intent.putExtra("index", index);
        intent.putStringArrayListExtra("media_urls", urls);
        intent.putStringArrayListExtra(MultiMediaSelectorFragment.KEY_MEDIA_SELECTED_LIST, selectUrls);
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.NO_ACTIONBAR);
        setContentView(R.layout.activity_image_details);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.BLACK);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mContext = this;
        handleIntent();
        mSubmitButton = (TextView) findViewById(me.nereo.multi_media_selector.R.id.commit);
        mCheckBox = (CheckBox) findViewById(R.id.checkbox);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);


        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);
//        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setCurrentItem(imagePosition);
        mViewPager.setOnPageChangeListener(this);
        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = mViewPager.getCurrentItem();
                String uri = imageUrlArrayList.get(pos);
                uri=uri.split(MultiMediaSelectorFragment.TYPE_SPLIT)[0];
                if (((CheckBox) v).isChecked()) {
                    if (mMediaSelectedList.size() >= mMaxPick) {
                        ((CheckBox) v).setChecked(false);
                        Toast.makeText(mContext, R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    mMediaSelectedList.add(uri);
                } else {
                    mMediaSelectedList.remove(uri);
                }

                if (mMediaSelectedList == null || mMediaSelectedList.size() <= 0) {
                    mSubmitButton.setText(me.nereo.multi_media_selector.R.string.action_done);
                    mSubmitButton.setEnabled(false);
                } else {
                    updateDoneText();
                    mSubmitButton.setEnabled(true);
                }
            }
        });
        if (mMediaSelectedList == null || mMediaSelectedList.size() <= 0) {
            mSubmitButton.setText(me.nereo.multi_media_selector.R.string.action_done);
            mSubmitButton.setEnabled(false);
        } else {
            updateDoneText();
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onResult();
            }
        });
        updateDisplay(imagePosition);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onResult();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onResult() {
        if (mMediaSelectedList != null) {
            Intent data = new Intent();
            data.putStringArrayListExtra(MultiMediaSelectorFragment.EXTRA_RESULT, mMediaSelectedList);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    /**
     * Update done button by select image data
     */
    private void updateDoneText() {
        int size = 0;
        if (mMediaSelectedList == null || mMediaSelectedList.size() <= 0) {
            mSubmitButton.setText(R.string.action_done);
            mSubmitButton.setEnabled(false);
        } else {
            size = mMediaSelectedList.size();
            mSubmitButton.setEnabled(true);
        }
        mSubmitButton.setText(getString(R.string.action_button_string,
                getString(R.string.action_done), size, mMaxPick));
    }

    private void handleIntent() {
        imagePosition = getIntent().getIntExtra("index", 0);
        if (imagePosition == -1) imagePosition = 0;
        imageUrlArrayList = getIntent().getStringArrayListExtra("media_urls");
        mMediaSelectedList = getIntent().getStringArrayListExtra(MultiMediaSelectorFragment.KEY_MEDIA_SELECTED_LIST);
    }


    class ViewPagerAdapter extends FragmentStatePagerAdapter {

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            String uri = imageUrlArrayList.get(position);
            Bundle bundle = new Bundle();
            String[] params=uri.split(MultiMediaSelectorFragment.TYPE_SPLIT);
            String imagePath=params[0];
            int type=Integer.parseInt(params[1]);
            bundle.putString("path", imagePath);
            Fragment fragment =(type== MediaItem.PHOTO? new ImageFragment():new VideoFragment());
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return imageUrlArrayList.size();
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int currentPage) {
        imagePosition = currentPage;
        updateDisplay(imagePosition);
    }

    private void updateDisplay(int position) {
        String uri = imageUrlArrayList.get(position);
        uri=uri.split(MultiMediaSelectorFragment.TYPE_SPLIT)[0];
        mCheckBox.setChecked(mMediaSelectedList.contains(uri));
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle((position + 1) + "/" + imageUrlArrayList.size());
        }
    }
}