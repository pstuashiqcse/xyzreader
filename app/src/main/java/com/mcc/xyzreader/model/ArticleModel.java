package com.mcc.xyzreader.model;


import android.os.Parcel;
import android.os.Parcelable;

public class ArticleModel implements Parcelable {

    private long id;
    private String title;
    private String date;
    private String author;
    private String thumbUrl;
    private float thumbAspectRatio;

    public ArticleModel(long id, String title, String date, String author, String thumbUrl, float thumbAspectRatio) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.author = author;
        this.thumbUrl = thumbUrl;
        this.thumbAspectRatio = thumbAspectRatio;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public float getThumbAspectRatio() {
        return thumbAspectRatio;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(date);
        dest.writeString(author);
        dest.writeString(thumbUrl);
        dest.writeFloat(thumbAspectRatio);
    }

    protected ArticleModel(Parcel in) {
        id = in.readLong();
        title = in.readString();
        date = in.readString();
        author = in.readString();
        thumbUrl = in.readString();
        thumbAspectRatio = in.readFloat();
    }

    public static final Parcelable.Creator<ArticleModel> CREATOR = new Parcelable.Creator<ArticleModel>() {
        @Override
        public ArticleModel createFromParcel(Parcel source) {
            return new ArticleModel(source);
        }

        @Override
        public ArticleModel[] newArray(int size) {
            return new ArticleModel[size];
        }
    };



}
