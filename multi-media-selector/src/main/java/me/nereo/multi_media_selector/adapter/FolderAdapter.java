package me.nereo.multi_media_selector.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import me.nereo.multi_media_selector.MultiMediaSelectorFragment;
import me.nereo.multi_media_selector.R;
import me.nereo.multi_media_selector.bean.Folder;

/**
 * 文件夹Adapter
 * Created by Nereo on 2015/4/7.
 * Updated by nereo on 2016/1/19.
 * Modify by skyArron 2016/6/1
 */
public class FolderAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;

    private List<Folder> mFolders = new ArrayList<>();


    int mImageSize;

    int lastSelected = 0;

    private int mMediaListModel;

    public FolderAdapter(Context context) {
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageSize = mContext.getResources().getDimensionPixelOffset(R.dimen.folder_cover_size);
    }

    public void setMediaListModel(int model){
        mMediaListModel= model;
    }

    /**
     * 设置数据集
     *
     * @param folders
     */
    public void setData(List<Folder> folders) {
        if (folders != null && folders.size() > 0) {
            mFolders = folders;
        } else {
            mFolders.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFolders.size();
    }

    @Override
    public Folder getItem(int i) {
        return mFolders.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.list_item_folder, viewGroup, false);
            holder = new ViewHolder(view);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        if (holder != null) {
//            if (i == 0) {
//                if(mMediaListModel== MultiMediaSelectorFragment.LIST_IMAGE){
//                    holder.name.setText(R.string.folder_all);
//                }else if(mMediaListModel== MultiMediaSelectorFragment.LIST_VIDEO){
//                    holder.name.setText(R.string.folder_all_video);
//                }else {
//                    if(isImgFolderFirst){
//                        holder.name.setText(R.string.folder_all);
//                    }else {
//                        holder.name.setText(R.string.folder_all_video);
//                    }
//                }
//                holder.path.setText("/sdcard");
//                holder.size.setText(String.format("%d%s",getTotalImageSize(), mContext.getResources().getString(R.string.photo_unit)));
//
//                if (mFolders.size() > 0) {
//                    Folder f = mFolders.get(0);
//                    Uri coverPath = null;
//                    if (f.cover != null) {
//                        coverPath=f.cover.getUriOrigin();
//                    }
//                    Picasso.with(mContext)
//                            .load(coverPath)
//                            .error(R.drawable.default_error)
//                            .resizeDimen(R.dimen.folder_cover_size, R.dimen.folder_cover_size)
//                            .centerCrop()
//                            .into(holder.cover);
//                }
//            }else if (i==2) {
//                if(mMediaListModel== MultiMediaSelectorFragment.LIST_IMAGE_VIDEO){
//                    if(isImgFolderFirst){
//                        holder.name.setText(R.string.folder_all_video);
//                    }else {
//                        holder.name.setText(R.string.folder_all);
//                    }
//                    holder.path.setText("/sdcard");
//                    holder.size.setText(String.format("%d%s",getTotalImageSize(), mContext.getResources().getString(R.string.photo_unit)));
//                }else {
//                    holder.bindData(getItem(i));
//                }
//
//            }
            holder.bindData(getItem(i));
            if (lastSelected == i) {
                holder.indicator.setVisibility(View.VISIBLE);
            } else {
                holder.indicator.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }
//
//    private int getTotalImageSize() {
//        int result = 0;
//        if (mFolders != null && mFolders.size() > 0) {
//            for (Folder f : mFolders) {
//                result += f.images.size();
//            }
//        }
//        return result;
//    }
//
//    private int getTotalVideoSize() {
//        int result = 0;
//        if (mFolders != null && mFolders.size() > 0) {
//            for (Folder f : mFolders) {
//                result += f.images.size();
//            }
//        }
//        return result;
//    }

    public void setSelectIndex(int i) {
        if (lastSelected == i) return;

        lastSelected = i;
        notifyDataSetChanged();
    }

    public int getSelectIndex() {
        return lastSelected;
    }

    class ViewHolder {
        ImageView cover;
        TextView name;
        TextView path;
        TextView size;
        ImageView indicator;

        ViewHolder(View view) {
            cover = (ImageView) view.findViewById(R.id.cover);
            name = (TextView) view.findViewById(R.id.name);
            path = (TextView) view.findViewById(R.id.path);
            size = (TextView) view.findViewById(R.id.size);
            indicator = (ImageView) view.findViewById(R.id.indicator);
            view.setTag(this);
        }

        void bindData(Folder data) {
            if (data == null) {
                return;
            }
            name.setText(data.name);
            path.setText((data.path.equals(MultiMediaSelectorFragment.IMAGE_PATH)||data.path.equals(MultiMediaSelectorFragment.VIDEO_PATH))?"/sdcard":data.path);
            if (data.images != null) {
                size.setText(String.format("%d%s", data.images.size(), mContext.getResources().getString(R.string.photo_unit)));
            } else {
                size.setText("*" + mContext.getResources().getString(R.string.photo_unit));
            }
            // 显示图片
            if (data.cover != null) {
                Uri coverPath = data.cover.getUriOrigin();
                Picasso.with(mContext)
                        .load(coverPath)
                        .placeholder(R.drawable.default_error)
                        .resizeDimen(R.dimen.folder_cover_size, R.dimen.folder_cover_size)
                        .centerCrop()
                        .into(cover);
            } else {
                cover.setImageResource(R.drawable.default_error);
            }
        }
    }

}
