package io.github.abhishek_rs.sheksmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.github.abhishek_rs.sheksmovies.data.MoviesContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    public List<Trailer> trailers = new ArrayList<Trailer>();
    private List<Review> reviews = new ArrayList<Review>();
    private final int FETCH_TRAILER_LIST = 2;
    private final int FETCH_REVIEW_LIST = 1;
    Intent shareIntent = new Intent(Intent.ACTION_SEND);

    private ShareActionProvider mShareActionProvider;

    public DetailActivityFragment() {
    }

  @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);
      inflater.inflate(R.menu.menu_detail, menu);
        MenuItem item = menu.findItem(R.id.action_shared);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        // Fetch and store ShareActionProvider
        // mShareActionProvider = (ShareActionProvider) item.getActionProvider();
      //mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    public Intent createShareForecastIntent() {

        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");


        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorite) {
            Bundle arguments = getArguments();
            Movie m = arguments.getParcelable("movie");
            int pos = arguments.getInt("position");

            int count;
            Uri uri = MoviesContract.FavoritesEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(m.id)).appendPath("poster").build();
            Log.d ("URI", uri.toString());


            try {
                //(this.getContentResolver().query(uri, null, null, null, null, null)).getCount();
                count = (getActivity()
                        .getContentResolver()
                        .query(MoviesContract.FavoritesEntry.CONTENT_URI,
                                null,
                                MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID + "= ?",
                                new String[]{Integer.toString(m.id)},
                                null)).getCount();


            }
            catch (Exception e) {

                count = 0;
            }

            if (count == 0 || MoviesFragment.IS_FAVORITE == 1)
            {
                insertIntoDBTask idb = new insertIntoDBTask(getActivity(), m);
                idb.execute();

                if (MoviesFragment.IS_FAVORITE == 0) {
                    saveImageToLocal(m.poster, Integer.toString(m.id) + "a", "poster");
                    saveImageToLocal(m.backdrop, Integer.toString(m.id) + "b", "backdrop");
                    Toast.makeText(getActivity(), m.title + " has been added to favorites!", Toast.LENGTH_LONG).show();



                } else {
                    Toast.makeText(getActivity(), m.title + " has been removed from favorites!", Toast.LENGTH_LONG).show();

                    MoviesFragment.posterAdapter.remove(MoviesFragment.posterAdapter.getItem(pos));
                    MoviesFragment.posterAdapter.setNotifyOnChange(true);

                }
            }
            else {
                Toast.makeText(getActivity(), m.title + " is already in your favorites!", Toast.LENGTH_LONG).show();

            }
        }

        return super.onOptionsItemSelected(item);
    }

    public class insertIntoDBTask extends AsyncTask<Void, Void, Void> {
        private Context mContext;
        Movie m;

        public insertIntoDBTask(Context c, Movie movie) {
            mContext = c;
            m = movie;
        }

        @Override
        protected Void doInBackground(Void... params) {

            if (MoviesFragment.IS_FAVORITE == 0) {


                ContentValues cv = new ContentValues();
                cv.put(MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID, m.id);
                cv.put(MoviesContract.FavoritesEntry.COLUMN_PLOT_SUMMARY, m.plotSummary);
                cv.put(MoviesContract.FavoritesEntry.COLUMN_TITLE, m.title);
                cv.put(MoviesContract.FavoritesEntry.COLUMN_RATING, m.rating);
                cv.put(MoviesContract.FavoritesEntry.COLUMN_NUMBER_VOTES, m.numberVotes);
                cv.put(MoviesContract.FavoritesEntry.COLUMN_RELEASE_DATE, m.release_date);


                String posterlocation = getActivity().getApplicationContext().getDir("sheksmovies", getActivity().MODE_PRIVATE) + "/" + m.id + "a.jpg";
                String backdroplocation = getActivity().getApplicationContext().getDir("sheksmovies", getActivity().MODE_PRIVATE) + "/" + m.id + "b.jpg";

                cv.put(MoviesContract.FavoritesEntry.COLUMN_BACKDROP, backdroplocation);
                cv.put(MoviesContract.FavoritesEntry.COLUMN_POSTER, posterlocation);

                mContext.getContentResolver().insert(
                        MoviesContract.FavoritesEntry.CONTENT_URI,
                        cv
                );


            } else {
                mContext.getContentResolver().delete(
                        MoviesContract.FavoritesEntry.CONTENT_URI,
                        MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{Integer.toString(m.id)}
                );


            }
            return null;
        }


    }

    public void saveImageToLocal(String url, String title, String type) {
        final String localtitle = title;
        final String localtype = type;
        Picasso.with(getActivity()).load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {

                    File myDir = getActivity().getApplicationContext().getDir("sheksmovies", getActivity().MODE_PRIVATE);
                    if (!myDir.exists()) {
                        myDir.mkdirs();
                    }
                    String name = localtitle + ".jpg";
                    File Dir = new File(myDir, name);
                    //retPath = Dir.getPath();
                    //  Log.d("retpath", retPath);
                    FileOutputStream out = new FileOutputStream(Dir);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        });
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_detail, container, false);
       // Intent intent = getActivity().getIntent();
        setHasOptionsMenu(true);
        Bundle arguments = getArguments();

      /*  if (intent != null && intent.hasExtra("movie")) {
            String moviename = intent.getStringExtra("title");
            String plot = intent.getStringExtra("plot");
            String rating = intent.getStringExtra("rating");
            String votes = intent.getStringExtra("votes");
            String release_date = intent.getStringExtra("release_date");
            getActivity().setTitle(moviename);
            ((TextView) rootview.findViewById(R.id.detail_title_textview)).setText(moviename);
            ((TextView) rootview.findViewById(R.id.detail_plot_textview)).setText(plot);
            ((TextView) rootview.findViewById(R.id.detail_rating_textview)).setText("Rating: " + rating + "/10" );
            ((TextView) rootview.findViewById(R.id.detail_votes_textview)).setText("Total votes: " + votes);
            ((TextView) rootview.findViewById(R.id.detail_release_date_textview)).setText("Release date: " + release_date);
            Picasso.with(getActivity()).load(intent.getStringExtra("backdrop")).resize(720,360).into((ImageView) rootview.findViewById(R.id.detail_imageView));

        */
        if (arguments != null){
            Movie m = arguments.getParcelable("movie");
            // String v = m.toString();
            getActivity().setTitle(m.title);
            // Toast.makeText(getActivity(), m.title, Toast.LENGTH_SHORT).show();
            ((TextView) rootview.findViewById(R.id.detail_title_textview)).setText(m.title);
            ((TextView) rootview.findViewById(R.id.detail_plot_textview)).setText(m.plotSummary);
            ((TextView) rootview.findViewById(R.id.detail_rating_textview)).setText(Double.toString(m.rating) + "/10");
            ((TextView) rootview.findViewById(R.id.detail_votes_textview)).setText(Integer.toString(m.numberVotes));
            ((TextView) rootview.findViewById(R.id.detail_release_date_textview)).setText("(Release - " + m.release_date +" )");
            FetchTrailerAndReviewsTask fTask = new FetchTrailerAndReviewsTask(getActivity(), rootview);
            fTask.execute(new String[]{Integer.toString(m.id)});

            if (MoviesFragment.IS_FAVORITE == 1) {
                Picasso.with(getActivity()).load(new File(m.backdrop)).resize(720, 360).into((ImageView) rootview.findViewById(R.id.detail_imageView));
            } else {
                Picasso.with(getActivity()).load(m.backdrop).resize(720, 360).into((ImageView) rootview.findViewById(R.id.detail_imageView));
            }
            //for Nexus 5 the resize suitable is (1080,540)

        }
        else {
            ((TextView) rootview.findViewById(R.id.detail_title_textview)).setText("Welcome to the movies app. Please select a movie for more information on it!");
            ((TextView) rootview.findViewById(R.id.detail_release_date_textview)).setVisibility(View.GONE);
            ((LinearLayout) rootview.findViewById(R.id.detail_trailer_layout)).setVisibility(View.GONE);
            ((LinearLayout) rootview.findViewById(R.id.detail_reviews_layout)).setVisibility(View.GONE);
            ((LinearLayout) rootview.findViewById(R.id.detail_rating_layout)).setVisibility(View.GONE);
            ((LinearLayout) rootview.findViewById(R.id.detail_votes_layout)).setVisibility(View.GONE);


        }

        return rootview;
    }

    public class FetchTrailerAndReviewsTask extends AsyncTask<String, Void, String[]> {
        private Context mContext;
        public View rView;

        public FetchTrailerAndReviewsTask(Context context, View rootview) {
            mContext = context;
            rView = rootview;
        }

        @Override
        protected String[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String[] resultStrings = new String[2];

            String movieId = params[0];

            String reviewsUrl = formUrl(movieId, 1);
            String trailersUrl = formUrl(movieId, 2);

            resultStrings[0] = getStringFromUrl(trailersUrl);
            resultStrings[1] = getStringFromUrl(reviewsUrl);


            return resultStrings;
        }

        @Override
        protected void onPostExecute(String[] resultStrings) {
            LinearLayout trailerlayout = (LinearLayout) rView.findViewById(R.id.detail_trailer_layout);
            LinearLayout reviewlayout = (LinearLayout) rView.findViewById(R.id.detail_reviews_layout);
            try {
                getDataFromJson(resultStrings[0], FETCH_TRAILER_LIST);
                getDataFromJson(resultStrings[1], FETCH_REVIEW_LIST);
                mShareActionProvider.setShareIntent(createShareForecastIntent());
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hi! check out this awesome trailer - " + trailers.get(0).url);
            } catch (Exception e) {
                Log.e("TRAILER FAILED", e.getMessage());
                trailerlayout.setVisibility(View.GONE);
                reviewlayout.setVisibility(View.GONE);
            //    shareIntent.putExtra(Intent.EXTRA_TEXT, "Hi! I wanted to show you a really cool trailer, but can't seem to find it. I owe you a trailer!");
                return;
            }

            for (int i = 0; i < trailers.size(); i++) {

                Button trailerButton = new Button(getActivity());

                final Trailer thistrailer = trailers.get(i);
                trailerButton.setText(thistrailer.name);

              //  trailerButton.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                float scale = getResources().getDisplayMetrics().density;
                int dpAsPixels = (int) (10*scale + 0.5f);
                trailerButton.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
                trailerButton.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = 10;

                trailerButton.setTextColor(getResources().getColor(R.color.colorWhite));
                trailerButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri videoUri = Uri.parse(thistrailer.url);
                        Intent videoIntent = new Intent(Intent.ACTION_VIEW, videoUri);
                        PackageManager packageManager = getActivity().getPackageManager();
                        List<ResolveInfo> activities = packageManager.queryIntentActivities(videoIntent, 0);
                        boolean isIntentSafe = activities.size() > 0;

// Start an activity if it's safe
                        if (isIntentSafe) {
                            startActivity(videoIntent);
                        }
                    }
                });
                trailerlayout.addView(trailerButton, params);

            }

            for (int i = 0; i < reviews.size(); i++) {

                final TextView reviewAuthorView = new TextView(getActivity());
                final TextView reviewContentView = new TextView(getActivity());
                final Review thisreview = reviews.get(i);
                reviewAuthorView.setText(thisreview.author + " says:");
                reviewAuthorView.setTextSize(18);
                float scale = getResources().getDisplayMetrics().density;
                int dpAsPixels = (int) (7*scale + 0.5f);
                reviewAuthorView.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.bottomMargin = 10;
                reviewlayout.addView(reviewAuthorView, params);
                reviewContentView.setText(thisreview.content);
                reviewContentView.setTextSize(15);
                reviewAuthorView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                reviewAuthorView.setTextColor(getResources().getColor(R.color.colorWhite));
                reviewlayout.addView(reviewContentView, params);



            }

        }

        private String formUrl(String movieId, int type) {
            final String APP_ID = "xx";
            final String BASE_URL_TRAILER_LIST = "http://api.themoviedb.org/3/movie";
            final String BASE_URL_REVIEW_LIST = "http://api.themoviedb.org/3/movie";
            final String PARAM_APPID = "api_key";
            Uri uri;
            switch (type) {
                case FETCH_REVIEW_LIST:
                    uri = Uri.parse(BASE_URL_REVIEW_LIST).buildUpon()
                            .appendPath(movieId)
                            .appendPath("reviews")
                            .appendQueryParameter(PARAM_APPID, APP_ID).build();
                    return uri.toString();

                case FETCH_TRAILER_LIST:
                    uri = Uri.parse(BASE_URL_TRAILER_LIST).buildUpon()
                            .appendPath(movieId)
                            .appendPath("videos")
                            .appendQueryParameter(PARAM_APPID, APP_ID).build();
                    return uri.toString();

                default:
                    return null;
            }

        }

        private String getStringFromUrl(String myUrl) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

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
                return buffer.toString();


            } catch (
                    IOException e
                    ) {

                Log.e("MoviesFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
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
        }

        private Void getDataFromJson(String jsonStr, int type)
                throws JSONException {


            final String PARAM_RESULT = "results";
            final String PARAM_NAME = "name";
            final String PARAM_AUTHOR_NAME = "author";
            final String PARAM_CONTENT = "content";
            final String PARAM_KEY = "key";
            final String YOUTUBE_BASE_URL = "https://youtu.be/";

            JSONObject json = new JSONObject(jsonStr);
            JSONArray resultsArray = json.getJSONArray(PARAM_RESULT);

            switch (type) {

                case FETCH_REVIEW_LIST:
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject review = resultsArray.getJSONObject(i);
                    String author = review.getString(PARAM_AUTHOR_NAME);
                    String content = review.getString(PARAM_CONTENT);
                    reviews.add(new Review(author, content));
                }
                    break;

                case FETCH_TRAILER_LIST:
                    for (int i = 0; i < resultsArray.length(); i++) {
                        JSONObject trailer = resultsArray.getJSONObject(i);
                        String name = trailer.getString(PARAM_NAME);
                        String key = trailer.getString(PARAM_KEY);
                        key = YOUTUBE_BASE_URL + key;
                        trailers.add(new Trailer(name, key));
                    }

                default:
                    return null;

            }
            return null;
        }


    }
}