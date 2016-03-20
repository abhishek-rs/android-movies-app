package io.github.abhishek_rs.sheksmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by I311917 on 1/28/2016.
 */
public class Movie implements Parcelable{
    int id;
    String title;
    String plotSummary;
    String backdrop;
    double rating;
    int numberVotes;
    String release_date;
    String poster;
     // drawable reference id

    @Override public int describeContents() {
        return 0;
    }



    public Movie(int id, String title, String plotSummary, String backdrop, String poster, double rating, int numberVotes, String release_date)
    {
        this.id = id;
        this.title = title;
        this.plotSummary = plotSummary;
        this.poster = poster;
        this.backdrop = backdrop;
        this.rating = rating;
        this.numberVotes = numberVotes;
        this.release_date = release_date;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(plotSummary);
        dest.writeString(backdrop);
        dest.writeString(poster);
        dest.writeDouble(rating);
        dest.writeInt(numberVotes);
        dest.writeString(release_date);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
    // "De-parcel object
    public Movie(Parcel in) {
        id = in.readInt();
        title = in.readString();
        plotSummary = in.readString();
        backdrop = in.readString();
        poster = in.readString();
        rating = in.readDouble();
        numberVotes = in.readInt();
        release_date = in.readString();
    }
}
