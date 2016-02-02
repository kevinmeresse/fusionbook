package com.km.fusionbook;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Configure Realm database
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name("fusionbook.realm")
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }
}