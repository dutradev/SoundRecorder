package com.example.ductran.soundrecorder.view.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.ductran.soundrecorder.R;
import com.example.ductran.soundrecorder.model.RecordingItem;
import com.example.ductran.soundrecorder.utils.RecordEvent;
import com.example.ductran.soundrecorder.view.PlaybackFragment;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class FileViewerAdapter extends RecyclerView.Adapter<FileViewerAdapter.RecordingsViewHolder> {
    private static final String LOG_TAG = "FileViewerAdapter";
    private static final int CONTENT_VIEW_ID = 10101010;

    private List<RecordingItem> arrRecordItems;
    private final LayoutInflater mInflater;
    private Context context;

    public FileViewerAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    @NonNull
    @Override
    public RecordingsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.item_file_viewer, parent, false);
        return new RecordingsViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordingsViewHolder holder, int position) {
        RecordingItem item = arrRecordItems.get(position);
        long itemDuration = item.getLength();

        long minutes = TimeUnit.MILLISECONDS.toMinutes(itemDuration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(itemDuration) - TimeUnit.MINUTES.toSeconds(minutes);

        holder.tvName.setText(item.getName());
        holder.vLength.setText(String.format("%02d:%02d", minutes, seconds));
        holder.vDateAdded.setText(
                DateUtils.formatDateTime(
                        context,
                        item.getTime(),
                        DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_YEAR
                )
        );

    }

    public void addItems(List<RecordingItem> recordingItems) {
        this.arrRecordItems = recordingItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (arrRecordItems != null)
            return arrRecordItems.size();
        return 0;
    }

    class RecordingsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.file_name_text)
        TextView tvName;
        @BindView(R.id.file_length_text)
        TextView vLength;
        @BindView(R.id.file_date_added_text)
        TextView vDateAdded;
        @BindView(R.id.card_view)
        View cardView;

        RecordingsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.card_view)
        public void onClick() {
            try {
                int pos = getAdapterPosition();
                RecordingItem item = arrRecordItems.get(pos);
                PlaybackFragment playbackFragment = PlaybackFragment.newInstance(item);

                FragmentTransaction transaction = ((FragmentActivity) context)
                        .getSupportFragmentManager()
                        .beginTransaction();

                playbackFragment.show(transaction, "dialog_playback");

            } catch (Exception e) {
                Log.e(LOG_TAG, "exception", e);
            }
        }

        @OnLongClick(R.id.card_view)
        public boolean onLongClick() {
            final int pos = getAdapterPosition();
            final RecordingItem item = arrRecordItems.get(pos);

            ArrayList<String> entrys = new ArrayList<String>();
            entrys.add(context.getString(R.string.dialog_file_share));
            entrys.add(context.getString(R.string.dialog_file_rename));
            entrys.add(context.getString(R.string.dialog_file_delete));

            final CharSequence[] items = entrys.toArray(new CharSequence[entrys.size()]);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.dialog_title_options));
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    RecordEvent event = new RecordEvent(which, pos, item);
                    EventBus.getDefault().post(event);
                }
            });

            builder.setCancelable(true);
            builder.setNegativeButton(context.getString(R.string.dialog_action_cancel),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();

            return false;
        }

    }
}
