package com.media.audiovideoplayer.activity;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.media.audiovideoplayer.R;
import com.media.audiovideoplayer.adapter.VideoPlayerAdapter;
import com.media.audiovideoplayer.adapter.ViewPagerAdapter;
import com.media.audiovideoplayer.constants.AudioVideoConstants;
import com.media.audiovideoplayer.fragment.AudioFragment;
import com.media.audiovideoplayer.fragment.VideoFragment;
import com.media.audiovideoplayer.service.PlayerService;
import com.media.audiovideoplayer.sharedpreferences.Preferences;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Permission";
    private static TabLayout tabLayout;
    private static ViewPager viewPager;
    public SwipeRefreshLayout swipeRefreshLayout;
    public static Activity mav;
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public ImageView navImageView;
    public ActionBarDrawerToggle drawerToggle;
    public SwitchCompat fingerPrint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_Pager);
        swipeRefreshLayout = findViewById(R.id.mainActivity);
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.nav_view);
        mav = this;
        isStoragePermissionGranted();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                playGif();
                fingerPrint = findViewById(R.id.fingerprint);
                fingerPrint.setChecked(Preferences.getSharedPreferences(getApplicationContext()).getBoolean("FingerPrintLockStatus", false));
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        onNavigationItemClick();
        getFingerPrintStatus();
        swipeToRefresh();
    }

    /**
     * Method invoked to Initiate UI
     */
    public void initiateTabs() {
        try {
            ArrayList<Fragment> fragments = new ArrayList<>();
            fragments.add(new VideoFragment());
            fragments.add(new AudioFragment());
            ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), fragments);
            viewPager.setAdapter(viewPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * To get the status whether storage permission is granted or not
     *
     * @return
     */
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getFingerPrintStatus();
        } else {
            Toast.makeText(getApplicationContext(), "Please provide storage access to proceed further", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Method Invoked for Swipe down to refresh the User Interface if UI is not loaded properly
     */

    public void swipeToRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            initiateTabs();
            swipeRefreshLayout.setRefreshing(false);
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                swipeRefreshLayout.setEnabled(false);
            }
        });
    }

    /**
     * Method invoked for enabling authentication
     *
     * @param v
     */
    public void enableFingerPrint(View v) {
        fingerPrint = findViewById(R.id.fingerprint);
        if (fingerPrint.isChecked()) {
            showFingerPrint();
        } else
            Preferences.getSharedPreferences(getApplicationContext()).edit().putBoolean("FingerPrintLockStatus", false).apply();

    }

    /**
     * Method invoked for getting the status of authentication (enabled or not)
     */

    public void getFingerPrintStatus() {
        if (Preferences.getSharedPreferences(getApplicationContext()).getBoolean("FingerPrintLockStatus", false)) {
            showFingerPrint();
        } else {
            if (isStoragePermissionGranted())
                initiateTabs();
        }
    }

    /**
     * To show authentication prompt if authentication is enabled
     */

    public void showFingerPrint() {
        if (!isDeviceSecured()) {
            Toast.makeText(getApplicationContext(), "Sorry Authentication will not work unless device is secured", Toast.LENGTH_LONG).show();
            Preferences.getSharedPreferences(getApplicationContext()).edit().putBoolean("FingerPrintLockStatus", false).apply();
            if (null != fingerPrint)
                fingerPrint.setChecked(Preferences.getSharedPreferences(getApplicationContext()).getBoolean("FingerPrintLockStatus", false));
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(getApplicationContext(),
                            "Authentication error: " + errString, Toast.LENGTH_SHORT)
                            .show();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(getApplicationContext(),
                            "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                    Preferences.getSharedPreferences(getApplicationContext()).edit().putBoolean("FingerPrintLockStatus", true).apply();
                    if (isStoragePermissionGranted())
                        initiateTabs();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    Toast.makeText(getApplicationContext(), "Authentication failed",
                            Toast.LENGTH_SHORT)
                            .show();
                    finishAndRemoveTask();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Verify your identity")
                    .setSubtitle("Use your fingerprint to verify your identity")
                    .setDeviceCredentialAllowed(true)
                    .build();
            biometricPrompt.authenticate(promptInfo);
        } else {
            enableAuthentication();
        }
    }

    /*
    Method used for Showing Gif in Drawer Layout
     */

    public void playGif() {
        navImageView = findViewById(R.id.nav_image_view);
        if (null != navImageView) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                Glide.with(getApplicationContext()).asGif().load(R.drawable.audiovideoplayer).into(navImageView);
            }
        }
    }

    public void onNavigationItemClick() {
        navigationView.setNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.sleepTimer:
                    break;
                case R.id.about:
                    break;
                case R.id.exitApp:
                    Intent intent = new Intent(this, PlayerService.class);
                    intent.setAction(AudioVideoConstants.STOP_FOREGROUND);
                    if (Preferences.getSharedPreferences(getApplicationContext()).getBoolean("serviceStatus", false))
                        startService(intent);
                    else
                        Toast.makeText(getApplicationContext(), "Sorry the player cannot be stopped", Toast.LENGTH_LONG).show();
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * method to return whether device is secured or not
     */
    private boolean isDeviceSecured() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.isKeyguardSecure();
    }

    /**
     * Method invoked for authenticating app for android versions below Q
     */

    private void enableAuthentication() {
        if (isDeviceSecured()) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            Intent keyguardManagerConfirmDeviceCredentialIntent = keyguardManager.createConfirmDeviceCredentialIntent("Unlock", "Confirm PIN");
            try {
                startActivityForResult(keyguardManagerConfirmDeviceCredentialIntent, 123);
            } catch (Exception e) {
                Intent securitySettings = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                try {
                    startActivityForResult(securitySettings, 456);
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Sorry Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Sorry Authentication Failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 123:
                if (resultCode == Activity.RESULT_OK) {
                    if (isStoragePermissionGranted())
                        initiateTabs();
                }
                Preferences.getSharedPreferences(getApplicationContext()).edit().putBoolean("FingerPrintLockStatus", true).apply();
                Toast.makeText(getApplicationContext(), "Successfully authenticated!!", Toast.LENGTH_LONG).show();
                break;
            case 456:
                if (isDeviceSecured()) {
                    enableAuthentication();
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry Authentication Failed", Toast.LENGTH_SHORT).show();
                    Preferences.getSharedPreferences(getApplicationContext()).edit().putBoolean("FingerPrintLockStatus", false).apply();
                    if (isStoragePermissionGranted())
                        initiateTabs();
                }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

}