package com.km.fusionbook.view.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.km.fusionbook.R;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.view.customviews.YesNoDialog;

import java.text.DateFormat;

import io.realm.Realm;

public class ShowPersonActivity extends AppCompatActivity {

    private Realm realm;
    private String personId;
    private Person person;

    private TextView birthdate;
    private TextView zipcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        zipcode = (TextView) findViewById(R.id.details_zipcode);
        Button editButton = (Button) findViewById(R.id.edit_button);
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
                                // Delete person from Firebase (if logged in)
                                Firebase rootRef = new Firebase(getResources().getString(R.string.firebase_url));
                                if (rootRef.getAuth() != null) {
                                    rootRef.child("persons").child(rootRef.getAuth().getUid()).child(person.getId()).removeValue();
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
            birthdate.setText(DateFormat.getDateInstance().format(person.getBirthdate()));
            zipcode.setText(person.getZipcode());
            String fullname = person.getFirstname() + " " + person.getLastname();
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(fullname);
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
}
