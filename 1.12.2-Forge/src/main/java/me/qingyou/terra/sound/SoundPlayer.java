package me.qingyou.terra.sound;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Objects;

public class SoundPlayer {
    public void playSound(SoundType st, float volume) {
        new Thread(() -> {
            AudioInputStream as;
            try {
                as = AudioSystem.getAudioInputStream(new BufferedInputStream(Objects.requireNonNull(this.getClass().getResourceAsStream("/assets/minecraft/terra/sound/" + st.getName()))));
                Clip clip = AudioSystem.getClip();
                clip.open(as);
                clip.start();
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume);
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public enum SoundType {
        //
        JIGOU("jigou.wav"),
        WOCAO("wocao.wav"),
        MOUSE("mouse.wav"),
        BEGIN("begin.wav");

        final String name;

        SoundType(String fileName) {
            this.name = fileName;
        }

        String getName() {
            return name;
        }
    }
}
