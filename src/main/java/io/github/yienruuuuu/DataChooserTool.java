package io.github.yienruuuuu;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DataChooserTool {
    private final JFrame frame;
    private File selectedFile;

    public DataChooserTool() {
        frame = new JFrame("Encrypt Tool");
        frame.setSize(500, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new FlowLayout());
        JTextField fileField = new JTextField(20);
        fileField.setEditable(false);
        JButton chooseButton = new JButton("選擇圖片");
        JButton encodeButton = new JButton("隱碼");
        JButton decodeButton = new JButton("解碼");

        chooseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnValue = fileChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                fileField.setText(selectedFile.getAbsolutePath());
            }
        });

        encodeButton.addActionListener(e -> {
            if (selectedFile != null) {
                String message = JOptionPane.showInputDialog("輸入要隱藏的訊息:");
                if (message != null) {
                    BufferedImage image = loadImage(selectedFile);
                    if (image != null) {
                        BufferedImage encodedImage = hideMessage(image, message);
                        saveImage(encodedImage, "encoded_image.png");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(frame, "請選擇圖片！");
            }
        });

        decodeButton.addActionListener(e -> {
            if (selectedFile != null) {
                BufferedImage image = loadImage(selectedFile);
                if (image != null) {
                    String message = revealMessage(image);
                    JOptionPane.showMessageDialog(frame, "解碼訊息: " + message);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "請選擇圖片！");
            }
        });

        panel.add(fileField);
        panel.add(chooseButton);
        panel.add(encodeButton);
        panel.add(decodeButton);
        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private BufferedImage loadImage(File file) {
        try {
            return ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "無法讀取圖片！");
            return null;
        }
    }

    private void saveImage(BufferedImage image, String fileName) {
        try {
            ImageIO.write(image, "png", new File(fileName));
            JOptionPane.showMessageDialog(frame, "圖片已儲存: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "無法儲存圖片！");
        }
    }

    private BufferedImage hideMessage(BufferedImage image, String message) {
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int messageIndex = 0;
        int messageLength = message.length();
        boolean messageCompleted = false;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                if (!messageCompleted && messageIndex < messageLength) {
                    char character = message.charAt(messageIndex);
                    int modifiedPixel = (pixel & 0xFFFFFFFE) | (character & 1);
                    newImage.setRGB(x, y, modifiedPixel);
                    messageIndex++;
                    if (messageIndex >= messageLength) {
                        messageCompleted = true;
                    }
                } else {
                    newImage.setRGB(x, y, pixel);
                }
            }
        }
        return newImage;
    }

    private String revealMessage(BufferedImage image) {
        StringBuilder message = new StringBuilder();
        int width = image.getWidth();
        int height = image.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                char character = (char) (pixel & 1);
                message.append(character);
            }
        }
        return message.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DataChooserTool::new);
    }
}