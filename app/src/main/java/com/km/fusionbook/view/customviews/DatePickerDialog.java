package com.km.fusionbook.view.customviews;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;

public class DatePickerDialog  extends DialogFragment {

    private android.app.DatePickerDialog.OnDateSetListener listener;
    private long defaultDate;

    public static DatePickerDialog newInstance(long defaultDate, android.app.DatePickerDialog.OnDateSetListener listener) {
        DatePickerDialog fragment = new DatePickerDialog();
        fragment.setDefaultDate(defaultDate);
        fragment.setListener(listener);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();

        // Use specified date as the default values for the picker
        if (defaultDate != 0) {
            c.setTime(new Date(defaultDate));
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

    public void setDefaultDate(long date) {
        this.defaultDate = date;
    }
}