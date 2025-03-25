package io.github.yienruuuuu;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Eric.Lee
 * Date: 2025/3/25
 */
public class LsbTool {
    public static BufferedImage hideTextInImageLSB(BufferedImage image, String secretMessage) {
        BufferedImage stegoImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = stegoImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        byte[] msgBytes = secretMessage.getBytes(StandardCharsets.UTF_8);
        int msgLength = msgBytes.length;
        int totalBits = (msgLength + 4) * 8; // 加 4 bytes 用來記錄長度
        int width = image.getWidth();
        int height = image.getHeight();
        int imgCapacity = width * height * 3; // 每個像素可藏 3 bits

        if (totalBits > imgCapacity) {
            throw new IllegalArgumentException("圖片容量不足以藏這段訊息");
        }

        // 將長度 + 訊息合併為位元列
        ByteBuffer buffer = ByteBuffer.allocate(msgBytes.length + 4);
        buffer.putInt(msgBytes.length);
        buffer.put(msgBytes);
        byte[] fullData = buffer.array();

        int bitIndex = 0;
        outerLoop:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = stegoImage.getRGB(x, y);
                Color color = new Color(rgb, true);

                int r = color.getRed();
                int g1 = color.getGreen();
                int b = color.getBlue();
                int a = color.getAlpha();

                // 修改 RGB 的最低位元（LSB）
                if (bitIndex < totalBits) r = (r & 0xFE) | getBit(fullData, bitIndex++);
                if (bitIndex < totalBits) g1 = (g1 & 0xFE) | getBit(fullData, bitIndex++);
                if (bitIndex < totalBits) b = (b & 0xFE) | getBit(fullData, bitIndex++);

                Color newColor = new Color(r, g1, b, a);
                stegoImage.setRGB(x, y, newColor.getRGB());

                if (bitIndex >= totalBits) break outerLoop;
            }
        }

        return stegoImage;
    }

    private static int getBit(byte[] data, int bitIndex) {
        int byteIndex = bitIndex / 8;
        int bitOffset = 7 - (bitIndex % 8);
        return (data[byteIndex] >> bitOffset) & 1;
    }

    public static String extractTextFromImageLSB(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int totalBitsToRead = -1;
        int bitIndex = 0;
        int currentByte = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb, true);

                int[] channels = {color.getRed(), color.getGreen(), color.getBlue()};

                for (int i = 0; i < 3; i++) {
                    int lsb = channels[i] & 1;
                    currentByte = (currentByte << 1) | lsb;
                    bitIndex++;

                    // 每讀滿 8 bits 就寫入一個 byte
                    if (bitIndex % 8 == 0) {
                        buffer.write(currentByte);
                        currentByte = 0;

                        // 讀完前 4 個 byte（32 bit）後，解析出訊息長度
                        if (bitIndex == 32) {
                            byte[] lengthBytes = buffer.toByteArray();
                            int msgLength = ByteBuffer.wrap(lengthBytes).getInt();
                            totalBitsToRead = (msgLength + 4) * 8;
                        }

                        // 若已知總共要讀幾個 bits 且已達上限，就結束
                        if (totalBitsToRead > 0 && bitIndex >= totalBitsToRead) {
                            byte[] fullMsg = buffer.toByteArray();
                            byte[] msgOnly = Arrays.copyOfRange(fullMsg, 4, fullMsg.length); // 去掉長度
                            return new String(msgOnly, StandardCharsets.UTF_8);
                        }
                    }
                }
            }
        }

        return null;
    }


    public static void main(String[] args) {
        try {
//            BufferedImage originalImg = ImageIO.read(new File("./pic/in/1.png"));
//            BufferedImage withHidden = hideTextInImageLSB(originalImg, "我是隱藏訊息，苦力怕炸你家");
//            ImageIO.write(withHidden, "png", new File("./pic/out/hidden_inside.png"));

            BufferedImage imageWithHidden = ImageIO.read(new File("./pic/out/222/hidden_inside.png"));
            String decodedMessage = extractTextFromImageLSB(imageWithHidden);
            System.out.println("🔓 解碼結果: " + decodedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
