package me.nereo.multi_media_selector.bean;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import me.nereo.multi_media_selector.utils.MediaUtils;


/**
 * @author TUNGDX
 *         <p/>
 *         TODO chenbc 1.(增加了path,减少cursor的查询)暂不确定path是否多余,2.更改path为equals的唯一指标去除uriOrigin,因为(其可能为file://,content://   scheme)
 *
 */
public class MediaItem implements Parcelable {
    public static final int PHOTO = 1;
    public static final int VIDEO = 2;
    private int type;
    private Uri uriOrigin;
    private String path;


    /**
     * @param mediaType Whether {@link #PHOTO} or {@link #VIDEO}
     * @param uriOrigin {@link Uri} of media item.
     */
    public MediaItem(int mediaType, Uri uriOrigin, String path) {
        this.type = mediaType;
        this.uriOrigin = uriOrigin;
        this.path = path;
    }

    /**
     * @return type of media item. Whether {@link #PHOTO} or {@link #VIDEO}
     */
    public int getType() {
        return type;
    }

    /**
     * Set type of media.
     *
     * @param type is {@link #PHOTO} or {@link #VIDEO}
     */
    public void setType(int type) {
        this.type = type;
    }

    public Uri getUriOrigin() {
        return uriOrigin;
    }


    public void setUriOrigin(Uri uriOrigin) {
        this.uriOrigin = uriOrigin;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isVideo() {
        return type == VIDEO;
    }

    public boolean isPhoto() {
        return type == PHOTO;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        if (this.uriOrigin == null) {
            dest.writeString(null);
        } else {
            dest.writeString(this.uriOrigin.toString());
        }
        if (this.path == null) {
            dest.writeString(null);
        } else {
            dest.writeString(this.path.toString());
        }
    }

    public MediaItem(Parcel in) {
        this.type = in.readInt();
        String origin = in.readString();
        if (!TextUtils.isEmpty(origin))
            this.uriOrigin = Uri.parse(origin);
        String path = in.readString();
        if (!TextUtils.isEmpty(path))
            this.path = path;
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }

        @Override
        public MediaItem createFromParcel(Parcel source) {
            return new MediaItem(source);
        }
    };

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result
                + ((uriOrigin == null) ? 0 : uriOrigin.hashCode());
        result = prime * result
                + ((path == null) ? 0 : path.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MediaItem other = (MediaItem) obj;
        if (path == null) {
            if (other.path != null)
                return false;
        } else if (!path.equals(other.path))
            return false;
//        if (uriOrigin == null) {
//            if (other.uriOrigin != null)
//                return false;
//        } else if (!uriOrigin.equals(other.uriOrigin))
//            return false;
        return true;
    }

    @Override
    public String toString() {
        return "MediaItem [type=" + type + ", path=" + path
                + ", uriOrigin=" + uriOrigin + "]";
    }

    /**
     * @param context
     * @return Path of origin file.
     */
    public String getPathOrigin(Context context) {
        return getPathFromUri(context, uriOrigin);
    }

    /**
     * @param context
     * @return Path of cropped file.
     */

    private String getPathFromUri(Context context, Uri uri) {
        if (uri == null)
            return null;
        String scheme = uri.getScheme();
        if (scheme.equals(ContentResolver.SCHEME_FILE)) {
            return uri.getPath();
        } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            if (isPhoto()) {
                return MediaUtils.getRealImagePathFromURI(
                        context.getContentResolver(), uri);
            } else {
                return MediaUtils.getRealVideoPathFromURI(
                        context.getContentResolver(), uri);
            }
        }
        return uri.toString();
    }
}