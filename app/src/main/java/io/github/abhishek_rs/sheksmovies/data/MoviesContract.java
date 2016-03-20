package io.github.abhishek_rs.sheksmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by I311917 on 3/11/2016.
 */
public class MoviesContract {
    public static final String CONTENT_AUTHORITY = "io.github.abhishek_rs.sheksmovies";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://io.github.abhishek_rs.sheksmovies/favorites/ is a valid path for
    // looking at favorited movies.
    public static final String PATH_FAVORITES = "favorites";

    /*
        Inner class that defines the table contents of the favorites table
     */
    /* Inner class that defines the table contents of the weather table */
    public static final class FavoritesEntry implements BaseColumns {

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_PLOT_SUMMARY = "plotSummary";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_BACKDROP = "backdrop";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_NUMBER_VOTES = "numberVotes";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        //  public static final String COLUMN_WIND_SPEED = "wind";
        //  public static final String COLUMN_DEGREES = "degrees";

/*
        public static Uri buildWeatherLocation(String location_name){
            Uri weatheruri = Uri.parse(BASE_CONTENT_URI.toString()).buildUpon().appendPath(PATH_WEATHER).appendPath(location_name).build();
            return weatheruri;
        }
        */

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITES;


        public static Uri buildFavoritesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        /*
            Student: Fill in this buildWeatherLocation function
         */
        public static Uri buildFavoritesWithMovieId(String MovieId) {
            return CONTENT_URI.buildUpon().appendPath(MovieId).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

    }
}