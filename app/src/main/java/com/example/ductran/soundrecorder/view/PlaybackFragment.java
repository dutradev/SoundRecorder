package com.example.ductran.soundrecorder.view;

import android.app.Dialog;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.ductran.soundrecorder.R;
import com.example.ductran.soundrecorder.model.RecordingItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PlaybackFragment extends DialogFragment {
    private static final String LOG_TAG = "PlaybackFragment";

    private static final String ARG_ITEM = "recording_item";

    @BindView(R.id.seekbar)
    SeekBar mSeekBar;
    @BindView(R.id.fab_play)
    FloatingActionButton btnPlay;
    @BindView(R.id.current_progress_text_view)
    TextView tvCurrentProgress;
    @BindView(R.id.file_name_text_view)
    TextView tvFileName;
    @BindView(R.id.file_length_text_view)
    TextView tvFileLength;

    private Unbinder unbinder;

    private MediaPlayer mMediaPlayer;
    private Handler mHandler = new Handler();

    private RecordingItem item;
    private boolean isPlaying = false;

    long minutes = 0;
    long seconds = 0;

    public static PlaybackFragment newInstance(RecordingItem item) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ITEM, item);
        PlaybackFragment fragment = new PlaybackFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = getArguments().getParcelable(ARG_ITEM);
        }
        long itemDuration = 0;
        if (item != null) {
            itemDuration = item.getLength();
        }
        minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration)
                - TimeUnit.MINUTES.toSeconds(minutes);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_media_playback, null);

        unbinder = ButterKnife.bind(this, view);

        ColorFilter filter = new LightingColorFilter(getResources().getColor(R.color.primary), getResources().getColor(R.color.primary));
        mSeekBar.getProgressDrawable().setColorFilter(filter);
        mSeekBar.getThumb().setColorFilter(filter);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mMediaPlayer != null && fromUser) {
                    mMediaPlayer.seekTo(progress);
                    mHandler.removeCallbacks(mRunnable);

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    tvCurrentProgress.setText(String.format("%02d:%02d", minutes, seconds));

                    updateSeekBar();
                } else if (mMediaPlayer == null && fromUser) {
                    prepareMediaPlayerFromPoint(progress);
                    updateSeekBar();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mHandler.removeCallbacks(mRunnable);
                    mMediaPlayer.seekTo(seekBar.getProgress());

                    long minutes = TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer.getCurrentPosition());
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer.getCurrentPosition())
                            - TimeUnit.MINUTES.toSeconds(minutes);
                    tvCurrentProgress.setText(String.format("%02d:%02d", minutes, seconds));
                    updateSeekBar();
                }
            }
        });

        tvFileName.setText(item.getName());
        tvFileLength.setText(String.format("%02d:%02d", minutes, seconds));

        builder.setView(view);

        // request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return builder.create();
    }

    @OnClick(R.id.fab_play)
    public void playSound() {
        onPlay(isPlaying);
        isPlaying = !isPlaying;
    }

    private void prepareMediaPlayerFromPoint(int progress) {
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(item.getFilePath());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.seekTo(progress);

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlaying();
                }
            });
        } catch (Exception e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void onPlay(boolean isPlaying) {
        if (!isPlaying) {
            if (mMediaPlayer == null) {
                startPlaying();
            } else {
                resumePlaying();
            }
        } else {
            pausePlaying();
        }
    }

    private void startPlaying() {
        btnPlay.setImageResource(R.drawable.ic_media_pause);

        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(item.getFilePath());
            mMediaPlayer.prepare();
            mSeekBar.setMax(mMediaPlayer.getDuration());
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });

        updateSeekBar();

        //keep screen on while playing audio
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void resumePlaying() {
        btnPlay.setImageResource(R.drawable.ic_media_pause);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.start();
        updateSeekBar();
    }

    private void pausePlaying() {
        btnPlay.setImageResource(R.drawable.ic_media_play);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.pause();
    }

    private void stopPlaying() {
        btnPlay.setImageResource(R.drawable.ic_media_play);
        mHandler.removeCallbacks(mRunnable);
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;

        mSeekBar.setProgress(mSeekBar.getMax());
        isPlaying = !isPlaying;
        tvCurrentProgress.setText(tvFileLength.getText());
        mSeekBar.setProgress(mSeekBar.getMax());

        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {
                int currentPosition = mMediaPlayer.getCurrentPosition();
                mSeekBar.setProgress(currentPosition);

                long minutes = TimeUnit.MILLISECONDS.toMinutes(currentPosition);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(currentPosition)
                        - TimeUnit.MINUTES.toSeconds(minutes);
                tvCurrentProgress.setText(String.format("%02d:%02d", minutes, seconds));

                updateSeekBar();

            }
        }
    };

    private void updateSeekBar() {
        mHandler.postDelayed(mRunnable, 1000);
    }

    @Override
    public void onStart() {
        super.onStart();
        //set transparent background
        Window window = getDialog().getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        //disable buttons from dialog
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEGATIVE).setEnabled(false);
        alertDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopPlaying();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
