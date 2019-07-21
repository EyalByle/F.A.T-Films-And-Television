package com.example.user.FilmsAndTelevision.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.FilmsAndTelevision.Methods;
import com.example.user.FilmsAndTelevision.R;
import com.example.user.FilmsAndTelevision.adapters.DoubleButtons;
import com.example.user.FilmsAndTelevision.adapters.DoubleButtonsAdapter;
import com.example.user.FilmsAndTelevision.adapters.Title;
import com.example.user.FilmsAndTelevision.adapters.TitlesAdapter;
import com.example.user.FilmsAndTelevision.User;
import com.example.user.FilmsAndTelevision.adapters.UserListItem;
import com.example.user.FilmsAndTelevision.adapters.UserListItemsAdapter;

import java.util.ArrayList;

public class PersonalScreen extends MenuActivity implements AdapterView.OnItemClickListener, View.OnClickListener
{

    int toggleVisible = 0;
    boolean selectDel = false;
    final int [] visibleToggleList = {View.INVISIBLE, View.VISIBLE};
    int listButtonStateDel = 0;
    final int [] LISTS_TITLES_TOGGLE = {R.string.personal_screen_view_list, R.string.personal_screen_delete_list};
    TextView listsTitle;
    View screen1, screen2;
    View listButtons1, listButtons2;
    ArrayList<String> listsNames;
    ListView userLists;
    TitlesAdapter listsAdapter;
    UserListItemsAdapter deleteListsAdapter;
    final View [] userListsScreens = new View[2];

    final String [] BUTTONS_TEXTS = {
            "Rated stuff", "Lists",
            "Bookmarks", "Settings"
            //"Bookmarks", "Watched",
            //"Settings", "Stats",
            //"Request title", "Contact us"
    };
    AlertDialog addDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        this.setTheme(User.theme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_screen);

        final DoubleButtonsAdapter adapter = new DoubleButtonsAdapter(this, R.layout.double_buttons_list, 17, this);
        for (int i = 0; i < BUTTONS_TEXTS.length; i += 2)
        {
            adapter.add(new DoubleButtons(BUTTONS_TEXTS[i], BUTTONS_TEXTS[i + 1]));
        }
        System.out.println("Adapter length: " + adapter.getCount());
        final ListView buttons = findViewById(R.id.AllButtons);

        screen1 = findViewById(R.id.UserBaseLayout);
        screen2 = findViewById(R.id.ListsLayout);
        listButtons1 = findViewById(R.id.StateViewButtons);
        listButtons2 = findViewById(R.id.StateDeleteButtons);
        userListsScreens[0] = findViewById(R.id.ExistingUserListsOption);
        userListsScreens[1] = findViewById(R.id.EmptyUserListsOption);

        TextView username = findViewById(R.id.UsernameTitleView);
        username.setText(getString(R.string.personal_screen_title) + " " + User.username);
        String request = Methods.joinArray("REQUEST", 0, "lists", User.email, "Useless", "false");
        String lists = Methods.sendRecv(request);
        listsAdapter = new TitlesAdapter(this, R.layout.titles_list);
        System.out.println(lists);
        listsNames = new ArrayList<>();
        if (lists.startsWith("Error"))
        {
            userListsScreens[0].setVisibility(View.INVISIBLE);
            userListsScreens[1].setVisibility(View.VISIBLE);
        }
        else
        {
            simpleTitlesAdapter(listsAdapter, lists, listsNames);
            System.out.println("Used thingy" + listsNames.size());
        }
        userLists = findViewById(R.id.UserPersonalLists);
        userLists.setAdapter(listsAdapter);
        userLists.setOnItemClickListener(this);
        String toast = getIntent().getStringExtra("TOAST");
        if (toast != null)
        {
            Toast.makeText(this, toast, Toast.LENGTH_LONG).show();
        }
        listsTitle = findViewById(R.id.UserListsTitle);

        addDialog = buildAddDialog().create();

        Handler handler = new Handler();
        handler.post(new Runnable()
        {
            @Override
            public void run()
            {
                adapter.setItemHeight((int)(buttons.getHeight() * 0.9));
                buttons.setAdapter(adapter);
            }
        });
    }

    /*
        Rated titles    | Lists
        Bookmarks       | Watched
        Settings        | Stats
        ---Disclaimer      | Apply admin
        ???Request title   | Contact us
     */
    @Override
    public void onClick(View view)
    {
        buttonsSwitch(view);
    }
    public void buttonsSwitch(View button)
    {
        switch ((int)button.getTag())
        {
            case 0:  // Rated titles
                gotoRated();
                break;
            case 1:  // User lists
                toggleLists(button);
                break;
            case 2: // User bookmarks
                gotoBookmarks();
                break;
            case 3: // Settings
                gotoSettings();
                break;
            default:
                Toast.makeText(this, Methods.viewString(button), Toast.LENGTH_SHORT).show();
                break;
        }
    }

    public static void simpleTitlesAdapter(TitlesAdapter adapter, String data, ArrayList<String> array)
    {
        if (data.startsWith("Error"))
        {
            return;
        }
        String [] arrData = Methods.splitString(data);
        String [] temp;
        boolean addToArray = array != null;
        for (String st: arrData)
        {
            System.out.println("String: " + st);
            temp = Methods.splitString(st);
            adapter.add(new Title(temp, ""));
            if (addToArray)
            {
                array.add(temp[0]);
            }
        }
    }
    public void openIntent(Intent intent, String request)
    {
        System.out.println("Attempting to open intent:" + intent.toString());
        String response = Methods.sendRecv(request);
        System.out.println("Response:" + response);
        if (response.startsWith("Error"))
        {
            System.out.println("Error thingy");
            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
            return;
        }
        System.out.println("Starting activity");
        intent.putExtra("response", response);
        intent.putExtra("FromPersonals", true);
        startActivity(intent);
    }

    public void gotoRated()
    {
        String request = Methods.joinArray("RATED_TITLES", User.email);
        Intent ratingScreen = new Intent(this, ViewRatingScreen.class);
        ratingScreen.putExtra("request", request);
        startActivity(ratingScreen);
    }

    public void toggleLists(View view)
    {
        screen1.setVisibility(visibleToggleList[toggleVisible]);
        toggleVisible = (toggleVisible + 1) % 2;
        screen2.setVisibility(visibleToggleList[toggleVisible]);
        if (listButtonStateDel > 0)
        {
            deleteLists(view);
        }
    }

    public void gotoBookmarks()
    {
        String request = Methods.joinArray("BOOKMARKS", 0, "get", User.email);
        Intent bookmarks = new Intent(this, TitleListScreen.class);
        bookmarks.putExtra("header", "Showing bookmarks");
        openIntent(bookmarks, request);
    }

    public void gotoSettings()
    {
        Intent settings = new Intent(this, SettingsScreen.class);
        startActivity(settings);
    }

    public void addList(String listName)
    {
        if (listName.length() == 0)
        {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        String response = Methods.sendRecv(Methods.joinArray("EDIT_LISTS", 0, User.email, "create", listName));
        if (response.startsWith("Error"))
        {
            Toast.makeText(this, response, Toast.LENGTH_LONG).show();
            return;
        }
        listsAdapter.clear();
        listsNames.clear();
        simpleTitlesAdapter(listsAdapter, response, listsNames);
        userListsScreens[0].setVisibility(View.VISIBLE);
        userListsScreens[1].setVisibility(View.INVISIBLE);
    }

    public void cancelDelete(View view)
    {
        listButtons1.setVisibility(View.VISIBLE);
        listButtons2.setVisibility(View.INVISIBLE);
        listButtonStateDel = 0;
        selectDel = false;
        userLists.setAdapter(listsAdapter);
    }

    public AlertDialog.Builder buildAddDialog()
    {
        Resources.Theme theme = this.getTheme();
        TypedValue resource = new TypedValue();
        theme.resolveAttribute(R.attr.DialogStyle, resource, true);
        AlertDialog.Builder builder = new AlertDialog.Builder(this, resource.resourceId);
        builder.setTitle("New List's name");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("List's name goes here");
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = (int)getResources().getDimension(R.dimen.padding_medium);
        params.rightMargin = (int)getResources().getDimension(R.dimen.padding_medium);
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int i)
            {
                addList(Methods.viewString(input));
                input.setText("");
                ((AlertDialog)dialog).hide();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int i)
            {
                input.setText(""); //?
                ((AlertDialog)dialog).hide();
            }
        });
        return builder;

    }

    public void showListAddDialog(View view)
    {
        addDialog.show();
    }

    public void deleteLists(View view)
    {
        selectDel = !selectDel;
        listButtons1.setVisibility(visibleToggleList[listButtonStateDel]);
        listButtonStateDel = (listButtonStateDel + 1) % 2;
        listButtons2.setVisibility(visibleToggleList[listButtonStateDel]);
        listsTitle.setText(LISTS_TITLES_TOGGLE[listButtonStateDel]);
        int length = listsNames.size();
        System.out.println("SELECT_DEL: " + selectDel);
        if (selectDel)
        {
            System.out.println("In first option");
            deleteListsAdapter = new UserListItemsAdapter(this, R.layout.change_lists_list);
            for (int i = 0; i < length; i++)
            {
                deleteListsAdapter.add(new UserListItem(listsNames.get(i), "False"));
            }
            System.out.println("Changing adapter to UserListItemAdapter");
            userLists.setAdapter(deleteListsAdapter);
        }
        else
        {
            System.out.println("In second option");
            int length2 = UserListItemsAdapter.getSelectedCount();
            if (length2 > 0)
            {
                String [] lists = new String[length2];
                UserListItem item;
                for (int i = 0, j = 0; i < length; i++)
                {
                    item = (UserListItem) deleteListsAdapter.getItem(i);
                    if ((item != null)&&(item.isChecked()))
                    {
                        lists[j] = item.getName();
                        j++;
                    }
                }
                String names = Methods.joinArray(lists);
                String response = Methods.sendRecv(Methods.joinArray("EDIT_LISTS", 0, User.email, "delete", names));
                if (response.startsWith("Error(Empty)"))
                {
                    System.out.println("Lists are empty");
                    userListsScreens[0].setVisibility(View.INVISIBLE);
                    userListsScreens[1].setVisibility(View.VISIBLE);
                    userLists.setAdapter(listsAdapter);
                    return;
                }
                else if (response.startsWith("Error"))
                {
                    Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    cancelDelete(view);
                    return;
                }
                listsAdapter.clear();
                listsNames.clear();
                simpleTitlesAdapter(listsAdapter, response, listsNames);
                //Call delete from server
                //Delete returns updated lists
                //Show original list
                System.out.println("Changing adapter to titles adapter");
            }
            userLists.setAdapter(listsAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Title listName = (Title)parent.getAdapter().getItem(position);
        String request = Methods.joinArray("REQUEST", 0, "lists", User.email, listName.name);
        Intent titlesList = new Intent(this, TitleListScreen.class);
        titlesList.putExtra("header", "Showing titles in: \"" + listName.name + "\"");
        openIntent(titlesList, request);
    }

    @Override
    public void onBackPressed()
    {
        if (toggleVisible % 2 == 0)
        {
            super.onBackPressed();
            return;
        }
        toggleLists(screen1);
    }
}
