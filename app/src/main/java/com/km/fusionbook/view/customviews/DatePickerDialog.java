package com.km.fusionbook.view.customviews;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by kevin on 2/2/16.
 */
public class DatePickerDialog  extends DialogFragment {

    private android.app.DatePickerDialog.OnDateSetListener listener;
    private Date defaultDate;

    public static DatePickerDialog newInstance(Date defaultDate, android.app.DatePickerDialog.OnDateSetListener listener) {
        DatePickerDialog fragment = new DatePickerDialog();
        fragment.setDefaultDate(defaultDate);
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();

        // Use specified date as the default values for the picker
        if (defaultDate != null) {
            c.setTime(defaultDate);
        }
        // Otherwise use the current date
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);


        // Create a new instance of DatePickerDialog and return it
        return new android.app.DatePickerDialog(getActivity(), listener, year, month, day);
    }

    public void setListener(android.app.DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    public void setDefaultDate(Date date) {
        this.defaultDate = date;
    }
}