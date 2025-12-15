import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundUtility {
    private final static Clip[] soundEffects = new Clip[10]; // Array to store sound effects
    private static Clip backgroundMusic; // Clip for background music
    private final static float[] soundVolumes = new float[10]; // Store volume levels for each sound effect

public static void loadSE(int index, String filePath) {
    try {
        File soundFile = new File(filePath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
        soundEffects[index] = AudioSystem.getClip();
        soundEffects[index].open(audioInputStream);
        soundVolumes[index] = 1.0f; // Default full volume
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
        System.out.println("Error loading sound: " + filePath);
        e.printStackTrace(System.err);
    }
}

    public static void playSE(int index) {
        if (soundEffects[index] != null) {
            soundEffects[index].stop(); // Stop the sound if it's already playing
            soundEffects[index].setFramePosition(0); // Rewind to the beginning
            
            // Apply the current volume setting
            setSoundVolume(index, soundVolumes[index]);
            
            soundEffects[index].start(); // Play the sound
        }
    }

    public static void setSoundVolume(int index, float volume) {
        if (soundEffects[index] != null) {
            try {
                // Store the volume setting
                soundVolumes[index] = volume;
                
                // Apply volume if clip has volume control
                if (soundEffects[index].isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                    FloatControl volumeControl = (FloatControl) soundEffects[index].getControl(FloatControl.Type.MASTER_GAIN);
                    
                    // Convert volume (0.0 to 1.0) to gain (-80.0 to 6.0 dB)
                    // Logarithmic scale works better for human perception of volume
                    
                    float gain;
                    
                    if (volume > 0.0f) {
                        gain = (float) (20.0 * Math.log10(volume));
                    } else {
                        gain = volumeControl.getMinimum();
                    }
                    
                    // Ensure gain is within allowed range
                    gain = Math.max(volumeControl.getMinimum(), Math.min(volumeControl.getMaximum(), gain));
                    
                    volumeControl.setValue(gain);
                }
            } catch (Exception e) {
                System.out.println("Error setting sound volume for index: " + index);
                e.printStackTrace(System.err);
            }
        }
    }

    public static float getSoundVolume(int index) {
        return soundVolumes[index];
    }

    public static void playBackgroundMusic(String filePath) {
        try {
            File musicFile = new File(filePath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(musicFile);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInputStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Loop the background music
            backgroundMusic.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace(System.err);
        }
    }
    

    public static void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }

    public static void setBackgroundMusicVolume(float volume) {
        if (backgroundMusic != null) {
            FloatControl volumeControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            float range = volumeControl.getMaximum() - volumeControl.getMinimum();
            float gain = (range * volume) + volumeControl.getMinimum();
            volumeControl.setValue(gain);
        }
    }

    public static void setBackgroundMusicMuted(boolean muted) {
        if (backgroundMusic != null) {
            if (muted) {
                backgroundMusic.stop();
            } else {
                backgroundMusic.start();
            }
        }
    }
}