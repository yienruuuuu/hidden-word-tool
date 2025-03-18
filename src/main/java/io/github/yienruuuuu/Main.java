package io.github.yienruuuuu;

import java.awt.*;

/**
 * @author Eric.Lee
 * Date: 2025/3/17
 */
public class Main {
    public static void main(String[] args) {
            GraphicsEnvironment gEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Font fonts[] = gEnv.getAllFonts();
            for(Font font : fonts)System.out.println(font);
        }
}