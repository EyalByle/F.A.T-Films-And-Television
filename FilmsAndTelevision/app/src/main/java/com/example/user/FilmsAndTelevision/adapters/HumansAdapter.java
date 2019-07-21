package com.example.user.FilmsAndTelevision.adapters;

import android.content.Context;
import android.support.annotation.*;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.adapters.Human;

/**
 * Created by User on 22/06/2018.
 */

public class HumansAdapter extends ArrayAdapter<Human>
{
    private String type;
    private static class Views
    {
        TextView name;
        TextView role;
    }

    public HumansAdapter(@NonNull Context context, @LayoutRes int resource, String type)
    {
        super(context, resource);
        this.type = type;
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {

        Views views;
        if (convertView == null)
        {
            views = new Views();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.crew_list, parent, false);
            views.name = (TextView)convertView.findViewById(R.id.Name);
            views.role = (TextView)convertView.findViewById(R.id.Role);
            convertView.setTag(views);
        }
        else
        {
            views = (Views)convertView.getTag();
        }
        Human human = getItem(position);
        if (human != null)
        {
            views.name.setText(human.name);
            views.role.setText(human.role);
        }
        //Client.waitForSock();
        //System.out.println("WAITING IS OVER");
        return convertView;
    }
}
