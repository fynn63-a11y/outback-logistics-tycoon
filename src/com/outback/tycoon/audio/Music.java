package com.outback.tycoon.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class Music {
    private static final int SAMPLE_RATE = 22_050;
    private static final double TEMPO = 0.24;

    private Thread thread;
    private volatile boolean running = false;
    private volatile boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled && !running) start();
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        thread = new Thread(this::loop, "outback-music");
        thread.setDaemon(true);
        thread.start();
    }

    private void loop() {
        AudioFormat fmt = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
            line.open(fmt);
            line.start();
            while (running) {
                if (!enabled) {
                    sleep(200);
                    continue;
                }
                byte[] bar = renderLoop();
                line.write(bar, 0, bar.length);
            }
        } catch (Exception ignored) {
            running = false;
        }
    }

    private byte[] renderLoop() {
        int[][] chords = {
                {57, 60, 64}, {53, 57, 60}, {48, 52, 55}, {55, 59, 62}
        };
        int[] melody = {72, 0, 76, 74, 0, 72, 69, 0, 67, 0, 71, 72, 0, 74, 76, 0};
        int stepsPerChord = 4;
        int totalSteps = chords.length * stepsPerChord;
        int stepSamples = (int) (SAMPLE_RATE * TEMPO);
        byte[] out = new byte[totalSteps * stepSamples];
        for (int step = 0; step < totalSteps; step++) {
            int[] chord = chords[step / stepsPerChord];
            int note = melody[step % melody.length];
            double fMelody = note > 0 ? 440.0 * Math.pow(2, (note - 69) / 12.0) : 0;
            for (int i = 0; i < stepSamples; i++) {
                double t = i / (double) SAMPLE_RATE;
                double env = Math.min(1.0, i / (double) (stepSamples * 0.3));
                double sample = 0;
                for (int n : chord) {
                    double f = 440.0 * Math.pow(2, (n - 69) / 12.0);
                    sample += 0.06 * env * Math.sin(2 * Math.PI * f * t);
                }
                if (fMelody > 0) sample += 0.10 * env * Math.sin(2 * Math.PI * fMelody * t)
                        + 0.03 * env * Math.sin(4 * Math.PI * fMelody * t);
                out[step * stepSamples + i] = (byte) (128 + sample * 100);
            }
        }
        return out;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
