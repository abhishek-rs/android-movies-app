package io.github.abhishek_rs.sheksmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.github.abhishek_rs.sheksmovies.data.MoviesContract;

/**
 * Created by I311917 on 2/7/2016.
 */
public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {
    private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
    private final Context mContext;

    public FetchMoviesTask(Context context) {
        mContext = context;
        //mForecastAdapter = forecastAdapter;

    }

    @Override
    protected void onPostExecute(String[] result) {
        if (result != null) {
            MoviesFragment.posterAdapter.clear();

            for(String posterStr : result) {
                MoviesFragment.posterAdapter.add(posterStr);
                Log.d("On post execute", posterStr);
                // Toast.makeText(getActivity(), posterStr, Toast.LENGTH_LONG).show();
            }

        }

    }

    @Override
    protected String[] doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String movieJsonStr = null;
        String sortBy = params[0];
        String pageNo = params[1];
        if (params[0].equals("favorites"))
        {
            return getFavoritesFromDb();
           // params[0] = "popularity.desc";
        }
        final String APP_ID = "xx";
        final String BASE_URL  = "http://api.themoviedb.org/3/discover/movie";
        final String PARAM_SORTBY = "sort_by";
        final String PARAM_APPID = "api_key";
        final String PARAM_PAGENO = "page";
        final int numMovies = 20;

        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter(PARAM_SORTBY,sortBy)
                .appendQueryParameter(PARAM_PAGENO,pageNo)
                .appendQueryParameter(PARAM_APPID,APP_ID).build();
        String myUrl = uri.toString();
       // String myUrl = formUrl(pageNo, sortBy, FETCH_MOVIE_LIST, 0);
     //   Log.d("URL", myUrl);
     //   movieJsonStr = getStringFromUrl(myUrl);

        try {

            URL url = new URL(myUrl);
            //Log.v(LOG_TAG,"Built url = "+url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }
            movieJsonStr = buffer.toString();


        } catch (
                IOException e
                )
        {

            Log.e("MoviesFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        }


        finally
        {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("MoviesFragment", "Error closing stream", e);
                }
            }
        }

        try {
            return getMovieDataFromJson(movieJsonStr,numMovies);
        }
        catch (JSONException e){
            Log.e("ForecastFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            return null;
        }

    }


    private String[] getFavoritesFromDb(){
        Cursor cursor = mContext.getContentResolver().query(MoviesContract.FavoritesEntry.CONTENT_URI, null, null, null, null);
        int numOfFavorites;
        Log.d("getFavFromDB", "Fav running");
        try {
            numOfFavorites = cursor.getCount();
            cursor.moveToFirst();
           // Toast.makeText(mContext, "Number of favorites" + numOfFavorites, Toast.LENGTH_LONG).show();
            String[] posterStrings = new String[numOfFavorites];
            for(int i = 0; i < numOfFavorites; i++, cursor.moveToNext()) {

                int id;
                String plot;
                String poster;
                String backdrop;
                String title;
                double rating;
                int votecount;
                String release_date;

                id = cursor.getInt(cursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID));
                plot = cursor.getString(cursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_PLOT_SUMMARY));
                poster = cursor.getString(cursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_POSTER));
                backdrop = cursor.getString(cursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_BACKDROP));
                title = cursor.getString(cursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_TITLE));
                rating = cursor.getDouble(cursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_RATING));
                votecount = cursor.getInt(cursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_NUMBER_VOTES));
                release_date = cursor.getString(cursor.getColumnIndex(MoviesContract.FavoritesEntry.COLUMN_RELEASE_DATE));
                MoviesFragment.movies.add(new Movie(id, title, plot, backdrop, poster, rating, votecount, release_date));
                posterStrings[i] = poster;
                Log.d("getFavFromDB", "Fav " + poster + id + plot + backdrop + title + rating + votecount + release_date);
            }

            return posterStrings;

        }
        catch (NullPointerException e){
            //Toast.makeText(mContext, "No favorites yet!", Toast.LENGTH_LONG).show();
            return null;
        }

    }

    public String makeValidPosterUrlFromDb(int id){
        return MoviesContract.FavoritesEntry.CONTENT_URI + "/" + id + "/poster";
    }

    public String makeValidBackdropUrlFromDb(int id){
        return MoviesContract.FavoritesEntry.CONTENT_URI + "/" + id + "/backdrop";
    }

    private String[] getMovieDataFromJson(String movieJsonStr, int numMovies)
            throws JSONException {


        final String PARAM_RESULT = "results";
        final String PARAM_ID = "id";
        final String PARAM_POSTER = "poster_path";
        final String PARAM_TITLE = "original_title";
        final String PARAM_VOTECOUNT = "vote_count";
        final String PARAM_VOTEAVERAGE = "vote_average";
        final String PARAM_BACKDROP = "backdrop_path";
        final String PARAM_DESC = "overview";
        final String PARAM_RELEASE_DATE = "release_date";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray moviesArray = movieJson.getJSONArray(PARAM_RESULT);

        String[] posterStrings = new String[numMovies];
        for(int i = 0; i < moviesArray.length(); i++) {

            int id;
            String plot;
            String poster;
            String backdrop;
            String title;
            double rating;
            int votecount;
            String release_date;

            JSONObject movie = moviesArray.getJSONObject(i);

            id = movie.getInt(PARAM_ID);
            plot = movie.getString(PARAM_DESC);
            poster = makeValidImageUrl(movie.getString(PARAM_POSTER));
            backdrop = makeValidImageUrl(movie.getString(PARAM_BACKDROP));
            title = movie.getString(PARAM_TITLE);
            rating = movie.getDouble(PARAM_VOTEAVERAGE);
            votecount = movie.getInt(PARAM_VOTECOUNT);
            release_date = movie.getString(PARAM_RELEASE_DATE);
            MoviesFragment.movies.add(new Movie(id, title, plot, backdrop, poster, rating, votecount, release_date));
            posterStrings[i] = poster;
        }

        return posterStrings;

    }

    public String makeValidImageUrl (String query){
        String BASE_URL  = "http://image.tmdb.org/t/p/w185/" + query;
        Uri uri = Uri.parse(BASE_URL).buildUpon().build();
        String myUrl = uri.toString();
        return myUrl;
    }
}