package com.example.ductran.soundrecorder.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.ductran.soundrecorder.view.MainActivity;
import com.example.ductran.soundrecorder.R;
import com.example.ductran.soundrecorder.model.RecordingItem;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class RecordingService extends Service {

    private static final String LOG_TAG = "RecordingService";

    private MediaRecorder mRecorder = null;
    private String mFileName = null;
    private String mFilePath = null;

    private long mStartingTimeMillis = 0;
    private long mElapsedMillis = 0;
    private int mElapsedSeconds = 0;

    public static final int request_code = 1000;
    private Timer mTimer = null;
    private NotificationCompat.Builder mBuilder;


    private TimerTask mIncrementTimerTask = null;

    private static final SimpleDateFormat mTimerFormat = new SimpleDateFormat("mm:ss", Locale.getDefault());

    final String ChannelID = "my_channel_01";


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startRecording();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        if (mRecorder != null) {
            stopRecording();
        }
        super.onDestroy();
    }

    public void startRecording() {
        setFileNameAndPath();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mRecorder.setOutputFile(mFilePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioChannels(1);
        if (MySharedPreferences.getPrefHighQuality(this)) {
            mRecorder.setAudioSamplingRate(44100);
            mRecorder.setAudioEncodingBitRate(192000);
        }

        try {
            mRecorder.prepare();
            mRecorder.start();
            mStartingTimeMillis = System.currentTimeMillis();

//            startTimer();
//            startForeground(1, createNotification());

        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }


    public void stopRecording() {
        mRecorder.stop();
        mElapsedMillis = (System.currentTimeMillis() - mStartingTimeMillis);
        mRecorder.release();
        Toast.makeText(this, getString(R.string.toast_recording_finish) + " " + mFilePath, Toast.LENGTH_LONG).show();

        //remove notification
        if (mIncrementTimerTask != null) {
            mIncrementTimerTask.cancel();
            mIncrementTimerTask = null;
        }

        mRecorder = null;
//        cancelNotification();

        try {
            RecordingItem item = new RecordingItem(mFileName, mFilePath, (int) mElapsedMillis, System.currentTimeMillis());
            EventBus.getDefault().post(item);

        } catch (Exception e) {
            Log.e(LOG_TAG, "exception", e);
        }
    }


    public void setFileNameAndPath() {
        int count = 0;
        File f;
        Calendar calendar = Calendar.getInstance();

        do {
            count++;
            mFileName = "recordings" + "_" + Utils.getDateTime(calendar.getTimeInMillis(), Utils.DATE_FORMAT_DEFAULT_STR) + ".mp4";
            mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFilePath += "/SoundRecorder/" + mFileName;

            f = new File(mFilePath);
        } while (f.exists() && !f.isDirectory());
    }

    private void startTimer() {
        mTimer = new Timer();
        mIncrementTimerTask = new TimerTask() {
            @Override
            public void run() {
                mElapsedSeconds++;
                NotificationManager mgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (mgr != null) {
                    mgr.notify(1, createNotification());
                }
            }
        };
        mTimer.scheduleAtFixedRate(mIncrementTimerTask, 1000, 1000);
    }

    private Notification createNotification() {
         mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), ChannelID)
                        .setSmallIcon(R.drawable.ic_mic_white_36dp)
                        .setContentTitle(getString(R.string.notification_recording))
                        .setContentText(mTimerFormat.format(mElapsedSeconds * 1000))
                        .setOngoing(true);

        mBuilder.setContentIntent(PendingIntent.getActivities(getApplicationContext(), 0,
                new Intent[]{new Intent(getApplicationContext(), MainActivity.class)}, 0));

        return mBuilder.build();
    }
    private void cancelNotification(){
        mBuilder.setAutoCancel(true);
    }
}
