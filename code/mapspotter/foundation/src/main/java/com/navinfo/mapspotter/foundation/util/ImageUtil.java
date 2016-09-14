package com.navinfo.mapspotter.foundation.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by gaojian on 2016/3/17.
 */
public class ImageUtil {

    public static byte[] getPNG(RenderedImage image) {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream(image.getHeight() * image.getWidth())) {
            ImageIO.write(image, "png", bout);
            return bout.toByteArray();
        } catch (IOException e) {
            Logger.getLogger(ImageUtil.class).error(e);
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        for (int i = 10; i < 100; i++) {
            for (int j = 100; j < 200; j++) {
                image.setRGB(i, j, 0xFFFFFF00);
            }
        }
    }
}
