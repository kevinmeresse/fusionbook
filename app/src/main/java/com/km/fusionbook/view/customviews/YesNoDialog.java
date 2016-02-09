package com.km.fusionbook.view.customviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.km.fusionbook.R;

public class YesNoDialog extends DialogFragment {

    private DialogInterface.OnClickListener positiveListener;
    private DialogInterface.OnClickListener negativeListener;

    public static YesNoDialog newInstance(int title, int message,
                                          DialogInterface.OnClickListener positiveListener) {
        return newInstance(title, message, R.string.dialog_ok, R.string.dialog_cancel,
                positiveListener, null);
    }

    public static YesNoDialog newInstance(int title, int message, int positiveText,
                                          DialogInterface.OnClickListener positiveListener) {
        return newInstance(title, message, positiveText, R.string.dialog_cancel,
                positiveListener, null);
    }

    public static YesNoDialog newInstance(int title, int message, int positiveText, int negativeText,
                                          DialogInterface.OnClickListener positiveListener,
                                          DialogInterface.OnClickListener negativeListener) {
        YesNoDialog fragment = new YesNoDialog();
        Bundle args = new Bundle();
        args.putInt("title", title);
        args.putInt("message", message);
        args.putInt("positiveText", positiveText);
        args.putInt("negativeText", negativeText);
        fragment.setArguments(args);
        fragment.setPositiveListener(positiveListener);
        fragment.setNegativeListener(negativeListener);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int title = getArguments().getInt("title");
        int message = getArguments().getInt("message");
        int positiveText = getArguments().getInt("positiveText");
        int negativeText = getArguments().getInt("negativeText");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveText, positiveListener)
                .setNegativeButton(negativeText, negativeListener)
                .create();
    }

    public void setPositiveListener(DialogInterface.OnClickListener positiveListener) {
        this.positiveListener = positiveListener;
    }

    public void setNegativeListener(DialogInterface.OnClickListener negativeListener) {
        this.negativeListener = negativeListener;
    }
}
