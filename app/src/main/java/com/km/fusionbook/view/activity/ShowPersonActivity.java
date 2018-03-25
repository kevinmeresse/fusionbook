package com.km.fusionbook.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.client.Firebase;
import com.km.fusionbook.R;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.util.ImageUtils;
import com.km.fusionbook.util.PermissionUtils;
import com.km.fusionbook.util.Utils;
import com.km.fusionbook.view.customviews.GlideCircleTransform;
import com.km.fusionbook.view.customviews.YesNoDialog;

import java.text.DateFormat;

import io.realm.Realm;

public class ShowPersonActivity extends AppCompatActivity {

    // Class data
    private String personId;
    private Person person;
    private boolean emptyScreen = false;
    private String clickedPhoneNumber;

    // Realm reference
    private Realm realm;

    // Layout views
    private TextView birthdate, mobilePhone, workPhone, email, address,
            mobilePhoneLabel, emailLabel, mobilePhoneAction, emailAction;
    private View mobilePhoneLayout, workPhoneLayout, phoneDividerLayout;
    private CardView phoneCard, emailCard, addressCard;
    private ImageView picture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set up transitions for Lollipop and after
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                getWindow().setEnterTransition(new Slide(Gravity.END));
                getWindow().setReturnTransition(new Slide(Gravity.END));
            } catch (Exception e) {
                // Do nothing
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_person);

        // Set up toolbar
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
        birthdate = (TextView) findViewById(R.id.details_birthdate);
        mobilePhone = (TextView) findViewById(R.id.details_phone_mobile);
        workPhone = (TextView) findViewById(R.id.details_phone_work);
        email = (TextView) findViewById(R.id.details_email);
        address = (TextView) findViewById(R.id.details_address);
        mobilePhoneLayout = findViewById(R.id.details_layout_phone_mobile);
        workPhoneLayout = findViewById(R.id.details_layout_phone_work);
        View emailLayout = findViewById(R.id.details_layout_email);
        View addressLayout = findViewById(R.id.details_layout_address);
        phoneDividerLayout = findViewById(R.id.details_phone_divider);
        mobilePhoneLabel = (TextView) findViewById(R.id.label_mobile);
        emailLabel = (TextView) findViewById(R.id.label_email);
        mobilePhoneAction = (TextView) findViewById(R.id.details_phone_mobile_action_label);
        emailAction = (TextView) findViewById(R.id.details_email_action_label);
        phoneCard = (CardView) findViewById(R.id.card_phone);
        emailCard = (CardView) findViewById(R.id.card_email);
        addressCard = (CardView) findViewById(R.id.card_address);
        picture = (ImageView) findViewById(R.id.details_picture);
        final Button editButton = (Button) findViewById(R.id.edit_button);
        Button deleteButton = (Button) findViewById(R.id.delete_button);

        // Retrieve person's ID
        Intent intent = getIntent();
        personId = intent.getStringExtra(Person.EXTRA_PERSON_ID);

        // ACTIONS
        // Edit
        editButton.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(ShowPersonActivity.this);
                Intent intent = new Intent(ShowPersonActivity.this, EditPersonActivity.class);
                intent.putExtra(Person.EXTRA_PERSON_ID, personId);
                ActivityCompat.startActivity(ShowPersonActivity.this, intent, options.toBundle());
            }
        });

        // Delete
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YesNoDialog dialog = YesNoDialog.newInstance(
                        R.string.dialog_delete_title,
                        R.string.dialog_delete_message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // If logged in
                                Firebase rootRef = new Firebase(getResources().getString(R.string.firebase_url));
                                if (rootRef.getAuth() != null) {
                                    // Delete person from Firebase
                                    rootRef.child("persons").child(rootRef.getAuth().getUid()).child(person.getId()).removeValue();
                                    // Delete picture from server
                                    String pictureId = rootRef.getAuth().getUid() + "-" + person.getId();
                                    ImageUtils.delete(ShowPersonActivity.this, pictureId);
                                }

                                // Delete person from Realm
                                realm.executeTransaction(new Realm.Transaction() {
                                    @Override
                                    public void execute(Realm realm) {
                                        person.removeFromRealm();
                                    }
                                });

                                // Go back to home screen
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    finishAfterTransition();
                                } else {
                                    finish();
                                }
                            }
                        });
                dialog.show(getSupportFragmentManager(), "dialog");
            }
        });

        // Phones
        mobilePhoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emptyScreen) {
                    editButton.callOnClick();
                } else {
                    tryToCall(mobilePhone.getText().toString());
                }
            }
        });
        workPhoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryToCall(workPhone.getText().toString());
            }
        });

        // Email
        emailLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (emptyScreen) {
                    editButton.callOnClick();
                } else {
                    String[] addresses = {email.getText().toString()};
                    Utils.writeEmail(ShowPersonActivity.this, addresses);
                }
            }
        });

        // Address
        addressLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.seeLocationOnMaps(ShowPersonActivity.this, address.getText().toString());
            }
        });
    }

    private void tryToCall(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber)) {

            // Save number to automatically call when permission is granted
            clickedPhoneNumber = phoneNumber;

            // Check permission
            if (PermissionUtils.isPhoneCallGranted(this)) {
                Utils.callPhoneNumber(this, clickedPhoneNumber);
            } else {
                PermissionUtils.requestPhoneCallPermission(this);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Upgrade person's details
        try {
            person = realm
                    .where(Person.class)
                    .equalTo("id", personId)
                    .findFirst();
        } catch (Exception e) {
            Snackbar.make(birthdate, "Unable to load data...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        if (person != null) {
            // NAME
            String fullname = person.getFirstname() + " " + person.getLastname();
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(fullname);
            }
            // PICTURE
            if (!TextUtils.isEmpty(person.getPictureUrl())) {
                Glide.with(this)
                        .load(person.getPictureUrl())
                        .transform(new GlideCircleTransform(this))
                        .into(picture);
            } else {
                picture.setImageDrawable(null);
            }
            // BIRTHDATE
            if (person.getBirthdate() != 0) {
                birthdate.setText(DateFormat.getDateInstance().format(person.getBirthdate()));
                birthdate.setVisibility(View.VISIBLE);
            } else {
                birthdate.setVisibility(View.GONE);
            }
            // PHONES
            int phoneCount = 0;
            phoneDividerLayout.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(person.getMobilePhone())) {
                phoneCount++;
                mobilePhone.setText(person.getMobilePhone());
                mobilePhoneLayout.setVisibility(View.VISIBLE);
                mobilePhoneLabel.setVisibility(View.VISIBLE);
                mobilePhoneAction.setVisibility(View.VISIBLE);
            } else {
                mobilePhoneLayout.setVisibility(View.GONE);
                phoneDividerLayout.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(person.getWorkPhone())) {
                phoneCount++;
                workPhone.setText(person.getWorkPhone());
                workPhoneLayout.setVisibility(View.VISIBLE);
            } else {
                workPhoneLayout.setVisibility(View.GONE);
                phoneDividerLayout.setVisibility(View.GONE);
            }
            phoneCard.setVisibility(phoneCount > 0 ? View.VISIBLE : View.GONE);
            // EMAIL
            int emailCount = 0;
            if (!TextUtils.isEmpty(person.getEmail())) {
                email.setText(person.getEmail());
                emailCard.setVisibility(View.VISIBLE);
                emailLabel.setVisibility(View.VISIBLE);
                emailAction.setVisibility(View.VISIBLE);
                emailCount++;
            } else {
                emailCard.setVisibility(View.GONE);
            }
            // ADDRESS
            int addressCount = 0;
            StringBuilder addressString = new StringBuilder();
            if (!TextUtils.isEmpty(person.getAddressStreet())) {
                addressString.append(person.getAddressStreet()).append(" \n");
            }
            if (!TextUtils.isEmpty(person.getAddressCity())) {
                addressString.append(person.getAddressCity()).append(", ");
            }
            if (!TextUtils.isEmpty(person.getAddressState())) {
                addressString.append(person.getAddressState()).append(" \n");
            } else if (!TextUtils.isEmpty(person.getAddressCity())) {
                if (addressString.length() >= 2) {
                    addressString.delete(addressString.length() - 2, addressString.length());
                    addressString.append(" \n");
                }
            }
            if (!TextUtils.isEmpty(person.getAddressZipcode())) {
                addressString.append(person.getAddressZipcode()).append(", ");
            }
            if (!TextUtils.isEmpty(person.getAddressCountry())) {
                addressString.append(person.getAddressCountry());
            } else {
                if (addressString.length() >= 2) {
                    addressString.delete(addressString.length() - 2, addressString.length());
                }
            }
            if (!TextUtils.isEmpty(addressString)) {
                address.setText(addressString);
                addressCard.setVisibility(View.VISIBLE);
                addressCount++;
            } else {
                addressCard.setVisibility(View.GONE);
            }

            // Set up empty screen
            emptyScreen = (phoneCount == 0 && emailCount == 0 && addressCount == 0);
            if (emptyScreen) {
                // Add phone number
                mobilePhoneLayout.setVisibility(View.VISIBLE);
                mobilePhoneLabel.setVisibility(View.GONE);
                mobilePhoneAction.setVisibility(View.GONE);
                mobilePhone.setText(R.string.add_phone_number);
                phoneCard.setVisibility(View.VISIBLE);

                // Add email
                emailLabel.setVisibility(View.GONE);
                emailAction.setVisibility(View.GONE);
                email.setText(R.string.add_email);
                emailCard.setVisibility(View.VISIBLE);
            }
        }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case PermissionUtils.PERMISSION_REQUEST_CALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && !TextUtils.isEmpty(clickedPhoneNumber)) {
                    Utils.callPhoneNumber(ShowPersonActivity.this, clickedPhoneNumber);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
