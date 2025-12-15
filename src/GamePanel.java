import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.IllegalComponentStateException;
import java.awt.Image;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
public class GamePanel extends JPanel {
    private Player player;
    private final List<Level> levels;
    private int currentLevel;
    private int lastPlayedLevel; // Store the last played level
    private boolean gameOver, gameWon, paused;
    private String currentUsername;
    private final Runnable onLevelComplete;
    private final Runnable onGameOver;
    private final UserManager userManager;
    private Timer gameTimer;
    private boolean gameStarted = false;
    private long lastMissileFiredTime = 0;
    private static final long serialVersionUID = 1L;
    private static final long MISSILE_COOLDOWN = 200; // Milliseconds between missiles
    private Runnable onVictory;
    private static final float DEFAULT_MISSILE_FIRE_VOLUME = 0.6f;
    public static final int SOUND_MISSILE_HIT = 1;
    private final List<Ricochet> ricochets;

    public boolean isGameWon() {
        return gameWon;
    }

    public GamePanel(Runnable onLevelComplete, Runnable onGameOver, Runnable onVictory, UserManager userManager) {
        this.onLevelComplete = onLevelComplete;
        this.onGameOver = onGameOver;
        this.onVictory = onVictory;
        this.userManager = userManager;
        this.levels = new ArrayList<>();
        this.currentLevel = 0;
        this.lastPlayedLevel = 0; // Initialize last played level
        this.gameWon = false;
        this.gameOver = false;
        this.paused = false;
        this.ricochets = new ArrayList<>();
    
        initializeLevels();
        loadSoundEffects();
        // Prompt for username
        promptUsername();
    
        // Add mouse motion listener to track mouse movement
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (!gameOver && !paused && gameStarted) {
                    player.move(e.getX(), e.getY()); // Move player to mouse position
                    repaint(); // Redraw the panel
                }
            }
        });
    
        // Set up keyboard input for pause feature using Key Bindings instead of KeyListener
        setupKeyBindings();
    
        setFocusable(true);
        createGameTimer();
    }
    private void loadSoundEffects() {
        // Load missile fire sound
        SoundUtility.loadSE(Missile.SOUND_MISSILE_FIRE, "sound/laserSE.wav");
        SoundUtility.setSoundVolume(Missile.SOUND_MISSILE_FIRE, DEFAULT_MISSILE_FIRE_VOLUME);
        // Fix the path for missile hit sound
        SoundUtility.loadSE(SOUND_MISSILE_HIT, "sound/HitSE.wav");
        SoundUtility.setSoundVolume(SOUND_MISSILE_HIT, DEFAULT_MISSILE_FIRE_VOLUME);
    }
    
    // Add to GamePanel class: method to update sound volumes from settings
    public void updateSoundVolumes(float missileFireVolume, float missileHitVolume) {
        SoundUtility.setSoundVolume(Missile.SOUND_MISSILE_FIRE, missileFireVolume);
        SoundUtility.setSoundVolume(SOUND_MISSILE_HIT, missileHitVolume);
    }
    
    private void setupKeyBindings() {
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();
        
        // Add key binding for Pause (P key)
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pause");
        actionMap.put("pause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
        
        // Also allow Space bar to pause
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "pause");
    }
    
    public void cleanup() {
        if (gameTimer != null && gameTimer.isRunning()) {
            gameTimer.stop();
        }
        // Remove any listeners that might prevent garbage collection
        removeMouseMotionListener(getMouseMotionListeners()[0]);
    }
    private void confineCursor() {
        try {
            Robot robot = new Robot();
    
            // Get component bounds in screen coordinates
            java.awt.Point compScreenLocation = getLocationOnScreen();
            int minScreenX = compScreenLocation.x;
            int minScreenY = compScreenLocation.y;
            int maxScreenX = minScreenX + getWidth() - 1;
            int maxScreenY = minScreenY + getHeight() - 1;
    
            // Get current cursor position in screen coordinates
            java.awt.PointerInfo pointerInfo = java.awt.MouseInfo.getPointerInfo();
            if (pointerInfo != null) {
                java.awt.Point screenMouse = pointerInfo.getLocation();
    
                // Check if cursor is outside the component bounds
                boolean outOfBounds = false;
                int newX = screenMouse.x;
                int newY = screenMouse.y;
    
                if (screenMouse.x < minScreenX) {
                    newX = minScreenX;
                    outOfBounds = true;
                } else if (screenMouse.x > maxScreenX) {
                    newX = maxScreenX;
                    outOfBounds = true;
                }
    
                if (screenMouse.y < minScreenY) {
                    newY = minScreenY;
                    outOfBounds = true;
                } else if (screenMouse.y > maxScreenY) {
                    newY = maxScreenY;
                    outOfBounds = true;
                }
    
                // Only move if cursor is out of bounds
                if (outOfBounds) {
                    robot.mouseMove(newX, newY);
    
                    // Update player position to match the new confined position
                    int compX = newX - compScreenLocation.x;
                    int compY = newY - compScreenLocation.y;
                    player.move(compX, compY);
                }
            }
        } catch (AWTException | IllegalComponentStateException | NullPointerException | IllegalArgumentException e) {
            System.err.println("Cursor confinement error: " + e.getMessage());
        }
    }
    


    private void togglePause() {
        paused = !paused;
        if (paused) {
            gameTimer.stop();
        } else {
            gameTimer.start();
        }
        repaint();
        System.out.println("Game Paused: " + paused);
    }

    private void promptUsername() {
        currentUsername = JOptionPane.showInputDialog("Enter your username:");
        if (currentUsername == null || currentUsername.isEmpty()) {
            currentUsername = "Guest";
        }
        userManager.createUser(currentUsername, 0);
    }

    public void startGame() {
        gameStarted = true;
        gameOver = false;
        gameWon = false;
        paused = false;
        
        // Reset player with initial stats
        player = new Player(500, 500, 80, 100, 60, 700, 5); // Initial values
        
        currentLevel = 0;
        lastPlayedLevel = 0;
        
        initializeLevels(); // Re-initialize levels
        ricochets.clear(); // Clear any existing ricochets
        
        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }
        gameTimer.start(); // Start game loop
        repaint();
        
        // Ensure panel has focus
        requestFocusInWindow();
    }
    public void setOnVictory(Runnable onVictory) {
        this.onVictory = onVictory;
    }
    private void createGameTimer() {
        gameTimer = new Timer(10, e -> {
            if (!gameOver && gameStarted && !paused) {
                updateGame();
                repaint();
            }
        });
    }
    private void updateGame() {
        confineCursor();
        player.update();
        Level level = levels.get(currentLevel);
        List<Enemy> enemies = level.getEnemies();
        
        // Update last played level
        lastPlayedLevel = Math.max(lastPlayedLevel, currentLevel);
    
        // Move enemies
        for (Enemy enemy : new ArrayList<>(enemies)) {
            enemy.moveTowards(player.getX(), player.getY());
        
            if (enemy.collidesWith(player)) {
                player.reduceHealth(enemy.getDamage());
                if (player.getHealth() <= 0) {
                    gameOver = true;
                    gameTimer.stop();
                    userManager.updateUserScore(currentUsername, calculateFinalScore());
                    onGameOver.run(); // Trigger game-over screen
                    return;
                }
            }
        }
       
        // Update ricochets
        for (Ricochet ricochet : new ArrayList<>(ricochets)) {
            ricochet.move(getWidth(), getHeight());
            
            // Check collision with player
            if (ricochet.collidesWith(player.getX(), player.getY(), player.getSize())) {
                player.reduceHealth(ricochet.getDamage());
                if (player.getHealth() <= 0) {
                    gameOver = true;
                    gameTimer.stop();
                    userManager.updateUserScore(currentUsername, calculateFinalScore());
                    onGameOver.run();
                    return;
                }
            }
            
            // Check collision with enemies
            for (Enemy enemy : new ArrayList<>(enemies)) {
                if (ricochet.collidesWith(enemy.getX(), enemy.getY(), enemy.getSize())) {
                    enemy.takeDamage(ricochet.getDamage());
                    if (enemy.isDead()) {
                        enemies.remove(enemy);
                    }
                }
            }
        }
       
        // Fire missiles with cooldown
        long currentTime = System.currentTimeMillis();
        if (!enemies.isEmpty() && currentTime - lastMissileFiredTime >= MISSILE_COOLDOWN) {
            Enemy nearestEnemy = findNearestEnemy(enemies);
            if (nearestEnemy != null) {
                player.fireMissile(nearestEnemy.getX(), nearestEnemy.getY());
                lastMissileFiredTime = currentTime;
            }
        }
        // Update missiles
        List<Missile> missiles = player.getMissiles();
        for (Missile missile : new ArrayList<>(missiles)) {
            if (!missile.isActive()) {
                missiles.remove(missile);
                continue;
            }
            Enemy targetEnemy = findNearestEnemy(enemies);
            if (targetEnemy != null) {
                missile.moveTowards(targetEnemy.getX(), targetEnemy.getY());
                // Check for collisions between missile and enemies
                for (Enemy enemy : new ArrayList<>(enemies)) {
                    if (missileCollidesWithEnemy(missile, enemy)) {
                        // Play impact sound BEFORE modifying the enemy and missile
                        SoundUtility.playSE(SOUND_MISSILE_HIT);
                        
                        enemy.takeDamage(missile.getDamage());
                        missile.setActive(false);
                        
                        if (enemy.isDead()) {
                            enemies.remove(enemy); // Remove dead enemy
                        }
                        break;
                    }
                }
            }
        }
        // Check if level is complete
        if (enemies.isEmpty()) {
            if (currentLevel < levels.size() - 1) {
                currentLevel++;
                gameTimer.stop();
                // Initialize ricochets for the next level
                initializeRicochets();
                onLevelComplete.run();
            } else {
                // Level 10 completed - game is won
                gameOver = true;
                gameWon = true;
                gameTimer.stop();
                userManager.updateUserScore(currentUsername, calculateFinalScore() * 2); // Bonus for winning
                
                // Call victory screen instead of game over screen
                SwingUtilities.invokeLater(() -> {
                    if (onVictory != null) {
                        onVictory.run(); // New callback for victory screen
                    } else {
                        onGameOver.run(); // Fallback to game over screen if victory callback not set
                    }
                });
            }
        }
    }
    private void initializeRicochets() {
        ricochets.clear();
    
        int screenWidth = getWidth();
        int screenHeight = getHeight();
switch (currentLevel + 1) {
    case 6 -> {
        ricochets.add(new Ricochet(screenWidth / 2, 0, 15, 5, 6, 5, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(0, screenHeight / 3, 15, 7, 4, 6, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(screenWidth, screenHeight / 2, 15, -6, 5, 7, Ricochet.FIRE_STYLE));
    }
    case 7 -> {
        ricochets.add(new Ricochet(0, screenHeight / 3, 15, 17, 11, 6, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth, screenHeight / 3, 15, -7,11, 7, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth / 2, 0, 15, 9, 9, 8, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(screenWidth / 2, screenHeight, 15, 1, -9, 9, Ricochet.FIRE_STYLE));
    }
    case 8 -> {
        ricochets.add(new Ricochet(100, 100, 15, 15, 15, 8, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth - 100, 100, 15, -7, 15, 8, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(100, screenHeight - 100, 15, 7, -7, 9, Ricochet.FIRE_STYLE));
        ricochets.add(new Ricochet(screenWidth - 100, screenHeight - 100, 15, -7, -7, 7, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth / 2, 0, 15, 9, 9, 1, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(screenWidth / 2, screenHeight, 15, 9, -9, 8, Ricochet.FIRE_STYLE));
    }
    case 9 -> {
        ricochets.add(new Ricochet(100, 100, 15, 15, 15, 7, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth - 100, 100, 15, -7, 15, 8, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(200, screenHeight / 2, 15, 13, 8, 1, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth / 2, 100, 15, 0, 6, 10, Ricochet.FIRE_STYLE));
        ricochets.add(new Ricochet(screenWidth / 4, 0, 15, 4, 4, 9, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(screenWidth, screenHeight / 4, 15, -8, 8, 8, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(0, screenHeight - 200, 15, 10, -5, 9, Ricochet.FIRE_STYLE));
    }
    case 10 -> {
        ricochets.add(new Ricochet(100, 100, 15, 15, 15, 10, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth - 100, 100, 15, -7, 15, 11, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(screenWidth, screenHeight / 3, 10, -9, 4, 5, Ricochet.FIRE_STYLE));
        ricochets.add(new Ricochet(screenWidth / 4, 0, 25, 5, 7, 7, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(screenWidth / 2, screenHeight, 15, 6, -6, 8, Ricochet.FIRE_STYLE));
        ricochets.add(new Ricochet(0, screenHeight / 2, 20, 8, 3, 7, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth / 2, 0, 12, -4, 10, 6, Ricochet.LIGHTNING_STYLE));
    }
    case 11 -> {
        ricochets.add(new Ricochet(100, 100, 15, 15, 15, 7, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth - 100, 100, 15, -7, 15, 7, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(100, 0, 15, 11, 8, 6, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(300, 0, 15, 11, 9, 5, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(500, 0, 15, 11, 8, 9, Ricochet.FIRE_STYLE));
        ricochets.add(new Ricochet(0, 150, 15, 9, 0, 7, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(0, 350, 15, 10, 0, 6, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(0, 550, 15, 9, 0, 6, Ricochet.FIRE_STYLE));
    }
    case 12 -> {
        ricochets.add(new Ricochet(100, 100, 15, 11, 9, 6, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth, screenHeight / 3, 20, -13, 5, 5, Ricochet.FIRE_STYLE));
        ricochets.add(new Ricochet(screenWidth / 2, screenHeight, 25, 4, -11, 5, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(screenWidth / 7, 0, 15, 5, 10, 8, Ricochet.LIGHTNING_STYLE));
        ricochets.add(new Ricochet(screenWidth - 200, screenHeight - 200, 12, -10, -10, 8, Ricochet.FIRE_STYLE));
        ricochets.add(new Ricochet(0, screenHeight / 4, 17, 15, 0, 9, Ricochet.WIND_STYLE));
        ricochets.add(new Ricochet(screenWidth / 5, 0, 12, 0, 10, 7, Ricochet.LIGHTNING_STYLE));
    }
}
    }
    
    public int calculateFinalScore() {
        // Example scoring logic: Use the player's health and current level to calculate the score
        return player.getHealth() * 10 + (currentLevel + 1) * 100;
    }
    public void startNextLevel() {
        if (currentLevel < levels.size()) {
            gameOver = false;
            // Preserve player stats between levels EXCEPT health which resets to 100
            int currentMissileDamage = player.getMissileDamage();
            int currentAttackSpeed = player.getAttackSpeed();
            int currentMoveSpeed = player.getMoveSpeed();
            
            // Create new player with full health (100) but keep other upgrades
            player = new Player(500, 500, 80, 100, currentMissileDamage, currentAttackSpeed, currentMoveSpeed);
            
            // Initialize ricochets for the new level
            initializeRicochets();
            
            gameTimer.start();
            repaint();
            
            // Ensure panel has focus
            requestFocusInWindow();
        }
    }
    public void restartGame() {
        gameOver = false;
        gameWon = false;
        gameStarted = true;
        // Reset player with some upgrades maintained for game balance
        int currentMissileDamage = player != null ? player.getMissileDamage() : 500;
        int currentAttackSpeed = player != null ? player.getAttackSpeed() : 1000;
        int currentMoveSpeed = player != null ? player.getMoveSpeed() : 6;
        // Create a new player with full health but keep some upgrades
        player = new Player(500, 500, 80, 100, currentMissileDamage, currentAttackSpeed, currentMoveSpeed);
        // Reinitialize levels but maintain the progress
        initializeLevels();
        // Set the current level to the last played level
        currentLevel = lastPlayedLevel;
        // Clear and initialize ricochets for current level
        ricochets.clear();
        initializeRicochets();
        // Restart the game timer
        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }
        gameTimer.start();
        repaint();
        requestFocusInWindow();
        System.out.println("Game restarted at level: " + (currentLevel + 1));
    }
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (gameOver) {
            gameTimer.stop();
        }
        repaint();
    }
    public boolean isGameOver() {
        return gameOver;
    }
    public Player getPlayer() {
        return player;
    }
    
    // Method to get the current level (for display)
    public int getCurrentLevel() {
        return currentLevel + 1; // +1 because it's 0-indexed internally
    }
    // Methods to create enemies for each level
    private List<Enemy> createLevel1Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(100, 100, 40, 2, 3, 100, 4));
        enemies.add(new Enemy(450, 100, 40, 3, 3, 100, 4));
        enemies.add(new Enemy(800, 100, 40, 4, 3, 100, 5));
        enemies.add(new Enemy(1150, 100, 40, 3, 3, 100, 4));
        enemies.add(new Enemy(1500, 100, 40, 3, 3, 100, 5));
        return enemies;
    }
    private List<Enemy> createLevel2Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(100, 100, 40, 4, 4, 110, 10));
        enemies.add(new Enemy(450, 100, 40, 3, 4, 100, 9));
        enemies.add(new Enemy(800, 100, 40, 3, 4, 100, 9));
        enemies.add(new Enemy(1150, 100, 40, 3, 4, 100, 10));
        enemies.add(new Enemy(1500, 100, 40, 4, 4, 100, 10));
        return enemies;
    }
    private List<Enemy> createLevel3Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(100, 100, 40, 3, 5, 110, 15));
        enemies.add(new Enemy(450, 100, 40, 2, 5, 100, 15));
        enemies.add(new Enemy(800, 100, 40, 5, 5, 100, 16));
        enemies.add(new Enemy(1150, 100, 40, 4, 5, 100, 15));
        enemies.add(new Enemy(1500, 100, 40, 3, 5, 100, 16));
        return enemies;
    }
    private List<Enemy> createLevel4Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(100, 100, 40, 4, 6, 110, 20));
        enemies.add(new Enemy(450, 100, 40, 4, 7, 100, 20));
        enemies.add(new Enemy(800, 100, 40, 5, 8, 110, 21));
        enemies.add(new Enemy(1150, 100, 40, 4, 8, 100, 20));
        enemies.add(new Enemy(1500, 100, 40, 4, 9, 100, 20));
        return enemies;
    }
    private List<Enemy> createLevel5Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(400, 100, 80, 5, 15, 210, 36));
        enemies.add(new Enemy(200, 150, 40, 6, 9, 125, 26));
        enemies.add(new Enemy(600, 150, 40, 6, 9, 125, 26));
        return enemies;
    }
    private List<Enemy> createLevel6Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(100, 100, 45, 5, 9, 130, 26));
        enemies.add(new Enemy(400, 100, 45, 4, 9, 120, 26));
        enemies.add(new Enemy(700, 100, 45, 6, 10, 130, 27));
        enemies.add(new Enemy(1000, 100, 45, 4, 11, 120, 25));
        enemies.add(new Enemy(1300, 100, 45, 4, 23, 130, 26));
        enemies.add(new Enemy(1600, 100, 45, 4, 20, 120, 25));
        return enemies;
    }
    private List<Enemy> createLevel7Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(150, 110, 50, 4, 10, 155, 30));
        enemies.add(new Enemy(450, 120, 50, 4, 11, 155, 30));
        enemies.add(new Enemy(750, 90, 50, 6, 12, 150, 31));
        enemies.add(new Enemy(1050, 120, 50, 4, 13, 155, 30));
        enemies.add(new Enemy(1350, 110, 50, 4, 14, 150, 31));
        return enemies;
    }
    private List<Enemy> createLevel8Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(100, 100, 55, 4, 11, 185, 32));
        enemies.add(new Enemy(350, 120, 55, 4, 12, 180, 31));
        enemies.add(new Enemy(600, 200, 55, 3, 13, 180, 32));
        enemies.add(new Enemy(850, 150, 55, 4, 14, 180, 31));
        enemies.add(new Enemy(1100, 100, 55, 3, 15, 180, 32));
        enemies.add(new Enemy(1350, 120, 55, 4, 16, 180, 31));
        return enemies;
    }
    private List<Enemy> createLevel9Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(100, 100, 55, 3, 12, 185, 33));
        enemies.add(new Enemy(350, 130, 55, 5, 13, 180, 32));
        enemies.add(new Enemy(600, 200, 55, 3, 14, 180, 33));
        enemies.add(new Enemy(850, 150, 55, 4, 15, 180, 32));
        enemies.add(new Enemy(1100, 100, 55, 3, 16, 180, 33));
        enemies.add(new Enemy(1350, 130, 55, 4, 17, 180, 32));
        return enemies;
    }
    private List<Enemy> createLevel10Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(100, 100, 55, 5, 13, 185, 36));
        enemies.add(new Enemy(350, 140, 55, 3, 14, 180, 35));
        enemies.add(new Enemy(600, 200, 55, 3, 15, 180, 36));
        enemies.add(new Enemy(850, 150, 55, 3, 16, 180, 35));
        enemies.add(new Enemy(1100, 100, 55, 3, 17, 180, 36));
        enemies.add(new Enemy(1350, 140, 55, 2, 18, 180, 35));
        return enemies;
    }
    
    private List<Enemy> createLevel11Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(150, 120, 60, 5, 14, 210, 41));
        enemies.add(new Enemy(400, 150, 60, 4, 15, 200, 40));
        enemies.add(new Enemy(650, 180, 60, 3, 16, 200, 40));
        enemies.add(new Enemy(900, 180, 60, 3, 17, 200, 40));
        enemies.add(new Enemy(1150, 150, 60, 4, 18, 200, 41));
        enemies.add(new Enemy(1400, 150, 60, 5, 19, 200, 41));
        return enemies;
    }
    private List<Enemy> createLevel12Enemies() {
        List<Enemy> enemies = new ArrayList<>();
        enemies.add(new Enemy(700, 150, 150, 3, 30, 850, 72));
        enemies.add(new Enemy(650, 250, 60, 4, 15, 510, 22));
        enemies.add(new Enemy(1000, 250, 60, 5, 16, 510, 22));
        enemies.add(new Enemy(900, 250, 60, 4, 17, 510, 22));
        enemies.add(new Enemy(800, 350, 60, 5, 20, 510, 22));
        return enemies;
    }
    private void initializeLevels() {
        levels.clear();
        // Level 1
    levels.add(new Level(1, createLevel1Enemies(), "images/GundamBG.jpg"));
    levels.add(new Level(2, createLevel2Enemies(), "images/bgace.jpg"));
    levels.add(new Level(3, createLevel3Enemies(), "images/bgluffy.jpg"));
    levels.add(new Level(4, createLevel4Enemies(), "images/bgZenitsu.jpg"));
    levels.add(new Level(5, createLevel5Enemies(), "images/bgzoro.jpg"));
    levels.add(new Level(6, createLevel6Enemies(), "images/bgrank3.jpg"));
    levels.add(new Level(7, createLevel7Enemies(), "images/bgkenpachi.jpg"));
    levels.add(new Level(8, createLevel8Enemies(), "images/bgItachi.jpg"));
    levels.add(new Level(9, createLevel9Enemies(), "images/bgNetero.jpg"));
    levels.add(new Level(10, createLevel10Enemies(), "images/bgGoku.jpg"));
    levels.add(new Level(11, createLevel11Enemies(), "images/bgMadara.jpg"));
    levels.add(new Level(12, createLevel12Enemies(), "images/BGfinal.jpg"));
    }
    private Enemy findNearestEnemy(List<Enemy> enemies) {
        if (enemies.isEmpty()) {
            return null;
        }
        Enemy nearest = null;
        double minDistance = Double.MAX_VALUE;
        for (Enemy enemy : enemies) {
            double dx = enemy.getX() - player.getX();
            double dy = enemy.getY() - player.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = enemy;
            }
        }
        return nearest;
    }
    private boolean missileCollidesWithEnemy(Missile missile, Enemy enemy) {
        int dx = missile.getX() - enemy.getX();
        int dy = missile.getY() - enemy.getY();
        int distance = (int) Math.sqrt(dx * dx + dy * dy);
        return distance < (missile.getSize() / 2 + enemy.getSize() / 2); // Use proper size for missile
    }
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow(); // Ensures panel can receive key events
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (gameStarted) {
            Level currentLevelObj = levels.get(currentLevel);
            Image backgroundImage = currentLevelObj.getBackgroundImage();
            if (backgroundImage != null) {
                g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        g.setColor(new Color(245, 255, 250));
        g.setFont(new Font("Garamond", Font.BOLD, 35));
        g.drawString("Level: " + (currentLevel + 1), 750, 40);
        // Health display with regeneration indicator
        g.setColor(new Color(0, 255, 0));
        g.drawString("HP: " + player.getHealth() + "/" + player.getMaxHealth(), 10, 40);
        g.setColor(new Color(0, 200, 0, 150));
        g.drawString("+5/sec", 10, 80); // Regeneration indicator
            if (gameOver) {
                g.setFont(new Font("Garamond", Font.BOLD, 40));
                g.setColor(Color.WHITE);
                if (gameWon) {
                    g.drawString("Game Completed!", 300, 300);
                } else {
                    g.drawString("Game Over!", 300, 300);
                }
                g.drawString("Score: " + calculateFinalScore(), 300, 350);
            } else if (paused) {
                g.setFont(new Font("Garamond", Font.BOLD, 40));
                g.setColor(Color.WHITE);
                g.drawString("Game Paused", 300, 300);
            } else {
                player.draw(g);
                // Draw missiles
                for (Missile missile : player.getMissiles()) {
                    missile.draw(g);
                }
                // Draw enemies
                for (Enemy enemy : levels.get(currentLevel).getEnemies()) {
                    enemy.draw(g);
                }
                
                // Draw ricochets
                for (Ricochet ricochet : ricochets) {
                    ricochet.draw(g);
                }
            }
        }
    }
}