package me.nereo.multi_media_selector;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.ListPopupWindow;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_media_selector.adapter.FolderAdapter;
import me.nereo.multi_media_selector.adapter.MediaGridAdapter;
import me.nereo.multi_media_selector.bean.Folder;
import me.nereo.multi_media_selector.bean.MediaItem;
import me.nereo.multi_media_selector.utils.FileUtils;
import me.nereo.multi_media_selector.utils.MediaUtils;
import me.nereo.multi_media_selector.utils.ScreenUtils;

/**
 * 图片选择Fragment
 * Created by Nereo on 2015/4/7.
 */
public class MultiMediaSelectorFragment extends Fragment {

    public static final String TAG = "me.nereo.multiimageselector.frag.MultiImageSelectorFragment";

    private static final String KEY_TEMP_FILE = "key_temp_file";
    private static final String KEY_MEDIA_SELECTED_LIST = "media_selected_list";

    public static final int LIST_IMAGE = 0;
    public static final int LIST_VIDEO = 1;
    public static final int LIST_IMAGE_VIDEO = 2;

    public static final String VIDEO_PATH = "/sdcard/video";
    public static final String IMAGE_PATH = "/sdcard/image";
    /**
     * 单选
     */
    public static final int MODE_SINGLE = 0;
    /**
     * 多选
     */
    public static final int MODE_MULTI = 1;

    /**
     * 选择结果，返回为 ArrayList&lt;String&gt; 图片路径集合
     */
    public static final String EXTRA_RESULT = "select_result";

    public static final String EXTRA_MEDIA_OPTIONS = "extra_media_options";


    //CursorLoader id 存在复用机制.
    private static final int LOADER_ALL = 1;
    private static final int LOADER_CATEGORY = 1 << 1;
    private static final int LOADER_ALL_VIDEO = 1 << 2;
    private static final int LOADER_CATEGORY_VIDEO = 1 << 3;

    //标记Loader的启用情况
    private int mLoaderStatus;


    // 请求加载系统照相机
    private static final int REQUEST_CAMERA = 100;
    public static final int VIDEO_RECORDER_REQUEST_CODE = 104;


    // 结果数据
    private ArrayList<MediaItem> mMediaSelectedList = new ArrayList<>();

    // 文件夹数据
    private ArrayList<Folder> mResultFolder = new ArrayList<>();

    // 图片Grid
    private GridView mGridView;
    private Callback mCallback;

    private MediaGridAdapter mImageAdapter;
    private FolderAdapter mFolderAdapter;

    private ListPopupWindow mFolderPopupWindow;

    // 类别
    private TextView mCategoryText;
    // 预览按钮
    private Button mPreviewBtn;
    // 底部View
    private View mPopupAnchorView;



    private boolean hasFolderGened = false;

    private File mTmpFile;


    private MediaOptions mMediaOptions;
    private Bundle mSavedInstanceState;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (Callback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("The Activity must implement MultiImageSelectorFragment.Callback interface...");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(me.nereo.multi_media_selector.R.layout.fragment_multi_image, container, false);
    }


    /**
     * image selectListener
     */
    public MultiMediaSelectorFragment() {
        mSavedInstanceState = new Bundle();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mMediaOptions = savedInstanceState
                    .getParcelable(EXTRA_MEDIA_OPTIONS);
            mMediaSelectedList = savedInstanceState
                    .getParcelableArrayList(KEY_MEDIA_SELECTED_LIST);
            mTmpFile = (File) savedInstanceState.getSerializable(KEY_TEMP_FILE);
            mSavedInstanceState = savedInstanceState;
        } else {
            mMediaOptions = getArguments().getParcelable(EXTRA_MEDIA_OPTIONS);
        }


        // 默认选择
        if (mMediaOptions.getmMode() == MODE_MULTI) {
            ArrayList<MediaItem> tmp = mMediaOptions.getMediaListSelected();
            if (tmp != null && tmp.size() > 0) {
                mMediaSelectedList = tmp;
            }
        }

        // 是否显示照相机
        mImageAdapter = new MediaGridAdapter(getActivity(), mMediaOptions.ismShowCamera(), 3);
        // 是否显示选择指示器
        mImageAdapter.showSelectIndicator(mMediaOptions.getmMode() == MODE_MULTI);

        mImageAdapter.setOnImageSelectedListener(new MediaGridAdapter.OnImageSelectedListener() {
            @Override
            public void onImageSelected(MediaItem mediaItem) {

                if (mediaItem == null || mediaItem.getPath() == null) {
                    return;
                }
                //对选择视频大小50M的限制.
                if(mediaItem.getType()== MediaItem.VIDEO){
//                    long videoSize=FileUtil.getFileSize(new File(mediaItem.getPath()));
//                    if(videoSize/ (1024 * 1024)>50){
//                        ToastUtil.Po(getContext(),getContext().getString(R.string.video_size_too_max));
//                        return;
//                    }
                }
                if (mMediaSelectedList.contains(mediaItem)) {
                    mMediaSelectedList.remove(mediaItem);
                    if (mMediaSelectedList.size() != 0) {
                        mPreviewBtn.setEnabled(true);
                        mPreviewBtn.setText(getResources().getString(me.nereo.multi_media_selector.R.string.preview) + "(" + mMediaSelectedList.size() + ")");
                    } else {
                        mPreviewBtn.setEnabled(false);
                        mPreviewBtn.setText(me.nereo.multi_media_selector.R.string.preview);
                    }
                    if (mCallback != null) {
                        mCallback.onImageUnselected(mediaItem);
                    }
                } else {
                    // 判断选择数量问题
                    if (mMediaOptions.getmMaxCount() == mMediaSelectedList.size()) {
                        Toast.makeText(getActivity(), getString(me.nereo.multi_media_selector.R.string.msg_amount_limit, mMediaOptions.getmMaxCount()), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mMediaSelectedList.add(mediaItem);
                    mPreviewBtn.setEnabled(true);
                    mPreviewBtn.setText(getResources().getString(me.nereo.multi_media_selector.R.string.preview) + "(" + mMediaSelectedList.size() + ")");
                    if (mCallback != null) {
                        mCallback.onImageSelected(mediaItem);
                    }
                }
                mImageAdapter.select(mediaItem);
            }
        });


        mPopupAnchorView = view.findViewById(me.nereo.multi_media_selector.R.id.footer);

        mCategoryText = (TextView) view.findViewById(me.nereo.multi_media_selector.R.id.category_btn);
        mCategoryText.setText(mMediaOptions.getmMediaType() == LIST_IMAGE ? R.string.folder_all : mMediaOptions.getmMediaType()  == LIST_VIDEO ? R.string.folder_all_video : mMediaOptions.getIsCarmeActionFirst() ? R.string.folder_all : R.string.folder_all_video);
        mCategoryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mFolderPopupWindow == null) {
                    createPopupFolderList();
                }

                if (mFolderPopupWindow.isShowing()) {
                    mFolderPopupWindow.dismiss();
                } else {
                    mFolderPopupWindow.show();
//                    int index = mFolderAdapter.getSelectIndex();
//                    index = index == 0 ? index : index - 1;
//                    mFolderPopupWindow.getListView().setSelection(index);
                }
            }
        });

        mPreviewBtn = (Button) view.findViewById(me.nereo.multi_media_selector.R.id.preview);
        // 初始化，按钮状态初始化
        if (mMediaSelectedList == null || mMediaSelectedList.size() <= 0) {
            mPreviewBtn.setText(me.nereo.multi_media_selector.R.string.preview);
            mPreviewBtn.setEnabled(false);
        }
        mPreviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO 预览
            }
        });

        mGridView = (GridView) view.findViewById(me.nereo.multi_media_selector.R.id.grid);
        mGridView.setAdapter(mImageAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                MediaItem item = (MediaItem) adapterView.getAdapter().getItem(i);
                if (mImageAdapter.isShowAction()) {
                    // 如果显示照相机，则第一个Grid显示为照相机，处理特殊逻辑
                    if (i == 0) {
                        if (mMediaOptions.getmMaxCount() == mMediaSelectedList.size() && mMediaOptions.getmMode() == MODE_MULTI) {
                            Toast.makeText(getActivity(), getString(me.nereo.multi_media_selector.R.string.msg_amount_limit, mMediaOptions.getmMaxCount()), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (mImageAdapter.getTypeAction() == 0) {
                            showCameraAction();
                        } else {
                            showVideoAction();
                        }

                    } else {
                        selectImageFromGrid(item, mMediaOptions.getmMode());

                    }
                } else {
                    selectImageFromGrid(item, mMediaOptions.getmMode());

                }
            }
        });
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_FLING) {
                    Picasso.with(view.getContext()).pauseTag(TAG);
                } else {
                    Picasso.with(view.getContext()).resumeTag(TAG);
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        mFolderAdapter = new FolderAdapter(getActivity());
        mFolderAdapter.setMediaListModel(mMediaOptions.getmMediaType());
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        Point point = ScreenUtils.getScreenSize(getActivity());
        int width = point.x;
        int height = (int) (point.y * (4.5f / 8.0f));
        mFolderPopupWindow = new ListPopupWindow(getActivity());
        mFolderPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        mFolderPopupWindow.setAdapter(mFolderAdapter);
        mFolderPopupWindow.setContentWidth(width);
        mFolderPopupWindow.setWidth(width);
        mFolderPopupWindow.setHeight(height);
        mFolderPopupWindow.setAnchorView(mPopupAnchorView);
        mFolderPopupWindow.setModal(true);
        mFolderPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                mFolderAdapter.setSelectIndex(i);

                final int index = i;
                final AdapterView v = adapterView;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFolderPopupWindow.dismiss();
                        //暂不从新拉取
                        if (index == 0) {
                            if (mMediaOptions.getmMediaType() == LIST_IMAGE) {
                                getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                            } else if (mMediaOptions.getmMediaType() == LIST_VIDEO) {
                                getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL_VIDEO, null, mVideoLoaderCallback);
                            } else {
                                if (mMediaOptions.getIsCarmeActionFirst()) {
                                    getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                                } else {
                                    getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL_VIDEO, null, mVideoLoaderCallback);
                                }
                            }
                            mCategoryText.setText(mMediaOptions.getmMediaType() == LIST_IMAGE ? R.string.folder_all : mMediaOptions.getmMediaType() == LIST_VIDEO ? R.string.folder_all_video : mMediaOptions.getIsCarmeActionFirst() ? R.string.folder_all : R.string.folder_all_video);
                            if (mMediaOptions.ismShowCamera()) {
                                mImageAdapter.setShowAction(true);
                            } else {
                                mImageAdapter.setShowAction(false);
                            }
                        } else if (index == 1) {
                            if (mMediaOptions.getmMediaType() == LIST_IMAGE_VIDEO) {
                                if (mMediaOptions.getIsCarmeActionFirst()) {
                                    getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL_VIDEO, null, mVideoLoaderCallback);
                                } else {
                                    getActivity().getSupportLoaderManager().restartLoader(LOADER_ALL, null, mLoaderCallback);
                                }
                                mCategoryText.setText(mMediaOptions.getmMediaType() == LIST_IMAGE ? R.string.folder_all : mMediaOptions.getmMediaType() == LIST_VIDEO ? R.string.folder_all_video : mMediaOptions.getIsCarmeActionFirst() ? R.string.folder_all_video : R.string.folder_all);
                                if (mMediaOptions.ismShowCamera()) {
                                    mImageAdapter.setShowAction(true);
                                } else {
                                    mImageAdapter.setShowAction(false);
                                }
                            } else {
                                Folder folder = (Folder) v.getAdapter().getItem(index);
                                changeFolderData(folder, index);
                            }
                        } else {
                            Folder folder = (Folder) v.getAdapter().getItem(index);
                            changeFolderData(folder, index);
                        }
                        // 滑动到最初始位置
                        mGridView.smoothScrollToPosition(0);
                    }
                }, 100);

            }

            private void changeFolderData(Folder folder, int index) {

                if (null != folder) {
                    mImageAdapter.setData(folder.images);
                    mCategoryText.setText(folder.name);
                    // 设定默认选择
                    if (mMediaSelectedList != null && mMediaSelectedList.size() > 0) {
                        mImageAdapter.setDefaultSelected(mMediaSelectedList);
                    }
                }
                if (folder != null && (folder.path.equals(VIDEO_PATH) || folder.path.equals(IMAGE_PATH))) {
                    mImageAdapter.setShowAction(true);
                } else {
                    mImageAdapter.setShowAction(false);
                }
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSavedInstanceState.putSerializable(KEY_TEMP_FILE, mTmpFile);
        mSavedInstanceState.putParcelable(
                MultiMediaSelectorFragment.EXTRA_MEDIA_OPTIONS, mMediaOptions);
        mSavedInstanceState.putParcelableArrayList(KEY_MEDIA_SELECTED_LIST,
                mMediaSelectedList);
        outState.putAll(mSavedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mTmpFile = (File) savedInstanceState.getSerializable(KEY_TEMP_FILE);
            mMediaOptions = savedInstanceState
                    .getParcelable(EXTRA_MEDIA_OPTIONS);
            mMediaSelectedList = savedInstanceState
                    .getParcelableArrayList(KEY_MEDIA_SELECTED_LIST);
            mSavedInstanceState = savedInstanceState;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 首次加载所有图片
        //new LoadImageTask().execute();
        if (mMediaOptions.getmMediaType() == LIST_VIDEO) {
            getActivity().getSupportLoaderManager().initLoader(LOADER_ALL_VIDEO, null, mVideoLoaderCallback);
        } else {
            getActivity().getSupportLoaderManager().initLoader(LOADER_ALL, null, mLoaderCallback);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相机拍照完成后，返回图片路径
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                if (mTmpFile != null) {
                    if (mCallback != null) {
                        MediaItem item
                                = new MediaItem(MediaItem.PHOTO, Uri.fromFile(mTmpFile), mTmpFile.getAbsolutePath());
                        mCallback.onCameraShot(item);
                    }
                }
            } else {
                while (mTmpFile != null && mTmpFile.exists()) {
                    boolean success = mTmpFile.delete();
                    if (success) {
                        mTmpFile = null;
                    }
                }
            }
        }

        if(requestCode ==VIDEO_RECORDER_REQUEST_CODE ){
            if (resultCode == Activity.RESULT_OK) {
                returnVideo(data.getData());
            }
        }

    }


    private void returnVideo(Uri videoUri) {
        final int code = checkValidVideo(videoUri);
        switch (code) {
            // not found. should never happen. Do nothing when happen.
            case -2:
                break;
            // smaller than min
            case -1:
                // in seconds
                int duration = mMediaOptions.getMinVideoDuration() / 1000;
                showVideoInvalid((getActivity().getString(R.string.picker_video_duration_min,duration)));
                break;

            // larger than max
            case 0:
                // in seconds.
                duration = mMediaOptions.getMaxVideoDuration() / 1000;
                showVideoInvalid(getActivity().getString(R.string.picker_video_duration_max,duration));
                break;
            // ok
            case 1:
                MediaItem item = new MediaItem(MediaItem.VIDEO, videoUri,videoUri.getPath());
                mCallback.onRecordShot(item);
                break;

            default:
                break;
        }
    }

    private void showVideoInvalid(String msg) {
        MediaPickerErrorDialog errorDialog = MediaPickerErrorDialog
                .newInstance(msg);
        errorDialog.show(getActivity().getSupportFragmentManager(), null);
    }


    private int checkValidVideo(Uri videoUri) {
        if (videoUri == null)
            return -2;
        // try get duration using MediaPlayer. (Should get duration using
        // MediaPlayer before use Uri because some devices can get duration by
        // Uri or not exactly. Ex: Asus Memo Pad8)
        long duration = MediaUtils.getDuration(getActivity().getApplicationContext(),
                MediaUtils.getRealVideoPathFromURI(getActivity().getContentResolver(), videoUri));
        if (duration == 0) {
            // try get duration one more, by uri of video. Note: Some time can
            // not get duration by Uri after record video.(It's usually happen
            // in HTC
            // devices 2.3, maybe others)
            duration = MediaUtils
                    .getDuration(getActivity().getApplicationContext(), videoUri);
        }
        // accept delta about < 1000 milliseconds. (ex: 10769 is still accepted
        // if limit is 10000)
        if (mMediaOptions.getMaxVideoDuration() != Integer.MAX_VALUE
                && duration >= mMediaOptions.getMaxVideoDuration() + 1000) {
            return 0;
        } else if (duration == 0
                || duration < mMediaOptions.getMinVideoDuration()) {
            return -1;
        }
        return 1;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mFolderPopupWindow != null) {
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 选择相机
     */
    private void showCameraAction() {
        // 跳转到系统照相机
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // 设置系统相机拍照后的输出路径
            // 创建临时文件
            try {
                mTmpFile = FileUtils.createTmpFile(getActivity());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (mTmpFile != null && mTmpFile.exists()) {
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            } else {
                Toast.makeText(getActivity(), "图片错误", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), me.nereo.multi_media_selector.R.string.msg_no_camera, Toast.LENGTH_SHORT).show();
        }
    }


    private void showVideoAction() {
        final Intent takeVideoIntent = new Intent(
                MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            int max = mMediaOptions.getMaxVideoDuration();
            if (max != Integer.MAX_VALUE) {
                max /= 1000;
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, max);
                if (mMediaOptions.isShowWarningVideoDuration()) {
                    MediaPickerErrorDialog dialog = MediaPickerErrorDialog
                            .newInstance(getString(R.string.picker_video_duration_warning,max));
                    dialog.setOnOKClickListener(new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivityForResult(takeVideoIntent,
                                    VIDEO_RECORDER_REQUEST_CODE);
                        }
                    });
                    dialog.show(getActivity().getSupportFragmentManager(), null);
                }else {
                    startActivityForResult(takeVideoIntent, VIDEO_RECORDER_REQUEST_CODE);
                }
            } else {
                startActivityForResult(takeVideoIntent, VIDEO_RECORDER_REQUEST_CODE);
            }
        }
    }



    /**
     * 选择图片操作
     *
     * @param
     */
    private void selectImageFromGrid(MediaItem item, int mode) {
        if (item != null) {
            // 多选模式
            if (mode == MODE_MULTI) {
                /*if (mMediaSelectedList.contains(image.path)) {
                    mMediaSelectedList.remove(image.path);
                    if(mMediaSelectedList.size() != 0) {
                        mPreviewBtn.setEnabled(true);
                        mPreviewBtn.setText(getResources().getString(R.string.preview) + "(" + mMediaSelectedList.size() + ")");
                    }else{
                        mPreviewBtn.setEnabled(false);
                        mPreviewBtn.setText(R.string.preview);
                    }
                    if (mCallback != null) {
                        mCallback.onImageUnselected(image.path);
                    }
                } else {
                    // 判断选择数量问题
                    if(mDesireImageCount == mMediaSelectedList.size()){
                        Toast.makeText(getActivity(), R.string.msg_amount_limit, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mMediaSelectedList.add(image.path);
                    mPreviewBtn.setEnabled(true);
                    mPreviewBtn.setText(getResources().getString(R.string.preview) + "(" + mMediaSelectedList.size() + ")");
                    if (mCallback != null) {
                        mCallback.onImageSelected(image.path);
                    }
                }
                mImageAdapter.select(image);*/
                if (item.isPhoto()) {
//                    PageSwitch.go2ViewBigImagePage(getContext(), Uri.fromFile(new File(item.getPath())), "");
                } else {
//                    PageSwitch.go2VideoPreviewPage(getContext(), item.getPath(), 99);
                }

            } else if (mode == MODE_SINGLE) {
                // 单选模式
                if (mCallback != null) {
                    mCallback.onSingleImageSelected(item);
                }
            }
        }
    }

    private boolean fileExist(String path) {
        if (!TextUtils.isEmpty(path)) {
            return new File(path).exists();
        }
        return false;
    }

    private void removeExist(int mime) {
        for (Folder folder : mResultFolder) {
            for (int i = 0; i < folder.images.size(); i++) {
                MediaItem item = folder.images.get(i);
                if (item.getType() == mime) {
                    folder.images.remove(item);
                    i--;
                }
            }
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID,
        };


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL) {
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[3] + "=? OR " + IMAGE_PROJECTION[3] + "=? ",
                        new String[]{"image/jpeg", "image/png"}, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY) {
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                        IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[0] + " like '%" + args.getString("path") + "%'",
                        null, IMAGE_PROJECTION[2] + " DESC");
                return cursorLoader;
            }

            return null;
        }


        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            List<MediaItem> images = packageImageData(data);
            if (mMediaOptions.getmMediaType() == LIST_IMAGE || ((mLoaderStatus & LOADER_ALL) == LOADER_ALL)) {
                mImageAdapter.setData(images);
                // 设定默认选择
                finishListData();
            } else {
                getActivity().getSupportLoaderManager().initLoader(LOADER_ALL_VIDEO, null, mVideoLoaderCallback);
            }

            mLoaderStatus |= LOADER_ALL;
        }

        private List<MediaItem> packageImageData(Cursor data) {
            List<MediaItem> images = new ArrayList<>();
            if (data != null) {
                if (data.getCount() > 0) {
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                        MediaItem media = null;
                        if (fileExist(path)) {
                            media = new MediaItem(MediaItem.PHOTO, MediaUtils.getPhotoUri(data), path);
                            images.add(media);
                        }
                        if (!hasFolderGened) {
                            // 获取文件夹名称
                            File folderFile = new File(path).getParentFile();
                            if (folderFile != null && folderFile.exists()) {
                                String fp = folderFile.getAbsolutePath();
                                Folder f = getFolderByPath(fp);
                                if (f == null) {
                                    Folder folder = new Folder();
                                    folder.name = folderFile.getName();
                                    folder.path = fp;
                                    folder.cover = media;
                                    List<MediaItem> imageList = new ArrayList<>();
                                    imageList.add(media);
                                    folder.images = imageList;
                                    mResultFolder.add(folder);
                                } else {
                                    f.images.add(media);
                                }
                            }
                        }

                    } while (data.moveToNext());

                    //last Folder
                    Folder f = getFolderByPath(IMAGE_PATH);
                    if (f == null) {
                        Folder folder = new Folder();
                        folder.name = getActivity().getResources().getString(R.string.folder_all);
                        folder.path = IMAGE_PATH;
                        folder.cover = images.get(0);
                        List<MediaItem> videoList = new ArrayList<>();
                        videoList.addAll(images);
                        folder.images = videoList;
                        mResultFolder.add(0, folder);
                    } else {
                        f.images.clear();
                        f.images.addAll(images);
                    }
                }
            }
            return images;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };


    private LoaderManager.LoaderCallbacks<Cursor> mVideoLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        /**video**/

        private final String[] VIDEO_PROJECTION = new String[]{
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media._ID,
        };

        /**video**/


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (id == LOADER_ALL_VIDEO) {
                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION, VIDEO_PROJECTION[4] + ">0", null, VIDEO_PROJECTION[2] + " DESC");
                return cursorLoader;
            } else if (id == LOADER_CATEGORY_VIDEO) {

                CursorLoader cursorLoader = new CursorLoader(getActivity(),
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_PROJECTION, VIDEO_PROJECTION[4] + ">0 AND " + VIDEO_PROJECTION[0] + " like '%" + args.getString("path") + "%'",
                        null, VIDEO_PROJECTION[2] + " DESC");
                return cursorLoader;
            }
            return null;
        }


        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            List<MediaItem> videos = packVideoData(data);
            if (mMediaOptions.getmMediaType() == LIST_VIDEO || ((mLoaderStatus & LOADER_ALL_VIDEO) == LOADER_ALL_VIDEO)) {
                mImageAdapter.setData(videos);
                finishListData();
            } else {
                mImageAdapter.setData(mResultFolder.get(0).images);
                finishListData();
            }
            mLoaderStatus |= LOADER_ALL_VIDEO;
        }

        private List<MediaItem> packVideoData(Cursor data) {
            List<MediaItem> videos = new ArrayList<>();
            if (data != null) {
                if (data.getCount() > 0) {
                    data.moveToFirst();
                    do {
                        String path = data.getString(data.getColumnIndexOrThrow(VIDEO_PROJECTION[0]));
                        MediaItem media = null;
                        if (fileExist(path)) {
                            media = new MediaItem(MediaItem.VIDEO, MediaUtils.getVideoUri(data), path);
                            videos.add(media);
                        }
                        if (!hasFolderGened) {
                            // 获取文件夹名称
                            File folderFile = new File(path).getParentFile();
                            if (folderFile != null && folderFile.exists()) {
                                String fp = folderFile.getAbsolutePath();
                                Folder f = getFolderByPath(fp);
                                if (f == null) {
                                    Folder folder = new Folder();
                                    folder.name = folderFile.getName();
                                    folder.path = fp;
                                    folder.cover = media;
                                    List<MediaItem> videoList = new ArrayList<>();
                                    videoList.add(media);
                                    folder.images = videoList;
                                    mResultFolder.add(folder);
                                } else {
                                    f.images.add(media);
                                }
                            }
                        }

                    } while (data.moveToNext());

                    //last Folder
                    Folder f = getFolderByPath(VIDEO_PATH);
                    if (f == null) {
                        Folder folder = new Folder();
                        folder.name = getActivity().getResources().getString(R.string.folder_all_video);
                        folder.path = VIDEO_PATH;
                        folder.cover = videos.get(0);
                        List<MediaItem> videoList = new ArrayList<>();
                        videoList.addAll(videos);
                        folder.images = videoList;
                        if (mMediaOptions.getIsCarmeActionFirst()) {
                            mResultFolder.add(1, folder);
                        } else {
                            mResultFolder.add(0, folder);
                        }

                    } else {
                        f.images.clear();
                        f.images.addAll(videos);
                    }

                }
            }
            return videos;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private void finishListData() {
        if (mMediaSelectedList != null && mMediaSelectedList.size() > 0) {
            mImageAdapter.setDefaultSelected(mMediaSelectedList);
        }
        if (!hasFolderGened) {
            mFolderAdapter.setData(mResultFolder);
            hasFolderGened = true;
        }
    }


    private Folder getFolderByPath(String path) {
        if (mResultFolder != null) {
            for (Folder folder : mResultFolder) {
                if (TextUtils.equals(folder.path, path)) {
                    return folder;
                }
            }
        }
        return null;
    }

    /**
     * 回调接口
     */
    public interface Callback {
        void onSingleImageSelected(MediaItem item);

        void onImageSelected(MediaItem item);

        void onImageUnselected(MediaItem item);

        void onCameraShot(MediaItem item);

        void onRecordShot(MediaItem item);
    }
}
