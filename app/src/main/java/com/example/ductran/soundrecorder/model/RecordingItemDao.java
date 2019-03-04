package com.example.ductran.soundrecorder.model;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface RecordingItemDao {

    @Query("select * from record")
    LiveData<List<RecordingItem>> getAllRecordingItems();

    @Query("select * from record where id = :position")
    RecordingItem getItemAt(int position) ;

    @Delete
    void removeItemWithId(RecordingItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addRecording(RecordingItem item);

    @Query("Select count(*) from record")
    int getCount();

    @Update
    void renameItem(RecordingItem item);


}
