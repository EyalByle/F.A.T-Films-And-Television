package com.example.user.FilmsAndTelevision.adapters;

import android.content.Context;
import android.support.annotation.*;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.adapters.Title;

/**
 * Created by User on 22/06/2018.
 */

public class TitlesAdapter extends ArrayAdapter<Title>
{
    private static class Views
    {
        ImageView image;
        TextView name;
        TextView date;
        TextView rating;
    }
    public TitlesAdapter(@NonNull Context context, @LayoutRes int resource)
    {
        super(context, resource);
    }
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Title title = getItem(position);
        Views views;
        if (convertView == null)
        {
            views = new Views();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.titles_list, parent, false);
            views.image = convertView.findViewById(R.id.Poster);
            views.name = convertView.findViewById(R.id.Title);
            views.date = convertView.findViewById(R.id.Date);
            views.rating = convertView.findViewById(R.id.Rating);
            convertView.setTag(views);
        }
        else
        {
            views = (Views)convertView.getTag();
        }
        if (title != null)
        {
            title.setImage(views.image);
            views.name.setText(title.name);
            views.date.setText(title.date);
            if (title.getRatingString().length() > 0)
            {
                views.rating.setText(title.getRatingString());
                views.rating.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }
}
