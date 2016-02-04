package io.github.abhishek_rs.sheksmovies;

/**
 * Created by I311917 on 1/28/2016.
 */
public class Movie {
    int id;
    String title;
    String plotSummary;
    String backdrop;
    double rating;
    long numberVotes;
    String release_date;
     // drawable reference id

    public Movie(int id, String title, String plotSummary, String backdrop, double rating, long numberVotes, String release_date)
    {
        this.id = id;
        this.title = title;
        this.plotSummary = plotSummary;
        this.backdrop = backdrop;
        this.rating = rating;
        this.numberVotes = numberVotes;
        this.release_date = release_date;
    }
}
