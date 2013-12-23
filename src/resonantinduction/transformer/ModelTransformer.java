package resonantinduction.transformer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import static net.minecraftforge.common.ForgeDirection.*;

@SideOnly(Side.CLIENT)
public class ModelTransformer extends ModelBase
{
    ModelRenderer Down_North;
    ModelRenderer Down_West;
    ModelRenderer Down_South;
    ModelRenderer Down_East;
    ModelRenderer Up_East;
    ModelRenderer Up_West;
    ModelRenderer Up_South;
    ModelRenderer Up_North;
    ModelRenderer South_Up;
    ModelRenderer South_Down;
    ModelRenderer South_West;
    ModelRenderer South_East;
    ModelRenderer North_Up;
    ModelRenderer North_East;
    ModelRenderer North_West;
    ModelRenderer North_Down;
    ModelRenderer East_South;
    ModelRenderer East_North;
    ModelRenderer East_Up;
    ModelRenderer East_Down;
    ModelRenderer West_Down;
    ModelRenderer West_Up;
    ModelRenderer West_South;
    ModelRenderer West_North;
    ModelRenderer io_Up;
    ModelRenderer io_East;
    ModelRenderer io_West;
    ModelRenderer io_South;
    ModelRenderer io_North;
    ModelRenderer io_Down;
    
    public ModelTransformer()
    {
        super();
        textureWidth = 128;
        textureHeight = 64;
        
        Down_North = new ModelRenderer(this, 108, 8);
        Down_North.addBox(0F, 0F, 0F, 8, 2, 2);
        Down_North.setRotationPoint(-7F, 21F, -1F);
        Down_North.setTextureSize(this.textureWidth, this.textureHeight);
        Down_North.mirror = true;
        setRotation(Down_North, 0F, 0F, 0F);
        Down_West = new ModelRenderer(this, 108, 22);
        Down_West.addBox(0F, 0F, 0F, 2, 2, 8);
        Down_West.setRotationPoint(-1F, 21F, -7F);
        Down_West.setTextureSize(this.textureWidth, this.textureHeight);
        Down_West.mirror = true;
        setRotation(Down_West, 0F, 0F, 0F);
        Down_South = new ModelRenderer(this, 108, 4);
        Down_South.addBox(0F, 0F, 0F, 8, 2, 2);
        Down_South.setRotationPoint(-1F, 21F, -1F);
        Down_South.setTextureSize(this.textureWidth, this.textureHeight);
        Down_South.mirror = true;
        setRotation(Down_South, 0F, 0F, 0F);
        Down_East = new ModelRenderer(this, 108, 12);
        Down_East.addBox(0F, 0F, 0F, 2, 2, 8);
        Down_East.setRotationPoint(-1F, 21F, -1F);
        Down_East.setTextureSize(this.textureWidth, this.textureHeight);
        Down_East.mirror = true;
        setRotation(Down_East, 0F, 0F, 0F);
        Up_East = new ModelRenderer(this, 88, 22);
        Up_East.addBox(0F, 0F, 0F, 2, 2, 8);
        Up_East.setRotationPoint(-1F, 9F, -1F);
        Up_East.setTextureSize(this.textureWidth, this.textureHeight);
        Up_East.mirror = true;
        setRotation(Up_East, 0F, 0F, 0F);
        Up_West = new ModelRenderer(this, 88, 12);
        Up_West.addBox(0F, 0F, 0F, 2, 2, 8);
        Up_West.setRotationPoint(-1F, 9F, -7F);
        Up_West.setTextureSize(this.textureWidth, this.textureHeight);
        Up_West.mirror = true;
        setRotation(Up_West, 0F, 0F, 0F);
        Up_South = new ModelRenderer(this, 108, 0);
        Up_South.addBox(0F, 0F, 0F, 8, 2, 2);
        Up_South.setRotationPoint(-1F, 9F, -1F);
        Up_South.setTextureSize(this.textureWidth, this.textureHeight);
        Up_South.mirror = true;
        setRotation(Up_South, 0F, 0F, 0F);
        Up_North = new ModelRenderer(this, 88, 0);
        Up_North.addBox(0F, 0F, 0F, 8, 2, 2);
        Up_North.setRotationPoint(-7F, 9F, -1F);
        Up_North.setTextureSize(this.textureWidth, this.textureHeight);
        Up_North.mirror = true;
        setRotation(Up_North, 0F, 0F, 0F);
        South_Up = new ModelRenderer(this, 0, 22);
        South_Up.addBox(0F, 0F, 0F, 2, 8, 2);
        South_Up.setRotationPoint(5F, 9F, -1F);
        South_Up.setTextureSize(this.textureWidth, this.textureHeight);
        South_Up.mirror = true;
        setRotation(South_Up, 0F, 0F, 0F);
        South_Down = new ModelRenderer(this, 8, 22);
        South_Down.addBox(0F, 0F, 0F, 2, 8, 2);
        South_Down.setRotationPoint(5F, 15F, -1F);
        South_Down.setTextureSize(this.textureWidth, this.textureHeight);
        South_Down.mirror = true;
        setRotation(South_Down, 0F, 0F, 0F);
        South_West = new ModelRenderer(this, 68, 22);
        South_West.addBox(0F, 0F, 0F, 2, 2, 8);
        South_West.setRotationPoint(5F, 15F, -7F);
        South_West.setTextureSize(this.textureWidth, this.textureHeight);
        South_West.mirror = true;
        setRotation(South_West, 0F, 0F, 0F);
        South_East = new ModelRenderer(this, 68, 12);
        South_East.addBox(0F, 0F, 0F, 2, 2, 8);
        South_East.setRotationPoint(5F, 15F, -1F);
        South_East.setTextureSize(this.textureWidth, this.textureHeight);
        South_East.mirror = true;
        setRotation(South_East, 0F, 0F, 0F);
        North_Up = new ModelRenderer(this, 16, 22);
        North_Up.addBox(0F, 0F, 0F, 2, 8, 2);
        North_Up.setRotationPoint(-7F, 9F, -1F);
        North_Up.setTextureSize(this.textureWidth, this.textureHeight);
        North_Up.mirror = true;
        setRotation(North_Up, 0F, 0F, 0F);
        North_East = new ModelRenderer(this, 48, 22);
        North_East.addBox(0F, 0F, 0F, 2, 2, 8);
        North_East.setRotationPoint(-7F, 15F, -1F);
        North_East.setTextureSize(this.textureWidth, this.textureHeight);
        North_East.mirror = true;
        setRotation(North_East, 0F, 0F, 0F);
        North_West = new ModelRenderer(this, 48, 12);
        North_West.addBox(0F, 0F, 0F, 2, 2, 8);
        North_West.setRotationPoint(-7F, 15F, -7F);
        North_West.setTextureSize(this.textureWidth, this.textureHeight);
        North_West.mirror = true;
        setRotation(North_West, 0F, 0F, 0F);
        North_Down = new ModelRenderer(this, 0, 12);
        North_Down.addBox(0F, 0F, 0F, 2, 8, 2);
        North_Down.setRotationPoint(-7F, 15F, -1F);
        North_Down.setTextureSize(this.textureWidth, this.textureHeight);
        North_Down.mirror = true;
        setRotation(North_Down, 0F, 0F, 0F);
        East_South = new ModelRenderer(this, 88, 4);
        East_South.addBox(0F, 0F, 0F, 8, 2, 2);
        East_South.setRotationPoint(-1F, 15F, 5F);
        East_South.setTextureSize(this.textureWidth, this.textureHeight);
        East_South.mirror = true;
        setRotation(East_South, 0F, 0F, 0F);
        East_North = new ModelRenderer(this, 88, 8);
        East_North.addBox(0F, 0F, 0F, 8, 2, 2);
        East_North.setRotationPoint(-7F, 15F, 5F);
        East_North.setTextureSize(this.textureWidth, this.textureHeight);
        East_North.mirror = true;
        setRotation(East_North, 0F, 0F, 0F);
        East_Up = new ModelRenderer(this, 8, 12);
        East_Up.addBox(0F, 0F, 0F, 2, 8, 2);
        East_Up.setRotationPoint(-1F, 9F, 5F);
        East_Up.setTextureSize(this.textureWidth, this.textureHeight);
        East_Up.mirror = true;
        setRotation(East_Up, 0F, 0F, 0F);
        East_Down = new ModelRenderer(this, 16, 12);
        East_Down.addBox(0F, 0F, 0F, 2, 8, 2);
        East_Down.setRotationPoint(-1F, 15F, 5F);
        East_Down.setTextureSize(this.textureWidth, this.textureHeight);
        East_Down.mirror = true;
        setRotation(East_Down, 0F, 0F, 0F);
        West_Down = new ModelRenderer(this, 24, 22);
        West_Down.addBox(0F, 0F, 0F, 2, 8, 2);
        West_Down.setRotationPoint(-1F, 15F, -7F);
        West_Down.setTextureSize(this.textureWidth, this.textureHeight);
        West_Down.mirror = true;
        setRotation(West_Down, 0F, 0F, 0F);
        West_Up = new ModelRenderer(this, 24, 12);
        West_Up.addBox(0F, 0F, 0F, 2, 8, 2);
        West_Up.setRotationPoint(-1F, 9F, -7F);
        West_Up.setTextureSize(this.textureWidth, this.textureHeight);
        West_Up.mirror = true;
        setRotation(West_Up, 0F, 0F, 0F);
        West_South = new ModelRenderer(this, 68, 0);
        West_South.addBox(0F, 0F, 0F, 8, 2, 2);
        West_South.setRotationPoint(-1F, 15F, -7F);
        West_South.setTextureSize(this.textureWidth, this.textureHeight);
        West_South.mirror = true;
        setRotation(West_South, 0F, 0F, 0F);
        West_North = new ModelRenderer(this, 68, 4);
        West_North.addBox(0F, 0F, 0F, 8, 2, 2);
        West_North.setRotationPoint(-7F, 15F, -7F);
        West_North.setTextureSize(this.textureWidth, this.textureHeight);
        West_North.mirror = true;
        setRotation(West_North, 0F, 0F, 0F);
        io_Up = new ModelRenderer(this, 0, 0);
        io_Up.addBox(0F, 0F, 0F, 4, 3, 4);
        io_Up.setRotationPoint(-2F, 8F, -2F);
        io_Up.setTextureSize(this.textureWidth, this.textureHeight);
        io_Up.mirror = true;
        setRotation(io_Up, 0F, 0F, 0F);
        io_East = new ModelRenderer(this, 0, 7);
        io_East.addBox(0F, 0F, 0F, 4, 4, 3);
        io_East.setRotationPoint(-2F, 14F, 5F);
        io_East.setTextureSize(this.textureWidth, this.textureHeight);
        io_East.mirror = true;
        setRotation(io_East, 0F, 0F, 0F);
        io_West = new ModelRenderer(this, 14, 8);
        io_West.addBox(0F, 0F, 0F, 4, 4, 3);
        io_West.setRotationPoint(-2F, 14F, -8F);
        io_West.setTextureSize(this.textureWidth, this.textureHeight);
        io_West.mirror = true;
        setRotation(io_West, 0F, 0F, 0F);
        io_South = new ModelRenderer(this, 16, 0);
        io_South.addBox(0F, 0F, 0F, 3, 4, 4);
        io_South.setRotationPoint(5F, 14F, -2F);
        io_South.setTextureSize(this.textureWidth, this.textureHeight);
        io_South.mirror = true;
        setRotation(io_South, 0F, 0F, 0F);
        io_North = new ModelRenderer(this, 30, 0);
        io_North.addBox(0F, 0F, 0F, 3, 4, 4);
        io_North.setRotationPoint(-8F, 14F, -2F);
        io_North.setTextureSize(this.textureWidth, this.textureHeight);
        io_North.mirror = true;
        setRotation(io_North, 0F, 0F, 0F);
        io_Down = new ModelRenderer(this, 28, 8);
        io_Down.addBox(0F, 0F, 0F, 4, 3, 4);
        io_Down.setRotationPoint(-2F, 21F, -2F);
        io_Down.setTextureSize(this.textureWidth, this.textureHeight);
        io_Down.mirror = true;
        setRotation(io_Down, 0F, 0F, 0F);
    }
    
    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
    {
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }
    
    public void renderIO(ForgeDirection dir, float f5)
    {
        switch (dir)
        {
            case UP: 
                this.io_Up.render(f5);
                break;
            case DOWN:
                this.io_Down.render(f5);
                break;
            case NORTH:
                this.io_North.render(f5);
                break;
            case SOUTH:
                this.io_South.render(f5);
                break;
            case EAST:
                this.io_East.render(f5);
                break;
            case WEST:
                this.io_West.render(f5);
                break;
            default:
                break;
        }
    }
    
    public void renderCores(ForgeDirection in, ForgeDirection out, float f5)
    {
        if (in == UP || out == UP)
        {
            if (in == DOWN || out == DOWN)
            {
                this.Down_East.render(f5);
                this.Down_North.render(f5);
                this.Down_South.render(f5);
                this.Down_West.render(f5);
                
                this.East_Down.render(f5);
                this.North_Down.render(f5);
                this.South_Down.render(f5);
                this.West_Down.render(f5);
                
                this.East_Up.render(f5);
                this.North_Up.render(f5);
                this.South_Up.render(f5);
                this.West_Up.render(f5);
                
                this.Up_East.render(f5);
                this.Up_North.render(f5);
                this.Up_South.render(f5);
                this.Up_West.render(f5);
            }
            else if (in == NORTH || out == NORTH)
            {
                this.North_East.render(f5);
                this.North_West.render(f5);
                
                this.East_North.render(f5);
                this.West_North.render(f5);
                
                this.East_Up.render(f5);
                this.West_Up.render(f5);
                
                this.Up_East.render(f5);
                this.Up_West.render(f5);
            }
            else if (in == SOUTH || out == SOUTH)
            {
                this.South_East.render(f5);
                this.South_West.render(f5);
                
                this.East_South.render(f5);
                this.West_South.render(f5);
                
                this.East_Up.render(f5);
                this.West_Up.render(f5);
                
                this.Up_East.render(f5);
                this.Up_West.render(f5);
            }
            else if (in == EAST || out == EAST)
            {
                this.East_North.render(f5);
                this.East_South.render(f5);
                
                this.North_East.render(f5);
                this.South_East.render(f5);
                
                this.North_Up.render(f5);
                this.South_Up.render(f5);
                
                this.Up_North.render(f5);
                this.Up_South.render(f5);
            }
            else if (in == WEST || out == WEST)
            {
                this.West_North.render(f5);
                this.West_South.render(f5);
                
                this.North_West.render(f5);
                this.South_West.render(f5);
                
                this.North_Up.render(f5);
                this.South_Up.render(f5);
                
                this.Up_North.render(f5);
                this.Up_South.render(f5);
            }
        }
        else if (in == DOWN || out == DOWN)
        {
            if (in == NORTH || out == NORTH)
            {
                this.North_East.render(f5);
                this.North_West.render(f5);
                
                this.East_North.render(f5);
                this.West_North.render(f5);
                
                this.East_Down.render(f5);
                this.West_Down.render(f5);
                
                this.Down_East.render(f5);
                this.Down_West.render(f5);
            }
            else if (in == SOUTH || out == SOUTH)
            {
                this.South_East.render(f5);
                this.South_West.render(f5);
                
                this.East_South.render(f5);
                this.West_South.render(f5);
                
                this.East_Down.render(f5);
                this.West_Down.render(f5);
                
                this.Down_East.render(f5);
                this.Down_West.render(f5);
            }
            else if (in == EAST || out == EAST)
            {
                this.East_North.render(f5);
                this.East_South.render(f5);
                
                this.North_East.render(f5);
                this.South_East.render(f5);
                
                this.North_Down.render(f5);
                this.South_Down.render(f5);
                
                this.Down_North.render(f5);
                this.Down_South.render(f5);
            }
            else if (in == WEST || out == WEST)
            {
                this.West_North.render(f5);
                this.West_South.render(f5);
                
                this.North_West.render(f5);
                this.South_West.render(f5);
                
                this.North_Down.render(f5);
                this.South_Down.render(f5);
                
                this.Down_North.render(f5);
                this.Down_South.render(f5);
            }
        }
        else if (in == NORTH || out == NORTH)
        {
            if (in == SOUTH || out == SOUTH)
            {
                this.North_Down.render(f5);
                this.North_East.render(f5);
                this.North_Up.render(f5);
                this.North_West.render(f5);
                
                this.Down_North.render(f5);
                this.East_North.render(f5);
                this.Up_North.render(f5);
                this.West_North.render(f5);
                
                this.Down_South.render(f5);
                this.East_South.render(f5);
                this.Up_South.render(f5);
                this.West_South.render(f5);
                
                this.South_Down.render(f5);
                this.South_East.render(f5);
                this.South_Up.render(f5);
                this.South_West.render(f5);
            }
            else if (in == EAST || out == EAST)
            {
                this.North_Down.render(f5);
                this.North_Up.render(f5);
                
                this.Down_North.render(f5);
                this.Up_North.render(f5);
                
                this.Down_East.render(f5);
                this.Up_East.render(f5);
                
                this.East_Down.render(f5);
                this.East_Up.render(f5);
            }
            else if (in == WEST || out == WEST)
            {
                this.North_Down.render(f5);
                this.North_Up.render(f5);
                
                this.Down_North.render(f5);
                this.Up_North.render(f5);
                
                this.Down_West.render(f5);
                this.Up_West.render(f5);
                
                this.West_Down.render(f5);
                this.West_Up.render(f5);
            }
        }
        else if (in == SOUTH || out == SOUTH)
        {
            if (in == EAST || out == EAST)
            {
                this.South_Down.render(f5);
                this.South_Up.render(f5);
                
                this.Down_South.render(f5);
                this.Up_South.render(f5);
                
                this.Down_East.render(f5);
                this.Up_East.render(f5);
                
                this.East_Down.render(f5);
                this.East_Up.render(f5);
            }
            else if (in == WEST || out == WEST)
            {
                this.South_Down.render(f5);
                this.South_Up.render(f5);
                
                this.Down_South.render(f5);
                this.Up_South.render(f5);
                
                this.Down_West.render(f5);
                this.Up_West.render(f5);
                
                this.West_Down.render(f5);
                this.West_Up.render(f5);
            }
        }
        else if (in == EAST || out == EAST)
        {
            if (in == WEST || out == WEST)
            {
                this.East_Down.render(f5);
                this.East_North.render(f5);
                this.East_South.render(f5);
                this.East_Up.render(f5);
                
                this.Down_East.render(f5);
                this.North_East.render(f5);
                this.South_East.render(f5);
                this.Up_East.render(f5);
                
                this.Down_West.render(f5);
                this.North_West.render(f5);
                this.South_West.render(f5);
                this.Up_West.render(f5);
                
                this.West_Down.render(f5);
                this.West_North.render(f5);
                this.West_South.render(f5);
                this.West_Up.render(f5);
            }
        }
    }
    
    private void setRotation(ModelRenderer model, float x, float y, float z)
    {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }
    
    @Override
    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
    {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }

    public void renderAll(float f5)
    {
        this.Down_North.render(f5);
        this.Down_West.render(f5);
        this.Down_South.render(f5);
        this.Down_East.render(f5);
        this.Up_East.render(f5);
        this.Up_West.render(f5);
        this.Up_South.render(f5);
        this.Up_North.render(f5);
        this.South_Up.render(f5);
        this.South_Down.render(f5);
        this.South_West.render(f5);
        this.South_East.render(f5);
        this.North_Up.render(f5);
        this.North_East.render(f5);
        this.North_West.render(f5);
        this.North_Down.render(f5);
        this.East_South.render(f5);
        this.East_North.render(f5);
        this.East_Up.render(f5);
        this.East_Down.render(f5);
        this.West_Down.render(f5);
        this.West_Up.render(f5);
        this.West_South.render(f5);
        this.West_North.render(f5);
    }
    
}