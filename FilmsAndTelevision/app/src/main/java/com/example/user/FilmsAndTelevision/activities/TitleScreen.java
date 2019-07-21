package com.example.user.FilmsAndTelevision.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.DownloadImageTask;
import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.adapters.Title;
import com.example.user.FilmsAndTelevision.User;
import com.example.user.FilmsAndTelevision.adapters.UserListItem;
import com.example.user.FilmsAndTelevision.adapters.UserListItemsAdapter;

import java.util.ArrayList;

public class TitleScreen extends MenuActivity implements DialogInterface.OnClickListener
{
    int toggleVisible = 0;
    final int [] visibleToggleList = {View.INVISIBLE, View.VISIBLE};
    final String [] TOGGLE_BOOKMARKS_MESSAGE = {
            "Are you sure you wish to add the %s \"%s\" to your bookmarks?",
            "Are you sure you wish to remove the %s \"%s\" from your bookmarks?"
    };
    int [][] BOOKMARK_BUTTON_CONFIG = {
            {R.string.bookmarks_button_text_remove, 0},
            {R.string.bookmarks_button_text_add, 0}
    };
    enum WatchState
    {
        CanWatch,
        Request,
        Requested
    }
    WatchState state;

    ImageView bigPoster;

    View mainScreen;
    View listsScreen;
    View ratingLayout;

    RatingBar ratingBar;
    ListView userLists;
    Button bookmarks;
    TextView ratingView;
    Button ratingToggle;

    TypedValue attrReference;
    Resources.Theme theme;

    private static Title title = null;
    boolean directly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_screen);
        Intent intent = getIntent();
        System.out.println("~~Activity created~~");

        // ImageViews
        ImageView poster = findViewById(R.id.PosterView);
        bigPoster = findViewById(R.id.FullPoster);

        // TextViews
        TextView titleName = findViewById(R.id.TitleName);
        TextView infoView = findViewById(R.id.InfoView);
        TextView plotView = findViewById(R.id.PlotView);
        ratingView = findViewById(R.id.RatingView);
        TextView ratingTitle = findViewById(R.id.RatingTitleView);

        // Screens
        mainScreen = findViewById(R.id.Screen1);
        ratingLayout = findViewById(R.id.RatingWrapper);
        listsScreen = findViewById(R.id.Screen3);

        // Buttons
        ratingToggle = findViewById(R.id.RatingTitleButton);
        bookmarks = findViewById(R.id.AddToBookmarksButton);

        // Other
        ratingBar = findViewById(R.id.RatingBar);
        attrReference = new TypedValue();
        theme = this.getTheme();
        Log.w("Title screen theme: ", theme.toString());

        // If no title was given: goes to search page
        if (!intent.getBooleanExtra("title", false))
        {
            Intent searchIntent = new Intent(TitleScreen.this, SearchScreen.class);
            startActivity(searchIntent);
        }

        directly = intent.getBooleanExtra("directly", false);
        if (Methods.map.size() > 0)
        {
            title = new Title(Methods.map);
        }

        //
        UserListItemsAdapter adapter = new UserListItemsAdapter(this, R.layout.crew_list);

        String lists = Methods.map.get("lists");
        userLists = findViewById(R.id.UserLists);
        // If no user lists exist
        if ((lists == null)||(lists.startsWith("Error")))
        {
            findViewById(R.id.FirstOption).setVisibility(View.INVISIBLE);
            findViewById(R.id.SecondOption).setVisibility(View.VISIBLE);
        }
        else
        {
            System.out.println(lists);
            String [] userListsNames = Methods.splitString(lists);
            System.out.println("[0]:" + userListsNames[0]);
            System.out.println("LENGTH: " + userListsNames.length);
            for (int i = 0; i < userListsNames.length; i++)
            {
                System.out.println("ITEM: " + userListsNames[i]);
                adapter.add(new UserListItem(Methods.splitString(userListsNames[i])));
            }
            userLists.setAdapter(adapter); // Please change this thing
        }
        Methods.map.clear();
        ArrayList<String> list = title.show();
        titleName.setText(list.remove(0));
        infoView.setText(list.remove(0));
        plotView.setText(list.remove(0));
        ratingView.setText(list.remove(0));
        ratingToggle.setText(list.remove(0));
        int userScore = Integer.parseInt(list.remove(0));
        ratingTitle.setText(String.format("You are currently rating the %s: %s(%s).", title.type, title.name, title.date));

        if (userScore > 0)
        {
            ratingBar.setRating(userScore);
        }

        theme.resolveAttribute(R.attr.TitleBookmarksRemove, attrReference, true);
        BOOKMARK_BUTTON_CONFIG[0][1] = attrReference.resourceId;
        System.out.println("Original color: " + R.color.Red);
        System.out.println("Reference color: " + attrReference.data);
        System.out.println("Reference id: " + attrReference.resourceId);
        theme.resolveAttribute(R.attr.TitleBookmarksAdd, attrReference, true);
        BOOKMARK_BUTTON_CONFIG[1][1] = attrReference.resourceId;

        int bookmarksState = title.bookmarked ? 0: 1;
        bookmarks.setText(BOOKMARK_BUTTON_CONFIG[bookmarksState][0]);
        bookmarks.setBackgroundResource(BOOKMARK_BUTTON_CONFIG[bookmarksState][1]);

        if (title.type.equals("Show"))
        {
            findViewById(R.id.TrailerButton).setVisibility(View.GONE);
        }

        Button watchNow = findViewById(R.id.WatchNowButton);
        if (title.canStream)
        {
            watchNow.setText(R.string.watch_now_button_text_can_watch);
            theme.resolveAttribute(R.attr.TitleWatchNowStateAvailable, attrReference, true);
            state = WatchState.CanWatch;
        }
        else
        {
            if (User.requests.contains(title.imdb))
            {
                theme.resolveAttribute(R.attr.TitleWatchNowStateRequested, attrReference, true);
                watchNow.setText(R.string.watch_now_button_text_requested);
                state = WatchState.Requested;
            }
            else
            {
                theme.resolveAttribute(R.attr.TitleWatchNowStateRequest, attrReference, true);
                watchNow.setText(R.string.watch_now_button_text_request);
                state = WatchState.Request;
            }
        }
        watchNow.setBackgroundResource(attrReference.resourceId);

        title.setImage(poster);
        new DownloadImageTask(bigPoster).execute(title.poster);
    }

    public void submitRating(View view)
    {
        switchRating(view);
        String userRating = Integer.toString((int)ratingBar.getRating());
        String request = Methods.joinArray("RATE_TITLE", 0, title.type, User.email,
                                                                      title.imdb, userRating);
        title.setUserRating(userRating);
        String [] response = Methods.splitString(Methods.sendRecv(request));
        title.setRating(response[0], true);
        ratingView.setText(title.getRatingString());
        ratingToggle.setText(title.getUserRating());
        System.out.println("String: " + title.getRatingString());
        Toast.makeText(TitleScreen.this, response[1], Toast.LENGTH_LONG).show();
    }

    public void gotoCrew(View view)
    {
        Intent intent = new Intent(this, CrewScreen.class);
        for (String [] string : title.getCrew())
        {
            System.out.println("KEY:" + string[0]);
            System.out.println("Value:" + string[1]);
            System.out.println("Length:" + string.length);
            intent.putExtra(string[0], string[1]);
        }
        startActivity(intent);
    }

    public void updateLists(View view)
    {
        System.out.println("UPDATING LISTS");
        Adapter adapter = userLists.getAdapter();
        ArrayList<String> adding = new ArrayList<>();
        ArrayList<String> removing = new ArrayList<>();
        UserListItem item;
        for (int i = 0; i < adapter.getCount(); i++)
        {
            item = (UserListItem)adapter.getItem(i);
            if (item.isChecked())
            {
                adding.add(item.getName());
            }
            else
            {
                removing.add(item.getName());
            }
        }
        String response = Methods.sendRecv(Methods.joinArray("EDIT_LISTS", 0, User.email, Methods.joinArray(adding), Methods.joinArray(removing), title.imdb));
        if (!response.startsWith("Error"))
        {
            response = "Success(?)";
        }
        Toast.makeText(this, response, Toast.LENGTH_LONG).show();
        chooseList(view);
    }

    public void viewToggle(View view)
    {
        mainScreen.setVisibility(visibleToggleList[toggleVisible]);
        toggleVisible = (toggleVisible + 1) % 2;
        view.setVisibility(visibleToggleList[toggleVisible]);
    }

    public void toggleMagnifiedImage(View view)
    {
        viewToggle(bigPoster);
    }

    public void switchRating(View view)
    {
        viewToggle(ratingLayout);
    }

    public void chooseList(View view)
    {
        viewToggle(listsScreen);
    }

    public void tryBookmarks(View view)
    {
        //
        String message = TOGGLE_BOOKMARKS_MESSAGE[title.bookmarked ? 1: 0];
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle("Confirmation")
                .setMessage(String.format(message, title.type, title.name))
                .setPositiveButton("Confirm", this)
                .setNegativeButton("Cancel", this)
                .create();
        dialog.show();
    }

    public void gotoStream(View view)
    {
        switch (state)
        {
            case CanWatch:
                if (title.type.equals("Show"))
                {
                    Intent episodesGuide = new Intent(this, EpisodeSelectScreen.class);
                    episodesGuide.putExtra("IMDb", title.imdb);
                    startActivity(episodesGuide);
                    break;
                }
                Intent stream = new Intent(this, StreamScreen.class);
                stream.putExtra("title", title.imdb);
                startActivity(stream);
                break;
            case Request:
                String request = Methods.joinArray("REQUEST", 0, "stream_requests",
                                                   User.email, title.imdb);
                String response = Methods.sendRecv(request);
                Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                if (response.startsWith("Success"))
                {
                    User.requests.add(title.imdb);
                }
                Button b = findViewById(R.id.WatchNowButton);
                b.setText(R.string.watch_now_button_text_requested);
                theme.resolveAttribute(R.attr.TitleWatchNowStateRequested, attrReference, true);
                // b.setBackgroundResource(R.color.LightBlue);
                b.setBackgroundResource(attrReference.resourceId);
                state = WatchState.Requested;
                break;
            default:
                Toast.makeText(this, "This button does nothing now, please wait until your request has been answered", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public void watchTrailer(View view)
    {
        String trailer = title.getTrailer();
        if ((trailer == null)||(trailer.equals("None found")))
        {
            Toast.makeText(this, "Error: Trailer does not exist", Toast.LENGTH_LONG).show();
            return;
        }

        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailer)));
    }

    public void onClick(DialogInterface dialog, int which)
    {
        switch (which)
        {
            case DialogInterface.BUTTON_POSITIVE:
                //Something about bookmarks
                dialog.dismiss();
                title.bookmarked = !title.bookmarked;
                String request = Methods.joinArray("BOOKMARKS", 0, "set",
                                                   User.email, "" + title.bookmarked, title.imdb);
                String response = Methods.sendRecv(request);
//                String response = Methods.sendRecv(Methods.joinArray("BOOKMARKS", 0, "set", User.email, "" + title.bookmarked, title.imdb));
                if (!response.startsWith("Success"))
                {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    break;
                }

                int bookmarksState = title.bookmarked ? 0: 1;
                bookmarks.setText(BOOKMARK_BUTTON_CONFIG[bookmarksState][0]);
                bookmarks.setBackgroundResource(BOOKMARK_BUTTON_CONFIG[bookmarksState][1]);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                dialog.dismiss();
                Toast.makeText(this, "Canceled.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        if (toggleVisible == 1)
        {
            View view = null;
            if (bigPoster.getVisibility() == View.VISIBLE)
            {
                view = bigPoster;
            }
            else if (ratingLayout.getVisibility() == View.VISIBLE)
            {
                view = ratingLayout;
            }
            else if (listsScreen.getVisibility() == View.VISIBLE)
            {
                view = listsScreen;
            }
            if (view != null)
            {
                viewToggle(view);
                return;
            }
        }
        else if (!directly)
        {
            super.onBackPressed();
            return;
        }
        Intent searchScreen = new Intent(this, SearchScreen.class);
        startActivity(searchScreen);
    }
}
