package io.github.abhishek_rs.sheksmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;

import io.github.abhishek_rs.sheksmovies.data.MoviesContract;

public class DetailActivity extends AppCompatActivity {
    public String retPath;
    private ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    /*    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });  */
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
/*
    private Intent createShareForecastIntent() {

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");

        shareIntent.putExtra(Intent.EXTRA_TEXT, "Hi! check out this awesome trailer - " + DetailActivityFragment.trailers.get(0).url);
        return shareIntent;
    }
*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
  /*      MenuItem item = menu.findItem(R.id.action_shared);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        // Fetch and store ShareActionProvider
        // mShareActionProvider = (ShareActionProvider) item.getActionProvider();
        mShareActionProvider.setShareIntent(createShareForecastIntent());*/
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Nothing here!", Toast.LENGTH_LONG).show();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_favorite) {
            Intent intent = this.getIntent();
            Movie m = intent.getExtras().getParcelable("movie");
            int pos = intent.getExtras().getInt("position");

            int count;
            Uri uri = MoviesContract.FavoritesEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(m.id)).appendPath("poster").build();
            Log.d ("URI", uri.toString());
            /*
            if ( (this
                    .getContentResolver()
                    .query(MoviesContract.FavoritesEntry.CONTENT_URI,
                            null,
                            MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID,
                            new String[]{Integer.toString(m.id)},
                            null)).getCount() > 0 && MoviesFragment.IS_FAVORITE == 0)
            {
                Toast.makeText(this, m.title + " is already in your favorites!", Toast.LENGTH_LONG).show();
            }
            */

            try {
                //(this.getContentResolver().query(uri, null, null, null, null, null)).getCount();
                count = (this
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
                insertIntoDBTask idb = new insertIntoDBTask(this, m);
                idb.execute();

                if (MoviesFragment.IS_FAVORITE == 0) {
                    saveImageToLocal(m.poster, Integer.toString(m.id) + "a", "poster");
                    saveImageToLocal(m.backdrop, Integer.toString(m.id) + "b", "backdrop");
                    Toast.makeText(this, m.title + " has been added to favorites!", Toast.LENGTH_LONG).show();
         /*       if (MoviesFragment.IS_FAVORITE == 0) {

                    ContentValues cv = new ContentValues();
                    cv.put(MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID, m.id);
                    cv.put(MoviesContract.FavoritesEntry.COLUMN_PLOT_SUMMARY, m.plotSummary);
                    cv.put(MoviesContract.FavoritesEntry.COLUMN_TITLE, m.title);
                    cv.put(MoviesContract.FavoritesEntry.COLUMN_RATING, m.rating);
                    cv.put(MoviesContract.FavoritesEntry.COLUMN_NUMBER_VOTES, m.numberVotes);
                    cv.put(MoviesContract.FavoritesEntry.COLUMN_RELEASE_DATE, m.release_date);


                    String posterlocation = getApplicationContext().getDir("sheksmovies", MODE_PRIVATE) + "/" + m.id + "a.jpg";
                    String backdroplocation = getApplicationContext().getDir("sheksmovies", MODE_PRIVATE) + "/" + m.id + "b.jpg";

                    cv.put(MoviesContract.FavoritesEntry.COLUMN_BACKDROP, backdroplocation);
                    cv.put(MoviesContract.FavoritesEntry.COLUMN_POSTER, posterlocation);

                    this.getContentResolver().insert(
                            MoviesContract.FavoritesEntry.CONTENT_URI,
                            cv
                    );


                }

*/


                } else {
                    Toast.makeText(this, m.title + " has been removed from favorites!", Toast.LENGTH_LONG).show();

                    MoviesFragment.posterAdapter.remove(MoviesFragment.posterAdapter.getItem(pos));
                    MoviesFragment.posterAdapter.setNotifyOnChange(true);
                    //Intent mainIntent = new Intent(this,MainActivity.class);
  /*              this.getContentResolver().delete(
                        MoviesContract.FavoritesEntry.CONTENT_URI,
                        MoviesContract.FavoritesEntry.COLUMN_MOVIE_ID + "=?",
                        new String[]{Integer.toString(m.id)}
                );
*/
                }
            }
            else {
                Toast.makeText(this, m.title + " is already in your favorites!", Toast.LENGTH_LONG).show();

            }
        }
       /* else if (id == R.id.menu_item_share){
            Intent intent = getIntent();
            String forecastStr = "OOps!"
            if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            }

            startActivity(shareIntent);
        }*/
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


                String posterlocation = getApplicationContext().getDir("sheksmovies", MODE_PRIVATE) + "/" + m.id + "a.jpg";
                String backdroplocation = getApplicationContext().getDir("sheksmovies", MODE_PRIVATE) + "/" + m.id + "b.jpg";

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
        Picasso.with(this).load(url).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                try {

                    File myDir = getApplicationContext().getDir("sheksmovies", MODE_PRIVATE);
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
}
