package com.km.fusionbook;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.firebase.client.Firebase;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Facebook SDK
        FacebookSdk.sdkInitialize(this);

        // Initialize Firebase SDK
        Firebase.setAndroidContext(this);

        // Configure Realm database
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name("fusionbook.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}