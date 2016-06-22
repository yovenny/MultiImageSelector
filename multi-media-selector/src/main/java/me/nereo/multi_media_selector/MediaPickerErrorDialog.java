package me.nereo.multi_media_selector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * @author TUNGDX
 */

public class MediaPickerErrorDialog extends DialogFragment {
    private String mMessage;
    private OnClickListener mOnPositionClickListener;

    public static MediaPickerErrorDialog newInstance(String msg) {
        MediaPickerErrorDialog dialog = new MediaPickerErrorDialog();
        Bundle bundle = new Bundle();
        bundle.putString("msg", msg);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessage = getArguments().getString("msg");
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setMessage(mMessage)
                .setPositiveButton(R.string.ok, mOnPositionClickListener)
                .create();
    }

    public void setOnOKClickListener(OnClickListener mOnClickListener) {
        this.mOnPositionClickListener = mOnClickListener;
    }
}