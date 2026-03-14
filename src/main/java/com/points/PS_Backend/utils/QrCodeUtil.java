package com.points.PS_Backend.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class QrCodeUtil {

    public static byte[] generateQRCode(String text) {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();

        try {

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    text,
                    BarcodeFormat.QR_CODE,
                    300,
                    300
            );

            BufferedImage image = new BufferedImage(
                    300,
                    300,
                    BufferedImage.TYPE_INT_RGB
            );

            for (int x = 0; x < 300; x++) {
                for (int y = 0; y < 300; y++) {

                    image.setRGB(
                            x,
                            y,
                            bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF
                    );
                }
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();

            ImageIO.write(image, "PNG", output);

            return output.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("QR generation failed");
        }
    }
}