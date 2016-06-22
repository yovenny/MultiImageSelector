package me.nereo.multi_media_selector.adapter;

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_media_selector.MultiMediaSelectorFragment;
import me.nereo.multi_media_selector.R;
import me.nereo.multi_media_selector.bean.MediaItem;

/**
 * 图片Adapter
 * Created by Nereo on 2015/4/7.
 * Updated by nereo on 2016/1/19.
 */
public class MediaGridAdapter extends BaseAdapter {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_NORMAL = 1;

    private Context mContext;

    private LayoutInflater mInflater;
    private boolean showAction = true;
    private boolean showSelectIndicator = true;
    private int mActionType=0;//img,video,img&video

    private List<MediaItem> mImages = new ArrayList<>();
    private List<MediaItem> mSelectedImages = new ArrayList<>();

    final int mGridWidth;
    private OnImageSelectedListener mOnImageSelectedListener;

    public MediaGridAdapter(Context context, boolean showAction, int column) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.showAction = showAction;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            width = size.x;
        } else {
            width = wm.getDefaultDisplay().getWidth();
        }
        mGridWidth = width / column;
    }

    /**
     * 显示选择指示器
     *
     * @param b
     */
    public void showSelectIndicator(boolean b) {
        showSelectIndicator = b;
    }

    public void setShowAction(boolean b) {
        if (showAction == b) return;
        showAction = b;
        syncTypeAction(mImages);
        notifyDataSetChanged();
    }

    public boolean isShowAction() {
        return showAction;
    }

    /**
     * 选择某个图片，改变选择状态
     *
     * @param image
     */
    public void select(MediaItem image) {
        if (mSelectedImages.contains(image)) {
            mSelectedImages.remove(image);
        } else {
            mSelectedImages.add(image);
        }
        notifyDataSetChanged();
    }

    /**
     * 通过图片路径设置默认选择
     *
     * @param resultList
     */
    public void setDefaultSelected(ArrayList<MediaItem> resultList) {
        mSelectedImages.clear();
        for (MediaItem item : resultList) {
            MediaItem image = getImageByPath(item.getPath());
            if (image != null) {
                mSelectedImages.add(image);
            }
        }
        if (mSelectedImages.size() > 0) {
            notifyDataSetChanged();
        }
    }

    private MediaItem getImageByPath(String path) {
        if (path != null && mImages != null && mImages.size() > 0) {
            for (MediaItem image : mImages) {
                    if (image != null && image.getPath() != null && image.getPath().equalsIgnoreCase(path)) {
                        return image;
                    }
                }
        }
        return null;
    }

    /**
     * 设置数据集
     *
     * @param images
     */
    public void setData(List<MediaItem> images) {
        mSelectedImages.clear();
        if (images != null && images.size() > 0) {
            mImages = images;
            syncTypeAction(images);
        } else {
            mImages.clear();
        }
        notifyDataSetChanged();
    }

    public void syncTypeAction(List<MediaItem> images){
            if (isShowAction()) {
                MediaItem item = images.get(0);
                mActionType = item.isPhoto() ? 0 : 1;
            }
    }

    public int getTypeAction(){
        return mActionType;
    }


    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (showAction) {
            return position == 0 ? TYPE_CAMERA : TYPE_NORMAL;
        }
        return TYPE_NORMAL;
    }

    @Override
    public int getCount() {
        return showAction ? mImages.size() + 1 : mImages.size();
    }

    @Override
    public MediaItem getItem(int i) {
        if (showAction) {
            if (i == 0) {
                return null;
            }
            return mImages.get(i - 1);
        } else {
            return mImages.get(i);
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        if (isShowAction()) {
            if (i == 0) {
                if(mActionType==0){
                    view = mInflater.inflate(R.layout.list_item_camera, viewGroup, false);
                }else {
                    view = mInflater.inflate(R.layout.list_item_record, viewGroup, false);
                }

                return view;
            }
        }

        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_image, viewGroup, false);
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.indicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnImageSelectedListener != null) {
                    mOnImageSelectedListener.onImageSelected(getItem(i));
                }
            }
        });

        if (holder != null) {
            holder.bindData(getItem(i));
        }

        return view;
    }

    class ViewHolder {
        View thumb;
        ImageView image;
        ImageView indicator;
//        View mask;

        ViewHolder(View view) {
            image = (ImageView) view.findViewById(R.id.image);
            indicator = (ImageView) view.findViewById(R.id.checkmark);
//            mask = view.findViewById(R.id.mask);
            thumb=view.findViewById(R.id.thumb);
            view.setTag(this);
        }

        void bindData(final MediaItem data) {
            if (data == null) return;
            // 处理单选和多选状态
            if (showSelectIndicator) {
                indicator.setVisibility(View.VISIBLE);
                if (mSelectedImages.contains(data)) {
                    // 设置选中状态
                    indicator.setImageResource(R.drawable.btn_selected);
//                    mask.setVisibility(View.VISIBLE);
                } else {
                    // 未选择
                    indicator.setImageResource(R.drawable.btn_unselected);
//                    mask.setVisibility(View.GONE);
                }
            } else {
                indicator.setVisibility(View.GONE);
            }
            if(data.isPhoto()){
                thumb.setVisibility(View.GONE);
            }else{
                thumb.setVisibility(View.VISIBLE);
            }
            // 显示图片
            Picasso.with(mContext)
                    .load(data.getUriOrigin())
                    .placeholder(R.drawable.default_error)
                    .tag(MultiMediaSelectorFragment.TAG)
                    .resize(mGridWidth, mGridWidth)
                    .error(R.drawable.default_error)
                    .centerCrop()
                    .into(image);
        }
    }

    public void setOnImageSelectedListener(OnImageSelectedListener onImageSelectedListener) {
        mOnImageSelectedListener = onImageSelectedListener;
    }

    public interface OnImageSelectedListener {

        void onImageSelected(MediaItem image);
    }

}
