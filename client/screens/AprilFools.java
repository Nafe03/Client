package dev.anarchy.waifuhax.client.screens;

import dev.anarchy.waifuhax.api.WHLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AprilFools extends Screen {

    private final List<Identifier> texturelist = new ArrayList<>();

    private int counter = 0;
    private boolean dontdraw = true;

    public AprilFools() {
        super(Text.of("APRIL FOOL !!!!!"));
    }

    // https://stackoverflow.com/a/8299175
    private static AudioInputStream convertToPCM(AudioInputStream audioInputStream) {
        AudioFormat m_format = audioInputStream.getFormat();

        if ((m_format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) &&
                (m_format.getEncoding() != AudioFormat.Encoding.PCM_UNSIGNED)) {
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    m_format.getSampleRate(), 16,
                    m_format.getChannels(), m_format.getChannels() * 2,
                    m_format.getSampleRate(), m_format.isBigEndian());
            audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
        }

        return audioInputStream;
    }

    @Override
    protected void init() {
        // apparently, anything below return is not an unused statement
        if (true) {return;}
        for (int i = 0; i != 5301; ++i) {
            texturelist.add(Identifier.of("calamity:out/" + i + ".png"));
        }
        MinecraftClient.getInstance().options.getMaxFps().setValue(25);
        try {
            InputStream audioSrc = AprilFools.class.getResourceAsStream("/assets/waifuhax/bday.wav");
            if (audioSrc == null) {
                WHLogger.print("SHIT IS FUCKED, ABORT !!!!");
            }
            else {
                InputStream buffer = new BufferedInputStream(audioSrc);
                AudioInputStream audioInputStream = convertToPCM(AudioSystem.getAudioInputStream(buffer));

                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);

                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (UnsupportedAudioFileException | IOException |
                 LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        dontdraw = false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (dontdraw) {return;}

        if (counter >= 5301) {
            counter = 0;
        }/*
        context.drawTexture(texturelist.get(++counter),
                0,
                0,
                MinecraftClient.getInstance().getWindow().getWidth(),
                MinecraftClient.getInstance().getWindow().getHeight(),
                MinecraftClient.getInstance().getWindow().getWidth(),
                MinecraftClient.getInstance().getWindow().getHeight());
*/
        context.drawText(MinecraftClient.getInstance().textRenderer, Text.of("Frame: " + counter), 0, 0, 0xFFFFFF, true);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public void blur() {
    }
}
