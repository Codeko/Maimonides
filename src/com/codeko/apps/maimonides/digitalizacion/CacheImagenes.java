/**
 *  Maimónides, gestión para centros escolares.
 *  Copyright Codeko and individual contributors
 *  as indicated by the @author tags.
 * 
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 * 
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *  
 *  For more information:
 *  maimonides@codeko.com
 *  http://codeko.com/maimonides
**/


package com.codeko.apps.maimonides.digitalizacion;

import com.codeko.apps.maimonides.elementos.ParteFaltas;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class CacheImagenes {

    private static final int MAX_IMAGENES = 10;
    static HashMap<String, BufferedImage> imagenes = new HashMap<String, BufferedImage>();
    static HashMap<String, ArrayList<ArrayList<Image>>> casillas = new HashMap<String, ArrayList<ArrayList<Image>>>();
    static HashMap<String, File> archivos = new HashMap<String, File>();
    static ArrayList<String> ids = new ArrayList<String>();

    public static BufferedImage getImagenParte(ParteFaltas parte, int pagina) {
        String id = parte.getId() + "-" + pagina;
        BufferedImage img = null;
        //Primero vemos si esta en las imágenes
        if (imagenes.containsKey(id)) {
            Logger.getLogger(CacheImagenes.class.getName()).log(Level.INFO, "Recuperando imagen {0} desde cach\u00e9 de im\u00e1genes.", id);
            img = imagenes.get(id);
        } else if (archivos.containsKey(id)) {
            try {
                Logger.getLogger(CacheImagenes.class.getName()).log(Level.INFO, "Recuperando imagen {0} desde cach\u00e9 de archivos.", id);
                img = ImageIO.read(archivos.get(id));
                addImagen(id, img);
            } catch (IOException ex) {
                Logger.getLogger(CacheImagenes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (img == null) {
            //Entonces tenemos que cargar la imagen del parte
            //TODO Implementar las páginas multiples
            Logger.getLogger(CacheImagenes.class.getName()).log(Level.INFO, "Cargando imagen {0} desde parte.", id);
            img = parte.getImagen_();
            if (img != null) {
                addImagen(id, img);
            }
        }
        return img;
    }

    public static Image getImagenCasilla(ParteFaltas parte, int pagina, int fila, int hora) {
        long t = System.currentTimeMillis();
        String id = parte.getId() + "-" + pagina;
        fila = fila - 1;
        hora = (hora - 1);//*2;
        Image img = null;
        long dif = 0;
        //Primero vemos si esta en las imágenes
        if (casillas.containsKey(id)) {
            Logger.getLogger(CacheImagenes.class.getName()).log(Level.INFO, "Recuperando casilla {0} desde cach\u00e9 de im\u00e1genes.", id);
            ArrayList<ArrayList<Image>> vCasillas = casillas.get(id);
            try {
                img = vCasillas.get(fila).get(hora);
            } catch (Exception e) {
                Logger.getLogger(CacheImagenes.class.getName()).log(Level.FINE, "No existe la fila solicitada", e);
            }
        } else {
            Logger.getLogger(CacheImagenes.class.getName()).log(Level.INFO, "Procesando casillas de imagen ''{0}''.", id);
            //Entonces tenemos que generar las casillas
            BufferedImage imgParte = getImagenParte(parte, pagina);
            dif = System.currentTimeMillis() - t;
            Logger.getLogger(CacheImagenes.class.getName()).log(Level.INFO, "Se ha tardado {0}ms en la carga de la imagen del parte.", dif);
            long t2 = System.currentTimeMillis();
            if (imgParte != null) {
                addImagenCasillas(parte, pagina, imgParte);
                ArrayList<ArrayList<Image>> vCasillas = casillas.get(id);
                img = vCasillas.get(fila).get(hora);
                dif = System.currentTimeMillis() - t2;
                Logger.getLogger(CacheImagenes.class.getName()).log(Level.INFO, "Se ha tardado {0}ms en procesar las casillas.", dif);
            }
        }
        dif = System.currentTimeMillis() - t;
        Logger.getLogger(CacheImagenes.class.getName()).log(Level.INFO, "Se ha tardado {0}ms en la carga.", dif);
        return img;
    }

    private static void addImagen(String id, final BufferedImage img) {
        if (!ids.contains(id)) {
            ids.add(id);
            //Si tenemos más imagenes de las cacheables, borramos la mas antigua
            if (ids.size() > MAX_IMAGENES) {
                String idBorrar = ids.get(0);
                borrarImagen(idBorrar, false);
            }
            //Y añadimos la imagen
            imagenes.put(id, img);
            //Vemos si existe el archivo
            if (!archivos.containsKey(id)) {
                try {
                    final File fImg = File.createTempFile("maimonides_parte_", "." + ConfiguracionParte.getConfiguracion().getExtensionImagenes());
                    archivos.put(id, fImg);
                    Thread t = new Thread() {

                        @Override
                        public void run() {
                            try {
                                ImageIO.write(img, ConfiguracionParte.getConfiguracion().getExtensionImagenes(), fImg);
                            } catch (IOException ex) {
                                Logger.getLogger(CacheImagenes.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    };
                    t.start();
                } catch (IOException ex) {
                    Logger.getLogger(CacheImagenes.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private static void borrarImagen(String idBorrar, boolean archivosTambien) {
        ids.remove(idBorrar);
        imagenes.remove(idBorrar);
        casillas.remove(idBorrar);
        if (archivosTambien) {
            archivos.remove(idBorrar);
        }
    }

    private static void addImagenCasillas(ParteFaltas parte, int pagina, BufferedImage imgParte) {
        String id = parte.getId() + "-" + pagina;
        addImagen(id, imgParte);
        DigitalizacionParte dig = new DigitalizacionParte(imgParte);
        dig.setIdParte(parte.getId());
        dig.setPagina(pagina);
        try {
            dig.prepararImagenExtraccionCasillas();
            ArrayList<ArrayList<Image>> vCasillas = dig.getCasillas(2);
            casillas.put(id, vCasillas);
        } catch (InterruptedException ex) {
            Logger.getLogger(CacheImagenes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void asignarImagen(ParteFaltas parte, int pagina, File archivo) {
        String id = parte.getId() + "-" + pagina;
        try {
            BufferedImage img = ImageIO.read(archivo);
            borrarImagen(id, true);
            addImagen(id, img);
        } catch (IOException ex) {
            Logger.getLogger(CacheImagenes.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
