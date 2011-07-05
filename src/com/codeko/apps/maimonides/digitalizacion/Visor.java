package com.codeko.apps.maimonides.digitalizacion;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.GUI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.MemoryImageSource;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import com.codeko.util.Img;
import com.codeko.util.Num;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

public class Visor {

    public static JFrame mostrarImagen(int ancho, int[] pixeles) {
        MemoryImageSource mis = new MemoryImageSource(ancho, pixeles.length / ancho, pixeles, 0, ancho);
        return mostrarImagen(java.awt.Toolkit.getDefaultToolkit().createImage(mis));
    }

    public static JFrame mostrarImagen(Image imagen) {
        //img.getGraphics().drawRect(0, 0, img.getWidth(null), img.getHeight(null));
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        BufferedImage tmpImg = null;
        if (imagen instanceof BufferedImage) {
            tmpImg = (BufferedImage) imagen;
        } else {
            tmpImg = gc.createCompatibleImage(imagen.getWidth(null), imagen.getHeight(null), Transparency.OPAQUE);
            Graphics g = tmpImg.createGraphics();
            // Paint the image onto the buffered image
            g.drawImage(imagen, 0, 0, null);
            g.setColor(Color.red);
            g.drawRect(0, 0, tmpImg.getWidth(null), tmpImg.getHeight(null));
            g.dispose();
        }
        final BufferedImage bimage = tmpImg;

        JFrame m = new JFrame();
        JScrollPane scroll = new JScrollPane();
        m.add(scroll, BorderLayout.CENTER);
        JLabel l = new JLabel();
        l.setIcon(new ImageIcon(bimage));
        final JLabel lStatus = new JLabel();
        m.add(lStatus, BorderLayout.SOUTH);

        scroll.getViewport().add(l);
        JButton b = new JButton("Imprimir");
        m.add(b, BorderLayout.NORTH);
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPrintable(new Printable() {

                    @Override
                    public int print(Graphics graphics, PageFormat pageFormat,
                            int pageIndex) throws PrinterException {
                        if (pageIndex > 0) {
                            return NO_SUCH_PAGE;
                        }
                        Graphics2D g2d = (Graphics2D) graphics;
                        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                        try {
                            g2d.drawImage(Img.getImagenEscalada(bimage,
                                    new Dimension((int) pageFormat.getImageableWidth(),
                                    (int) pageFormat.getImageableHeight())), 0,
                                    0, null);

                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                        return PAGE_EXISTS;
                    }
                });
                boolean ok = job.printDialog();
                if (ok) {
                    try {
                        job.print();
                    } catch (PrinterException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        m.setSize(800, 600);
        if (MaimonidesApp.getApplication() != null) {
            m.setIconImage(MaimonidesApp.getApplication().getMainFrame().getIconImage());
            GUI.centrar(MaimonidesApp.getApplication().getMainFrame(),m);
        }
        m.setVisible(true);
        return m;
    }

    public static JFrame mostrarImagen(final Image img, final double proporcion,
            final Point marcaIzquierda) {
        JFrame m = new JFrame();
        JScrollPane scroll = new JScrollPane();
        m.add(scroll, BorderLayout.CENTER);
        JLabel l = new JLabel();
        l.setIcon(new ImageIcon(img));
        final JLabel lStatus = new JLabel();
        m.add(lStatus, BorderLayout.SOUTH);
        l.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                lStatus.setText("R: " + e.getX() + " : " + e.getY() + "       P: " + (int) Num.round(e.getX() / proporcion, 0) + " : " + (int) Num.round(e.getY() / proporcion, 0) + "       PM: " + (int) Num.round((e.getX() - marcaIzquierda.getX()) / proporcion, 0) + " : " + (int) Num.round((e.getY() - marcaIzquierda.getY()) / proporcion, 0));

            }
        });
        scroll.getViewport().add(l);
        JButton b = new JButton("Imprimir");
        m.add(b, BorderLayout.NORTH);
        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                PrinterJob job = PrinterJob.getPrinterJob();
                job.setPrintable(new Printable() {

                    @Override
                    public int print(Graphics graphics, PageFormat pageFormat,
                            int pageIndex) throws PrinterException {
                        if (pageIndex > 0) {
                            return NO_SUCH_PAGE;
                        }
                        Graphics2D g2d = (Graphics2D) graphics;
                        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                        try {
                            g2d.drawImage(Img.getImagenEscalada(img,
                                    new Dimension((int) pageFormat.getImageableWidth(),
                                    (int) pageFormat.getImageableHeight())), 0,
                                    0, null);

                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                        return PAGE_EXISTS;
                    }
                });
                boolean ok = job.printDialog();
                if (ok) {
                    try {
                        job.print();
                    } catch (PrinterException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        m.setBounds(50, 50, 800, 300);
        m.validate();
        m.setVisible(true);
        return m;
    }
}
