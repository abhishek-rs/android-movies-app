package io.github.abhishek_rs.sheksmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

/**
 * Created by I311917 on 3/11/2016.
 */
public class MoviesProvider extends ContentProvider {
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int FAVORITE = 100;
    static final int FAVORITE_WITH_ID = 101;
    static final int FAVORITE_POSTER = 102;
    static final int FAVORITE_BACKDROP = 103;


    private static final SQLiteQueryBuilder sFavoriteQueryBuilder;

    static {
        sFavoriteQueryBuilder = new SQLiteQueryBuilder();
    }
        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
    /*    sFavoriteQueryBuilder.setTables(
                MoviesContract.FavoritesEntry.TABLE_NAME +
                        " ON " + WeatherContract.WeatherEntry.TABLE_NAME +
                        "." + WeatherContract.WeatherEntry.COLUMN_LOC_KEY +
                        " = " + WeatherContract.LocationEntry.TABLE_NAME +
                        "." + WeatherContract.LocationEntry._ID);
    } */

    private static final String sFavoriteIdSelection =
            MoviesContract.FavoritesEntry.TABLE_NAME+
                    "." + MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ? ";

    private Cursor getFavoriteById(Uri uri, String[] projection, String sortOrder) {
        String id = MoviesContract.FavoritesEntry.getMovieIdFromUri(uri);
        String[] selectionArgs;
        String selection;
        selection = sFavoriteIdSelection;
        selectionArgs = new String[]{id};

        return sFavoriteQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    /*
        Students: Here is where you need to create the UriMatcher. This UriMatcher will
        match each URI to the WEATHER, WEATHER_WITH_LOCATION, WEATHER_WITH_LOCATION_AND_DATE,
        and LOCATION integer constants defined above.  You can test this by uncommenting the
        testUriMatcher test within TestUriMatcher.
     */
    static UriMatcher buildUriMatcher() {
        // 1) The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case. Add the constructor below.

        final UriMatcher newUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        // 2) Use the addURI function to match each of the types.  Use the constants from
        // WeatherContract to help define the types to the UriMatcher.

        newUriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES, FAVORITE);
        newUriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES + "/#", FAVORITE_WITH_ID);
        newUriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES + "/#" +"/poster", FAVORITE_POSTER);
        newUriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_FAVORITES + "/#" +"/backdrop", FAVORITE_BACKDROP);

        return newUriMatcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case FAVORITE_WITH_ID:
                return MoviesContract.FavoritesEntry.CONTENT_ITEM_TYPE;
            case FAVORITE:
                return MoviesContract.FavoritesEntry.CONTENT_TYPE;
            case FAVORITE_POSTER:
                return MoviesContract.FavoritesEntry.CONTENT_ITEM_TYPE;
            case FAVORITE_BACKDROP:
                return MoviesContract.FavoritesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;

        switch (sUriMatcher.match(uri)) {
            // "weather/*/*"
            case FAVORITE_WITH_ID:
            {
                retCursor = getFavoriteById(uri, projection, sortOrder);
                break;
            }
            // "weather"
            case FAVORITE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case FAVORITE_POSTER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.FavoritesEntry.TABLE_NAME,
                        new String[]{MoviesContract.FavoritesEntry.COLUMN_POSTER},
                        MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID,
                        new String[]{getMovieIdFromUri(uri)},
                        null,
                        null,
                        null);
                Log.d("In provider", "Reached here");
                break;
            }
            case FAVORITE_BACKDROP: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MoviesContract.FavoritesEntry.TABLE_NAME,
                        new String[]{MoviesContract.FavoritesEntry.COLUMN_BACKDROP},
                        MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID,
                        new String[]{getMovieIdFromUri(uri)},
                        null,
                        null,
                        null);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case FAVORITE: {
                long _id = db.insert(MoviesContract.FavoritesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MoviesContract.FavoritesEntry.buildFavoritesUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    public String getMovieIdFromUri(Uri uri){
        return uri.getPathSegments().get(1);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Student: Start by getting a writable database

        // Student: Use the uriMatcher to match the WEATHER and LOCATION URI's we are going to
        // handle.  If it doesn't match these, throw an UnsupportedOperationException.

        // Student: A null value deletes all rows.  In my implementation of this, I only notified
        // the uri listeners (using the content resolver) if the rowsDeleted != 0 or the selection
        // is null.
        // Oh, and you should notify the listeners here.

        // Student: return the actual rows deleted
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnval = 0;

        switch (match) {
            case FAVORITE:
                returnval = db.delete(MoviesContract.FavoritesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknow Uri:" + uri);
        }
        if(returnval != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return returnval;
    }


    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Student: This is a lot like the delete function.  We return the number of rows impacted
        // by the update.
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnval = 0;

        switch (match) {

            case FAVORITE:
                returnval = db.update(MoviesContract.FavoritesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknow Uri:" + uri);
        }
        if(returnval != 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return returnval;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FAVORITE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MoviesContract.FavoritesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
