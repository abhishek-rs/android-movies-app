package io.github.abhishek_rs.sheksmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by I311917 on 1/26/2016.
 */
public class PosterAdapter extends ArrayAdapter<String> {
    private static final String LOG_TAG = PosterAdapter.class.getSimpleName();
    public Context my_context;
    private LayoutInflater inflater;

    public PosterAdapter(Activity context, List<String> urls) {
        super(context, 0, urls);
        my_context = context;
        inflater = LayoutInflater.from(my_context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v;
        if(convertView == null){
            v = inflater.inflate(R.layout.imageview, parent, false);
        }
        else{
            v = (View) convertView;
        }

        ImageView iv = (ImageView) v.findViewById(R.id.poster_imageView);
        String url = getItem(position);

        Picasso.with(my_context).load(url).resize(360,540).into(iv); //for Nexus 5 resize(540,720)
        return v;
    }
}
