import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ricochet {
    // Constants for styles
    public static final int LIGHTNING_STYLE = 0;
    public static final int FIRE_STYLE = 1;
    public static final int WIND_STYLE = 2;
    
    private int x;
    private int y;
    private final int size;
    private int speedX;
    private int speedY;
    private final int damage;
    private final int style;
    private final Random random;
    
    
    // Style-specific properties
    private Color mainColor;
    private Color secondaryColor;
    private Color coreColor;
    
    // For lightning effect
    private static final int LIGHTNING_SEGMENTS = 5;
    private List<Point> lightningPoints;
    
    // For fire effect
    private List<Particle> fireParticles;
    private int fireUpdateCounter = 0;
    
    // For wind effect
    private double windAngle = 0;
    private List<WindTrail> windTrails;
    
    // Particle class for fire effect
    private class Particle {
        int x, y;
        int lifespan;
        int currentLife;
        double speedX, speedY;
        Color color;
        
        public Particle(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.lifespan = 10 + random.nextInt(10);
            this.currentLife = this.lifespan;
            this.speedX = -1 + random.nextDouble() * 2;
            this.speedY = -2 - random.nextDouble() * 2;
            this.color = color;
        }
        
        public void update() {
            x += speedX;
            y += speedY;
            currentLife--;
        }
        
        public boolean isDead() {
            return currentLife <= 0;
        }
        
        public void draw(Graphics2D g2d) {
            float alpha = (float)currentLife / lifespan;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(color);
            int particleSize = (int)(size * 0.3 * alpha);
            g2d.fillOval(x, y, particleSize, particleSize);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
    }
    
    // Wind trail class for wind effect
    private class WindTrail {
        List<Point> points;
        int lifespan;
        int currentLife;
        Color color;
        
        public WindTrail(int startX, int startY, double angle, Color color) {
            this.points = new ArrayList<>();
            this.points.add(new Point(startX, startY));
            
            // Generate curved trail points
            int segmentLength = size / 2;
            double baseAngle = angle;
            int trailLength = 5 + random.nextInt(3);
            
            for (int i = 0; i < trailLength; i++) {
                double curveModifier = (random.nextDouble() - 0.5) * 0.3;
                baseAngle += curveModifier;
                
                int nextX = (int)(points.get(i).x + Math.cos(baseAngle) * segmentLength);
                int nextY = (int)(points.get(i).y + Math.sin(baseAngle) * segmentLength);
                
                this.points.add(new Point(nextX, nextY));
            }
            
            this.lifespan = 15 + random.nextInt(10);
            this.currentLife = this.lifespan;
            this.color = color;
        }
        
        public void update() {
            currentLife--;
        }
        
        public boolean isDead() {
            return currentLife <= 0;
        }
        
        public void draw(Graphics2D g2d) {
            float alpha = (float)currentLife / lifespan;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.setColor(color);
            
            // Draw connected line segments
            g2d.setStroke(new BasicStroke(1 + 2 * alpha));
            
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
            
            g2d.setComposite(AlphaComposite.SrcOver);
            g2d.setStroke(new BasicStroke(1.0f));
        }
    }
    
    // Simple point class
    private class Point {
        int x, y;
        
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    public Ricochet(int x, int y, int size, int speedX, int speedY, int damage, int style) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speedX = speedX;
        this.speedY = speedY;
        this.damage = damage;
        this.style = style;
        this.random = new Random();
        
        // Initialize style-specific properties
        initializeStyle();
        
        // Create initial animation elements
        updateAnimationElements();
    }
    
    private void initializeStyle() {
        switch (style) {
            case LIGHTNING_STYLE -> {
                mainColor = new Color(100, 180, 255); // Electric blue
                secondaryColor = new Color(220, 240, 255); // Light blue
                coreColor = new Color(255, 255, 255); // White core
                lightningPoints = new ArrayList<>();
            }
            
            case FIRE_STYLE -> {
                mainColor = new Color(255, 80, 0); // Orange-red
                secondaryColor = new Color(255, 200, 0); // Yellow-orange
                coreColor = new Color(255, 255, 200); // Bright yellow core
                fireParticles = new ArrayList<>();
            }
            
            case WIND_STYLE -> {
                mainColor = new Color(180, 255, 180); // Light green
                secondaryColor = new Color(220, 255, 220); // Very light green
                coreColor = new Color(240, 255, 240); // Almost white
                windTrails = new ArrayList<>();
            }
        }
    }
    
    private void updateAnimationElements() {
        
        
        switch (style) {
            case LIGHTNING_STYLE -> updateLightningAnimation();
            case FIRE_STYLE -> updateFireAnimation();
            case WIND_STYLE -> updateWindAnimation();
        }
        
    }
    
    private void updateLightningAnimation() {
        // Clear previous lightning points
        lightningPoints.clear();
        
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Generate new lightning path
        int lastX = centerX;
        int lastY = centerY;
        lightningPoints.add(new Point(lastX, lastY));
        
        for (int i = 0; i < LIGHTNING_SEGMENTS; i++) {
            // Calculate lightning zigzag effect - direction based on movement
            int nextX = lastX + (speedX > 0 ? 1 : -1) * (Math.abs(speedX) * 2 + random.nextInt(10) - 5); 
            int nextY = lastY + (speedY > 0 ? 1 : -1) * (Math.abs(speedY) * 2 + random.nextInt(10) - 5);
            
            lightningPoints.add(new Point(nextX, nextY));
            
            lastX = nextX;
            lastY = nextY;
        }
    }
    
    private void updateFireAnimation() {
        // Update existing particles
        for (int i = fireParticles.size() - 1; i >= 0; i--) {
            Particle p = fireParticles.get(i);
            p.update();
            if (p.isDead()) {
                fireParticles.remove(i);
            }
        }
        
        // Add new particles every few frames
        fireUpdateCounter++;
        if (fireUpdateCounter >= 2) { // Adjust for particle generation rate
            int centerX = x + size / 2 - size / 4;
            int centerY = y + size / 2 - size / 4;
            
            // Generate 2-3 new particles
            int particlesToAdd = 2 + random.nextInt(2);
            for (int i = 0; i < particlesToAdd; i++) {
                // Randomize position slightly around center
                int particleX = centerX + random.nextInt(size/2) - size/4;
                int particleY = centerY + random.nextInt(size/2) - size/4;
                
                // Alternate between main and secondary color
                Color particleColor = (random.nextBoolean()) ? mainColor : secondaryColor;
                
                fireParticles.add(new Particle(particleX, particleY, particleColor));
            }
            
            fireUpdateCounter = 0;
        }
    }
    
    private void updateWindAnimation() {
        // Update wind angle
        windAngle += 0.1;
        if (windAngle > Math.PI * 2) {
            windAngle -= Math.PI * 2;
        }
        
        // Update existing trails
        for (int i = windTrails.size() - 1; i >= 0; i--) {
            WindTrail trail = windTrails.get(i);
            trail.update();
            if (trail.isDead()) {
                windTrails.remove(i);
            }
        }
        
        // Add new trail occasionally
        if (random.nextDouble() < 0.2) {
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            
            // Create trail starting from center, moving in general direction of ricochet
            double trailAngle = Math.atan2(speedY, speedX) + (random.nextDouble() - 0.5) * 1.0;
            
            // Alternate between colors
            Color trailColor = (random.nextBoolean()) ? mainColor : secondaryColor;
            
            windTrails.add(new WindTrail(centerX, centerY, trailAngle, trailColor));
        }
    }
    
    public void move(int panelWidth, int panelHeight) {
        // Update position
        x += speedX;
        y += speedY;
        
        // Bounce off screen edges
        if (x <= 0 || x >= panelWidth - size) {
            speedX = -speedX;
            x = Math.max(0, Math.min(x, panelWidth - size)); // Keep within bounds
        }
        
        if (y <= 0 || y >= panelHeight - size) {
            speedY = -speedY;
            y = Math.max(0, Math.min(y, panelHeight - size)); // Keep within bounds
        }
        
        // Update animation elements
        updateAnimationElements();
    }
    
    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
    
        switch (style) {
            case LIGHTNING_STYLE -> drawLightningStyle(g2d);
            case FIRE_STYLE -> drawFireStyle(g2d);
            case WIND_STYLE -> drawWindStyle(g2d);
        }
    }
    
    
    private void drawLightningStyle(Graphics2D g2d) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Draw lightning trails
        g2d.setColor(secondaryColor);
        g2d.setStroke(new BasicStroke(2.0f));
        
        // Draw main lightning path
        for (int i = 0; i < lightningPoints.size() - 1; i++) {
            Point p1 = lightningPoints.get(i);
            Point p2 = lightningPoints.get(i + 1);
            g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            
            // Small branch with 30% chance
            if (i > 0 && random.nextDouble() < 0.3) {
                int branchX = p2.x + random.nextInt(20) - 10;
                int branchY = p2.y + random.nextInt(20) - 10;
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawLine(p2.x, p2.y, branchX, branchY);
                g2d.setStroke(new BasicStroke(2.0f));
            }
        }
        
        // Draw the base ricochet
        g2d.setColor(mainColor);
        g2d.fillOval(x, y, size, size);
        
        // Draw glowing center
        g2d.setColor(coreColor);
        g2d.fillOval(centerX - size/4, centerY - size/4, size/2, size/2);
        
        // Reset stroke
        g2d.setStroke(new BasicStroke(1.0f));
    }
    
    private void drawFireStyle(Graphics2D g2d) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Draw particles first (background)
        for (Particle p : fireParticles) {
            p.draw(g2d);
        }
        
        // Draw glowing effect under the base
        Point2D center = new Point2D.Float(centerX, centerY);
        float radius = size;
        float[] dist = {0.0f, 0.7f, 1.0f};
        Color[] colors = {coreColor, mainColor, new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 0)};
        RadialGradientPaint paint = new RadialGradientPaint(center, radius, dist, colors, CycleMethod.NO_CYCLE);
        
        g2d.setPaint(paint);
        g2d.fillOval(centerX - size/2, centerY - size/2, size, size);
        
        // Draw the base ricochet
        g2d.setColor(mainColor);
        g2d.fillOval(x, y, size, size);
        
        // Draw glowing center
        g2d.setColor(coreColor);
        g2d.fillOval(centerX - size/4, centerY - size/4, size/2, size/2);
        
        // Create flickering effect
        if (random.nextDouble() < 0.3) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g2d.setColor(secondaryColor);
            g2d.fillOval(x - size/4, y - size/4, size + size/2, size + size/2);
            g2d.setComposite(AlphaComposite.SrcOver);
        }
    }
    
    private void drawWindStyle(Graphics2D g2d) {
        int centerX = x + size / 2;
        int centerY = y + size / 2;
        
        // Draw all wind trails
        for (WindTrail trail : windTrails) {
            trail.draw(g2d);
        }
        
        // Draw swirling effect around the base
        g2d.setColor(new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), 100));
double swirl1 = windAngle;
double swirl2 = windAngle + Math.PI * 2 / 3;
double swirl3 = windAngle + Math.PI * 4 / 3;

        
        // Draw spirals
        drawSpiral(g2d, centerX, centerY, swirl1);
        drawSpiral(g2d, centerX, centerY, swirl2);
        drawSpiral(g2d, centerX, centerY, swirl3);
        
        // Draw the base ricochet
        g2d.setColor(mainColor);
        g2d.fillOval(x, y, size, size);
        
        // Draw glowing center
        g2d.setColor(coreColor);
        g2d.fillOval(centerX - size/4, centerY - size/4, size/2, size/2);
    }
    
    private void drawSpiral(Graphics2D g2d, int centerX, int centerY, double startAngle) {
        g2d.setStroke(new BasicStroke(1.5f));
        
        double radius = size / 4;
        double angle = startAngle;
        int lastX = centerX + (int)(Math.cos(angle) * radius);
        int lastY = centerY + (int)(Math.sin(angle) * radius);
        
        for (int i = 0; i < 6; i++) {
            radius += 2;
            angle += 0.5;
            int newX = centerX + (int)(Math.cos(angle) * radius);
            int newY = centerY + (int)(Math.sin(angle) * radius);
            
            g2d.drawLine(lastX, lastY, newX, newY);
            
            lastX = newX;
            lastY = newY;
        }
        
        g2d.setStroke(new BasicStroke(1.0f));
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
    
    public int getDamage() {
        return damage;
    }
    
    public int getStyle() {
        return style;
    }
    
    // Method to check collision with player or enemy
    public boolean collidesWith(int otherX, int otherY, int otherSize) {
        int dx = (x + size/2) - (otherX + otherSize/2);
        int dy = (y + size/2) - (otherY + otherSize/2);
        int distance = (int) Math.sqrt(dx * dx + dy * dy);
        
        return distance < (size/2 + otherSize/2);
    }
}