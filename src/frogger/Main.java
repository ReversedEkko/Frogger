/**
 * Copyright (c) 2009 Vitaliy Pavlenko
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package frogger;

import java.awt.Image;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import jig.engine.ImageResource;
import jig.engine.PaintableCanvas;
import jig.engine.RenderingContext;
import jig.engine.ResourceFactory;
import jig.engine.PaintableCanvas.JIGSHAPE;
import jig.engine.hli.ImageBackgroundLayer;
import jig.engine.hli.StaticScreenGame;
import jig.engine.physics.AbstractBodyLayer;
import jig.engine.util.Vector2D;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main extends StaticScreenGame {
	static final int WORLD_WIDTH = (30 * 32);
	static final int WORLD_HEIGHT = (16 * 32);
	static final Vector2D FROGGER_START = new Vector2D(15 * 32, WORLD_HEIGHT - 32);

	static final String RSC_PATH = "resources/";
	static final String SPRITE_SHEET = RSC_PATH + "frogger_sprites.png";
	static final String SKINS_SHEET = RSC_PATH + "frogger_skins.png";

	// static String backGround = RSC_PATH + "Background.png";

	// RSC_PATH + "Background.png";

	static final int FROGGER_LIVES = 1;
	static final int STARTING_LEVEL = 1;
	static final int DEFAULT_LEVEL_TIME = 60;

	private FroggerCollisionDetection frogCol;
	private FroggerCollisionDetection frogCol2;
	private Frogger frog;
	private Frogger frog2;
	private AudioEfx audiofx;
	private FroggerUI ui;
	private WindGust wind;
	private HeatWave hwave;
	private GoalManager goalmanager;

	private AbstractBodyLayer<MovingEntity> movingObjectsLayer;
	private AbstractBodyLayer<MovingEntity> particleLayer;

	// private MovingEntityFactory[] roadLine;
	// private MovingEntityFactory[] riverLine;
	private MovingEntityFactory roadLine[] = new MovingEntityFactory[12];
	private MovingEntityFactory riverLine[] = new MovingEntityFactory[12];

	MakeBackground backgroundPanel = new MakeBackground();

	private ImageBackgroundLayer backgroundLayer;

	static final int GAME_INTRO = 0;
	static final int GAME_PLAY = 1;
	static final int GAME_FINISH_LEVEL = 2;
	static final int GAME_INSTRUCTIONS = 3;
	static final int GAME_OVER = 4;
	static final int SKIN_Selection = 5;

	public static int skinSelected = 0;
	public static String CurrentSkin = "#frog";

	protected int GameState = GAME_INTRO;
	protected int GameLevel = STARTING_LEVEL;

	public int GameLives = FROGGER_LIVES;
	public int GameScore = 0;

	public int levelTimer = DEFAULT_LEVEL_TIME;

	private boolean space_has_been_released = false;
	private boolean keyPressed = false;
	private boolean keyPressed2 = false;
	private boolean listenInput = true;
	private boolean listenInput2 = true;

	public static boolean twoPlayer = true;
	public static double speedMultipler = 1;

	ImageResource newBackground = null;

	String backGround = RSC_PATH + "map_sprites/background.png";
	ImageResource bkg = ResourceFactory.getFactory().getFrames(
			backGround).get(0);

	ImageResource tempBkg = ResourceFactory.getFactory().getFrames(
			RSC_PATH + "map_sprites/tempbkg.png").get(0);

	JFrame frame = new JFrame("Frogger Background");

	/**
	 * Initialize game objects
	 */
	public Main() {
		super(WORLD_WIDTH, WORLD_HEIGHT, false);

		ResourceFactory.getFactory().loadResources(RSC_PATH, "resources.xml");

		backgroundLayer = new ImageBackgroundLayer(tempBkg, WORLD_WIDTH,
				WORLD_HEIGHT, ImageBackgroundLayer.TILE_IMAGE);

		// Used in CollisionObject, basically 2 different collision spheres
		// 30x30 is a large sphere (sphere that fits inside a 30x30 pixel rectangle)
		// 4x4 is a tiny sphere
		PaintableCanvas.loadDefaultFrames("col", 30, 30, 2, JIGSHAPE.RECTANGLE, null);
		PaintableCanvas.loadDefaultFrames("colSmall", 4, 4, 2, JIGSHAPE.RECTANGLE, null);

		frog = new Frogger(this);
		// if (twoPlayer) {
		frog2 = new Frogger(this);
		frogCol2 = new FroggerCollisionDetection(frog2);
		audiofx = new AudioEfx(frogCol2, frog2);
		// }
		frogCol = new FroggerCollisionDetection(frog);
		audiofx = new AudioEfx(frogCol, frog);
		ui = new FroggerUI(this);
		wind = new WindGust();
		hwave = new HeatWave();
		goalmanager = new GoalManager();

		movingObjectsLayer = new AbstractBodyLayer.IterativeUpdate<MovingEntity>();
		particleLayer = new AbstractBodyLayer.IterativeUpdate<MovingEntity>();

		initializeLevel(1);
	}

	public void makeMap(String roadType, int currentMapY, double speed, int pos, int roadCount, int theRiverCount) {

		speed = speed * speedMultipler;

		if ("water".equals(roadType)) {
			riverLine[theRiverCount] = new MovingEntityFactory(new Vector2D(pos, currentMapY * 32),
					new Vector2D(speed, 0));

			try {
				// Append the current map Y to "CurrentRiverPositions.txt"
				FileWriter write = new FileWriter("CurrentRiverPositions.txt", true);
				write.write(currentMapY + "\n");
				write.close();
			} catch (IOException e) {
				System.out.println("An error occurred while saving the river positions.");
				e.printStackTrace();
			}

		} else if ("road".equals(roadType)) {
			roadLine[roadCount] = new MovingEntityFactory(new Vector2D(pos, currentMapY * 32),
					new Vector2D(speed, 0));

			try {
				// Append the current map Y to "CurrentRoadPositions.txt"
				FileWriter write = new FileWriter("CurrentRoadPositions.txt", true);
				write.write(currentMapY + "\n");
				write.close();
			} catch (IOException e) {
				System.out.println("An error occurred while saving the road positions.");
				e.printStackTrace();
			}
		}

		for (int x = roadCount + 1; x < roadLine.length; x++) {
			roadLine[x] = null;
		}
		for (int x = theRiverCount + 1; x < riverLine.length; x++) {
			riverLine[x] = null;
		}

	}

	public void initializeLevel(int level) {
		final String[] RoadType = { "water", "road", "grass" };

		// clears info from temp storage
		File tempRiverPos = new File("CurrentRiverPositions.txt");
		if (tempRiverPos.delete()) {
			System.out.println("Deleted the file: " + tempRiverPos.getName());
		} else {
			System.out.println("Failed to delete the file.");
		}

		File tempRoadPos = new File("CurrentRoadPositions.txt");
		if (tempRoadPos.delete()) {
			System.out.println("Deleted the file: " + tempRoadPos.getName());
		} else {
			System.out.println("Failed to delete the file.");
		}

		int roadcountpremium = 0;
		int theRiverCount = 0;

		for (int q = 0; q < 13; q++) {
			int vectorPos = (-(32 * 3));
			// Generate a random index to access the RoadType array
			int randomIndex = new Random().nextInt(RoadType.length);

			// Get the road type at the random index
			String rnd = RoadType[randomIndex];
			double randomValue = 0;

			do {
				randomValue = (Math.random() * 0.2) - 0.1;
			} while ((randomValue > 0.05) && (randomValue < -.05));

			if (randomValue < 0) {
				vectorPos = Main.WORLD_WIDTH;
			}

			switch (rnd) {
				case "water":
					theRiverCount++;
					break;

				case "road":
					roadcountpremium++;
					break;

				default:
					break;
			}

			// the move/creates the objects factory(each row of the map is a factory)
			makeMap(rnd, q + 2, randomValue * (level + 1), vectorPos, roadcountpremium, theRiverCount);
		}

		// (newBackground, WORLD_WIDTH, WORLD_HEIGHT, ImageBackgroundLayer.TILE_IMAGE);

		SwingUtilities.invokeLater(() -> {
			frame.getContentPane().add(backgroundPanel);

			frame.setSize(32, 512); // Set your desired frame size
			frame.setLocationRelativeTo(null); // Center the frame
			frame.setVisible(true);
		});

		frame.setVisible(false);

		System.out.println("end");
		System.out.println();

		movingObjectsLayer.clear();

		goalmanager.init(level);
		for (Goal g : goalmanager.get()) {
			movingObjectsLayer.add(g);
		}

		/*
		 * Build some traffic before game starts buy running MovingEntityFactories for
		 * fews cycles
		 */
		for (int i = 0; i < 500; i++) {
			cycleTraffic(10);
		}

		// CURRENTLY WORKING ON THIS

		if (MakeBackground.isDone() == true) {
			String backGround = RSC_PATH + "map_sprites/background.png";

			backgroundLayer.setActivation(false);

			backgroundLayer.setBackground(newBackground,
					ImageBackgroundLayer.TILE_IMAGE);

			backgroundLayer.setActivation(true);
			System.out.println(backgroundLayer.isActive());
		}

	}

	/**
	 * Populate movingObjectLayer with a cycle of cars/trucks, moving tree logs, etc
	 * 
	 * @param deltaMs
	 */
	public void cycleTraffic(long deltaMs) {
		MovingEntity m;
		/* Road traffic updates */
		for (int y = 0; y < roadLine.length; y++) {
			if (roadLine[y] != null) {
				roadLine[y].update(deltaMs);
				if ((m = roadLine[y].buildVehicle()) != null) {
					movingObjectsLayer.add(m);
				}
			}
		}

		/* River traffic updates */
		for (int x = 0; x < riverLine.length; x++) {
			if (riverLine[x] != null) {
				riverLine[x].update(deltaMs);
				if ((m = riverLine[x].buildLongLogWithCrocodile(40)) != null) {
					movingObjectsLayer.add(m);
				}
			}
		}

		// Do Wind
		if ((m = wind.genParticles(GameLevel)) != null)
			particleLayer.add(m);

		// HeatWave
		if ((m = hwave.genParticles(frog.getCenterPosition())) != null)
			particleLayer.add(m);

		movingObjectsLayer.update(deltaMs);
		particleLayer.update(deltaMs);
	}

	/**
	 * Handling Frogger movement from keyboard input
	 */
	public void froggerKeyboardHandler() {
		keyboard.poll();

		boolean keyReleased = false;
		boolean keyReleased2 = false;
		boolean downPressed;
		boolean upPressed;
		boolean leftPressed;
		boolean rightPressed;
		boolean downPressed2;
		boolean upPressed2;
		boolean leftPressed2;
		boolean rightPressed2;

		downPressed = keyboard.isPressed(KeyEvent.VK_S);
		downPressed2 = keyboard.isPressed(KeyEvent.VK_DOWN);

		upPressed = keyboard.isPressed(KeyEvent.VK_W);
		upPressed2 = keyboard.isPressed(KeyEvent.VK_UP);

		leftPressed = keyboard.isPressed(KeyEvent.VK_A);
		leftPressed2 = keyboard.isPressed(KeyEvent.VK_LEFT);

		rightPressed = keyboard.isPressed(KeyEvent.VK_D);
		rightPressed2 = keyboard.isPressed(KeyEvent.VK_RIGHT);

		// Enable/Disable cheating
		if (keyboard.isPressed(KeyEvent.VK_C)) {
			frog.cheating = true;
		}
		if (keyboard.isPressed(KeyEvent.VK_V)) {
			frog.cheating = false;
		}
		if (keyboard.isPressed(KeyEvent.VK_0)) {
			GameLevel = 10;
			initializeLevel(GameLevel);
		}
		if (keyboard.isPressed(KeyEvent.VK_O)) {

			System.out.println(frog.getPosition());
		}

		/*
		 * This logic checks for key strokes.
		 * It registers a key press, and ignores all other key strokes
		 * until the first key has been released
		 */
		if (downPressed || upPressed || leftPressed || rightPressed) {
			keyPressed = true;
		} else if (keyPressed) {
			keyReleased = true;
		}

		if (downPressed2 || upPressed2 || leftPressed2 || rightPressed2) {
			keyPressed2 = true;
		} else if (keyPressed2) {
			keyReleased2 = true;
		}

		if (listenInput) {
			if (downPressed)
				frog.moveDown();
			if (upPressed)
				frog.moveUp();
			if (leftPressed)
				frog.moveLeft();
			if (rightPressed)
				frog.moveRight();

			if (keyPressed)
				listenInput = false;
		}

		if (twoPlayer) {
			if (listenInput2) {
				if (downPressed2)
					frog2.moveDown();
				if (upPressed2)
					frog2.moveUp();
				if (leftPressed2)
					frog2.moveLeft();
				if (rightPressed2)
					frog2.moveRight();

				if (keyPressed2)
					listenInput2 = false;
			}
		} else {
			if (listenInput2) {
				if (downPressed2)
					frog.moveDown();
				if (upPressed2)
					frog.moveUp();
				if (leftPressed2)
					frog.moveLeft();
				if (rightPressed2)
					frog.moveRight();

				if (keyPressed2)
					listenInput2 = false;
			}
		}

		if (keyReleased) {
			listenInput = true;
			keyPressed = false;
		}

		if (keyReleased2) {
			listenInput2 = true;
			keyPressed2 = false;
		}

		if (keyboard.isPressed(KeyEvent.VK_ESCAPE)) {
			GameState = GAME_INTRO;
		}

	}

	/**
	 * Handle keyboard events while at the game intro menu
	 */
	public void menuKeyboardHandler() {
		keyboard.poll();

		// Following 2 if statements allow capture space bar key strokes
		if (!keyboard.isPressed(KeyEvent.VK_SPACE)) {
			space_has_been_released = true;
		}

		if (!space_has_been_released)
			return;

		if (keyboard.isPressed(KeyEvent.VK_SPACE)) {
			switch (GameState) {
				case GAME_INSTRUCTIONS:

				case SKIN_Selection:

				case GAME_OVER:
					GameState = GAME_INTRO;
					space_has_been_released = false;
					break;
				default:
					GameLives = FROGGER_LIVES;
					GameScore = 0;
					GameLevel = STARTING_LEVEL;
					levelTimer = DEFAULT_LEVEL_TIME;
					frog.setPosition(FROGGER_START);
					if (twoPlayer) {
						frog2.setPosition(FROGGER_START);
					}
					GameState = GAME_PLAY;
					audiofx.playGameMusic();
					initializeLevel(GameLevel);
			}
		}
		if (keyboard.isPressed(KeyEvent.VK_H)) {
			GameState = GAME_INSTRUCTIONS;

		}

		if (keyboard.isPressed(KeyEvent.VK_K)) {
			GameState = SKIN_Selection;

		}
	}

	/**
	 * Handle keyboard when finished a level
	 */
	public void finishLevelKeyboardHandler() {
		keyboard.poll();
		if (keyboard.isPressed(KeyEvent.VK_SPACE)) {
			GameState = GAME_PLAY;
			audiofx.playGameMusic();
			initializeLevel(++GameLevel);
		}
	}

	/**
	 * w00t
	 */
	public void update(long deltaMs) {
		switch (GameState) {
			case GAME_PLAY:

				froggerKeyboardHandler();
				wind.update(deltaMs);
				hwave.update(deltaMs);
				frog.update(deltaMs);
				if (twoPlayer) {
					frog2.update(deltaMs);
				}
				audiofx.update(deltaMs);
				ui.update(deltaMs);

				cycleTraffic(deltaMs);
				frogCol.testCollision(movingObjectsLayer);
				frogCol2.testCollision(movingObjectsLayer);

				// Wind gusts work only when Frogger is on the river
				if (frogCol.isInRiver())
					wind.start(GameLevel);
				wind.perform(frog, GameLevel, deltaMs);

				// Do the heat wave only when Frogger is on hot pavement
				if (frogCol.isOnRoad())
					hwave.start(frog, GameLevel);
				hwave.perform(frog, deltaMs, GameLevel);

				// Wind gusts work only when Frogger is on the river
				if (frogCol2.isInRiver())
					wind.start(GameLevel);
				wind.perform(frog, GameLevel, deltaMs);

				// Do the heat wave only when Frogger is on hot pavement
				if (frogCol2.isOnRoad())
					hwave.start(frog, GameLevel);
				hwave.perform(frog, deltaMs, GameLevel);

				if (!frog.isAlive)
					particleLayer.clear();

				if (twoPlayer) {
					if (!frog2.isAlive)
						particleLayer.clear();
				}

				goalmanager.update(deltaMs);

				if (twoPlayer) {
					if (goalmanager.getUnreached().size() == 0) {
						GameState = GAME_FINISH_LEVEL;
						audiofx.playCompleteLevel();
						particleLayer.clear();
					}
				} else {
					if (goalmanager.getUnreached().size() == 0) {
						GameState = GAME_FINISH_LEVEL;
						audiofx.playCompleteLevel();
						particleLayer.clear();
					}
				}

				if (GameLives < 1) {
					GameState = GAME_OVER;
				}

				break;

			case GAME_OVER:
			case GAME_INSTRUCTIONS:
			case SKIN_Selection:
			case GAME_INTRO:
				goalmanager.update(deltaMs);
				menuKeyboardHandler();
				cycleTraffic(deltaMs);
				break;

			case GAME_FINISH_LEVEL:
				finishLevelKeyboardHandler();
				break;
		}
	}

	/**
	 * Rendering game objects
	 */
	public void render(RenderingContext rc) {
		switch (GameState) {
			case GAME_FINISH_LEVEL:
			case GAME_PLAY:
				backgroundLayer.render(rc);

				if (frog.isAlive) {
					movingObjectsLayer.render(rc);
					// frog.collisionObjects.get(0).render(rc);
					frog.render(rc);

				} else {
					frog.render(rc);

					movingObjectsLayer.render(rc);
				}

				if (twoPlayer) {
					if (frog2.isAlive) {
						movingObjectsLayer.render(rc);
						frog2.render(rc);
					} else {
						frog2.render(rc);

						movingObjectsLayer.render(rc);
					}
				}

				particleLayer.render(rc);
				ui.render(rc);
				break;

			case GAME_OVER:
				new Score(GameScore);

			case GAME_INSTRUCTIONS:

			case SKIN_Selection:

			case GAME_INTRO:
				backgroundLayer.render(rc);
				movingObjectsLayer.render(rc);
				ui.render(rc);
				break;
		}
		newBackground = ResourceFactory.getFactory().getFrames(backGround).get(0);

	}

	public static void main(String[] args) {

		scoreLog scoreLog = new scoreLog();

		Scanner scanner = new Scanner(System.in);

		JFrame frame = new JFrame("TestFile Example");

		System.out.println("1 or 2 players? 1 for 1, 2 for 2");
		int players = scanner.nextInt();
		if (players == 2) {
			twoPlayer = true;
		} else {
			twoPlayer = false;
		}

		System.out.println("What difficulty would you like to play at?");
		System.out.println("1: very easy 2: easy 3: normal 4: hard 5: don't ");
		int diffculty = scanner.nextInt();

		switch (diffculty) {
			case 1:
				speedMultipler = 0.5;

				break;

			case 2:
				speedMultipler = 0.75;

				break;

			case 4:
				speedMultipler = 2;

				break;

			case 5:
				speedMultipler = 20;

				break;

			default:
				speedMultipler = 1;

				break;
		}

		frame.add(new JLabel(new ImageIcon("src/resources/frogger_skins.png")));
		frame.setSize(350, 400); // Adjust size as needed
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		boolean hasskinSelected = false;
		while (!hasskinSelected) {
			System.out.println("Which skin would you like? Enter the corresponding number.");

			skinSelected = scanner.nextInt();

			if (skinSelected == 1) {
				CurrentSkin = "#frog_pink";
				hasskinSelected = true;
			} else if (skinSelected == 2) {
				CurrentSkin = "#frog_normal";
				hasskinSelected = true;
			} else if (skinSelected == 3) {
				CurrentSkin = "#frog_fish";
				hasskinSelected = true;
			}
		}

		if (hasskinSelected) {
			scanner.close();
			frame.setVisible(false);
		}

		Main f = new Main();
		f.run();

		scanner.close();

	}
}