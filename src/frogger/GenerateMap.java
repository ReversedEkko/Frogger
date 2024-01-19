package frogger;

import java.util.Random;

public class GenerateMap {
    public static String[] RoadType = { "water", "road", "grass" };

    public static String gen(String[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }
}
