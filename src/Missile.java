import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;

public class Missile {
    private int x, y;
    private final int speed, damage;
    private boolean active;
    private final int size = 14; // Slightly larger size for better visual effect
    
    // Color animation properties
    private float hue = 0.0f;
    private static final float HUE_SHIFT_SPEED = 0.05f;
    private static final float SATURATION = 1.0f;
    private static final float BRIGHTNESS = 1.0f;
    
    // Trail effect properties
    private static final int TRAIL_LENGTH = 3;
    private final int[] trailX = new int[TRAIL_LENGTH];
    private final int[] trailY = new int[TRAIL_LENGTH];
    
    // Sound effect indices (define these as constants in your game class)
    public static final int SOUND_MISSILE_FIRE = 0;
    public static final int SOUND_MISSILE_HIT = 1;
    
    public Missile(int x, int y, int speed, int damage) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.damage = damage;
        this.active = true;
        
        // Initialize trail positions
        for (int i = 0; i < TRAIL_LENGTH; i++) {
            trailX[i] = x;
            trailY[i] = y;
        }
        
        // Play missile launch sound effect
        SoundUtility.playSE(SOUND_MISSILE_FIRE);
    }
    
    public void moveTowards(int targetX, int targetY) {
        if (!active) return;
        
        // Update trail positions (shift positions)
        for (int i = TRAIL_LENGTH - 1; i > 0; i--) {
            trailX[i] = trailX[i-1];
            trailY[i] = trailY[i-1];
        }
        trailX[0] = x;
        trailY[0] = y;
        
        // Move missile towards target
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            x += (dx / distance) * speed;
            y += (dy / distance) * speed;
        }
        
        // Update color animation
        hue = (hue + HUE_SHIFT_SPEED) % 1.0f;
    }
    
    // Add this method to handle missile hit/explosion
    public void hit() {
        this.active = false;
        // Play missile hit/explosion sound effect
        SoundUtility.playSE(SOUND_MISSILE_HIT);
    }
    
    public void draw(Graphics g) {
        if (!active) return;
        
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw trail with fading effect
        for (int i = TRAIL_LENGTH - 1; i >= 0; i--) {
            float trailHue = (hue + 0.1f * i) % 1.0f;
            float alpha = 0.7f - (0.2f * i);
            
            Color trailColor = Color.getHSBColor(trailHue, SATURATION, BRIGHTNESS);
            g2d.setColor(new Color(
                trailColor.getRed(), 
                trailColor.getGreen(), 
                trailColor.getBlue(), 
                (int)(alpha * 255)
            ));
            
            int trailSize = size - (i * 2);
            if (trailSize > 0) {
                g2d.fillOval(
                    trailX[i] - trailSize/2, 
                    trailY[i] - trailSize/2, 
                    trailSize, 
                    trailSize
                );
            }
        }
        
        // Get current color from HSB values
        Color primaryColor = Color.getHSBColor(hue, SATURATION, BRIGHTNESS);
        
        // Create a gradient for the main missile
        Point2D center = new Point2D.Float(x, y);
        float radius = size / 2.0f;
        float[] dist = {0.0f, 0.7f, 1.0f};
        
        // Create brighter center with current color
        Color brightCenter = new Color(
            Math.min(255, primaryColor.getRed() + 50),
            Math.min(255, primaryColor.getGreen() + 50),
            Math.min(255, primaryColor.getBlue() + 50)
        );
        
        Color[] colors = {
            Color.WHITE,       // Center is white for glow effect
            brightCenter,      // Mid-radius uses bright version of current color
            primaryColor       // Edge uses primary color
        };
        
        RadialGradientPaint gradient = new RadialGradientPaint(
            center, radius, dist, colors, CycleMethod.NO_CYCLE
        );
        
        // Draw the missile with gradient
        g2d.setPaint(gradient);
        g2d.fillOval(x - size/2, y - size/2, size, size);
        
        // Draw a small white core in the center for extra glow
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - size/6, y - size/6, size/3, size/3);
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
        
        // If missile is being deactivated, it might be because it hit something
        if (!active) {
            // Consider calling hit() instead of direct deactivation
            // Or check why it's being deactivated and play appropriate sounds
        }
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getDamage() {
        return damage;
    }
    
    public int getSize() {
        return size;
    }
}