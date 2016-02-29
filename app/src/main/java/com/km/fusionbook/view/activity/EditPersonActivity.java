package com.km.fusionbook.view.activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.km.fusionbook.R;
import com.km.fusionbook.interfaces.StringCallback;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.util.ImageUtils;
import com.km.fusionbook.view.customviews.DatePickerDialog;
import com.km.fusionbook.view.customviews.GlideCircleTransform;
import com.km.fusionbook.view.customviews.ListDialog;
import com.km.fusionbook.view.customviews.YesNoDialog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import io.realm.Realm;

public class EditPersonActivity extends AppCompatActivity {

    // Activity result codes
    private static final int REQUEST_IMAGE_PICK = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_CROP = 3;

    // Class data
    private Person person;
    private boolean isLoading = false;
    private Uri photoUri = null;

    // Layout views
    private EditText inputFirstname, inputLastname, inputBirthdate, inputMobilePhone, inputCountry,
            inputWorkPhone, inputEmail, inputStreet, inputCity, inputState, inputZipcode;
    private TextInputLayout inputLayoutFirstname, inputLayoutLastname,
            inputLayoutBirthdate, inputLayoutEmail, inputLayoutZipcode;
    private ImageView picture, editPicture;
    private ProgressBar pictureProgress;

    // Utils
    private Pattern zipcodePattern, emailPattern;

    // Realm reference
    private Realm realm;

    // Firebase reference
    private Firebase firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set up transitions for Lollipop and after
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

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        // Get Firebase reference
        firebaseRef = new Firebase(getResources().getString(R.string.firebase_url));

        // Get the default realm
        realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Date now = new Date();

        // Retrieve person's ID
        Intent intent = getIntent();
        String personId = intent.getStringExtra(Person.EXTRA_PERSON_ID);

        // Get person object from Realm
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
        picture = (ImageView) findViewById(R.id.details_picture);
        inputLayoutBirthdate = (TextInputLayout) findViewById(R.id.input_layout_birthdate);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.input_layout_email);
        inputLayoutZipcode = (TextInputLayout) findViewById(R.id.input_layout_zipcode);
        inputFirstname = (EditText) findViewById(R.id.input_firstname);
        inputLastname = (EditText) findViewById(R.id.input_lastname);
        inputBirthdate = (EditText) findViewById(R.id.input_birthdate);
        inputEmail = (EditText) findViewById(R.id.input_email);
        inputMobilePhone = (EditText) findViewById(R.id.input_phone_mobile);
        inputWorkPhone = (EditText) findViewById(R.id.input_phone_work);
        inputStreet = (EditText) findViewById(R.id.input_street);
        inputCity = (EditText) findViewById(R.id.input_city);
        inputState = (EditText) findViewById(R.id.input_state);
        inputZipcode = (EditText) findViewById(R.id.input_zipcode);
        inputCountry = (EditText) findViewById(R.id.input_country);
        pictureProgress = (ProgressBar) findViewById(R.id.picture_progress);
        editPicture = (ImageView) findViewById(R.id.picture_edit);
        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        Button saveButton = (Button) findViewById(R.id.save_button);

        // Fill in form fields if person already exists
        if (person != null) {
            inputFirstname.setText(person.getFirstname());
            inputLastname.setText(person.getLastname());
            if (!TextUtils.isEmpty(person.getPictureUrl())) {
                Glide.with(this)
                        .load(person.getPictureUrl())
                        .transform(new GlideCircleTransform(this))
                        .into(picture);
            }
            if (person.getBirthdate() != 0) {
                inputBirthdate.setText(DateFormat.getDateInstance().format(person.getBirthdate()));
            }
            inputEmail.setText(person.getEmail());
            inputMobilePhone.setText(person.getMobilePhone());
            inputWorkPhone.setText(person.getWorkPhone());
            inputStreet.setText(person.getAddressStreet());
            inputCity.setText(person.getAddressCity());
            inputState.setText(person.getAddressState());
            inputZipcode.setText(person.getAddressZipcode());
            inputCountry.setText(person.getAddressCountry());
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
        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputZipcode.addTextChangedListener(new MyTextWatcher(inputZipcode));

        // Initialize patterns for validation
        zipcodePattern = Pattern.compile("^[0-9]{5}(?:-[0-9]{4})?$");
        emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

        // ACTIONS
        // Edit picture
        picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Edit picture only if logged in as it needs a unique Id to save picture in the cloud
                if (!isLoading && firebaseRef.getAuth() != null) {
                    ListDialog dialog = ListDialog.newInstance(
                            TextUtils.isEmpty(person.getPictureUrl()) ? R.array.choose_picture : R.array.change_picture,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0: // Take photo
                                            try {
                                                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                                                    // Create the File where the photo should go
                                                    File photoFile = ImageUtils.createImageFile();
                                                    photoUri = Uri.fromFile(photoFile);
                                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                                                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                                                }
                                            } catch (ActivityNotFoundException e) {
                                                // No camera app found
                                                Snackbar.make(picture, R.string.dialog_error_capture_image, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            } catch (IOException ex) {
                                                // Error occurred while creating the File
                                                Snackbar.make(picture, R.string.dialog_error_create_file, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                            break;
                                        case 1: // Choose picture
                                            try {
                                                Intent intent = new Intent();
                                                // Show only images, no videos or anything else
                                                intent.setType("image/*");
                                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                                // Always show the chooser (if there are multiple options available)
                                                startActivityForResult(
                                                        Intent.createChooser(intent, getString(R.string.select_picture)),
                                                        REQUEST_IMAGE_PICK);
                                            } catch (ActivityNotFoundException e) {
                                                // No gallery app found
                                                Snackbar.make(picture, R.string.dialog_error_select_image, Snackbar.LENGTH_LONG)
                                                        .setAction("Action", null).show();
                                            }
                                            break;
                                        default: // Remove picture
                                            picture.setImageDrawable(null);
                                            person.setPictureUrl(null);
                                            // Delete picture from server
                                            String pictureId = firebaseRef.getAuth().getUid() + "-" + person.getId();
                                            ImageUtils.delete(EditPersonActivity.this, pictureId);
                                            break;
                                    }
                                }
                            });
                    dialog.show(getSupportFragmentManager(), "dialog");
                } else {
                    YesNoDialog dialog = YesNoDialog.newInstance(
                            R.string.dialog_edit_picture_title,
                            R.string.dialog_edit_picture_message,
                            R.string.nav_login,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Quit activity and return to Login screen
                                    Intent intent = new Intent(EditPersonActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            });
                    dialog.show(getSupportFragmentManager(), "dialog");
                }
            }
        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            photoUri = data.getData();
            cropImageAndUpload(photoUri);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK
                && photoUri != null) {
            cropImageAndUpload(photoUri);
        } else if (requestCode == REQUEST_IMAGE_CROP && resultCode == RESULT_OK
                && photoUri != null) {
            startImageUpload(photoUri);
        }
    }

    /**
     * Crops image and upload to server
     * @param imageUri URI of image
     */
    private void cropImageAndUpload(Uri imageUri) {
        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(imageUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 512);
            cropIntent.putExtra("outputY", 512);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
        } catch (ActivityNotFoundException e) {
            // Display an error message
            Snackbar.make(picture, R.string.dialog_error_crop_image, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            // Upload uncropped image
            startImageUpload(imageUri);
        }
    }

    /**
     * Uploads image to server
     * @param imageUri URI of image to upload
     */
    private void startImageUpload(Uri imageUri) {
        if (firebaseRef != null && firebaseRef.getAuth() != null
                && person != null && imageUri != null) {
            try {
                loadingPicture(true);
                String pictureId = firebaseRef.getAuth().getUid() + "-" + person.getId();
                InputStream in = getContentResolver().openInputStream(imageUri);
                ImageUtils.upload(this, in, pictureId, new StringCallback() {
                    @Override
                    public void done(String result, Exception e) {
                        updatePicture(result);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
                loadingPicture(false);
                Snackbar.make(picture, R.string.dialog_connection_upload_message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else {
            Snackbar.make(picture, R.string.dialog_connection_upload_message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    /**
     * Update picture view
     * @param url URL of picture to display
     */
    private void updatePicture(final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (url != null) {
                    person.setPictureUrl(url);
                    Glide.with(EditPersonActivity.this)
                            .load(url)
                            .transform(new GlideCircleTransform(EditPersonActivity.this))
                            .into(picture);
                } else {
                    Snackbar.make(picture, R.string.dialog_connection_picture_message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                loadingPicture(false);
            }
        });
    }

    /**
     * Show or hide a progress bar for loading picture
     * @param isLoading True if picture is loading
     */
    private void loadingPicture(boolean isLoading) {
        this.isLoading = isLoading;
        pictureProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        editPicture.setVisibility(!isLoading ? View.VISIBLE : View.GONE);

    }

    /**
     * Validates the firstname format
     * @return True if field is valid
     */
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

    /**
     * Validates the lastname format
     * @return True if field is valid
     */
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

    /**
     * Validates the birthdate format
     * @return True if field is valid
     */
    private boolean validateBirthdate() {
        if (person.getBirthdate() > (new Date()).getTime()) {
            inputLayoutBirthdate.setError(getString(R.string.err_msg_birthdate_past));
            return false;
        } else {
            inputLayoutBirthdate.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Validates the zipcode format
     * @return True if field is valid
     */
    private boolean validateZipcode() {
        String content = inputZipcode.getText().toString().trim();
        if (!content.isEmpty() && !zipcodePattern.matcher(content).matches()) {
            inputLayoutZipcode.setError(getString(R.string.err_msg_zipcode_format));
            requestFocus(inputZipcode);
            return false;
        } else {
            inputLayoutZipcode.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Validates the email format
     * @return True if field is valid
     */
    private boolean validateEmail() {
        String content = inputEmail.getText().toString().trim();
        if (!content.isEmpty() && !emailPattern.matcher(content).matches()) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email_format));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Tries to focus on a specific view in the layout
     * @param view The view to focus to
     */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    /**
     * Submits the form if all the fields are valid
     */
    private void submitForm() {
        // Validate form
        if (!validateFirstname() || !validateLastname() || !validateEmail()
                || !validateBirthdate() || !validateZipcode()) {
            return;
        }

        // Update person object
        person.setFirstname(inputFirstname.getText().toString().trim());
        person.setLastname(inputLastname.getText().toString().trim());
        person.setEmail(inputEmail.getText().toString().trim());
        person.setMobilePhone(inputMobilePhone.getText().toString().trim());
        person.setWorkPhone(inputWorkPhone.getText().toString().trim());
        person.setAddressStreet(inputStreet.getText().toString().trim());
        person.setAddressCity(inputCity.getText().toString().trim());
        person.setAddressState(inputState.getText().toString().trim());
        person.setAddressZipcode(inputZipcode.getText().toString().trim());
        person.setAddressCountry(inputCountry.getText().toString().trim());
        person.setModifiedAt((new Date()).getTime());

        // Update person on Firebase (if logged in)
        if (firebaseRef.getAuth() != null) {
            Firebase personRef =
                    firebaseRef.child("persons").child(firebaseRef.getAuth().getUid()).child(person.getId());
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

    /**
     * When this object is attached to an Editable, its methods will
     * be called when the text is changed
     */
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
                case R.id.input_email:
                    validateEmail();
                    break;
                case R.id.input_zipcode:
                    validateZipcode();
                    break;
            }
        }
    }
}
