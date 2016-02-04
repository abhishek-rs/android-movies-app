package io.github.abhishek_rs.sheksmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview =  inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra("title")) {
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
            //for Nexus 5 the resize suitable is (1080,540)
        }

        return rootview;
    }
}
