package com.km.fusionbook.view.activity;

import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.km.fusionbook.R;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.view.customviews.DatePickerDialog;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import io.realm.Realm;

public class EditPersonActivity extends AppCompatActivity {

    private EditText inputFirstname, inputLastname, inputBirthdate, inputZipcode;
    private TextInputLayout inputLayoutFirstname, inputLayoutLastname,
            inputLayoutBirthdate, inputLayoutZipcode;
    private Pattern zipcodePattern;
    private Date birthdate;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
                getWindow().setReturnTransition(new Slide(Gravity.BOTTOM));
            } catch (Exception e) {
                // Do nothing
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_person);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Get the default realm
        realm = Realm.getDefaultInstance();

        // Retrieve views
        inputLayoutFirstname = (TextInputLayout) findViewById(R.id.input_layout_firstname);
        inputLayoutLastname = (TextInputLayout) findViewById(R.id.input_layout_lastname);
        inputLayoutBirthdate = (TextInputLayout) findViewById(R.id.input_layout_birthdate);
        inputLayoutZipcode = (TextInputLayout) findViewById(R.id.input_layout_zipcode);
        inputFirstname = (EditText) findViewById(R.id.input_firstname);
        inputLastname = (EditText) findViewById(R.id.input_lastname);
        inputBirthdate = (EditText) findViewById(R.id.input_birthdate);
        inputZipcode = (EditText) findViewById(R.id.input_zipcode);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        Button saveButton = (Button) findViewById(R.id.save_button);

        // Listen to text edition
        inputFirstname.addTextChangedListener(new MyTextWatcher(inputFirstname));
        inputLastname.addTextChangedListener(new MyTextWatcher(inputLastname));
        inputBirthdate.addTextChangedListener(new MyTextWatcher(inputBirthdate));
        inputZipcode.addTextChangedListener(new MyTextWatcher(inputZipcode));

        // Initialize pattern for validating zip-codes
        zipcodePattern = Pattern.compile("^[0-9]{5}(?:-[0-9]{4})?$");

        // ACTIONS
        // Click on birthdate field
        inputBirthdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(birthdate, new android.app.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        String birth = (monthOfYear + 1) + "/" + dayOfMonth + "/" + year;
                        inputBirthdate.setText(birth);
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, monthOfYear, dayOfMonth);
                        birthdate = cal.getTime();
                    }
                });
                datePickerDialog.show(EditPersonActivity.this.getSupportFragmentManager(), "datePicker");
            }
        });

        // Cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
            }
        });

        // Save
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (realm != null) {
            realm.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validateFirstname() {
        if (inputFirstname.getText().toString().trim().isEmpty()) {
            inputLayoutFirstname.setError(getString(R.string.err_msg_firstname));
            requestFocus(inputFirstname);
            return false;
        } else {
            inputLayoutFirstname.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateLastname() {
        if (inputLastname.getText().toString().trim().isEmpty()) {
            inputLayoutLastname.setError(getString(R.string.err_msg_lastname));
            requestFocus(inputLastname);
            return false;
        } else {
            inputLayoutLastname.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateBirthdate() {
        if (inputBirthdate.getText().toString().trim().isEmpty()) {
            inputLayoutBirthdate.setError(getString(R.string.err_msg_birthdate));
            requestFocus(inputBirthdate);
            return false;
        } else {
            inputLayoutBirthdate.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateZipcode() {
        if (inputZipcode.getText().toString().trim().isEmpty()) {
            inputLayoutZipcode.setError(getString(R.string.err_msg_zipcode));
            requestFocus(inputZipcode);
            return false;
        } else if (!zipcodePattern.matcher(inputZipcode.getText().toString().trim()).matches()) {
            inputLayoutZipcode.setError(getString(R.string.err_msg_zipcode_format));
            requestFocus(inputZipcode);
            return false;
        } else {
            inputLayoutZipcode.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    // Validating form
    private void submitForm() {
        if (!validateFirstname() || !validateLastname()
                || !validateBirthdate() || !validateZipcode()) {
            return;
        }

        // TODO: Send request to server

        // Create person in Realm
        realm.beginTransaction();
        Person person = realm.createObject(Person.class);
        person.setFirstname(inputFirstname.getText().toString().trim());
        person.setLastname(inputLastname.getText().toString().trim());
        person.setBirthdate(birthdate);
        person.setZipcode(inputZipcode.getText().toString().trim());
        realm.cancelTransaction();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        } else {
            finish();
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_firstname:
                    validateFirstname();
                    break;
                case R.id.input_lastname:
                    validateLastname();
                    break;
                case R.id.input_birthdate:
                    validateBirthdate();
                    break;
                case R.id.input_zipcode:
                    validateZipcode();
                    break;
            }
        }
    }
}
