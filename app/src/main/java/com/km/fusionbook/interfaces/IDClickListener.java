package com.km.fusionbook.interfaces;

import android.view.View;

/**
 * Interface usually used as an action after clicking on a list item identified by a String ID
 */
public interface IDClickListener {
    void onClick(View v, String id, int position);
}
