import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

public class IntroScreen extends JPanel {
    private final JButton playButton, creditsButton, settingsButton, viewScoresButton, exitButton;
    private final JLabel titleLabel;
    private Image backgroundImage;
    private UserManager userManager;
    private String clickSound;
    private final static SoundEffect se = new SoundEffect();
    // Custom colors
    private final Color BUTTON_COLOR = new Color(127, 255, 212); //bg text button
    private final Color BUTTON_HOVER_COLOR = new Color(147, 112, 219); //hover color
    private final Color BUTTON_TEXT_COLOR = new Color(248, 248, 255); // text color button
    private final Color TITLE_COLOR = new Color(245, 255, 250); //title color
    private final Color DIALOG_BG_COLOR = new Color(50, 20, 80); // Dark purple background for dialogs
    private final Color DIALOG_TEXT_COLOR = new Color(240, 240, 255); // Light text for dialogs

    public IntroScreen(Runnable onPlay, Runnable onCredits, Runnable onSettings, UserManager userManager) {
        
        this.userManager = userManager;
        // Load background image with error handling
        try {
            backgroundImage = new ImageIcon("images/bgGojo.png").getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
        // Set layout to null for custom positioning
        setLayout(null);
        
        // Add game title
        titleLabel = new JLabel("Dota 3 Game", SwingConstants.CENTER);
        titleLabel.setBounds(75, 80, 400, 80);
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 42));
        titleLabel.setForeground(TITLE_COLOR);
        // Add drop shadow effect
        titleLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 2, 2, 2),
            BorderFactory.createLineBorder(new Color(0, 0, 0, 150), 1, true)
        ));
        add(titleLabel);
        // Use a relative path for the click sound
        clickSound = "sound/uwuSE.wav";
        
        // Create styled buttons
        playButton = createStyledButton("Play the Game", 150, 200);
        creditsButton = createStyledButton("Credits", 150, 270);
        settingsButton = createStyledButton("Settings", 150, 340);
        viewScoresButton = createStyledButton("View Scores", 150, 410);
        exitButton = createStyledButton("Exit", 150, 480);
        // Add action listeners to buttons
        playButton.addActionListener(e -> {
            System.out.println("Play button clicked!");
            se.setFile(clickSound);
            se.play();
            onPlay.run();
        });
        creditsButton.addActionListener(e -> {
            se.setFile(clickSound);
            se.play();
            onCredits.run();
        });
        settingsButton.addActionListener(e -> {
            se.setFile(clickSound);
            se.play();
            onSettings.run();
        });
        viewScoresButton.addActionListener(e -> {
            se.setFile(clickSound);
            se.play();
            showUserManagementDialog();
        });
        exitButton.addActionListener(_ -> {
            se.setFile(clickSound);
            se.play();
            int choice = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to exit?", 
                "Confirm Exit", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        // Add buttons to the panel
        add(playButton);
        add(creditsButton);
        add(settingsButton);
        add(viewScoresButton);
        add(exitButton);
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
                    0, getHeight(), new Color(30, 0, 60)
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 25, 25));
                // Draw text with shadow for depth
                g2.setColor(new Color(0, 0, 0, 100));
                g2.setFont(new Font("Garamond", Font.BOLD, 16));
                g2.drawString(getText(), 22, 29);
                g2.setColor(BUTTON_TEXT_COLOR);
                g2.drawString(getText(), 20, 27);
            }
        };
        // Make the button transparent so our custom painting shows
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFont(new Font("Garamond", Font.BOLD, 16));
        button.setBounds(x, y, 200, 50);
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

    // New button style for dialog buttons
    private JButton createDialogButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, getModel().isRollover() ? BUTTON_HOVER_COLOR : BUTTON_COLOR,
                    0, getHeight(), new Color(40, 10, 70)
                );
                g2.setPaint(gradient);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                // Draw text with shadow for depth
                g2.setColor(new Color(0, 0, 0, 100));
                g2.setFont(new Font("Garamond", Font.BOLD, 14));
                g2.drawString(getText(), 12, 24);
                g2.setColor(BUTTON_TEXT_COLOR);
                g2.drawString(getText(), 10, 22);
            }
        };
        
        // Make the button transparent so our custom painting shows
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFont(new Font("Garamond", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 40));
        
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

    private void showUserManagementDialog() {
        // Create a custom dialog
        JDialog dialog = new JDialog((JFrame) getTopLevelAncestor(), "User Management", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        
        // Create a panel with a dark background
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(DIALOG_BG_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Add title at the top
        JLabel dialogTitleLabel = new JLabel("HIGH SCORE", SwingConstants.CENTER);
        dialogTitleLabel.setFont(new Font("Times New Roman", Font.BOLD, 22));
        dialogTitleLabel.setForeground(DIALOG_TEXT_COLOR);
        mainPanel.add(dialogTitleLabel, BorderLayout.NORTH);
        
        // Create the list model and populate with users
        DefaultListModel<String> userListModel = new DefaultListModel<>();
        refreshUserList(userListModel);
        
        // Create the list with custom rendering
        JList<String> userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setFont(new Font("Garamond", Font.PLAIN, 16));
        userList.setForeground(DIALOG_TEXT_COLOR);
        userList.setBackground(new Color(70, 30, 100));
        userList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Put the list in a scroll pane
        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setPreferredSize(new Dimension(450, 250));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 50, 150), 2));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(DIALOG_BG_COLOR);
        
        // Create button for deleting selected user
        JButton deleteButton = createDialogButton("Delete User");
        deleteButton.addActionListener(_ -> {
            se.setFile(clickSound);
            se.play();
            int selectedIndex = userList.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedUser = userListModel.getElementAt(selectedIndex);
                
                // Skip handling for the "No users available yet" message
                if (selectedUser.equals("No users available yet")) {
                    return;
                }
                
                // Improved parsing to extract just the username
                // Format is: "1. username: score"
                String username = selectedUser.split("\\.")[1]; // Split by the period after rank number
                if (username != null) {
                    username = username.trim(); // Remove leading/trailing spaces
                    int colonIndex = username.lastIndexOf(':');
                    if (colonIndex > 0) {
                        username = username.substring(0, colonIndex).trim(); // Extract just the username part
                    }
                }
                
                int choice = JOptionPane.showConfirmDialog(dialog, 
                    "Are you sure you want to delete user '" + username + "'?", 
                    "Confirm Deletion", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
                
                if (choice == JOptionPane.YES_OPTION) {
                    userManager.deleteUser(username);
                    refreshUserList(userListModel);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select a user to delete", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        // Create button for closing the dialog
        JButton closeButton = createDialogButton("Close");
        closeButton.addActionListener(_ -> {
            se.setFile(clickSound);
            se.play();
            dialog.dispose();
        });
        
        // Add buttons to the panel
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to dialog
        dialog.add(mainPanel);
        dialog.setResizable(false);
        dialog.setVisible(true);
    }
    
    
    private void refreshUserList(DefaultListModel<String> model) {
        model.clear();
        List<User> users = userManager.getAllUsers();
        if (users.isEmpty()) {
            model.addElement("No users available yet");
        } else {
            // Sort users by score (highest to lowest)
            users.sort((u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));
            
            // Add users to list with their rank number
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                model.addElement((i + 1) + ". " + user.getUsername() + ": " + user.getScore());
            }
        }
    }
    public static class SoundEffect {
        Clip clip;
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

    public class ButtonHandler implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            se.setFile(clickSound);
            se.play();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;       
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        // Draw background image
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            // Add a semi-transparent overlay for better text visibility
            g2.setColor(new Color(0, 0, 0, 100));
            g2.fillRect(0, 0, getWidth(), getHeight());
        } else {
            // Fallback if image fails to load
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}