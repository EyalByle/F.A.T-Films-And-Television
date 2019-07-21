package com.example.user.FilmsAndTelevision.adapters;

public class DoubleButtons
{
    private String left;  // Left button
    private String right; // Right button
    private int colourL;   // Colour resource - left side
    private int colourR;   // Colour resource - right side

    public DoubleButtons(String left, String right)
    {
        this.left = left;
        this.right = right;
        this.colourL = -1;  // Default colour
        this.colourR = -1;
    }

    public DoubleButtons(String left, String right, int colourL, int colourR)
    {
        this.left = left;
        this.right = right;
        this.colourL = colourL;
        this.colourR = colourR;
    }

    public String getLeft()
    {
        return this.left;
    }

    public int getLeftColour()
    {
        return this.colourL;
    }

    public String getRight()
    {
        return this.right;
    }

    public int getRightColour()
    {
        return this.colourR;
    }
}
