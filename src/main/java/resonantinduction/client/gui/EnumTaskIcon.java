package resonantinduction.client.gui;

/** Used to reference icons in the gui coder sheet
 * 
 * @author DarkGuardsman */
public enum EnumTaskIcon
{
    VERT_LINE(0, 0),
    HORT_LINE(20, 0),
    ARROW_UP(40, 0),
    ARROW_RIGHT(60, 0),
    ARROW_DOWN(80, 0),
    ARROW_LEFT(100, 0),
    VERT_LINE_DOT(120, 0),
    HORT_LINE_DOT(140, 0),
    LEFT_UP_BEND(160, 0),
    LEFT_DOWN_BEND(180, 0),
    RIGHT_UP_BEND(200, 0),
    RIGHT_DOWN_BEND(220, 0);

    int uu, vv;
    int sizeX = 20, sizeY = 20;

    private EnumTaskIcon(int uu, int vv)
    {

    }
}
