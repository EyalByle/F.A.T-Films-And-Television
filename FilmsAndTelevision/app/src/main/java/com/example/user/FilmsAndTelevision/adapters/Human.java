package com.example.user.FilmsAndTelevision.adapters;

public class Human
{
    String name;
    String role;
    public Human(String... data)
    {
        this.name = data[0];
        this.role = data[1];
        System.out.println("NAME: " + this.name);
        System.out.println("ROLE: " + this.role);
    }
}
