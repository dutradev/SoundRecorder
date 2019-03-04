package com.example.ductran.soundrecorder.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.example.ductran.soundrecorder.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @BindView(R.id.pager)
    ViewPager pager;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.main_activity)
    LinearLayout linearLayout;


    private static final int REQUEST_PERMISSION = 1;
    private static final String TAG = "permission";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        checkAndRequestPermissions();

        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));

        tabs.setViewPager(pager);
        toolbar.setPopupTheme(R.style.ThemeOverlay_AppCompat_Light);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
    }

//    private void checkRequestPermission() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
//                    || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                    ) {
//
//                ActivityCompat.requestPermissions(this, new String[]{
//                        Manifest.permission.RECORD_AUDIO,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                        Manifest.permission.READ_EXTERNAL_STORAGE
//                }, REQUEST_PERMISSION);
//            }
//        }
//    }

    private void checkAndRequestPermissions() {
        int permissionRecordAudio = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);
        int permissionWrite = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRead = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionRecordAudio != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissionRead != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_PERMISSION);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class MyAdapter extends FragmentPagerAdapter {
        private String[] titles = {
                getString(R.string.tab_title_record),
                getString(R.string.tab_title_saved_recordings)
        };

        MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return RecordFragment.newInstance(position);
                case 1:
                    return FileViewerFragment.newInstance(position);
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    boolean recordAccepted = perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
                    boolean writeAccepted = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                    boolean readAccepted = perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

                    if (recordAccepted && writeAccepted && readAccepted) {
                        Log.d(TAG, "Record & storage services permission granted");
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again");
                        Snackbar.make(linearLayout, "Go to settings and enable permissions", Snackbar.LENGTH_SHORT).show();
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                ) {
                            showDialogOK("This permission is important to record audio",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            checkAndRequestPermissions();
                                        }
                                    }
                            );
                        } else {
                            Toast.makeText(getApplicationContext(), "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                        }

                    }

                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);


        }
    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .create()
                .show();
    }
}
