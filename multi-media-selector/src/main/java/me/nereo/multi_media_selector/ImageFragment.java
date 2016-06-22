package me.nereo.multi_media_selector;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.pnikosis.materialishprogress.ProgressWheel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

/**
 * Created by skyArraon on 16/6/16.
 */
public class ImageFragment extends Fragment {
     ProgressWheel loading;
     PhotoView photoView;
     String path;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.item_media, null);
        initArgs();
        initView(view);
        return view;
    }

    private void initView(View view) {
        loading= (ProgressWheel)view.findViewById(R.id.loading);
        photoView = (PhotoView)view.findViewById(R.id.photo);
        photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        photoView.enable();
        // 显示图片
        Picasso.with(getActivity())
                .load(new File(path))
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .config(Bitmap.Config.RGB_565)
                .error(R.drawable.default_error)
                .into(photoView, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        loading.setVisibility(View.GONE);
                    }
                });
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPhotoClick();
            }
        });
    }

    private void initArgs() {
        Bundle bundle = getArguments();
        path = bundle.getString("path");
    }

    private void onPhotoClick() {
        getActivity().onBackPressed();
        getActivity().overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);

    }
}
