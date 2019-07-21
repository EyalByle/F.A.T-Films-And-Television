package com.example.user.FilmsAndTelevision;

import java.util.ArrayList;

public class User
{
    public static String email = "";
    public static String username = "";
    public static ArrayList<String> requests = new ArrayList<>();

    public static int theme = R.style.DarkTheme;
    public static int [] languages = {0, 0, 0}; //All 3 are None until a real value has been given
}
/*TODO LIST
    New:
        Merge crew screen with title screen
        Make lists update after a change happens
        Make titlesList update after a rating change
        Add advanced search option (filters)
            title's name
            year (before, after or between)
            rating (above or below)
            genres (keep / hide)
            parents guide
            length
            languages
        sort titles list
            alphabetical order
            release year
            rating
            length
            raters count
        edit list name
        edit bookmarks and lists (move titles up/down, delete titles on-place)
        make humans screen? (screen for a living being that contains their movies and shows)
    Documentation
    Encrypt the messages
    Watched list
    Edit title option
    Update title option
    Download videos option

    Fixed:
        Difference between cast and others
        Capitalize the roles in the personals
        Save login
        Register
        Video streaming
        Bookmarks
        Subtitles
        Add search to menu
        Activities in a folder, adapters in a folder
        Add_list -> addList in PersonalScreen
        Streaming -> gotoStream in TitleScreen
        Add 2 buttons adapter
        stop flashing when trying to play non-existing file
        Buttons in bad size in title screen
 */