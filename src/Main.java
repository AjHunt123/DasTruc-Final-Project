import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Main {
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create the game window
            JFrame frame = new JFrame("Shooter Game");
            frame.setSize(1600, 900);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            // Create a card layout to switch between screens
            CardLayout cardLayout = new CardLayout();
            JPanel mainPanel = new JPanel(cardLayout);
            frame.add(mainPanel);
            
            // Create the MusicPlayer instance
            MusicPlayer musicPlayer = new MusicPlayer();
            
            // Create the UserManager instance
            UserManager userManager = new UserManager();
            
            // Create a temporary dummy runnable for game-related actions that will be initialized later
            Runnable dummyAction = () -> System.out.println("This action will be replaced later");
            
            // Create game over screen first
            GameOverScreen gameOverScreen = new GameOverScreen(
                dummyAction, // Will be set later
                () -> System.exit(0), // Exit the game
                () -> {
                    cardLayout.show(mainPanel, "intro"); // Return to intro screen
                }
            );
            
            // Create victory screen with placeholder actions initially
            VictoryScreen victoryScreen = new VictoryScreen(
                () -> {
                    cardLayout.show(mainPanel, "intro"); // Return to menu
                },
                () -> System.exit(0), // Exit the game
                dummyAction, // Will be updated once GamePanel is created
                musicPlayer // Pass the music player instance
            );
            
            //the GamePanel
            GamePanel gamePanel = new GamePanel(
                () -> {
                    cardLayout.show(mainPanel, "levelUp"); // Show level-up screen when a level is completed
                },
                () -> {
                    // Show game-over screen when the player dies
                    cardLayout.show(mainPanel, "gameOver");
                    gameOverScreen.onShow(); 
                },
                () -> {
                    // Show victory screen when player wins
                    System.out.println("Victory condition triggered");
                    cardLayout.show(mainPanel, "victory");
                    victoryScreen.onShow(); 
                },
                userManager // Pass the UserManager instance
            );
    
            // Now update the actions with the properly initialized gamePanel
            gameOverScreen.setRestartAction(() -> {
                gamePanel.restartGame();
                cardLayout.show(mainPanel, "game");
            });
            
            // The critical fix - properly set the replay action
            victoryScreen.setReplayAction(() -> {
                System.out.println("Starting a new game from victory screen");
                gamePanel.startGame();
                cardLayout.show(mainPanel, "game");
            });
            
            // Set the GamePanel references
            gameOverScreen.setGamePanel(gamePanel);
            victoryScreen.setGamePanel(gamePanel);
            
            // Print to confirm replay action was set
            System.out.println("Replay action set: " + victoryScreen.isReplayActionSet());
            
            // Add panels to the card layout
            mainPanel.add(gamePanel, "game");
            mainPanel.add(gameOverScreen, "gameOver");
            mainPanel.add(victoryScreen, "victory");
            
            // Intro screen
            IntroScreen introScreen = new IntroScreen(
                () -> {
                    gamePanel.startGame(); // Start the game when Play is clicked
                    cardLayout.show(mainPanel, "game");
                },
                () -> JOptionPane.showMessageDialog(frame, "Credits:\n(Hazel + AJ + JL + Emilley)"), // Show credits
                () -> cardLayout.show(mainPanel, "settings"), // Switch to settings screen
                userManager // Pass the UserManager instance
            );
            
            mainPanel.add(introScreen, "intro");
            
            // Settings screen
            SettingsScreen settingsScreen = new SettingsScreen(musicPlayer, gamePanel, () -> cardLayout.show(mainPanel, "intro"));
            mainPanel.add(settingsScreen, "settings");
            
            // Level-up screen
            LevelUpScreen levelUpScreen = new LevelUpScreen(
                () -> {
                    gamePanel.getPlayer().increaseMissileDamage(12); // Add 12 damage
                    cardLayout.show(mainPanel, "game");
                    gamePanel.startNextLevel();
                },
                () -> {
                    gamePanel.getPlayer().increaseMoveSpeed(2); // Increase move speed
                    cardLayout.show(mainPanel, "game");
                    gamePanel.startNextLevel();
                },
                () -> {
                    gamePanel.getPlayer().increaseAttackSpeed(80); // Reduce attack delay by 80ms
                    cardLayout.show(mainPanel, "game");
                    gamePanel.startNextLevel();
                },
                () -> {
                    cardLayout.show(mainPanel, "game");
                    gamePanel.startNextLevel();
                }
            );
            
            mainPanel.add(levelUpScreen, "levelUp");
            
            // Show the intro screen first
            cardLayout.show(mainPanel, "intro");
            
            frame.setVisible(true);
            
            // Play the intro music - NOW WITH RELATIVE PATHS
            musicPlayer.loadMusic("sound/BGdontsaaword.wav", "sound/BGIzzo.wav");
            musicPlayer.play();
        });
    }
}