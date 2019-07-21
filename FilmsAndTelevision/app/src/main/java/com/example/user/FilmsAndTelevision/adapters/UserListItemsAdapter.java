package com.example.user.FilmsAndTelevision.adapters;

import android.content.Context;
import android.support.annotation.*;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;

import java.util.ArrayList;

/**
 * Created by User on 22/06/2018.
 */

public class UserListItemsAdapter extends ArrayAdapter<UserListItem>
{
    private static int selectedCount;
    private ArrayList<UserListItem> items = new ArrayList<>();
    private static class Views
    {
        CheckBox inList;
        TextView listName;
        int position;
    }
    public UserListItemsAdapter(@NonNull Context context, @LayoutRes int resource)
    {
        super(context, resource);
        selectedCount = 0;
    }
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        UserListItem item = getItem(position);
        Views views;
        //if (item.getName().equals(items.get(position).getName()))
        if (convertView == null)
        {
            views = new Views();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.change_lists_list, parent, false);
            views.inList = convertView.findViewById(R.id.ItemCheck);
            views.listName = convertView.findViewById(R.id.ItemText);
            views.position = position;
            items.add(item);
            convertView.setTag(views);
            if (item == null)
            {
                System.out.println("This item is somehow thing null");
            }
            else
            {
                views.inList.setChecked(item.isChecked());
                views.listName.setText(item.getName());
                System.out.println("Doing things here: " + Methods.viewString(views.listName));
            }
        }
        else
        {
            return convertView;
            //views = (Views)convertView.getTag();
        }
        final CheckBox checkBox = views.inList;

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                // Populate the listInfo with check box status
                System.out.println("CHANGE");
                //throw new NullPointerException("Oops");
                items.get(position).setChecked(isChecked);
                selectedCount--;
                if (isChecked)
                {
                    selectedCount += 2;
                }
            }
        });
        views.listName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                System.out.println("CLICK");
                checkBox.performClick();
            }
        });
        views.inList = checkBox;
        return convertView;
    }

    public static int getSelectedCount()
    {
        return UserListItemsAdapter.selectedCount;
    }
}
