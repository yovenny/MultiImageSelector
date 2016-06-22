package me.nereo.multi_media_selector;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;

import me.nereo.multi_media_selector.bean.MediaItem;

/**
 * 选择器
 */
public class MediaOptions implements Parcelable {
    private boolean isCropped;
    private int maxVideoDuration;
    private int minVideoDuration;
    private File photoCaptureFile;
    private File croppedFile;
    private ArrayList<MediaItem> mediaListSelected = new ArrayList<>();
    private boolean showWarningVideoDuration;
    private boolean mShowCamera = true;
    private int mMaxCount = 9;
    private int mMode = MultiMediaSelectorFragment.MODE_MULTI;
    private int mMediaType;
    private boolean isCarmeActionFirst;
    private int aspectX;
    private int aspectY;
    private boolean fixAspectRatio;

    public boolean isShowWarningVideoDuration() {
        return showWarningVideoDuration;
    }

    public ArrayList<MediaItem> getMediaListSelected() {
        return mediaListSelected;
    }

    public File getCroppedFile() {
        return croppedFile;
    }

    public boolean isCropped() {
        return isCropped;
    }

    public int getMaxVideoDuration() {
        return maxVideoDuration;
    }

    public int getMinVideoDuration() {
        return minVideoDuration;
    }

    public File getPhotoCaptureFile() {
        return photoCaptureFile;
    }

    public boolean ismShowCamera() {
        return mShowCamera;
    }

    public int getmMaxCount() {
        return mMaxCount;
    }

    public int getmMode() {
        return mMode;
    }

    public int getmMediaType() {
        return mMediaType;
    }

    public File getPhotoFile() {
        return photoCaptureFile;
    }

    public boolean isCarmeActionFirst() {
        return isCarmeActionFirst;
    }

    public int getAspectX() {
        return aspectX;
    }

    public int getAspectY() {
        return aspectY;
    }

    public boolean isFixAspectRatio() {
        return fixAspectRatio;
    }

    private MediaOptions(Builder builder) {
        this.isCropped = builder.isCropped;
        this.maxVideoDuration = builder.maxVideoDuration;
        this.minVideoDuration = builder.minVideoDuration;
        this.photoCaptureFile = builder.photoCaptureFile;
        this.croppedFile = builder.croppedFile;
        this.mediaListSelected = builder.mediaListSelected;
        this.showWarningVideoDuration = builder.showWarningVideoDuration;
        this.mShowCamera=builder.mShowCamera;
        this.mMaxCount=builder.mMaxCount;
        this.mMode=builder.mMode;
        this.mMediaType=builder.mMediaType;
        this.isCarmeActionFirst=builder.isCarmeActionFirst;
        this.aspectX=builder.aspectX;
        this.aspectY=builder.aspectY;
        this.fixAspectRatio=builder.fixAspectRatio;
    }




    public static MediaOptions createDefault() {
        return new Builder().build();
    }


    public static class Builder {
        private boolean isCropped = false;
        private int maxVideoDuration = Integer.MAX_VALUE;
        private int minVideoDuration = 0;
        private File photoCaptureFile;
        private File croppedFile;
        private ArrayList<MediaItem> mediaListSelected;
        private boolean showWarningVideoDuration = false;
        private boolean mShowCamera = true;
        private int mMaxCount = 9;
        private int mMode = MultiMediaSelectorFragment.MODE_MULTI;
        private int mMediaType;
        private boolean isCarmeActionFirst=true;
        private int aspectX = 1;
        private int aspectY = 1;
        private boolean fixAspectRatio = true;

        public Builder() {

        }

        public Builder setMediaType(int mediaType) {
            this.mMediaType = mediaType;
            return this;
        }

        public Builder setMode(int mode) {
            this.mMode = mode;
            return this;
        }


        public Builder setMaxcount(int maxcount) {
            this.mMaxCount = maxcount;
            return this;
        }
        public Builder setCarmeFirst(boolean first) {
            this.isCarmeActionFirst = first;
            return this;
        }


        public Builder setShowCamera(boolean show) {
            this.mShowCamera = show;
            return this;
        }

        public Builder showWarningVideoDuration(
                boolean showWarningVideoDuration) {
            this.showWarningVideoDuration = showWarningVideoDuration;
            return this;
        }

        public Builder setMediaListSelected(ArrayList<MediaItem> mediaSelecteds) {
            this.mediaListSelected = mediaSelecteds;
            return this;
        }


        public Builder setCroppedFile(File croppedFile) {
            this.croppedFile = croppedFile;
            return this;
        }

        public Builder setIsCropped(boolean isCropped) {
            this.isCropped = isCropped;
            return this;
        }

        public Builder setMaxVideoDuration(int maxDuration) {
            if (maxDuration <= 0) {
                throw new IllegalArgumentException("Max duration must be > 0");
            }
            this.maxVideoDuration = maxDuration;
            return this;
        }

        public Builder setMinVideoDuration(int minDuration) {
            if (minDuration <= 0) {
                throw new IllegalArgumentException("Min duration must be > 0");
            }
            this.minVideoDuration = minDuration;
            return this;
        }
        public Builder setPhotoCaptureFile(File file) {
            photoCaptureFile = file;
            return this;
        }

        public void setCropped(boolean cropped) {
            isCropped = cropped;
        }

        public void setShowWarningVideoDuration(boolean showWarningVideoDuration) {
            this.showWarningVideoDuration = showWarningVideoDuration;
        }

        public void setmShowCamera(boolean mShowCamera) {
            this.mShowCamera = mShowCamera;
        }

        public void setmMaxCount(int mMaxCount) {
            this.mMaxCount = mMaxCount;
        }

        public void setmMode(int mMode) {
            this.mMode = mMode;
        }

        public void setmMediaType(int mMediaType) {
            this.mMediaType = mMediaType;
        }

        public void setCarmeActionFirst(boolean carmeActionFirst) {
            isCarmeActionFirst = carmeActionFirst;
        }

        public void setAspectX(int aspectX) {
            this.aspectX = aspectX;
        }

        public void setAspectY(int aspectY) {
            this.aspectY = aspectY;
        }

        public void setFixAspectRatio(boolean fixAspectRatio) {
            this.fixAspectRatio = fixAspectRatio;
        }

        public MediaOptions build() {
            return new MediaOptions(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(isCropped ? 1 : 0);
        dest.writeInt(this.maxVideoDuration);
        dest.writeInt(this.minVideoDuration);
        dest.writeSerializable(photoCaptureFile);
        dest.writeSerializable(croppedFile);
        dest.writeTypedList(mediaListSelected);
        dest.writeInt(showWarningVideoDuration ? 1 : 0);
        dest.writeInt(mShowCamera?1:0);
        dest.writeInt(mMaxCount);
        dest.writeInt(mMode);
        dest.writeInt(mMediaType);
        dest.writeInt(isCarmeActionFirst?1:0);
        dest.writeInt(aspectX);
        dest.writeInt(aspectY);
        dest.writeInt(fixAspectRatio?1:0);
    }

    public MediaOptions(Parcel in) {
        this.isCropped = in.readInt() == 0 ? false : true;
        this.maxVideoDuration = in.readInt();
        this.minVideoDuration = in.readInt();
        this.photoCaptureFile = (File) in.readSerializable();
        this.croppedFile = (File) in.readSerializable();
        in.readTypedList(this.mediaListSelected, MediaItem.CREATOR);
        this.showWarningVideoDuration = in.readInt() == 0 ? false : true;
        this.mShowCamera = in.readInt() == 0 ? false : true;
        this.mMaxCount=in.readInt();
        this.mMode=in.readInt();
        this.mMediaType=in.readInt();
        this.isCarmeActionFirst=in.readInt()==0?false:true;
        this.aspectX=in.readInt();
        this.aspectY=in.readInt();
        this.fixAspectRatio=in.readInt()==0?false:true;
    }


    public static final Creator<MediaOptions> CREATOR = new Creator<MediaOptions>() {

        @Override
        public MediaOptions[] newArray(int size) {
            return new MediaOptions[size];
        }

        @Override
        public MediaOptions createFromParcel(Parcel source) {
            return new MediaOptions(source);
        }
    };

}