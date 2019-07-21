package com.example.user.FilmsAndTelevision;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
//import java.util.HashSet;

public class Methods
{
    public static HashMap<String, String> map;
    public static String [] stringSeparators;

    public static void setStringSeparators(String st)
    {
        stringSeparators = splitString(st);
        System.out.println("PRINTABLE:" + stringSeparators.length + ", " + stringSeparators[0]);
    }
    public static String encrypt(String st)
    {
        return st;
    }
    public static String decrypt(String st)
    {
        return st;
    }
    /*public static String viewString(TextView view) {
        return view.getText().toString();
    }*/
    public static String viewString(View view)
    {
        if (view instanceof TextView)
        {
            return ((TextView) view).getText().toString();
        }
        return "Error";
    }

    public static String sendRecv(String send)
    {
        System.out.println("SENDRECV START");
        Client.sent_text = send;
        new Thread(new Client.SendToServer()).start();
        Client.waitForSock();
        System.out.println("SENDRECV MID");
        new Thread(new Client.ReceiveFromServer()).start();
        Client.waitForSock();
        System.out.println("SENDRECV END");
        return Client.received_text;
    }

    public static String [] splitString(String data)
    {
        /**
         * First time docs
         **/
        //System.out.println("DATA:" + data);
        String sep = data.substring(data.indexOf('(') + 1, data.indexOf(")"));
        //System.out.println("SEP:" + sep);
        //System.out.println("NEW: " + data.substring(data.indexOf(")") + 1));
        //System.out.println("NEW_LEN: " + data.substring(data.indexOf(")") + 1).split(sep).length);
        return data.substring(data.indexOf(")") + 1).split(sep);
    }

    public static String joinArray(String... arr)
    {
        String joined = TextUtils.join("", arr);
        for (String string: stringSeparators)
        {
            if (!joined.contains(string))
            {
                return "(" + string + ")" + TextUtils.join(string, arr);
            }
        }
        return TextUtils.join("|", arr);
    }
    public static String joinArray(String start, int num, String... arr)
    {
        /**
         * param start: starting string of the result
         * param num: only there to differ this method from the other one with the same name
         * param arr: array of strings
         */
        System.out.println(arr[0] + "\n" + start);
        String joined = joinArray(arr);
        System.out.println(joined);
        for (String string: stringSeparators)
        {
            if ((!joined.contains(string))&&(!start.contains(string)))
            {
                System.out.println("JOINED:" + "(" + string + ")" + start + string + joined);
                return "(" + string + ")" + start + string + joined;
            }
        }
        return start + ":" + joined;
    }

    public static String joinArray(ArrayList<String> list)
    {
        String [] strings = new String[list.size()];
        for (int i = 0; i < strings.length; i++)
        {
            strings[i] = list.get(i);
            //Adding to normal list here and passing to joinarray
        }
        return joinArray(strings);
    }

    public static String [][] copy2DArray(String [][] arr)
    {
        String [][] arr2 = new String[arr.length][arr[0].length];
        for (int i = 0; i < arr2.length; i++)
        {
            for (int j = 0; j < arr2[i].length; j++)
            {
                arr2[i][j] = arr[i][j];
            }
        }
        return arr2;
    }

    public static HashMap<String, String> stringToTitle(String raw)
    {
        String [] data = Methods.splitString(raw), splitArr;
        HashMap<String, String> valuesMap = new HashMap<>();

        for (int i = 1; i < data.length; i++)
        {
            splitArr = Methods.splitString(data[0] + data[i]);
            valuesMap.put(splitArr[0], splitArr[1]);
        }
        System.out.println("~~~Done getting title~~~");
        return valuesMap;
    }

    // For "aaaa", '0', 10 -> 000000aaaa
    public static String zfill(String string, int length)
    {
        int len = string.length();
        if (len >= length)
        {
            return string;
        }

        return String.format("%0" + (length - len) + "d", 0) + string;
    }
}
