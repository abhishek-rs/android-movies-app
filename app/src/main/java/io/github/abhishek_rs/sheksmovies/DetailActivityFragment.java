package io.github.abhishek_rs.sheksmovies;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        MenuItem item = menu.findItem(R.id.action_shared);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        // Fetch and store ShareActionProvider
        // mShareActionProvider = (ShareActionProvider) item.getActionProvider();
            mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    public Intent createShareForecastIntent() {

        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");


        return shareIntent;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        setHasOptionsMenu(true);
        Movie m = intent.getExtras().getParcelable("movie");
        if (intent != null && intent.hasExtra("movie")) {
        /*    String moviename = intent.getStringExtra("title");
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
            // String v = m.toString();
            getActivity().setTitle(m.title);
            // Toast.makeText(getActivity(), m.title, Toast.LENGTH_SHORT).show();
            ((TextView) rootview.findViewById(R.id.detail_title_textview)).setText(m.title);
            ((TextView) rootview.findViewById(R.id.detail_plot_textview)).setText(m.plotSummary);
            ((TextView) rootview.findViewById(R.id.detail_rating_textview)).setText(Double.toString(m.rating) + "/10");
            ((TextView) rootview.findViewById(R.id.detail_votes_textview)).setText(Integer.toString(m.numberVotes));
            ((TextView) rootview.findViewById(R.id.detail_release_date_textview)).setText(m.release_date);

/*
            LinearLayout trailers = (LinearLayout) rootview.findViewById(R.id.detail_trailer_layout);
            LinearLayout reviews = (LinearLayout) rootview.findViewById(R.id.detail_reviews_layout);

            for (int i=0;i<5;i++) {

                final Button trailer1 = new Button(getActivity());
                trailer1.setText("Trailer1");
                trailer1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Uri locationUri = Uri.parse("geo:0,0?q=" + trailer1.getText());
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, locationUri);


                        PackageManager packageManager = getActivity().getPackageManager();
                        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
                        boolean isIntentSafe = activities.size() > 0;

// Start an activity if it's safe
                        if (isIntentSafe) {
                            startActivity(mapIntent);
                        }
                    }
                });
                trailers.addView(trailer1);

                TextView review1 = new TextView(getActivity());
                review1.setText("Review BLAHSADSAFSA F");
                reviews.addView(review1);
            }*/

            FetchTrailerAndReviewsTask fTask = new FetchTrailerAndReviewsTask(getActivity(), rootview);
            fTask.execute(new String[]{Integer.toString(m.id)});

            if (MoviesFragment.IS_FAVORITE == 1) {
                Picasso.with(getActivity()).load(new File(m.backdrop)).resize(720, 360).into((ImageView) rootview.findViewById(R.id.detail_imageView));
            } else {
                Picasso.with(getActivity()).load(m.backdrop).resize(720, 360).into((ImageView) rootview.findViewById(R.id.detail_imageView));
            }
            //for Nexus 5 the resize suitable is (1080,540)

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
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hi! check out this awesome trailer - " + trailers.get(0).url);
            } catch (Exception e) {
                Log.e("FETCHING trailers", "FAILED!!");
                trailerlayout.setVisibility(View.GONE);
                reviewlayout.setVisibility(View.GONE);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Hi! I wanted to show you a really cool trailer, but can't seem to find it. I owe you a trailer!");
                return;
            }

            for (int i = 0; i < trailers.size(); i++) {

                final Button trailerButton = new Button(getActivity());
                final Trailer thistrailer = trailers.get(i);
                trailerButton.setText(thistrailer.name);
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
                trailerlayout.addView(trailerButton);

            }

            for (int i = 0; i < reviews.size(); i++) {

                final TextView reviewAuthorView = new TextView(getActivity());
                final TextView reviewContentView = new TextView(getActivity());
                final Review thisreview = reviews.get(i);
                reviewAuthorView.setText("By " + thisreview.author + ":");
                reviewlayout.addView(reviewAuthorView);
                reviewContentView.setText(thisreview.content);
                reviewlayout.addView(reviewContentView);

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