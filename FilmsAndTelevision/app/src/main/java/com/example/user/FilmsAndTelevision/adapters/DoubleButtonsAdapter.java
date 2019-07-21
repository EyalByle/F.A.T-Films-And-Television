package com.example.user.FilmsAndTelevision.adapters;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;

public class DoubleButtonsAdapter extends ArrayAdapter<DoubleButtons>
{
    private static class Buttons
    {
        Button leftButton;
        Button rightButton;
    }
    private int textSize;
    private View.OnClickListener listener;
    private int itemHeight;

    public DoubleButtonsAdapter(@NonNull Context context, @LayoutRes int resource,
                                int textSize, View.OnClickListener listener)
    {
        super(context, resource);
        this.textSize = textSize;
        this.listener = listener;
        this.itemHeight = 0;
    }

    public void setItemHeight(int totalHeight)
    {
        this.itemHeight = totalHeight / getCount();
        System.out.println("itemHeight: " + this.itemHeight);
    }

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        DoubleButtons item = getItem(position);
        Buttons buttons;
        if (convertView == null)
        {
            buttons = new Buttons();
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.double_buttons_list, parent, false);
            buttons.leftButton = convertView.findViewById(R.id.LeftButton);
            buttons.rightButton = convertView.findViewById(R.id.RightButton);
            convertView.setTag(buttons);
            System.out.println("Item is: " + item);
            if (item != null)
            {
                buttons.leftButton.setText(item.getLeft());
                buttons.rightButton.setText(item.getRight());
                buttons.leftButton.setTextSize(this.textSize);
                buttons.rightButton.setTextSize(this.textSize);
                int leftColour = item.getLeftColour();
                if (leftColour != -1)
                {
                    buttons.leftButton.setBackgroundResource(leftColour);
                    buttons.rightButton.setBackgroundResource(item.getRightColour());
                }
            }
            buttons.leftButton.setTag(position * 2);
            buttons.rightButton.setTag(position * 2 + 1);
            buttons.leftButton.setOnClickListener(this.listener);
            buttons.rightButton.setOnClickListener(this.listener);

            // Right matches left by default
            if (Methods.viewString(buttons.leftButton).length() < Methods.viewString(buttons.rightButton).length())
            {
                LayoutParams leftParams = (LayoutParams)buttons.leftButton.getLayoutParams();
                LayoutParams rightParams = (LayoutParams)buttons.rightButton.getLayoutParams();
                leftParams.height = 0;
                rightParams.height = LayoutParams.WRAP_CONTENT;
                buttons.leftButton.setLayoutParams(leftParams);
                buttons.rightButton.setLayoutParams(rightParams);
                convertView.requestFocus();
            }
            System.out.println("\t\tLeft: " + Methods.viewString(buttons.leftButton));
            System.out.println("\t\tRight: " + Methods.viewString(buttons.rightButton));
        }
        if (itemHeight != 0)
        {
            buttons = (Buttons)convertView.getTag();
            buttons.leftButton.setMinHeight(0);
            buttons.leftButton.setMinimumHeight(0);
            buttons.leftButton.setHeight(itemHeight);
            buttons.rightButton.setMinHeight(0);
            buttons.rightButton.setMinimumHeight(0);
            buttons.rightButton.setHeight(itemHeight);
//            convertView.setMinimumHeight(itemHeight);
        }

        return convertView;
    }
}
