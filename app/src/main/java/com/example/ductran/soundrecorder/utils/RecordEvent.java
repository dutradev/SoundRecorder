package com.example.ductran.soundrecorder.utils;

import com.example.ductran.soundrecorder.model.RecordingItem;

public class RecordEvent {
    int select;
    int pos;
    public RecordingItem item;

    public RecordEvent(int select, int pos, RecordingItem item) {
        this.select = select;
        this.pos = pos;
        this.item = item;
    }

    public int getSelect() {
        return select;
    }

    public void setSelect(int select) {
        this.select = select;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public RecordingItem getItem() {
        return item;
    }

    public void setItem(RecordingItem item) {
        this.item = item;
    }
}
