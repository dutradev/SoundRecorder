package com.example.ductran.soundrecorder.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ductran.soundrecorder.R;
import com.example.ductran.soundrecorder.model.RecordingItem;
import com.example.ductran.soundrecorder.utils.RecordEvent;
import com.example.ductran.soundrecorder.view.adapter.FileViewerAdapter;
import com.example.ductran.soundrecorder.viewmodel.RecordViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FileViewerFragment extends Fragment {
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    private Unbinder unbinder;

    List<RecordingItem> recordingItemList;
    FileViewerAdapter adapter;
    private RecordViewModel recordViewModel;


    public static FileViewerFragment newInstance(int position) {

        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        FileViewerFragment fragment = new FileViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_file_viewer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unbinder = ButterKnife.bind(this, view);
        initData();
    }


    private void initData() {
        recordingItemList = new ArrayList<>();

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);

        recyclerView.setLayoutManager(llm);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new FileViewerAdapter(getActivity());
        recyclerView.setAdapter(adapter);

        recordViewModel = ViewModelProviders.of(this).get(RecordViewModel.class);
        recordViewModel.getItemList().observe(this, new Observer<List<RecordingItem>>() {
            @Override
            public void onChanged(@Nullable List<RecordingItem> recordingItems) {
                adapter.addItems(recordingItems);
            }
        });
    }

    @Subscribe
    public void onMessageEvent(RecordEvent event) {
        showDialog(event);
    }

    private void showDialog(RecordEvent event) {
        switch (event.getSelect()) {
            case 0:
                shareFileDialog(event);
                break;
            case 1:
                renameFileDialog(event);
                break;
            case 2:
                deleteFileDialog(event);
                break;
        }
    }

    private void shareFileDialog(RecordEvent event) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(event.getItem().getFilePath())));
        shareIntent.setType("audio/mp4");
        startActivity(Intent.createChooser(shareIntent, getText(R.string.send_to)));
    }

    private void renameFileDialog(final RecordEvent event) {
        AlertDialog.Builder renameBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_rename_file, null);
        final EditText edInput = view.findViewById(R.id.new_name);

        renameBuilder.setTitle(getString(R.string.dialog_title_rename));
        renameBuilder.setCancelable(true);
        renameBuilder.setPositiveButton(getString(R.string.dialog_action_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String value = edInput.getText().toString().trim() + ".mp4";
                    rename(event, value);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception", e);
                }
                dialog.cancel();
            }
        });

        renameBuilder.setNegativeButton(getString(R.string.dialog_action_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        renameBuilder.setView(view);
        AlertDialog dialog = renameBuilder.create();
        dialog.show();


    }

    private void rename(RecordEvent event, String value) {
        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePath += "/SoundRecorder/" + value;
        File f = new File(filePath);

        if (f.exists() && !f.isDirectory()) {
            Toast.makeText(getActivity(),
                    String.format(getString(R.string.toast_file_exists), value),
                    Toast.LENGTH_SHORT).show();
        } else {
            File oldFile = new File(event.getItem().getFilePath());
            boolean isRename = oldFile.renameTo(f);
            if (isRename) {
                event.getItem().setName(value);
                event.getItem().setFilePath(filePath);
                recordViewModel.update(event.getItem());
                adapter.notifyItemChanged(event.getPos());
            }
        }
    }

    private void deleteFileDialog(final RecordEvent event) {
        AlertDialog.Builder deleteBuilder = new AlertDialog.Builder(getActivity());
        deleteBuilder.setTitle(getString(R.string.dialog_title_delete));
        deleteBuilder.setMessage(getString(R.string.dialog_text_delete));
        deleteBuilder.setCancelable(true);

        deleteBuilder.setPositiveButton(getString(R.string.dialog_action_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    remove(event);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "exception", e);
                }
                dialog.cancel();
            }
        });
        deleteBuilder.setNegativeButton(getString(R.string.dialog_action_no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = deleteBuilder.create();
        alert.show();
    }

    private void remove(RecordEvent event) {
        File file = new File(event.getItem().getFilePath());
        boolean isDelete = file.delete();
        if (isDelete) {
            Toast.makeText(
                    getActivity(),
                    String.format(
                            getString(R.string.toast_file_delete),
                            event.getItem().getName()
                    ),
                    Toast.LENGTH_SHORT
            ).show();

            recordViewModel.delete(event.getItem());
            adapter.notifyItemChanged(event.getPos());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
