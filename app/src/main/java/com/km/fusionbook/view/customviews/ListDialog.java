package com.km.fusionbook.view.customviews;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * A simple dialog showing a list of items
 */
public class ListDialog extends DialogFragment {

    private DialogInterface.OnClickListener itemListener;

    public static ListDialog newInstance(int itemsId, DialogInterface.OnClickListener itemListener) {
        ListDialog fragment = new ListDialog();
        Bundle args = new Bundle();
        args.putInt("itemsId", itemsId);
        fragment.setArguments(args);
        fragment.setItemListener(itemListener);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int itemsId = getArguments().getInt("itemsId");

        return new AlertDialog.Builder(getActivity())
                .setItems(itemsId, itemListener)
                .create();
    }

    public void setItemListener(DialogInterface.OnClickListener itemListener) {
        this.itemListener = itemListener;
    }
}
