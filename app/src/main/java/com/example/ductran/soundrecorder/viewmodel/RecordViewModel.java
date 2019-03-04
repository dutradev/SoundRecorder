package com.example.ductran.soundrecorder.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import com.example.ductran.soundrecorder.model.RecordRepository;
import com.example.ductran.soundrecorder.model.RecordingItem;

import java.util.List;

public class RecordViewModel extends AndroidViewModel {

    private RecordRepository mRepository;

    private LiveData<List<RecordingItem>> itemList;

    public RecordViewModel(Application application) {
        super(application);
        mRepository = new RecordRepository(application);
        itemList = mRepository.getItemList();
    }

    public LiveData<List<RecordingItem>> getItemList() {
        return itemList;
    }

    public void insert(RecordingItem item) {
        mRepository.insertRecord(item);
    }

    public void update(RecordingItem item) {
        mRepository.updateItem(item);
    }

    public void delete(RecordingItem item) {
        mRepository.deleteItem(item);
    }
}
