package com.outback.tycoon.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public class Sfx {
    private static final int RATE = 22_050;
    private static volatile boolean enabled = true;

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void tone(double freq, int ms, double vol) {
        if (!enabled) return;
        Thread th = new Thread(() -> {
            try {
                int n = RATE * ms / 1000;
                byte[] buf = new byte[n];
                for (int i = 0; i < n; i++) {
                    double tt = i / (double) RATE;
                    double env = Math.min(1.0, (n - i) / (double) (n * 0.3));
                    buf[i] = (byte) (128 + vol * env * 100 * Math.sin(2 * Math.PI * freq * tt));
                }
                AudioFormat fmt = new AudioFormat(RATE, 8, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(fmt)) {
                    line.open(fmt);
                    line.start();
                    line.write(buf, 0, n);
                    line.drain();
                }
            } catch (Exception ignored) {
            }
        }, "sfx");
        th.setDaemon(true);
        th.start();
    }

    public static void alert() {
        tone(660, 110, 0.12);
        tone(495, 140, 0.10);
    }

    public static void coin() {
        tone(880, 70, 0.12);
        tone(1320, 120, 0.10);
    }

    public static void crash() {
        tone(220, 250, 0.18);
        tone(160, 350, 0.15);
    }
}
