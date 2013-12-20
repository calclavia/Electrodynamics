package com.builtbroken.minecraft.prefab.invgui;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import universalelectricity.core.vector.Vector2;

import com.builtbroken.minecraft.DarkCore;

/** When done should be a prefab that can be used to render a power bar on the screen
 *
 * @author DarkGuardsman */
public class GuiBar extends Gui
{
    public static final ResourceLocation TEXTURE = new ResourceLocation(DarkCore.DOMAIN, DarkCore.GUI_DIRECTORY + "bar.png");

    protected float currentLevel = 0;
    protected float maxLevel = 10;
    protected float scale = 1.0f;

    protected Vector2 position;
    protected Color color = Color.red;

    protected boolean horizontal = true;

    private final int desiredH = 240;
    private final int desiredW = 427;

    public GuiBar(int xx, int yy, float scale, boolean horizontal)
    {
        this.position = new Vector2(xx, yy);
        this.scale = scale;
        this.horizontal = horizontal;
    }

    public GuiBar setColor(Color color)
    {
        this.color = color;
        return this;
    }

    /** Sets the parms for the bar that determ the length of the bar
     *
     * @param current - current level of the reading
     * @param max - max level of the reading; */
    public GuiBar setMeter(float current, float max)
    {
        this.currentLevel = current;
        this.maxLevel = max;
        return this;
    }

    public void draw(Minecraft minecraft)
    {
        GL11.glPushMatrix();
        if (scale != 1.0f)
        {
            //With everything scaled the gui will not align like a normal one so use a scaled distance from the main GUI
            ScaledResolution scaledresolution = new ScaledResolution(minecraft.gameSettings, minecraft.displayWidth, minecraft.displayHeight);
            int scaleH = scaledresolution.getScaledHeight();
            int scaleW = scaledresolution.getScaledWidth();
            //this.drawCenteredString(minecraft.fontRenderer, "Scale - " + scaleW + "x " + scaleH + "y", 100, 100, color);

            float sh = (scaleH / desiredH) / scale;
            float sW = (scaleW / desiredW) / scale;
            //Start drawing after everying is scaled down
            GL11.glScalef(scale, scale, scale);

            this.drawCenteredString(minecraft.fontRenderer, "Scale - " + scaleW + "x " + scaleH + "y", 100, 100, Color.blue.getRGB());
        }
        Gui.drawRect(this.position.intX(), this.position.intY(), this.position.intX() + (horizontal ? 10 : 3), this.position.intY() + (!horizontal ? 10 : 3), Color.black.getRGB());

    }
}
