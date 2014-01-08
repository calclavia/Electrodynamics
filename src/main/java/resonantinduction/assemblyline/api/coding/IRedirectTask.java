package resonantinduction.assemblyline.api.coding;

/** Used to tell the program that this task is used to tell the program were to go next. Used by
 * things like LOOP, IF, and GOTO statement's end catches. Not actually used by the statement itself
 * other than to help control the flow of the program
 * 
 * @author DarkGuardsman */
public interface IRedirectTask extends ILogicTask
{
    /** Should we show this in the encoder. Useful if your using a task as part of another task */
    public boolean render();
}
