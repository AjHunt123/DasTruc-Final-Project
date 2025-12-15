import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GameOverScreen extends JPanel {
    // Add serialVersionUID to address serialization warning
    private static final long serialVersionUID = 1L;
    
    private final JButton restartButton;
    private final JButton exitButton;
    private final JButton menuButton;
    private GamePanel gamePanel; // Reference to the GamePanel for level information
    private final Timer fadeTimer;
    private float alpha = 0.0f;
    private int yOffset = 50;
    private Runnable onRestart;
    
    public GameOverScreen(Runnable onRestart, Runnable onExit, Runnable onMenuReturn) {
        setLayout(null); // Use absolute positioning
        
        this.onRestart = onRestart;
        
        // Create buttons but don't position them yet (will be done in onShow)
        restartButton = createStyledButton("Restart from Last Level", 300);
        exitButton = createStyledButton("Exit Game", 380);
        menuButton = createStyledButton("Return to Menu", 460);
        // Initially make buttons invisible
        restartButton.setVisible(false);
        exitButton.setVisible(false);
        menuButton.setVisible(false);
        // Add action listeners using method references to avoid duplicate code
        restartButton.addActionListener(e -> {
            if (onRestart != null) {
                onRestart.run();
            }
        });
        exitButton.addActionListener(e-> {
                int choice = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to exit?", 
                "Confirm Exit", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            if (onExit != null) {
                onExit.run();
            }
            System.exit(0);
        }
            
        });
        menuButton.addActionListener(e -> {
            if (onMenuReturn != null) {
                onMenuReturn.run();
            }
        });
        // Add buttons to panel
        add(restartButton);
        add(exitButton);
        add(menuButton);
        // Set up fade-in animation
        fadeTimer = new Timer(30, e -> {
            updateFade();
            repaint();
        });
    }
    // Method to update the restart action after GamePanel is created
    public void setRestartAction(Runnable onRestart) {
        if (onRestart == null) {
            return; // Guard against null
        }
        this.onRestart = onRestart;
        // Update the action listener to use the new runnable
        for (ActionListener al : restartButton.getActionListeners()) {
            restartButton.removeActionListener(al);
        }
        restartButton.addActionListener(e -> {
            if (this.onRestart != null) {
                this.onRestart.run();
            }
        });
    }
    // Method to set the GamePanel reference
    public void setGamePanel(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }
    private JButton createStyledButton(String text, int yPosition) {
        JButton button = new JButton(text);
        button.setBounds(0, yPosition, 300, 60); // X will be set in onShow
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(70, 10, 10));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        // Update button positions when panel size changes
        if (restartButton != null) {
            restartButton.setBounds(width/2 - 150, 300, 300, 60);
            exitButton.setBounds(width/2 - 150, 380, 300, 60);
            menuButton.setBounds(width/2 - 150, 460, 300, 60);
        }
    }
    public void onShow() {
        // Reset button positions based on current panel size
        restartButton.setBounds(getWidth()/2 - 150, 300, 300, 60);
        exitButton.setBounds(getWidth()/2 - 150, 380, 300, 60);
        menuButton.setBounds(getWidth()/2 - 150, 460, 300, 60);
        // Reset animation
        alpha = 0.0f;
        yOffset = 50;
        // Hide buttons initially
        restartButton.setVisible(false);
        exitButton.setVisible(false);
        menuButton.setVisible(false);
        // Start fade-in animation
        if (fadeTimer != null && !fadeTimer.isRunning()) {
            fadeTimer.start();
        }
        // Ensure we have focus for key events
        requestFocusInWindow();
    }
    // Method to clean up resources when this panel is no longer needed
    public void cleanup() {
        if (fadeTimer != null && fadeTimer.isRunning()) {
            fadeTimer.stop();
        }
    }
    private void updateFade() {
        alpha += 0.05f;
        if (alpha > 1.0f) alpha = 1.0f;
        yOffset -= 2;
        if (yOffset < 0) yOffset = 0;
        // Show buttons after fade-in is mostly complete
        if (alpha > 0.7f) {
            restartButton.setVisible(true);
            exitButton.setVisible(true);
            menuButton.setVisible(true);
        }
        // Stop timer when animation is complete
        if (alpha >= 1.0f && yOffset <= 0) {
            fadeTimer.stop();
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw dark background with gradient
        GradientPaint bgGradient = new GradientPaint(
            0, 0, new Color(40, 0, 0),
            0, getHeight(), new Color(10, 0, 0)
        );
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        // Set opacity for fade effect
        float displayAlpha = Math.min(1.0f, alpha * 1.5f);
        g2d.setComposite(java.awt.AlphaComposite.getInstance(
            java.awt.AlphaComposite.SRC_OVER, displayAlpha));
        // Draw game over text with slight animation
        g2d.setFont(new Font("Impact", Font.BOLD, 70));
        GradientPaint textGradient = new GradientPaint(
            0, 100, new Color(200, 0, 0),
            0, 180, new Color(255, 50, 50)
        );
        g2d.setPaint(textGradient);
        String gameOverText = "GAME OVER";
        int textWidth = g2d.getFontMetrics().stringWidth(gameOverText);
        g2d.drawString(gameOverText, getWidth()/2 - textWidth/2, 150 + yOffset);
        // Draw subtitle
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(200, 200, 200));
        String subtitle = "Your journey ends here...";
        int subtitleWidth = g2d.getFontMetrics().stringWidth(subtitle);
        g2d.drawString(subtitle, getWidth()/2 - subtitleWidth/2, 200 + yOffset);
        // Draw score with shadow effect - ensure gamePanel isn't null
        if (gamePanel != null) {
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            // Draw shadow
            g2d.setColor(new Color(100, 0, 0));
            String scoreText = "Score: " + gamePanel.calculateFinalScore();
            int scoreWidth = g2d.getFontMetrics().stringWidth(scoreText);
            g2d.drawString(scoreText, getWidth()/2 - scoreWidth/2 + 2, 250 + yOffset + 2);
            // Draw text
            g2d.setColor(new Color(255, 200, 200));
            g2d.drawString(scoreText, getWidth()/2 - scoreWidth/2, 250 + yOffset);
        }
    }
}