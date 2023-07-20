package com.example.pos;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class printPic {

    public Canvas canvas = null;

    public Paint paint = null;

    public Bitmap bm = null;
    public int width;
    public float length = 0.0F;

    public byte[] bitbuf = null;

    private printPic() {
    }

    private static printPic instance = new printPic();

    public static printPic getInstance() {
        return instance;
    }

    public int getLength() {
        return (int) this.length + 20;
    }

    public void init(Bitmap bitmap, int paperWidth, int paperHeight) {
        if (null != bitmap) {
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            // Calculate the scaled dimensions while maintaining the aspect ratio
            float scale = Math.min((float) paperWidth / imageWidth, (float) paperHeight / imageHeight);
            int scaledWidth = (int) (imageWidth * scale);
            int scaledHeight = (int) (imageHeight * scale);

            // Create a scaled bitmap
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true);

            initCanvas(paperWidth, paperHeight);
            if (null == paint) {
                initPaint();
            }
            drawImage(0, 0, scaledBitmap);

            scaledBitmap.recycle(); // Recycle the scaled bitmap
        }
    }

    public void initCanvas(int paperWidth, int paperHeight) {
        this.bm = Bitmap.createBitmap(paperWidth, paperHeight, Bitmap.Config.RGB_565);
        this.canvas = new Canvas(this.bm);

        this.canvas.drawColor(-1);
        this.width = paperWidth;
        this.bitbuf = new byte[this.width / 8];
    }


    public void initPaint() {
        this.paint = new Paint();// 新建一个画笔

        this.paint.setAntiAlias(true);//

        this.paint.setColor(-16777216);

        this.paint.setStyle(Paint.Style.STROKE);
    }

    /**
     * draw bitmap
     */
    public void drawImage(float x, float y, Bitmap btm) {
        try {
            // Bitmap btm = BitmapFactory.decodeFile(path);
            this.canvas.drawBitmap(btm, x, y, null);
            if (this.length < y + btm.getHeight())
                this.length = (y + btm.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != btm) {
                btm.recycle();
            }
        }
    }

    /**
     * 使用光栅位图打印
     *
     * @return 字节
     */
    public byte[] printDraw() {
        Bitmap nbm = Bitmap.createBitmap(this.bm, 0, 0, this.width, this.bm.getHeight());

        int scaledLength = (int) (nbm.getHeight() * (float) this.width / nbm.getWidth());

        byte[] imgbuf = new byte[this.width / 8 * scaledLength + 8];

        int s = 0;

        // Print raster bitmap command
        imgbuf[0] = 29; // 0x1D
        imgbuf[1] = 118; // 0x76
        imgbuf[2] = 48; // 0x30
        imgbuf[3] = 0; // Bitmap mode 0,1,2,3
        imgbuf[4] = (byte) (this.width / 8); // Horizontal direction of bitmap bytes (xL + xH × 256)
        imgbuf[5] = 0;
        imgbuf[6] = (byte) (scaledLength % 256); // Vertical direction of bitmap dots (yL + yH × 256)
        imgbuf[7] = (byte) (scaledLength / 256);

        s = 7;
        for (int i = 0; i < scaledLength; i++) { // Loop through the height of the bitmap
            for (int k = 0; k < this.width / 8; k++) { // Loop through the width of the bitmap
                int c0 = nbm.getPixel(k * 8 + 0, (int) (i * (float) nbm.getHeight() / scaledLength)); // Get the color at the specified coordinates
                int p0 = c0 == -1 ? 0 : 1; // Determine if the color is white or not (0 - not print, 1 - print)

                int c1 = nbm.getPixel(k * 8 + 1, (int) (i * (float) nbm.getHeight() / scaledLength));
                int p1 = c1 == -1 ? 0 : 1;

                int c2 = nbm.getPixel(k * 8 + 2, (int) (i * (float) nbm.getHeight() / scaledLength));
                int p2 = c2 == -1 ? 0 : 1;

                int c3 = nbm.getPixel(k * 8 + 3, (int) (i * (float) nbm.getHeight() / scaledLength));
                int p3 = c3 == -1 ? 0 : 1;

                int c4 = nbm.getPixel(k * 8 + 4, (int) (i * (float) nbm.getHeight() / scaledLength));
                int p4 = c4 == -1 ? 0 : 1;

                int c5 = nbm.getPixel(k * 8 + 5, (int) (i * (float) nbm.getHeight() / scaledLength));
                int p5 = c5 == -1 ? 0 : 1;

                int c6 = nbm.getPixel(k * 8 + 6, (int) (i * (float) nbm.getHeight() / scaledLength));
                int p6 = c6 == -1 ? 0 : 1;

                int c7 = nbm.getPixel(k * 8 + 7, (int) (i * (float) nbm.getHeight() / scaledLength));
                int p7 = c7 == -1 ? 0 : 1;

                int value = p0 * 128 + p1 * 64 + p2 * 32 + p3 * 16 + p4 * 8 + p5 * 4 + p6 * 2 + p7;
                this.bitbuf[k] = (byte) value;
            }

            for (int t = 0; t < this.width / 8; t++) {
                s++;
                imgbuf[s] = this.bitbuf[t];
            }
        }

        if (null != this.bm) {
            this.bm.recycle();
            this.bm = null;
        }

        return imgbuf;
    }

}
