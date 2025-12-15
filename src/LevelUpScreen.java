import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class LevelUpScreen extends JPanel {
    private final JButton addDamageButton, addProjectileSpeedButton, addAttackSpeedButton;
    private final JLabel selectUpgradeLabel, levelUpTitleLabel;
    
    private Image backgroundImage;
    private String clickSound;
    private final static SoundEffect se = new SoundEffect();
    
    // Custom colors
    private final Color BUTTON_COLOR = new Color(0,255,0);  // button bg color
    private final Color BUTTON_HOVER_COLOR = new Color(220, 20, 60);  // Crimson
    private final Color BUTTON_TEXT_COLOR = new Color(245,255,250); //button text color
    private final Color TITLE_COLOR = new Color(245,255,250);  //level up text
    private final Color SUBTITLE_COLOR = new Color(245,255,250);  // choose your power

    public LevelUpScreen(Runnable onAddDamage, Runnable onAddProjectileSpeed, Runnable onAddAttackSpeed, Runnable onNextLevel) {
        // Load background image with error handling
        try {
            backgroundImage = new ImageIcon("images/bgsukuna.jpg").getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
        setLayout(null); // Disable default layout for custom positioning
        // Add level up title
        levelUpTitleLabel = new JLabel("LEVEL UP!", SwingConstants.CENTER);
        levelUpTitleLabel.setBounds(75, 80, 350, 80);
        levelUpTitleLabel.setFont(new Font("Times New Roman", Font.BOLD, 48));
        levelUpTitleLabel.setForeground(TITLE_COLOR);
        // Add glow effect with border
        levelUpTitleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(3, 3, 3, 3),
            BorderFactory.createLineBorder(new Color(245,255,250), 2, true)
        ));
        add(levelUpTitleLabel);
        // Subtitle with clearer instruction
        selectUpgradeLabel = new JLabel("Choose Your UPGRADE", SwingConstants.CENTER);
        selectUpgradeLabel.setBounds(125, 160, 250, 40);
        selectUpgradeLabel.setFont(new Font("Garamond", Font.BOLD, 24));
        selectUpgradeLabel.setForeground(SUBTITLE_COLOR);
        add(selectUpgradeLabel);
        // Create styled buttons
        addDamageButton = createStyledButton("Increase Damage", 150, 230);
        addProjectileSpeedButton = createStyledButton("Increase Movement Speed", 150, 300);
        addAttackSpeedButton = createStyledButton("Increase Attack Speed", 150, 370);
        // Add descriptions to buttons
        addIconToButton(addDamageButton, "⚔️", "Deal more damage to enemies");
        addIconToButton(addProjectileSpeedButton, "💨", "Move faster across the battlefield");
        addIconToButton(addAttackSpeedButton, "⚡", "Attack more frequently");
        // Use a relative path for the click sound
        clickSound = "sound/LevelUpSE.wav";
        addDamageButton.addActionListener(e -> {
            se.setFile(clickSound);
            se.play();
            onAddDamage.run();
            onNextLevel.run(); // Make sure to trigger the next level after upgrade
        });
        addProjectileSpeedButton.addActionListener(e -> {
            se.setFile(clickSound);
            se.play();
            onAddProjectileSpeed.run();
            onNextLevel.run(); // Make sure to trigger the next level after upgrade
        });
        addAttackSpeedButton.addActionListener(e -> {
            se.setFile(clickSound);
            se.play();
            onAddAttackSpeed.run();
            onNextLevel.run(); // Make sure to trigger the next level after upgrade
        });
        add(addDamageButton);
        add(addProjectileSpeedButton);
        add(addAttackSpeedButton);
    }
    private JButton createStyledButton(String text, int x, int y) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, getModel().isRollover() ? BUTTON_HOVER_COLOR : BUTTON_COLOR,
                    0, getHeight(), new Color(80, 0, 0)
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                // Add a glowing border when hovered
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 190, 150));
                    g2.draw(new RoundRectangle2D.Double(1, 1, getWidth()-3, getHeight()-3, 25, 25));
                }
                // Draw text with shadow for depth
                g2.setColor(new Color(0, 0, 0, 100));
                g2.setFont(new Font("Garamond", Font.BOLD, 16));
                g2.drawString(getText(), 42, 27);
                g2.setColor(BUTTON_TEXT_COLOR);
                g2.drawString(getText(), 40, 25);
            }
        };
        // Make the button transparent so our custom painting shows
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFont(new Font("Garamond", Font.BOLD, 16));
        button.setBounds(x, y, 250, 50);
        // Add hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                button.repaint();
            }
        });
        return button;
    }
    private void addIconToButton(JButton button, String icon, String description) {
        // Add tooltip with description
        button.setToolTipText(description);
        // Modify button text to include icon
        button.setText(icon + " " + button.getText());
    }
    public static class SoundEffect {
        private Clip clip;
        public void setFile(String soundFileName) {
    try {
        File file = new File(soundFileName);
        if (!file.exists()) {
            System.err.println("Sound file not found: " + file.getAbsolutePath());
            return;
        }
        AudioInputStream sound = AudioSystem.getAudioInputStream(file);
        clip = AudioSystem.getClip();
        clip.open(sound);
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
        System.err.println("Error loading sound: " + e.getMessage());
    }
}
        public void play() {
            if (clip != null) {
                clip.setFramePosition(0);
                clip.start();
            }
        }
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Enable antialiasing for smoother rendering
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Draw background image
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            
            // Add a semi-transparent overlay for better text visibility
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRect(0, 0, getWidth(), getHeight());
            
            // Add some particle effects for level up atmosphere
            g2.setColor(new Color(255, 215, 0, 150));
            for (int i = 0; i < 30; i++) {
                int size = (int)(Math.random() * 5) + 2;
                int x = (int)(Math.random() * getWidth());
                int y = (int)(Math.random() * getHeight());
                g2.fillOval(x, y, size, size);
            }
        } else {
            // Fallback if image fails to load
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}