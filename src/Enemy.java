import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Enemy {
    private int x, y;
    private final int size, speed, damage, armor;
    private int hp;
    private final int maxHp;
    private BufferedImage enemyImage;
    private boolean imageLoaded = false;
    private boolean showHitbox = true; // Toggle for debugging
    
    public Enemy(int x, int y, int size, int speed, int damage, int hp, int armor) {
        // Validate parameters
        if (size <= 0) throw new IllegalArgumentException("Size must be positive");
        if (speed < 0) throw new IllegalArgumentException("Speed cannot be negative");
        if (damage < 0) throw new IllegalArgumentException("Damage cannot be negative");
        if (hp <= 0) throw new IllegalArgumentException("HP must be positive");
        if (armor < 0) throw new IllegalArgumentException("Armor cannot be negative");
        
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
        this.damage = damage;
        this.hp = hp;
        this.maxHp = hp;
        this.armor = armor;
        
        // Load enemy image
        try {
            enemyImage = ImageIO.read(new File("images/enemy.png"));
            imageLoaded = true;
        } catch (IOException e) {
            System.err.println("Error loading enemy image: " + e.getMessage());
            enemyImage = null;
            imageLoaded = false;
        }
    }
    
    public void update(Player player, int screenWidth, int screenHeight) {
        if (player == null) return;
        
        moveTowards(player.getX(), player.getY());
        // Keep enemy within screen bounds
        x = Math.max(size/2, Math.min(x, screenWidth - size/2));
        y = Math.max(size/2, Math.min(y, screenHeight - size/2));
    }
    
    public void moveTowards(int targetX, int targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            // Normalize and apply speed
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
    }
    
    public void draw(Graphics g) {
        if (g == null) return;
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (imageLoaded && enemyImage != null) {
            g2.drawImage(enemyImage, x - size/2, y - size/2, size, size, null);
        } else {
            g2.setColor(Color.RED);
            g2.fillOval(x - size/2, y - size/2, size, size);
        }
        
        // Draw hitbox if enabled
        if (showHitbox) {
            drawHitbox(g2);
        }
        
        // Draw HP bar
        drawHealthBar(g2);
    }
    
    private void drawHitbox(Graphics2D g2) {
        // Save original stroke
        BasicStroke originalStroke = (BasicStroke) g2.getStroke();
        
        // Draw circular hitbox boundary
        g2.setColor(new Color(255, 0, 0, 128)); // Semi-transparent red
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x - size/2, y - size/2, size, size);
        
        // Draw center point
        g2.setColor(Color.RED);
        g2.fillOval(x - 3, y - 3, 6, 6);
        
        // Draw crosshair
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(x - 8, y, x + 8, y);
        g2.drawLine(x, y - 8, x, y + 8);
        
        // Restore original stroke
        g2.setStroke(originalStroke);
    }
    
    private void drawHealthBar(Graphics2D g2) {
        int hpBarWidth = size;
        int hpBarHeight = 5;
        int hpBarY = y - size/2 - 10;
        
        // Background
        g2.setColor(Color.BLACK);
        g2.fillRect(x - size/2, hpBarY, hpBarWidth, hpBarHeight);
        
        // Health fill
        double healthPercentage = (double)hp / maxHp;
        g2.setColor(getHealthColor(healthPercentage));
        
        int fillWidth = (int)(hpBarWidth * Math.max(0, healthPercentage));
        g2.fillRect(x - size/2, hpBarY, fillWidth, hpBarHeight);
    }
    
    private Color getHealthColor(double healthPercentage) {
        if (healthPercentage > 0.75) return Color.GREEN;
        if (healthPercentage > 0.25) return Color.YELLOW;
        return Color.RED;
    }
    
    public boolean collidesWith(Player player) {
        if (player == null) return false;
        
        double dx = x - player.getX();
        double dy = y - player.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (size / 2.0 + player.getSize() / 2.0);
    }
    
    public void takeDamage(int damage) {
        if (damage <= 0) return;
        
        int actualDamage = Math.max(1, damage - armor);
        hp = Math.max(0, hp - actualDamage);
    }
    
    public boolean isDead() {
        return hp <= 0;
    }
    
    // Toggle hitbox visibility
    public void setShowHitbox(boolean show) {
        this.showHitbox = show;
    }
    
    public boolean isShowingHitbox() {
        return showHitbox;
    }
    
    // Getters
    public int getDamage() {
        return damage;
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getSize() {
        return size;
    }
    
    public int getHp() {
        return hp;
    }
    
    public int getMaxHp() {
        return maxHp;
    }
}