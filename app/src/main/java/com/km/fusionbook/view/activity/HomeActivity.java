package com.km.fusionbook.view.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.facebook.login.LoginManager;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.km.fusionbook.R;
import com.km.fusionbook.interfaces.IDClickListener;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.util.Analytics;
import com.km.fusionbook.view.adapter.PersonAdapter;
import com.km.fusionbook.view.adapter.RealmModelAdapter;
import com.km.fusionbook.view.customviews.GlideCircleTransform;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = LoginActivity.class.getSimpleName();

    // Layout views
    private PersonAdapter adapter;
    private View emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;

    // Realm reference
    private Realm realm;
    private RealmResults<Person> result;

    // Firebase reference
    private Firebase firebaseRef;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        /**
         * This is called whenever an intent for HomeActivity is started and there is already an
         * instance of HomeActivity. Since its launchMode is defined as SingleTask in Manifest,
         * the existing instance will be brought to foreground and onNewIntent() will be called.
         */

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            updateList(query);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);

        // Set up toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Analytics log
        Analytics.viewedScreen(getApplicationContext(), R.string.analytics_screen_home);

        // Get Firebase reference
        firebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
        AuthData authData = firebaseRef.getAuth();

        // Set up floating ADD button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View view) {
                Analytics.action(getApplicationContext(), R.string.analytics_action_click_add_person);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(HomeActivity.this);
                Intent intent = new Intent(HomeActivity.this, EditPersonActivity.class);
                ActivityCompat.startActivity(HomeActivity.this, intent, options.toBundle());
            }
        });

        // Set up drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Set up Navigation View
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

            // Add user information to Crashlytics
            try {
                Crashlytics.setUserIdentifier(authData.getUid());
                Crashlytics.setUserEmail(authData.getProviderData().get("email").toString());
                Crashlytics.setUserName(authData.getProviderData().get("displayName").toString());
            } catch (Exception e) {
                // Some data could not be retrieved from Auth data
                // Do nothing
            }
        } else {
            username.setText(R.string.anonymous);
            // Add user information to Crashlytics
            Crashlytics.setUserName(getString(R.string.anonymous));
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

        // Set up recyclerview holding the list of persons
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

        updateList(null);
    }

    /**
     * Updates the list of person based on local data, with an optional text filter
     * @param textToSearch Text to filter list
     */
    private void updateList(String textToSearch) {
        RealmQuery<Person> query = realm.where(Person.class);
        if (!TextUtils.isEmpty(textToSearch)) {
            String[] texts = textToSearch.trim().split(" ");
            for (String text : texts) {
                query.beginGroup();
                // For now we only search on firstname and lastname
                query.beginsWith("firstname", text, Case.INSENSITIVE);
                query.or().beginsWith("lastname", text, Case.INSENSITIVE);
                query.endGroup();
            }
        }
        result = query.findAll();
        result.sort("lastname", Sort.ASCENDING);
        RealmModelAdapter<Person> realmAdapter = new RealmModelAdapter<>(getApplicationContext(), result, true);
        // Set the data and tell the RecyclerView to draw
        adapter.setRealmAdapter(realmAdapter);
        adapter.notifyDataSetChanged();
        showOrHideEmptyView();
    }

    /**
     * Refresh local data with latest changes from server
     */
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
                    Snackbar.make(emptyView, R.string.dialog_connection_load_message, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        } else {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * Toggles empty view depending on Realm result
     */
    private void showOrHideEmptyView() {
        try {
            if (result != null && result.size() == 0) {
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Realm error: Realm results no longer available. " + e.getMessage());
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
        // Handle navigation view item clicks
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
            Analytics.action(getApplicationContext(), R.string.analytics_action_logout);

            // Logout from Firebase
            firebaseRef.unauth();

            // Logout from Facebook
            if (authData.getProvider().equals(getString(R.string.auth_provider_facebook))) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_book_menu, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // Set up search view behavior
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                updateList(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                updateList(newText);
                return true;
            }
        });

        return true;
    }
}
