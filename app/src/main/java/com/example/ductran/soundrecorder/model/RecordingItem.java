package com.example.ductran.soundrecorder.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "record")
public class RecordingItem implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "file_path")
    private String filePath;

    @ColumnInfo(name = "length")
    private int length;

    @ColumnInfo(name = "time")
    private long time;

    public RecordingItem(String name, String filePath, int length, long time) {
        this.name = name;
        this.filePath = filePath;
        this.length = length;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLength() {
        return length;
    }

    public long getTime() {
        return time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Ignore
    @Override
    public int describeContents() {
        return 0;
    }

    @Ignore
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.filePath);
        dest.writeInt(this.length);
        dest.writeLong(this.time);
    }

    @Ignore
    protected RecordingItem(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.filePath = in.readString();
        this.length = in.readInt();
        this.time = in.readLong();
    }

    @Ignore
    public static final Creator<RecordingItem> CREATOR = new Creator<RecordingItem>() {
        @Override
        public RecordingItem createFromParcel(Parcel source) {
            return new RecordingItem(source);
        }

        @Override
        public RecordingItem[] newArray(int size) {
            return new RecordingItem[size];
        }
    };
}
