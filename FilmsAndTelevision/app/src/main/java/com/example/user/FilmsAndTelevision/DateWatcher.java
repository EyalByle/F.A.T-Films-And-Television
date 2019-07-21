package com.example.user.FilmsAndTelevision;

import android.text.Editable;
import android.text.TextWatcher;

public class DateWatcher implements TextWatcher
{

    private static int [] separators = null;
    private static final char SEP = '-';
    private static final String FORMAT = "YYYY-MM-DD";
    private boolean userChange;
    private String text;

    public DateWatcher()
    {
        if (separators == null)
        {
            separators = new int[2];
            separators[0] = FORMAT.indexOf(SEP);
            separators[1] = FORMAT.indexOf(SEP, separators[0] + 1) - 1;
            System.out.println("Separator:" + separators[0] + ", " + separators[1]);
        }
        this.userChange = true;
        this.text = "";
    }

    public static boolean isDate(String date)
    {
        if (date.length() != FORMAT.length())
        {
            return false;
        }
        // For now, should expand the check to the years, months and days
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence sequence, int startIndex, int removed, int added)
    {
        if (this.userChange)
        {
            System.out.println("Before: " + sequence);
        }
    }

    @Override
    public void onTextChanged(CharSequence sequence, int startIndex, int removed, int added)
    {
        if (this.userChange)
        {
            System.out.println("On change: " + sequence);
        }
    }

    @Override
    public void afterTextChanged(Editable editable)
    {
        System.out.println("After: " + editable.toString());
        if ((this.userChange))
        {
            this.userChange = false;
            this.text = editable.toString().replaceAll("[^\\d.]", "");
            //this.text = Methods.viewString(this.view);
            System.out.println("New: " + this.text);
            editable.clear();
            int length = this.text.length();
            if (length > separators[0])
            {
                editable.append(this.text.substring(0, separators[0]) + SEP);
                if (length > separators[1])
                {
                    editable.append(this.text.substring(separators[0], separators[1]) + SEP);
                    editable.append(this.text.substring(separators[1]));
                }
                else
                {
                    editable.append(this.text.substring(separators[0]));
                }
            }
            else
            {
                editable.append(this.text);
            }
            //editable.append(this.text);
            System.out.println("After2: " + editable.toString());
            this.userChange = true;
        }
    }
}