import java.awt.Image;
import java.util.List;
import javax.swing.*;

public class Level {
    private final int levelNumber;
    private final List<Enemy> enemies;
    private final Image backgroundImage;
    public Level(int levelNumber, List<Enemy> enemies, String backgroundImagePath) {
        this.levelNumber = levelNumber;
        this.enemies = enemies;
        this.backgroundImage = new ImageIcon(backgroundImagePath).getImage();
    }
    public int getLevelNumber() {
        return levelNumber;
    }
    public List<Enemy> getEnemies() {
        return enemies;
    }
    public Image getBackgroundImage() {
        return backgroundImage;
    }
}