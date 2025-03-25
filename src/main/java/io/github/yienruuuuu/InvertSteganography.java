package io.github.yienruuuuu;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author Eric.Lee
 * Date: 2025/3/18
 */
public class InvertSteganography {

    public static void main(String[] args) {
        try {
            // 1. 讀取原始圖片
            File inputFile = new File("./pic/in/1.png");
            BufferedImage originalImg = ImageIO.read(inputFile);
            int width = originalImg.getWidth();
            int height = originalImg.getHeight();

            // 2. 設定搜尋參數
            int regionWidth = 200;      // 欲放置文字的區域寬
            int regionHeight = 50;      // 欲放置文字的區域高
            int whiteThreshold = 220;   // 當像素平均值大於此，視為「接近白色」
            int minWhiteArea = 5000;    // 判斷白色區域足以放文字的最低像素數量

            // 3. 建立「是否為白色像素」的二維陣列：值為 1 表示該像素算白，0 表示不算
            int[][] whiteMap = new int[width][height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = new Color(originalImg.getRGB(x, y));
                    int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
                    whiteMap[x][y] = (avg > whiteThreshold) ? 1 : 0;
                }
            }

            // 4. 建立積分圖 (Integral Image)，以快速計算任意區域的白像素總和
            //    為了方便計算邊界，積分圖建議用 (width+1) x (height+1) 大小，從 (1,1) 開始存實際值
            int[][] integral = new int[width + 1][height + 1];
            for (int y = 1; y <= height; y++) {
                for (int x = 1; x <= width; x++) {
                    integral[x][y] = whiteMap[x - 1][y - 1]
                            + integral[x - 1][y]
                            + integral[x][y - 1]
                            - integral[x - 1][y - 1];
                }
            }

            // 5. 使用積分圖，找出白像素總數最大的區域
            int bestX = 0, bestY = 0;
            int maxWhiteArea = 0;

            // 全面掃描 (x, y)；也可以改成多層 for 迴圈來加速，但這裡示範完整搜尋
            for (int y = 0; y <= height - regionHeight; y++) {
                for (int x = 0; x <= width - regionWidth; x++) {
                    // 計算 [x, y] ~ [x+regionWidth-1, y+regionHeight-1] 的白像素總數
                    int x2 = x + regionWidth;
                    int y2 = y + regionHeight;
                    // 透過積分圖做快速計算
                    int whiteSum = integral[x2][y2]
                            - integral[x][y2]
                            - integral[x2][y]
                            + integral[x][y];

                    if (whiteSum > maxWhiteArea) {
                        maxWhiteArea = whiteSum;
                        bestX = x;
                        bestY = y;
                    }
                }
            }

            // 6. 如果找到的最大白像素數量足夠，則嘗試在該區域繪製文字
            BufferedImage hiddenImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            // 先複製原圖到新的 BufferedImage，保留原圖內容
            Graphics2D gHidden = hiddenImg.createGraphics();
            gHidden.drawImage(originalImg, 0, 0, null);

            if (maxWhiteArea > minWhiteArea) {
                System.out.println("✅ 找到偏白區塊，將訊息放置於: (" + bestX + ", " + bestY + ")，白色像素數：" + maxWhiteArea);

                String message = "我是隱藏訊息";

                // (a) 先嘗試一個最大字型大小
                int maxFontSize = 120;
                int minFontSize = 10; // 可以自訂最小字型
                int fontSize = maxFontSize;

                // 建立暫時用來量測文字大小的 Graphics2D（不直接在 hiddenImg 上操作）
                BufferedImage tmp = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gTmp = tmp.createGraphics();

                // (b) 不斷嘗試從 maxFontSize 往下減，直到文字寬高可放入 regionWidth x regionHeight
                while (fontSize >= minFontSize) {
                    Font font = new Font("Arial", Font.BOLD, fontSize);
                    FontMetrics fm = gTmp.getFontMetrics(font);
                    int textWidth = fm.stringWidth(message);
                    int textHeight = fm.getHeight(); // 包含上升(ascent)與下降(descent)

                    // 若寬高都能放進指定區域，就可以停止了
                    if (textWidth <= regionWidth && textHeight <= regionHeight) {
                        break;
                    }
                    fontSize--;
                }
                gTmp.dispose(); // 不再需要暫時 Graphics

                if (fontSize < minFontSize) {
                    System.out.println("⚠️ 找不到適合的字型大小，無法繪製文字。");
                    return;
                } else {
                    // (c) 使用找到的字型大小，在 hiddenImg 上實際繪製文字
                    Font finalFont = new Font("LXGW WenKai TC Bold", Font.BOLD, fontSize);
                    gHidden.setFont(finalFont);

                    // 取得字體量測，用來計算垂直置中
                    FontMetrics fm = gHidden.getFontMetrics(finalFont);
                    int textWidth = fm.stringWidth(message);
                    int textHeight = fm.getHeight();

                    // 計算置中位置 (讓文字置中到該區域)
                    int drawX = bestX + (regionWidth - textWidth) / 2;
                    // 注意字元繪製時，Y 座標其實是 baseline
                    int drawY = bestY + (regionHeight - textHeight) / 2 + fm.getAscent();

                    // (d) 設定透明度
                    float alphaValue = 0.02f; // 5% (你也可改成 0.04f = 4%)
                    gHidden.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaValue));
                    gHidden.setColor(Color.BLACK);
                    gHidden.drawString(message, drawX, drawY);

                    System.out.println("✅ 文字已繪製於指定區域，實際字型大小：" + fontSize);
                }
            } else {
                System.out.println("⚠️ 沒有足夠大的白色區塊來放置隱藏訊息！");
            }

            gHidden.dispose();

            // 7. 儲存帶有隱藏訊息的圖片 (ARGB -> PNG)
            File hiddenFile = new File("./pic/out/hidden_message.png");
            ImageIO.write(hiddenImg, "png", hiddenFile);
            System.out.println("✅ 隱碼圖片已生成：hidden_message.png");

            // 8. 產生負片版本（示範：以含隱藏文字的版本來做負片）
            BufferedImage negativeImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    Color color = new Color(hiddenImg.getRGB(x, y), true);
                    // 取反 (負片)
                    int r = 255 - color.getRed();
                    int g = 255 - color.getGreen();
                    int b = 255 - color.getBlue();
                    Color negColor = new Color(r, g, b);
                    negativeImg.setRGB(x, y, negColor.getRGB());
                }
            }

            File negativeFile = new File("./pic/out/negative_message.png");
            ImageIO.write(negativeImg, "png", negativeFile);
            System.out.println("✅ 負片圖片已生成：negative_message.png");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}