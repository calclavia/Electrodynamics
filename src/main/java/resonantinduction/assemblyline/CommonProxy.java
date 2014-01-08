package resonantinduction.assemblyline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import resonantinduction.assemblyline.generators.ContainerCoalGenerator;
import resonantinduction.assemblyline.generators.TileEntitySteamGen;
import resonantinduction.assemblyline.imprinter.ContainerImprinter;
import resonantinduction.assemblyline.imprinter.TileEntityImprinter;
import resonantinduction.assemblyline.machine.ContainerBatteryBox;
import resonantinduction.assemblyline.machine.TileEntityBatteryBox;
import resonantinduction.assemblyline.machine.encoder.ContainerEncoder;
import resonantinduction.assemblyline.machine.encoder.TileEntityEncoder;
import resonantinduction.assemblyline.machine.processor.ContainerProcessor;
import resonantinduction.assemblyline.machine.processor.TileEntityProcessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.builtbroken.minecraft.prefab.invgui.ContainerFake;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler
{
    public static final int GUI_IMPRINTER = 1;
    public static final int GUI_ENCODER = 2;
    public static final int GUI_ENCODER_CODE = 3;
    public static final int GUI_ENCODER_HELP = 4;
    public static final int GUI_PROCESSOR = 5;
    public static final int GUI_MANIPULATOR = 6;
    public static final int GUI_COAL_GEN = 7;
    public static final int GUI_FUEL_GEN = 8;
    public static final int GUI_FURNACE_ELEC = 9;
    public static final int GUI_BATTERY_BOX = 10;

    public void preInit()
    {

    }

    public void init()
    {

    }

    public void postInit()
    {

    }

    private void extractZipToLocation(File zipFile, String sourceFolder, String destFolder)
    {
        try
        {

            File destFile = new File(FMLCommonHandler.instance().getMinecraftServerInstance().getFile("."), destFolder);
            String destinationName = destFile.getAbsolutePath();
            byte[] buf = new byte[1024];
            ZipInputStream zipinputstream = null;
            ZipEntry zipentry;
            zipinputstream = new ZipInputStream(new FileInputStream(zipFile));

            zipentry = zipinputstream.getNextEntry();
            while (zipentry != null)
            {
                // for each entry to be extracted
                String zipentryName = zipentry.getName();
                if (!zipentryName.startsWith(sourceFolder))
                {
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }

                String entryName = destinationName + zipentryName.substring(Math.min(zipentryName.length(), sourceFolder.length() - 1));
                entryName = entryName.replace('/', File.separatorChar);
                entryName = entryName.replace('\\', File.separatorChar);
                int n;
                FileOutputStream fileoutputstream;
                File newFile = new File(entryName);
                if (zipentry.isDirectory())
                {
                    if (!newFile.mkdirs())
                    {
                        break;
                    }
                    zipentry = zipinputstream.getNextEntry();
                    continue;
                }

                fileoutputstream = new FileOutputStream(entryName);

                while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
                {
                    fileoutputstream.write(buf, 0, n);
                }

                fileoutputstream.close();
                zipinputstream.closeEntry();
                zipentry = zipinputstream.getNextEntry();

            }// while

            zipinputstream.close();
        }
        catch (Exception e)
        {
            System.out.println("Error while loading AssemblyLine Lua libraries: ");
            e.printStackTrace();
        }
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity != null)
        {
            switch (ID)
            {
                case GUI_IMPRINTER:
                {
                    return new ContainerImprinter(player.inventory, (TileEntityImprinter) tileEntity);
                }
                case GUI_ENCODER:
                {
                    if (tileEntity instanceof TileEntityEncoder)
                    {
                        return new ContainerEncoder(player.inventory, (TileEntityEncoder) tileEntity);
                    }
                }
                case GUI_PROCESSOR:
                {
                    return new ContainerProcessor(player.inventory, (TileEntityProcessor) tileEntity);
                }
                case GUI_MANIPULATOR:
                {
                    return new ContainerProcessor(player.inventory, (TileEntityProcessor) tileEntity);
                }
                case GUI_COAL_GEN:
                    if (tileEntity instanceof TileEntitySteamGen)
                    {
                        return new ContainerCoalGenerator(player.inventory, ((TileEntitySteamGen) tileEntity));
                    }
                case GUI_BATTERY_BOX:
                    if (tileEntity instanceof TileEntityBatteryBox)
                    {
                        return new ContainerBatteryBox(player.inventory, (TileEntityBatteryBox) tileEntity);
                    }
                default:
                    return new ContainerFake(tileEntity);
            }
        }

        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
    {
        return null;
    }

    public boolean isCtrKeyDown()
    {
        return false;
    }

}
