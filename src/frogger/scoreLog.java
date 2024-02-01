package frogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class scoreLog {
    public scoreLog() {
        Scanner scanner = new Scanner(System.in);

        String[] names = getFileInfo("userlog.txt");

        System.out.println("What would you like to be called?");
        String userName = scanner.nextLine();
        boolean isFound = false;

        // Check if the name is already in the file
        for (int x = 0; x < names.length; x++) {
            if (userName.equals(names[x])) {
                isFound = true;
                break; // No need to continue searching if the name is found
            }
        }

        if (!isFound) {
            try {
                // Append the name to the file
                Files.write(Paths.get("userlog.txt"), (userName + "\n").getBytes(), StandardOpenOption.APPEND);
                System.out.println("Name successfully logged.");

            } catch (IOException e) {
                // Handle the exception (left as an exercise for the reader)
                System.out.println("An error occurred while logging the name.");
            }
        }
    }

    public String[] getFileInfo(String filePath) {
        try {
            // Read names from the file
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // List to store the names
            List<String> nameList = new ArrayList<>();

            // Read each line and add it to the list
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                nameList.add(line.trim());
            }

            // Close the BufferedReader
            bufferedReader.close();

            // Convert the list to a string array
            String[] resultArray = nameList.toArray(new String[0]);

            return resultArray;

        } catch (IOException e) {
            System.out.println("An error occurred while reading the file.");
            e.printStackTrace();

            // Return an array with default values in case of an error
            return new String[] { "-1", "-2" };
        }
    }
}
