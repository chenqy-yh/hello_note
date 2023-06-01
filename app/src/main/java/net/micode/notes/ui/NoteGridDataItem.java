package net.micode.notes.ui;

public class NoteGridDataItem {
    private int picId;
    private String desc;

    //cons
    public NoteGridDataItem(int picId, String desc) {
        this.picId = picId;
        this.desc = desc;
    }

    //getters
    public int getPicId() {
        return picId;
    }

    public String getDesc() {
        return desc;
    }

    //setters

    public void setPicId(int picId) {
        this.picId = picId;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
