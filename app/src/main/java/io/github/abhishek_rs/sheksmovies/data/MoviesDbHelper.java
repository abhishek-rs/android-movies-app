package io.github.abhishek_rs.sheksmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by I311917 on 3/11/2016.
 */
public class MoviesDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "favorites.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + MoviesContract.FavoritesEntry.TABLE_NAME + " (" +

                MoviesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +

                // the ID of the location entry associated with this weather data
                MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_PLOT_SUMMARY + " TEXT NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_POSTER + " TEXT NOT NULL," +

                MoviesContract.FavoritesEntry.COLUMN_BACKDROP + " TEXT NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_RATING + " REAL NOT NULL, " +

                MoviesContract.FavoritesEntry.COLUMN_NUMBER_VOTES + " INTEGER NOT NULL, " +
                MoviesContract.FavoritesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL " +
              //  MoviesContract.FavoritesEntry.COLUMN_WIND_SPEED + " REAL NOT NULL, " +
              //  MoviesContract.FavoritesEntry.COLUMN_DEGREES + " REAL NOT NULL, "
                ");";

        Log.d("Query", SQL_CREATE_FAVORITES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
      }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesContract.FavoritesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
