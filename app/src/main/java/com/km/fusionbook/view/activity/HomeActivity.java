package com.km.fusionbook.view.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.km.fusionbook.R;
import com.km.fusionbook.interfaces.IDClickListener;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.view.adapter.PersonAdapter;
import com.km.fusionbook.view.adapter.RealmModelAdapter;
import com.km.fusionbook.view.customviews.GlideCircleTransform;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private PersonAdapter adapter;
    private View emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Realm reference
    private Realm realm;
    private RealmResults<Person> result;

    // Firebase reference
    private Firebase firebaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get Firebase reference
        firebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
        AuthData authData = firebaseRef.getAuth();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View view) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeActivity.this);
                Intent intent = new Intent(HomeActivity.this, EditPersonActivity.class);
                ActivityCompat.startActivity(HomeActivity.this, intent, options.toBundle());
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Setup Navigation View
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().findItem(R.id.nav_login).setVisible(authData == null);
        navigationView.getMenu().findItem(R.id.nav_logout).setVisible(authData != null);
        TextView username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
        if (authData != null && authData.getProviderData().containsKey("displayName")) {
            username.setText(authData.getProviderData().get("displayName").toString());
            if (authData.getProviderData().containsKey("profileImageURL")) {
                ImageView avatar = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.avatar);
                Glide.with(this)
                        .load(authData.getProviderData().get("profileImageURL").toString())
                        .transform(new GlideCircleTransform(this))
                        .into(avatar);
            }
        } else {
            username.setText(R.string.anonymous);
        }
        navigationView.setNavigationItemSelectedListener(this);

        // Set up Swipe Refresh Layout
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        // Get the default realm
        realm = Realm.getDefaultInstance();

        // Set up list of persons
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview);
        emptyView = findViewById(R.id.empty_view);
        setupRecyclerView(rv);

        // Refresh data
        if (authData != null) {
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                    refreshData();
                }
            });
        }
    }

    @SuppressWarnings("deprecation")
    private void setupRecyclerView(RecyclerView recyclerView) {
        adapter = new PersonAdapter(this, new IDClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v, String id, int position) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeActivity.this);
                Intent intent = new Intent(HomeActivity.this, ShowPersonActivity.class);
                intent.putExtra(Person.EXTRA_PERSON_ID, id);
                ActivityCompat.startActivity(HomeActivity.this, intent, options.toBundle());
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new PersonAdapter.DividerItemDecoration(getResources().getColor(R.color.black_divider)));
        recyclerView.setAdapter(adapter);

        result = realm
                .where(Person.class)
                .findAll();
        result.sort("lastname", Sort.ASCENDING);
        RealmModelAdapter<Person> realmAdapter = new RealmModelAdapter<>(getApplicationContext(), result, true);
        // Set the data and tell the RecyclerView to draw
        adapter.setRealmAdapter(realmAdapter);
        adapter.notifyDataSetChanged();
        showOrHideEmptyView();
    }

    private void refreshData() {
        // Retrieve data from Firebase and update local database (if logged in)
        if (firebaseRef.getAuth() != null) {
            Firebase personsRef = firebaseRef.child("persons").child(firebaseRef.getAuth().getUid());
            personsRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    for (DataSnapshot personSnapshot : snapshot.getChildren()) {
                        Person.addToRealm(personSnapshot.getValue(Person.class));
                    }
                    adapter.notifyDataSetChanged();
                    showOrHideEmptyView();
                    swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Snackbar.make(emptyView, "Unable to load data from server. Check your Internet connection and try again...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void showOrHideEmptyView() {
        try {
            if (result != null && result.size() == 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        } catch (IllegalStateException e) {
            Log.e("Realm Error", "Realm results no longer available. " + e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            showOrHideEmptyView();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition();
            } else {
                finish();
            }
        } else if (id == R.id.nav_logout) {
            logout();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Unauthenticate from Firebase and from providers where necessary.
     */
    private void logout() {
        AuthData authData = firebaseRef.getAuth();
        if (authData != null) {
            // Logout from Firebase
            firebaseRef.unauth();

            // Logout from Facebook
            if (authData.getProvider().equals("facebook")) {
                LoginManager.getInstance().logOut();
            }

            // Delete all cached data from Realm
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.clear(Person.class);
                }
            });
            realm.close();

            // Quit activity and return to Login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
