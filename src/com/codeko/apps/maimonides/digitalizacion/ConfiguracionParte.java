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

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.conf.Configuracion;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfiguracionParte {
    @CdkAutoTablaCol(titulo="Ancho de las marcas de posicionamiento")
    private double anchoMarca = 8;
    @CdkAutoTablaCol(titulo="% de negro marcas posición")
    private double porcentajeNegroMarcasPosicion = 84;
    @CdkAutoTablaCol(titulo="Ancho del parte de asistencia")
    private double anchoParte = 539;
    @CdkAutoTablaCol(titulo="Alto de cada fila del parte")
    private double altoFila = 12.42;
    @CdkAutoTablaCol(titulo="Ancho de las celdas")
    private double anchoColumna = 27.15;
    @CdkAutoTablaCol(titulo="Distancia desde la marca de posicionamiento a la cabecera")
    private double distanciaDesdeMarcaACabecera = 125;
    @CdkAutoTablaCol(titulo="Distancia desde la marca de posicionamiento a la primera columna")
    private double distanciaDesdeMarcaAColumna = 213.1;
    @CdkAutoTablaCol(titulo="Número de columnas")
    private int numColumnas = 12;
    @CdkAutoTablaCol(titulo="% de negro manchado")
    private double porcentajeNegroManchado = 1;
    @CdkAutoTablaCol(titulo="% de negro marcado")
    private double porcentajeNegroMarcado = 5;
    @CdkAutoTablaCol(titulo="% de negro anulado")
    private double porcentajeNegroAnulado = 75;
    @CdkAutoTablaCol(titulo="% de negro dudoso")
    private double porcentajeNegroAnuladoDudoso = 50;
    @CdkAutoTablaCol(titulo="Margen para búsqueda de marca")
    private double margenBusquedaMarca = 70;
    @CdkAutoTablaCol(titulo="Margen de limpieza lateral de marca")
    private double margenLimpiezaLateral = 6;
    @CdkAutoTablaCol(titulo="Margen de limpieza del pie")
    private double margenLimpiezaPie = 8;
    @CdkAutoTablaCol(titulo="Número máximo de filas")
    private int numFilas = 43;
    @CdkAutoTablaCol(titulo="Posición de inicio del pie")
    private double inicioPie = 670;
    @CdkAutoTablaCol(titulo="Alto del bloque de firmas")
    private double altoBloqueFirmas = 75;
    @CdkAutoTablaCol(titulo="Número de casillas de firmas")
    private int numCasillasPie = 6;
    @CdkAutoTablaCol(titulo="% de negro firmado")
    private double porcentajeNegroFirmado = 1;
    @CdkAutoTablaCol(titulo="Extensión de imágenes de partes")
    private String extensionImagenes = "png";
    @CdkAutoTablaCol(titulo="Mostrar logs")
    private boolean mostrarLogs = false;
    @CdkAutoTablaCol(titulo="Mostrar marcas")
    private boolean mostrarMarcas = false;
    @CdkAutoTablaCol(titulo="Mostrar imagen final")
    private boolean mostrarImagenFinal = false;
    @CdkAutoTablaCol(titulo="% de negro linea fila")
    private double porcentajeNegroLineaFila = 40;
    @CdkAutoTablaCol(titulo="Ancho muestra linea")
    private double anchoMuestraLinea = 10;
    @CdkAutoTablaCol(titulo="Ancho muestra cabecera")
    private double anchoMuestraCabecera = 20;
    @CdkAutoTablaCol(titulo="% de negro linea cabecera")
    private double porcentajeNegroLineaCab = 77;
    @CdkAutoTablaCol(ignorar=true)
    private static ConfiguracionParte configuracion = null;
    @CdkAutoTablaCol(ignorar=true)
    public static final String NOMBRE_GRUPO="digitalizacion";

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
        return getConfiguracion(false);
    }

    public static ConfiguracionParte getConfiguracion(boolean forzar) {
        if (configuracion == null || forzar) {
            configuracion = new ConfiguracionParte();
            configuracion.cargarConfiguracion();
        }
        return configuracion;
    }

    private Properties getProperties(File props) {
        Properties tmpProp = new Properties();
        if (props != null && props.exists() && props.canRead()) {
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(props);
                tmpProp.load(fis);
            } catch (Exception ex) {
                Logger.getLogger(ConfiguracionParte.class.getName()).log(Level.SEVERE, "Error cargando propiedades de " + props, ex);
            } finally {
                Obj.cerrar(fis);
            }
        }
        return tmpProp;
    }

    public void cargarConfiguracion() {
        //Vamos cargando las propiedades progresivamente de las diferentes localizaciones creando un archivo de 
        // propiedades comun
        Configuracion cfg = MaimonidesApp.getApplication().getConfiguracion();
        //Primero de la base de datos
        Properties finalProp = new Properties();
        finalProp.putAll(cfg.getGroup(NOMBRE_GRUPO));
        //Luego el fichero en la carpeta de ejecución       
        finalProp.putAll(getProperties(new File("partes.cfg")));
        //El de la carpeta de usuario
        finalProp.putAll(getProperties(new File(Configuracion.getCarpetaUsuarioMaimonides(), "partes.cfg")));
        //Y finalmente la carpeta de partes
        finalProp.putAll(getProperties(new File(cfg.getCarpetaPartes(), "partes.cfg")));
        //Una vez cargadas todas las propiedades rellenamos la clase de configuracion
        try {
            Iterator<Object> it = finalProp.keySet().iterator();
            while (it.hasNext()) {
                try {
                    String k = it.next().toString();
                    String v = finalProp.getProperty(k).toString().trim();
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
        return getAnchoColumna() * 2;
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
    
    public Object getValor(Field f,String nombre) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException{
        return f.get(this);
    }
}
