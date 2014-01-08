package co;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import universalelectricity.core.vector.Vector3;

public class Main
{
    static List<Vector3> list = new ArrayList<Vector3>();
    static int listSize = 10;
    static int maxChangeY = 3;
    static int maxChangeD = 10;
    static Random random = new Random();

    public static void main(String[] args)
    {
        System.out.println("------ListSortingTest------");
        /* Randomly generate starting point */
        Vector3 start = new Vector3(random.nextInt(10), random.nextInt(10), random.nextInt(10));
        /* Randomly generate list of vectors to sort */
        for (int l = 0; l < listSize; l++)
        {
            list.add(start.clone().add(new Vector3(random.nextInt(maxChangeD * 2) - maxChangeD, random.nextInt(maxChangeY * 2) - maxChangeY, random.nextInt(maxChangeD * 2) - maxChangeD)));
        }
        /* shuffle list to prevent the starting list looking like the ending list */
        Collections.shuffle(list);
        Collections.shuffle(list);
        System.out.println("List Added and randomized\n");
        /* Output randomly generated and shuffled list for comarasion */
        System.out.println("--List Content--");
        int i = 0;
        for (Vector3 vec : list)
        {
            System.out.format("Entry%d: %s  D:%.3f   Y:%d \n", i, vec.toString(), vec.distanceTo(start), vec.intY());
        }
        System.out.println("--List End--\n");
        /* sort list by settings */
        System.out.print("Sorting list...");
        sortBlockList(start, list, false, false);
        System.out.print("  Done\n");
        /* Output sorted list for comparision */
        System.out.println("--List Content--\n");
        i = 0;
        for (Vector3 vec : list)
        {
            System.out.format("Entry%d: %s   D:%.3f   Y:%d \n", i, vec.toString(), vec.distanceTo(start), vec.intY());
        }
        System.out.println("--List End--\n");

    }

    /** Used to sort a list of vector3 locations using the vector3's distance from one point and
     * elevation in the y axis
     *
     * @param start - start location to measure distance from
     * @param locations - list of vectors to sort
     * @param closest - sort closest distance to the top
     * @param highest - sort highest y value to the top.
     *
     * Note: highest takes priority over closest */
    public static void sortBlockList(final Vector3 start, final List<Vector3> locations, final boolean closest, final boolean highest)
    {
        try
        {
            Collections.sort(locations, new Comparator<Vector3>()
            {
                @Override
                public int compare(Vector3 vecA, Vector3 vecB)
                {
                    //Though unlikely always return zero for equal vectors
                    if (vecA.equals(vecB))
                    {
                        return 0;
                    }
                    //Check y value fist as this is the primary search area
                    if (Integer.compare(vecA.intY(), vecB.intY()) != 0)
                    {
                        if (highest)
                        {
                            return vecA.intY() > vecB.intY() ? -1 : 1;
                        }
                        else
                        {
                            return vecA.intY() > vecB.intY() ? 1 : -1;
                        }
                    }
                    //Check distance after that
                    double distanceA = Vector3.distance(vecA, start);
                    double distanceB = Vector3.distance(vecB, start);
                    if (Double.compare(distanceA, distanceB) != 0)
                    {
                        if (closest)
                        {
                            return distanceA > distanceB ? 1 : -1;
                        }
                        else
                        {
                            return distanceA > distanceB ? -1 : 1;
                        }
                    }
                    return Double.compare(distanceA, distanceB);
                }
            });
        }
        catch (Exception e)
        {
            System.out.println("FluidMech>>>BlockDrain>>FillArea>>Error>>CollectionSorter");
            e.printStackTrace();
        }
    }
}
