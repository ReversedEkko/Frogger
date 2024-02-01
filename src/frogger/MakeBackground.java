package frogger;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MakeBackground extends JPanel {
    private String roadPosFile = "CurrentRoadPositions.txt";
    private String riverPosFile = "CurrentRiverPositions.txt";

    private int[] posOfRoad;
    private int[] posOfRiver;
    public String[] backgroundImages = {
            "src/resources/map_sprites/background.png",
            "src/resources/map_sprites/background2.png"
    };

    public int lols = 0;

    public static boolean isDone = false;

    @Override
    protected void paintComponent(Graphics g) {

        isDone = false;

        super.paintComponent(g);
        posOfRoad = getFileInfo(roadPosFile);
        posOfRiver = getFileInfo(riverPosFile);

        try {
            // // Load images
            int maxWidth = 32;
            int totalHeight = 512;

            BufferedImage grassImage = ImageIO.read(new File("src/resources/map_sprites/grass.png"));
            BufferedImage roadImage = ImageIO.read(new File("src/resources/map_sprites/road.png"));
            BufferedImage waterImage = ImageIO.read(new File("src/resources/map_sprites/water.png"));
            BufferedImage combinedImage = new BufferedImage(maxWidth, totalHeight,
                    BufferedImage.TYPE_INT_ARGB);

            // Create a new image with dimensions based on the input images

            for (int i = 0; i < 16; i++) {

                boolean isFound = false;

                for (int y = 0; y < posOfRiver.length; y++) {
                    if (posOfRiver[y] == i) {
                        // combinedImage.getGraphics().drawImage(bkg, 0, 0, null);
                        combinedImage.getGraphics().drawImage(waterImage, 0,
                                i * 32, null);

                        isFound = true;

                        System.out.println("river at " + i);
                        break;
                    }
                }

                if (!isFound) {
                    for (int z = 0; z < posOfRoad.length; z++) {
                        if (posOfRoad[z] == i) {
                            // combinedImage.getGraphics().drawImage(bkg, 0, 0, null);
                            combinedImage.getGraphics().drawImage(roadImage, 0,
                                    i * 32, null);

                            isFound = true;

                            System.out.println("road at " + i);
                            break;
                        }
                    }
                }

                if (!isFound) {
                    combinedImage.getGraphics().drawImage(grassImage, 0,
                            i * 32, null);

                    System.out.println("grass at " + i);
                }
            }

            if (lols == 0) {

            }

            ImageIO.write(combinedImage, "png", new File("src/resources/map_sprites/background.png"));
            System.out.println("Images combined successfully.");

            isDone = true;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isDone() {
        return isDone;
    }

    public int[] getFileInfo(String filePath) {
        try {
            // Read scores from the file
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // List to store the integers
            List<Integer> integerList = new ArrayList<>();

            // Read each line and parse it as an integer
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                int value = Integer.parseInt(line.trim());
                integerList.add(value);
            }

            // Close the BufferedReader
            bufferedReader.close();

            // Convert the list to an array
            int[] resultArray = new int[integerList.size()];
            for (int i = 0; i < integerList.size(); i++) {
                resultArray[i] = integerList.get(i);
            }

            return resultArray;

        } catch (IOException | NumberFormatException e) {
            System.out.println("An error occurred while reading the file.");
            e.printStackTrace();

            // Return an array with default values in case of an error
            return new int[] { -1, -2 };
        }
    }

}
