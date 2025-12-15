import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Player {
    private int x, y;
    private int size;
    private int health;
    private int maxHealth;  // Added to track maximum health
    private int missileDamage;
    private int attackSpeed; // milliseconds between attacks
    private int moveSpeed;
    private long lastAttackTime;
    private long lastRegenTime;  // Added to track regeneration
    private List<Missile> missiles;
    private BufferedImage playerImage;
    private static final int REGEN_AMOUNT = 5;  // HP to regenerate per second
    private static final long REGEN_INTERVAL = 1000;  // 1 second in milliseconds

    public Player(int x, int y, int size, int health, int missileDamage, int attackSpeed, int moveSpeed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.health = health;
        this.maxHealth = health;  // Initialize max health
        this.missileDamage = missileDamage;
        this.attackSpeed = attackSpeed;
        this.moveSpeed = moveSpeed;
        this.missiles = new ArrayList<>();
        this.lastAttackTime = System.currentTimeMillis();
        this.lastRegenTime = System.currentTimeMillis();  // Initialize regeneration timer
        
        // Load player image
        try {
            playerImage = ImageIO.read(new File("images/player.png"));
        } catch (IOException e) {
            System.err.println("Error loading player image: " + e.getMessage());
        }
    }

    // Call this method in your game loop to update regeneration
    public void update() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRegenTime >= REGEN_INTERVAL) {
            health = Math.min(maxHealth, health + REGEN_AMOUNT);
            lastRegenTime = currentTime;
        }
    }


    public void move(int targetX, int targetY) {
        // Calculate direction
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Only move if not already at target
        if (distance > moveSpeed) {
            x += (dx / distance) * moveSpeed;
            y += (dy / distance) * moveSpeed;
        } else {
            // If very close, just set to target position
            x = targetX;
            y = targetY;
        }
    }

    public void fireMissile(int targetX, int targetY) {
        long currentTime = System.currentTimeMillis();
        // Only fire if attack cooldown has passed
        if (currentTime - lastAttackTime >= attackSpeed) {
            // Create missile at player position
            Missile missile = new Missile(x + size/2, y + size/2, 8, missileDamage);
            missiles.add(missile);
            lastAttackTime = currentTime;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        // Enable anti-aliasing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (playerImage != null) {
            g2.drawImage(playerImage, x - size/2, y - size/2, size, size, null);
        } else {
            // Fallback if image not loaded
            g2.setColor(Color.BLUE);
            g2.fillOval(x - size/2, y - size/2, size, size);
        }
        
        // Draw missiles
        for (Missile missile : missiles) {
            missile.draw(g);
        }
        
        // Draw health bar (now shows percentage of max health)
        g2.setColor(Color.BLACK);
        g2.fillRect(x - size/2, y - size/2 - 15, size, 5);
        g2.setColor(Color.GREEN);
        g2.fillRect(x - size/2, y - size/2 - 15, (int)(size * ((double)health / maxHealth)), 5);
    }

    public void reduceHealth(int damage) {
        health = Math.max(0, health - damage);
    }

    public void increaseHealth(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public void increaseMissileDamage(int amount) {
        missileDamage += amount;
    }
    
    public void increaseMoveSpeed(int amount) {
        moveSpeed += amount;
    }
    
    public void increaseAttackSpeed(int amount) {
        attackSpeed = Math.max(100, attackSpeed - amount); // Decrease delay (min 100ms)
    }

    // Getters and setters
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
    public int getSize() {
        return size;
    }
    
    public int getHealth() {
        return health;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public int getMissileDamage() {
        return missileDamage;
    }
    
    public int getAttackSpeed() {
        return attackSpeed;
    }
    
    public int getMoveSpeed() {
        return moveSpeed;
    }
    
    public List<Missile> getMissiles() {
        return missiles;
    }
}