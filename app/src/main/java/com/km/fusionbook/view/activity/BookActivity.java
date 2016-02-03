package com.km.fusionbook.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.km.fusionbook.R;
import com.km.fusionbook.interfaces.LongIDClickListener;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.view.adapter.PersonAdapter;
import com.km.fusionbook.view.adapter.RealmModelAdapter;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class BookActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RealmResults<Person> result;
    private PersonAdapter adapter;
    private View emptyView;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, R.string.add_person, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(BookActivity.this);
                Intent intent = new Intent(BookActivity.this, EditPersonActivity.class);
                ActivityCompat.startActivity(BookActivity.this, intent, options.toBundle());
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Get the default realm
        realm = Realm.getDefaultInstance();

        // Setup list of persons
        RecyclerView rv = (RecyclerView) findViewById(R.id.recyclerview);
        emptyView = findViewById(R.id.empty_view);
        setupRecyclerView(rv);
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        adapter = new PersonAdapter(new LongIDClickListener() {
            @Override
            public void onClick(View v, long id, int position) {
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(BookActivity.this);
                Intent intent = new Intent(BookActivity.this, EditPersonActivity.class);
                intent.putExtra(Person.EXTRA_PERSON_ID, id);
                ActivityCompat.startActivity(BookActivity.this, intent, options.toBundle());
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new PersonAdapter.DividerItemDecoration(getResources().getColor(R.color.black_divider)));
        recyclerView.setAdapter(adapter);

        result = realm
                .where(Person.class)
                .findAll();
        result.sort("firstname", Sort.ASCENDING);
        RealmModelAdapter<Person> realmAdapter = new RealmModelAdapter<>(getApplicationContext(), result, true);
        // Set the data and tell the RecyclerView to draw
        adapter.setRealmAdapter(realmAdapter);
        adapter.notifyDataSetChanged();
        showOrHideEmptyView();
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

        if (id == R.id.nav_logout) {
            // Handle the logout action
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
