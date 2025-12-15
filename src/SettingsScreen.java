import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;

public class SettingsScreen extends JPanel {
    private final MusicPlayer musicPlayer;
    private final GamePanel gamePanel;
    private JSlider volumeSlider;
    private JSlider soundEffectsSlider;
    private JToggleButton muteButton;
    
    // Simple color scheme
    private final Color backgroundColor = new Color(40, 44, 52);
    private final Color textColor = Color.WHITE;
    private final Color accentColor = new Color(86, 156, 214);
    private final Font titleFont = new Font("Arial", Font.BOLD, 20);
    private final Font regularFont = new Font("Arial", Font.PLAIN, 14);

    // Default volume value
    private final int DEFAULT_VOLUME = 70;

    public SettingsScreen(MusicPlayer musicPlayer, GamePanel gamePanel, Runnable onBack) {
        this.musicPlayer = musicPlayer;
        this.gamePanel = gamePanel;
        
        // Set default volumes
        musicPlayer.setVolume(DEFAULT_VOLUME / 100f);
        SoundUtility.setSoundVolume(Missile.SOUND_MISSILE_FIRE, DEFAULT_VOLUME / 100f);
        SoundUtility.setSoundVolume(GamePanel.SOUND_MISSILE_HIT, DEFAULT_VOLUME / 100f);
        
        // Setup panel properties
        setLayout(new BorderLayout(10, 10));
        setBackground(backgroundColor);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Create the components
        JPanel headerPanel = createHeaderPanel();
        JPanel contentPanel = createContentPanel();
        JPanel buttonPanel = createButtonPanel(onBack);
        
        // Add components to main panel
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(backgroundColor);
        panel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(titleFont);
        titleLabel.setForeground(textColor);
        
        panel.add(titleLabel);
        return panel;
    }
    
    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(backgroundColor);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Music volume controls
        JLabel musicLabel = createLabel("Music Volume");
        
        volumeSlider = createSlider(DEFAULT_VOLUME);
        volumeSlider.addChangeListener(_ -> {
            float volume = volumeSlider.getValue() / 100f;
            musicPlayer.setVolume(volume);
        });
        
        muteButton = createToggleButton("Mute");
        muteButton.addActionListener(_-> {
            if (musicPlayer.isMuted()) {
                musicPlayer.unmute();
                muteButton.setText("Mute");
            } else {
                musicPlayer.mute();
                muteButton.setText("Unmute");
            }
        });
        
        // Sound effects controls
        JLabel soundLabel = createLabel("Sound Effects");
        
        soundEffectsSlider = createSlider(DEFAULT_VOLUME);
        soundEffectsSlider.addChangeListener(_ -> {
            float volume = soundEffectsSlider.getValue() / 100f;
            // Apply the same volume to all sound effects
            SoundUtility.setSoundVolume(Missile.SOUND_MISSILE_FIRE, volume);
            SoundUtility.setSoundVolume(GamePanel.SOUND_MISSILE_HIT, volume);
        });
        
        JButton testSoundButton = createButton("Test Sound");
        testSoundButton.addActionListener(_ -> {
            SoundUtility.playSE(Missile.SOUND_MISSILE_FIRE);
        });
        
        // Add components with spacing
        panel.add(musicLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(volumeSlider);
        panel.add(Box.createVerticalStrut(5));
        panel.add(muteButton);
        panel.add(Box.createVerticalStrut(20));
        
        panel.add(soundLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(soundEffectsSlider);
        panel.add(Box.createVerticalStrut(5));
        panel.add(testSoundButton);
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(regularFont);
        label.setForeground(textColor);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private JSlider createSlider(int defaultValue) {
        JSlider slider = new JSlider(0, 100, defaultValue);
        slider.setBackground(backgroundColor);
        slider.setForeground(textColor);
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setAlignmentX(Component.LEFT_ALIGNMENT);
        return slider;
    }
    
    private JToggleButton createToggleButton(String text) {
        JToggleButton button = new JToggleButton(text);
        button.setBackground(accentColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(100, 30));
        return button;
    }
    
    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(accentColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(100, 30));
        return button;
    }
    
    private JPanel createButtonPanel(Runnable onBack) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(backgroundColor);
        
        JButton saveButton = createButton("Save");
        saveButton.addActionListener(_ -> {
            // Apply all sound settings at once
            float soundEffectsVolume = soundEffectsSlider.getValue() / 100f;
            gamePanel.updateSoundVolumes(soundEffectsVolume, soundEffectsVolume);
            onBack.run();
        });
        
        JButton cancelButton = createButton("Cancel");
        cancelButton.addActionListener(_ -> onBack.run());
        
        panel.add(saveButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(cancelButton);
        return panel;
    }
}