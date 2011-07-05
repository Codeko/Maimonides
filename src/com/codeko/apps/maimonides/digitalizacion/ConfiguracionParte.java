package com.codeko.apps.maimonides.digitalizacion;

import com.codeko.util.Num;
import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

//TODO Limpiar 
public class ConfiguracionParte {

    private double anchoMarca = 8;
    private double anchoParte = 539;
    private double altoFila = 12.45;
    private double anchoColumna = 27;
    private double distanciaDesdeMarcaACabecera = 170.2;
    private double distanciaDesdeMarcaAColumna = 213.1;
    private int numColumnas = 12;
    private double porcentajeNegroManchado = 1;
    private double porcentajeNegroMarcado = 5;
    private double porcentajeNegroAnulado = 60;
    private double porcentajeNegroAnuladoDudoso = 50;
    private double porcentajeNegroFila = 6;
    private double margenBusquedaMarca = 70;
    private double margenLimpiezaSuperior = 4;
    private double margenLimpiezaLateral = 6;
    private double margenLimpiezaPie = 8;
    private int numFilas = 36;
    private double inicioPie = 626;
    private double anchoBloqueFirmas = anchoColumna * 2;
    private double altoBloqueFirmas = 72;
    private int numCasillasPie = 6;
    private double porcentajeNegroFirmado = 1;
    private String extensionImagenes = "png";
    private boolean mostrarLogs = false;
    private boolean mostrarMarcas = false;
    private boolean mostrarImagenFinal = false;
    private double porcentajeNegroMarcasPosicion = 84;
    private double porcentajeNegroLineaFila = 30;
    private double anchoMuestraLinea = 15;
    private double anchoMuestraCabecera = 20;
    private double porcentajeNegroLineaCab = 77;
    private static ConfiguracionParte configuracion = null;
    private static long modificacionFichero = 0;

    public double getPorcentajeNegroLineaCab() {
        return porcentajeNegroLineaCab;
    }

    public void setPorcentajeNegroLineaCab(double porcentajeNegroLineaCab) {
        this.porcentajeNegroLineaCab = porcentajeNegroLineaCab;
    }

    public double getAnchoMuestraLinea() {
        return anchoMuestraLinea;
    }

    public void setAnchoMuestraLinea(double anchoMuestraLinea) {
        this.anchoMuestraLinea = anchoMuestraLinea;
    }

    public double getAnchoMuestraCabecera() {
        return anchoMuestraCabecera;
    }

    public void setAnchoMuestraCabecera(double anchoMuestraCabecera) {
        this.anchoMuestraCabecera = anchoMuestraCabecera;
    }

    public static long getModificacionFichero() {
        return modificacionFichero;
    }

    public static void setModificacionFichero(long modificacionFichero) {
        ConfiguracionParte.modificacionFichero = modificacionFichero;
    }

    public double getPorcentajeNegroMarcasPosicion() {
        return porcentajeNegroMarcasPosicion;
    }

    public void setPorcentajeNegroMarcasPosicion(double porcentajeNegroMarcasPosicion) {
        this.porcentajeNegroMarcasPosicion = porcentajeNegroMarcasPosicion;
    }

    public double getMargenLimpiezaPie() {
        return margenLimpiezaPie;
    }

    public void setMargenLimpiezaPie(double margenLimpiezaPie) {
        this.margenLimpiezaPie = margenLimpiezaPie;
    }

    public boolean isMostrarImagenFinal() {
        return mostrarImagenFinal;
    }

    public void setMostrarImagenFinal(boolean mostrarImagenFinal) {
        this.mostrarImagenFinal = mostrarImagenFinal;
    }

    public boolean isMostrarLogs() {
        return mostrarLogs;
    }

    public void setMostrarLogs(boolean mostrarLogs) {
        this.mostrarLogs = mostrarLogs;
    }

    public boolean isMostrarMarcas() {
        return mostrarMarcas;
    }

    public void setMostrarMarcas(boolean mostrarMarcas) {
        this.mostrarMarcas = mostrarMarcas;
    }

    public static ConfiguracionParte getConfiguracion() {
        if (configuracion == null) {
            configuracion = new ConfiguracionParte();
        }
        configuracion.cargarConfiguracion(false);
        return configuracion;
    }

    public void cargarConfiguracion(boolean forzar) {
        //TODO Esto moverlo a codekolib
        File props = new File("partes.cfg");
        if (props.exists() && (forzar || props.lastModified() != getModificacionFichero())) {
            Logger.getLogger(ConfiguracionParte.class.getName()).info("Recargando configuración partes.");
            setModificacionFichero(props.lastModified());
            Properties p = new Properties();
            FileInputStream is;
            try {
                is = new FileInputStream(props);
                p.load(is);
                Iterator it = p.keySet().iterator();
                while (it.hasNext()) {
                    try {
                        String k = it.next().toString();
                        String v = p.getProperty(k).toString().trim();
                        if (Num.esNumero(v)) {
                            if (v.contains(".")) {
                                this.getClass().getDeclaredField(k).setDouble(this, Num.getDouble(v));
                            } else {
                                this.getClass().getDeclaredField(k).setInt(this, Num.getInt(v));
                            }
                        } else if (v.equals("true") || v.equals("false")) {
                            this.getClass().getDeclaredField(k).setBoolean(this, v.equals("true"));
                        } else {
                            this.getClass().getDeclaredField(k).set(this, v);
                        }
                    } catch (Exception e) {
                        Logger.getLogger(ConfiguracionParte.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(ConfiguracionParte.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private ConfiguracionParte() {
    }

    public double getPorcentajeNegroLineaFila() {
        return porcentajeNegroLineaFila;
    }

    public void setPorcentajeNegroLineaFila(double porcentajeNegroLineaFila) {
        this.porcentajeNegroLineaFila = porcentajeNegroLineaFila;
    }

    public String getExtensionImagenes() {
        return extensionImagenes;
    }

    public void setExtensionImagenes(String extensionImagenes) {
        this.extensionImagenes = extensionImagenes;
    }

    public double getPorcentajeNegroFirmado() {
        return porcentajeNegroFirmado;
    }

    public void setPorcentajeNegroFirmado(int porcentajeNegroFirmado) {
        this.porcentajeNegroFirmado = porcentajeNegroFirmado;
    }

    public int getNumCasillasPie() {
        return numCasillasPie;
    }

    public void setNumCasillasPie(int numCasillasPie) {
        this.numCasillasPie = numCasillasPie;
    }

    public double getAltoBloqueFirmas() {
        return altoBloqueFirmas;
    }

    public void setAltoBloqueFirmas(double altoBloqueFirmas) {
        this.altoBloqueFirmas = altoBloqueFirmas;
    }

    public double getAnchoBloqueFirmas() {
        return anchoBloqueFirmas;
    }

    public void setAnchoBloqueFirmas(double anchoBloqueFirmas) {
        this.anchoBloqueFirmas = anchoBloqueFirmas;
    }

    public double getPorcentajeNegroAnuladoDudoso() {
        return porcentajeNegroAnuladoDudoso;
    }

    public void setPorcentajeNegroAnuladoDudoso(int porcentajeNegroAnuladoDudoso) {
        this.porcentajeNegroAnuladoDudoso = porcentajeNegroAnuladoDudoso;
    }

    public double getPorcentajeNegroManchado() {
        return porcentajeNegroManchado;
    }

    public void setPorcentajeNegroManchado(int porcentajeNegroManchado) {
        this.porcentajeNegroManchado = porcentajeNegroManchado;
    }

    public int getNumFilas() {
        return numFilas;
    }

    public void setNumFilas(int numFilas) {
        this.numFilas = numFilas;
    }

    public double getAnchoMarca() {
        return anchoMarca;
    }

    public void setAnchoMarca(int anchoMarca) {
        this.anchoMarca = anchoMarca;
    }

    public double getInicioPie() {
        return inicioPie;
    }

    public void setInicioPie(int inicioPie) {
        this.inicioPie = inicioPie;
    }

    public double getAnchoParte() {
        return anchoParte;
    }

    public void setAnchoParte(double anchoParte) {
        this.anchoParte = anchoParte;
    }

//    public double getMargenSuperior() {
//        return margenSuperior;
//    }
//
//    public void setMargenSuperior(int margenSuperior) {
//        this.margenSuperior = margenSuperior;
//    }
//
//    public double getMargenLateral() {
//        return margenLateral;
//    }
//
//    public void setMargenLateral(int margenLateral) {
//        this.margenLateral = margenLateral;
//    }
//
//    public double getAltoParte() {
//        return altoParte;
//    }
//    public void setAltoParte(int altoParte) {
//        this.altoParte = altoParte;
//    }
    public double getAltoFila() {
        return altoFila;
    }

    public void setAltoFila(int altoFila) {
        this.altoFila = altoFila;
    }

    public double getAnchoColumna() {
        return anchoColumna;
    }

    public void setAnchoColumna(double anchoColumna) {
        this.anchoColumna = anchoColumna;
    }

    public double getDistanciaDesdeMarcaACabecera() {
        return distanciaDesdeMarcaACabecera;
    }

    public void setDistanciaDesdeMarcaACabecera(int distanciaDesdeMarcaACabecera) {
        this.distanciaDesdeMarcaACabecera = distanciaDesdeMarcaACabecera;
    }

    public double getDistanciaDesdeMarcaAColumna() {
        return distanciaDesdeMarcaAColumna;
    }

    public void setDistanciaDesdeMarcaAColumna(int distanciaDesdeMarcaAColumna) {
        this.distanciaDesdeMarcaAColumna = distanciaDesdeMarcaAColumna;
    }

    public int getNumColumnas() {
        return numColumnas;
    }

    public void setNumColumnas(int columnas) {
        this.numColumnas = columnas;
    }

    public double getPorcentajeNegroMarcado() {
        return porcentajeNegroMarcado;
    }

    public void setPorcentajeNegroMarcado(int porcentajeNegroMarcado) {
        this.porcentajeNegroMarcado = porcentajeNegroMarcado;
    }

    public double getPorcentajeNegroAnulado() {
        return porcentajeNegroAnulado;
    }

    public void setPorcentajeNegroAnulado(int porcentajeNegroAnulado) {
        this.porcentajeNegroAnulado = porcentajeNegroAnulado;
    }

    public double getPorcentajeNegroFila() {
        return porcentajeNegroFila;
    }

    public void setPorcentajeNegroFila(int porcentajeNegroFila) {
        this.porcentajeNegroFila = porcentajeNegroFila;
    }

    public double getMargenBusquedaMarca() {
        return margenBusquedaMarca;
    }

    public void setMargenBusquedaMarca(int margenBusquedaMarca) {
        this.margenBusquedaMarca = margenBusquedaMarca;
    }

    public double getMargenLimpiezaLateral() {
        return margenLimpiezaLateral;
    }

    public void setMargenLimpiezaLateral(double margenLimpiezaLateral) {
        this.margenLimpiezaLateral = margenLimpiezaLateral;
    }

    public double getMargenLimpiezaSuperior() {
        return margenLimpiezaSuperior;
    }

    public void setMargenLimpiezaSuperior(double margenLimpiezaSuperior) {
        this.margenLimpiezaSuperior = margenLimpiezaSuperior;
    }
}
