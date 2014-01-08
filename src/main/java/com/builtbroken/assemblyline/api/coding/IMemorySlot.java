package com.builtbroken.assemblyline.api.coding;

import java.util.HashMap;

/** Used by task to tell the program it needs to remember a value outside the task. Should only be
 * used by task that really need to save values beyond there local values. Cases were this is used
 * should be is loops, items counts, and run conditions.
 * 
 * @author DarkGuardsman */
public interface IMemorySlot
{
    /** Called per update to store the changes in memory. If return is null the memory location will
     * be released. Make sure to do this if the value is no longer needed. Memory is limited to
     * basic java variables, and will not accept arrays, or collections */
    public Object getMemory(String name);

    /** All memory locations needed by this task with there names, data types, and starting/current
     * values. Take care that this is designed to keep data over several program cycles. This is not
     * used to save local data. As well machines have limited memory of only a few active vars. */
    public HashMap<String, Object> getMemory();

    /** Any memory location that needs to be saved to the machines hard disk. Should only do this for
     * information that must be saved. Treat this as real world memory to hard drive saving. As well
     * if the machine is running between world saves its active memory will be save. However, if it
     * turns off its active memory will clear. Called as the task is terminated. */
    public HashMap<String, Object> getSavedData();
}
