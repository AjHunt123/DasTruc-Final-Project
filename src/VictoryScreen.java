import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;


public class VictoryScreen extends JPanel {
    private final JButton menuButton, exitButton, replayButton;
    private GamePanel gamePanel; // Reference to the GamePanel for score information
    private final Timer animationTimer;
    private float alpha = 0.0f; // For fade-in effect
    private int titleYPos = -100; // Title starts off-screen
    private final List<Particle> particles; // Celebratory particles
    private float messageAlpha = 0.0f; // For message fade-in
    private int scoreCounter = 0; // For counting up score animation
    private boolean scoreAnimationComplete = false;
    private int sparklePhase = 0; // For sparkling text effect
    private final Random random = new Random();
    private Runnable onReplay;
    private final MusicPlayer musicPlayer;
    // Text for congratulations paragraph
    private final String[] congratsText = {
        "CONGRATULATIONS HERO!",
        "You have defeated all enemies and saved the universe!",
        "Your courage and skill have brought peace to the realm.",
        "Your legend will be remembered throughout the ages!"
    };
    
    public VictoryScreen(Runnable onMenuReturn, Runnable onExit, Runnable onReplay, MusicPlayer musicPlayer) {
        setLayout(null); // Use absolute positioning
        particles = new ArrayList<>();
        this.onReplay = onReplay;
        this.musicPlayer = musicPlayer;
        // Create styled buttons
        menuButton = createStyledButton("Return to Menu", 500);
        exitButton = createStyledButton("Exit Game", 580);
        replayButton = createStyledButton("Play Again", 660);
        
        // Initially set buttons invisible (will fade in)
        menuButton.setVisible(false);
        exitButton.setVisible(false);
        replayButton.setVisible(false);
        
        // Add action listeners
        menuButton.addActionListener(_ -> {
            onHide(); 
            if (onMenuReturn != null) {
                onMenuReturn.run();
            }
        });
        
        exitButton.addActionListener(_ -> {
            onHide();
            if (onExit != null) {
                onExit.run();
            }
        });
        
        // IMPORTANT CHANGE: Use method reference instead of capturing the initial value
        replayButton.addActionListener(_ -> {
            onHide();
            // This will use the current value of onReplay when clicked, not the initial value
            handleReplayAction();
        });
        
        // Add buttons to panel
        add(menuButton);
        add(exitButton);
        add(replayButton);
        
        // Initialize animation timer
        animationTimer = new Timer(16, _ -> {
            updateAnimation();
            repaint();
        });
    }
    
    // New helper method to handle replay action
    private void handleReplayAction() {
        if (onReplay != null) {
            System.out.println("Play Again button clicked! Running replay action...");
            onReplay.run();
        } else {
            System.out.println("Error: onReplay action is null!");
        }
    }
    
    private void playVictoryMusic() {
        if (musicPlayer != null) {
            musicPlayer.loadMusic("sound/victoryBGM.wav","sound/victoryBGM.wav"); // Load victory music file
            musicPlayer.play(); // Play the music
        }
    }
    
    // Stop victory music
    private void stopVictoryMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop(); // Stop the currently playing music
        }
    }
    
    // Method to update the replay action after GamePanel is created
    public void setReplayAction(Runnable onReplay) {
        this.onReplay = onReplay;
        System.out.println("Replay action has been set: " + (onReplay != null));
        // Additional debugging line to help trace the issue
        if (onReplay != null) {
            System.out.println("Replay action successfully assigned!");
        }
    }
    
    // Method to set the GamePanel reference
    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        System.out.println("GamePanel reference set in VictoryScreen");
    }
    
    // Method to check if replay action is properly set
    public boolean isReplayActionSet() {
        return onReplay != null;
    }
    
    public void startAnimation() {
        // Reset animation properties
        alpha = 0.0f;
        titleYPos = -100;
        messageAlpha = 0.0f;
        scoreCounter = 0;
        scoreAnimationComplete = false;
        particles.clear();
        
        // Make buttons invisible initially
        menuButton.setVisible(false);
        exitButton.setVisible(false);
        replayButton.setVisible(false);
        
        // Start the animation
        animationTimer.start();
    }
    
    private void updateAnimation() {
        // Update fade-in effect
        if (alpha < 1.0f) {
            alpha += 0.02f;
            if (alpha > 1.0f) alpha = 1.0f;
        }
        
        // Animate title sliding in from top
        if (titleYPos < 100) {
            titleYPos += 5;
            if (titleYPos > 100) titleYPos = 100;
        }
        
        // Fade in message text after title is in place
        if (titleYPos >= 100 && messageAlpha < 1.0f) {
            messageAlpha += 0.01f;
            if (messageAlpha > 1.0f) messageAlpha = 1.0f;
        }
        
        // Animate score counter
        if (messageAlpha >= 0.7f && !scoreAnimationComplete && gamePanel != null) {
            int targetScore = gamePanel.calculateFinalScore() * 2; // Double score for winning
            if (scoreCounter < targetScore) {
                scoreCounter += Math.max(1, targetScore / 100); // Gradually increase
                if (scoreCounter > targetScore) scoreCounter = targetScore;
            } else {
                scoreAnimationComplete = true;
                // Show buttons after score animation completes
                menuButton.setVisible(true);
                exitButton.setVisible(true);
                replayButton.setVisible(true);
                
                // Verify replay action is set when buttons become visible
                System.out.println("Buttons now visible. Replay action status: " + (onReplay != null ? "SET" : "NULL"));
            }
        }
        
        // Update sparkle effect
        sparklePhase = (sparklePhase + 1) % 100;
        
        // Add new particles occasionally
        if (random.nextInt(5) == 0 && alpha > 0.5f) {
            addParticle();
        }
        
        // Update existing particles
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.alpha <= 0) {
                particles.remove(i);
            }
        }
    }
    
    private void addParticle() {
        int x = random.nextInt(getWidth());
        int y = random.nextInt(getHeight());
        float size = 5 + random.nextFloat() * 20;
        Color color;
        
        // Choose random bright colors for particles
        color = switch (random.nextInt(5)) {
            case 0 -> new Color(255, 215, 0);      // Gold
            case 1 -> new Color(255, 100, 100);    // Light red
            case 2 -> new Color(100, 255, 100);    // Light green
            case 3 -> new Color(100, 100, 255);    // Light blue
            default -> new Color(255, 255, 255);   // White
        };
        particles.add(new Particle(x, y, size, color));
    }
    
    private JButton createStyledButton(String text, int yPosition) {
        JButton button = new JButton(text);
        button.setBounds(getWidth()/2 - 150, yPosition, 300, 60);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setBackground(new Color(30, 30, 60));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
    
    // Method called when this panel is shown
    public void onShow() {
        // Reset button positions based on current panel size
        menuButton.setBounds(getWidth()/2 - 150, 500, 300, 60);
        exitButton.setBounds(getWidth()/2 - 150, 580, 300, 60);
        replayButton.setBounds(getWidth()/2 - 150, 660, 300, 60);
        
        // Check if replay action is set
        System.out.println("Victory Screen shown. Replay action is " + (onReplay != null ? "set" : "NOT SET"));
        
        // Ensure buttons are properly initialized
        menuButton.setVisible(false);
        exitButton.setVisible(false);
        replayButton.setVisible(false);
        
        // Start animation sequence
        startAnimation();
        
        // Play victory music
        playVictoryMusic();
    }
    
    // Method to be called when leaving this screen
    public void onHide() {
        stopVictoryMusic();
    }
    
    // Rest of the class remains unchanged...
    // (paintComponent and Particle inner class code unchanged)
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Set background with gradient
        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(0, getHeight());
        float[] dist = {0.0f, 0.3f, 0.7f, 1.0f};
        Color[] colors = {
            new Color(10, 10, 40),
            new Color(30, 30, 80),
            new Color(40, 40, 100),
            new Color(10, 10, 40)
        };
        
        LinearGradientPaint bgGradient = new LinearGradientPaint(start, end, dist, colors);
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw particles behind everything else
        for (Particle p : particles) {
            p.draw(g2d);
        }
        
        // Draw animated title with glow effect
        drawGlowingTitle(g2d);
        
        // Draw congratulatory text paragraph with fade-in effect
        drawCongratsParagraph(g2d);
        
        // Draw animated score display
        drawScoreDisplay(g2d);
    }
    
    private void drawGlowingTitle(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        String title = "VICTORY!";
        Font titleFont = new Font("Impact", Font.BOLD, 80);
        g2d.setFont(titleFont);
        
        // Draw glow effect
        for (int i = 10; i > 0; i--) {
            float glowAlpha = alpha * 0.1f;
            g2d.setColor(new Color(255, 215, 0, (int)(glowAlpha * 255 / i)));
            g2d.drawString(title, getWidth()/2 - g2d.getFontMetrics().stringWidth(title)/2 - i/2, titleYPos + i/2);
            g2d.drawString(title, getWidth()/2 - g2d.getFontMetrics().stringWidth(title)/2 + i/2, titleYPos - i/2);
        }
        
        // Draw base text with gradient
        GradientPaint textGradient = new GradientPaint(
            0, titleYPos - 40, new Color(255, 215, 0),  // Gold at top
            0, titleYPos + 40, new Color(255, 100, 0)   // Orange at bottom
        );
        g2d.setPaint(textGradient);
        g2d.drawString(title, getWidth()/2 - g2d.getFontMetrics().stringWidth(title)/2, titleYPos);
        
        // Sparkle effect on title text
        if (sparklePhase % 20 < 10 && alpha >= 1.0f) {
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.setStroke(new BasicStroke(2.0f));
            int sparkleX = getWidth()/2 - g2d.getFontMetrics().stringWidth(title)/2 + random.nextInt(g2d.getFontMetrics().stringWidth(title));
            int sparkleY = titleYPos - 30 + random.nextInt(60);
            int sparkleSize = 5 + random.nextInt(10);
            
            // Draw star-like sparkle
            for (int i = 0; i < 8; i++) {
                double angle = Math.PI * i / 4;
                g2d.drawLine(
                    sparkleX, sparkleY,
                    (int)(sparkleX + Math.cos(angle) * sparkleSize),
                    (int)(sparkleY + Math.sin(angle) * sparkleSize)
                );
            }
        }
    }
    
    private void drawCongratsParagraph(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, messageAlpha));
        g2d.setColor(Color.WHITE);
        
        int baseY = titleYPos + 100;
        int spacing = 40;
        
        for (int i = 0; i < congratsText.length; i++) {
            Font font = (i == 0) ? 
                new Font("Arial", Font.BOLD, 30) : 
                new Font("Arial", Font.PLAIN, 22);
            g2d.setFont(font);
            
            String line = congratsText[i];
            int lineWidth = g2d.getFontMetrics().stringWidth(line);
            g2d.drawString(line, getWidth()/2 - lineWidth/2, baseY + i * spacing);
        }
    }
    
    private void drawScoreDisplay(Graphics2D g2d) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, messageAlpha));
        
        String scoreLabel = "FINAL SCORE:";
        String scoreValue = Integer.toString(scoreCounter);
        
        // Draw score label
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        g2d.setColor(new Color(200, 200, 255));
        int labelWidth = g2d.getFontMetrics().stringWidth(scoreLabel);
        g2d.drawString(scoreLabel, getWidth()/2 - labelWidth/2, titleYPos + 250);
        
        // Draw score value with special effect if animation complete
        if (scoreAnimationComplete) {
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            
            // Create rainbow effect for the score text
            for (int i = 0; i < scoreValue.length(); i++) {
                float hue = (sparklePhase + i * 30) % 360 / 360.0f;
                g2d.setColor(Color.getHSBColor(hue, 0.8f, 1.0f));
                String character = scoreValue.substring(i, i+1);
                
                int totalWidth = g2d.getFontMetrics().stringWidth(scoreValue);
                int startX = getWidth()/2 - totalWidth/2;
                
                int x = startX;
                for (int j = 0; j < i; j++) {
                    x += g2d.getFontMetrics().stringWidth(scoreValue.substring(j, j+1));
                }
                
                g2d.drawString(character, x, titleYPos + 320);
            }
        } else {
            // Simple display during animation
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.setColor(Color.WHITE);
            int valueWidth = g2d.getFontMetrics().stringWidth(scoreValue);
            g2d.drawString(scoreValue, getWidth()/2 - valueWidth/2, titleYPos + 320);
        }
    }
    
    // Inner class for particle effects
    private class Particle {
        float x, y;
        float xVel, yVel;
        float size;
        float alpha;
        Color color;
        
        public Particle(float x, float y, float size, Color color) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.color = color;
            this.alpha = 1.0f;
            
            // Random velocity
            this.xVel = -1 + random.nextFloat() * 2;
            this.yVel = -1 + random.nextFloat() * 2;
        }
        
        public void update() {
            x += xVel;
            y += yVel;
            alpha -= 0.01f;
            size *= 0.99f;
        }
        
        public void draw(Graphics2D g2d) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(color);
            g2d.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
        }
    }
    
}