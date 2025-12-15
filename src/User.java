// User.java
import java.io.Serializable;

public class User implements Serializable {
    // This is important for serialization version control
    private static final long serialVersionUID = 1L;
    
    private String username;
    private int score;

    public User(String username, int score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", score=" + score +
                '}';
    }
}