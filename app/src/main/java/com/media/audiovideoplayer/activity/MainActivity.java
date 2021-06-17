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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.media.audiovideoplayer.R;
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
    public static View mainActivity;
    public static Activity mav;
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public ImageView navImageView;
    public ActionBarDrawerToggle drawerToggle;
    public SwitchCompat fingerPrint;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_Pager);
        mainActivity = findViewById(R.id.mainActivity);
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
    }

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

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    public void enableFingerPrint(View v) {
        fingerPrint = findViewById(R.id.fingerprint);
        if (fingerPrint.isChecked()) {
            Preferences.getSharedPreferences(getApplicationContext()).edit().putBoolean("FingerPrintLockStatus", true).apply();
            showFingerPrint();
        } else
            Preferences.getSharedPreferences(getApplicationContext()).edit().putBoolean("FingerPrintLockStatus", false).apply();

    }

    public void getFingerPrintStatus() {
        if (Preferences.getSharedPreferences(getApplicationContext()).getBoolean("FingerPrintLockStatus", false)) {
            showFingerPrint();
        } else {
            if (isStoragePermissionGranted())
                initiateTabs();
        }
    }

    public void showFingerPrint() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            executor = ContextCompat.getMainExecutor(this);
            biometricPrompt = new BiometricPrompt(MainActivity.this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode,
                                                  @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    Toast.makeText(getApplicationContext(),
                            "Authentication error: " + errString +": Sorry FingerPrint option will not work unless device is secured", Toast.LENGTH_SHORT)
                            .show();
                    Preferences.getSharedPreferences(getApplicationContext()).edit().putBoolean("FingerPrintLockStatus", false).apply();
                    if (isStoragePermissionGranted())
                        initiateTabs();
                }

                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    Toast.makeText(getApplicationContext(),
                            "Authentication succeeded!", Toast.LENGTH_SHORT).show();
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

            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Verify your identity")
                    .setSubtitle("Use your fingerprint to verify your identity")
                    .setDeviceCredentialAllowed(true)
                    .build();
            biometricPrompt.authenticate(promptInfo);
        } else {
            authenticateApp();
        }
    }

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
     * method to return whether device has screen lock enabled or not
     */
    private boolean isDeviceSecure() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        return keyguardManager.isKeyguardSecure();
    }

    private void authenticateApp() {
        if (isDeviceSecure()) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            Intent keyGuardIntent = keyguardManager.createConfirmDeviceCredentialIntent("Unlock", "Confirm PIN");
            try {
                startActivityForResult(keyGuardIntent, 123);
            } catch (Exception e) {
                Intent securityIntent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                try {
                    startActivityForResult(securityIntent, 456);
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Sorry Authentication Failed", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "Sorry FingerPrint option will not work unless device is secured", Toast.LENGTH_SHORT).show();
            Preferences.getSharedPreferences(getApplicationContext()).edit().putBoolean("FingerPrintLockStatus", false).apply();
            if (isStoragePermissionGranted())
                initiateTabs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
            getFingerPrintStatus();
        } else {
            Toast.makeText(getApplicationContext(), "Please provide storage access to proceed further", Toast.LENGTH_LONG).show();
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
                Toast.makeText(getApplicationContext(), "Successfully authenticated!!", Toast.LENGTH_LONG).show();
                break;
            case 456:
                if (isDeviceSecure()) {
                    authenticateApp();
                } else {
                    Toast.makeText(getApplicationContext(), "Sorry FingerPrint option will not work unless device is secured", Toast.LENGTH_SHORT).show();
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