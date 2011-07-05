package com.codeko.apps.maimonides.digitalizacion;

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.IObjetoBD;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.excepciones.NoExisteElementoException;
import com.codeko.util.Img;
import java.awt.Transparency;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Digitalizador extends MaimonidesBean {

    File carpetaGeneralPartes = null;
    File carpetaPartesProcesados = null;
    File carpetaPartesFallidos = null;
    ArrayList<MensajeDigitalizacion> mensajes = new ArrayList<MensajeDigitalizacion>();
    ArrayList<File> archivos = null;
    boolean cancelado = false;
    boolean terminado = true;
    ParteFaltas p = null;

    public synchronized boolean isTerminado() {
        return terminado;
    }

    public synchronized void setTerminado(boolean terminado) {
        this.terminado = terminado;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
    }

    public File getCarpetaGeneralPartes() {
        return carpetaGeneralPartes;
    }

    public final void setCarpetaGeneralPartes(File carpetaGeneralPartes) {
        this.carpetaGeneralPartes = carpetaGeneralPartes;
        archivos = null;
    }

    public File getCarpetaPartesFallidos() {
        return carpetaPartesFallidos;
    }

    public final void setCarpetaPartesFallidos(File carpetaPartesFallidos) {
        this.carpetaPartesFallidos = carpetaPartesFallidos;
    }

    public File getCarpetaPartesProcesados() {
        return carpetaPartesProcesados;
    }

    public final void setCarpetaPartesProcesados(File carpetaPartesProcesados) {
        this.carpetaPartesProcesados = carpetaPartesProcesados;
    }

    public ArrayList<MensajeDigitalizacion> getMensajes() {
        return mensajes;
    }

    public Digitalizador(File carpetaPartes, File carpetaProcesados, File carpetaFallidos) {
        setCarpetaGeneralPartes(carpetaPartes);
        setCarpetaPartesProcesados(carpetaProcesados);
        setCarpetaPartesFallidos(carpetaFallidos);
    }

    public boolean digitalizar(final boolean forzar) {
        getMensajes().clear();
        boolean ret = true;
        int cont = 1;
        for (File f : getArchivos()) {
            p = null;
            if (isCancelado()) {
                return ret;
            }
            setTerminado(false);
            firePropertyChange("progress", 0, (cont * 100) / getArchivos().size());
            final String msgActual = "Parte " + cont + "/" + getArchivos().size();
            firePropertyChange("message", null, msgActual);
            cont++;
            DigitalizacionParte digital = null;
            try {
                digital = new DigitalizacionParte(f.getAbsolutePath()) {

                    public boolean continuarProcesando(String codigoBarras) {
                        if (!forzar) {
                            try {
                                p = getParteFaltas();
                                return !p.isDigitalizado();
                            } catch (Exception ex) {
                                Logger.getLogger(Digitalizador.class.getName()).log(Level.SEVERE, null, ex);
                                return false;
                            }
                        } else {
                            return true;
                        }
                    }
                };
                digital.addPropertyChangeListener(new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("message".equals(evt.getPropertyName())) {
                            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), msgActual + ": " + evt.getNewValue());
                        } else {
                            firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                        }
                    }
                });
                try {
                    if (digital.procesar()) {
                        firePropertyChange("message", null, "Parte analizado. Guardando imagen...");
                        //Ahora tenemos que buscar el parte
                        String ext = "." + ConfiguracionParte.getConfiguracion().getExtensionImagenes();
                        File tmpImg = File.createTempFile("img_parte", ext);
                        ImageIO.write(Img.toBufferedImage(digital.getImagen(), Transparency.OPAQUE), ConfiguracionParte.getConfiguracion().getExtensionImagenes(), tmpImg);
//                        //TODO Verificar que esto está ok y quitar toda la parte de imagenes de advertencias de digitalizacion
                        int idImg = 0;// guardarImagenAdvertencia(tmpImg);
                        try {
                            firePropertyChange("message", null, "Aplicando datos a parte...");
                            if (p == null) {
                                p = digital.getParteFaltas();
                            }
                            p.addPropertyChangeListener(new PropertyChangeListener() {

                                @Override
                                public void propertyChange(PropertyChangeEvent evt) {
                                    if ("message".equals(evt.getPropertyName())) {
                                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), msgActual + ": " + evt.getNewValue());
                                    } else {
                                        firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                                    }
                                }
                            });

                            File destino = new File(getCarpetaPartesProcesados(), "Parte_" + p.getId() + "-" + digital.getPagina() + ext);
                            destino.getParentFile().mkdirs();
                            int nomPos = 1;
                            while (destino.exists()) {
                                destino = new File(getCarpetaPartesProcesados(), "Parte_" + p.getId() + "-" + digital.getPagina() + "_" + nomPos + ext);
                                nomPos++;
                            }
                            if (p.aplicarDigitalizacion(digital, forzar)) {
                                p.setArchivoImagen(tmpImg);
                                CacheImagenes.asignarImagen(p, digital.getPagina(), tmpImg);
                                if (p.getAdvertenciasDigitalizacion().isEmpty()) {
                                    MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_OK, "Parte " + p.getDescripcionObjeto() + " digitalizado correctamente.");
                                    msg.setParte(p);
                                    getMensajes().add(msg);
                                } else {

                                    ArrayList<MensajeDigitalizacion> advs = p.getAdvertenciasDigitalizacion();
                                    //TODO Parametrizar % de errores máximo
                                    //TODO Pone mensajes siempre en partes pequeños
//                                    double maxAdvertencias = p.getAlumnos().size() * 0.6;
//                                    if (advs.size() > maxAdvertencias) {
//                                        MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ADVERTENCIA, "Parte " + p.getDescripcionObjeto() + " tiene "+advs.size()+" advertencias, son demasiadas. Puede que se haya digitalizado mal o que no se haya rellenado correctamente. Sería conveniente revisarlo.");
//                                        msg.setParte(p);
//                                        msg.setIdImagen(idImg);
//                                        getMensajes().add(msg);
//                                    }
                                    for (MensajeDigitalizacion msgAd : advs) {
                                        msgAd.setIdImagen(idImg);
                                        getMensajes().add(msgAd);
                                    }

                                }
                                p.guardar();
                            } else {
                                for (String s : p.getErroresDigitalizacion()) {
                                    MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_IGNORADO, s);
                                    msg.setIdImagen(idImg);
                                    msg.setParte(p);
                                    getMensajes().add(msg);
                                }
                            }
                            f.renameTo(destino);
                        } catch (SQLException ex) {
                            MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_NO_EXISTE, "No se ha podido cargar el parte con código '" + digital.getCodigoBarras() + "'.");
                            msg.setIdImagen(idImg);
                            getMensajes().add(msg);
                        } catch (NoExisteElementoException e1) {
                            MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_NO_EXISTE, "No existe el parte con código '" + digital.getCodigoBarras() + "'.");
                            msg.setIdImagen(idImg);
                            getMensajes().add(msg);
                            f = moverFallido(f, msg);
                        }
                    } else {
                        for (String s : digital.getErrores()) {
                            MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_PARTE_FALLIDO, s);
                            getMensajes().add(msg);
                            f = moverFallido(f, msg);
                        }
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Digitalizador.class.getName()).log(Level.SEVERE, "Error procesando imagen.", ex);
                    MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_PARTE_FALLIDO, "Error procesando imagen digitalizada de parte.");
                    getMensajes().add(msg);
                    f = moverFallido(f, msg);
                }
            } catch (IOException ex) {
                //f = moverFallido(f);
                Logger.getLogger(Digitalizador.class.getName()).log(Level.SEVERE, "Error accediendo a archivo:" + f, ex);
                MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ERROR, "Error accediendo a archivo: " + f);
                getMensajes().add(msg);
            } catch (Exception e) {
                //f = moverFallido(f);
                Logger.getLogger(Digitalizador.class.getName()).log(Level.SEVERE, "Error desconocido procesando parte:" + f, e);
                Logger.getLogger(Digitalizador.class.getName()).log(Level.SEVERE, "Error desconocido procesando parte:" + f, e);
                MensajeDigitalizacion msg = new MensajeDigitalizacion(MensajeDigitalizacion.TIPO_ERROR, "Error desconocido procesando parte: " + f);
                getMensajes().add(msg);
            } finally {
                if (digital != null) {
                    digital.dispose();
                    digital = null;
                }
                ArrayList<IObjetoBD> copia = new ArrayList<IObjetoBD>();
                for (MensajeDigitalizacion m : getMensajes()) {
                    firePropertyChange("mensajeDigitalizacion", null, m);
                    if (m.isMostrar()) {
                        copia.add(m);
                    }
                }
                firePropertyChange("message", null, "Guardando mensajes...");
                guardarObjetosBD(copia);
                getMensajes().clear();
                copia = null;
            }
            System.gc();
            setTerminado(true);
        }

        return ret;
    }

    private File moverFallido(File original, MensajeDigitalizacion msg) {
        try {
            File destinoFallido = new File(getCarpetaPartesFallidos(), original.getName());
            destinoFallido.getParentFile().mkdirs();
            int pos = 1;
            while (destinoFallido.exists()) {
                destinoFallido = new File(getCarpetaPartesFallidos(), "(" + pos + ")" + original.getName());
                pos++;
            }
            if (original.renameTo(destinoFallido)) {
                original = destinoFallido;
                msg.setParteErroneo(original);
                File meta = new File(getCarpetaPartesFallidos(), original.getName() + ".info");
                Properties pMeta = new Properties();
                pMeta.setProperty("tipo", msg.getTipo() + "");
                pMeta.setProperty("tipoAdvertencia", msg.getTipoAdvertencia() + "");
                pMeta.setProperty("texto", msg.getMensaje());
                FileOutputStream fos = new FileOutputStream(meta);
                pMeta.store(fos, null);
                msg.setMetadatosParte(meta);
                fos.close();
            } else {
                Logger.getLogger(Digitalizador.class.getName()).log(Level.SEVERE, "Error moviendo parte {0} a {1}.", new Object[]{original, destinoFallido});
            }
        } catch (Exception e) {
            Logger.getLogger(Digitalizador.class.getName()).log(Level.SEVERE, "Error moviendo parte " + original + " a fallidos.", e);
        }
        return original;
    }

//    private int guardarImagenAdvertencia(File imagen) {
//        int id = 0;
//        try {
//            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("INSERT INTO advertencias_imagenes (imagen) VALUES(?)", PreparedStatement.RETURN_GENERATED_KEYS);
//            FileInputStream fis = new FileInputStream(imagen);
//            st.setBinaryStream(1, fis);
//            if (st.executeUpdate() > 0) {
//                id = (int) st.getLastInsertID();
//            }
//            fis.close();
//            st.close();
//        } catch (Exception e) {
//            Logger.getLogger(Digitalizador.class.getName()).log(Level.SEVERE, "Error guardando imagen advertencia:" + imagen, e);
//        }
//        return id;
//    }
    public ArrayList<File> getArchivos() {
        if (archivos == null) {
            archivos = new ArrayList<File>();
            File[] arch = getCarpetaGeneralPartes().listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().toLowerCase().endsWith("." + ConfiguracionParte.getConfiguracion().getExtensionImagenes());
                }
            });
            if (arch != null) {
                archivos.addAll(Arrays.asList(arch));
            }
        }
        return archivos;
    }
}
