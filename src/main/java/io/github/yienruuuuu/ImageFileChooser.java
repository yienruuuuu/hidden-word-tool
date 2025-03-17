package io.github.yienruuuuu;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * @author Eric.Lee
 * Date: 2025/3/17
 */
public class ImageFileChooser {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ImageFileChooser::showFileChooser);
    }

    private static void showFileChooser() {
        // 創建 JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        // 設定只允許選擇圖片
        fileChooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }
                String name = f.getName().toLowerCase();
                return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".gif");
            }

            @Override
            public String getDescription() {
                return "圖片文件 (*.jpg, *.jpeg, *.png, *.bmp, *.gif)";
            }
        });

        // 加入圖片預覽面板
        ImagePreviewPanel preview = new ImagePreviewPanel();
        fileChooser.setAccessory(preview);
        fileChooser.addPropertyChangeListener(evt -> {
            if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(evt.getPropertyName())) {
                File selectedFile = (File) evt.getNewValue();
                preview.loadImage(selectedFile);
            }
        });

        // 顯示對話框
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("選擇的圖片: " + selectedFile.getAbsolutePath());
        }
    }
}

// 圖片預覽面板
class ImagePreviewPanel extends JPanel {
    private JLabel imageLabel;
    private int previewWidth = 150;  // 預覽圖寬度
    private int previewHeight = 150; // 預覽圖高度

    public ImagePreviewPanel() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(previewWidth, previewHeight));
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        add(imageLabel, BorderLayout.CENTER);
        setBorder(BorderFactory.createTitledBorder("圖片預覽"));
    }

    public void loadImage(File file) {
        if (file == null || !file.exists()) {
            imageLabel.setIcon(null);
            return;
        }
        try {
            BufferedImage img = ImageIO.read(file);
            if (img != null) {
                // 產生縮圖
                Image scaledImg = img.getScaledInstance(previewWidth, previewHeight, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                imageLabel.setIcon(null);
            }
        } catch (IOException e) {
            imageLabel.setIcon(null);
        }
    }
}
