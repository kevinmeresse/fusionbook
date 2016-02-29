package com.km.fusionbook.view.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.km.fusionbook.R;
import com.km.fusionbook.model.Person;
import com.km.fusionbook.util.Analytics;
import com.km.fusionbook.view.customviews.YesNoDialog;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    // Layout views
    private ProgressDialog authProgressDialog;

    // Firebase stuff
    private Firebase firebaseRef;
    private AuthData authData;
    private Firebase.AuthStateListener authStateListener;

    // Facebook stuff
    private CallbackManager facebookCallbackManager;
    private AccessTokenTracker facebookAccessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Set up transitions for Lollipop and after
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                getWindow().setReenterTransition(new Slide(Gravity.TOP));
                getWindow().setExitTransition(new Slide(Gravity.TOP));
            } catch (Exception e) {
                // Do nothing
            }
        }

        super.onCreate(savedInstanceState);

        // Create the Firebase reference that is used for all authentication with Firebase
        firebaseRef = new Firebase(getResources().getString(R.string.firebase_url));

        // If user is already logged in, clear all tasks and redirect to HomeActivity
        if (firebaseRef.getAuth() != null) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                intent.putExtras(extras);
            }
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_login);

        // Analytics log
        Analytics.viewedScreen(getApplicationContext(), R.string.analytics_screen_login);

        // Retrieve layout views
        Button discoverButton = (Button) findViewById(R.id.discover_button);
        LoginButton facebookButton = (LoginButton) findViewById(R.id.facebook_button);

        /* FACEBOOK */
        // Request FB permissions
        facebookButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        // Create callback manager and handle connection errors
        facebookCallbackManager = CallbackManager.Factory.create();
        facebookButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {}

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {
                showErrorDialog(getString(R.string.dialog_connection_message));
            }
        });
        // Set up tracker to monitor access token changes
        facebookAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                LoginActivity.this.onFacebookAccessTokenChange(currentAccessToken);
            }
        };

        // If discovering, show dialog to inform about login benefits
        discoverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YesNoDialog dialog = YesNoDialog.newInstance(
                        R.string.dialog_discover_title,
                        R.string.dialog_discover_message,
                        R.string.discover,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                redirectHome();
                            }
                        });
                dialog.show(getSupportFragmentManager(), "dialog");
                // Analytics log
                Analytics.action(getApplicationContext(), R.string.analytics_action_click_discover);
            }
        });

        // Setup the progress dialog that is displayed later when authenticating
        authProgressDialog = new ProgressDialog(this);
        authProgressDialog.setTitle(getString(R.string.dialog_loading));
        authProgressDialog.setMessage(getString(R.string.dialog_authenticating));
        authProgressDialog.setCancelable(false);
        authProgressDialog.show();

        authStateListener = new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                authProgressDialog.hide();
                setAuthenticatedUser(authData);
            }
        };
        // Check if the user is authenticated with Firebase already
        firebaseRef.addAuthStateListener(authStateListener);
    }

    /**
     * Called when Facebook access token has changed
     * @param token Access token from Facebook
     */
    private void onFacebookAccessTokenChange(AccessToken token) {
        if (token != null) {
            authProgressDialog.show();
            firebaseRef.authWithOAuthToken(getString(R.string.auth_provider_facebook),
                    token.getToken(),
                    new AuthResultHandler(getString(R.string.auth_provider_facebook)));
            Analytics.action(getApplicationContext(), R.string.analytics_action_login_facebook);
        } else {
            // Logged out of Facebook and currently authenticated with Firebase using Facebook, so do a logout
            if (this.authData != null
                    && this.authData.getProvider().equals(getString(R.string.auth_provider_facebook))) {
                firebaseRef.unauth();
                setAuthenticatedUser(null);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // If user logged in with Facebook, stop tracking their token
        if (facebookAccessTokenTracker != null) {
            facebookAccessTokenTracker.stopTracking();
        }

        // If changing configurations, stop tracking Firebase session
        firebaseRef.removeAuthStateListener(authStateListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // It's probably the request by the Facebook login button, keep track of the session
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Called after Firebase authentication
     * @param authData Authenticated user data from Firebase
     */
    private void setAuthenticatedUser(AuthData authData) {
        if (authData != null) {
            // Save user in Firebase
            Map<String, String> map = new HashMap<>();
            map.put("provider", authData.getProvider());
            if (authData.getProviderData().containsKey("displayName")) {
                map.put("displayName", authData.getProviderData().get("displayName").toString());
            }
            if (authData.getProviderData().containsKey("email")) {
                map.put("email", authData.getProviderData().get("email").toString());
            }
            if (authData.getProviderData().containsKey("profileImageURL")) {
                map.put("pictureUrl", authData.getProviderData().get("profileImageURL").toString());
            }
            firebaseRef.child("users").child(authData.getUid()).setValue(map);

            // Delete all cached data from Realm
            Realm realm = Realm.getDefaultInstance();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.clear(Person.class);
                }
            });
            realm.close();

            // Show home screen
            redirectHome();
        }
        this.authData = authData;
    }

    @SuppressWarnings("unchecked")
    private void redirectHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        // No transition animation as we are automatically redirected
        // and it would throw an error in Android 6.0
        ActivityCompat.startActivity(LoginActivity.this, intent, null);
    }

    /**
     * Shows an alert dialog with a custom message
     * @param message The message to be displayed
     */
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_error)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            authProgressDialog.hide();
            Log.i(TAG, provider + " auth successful");
            setAuthenticatedUser(authData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            authProgressDialog.hide();
            showErrorDialog(firebaseError.toString());
        }
    }
}
