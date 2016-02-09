package com.km.fusionbook.view.activity;

import android.content.Intent;
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

import com.firebase.client.Firebase;
import com.km.fusionbook.R;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.view.customviews.DatePickerDialog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import io.realm.Realm;

public class EditPersonActivity extends AppCompatActivity {

    private EditText inputFirstname, inputLastname, inputBirthdate, inputZipcode;
    private TextInputLayout inputLayoutFirstname, inputLayoutLastname,
            inputLayoutBirthdate, inputLayoutZipcode;
    private Pattern zipcodePattern;
    private Realm realm;
    private Person person;

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
        realm.beginTransaction();
        Date now = new Date();

        // Retrieve person's ID
        Intent intent = getIntent();
        String personId = intent.getStringExtra(Person.EXTRA_PERSON_ID);

        try {
            person = realm
                    .where(Person.class)
                    .equalTo("id", personId)
                    .findFirst();
        } catch (Exception e) {
            // Unable to query local DB
            // Do nothing
        }

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

        // Fill in form fields if person already exists
        if (person != null) {
            inputFirstname.setText(person.getFirstname());
            inputLastname.setText(person.getLastname());
            inputBirthdate.setText(DateFormat.getDateInstance().format(person.getBirthdate()));
            inputZipcode.setText(person.getZipcode());
        } else {
            if (actionBar != null) {
                actionBar.setTitle(R.string.add_person);
            }
            person = realm.createObject(Person.class);
            person.setId(Long.toString(now.getTime()));
            person.setCreatedAt(now.getTime());
        }

        // Listen to text edition
        inputFirstname.addTextChangedListener(new MyTextWatcher(inputFirstname));
        inputLastname.addTextChangedListener(new MyTextWatcher(inputLastname));
        inputBirthdate.addTextChangedListener(new MyTextWatcher(inputBirthdate));
        inputZipcode.addTextChangedListener(new MyTextWatcher(inputZipcode));

        // Initialize pattern for validating zip-codes
        zipcodePattern = Pattern.compile("^[0-9]{5}(?:-[0-9]{4})?$");

        // ACTIONS
        // Click on birthdate field
        inputBirthdate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
                if (hasFocus) {
                    DatePickerDialog datePickerDialog =
                            DatePickerDialog.newInstance(person.getBirthdate(),
                                    new android.app.DatePickerDialog.OnDateSetListener() {

                                        @Override
                                        public void onDateSet(DatePicker view, int year, int month, int day) {
                                            Calendar cal = Calendar.getInstance();
                                            cal.set(year, month, day);
                                            person.setBirthdate(cal.getTime().getTime());
                                            inputBirthdate.setText(DateFormat.getDateInstance().format(person.getBirthdate()));
                                        }
                                    });
                    datePickerDialog.show(EditPersonActivity.this.getSupportFragmentManager(),
                            "datePicker");
                }
            }
        });

        // Cancel
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                realm.cancelTransaction();
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
            try {
                realm.cancelTransaction();
            } catch (IllegalStateException e) {
                // Transaction was already committed/cancelled
                // Do nothing
            }
            realm.close();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            realm.cancelTransaction();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        realm.cancelTransaction();
        super.onBackPressed();
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
        } else if (person.getBirthdate() > (new Date()).getTime()) {
            inputLayoutBirthdate.setError(getString(R.string.err_msg_birthdate_past));
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

    private void submitForm() {
        // Validate form
        if (!validateFirstname() || !validateLastname()
                || !validateBirthdate() || !validateZipcode()) {
            return;
        }

        // Update person object
        person.setFirstname(inputFirstname.getText().toString().trim());
        person.setLastname(inputLastname.getText().toString().trim());
        person.setZipcode(inputZipcode.getText().toString().trim());
        person.setModifiedAt((new Date()).getTime());

        // Update person on Firebase (if logged in)
        Firebase rootRef = new Firebase(getResources().getString(R.string.firebase_url));
        if (rootRef.getAuth() != null) {
            Firebase personRef =
                    rootRef.child("persons").child(rootRef.getAuth().getUid()).child(person.getId());
            personRef.setValue(person);
        }


        // Commit changes to Realm
        realm.commitTransaction();

        // Go back to home screen
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
