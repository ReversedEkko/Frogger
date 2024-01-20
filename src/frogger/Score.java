package frogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Score {
    double previousGameScore;

    public Score(int gameScore) {
        previousGameScore = readScore();
        saveScore(gameScore);
    }

    public void saveScore(int gameScore) {
        try {
            if (gameScore > previousGameScore) {
                // Save a sample score to "scores.txt"
                PrintWriter writer = new PrintWriter("highsccore.txt", "UTF-8");
                writer.println(gameScore);
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("An error occurred while saving the score.");
            e.printStackTrace();
        }
    }

    public int readScore() {
        try {
            // Read scores from "scores.txt"
            FileReader fileReader = new FileReader("highsccore.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Print each line of the file
            int line = Integer.parseInt(bufferedReader.readLine());

            // Close the BufferedReader
            bufferedReader.close();

            return line;

        } catch (IOException e) {
            System.out.println("An error occurred while reading the scores.");
            e.printStackTrace();

            return -1;
        }
    }
}
