package com.coremachine;

public class CoreMachine
{
    private static CoreMachine instance;

    public static CoreMachine instance()
    {
        if (instance == null)
        {
            instance = new CoreMachine();
        }
        return instance;
    }
}
