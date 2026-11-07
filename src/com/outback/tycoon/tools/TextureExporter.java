package com.outback.tycoon.tools;

import com.outback.tycoon.ui.Textures;

import javax.imageio.ImageIO;
import java.io.File;

public class TextureExporter {
    public static void main(String[] args) throws Exception {
        File dir = new File("assets/textures");
        dir.mkdirs();
        ImageIO.write(Textures.land(), "png", new File(dir, "land.png"));
        ImageIO.write(Textures.water(), "png", new File(dir, "water.png"));
        ImageIO.write(Textures.floorA(), "png", new File(dir, "floor_a.png"));
        ImageIO.write(Textures.floorB(), "png", new File(dir, "floor_b.png"));
        ImageIO.write(Textures.metal(), "png", new File(dir, "metal.png"));
        System.out.println("5 Texturen als PNG exportiert nach assets/textures/");
    }
}
