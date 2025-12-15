import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MusicPlayer {
    private final Clip[] clips = new Clip[2];
    private int currentIndex = 0;
    private final FloatControl[] volumeControls = new FloatControl[2];
    private boolean isMuted = false;
    private float currentVolume = 0.75f; // Default volume

    public void loadMusic(String filePath1, String filePath2) {
        try {
            clips[0] = AudioSystem.getClip();
            clips[1] = AudioSystem.getClip();

            AudioInputStream audioStream1 = AudioSystem.getAudioInputStream(new File(filePath1));
            AudioInputStream audioStream2 = AudioSystem.getAudioInputStream(new File(filePath2));

            clips[0].open(audioStream1);
            clips[1].open(audioStream2);

            volumeControls[0] = (FloatControl) clips[0].getControl(FloatControl.Type.MASTER_GAIN);
            volumeControls[1] = (FloatControl) clips[1].getControl(FloatControl.Type.MASTER_GAIN);

            setVolume(currentVolume);

            // Set listeners for when each clip finishes
            clips[0].addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP && !isMuted) {
                    playNext(1); // After clip[0], play clip[1]
                }
            });

            clips[1].addLineListener(e -> {
                if (e.getType() == LineEvent.Type.STOP && !isMuted) {
                    playNext(0); // After clip[1], play clip[0]
                }
            });

        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace(System.err);
        }
    }

    public void play() {
        if (!isMuted && clips[currentIndex] != null) {
            clips[currentIndex].setFramePosition(0);
            clips[currentIndex].start();
        }
    }

    private void playNext(int nextIndex) {
        if (!isMuted) {
            clips[currentIndex].stop();
            currentIndex = nextIndex;
            clips[currentIndex].setFramePosition(0);
            clips[currentIndex].start();
        }
    }

    public void stop() {
        if (clips[currentIndex] != null && clips[currentIndex].isRunning()) {
            clips[currentIndex].stop();
        }
    }

    public void setVolume(float volume) {
        currentVolume = volume;
        for (FloatControl control : volumeControls) {
            if (control != null) {
                float range = control.getMaximum() - control.getMinimum();
                float gain = (range * volume) + control.getMinimum();
                control.setValue(gain);
            }
        }
    }

    public void mute() {
        isMuted = true;
        stop();
    }

    public void unmute() {
        isMuted = false;
        play();
    }

    public boolean isMuted() {
        return isMuted;
    }

    public float getVolume() {
        return currentVolume;
    }
}
