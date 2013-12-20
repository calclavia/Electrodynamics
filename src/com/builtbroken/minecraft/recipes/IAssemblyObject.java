package com.builtbroken.minecraft.recipes;

/** Applied to objects that are not complete and are being constructed from parts slowly. Used mainly
 * with assembly line armbots to create large objects like rockets automatically.
 * 
 * @author DarkGuardsman */
public interface IAssemblyObject
{
    /** Gets the recipe that this object is being build from */
    public IAssemblyRecipe getRecipe(Object object);

    /** Called each time the assembler makes a change to the object. Use this to trigger render
     * updates of the object */
    public void onChanged(Object object);

    public void setStep(Object object, int step);

    public int getStep(Object object);
}
