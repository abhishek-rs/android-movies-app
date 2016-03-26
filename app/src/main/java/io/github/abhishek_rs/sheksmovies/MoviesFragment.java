package io.github.abhishek_rs.sheksmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MoviesFragment extends Fragment implements FetchMoviesTask.FragmentCallback {
    public static PosterAdapter posterAdapter;
    private int pageNo;
    public static int IS_FAVORITE;
    public static List<Movie> movies = new ArrayList<Movie>();
    FloatingActionButton page_down, page_up;
    private static final String SELECTED_KEY = "selected_position";
    public int mPosition = GridView.INVALID_POSITION;
    public GridView gridView;

   // public FetchMoviesTask.FragmentCallback fg = (FetchMoviesTask.FragmentCallback) getActivity();

    @Override
    public void updateData(String[] results) {
        if (results != null) {
            posterAdapter.clear();
            for(String posterStr : results) {
                posterAdapter.add(posterStr);
            }
        }

    }

public interface Callback{
    public void onItemSelected(Movie movie, int position);
}

    private void updateList(){
        movies.clear();
        FetchMoviesTask task = new FetchMoviesTask(getActivity(), this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String sortBy = prefs.getString(getString(R.string.pref_sortBy_list_key),getString(R.string.pref_sortBy_list_default));
        String[] params = new String[2];

        if(IS_FAVORITE == 0) {
            page_down.setVisibility(View.VISIBLE);
            page_up.setVisibility(View.VISIBLE);
        }
        else {
            page_down.setVisibility(View.GONE);
            page_up.setVisibility(View.GONE);

        }

        switch(sortBy){
            case "1": params[0] = "vote_average.desc";
                        IS_FAVORITE = 0;
                        break;
            case "2": params[0] = "revenue.desc";
                IS_FAVORITE = 0;
                break;

            case "3": params[0] = "release_date.desc";
                IS_FAVORITE = 0;
                break;
            case "4": params[0] = "favorites";
                IS_FAVORITE = 1;
                break;
            default : params[0] = "popularity.desc";
                IS_FAVORITE = 0;
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
        if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            gridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
        if (mPosition != GridView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            gridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != GridView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(outState);
    }

    public MoviesFragment() {
        // Log.v("Hello", "MAN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        pageNo = 1;
        page_down = (FloatingActionButton) rootView.findViewById(R.id.pageInc);
        page_up = (FloatingActionButton) rootView.findViewById(R.id.pageDec);

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
                        // The listview probably hasn't even been populated yet.  Actually perform the
                                // swapout in onLoadFinished.
                                        mPosition = savedInstanceState.getInt(SELECTED_KEY);
                    }



            page_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (pageNo < 999) {
                        pageNo++;
                        Toast.makeText(getActivity(), "Page " + pageNo, Toast.LENGTH_SHORT).show();
                        updateList();
                    } else {
                        Toast.makeText(getActivity(), "That's all folks!", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        page_down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pageNo < 999) {
                    pageNo++;
                    Toast.makeText(getActivity(), "Page " + pageNo, Toast.LENGTH_SHORT).show();
                    updateList();
                } else {
                    Toast.makeText(getActivity(), "That's all folks!", Toast.LENGTH_SHORT).show();
                }
            }
        });



        page_up.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (pageNo > 1) {
                        pageNo--;
                        Toast.makeText(getActivity(), "Page " + pageNo, Toast.LENGTH_SHORT).show();
                        updateList();
                    } else {
                        Toast.makeText(getActivity(), "There's only one way to go. That's up!", Toast.LENGTH_SHORT).show();
                    }
                }
            });


        posterAdapter = new PosterAdapter(getActivity(), new ArrayList<String>()/* Arrays.asList(data) */);

        gridView = (GridView) rootView.findViewById(R.id.poster_gridView);
        gridView.setAdapter(posterAdapter);
        updateList();
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        /*        String movie_title = movies.get(position).title;
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
                        .putExtra("release_date", release_date);  */
             //   Intent detailIntent = new Intent(getActivity(),DetailActivity.class).putExtra("movie", movies.get(position)).putExtra("position", position);

    //            startActivity(detailIntent);

                ((Callback) getActivity())
                        .onItemSelected(movies.get(position), position);
                mPosition = position;
            }
        });

        return rootView;
    }



}


