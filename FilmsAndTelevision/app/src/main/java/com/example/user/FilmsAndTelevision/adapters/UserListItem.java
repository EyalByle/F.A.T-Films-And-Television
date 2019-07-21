package com.example.user.FilmsAndTelevision.adapters;

public class UserListItem
{
    private String listName;
    private boolean inList;

    public UserListItem(String... strings)
    {
        System.out.println("CREATING A USER LIST ITEM THING");
        this.listName = strings[0];
        this.inList = strings[1].equals("True");
        System.out.println(this.inList);
    }

    public boolean isChecked()
    {
        return this.inList;
    }

    public String getName()
    {
        return this.listName;
    }

    public void setChecked(boolean status)
    {
        this.inList = status;
    }
}
