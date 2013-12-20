package dark.api.reciepes;

/** Machine or entity that is creating a AssemblyObject. Avoid actually storing the recipe item if
 * there is one. Instead do what a few other mods do an give the illusion of the recipe being
 * imprinted into the machine while letting the player keep the item
 * 
 * @author DarkGuardsman */
public interface IAssemblier
{
    /** @param assembler - this, used in the case that an item is the assembler, or even a block
     * without a tileEntiy. Eg a Workbench is an example of this as it has no tileEntiy but supports
     * crafting
     * @return current recipe */
    public IAssemblyRecipe getCurrentRecipe(Object object);

    /** @param assembler - this, used in the case that an item is the assembler, or even a block
     * without a tileEntiy. Eg a Workbench is an example of this as it has no tileEntiy but supports
     * crafting
     * @return true if the recipe was set correctly */
    public boolean setCurrentRecipe(Object assembler, IAssemblyRecipe recipe);

    /** @param assembler - this, used in the case that an item is the assembler, or even a block
     * without a tileEntiy. Eg a Workbench is an example of this as it has no tileEntiy but supports
     * crafting
     * @return current work in progress */
    public IAssemblyObject getCurrentWork(Object assembler);

    /** Checks if the recipe can be created by this assembler. Should be used in cases were an
     * assembler is designed for one task type
     * 
     * @param assembler - this, used in the case that an item is the assembler, or even a block
     * without a tileEntiy. Eg a Workbench is an example of this as it has no tileEntiy but supports
     * crafting
     * @param recipe - recipe
     * @return */
    public boolean canSupportRecipe(Object assembler, IAssemblyRecipe recipe);

}
