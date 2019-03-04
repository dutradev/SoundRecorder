package com.example.ductran.soundrecorder.view;


import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ductran.soundrecorder.R;
import com.example.ductran.soundrecorder.model.RecordingItem;
import com.example.ductran.soundrecorder.utils.RecordingService;
import com.example.ductran.soundrecorder.viewmodel.RecordViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class RecordFragment extends Fragment implements Chronometer.OnChronometerTickListener {

    private static final String ARG_POSITION = "position";
    private int position;

    @BindView(R.id.chronometer)
    Chronometer mChronometer;
    @BindView(R.id.recording_status_text)
    TextView mRecordingPrompt;
    @BindView(R.id.btn_record)
    FloatingActionButton mRecordButton;
    @BindView(R.id.btn_pause)
    Button mPauseButton;

    private Unbinder unbinder;

    private int mRecordPromptCount = 0;

    private boolean mStartRecording;
    private boolean mPauseRecording;

    long timeWhenPaused = 0;//stores time when user clicks pause button
    private RecordViewModel recordViewModel;


    public static RecordFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        RecordFragment fragment = new RecordFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recordViewModel = ViewModelProviders.of(this).get(RecordViewModel.class);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);
        }
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_record, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        unbinder = ButterKnife.bind(this, view);

        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void onPause() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onPause();
    }

    @Subscribe
    public void onEvent(RecordingItem event) {
        recordViewModel.insert(event);
    }

    private void initData() {
        mStartRecording = true;
        mPauseRecording = true;

        mPauseButton.setVisibility(View.GONE);
//        mPauseButton.setOnClickListener(this);
//        mRecordButton.setOnClickListener(this);


    }

    private boolean checkAndRequestPermissions() {
        int permissionRecordAudio = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.RECORD_AUDIO);
        int permissionWrite = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionRead = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE);
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
            ActivityCompat.requestPermissions(getActivity(), listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;
    }

    @OnClick(R.id.btn_record)
    public void record(){
        if (checkAndRequestPermissions()) {
            onRecord(mStartRecording);
            mStartRecording = !mStartRecording;
        }
    }

    @OnClick(R.id.btn_pause)
    public void pauseRecord(){
        onPauseRecord(mPauseRecording);
        mPauseRecording = !mPauseRecording;
    }

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btn_record:
//                if (checkAndRequestPermissions()) {
//                    onRecord(mStartRecording);
//                    mStartRecording = !mStartRecording;
//                }
//                break;
//            case R.id.btn_pause:
//                onPauseRecord(mPauseRecording);
//                mPauseRecording = !mPauseRecording;
//                break;
//        }
//    }


    private void onRecord(boolean start) {
        Intent intent = new Intent(getActivity(), RecordingService.class);

        if (start) {
            mRecordButton.setImageResource(R.drawable.ic_media_stop);
            mPauseButton.setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();

            File folder = new File(Environment.getExternalStorageDirectory() + "/SoundRecorder");
            if (!folder.exists()) {
                folder.mkdir();
            }

            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.start();
            mChronometer.setOnChronometerTickListener(this);

            getActivity().startService(intent);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
            mRecordPromptCount++;

        } else {
            mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
            mPauseButton.setVisibility(View.GONE);
            mChronometer.stop();
            mChronometer.setBase(SystemClock.elapsedRealtime());
            timeWhenPaused = 0;
            mRecordingPrompt.setText(getString(R.string.record_prompt));


            getActivity().stopService(intent);
            //allow the screen to turn off again once recording is finished
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        }
    }

    private void onPauseRecord(boolean pause) {
        if (pause) {
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_play, 0, 0, 0);
            mRecordingPrompt.setText(getString(R.string.resume_recording_button).toUpperCase());
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
        } else {
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_media_pause, 0, 0, 0);
            mRecordingPrompt.setText(getString(R.string.pause_recording_button).toUpperCase());
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            mChronometer.start();
        }
    }

    @Override
    public void onChronometerTick(Chronometer chronometer) {
        switch (mRecordPromptCount) {
            case 0:
                mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                break;
            case 1:
                mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                break;
            case 2:
                mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                mRecordPromptCount = -1;
                break;
        }
        mRecordPromptCount++;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
