package com.codeko.apps.maimonides.digitalizacion;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class UtilImgParte {

    static final int MARGEN_BLANCO_RGB = 200;

    public static int[][] arrayToMatriz(int[] array, int ancho) {
        int[][] res = new int[array.length / ancho][ancho];
        for (int i = 0; i < res.length; i++) {
            for (int x = 0; x < ancho; x++) {
                res[i][x] = array[(i * ancho) + x];
            }
        }
        return res;
    }

    public static int[] matrizToArray(int[][] matriz) {
        int[] array = new int[matriz.length * matriz[0].length];
        for (int i = 0; i < matriz.length; i++) {
            for (int z = 0; z < matriz[0].length; z++) {
                array[(i * matriz[0].length) + z] = matriz[i][z];
            }
        }
        return array;
    }

    public static int[][] extraerCuadradoDeMatriz(int[][] origen, int x, int y, int tam) {
        return extraerRectanguloDeMatriz(origen, x, y, tam, tam);
    }

    public static int[][] extraerRectanguloDeMatriz(int[][] origen, int x, int y, int ancho, int alto) {
        int[][] cuadrado = new int[alto][ancho];
        for (int i = 0; i < alto; i++) {
            for (int z = 0; z < ancho; z++) {
                cuadrado[i][z] = origen[y + i][x + z];
            }
        }
        return cuadrado;
    }

    static boolean esBlanco(int pixel) {
        // int alpha = (pixel >> 24) & 0xff;
        int rojo = (pixel >> 16) & 0xff;
        int verde = (pixel >> 8) & 0xff;
        int azul = (pixel) & 0xff;
        return rojo > MARGEN_BLANCO_RGB && verde > MARGEN_BLANCO_RGB && azul > MARGEN_BLANCO_RGB;
    }

    public static BufferedImage rotar(Image img, double anguloRadianes) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        int type = BufferedImage.TYPE_INT_RGB; // other options, see api
        BufferedImage image = new BufferedImage(w, h, type);
        Graphics2D g2 = image.createGraphics();
        AffineTransform at = AffineTransform.getRotateInstance(anguloRadianes);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        g2.drawImage(img, at, null);
        g2.dispose();
        img = null;
        return (image);
    }
}
