package io.github.abhishek_rs.sheksmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MoviesFragment extends Fragment {
    private PosterAdapter posterAdapter;
    private int pageNo;
    List<Movie> movies = new ArrayList<Movie>();
    private void updateList(){
        movies.clear();
        FetchMoviesTask task = new FetchMoviesTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sortBy = prefs.getString(getString(R.string.pref_sortBy_list_key),getString(R.string.pref_sortBy_list_default));
        String[] params = new String[2];

        switch(sortBy){
            case "1": params[0] = "vote_average.desc";
                        break;
            case "2": params[0] = "revenue.desc";
                break;

            case "3": params[0] = "release_date.desc";
                break;
            default : params[0] = "popularity.desc";
                break;
        }

        params[1] = Integer.toString(pageNo);
        //Toast.makeText(getActivity(), sortBy, Toast.LENGTH_LONG).show();
        task.execute(params);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateList();
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    public MoviesFragment() {
        // Log.v("Hello", "MAN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        pageNo = 1;

        FloatingActionButton page_down = (FloatingActionButton) rootView.findViewById(R.id.pageInc);

        page_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pageNo < 999){
                    pageNo++;
                    Toast.makeText(getActivity(), "Page " + pageNo , Toast.LENGTH_SHORT).show();
                    updateList();
                }
                else {
                    Toast.makeText(getActivity(), "That's all folks!", Toast.LENGTH_SHORT).show();
                }
            }
        });


        FloatingActionButton page_up = (FloatingActionButton) rootView.findViewById(R.id.pageDec);

        page_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(pageNo > 1 ){
                    pageNo--;
                    Toast.makeText(getActivity(), "Page " + pageNo , Toast.LENGTH_SHORT).show();
                    updateList();
                }
                else {
                    Toast.makeText(getActivity(), "There's only one way to go. That's up!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        posterAdapter = new PosterAdapter(getActivity(), new ArrayList<String>()/* Arrays.asList(data) */);

        GridView gridView = (GridView) rootView.findViewById(R.id.poster_gridView);
        gridView.setAdapter(posterAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String movie_title = movies.get(position).title;
                String movie_backdrop = movies.get(position).backdrop;
                String movie_plot = movies.get(position).plotSummary;
                double movie_rating =  (movies.get(position).rating);
                String rating = Double.toString(movie_rating);
                long movie_numvotes = movies.get(position).numberVotes;
                String release_date = movies.get(position).release_date;
                String votes = Long.toString(movie_numvotes);
                // Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra("title", movie_title)
                        .putExtra("backdrop", movie_backdrop)
                        .putExtra("plot", movie_plot)
                        .putExtra("rating", rating)
                        .putExtra("votes", votes)
                        .putExtra("release_date", release_date);
                startActivity(detailIntent);
            }
        });

        return rootView;
    }


    public class FetchMoviesTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                posterAdapter.clear();
                for(String posterStr : result) {
                    posterAdapter.add(posterStr);
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
                long votecount;
                String release_date;

                JSONObject movie = moviesArray.getJSONObject(i);

                id = movie.getInt(PARAM_ID);
                plot = movie.getString(PARAM_DESC);
                poster = makeValidImageUrl(movie.getString(PARAM_POSTER));
                backdrop = makeValidImageUrl(movie.getString(PARAM_BACKDROP));
                title = movie.getString(PARAM_TITLE);
                rating = movie.getDouble(PARAM_VOTEAVERAGE);
                votecount = movie.getLong(PARAM_VOTECOUNT);
                release_date = movie.getString(PARAM_RELEASE_DATE);
                movies.add(new Movie(id, title, plot, backdrop, rating, votecount, release_date));
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
}


