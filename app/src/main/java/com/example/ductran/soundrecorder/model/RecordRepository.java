package com.example.ductran.soundrecorder.model;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class RecordRepository {
    private RecordingItemDao itemDao;
    private LiveData<List<RecordingItem>> itemList;

    public RecordRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        itemDao = db.itemDao();
        itemList = itemDao.getAllRecordingItems();
    }

    public LiveData<List<RecordingItem>> getItemList() {
        return itemList;
    }

    public void insertRecord(RecordingItem item) {
        new RecordAsyncTask(itemDao).execute(item);
    }

    private static class RecordAsyncTask extends AsyncTask<RecordingItem, Void, Void> {
        private RecordingItemDao mAsyncTaskDao;

        RecordAsyncTask(RecordingItemDao dao) {
            this.mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(RecordingItem... recordingItems) {
            mAsyncTaskDao.addRecording(recordingItems[0]);
            return null;
        }
    }

    public void updateItem(RecordingItem item) {
        new updateRecordAsyncTask(itemDao).execute(item);
    }

    private static class updateRecordAsyncTask extends AsyncTask<RecordingItem, Void, Void> {
        private RecordingItemDao mAsyncTaskDao;


        updateRecordAsyncTask(RecordingItemDao mAsyncTaskDao) {
            this.mAsyncTaskDao = mAsyncTaskDao;
        }

        @Override
        protected Void doInBackground(RecordingItem... recordingItems) {
            mAsyncTaskDao.renameItem(recordingItems[0]);
            return null;
        }
    }

    public void deleteItem(RecordingItem item) {
        new deleteRecordAsyncTask(itemDao).execute(item);
    }

    private static class deleteRecordAsyncTask extends AsyncTask<RecordingItem, Void, Void> {
        private RecordingItemDao mAsyncTaskDao;


        deleteRecordAsyncTask(RecordingItemDao mAsyncTaskDao) {
            this.mAsyncTaskDao = mAsyncTaskDao;
        }

        @Override
        protected Void doInBackground(RecordingItem... recordingItems) {
            mAsyncTaskDao.removeItemWithId(recordingItems[0]);
            return null;
        }
    }
}
