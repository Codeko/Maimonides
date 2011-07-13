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

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.excepciones.NoExisteElementoException;
import com.codeko.util.Img;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.codeko.util.Num;
import com.codeko.util.Str;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.GlobalHistogramBinarizer;
import java.awt.Image;
import java.awt.Transparency;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
//TODO Limpiar
public class DigitalizacionParte extends MaimonidesBean {

    public static final int MANCHADO = -2;
    public static final int MARCADO = 1;
    public static final int LIMPIO = 0;
    public static final int ANULADO = -1;
    public static final int ANULADO_DUDOSO = 2;
    String codigoBarras = "";
    Integer idParte = null;
    Integer pagina = null;
    double proporcion = 1;
    int[] pix = null;
    BufferedImage imagen = null;
    BufferedImage imagenNoModificada = null;
    int anchoImagen = 0;
    int altoImagen = 0;
    boolean girarImagen = false;
    int iniCabReal = (int) (getConf().getDistanciaDesdeMarcaACabecera());
    int iniColReal = (int) (getConf().getDistanciaDesdeMarcaAColumna());
    int iniPieReal = 0;
    //int altoFilaReal = 0;
    ArrayList<DatoDigitalizacion> datos = new ArrayList<DatoDigitalizacion>();
    boolean mostrarMarcas = false;
    boolean mostrarImagenFinal = false;
    Graphics2D graficos = null;
    boolean mostrarLogs = false;
    DatoDigitalizacion pie = new DatoDigitalizacion(-1);
    ArrayList<String> errores = new ArrayList<String>();
    ConfiguracionParte conf = null;
    ParteFaltas parteFaltas = null;

    public BufferedImage getImagenNoModificada() {
        return imagenNoModificada;
    }

    public final void setImagenNoModificada(BufferedImage imagenNoModificada) {
        this.imagenNoModificada = imagenNoModificada;
    }

    public ArrayList<String> getErrores() {
        return errores;
    }

    public ConfiguracionParte getConf() {
        if (conf == null) {
            conf = ConfiguracionParte.getConfiguracion();
        }
        return conf;
    }

    public void setConf(ConfiguracionParte conf) {
        this.conf = conf;
    }

    public DatoDigitalizacion getPie() {
        return pie;
    }

    protected Graphics2D getGraficos() {
        if (graficos == null) {
            graficos = getImagen().createGraphics();
        }
        return graficos;
    }

    protected void setGraficos(Graphics2D graficos) {
        this.graficos = graficos;
    }

    protected int getIniColReal() {
        return iniColReal;
    }

    protected void setIniColReal(int iniColReal) {
        this.iniColReal = iniColReal;
    }

    protected int getIniCabReal() {
        return iniCabReal;
    }

    protected void setIniCabReal(int iniCabReal) {
        System.out.println("Cambiando INI CAB de " + this.iniCabReal + " a " + iniCabReal);
        this.iniCabReal = iniCabReal;
    }

    public ArrayList<DatoDigitalizacion> getDatos() {
        return datos;
    }

    protected void addDato(DatoDigitalizacion d) {
        getDatos().add(d);
    }

    protected boolean isGirarImagen() {
        return girarImagen;
    }

    protected void setGirarImagen(boolean girarImagen) {
        this.girarImagen = girarImagen;
    }

    protected int getAnchoImagen() {
        return anchoImagen;
    }

    protected void setAnchoImagen(int anchoImagen) {
        this.anchoImagen = anchoImagen;
    }

    protected int getAltoImagen() {
        return altoImagen;
    }

    protected void setAltoImagen(int altoImagen) {
        this.altoImagen = altoImagen;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    protected void setCodigoBarras(String id) {
        this.codigoBarras = id;
        String sIdParte = id;
        String sPagina = "1";
        if (id.indexOf("-") > -1) {
            sIdParte = id.substring(0, id.indexOf("-"));
            sPagina = id.substring(id.indexOf("-") + 1);
        }
        setIdParte(Num.getInt(sIdParte));
        setPagina(Num.getInt(sPagina));
    }

    public Integer getIdParte() {
        return idParte;
    }

    public void setIdParte(Integer idParte) {
        this.idParte = idParte;
    }

    public Integer getPagina() {
        return pagina;
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina;
    }

    protected double getProporcion() {
        return proporcion;
    }

    protected void setProporcion(double proporcion) {
        this.proporcion = proporcion;
    }

    protected int[] getPix() {
        return pix;
    }

    protected void setPix(int[] pix) {
        this.pix = pix;
    }

    protected BufferedImage getImagen() {
        return imagen;
    }

    protected final void setImagen(BufferedImage imagen) {
        this.imagen = imagen;
        setGraficos(null);
    }

    public DigitalizacionParte(BufferedImage imagen) {
        setImagen(imagen);
        setImagenNoModificada(imagen);
    }

    public DigitalizacionParte(String archivo) throws IOException {
        setImagen(cargarImagen(archivo));
    }

    public void dispose() {
        setImagen(null);
        setImagenNoModificada(null);
        if (graficos != null) {
            graficos.dispose();
        }
        getDatos().clear();
        setPix(null);
    }

    private Point buscarMarcaDerecha(int[][] imgPix) {
        Point derecha = null;
        int margenBusqueda = getProporcionado(getConf().getMargenBusquedaMarca());
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.red);
            getGraficos().drawRect(getAnchoImagen() - margenBusqueda, 0, margenBusqueda, margenBusqueda);
        }
        int porcentaje = 0;
        Point punto = null;
        for (int y = 0; y < margenBusqueda && derecha == null; y++) {
            for (int x = getAnchoImagen() - 1; x >= getAnchoImagen() - margenBusqueda && derecha == null; x--) {
                int pixel = imgPix[y][x];
                if (!UtilImgParte.esBlanco(pixel)) {
                    // Sacamos un cuadrado de la mitad de la marca
                    // proporcionada
                    int anchoMarca = getProporcionado((int) (getConf().getAnchoMarca()));
                    int[][] marca = UtilImgParte.extraerCuadradoDeMatriz(imgPix, x - anchoMarca, y, anchoMarca);
                    // Si el cuadrado es negro es la marca
                    int nuevoPorcent = getPorcentajeNegro(UtilImgParte.matrizToArray(marca));

                    if (nuevoPorcent > porcentaje) {
                        punto = new Point(x, y);
                        porcentaje = nuevoPorcent;
                    }
                }
            }
        }
        if (porcentaje >= getConf().getPorcentajeNegroMarcasPosicion()) {
            derecha = punto;
        } else {
            if (punto == null) {
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "No se ha encontrado ninguna marca derecha!: {0}", getCodigoBarras());
            } else {
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Se ha encontrado una marca derecha pero no cumple el porcentaje {0} tiene {1} : {2}", new Object[]{getConf().getPorcentajeNegroMarcasPosicion(), porcentaje, getCodigoBarras()});
            }
        }
        return derecha;
    }

    public void prepararImagenExtraccionCasillas() throws InterruptedException {
        scanImage();
        setIniCabReal(getProporcionado(getConf().getDistanciaDesdeMarcaACabecera()));
        setIniColReal(getProporcionado(getConf().getDistanciaDesdeMarcaAColumna()));
    }

    public Image getImagen(int casillaInicio, int posFila, int numCasillas) {
        //getConf().setMostrarMarcas(true);
        ArrayList<FilaDig> filas = cargarFilas();
        //Visor.mostrarImagen(getAnchoImagen(), getPix());
        // Ahora por cada fila vemos su recuadros
        int iniCol = getIniColReal();
        int ancho = getAnchoImagen() - iniCol;
        int anchoBloque = getProporcionado(getConf().getAnchoColumna() * numCasillas);
        log("BLOQUES DE :" + anchoBloque / getProporcion());
        FilaDig fila = filas.get(posFila);
        int offSet = getProporcionado((getConf().getAnchoColumna()) * casillaInicio);
        int[] bloque = getBloque(anchoBloque, offSet, fila.getPix(), ancho);
        //Visor.mostrarImagen(ancho, fila.getPix());
        MemoryImageSource mis = new MemoryImageSource(anchoBloque, fila.getAlto(), bloque, 0, anchoBloque);
        Image m = java.awt.Toolkit.getDefaultToolkit().createImage(mis);
        mis = null;
        bloque = null;
        filas = null;
        return m;
    }

    public ArrayList<ArrayList<Image>> getCasillas(int numCasillas) {
        ArrayList<ArrayList<Image>> casillas = new ArrayList<ArrayList<Image>>();
        ArrayList<FilaDig> filas = cargarFilas();
        //Visor.mostrarImagen(getAnchoImagen(), getPix());
        // Ahora por cada fila vemos su recuadros
        int iniCol = getIniColReal();
        //MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource(subImg);
        int ancho = getAnchoImagen() - iniCol;
        int anchoBloque = getProporcionado(getConf().getAnchoColumna() * numCasillas);
        log("BLOQUES DE :" + anchoBloque / getProporcion());
        for (FilaDig fd : filas) {
            //Ahora por cada bloque de casillas sacamos su imagen
            int cols = getConf().getNumColumnas() / numCasillas;
            ArrayList<Image> imagenesFila = new ArrayList<Image>();
            for (int i = 0; i < cols; i++) {
                int offSet = getProporcionado((getConf().getAnchoColumna()) * (i * 2));
                int[] bloque = getBloque(anchoBloque, offSet, fd.getPix(), ancho);
                MemoryImageSource mis = new MemoryImageSource(anchoBloque, fd.getAlto(), bloque, 0, anchoBloque);
                Image m = java.awt.Toolkit.getDefaultToolkit().createImage(mis);
                imagenesFila.add(m);
                mis = null;
                bloque = null;
            }
            casillas.add(imagenesFila);
        }
        filas = null;
        return casillas;
    }

    private boolean procesarImagen() throws InterruptedException {
        //Converitmos la imagen a pixeles
        boolean ret = scanImage();
        //Vemos si hay que darle la vuelta
        if (ret && isGirarImagen()) {
            log("LA IMAGEN ESTÁ BOCA ABAJO. ROTADA.");
            firePropertyChange("message", null, "Imagen boca abajo. Enderezando imagen...");
            int[] pixelesInvertidos = new int[getPix().length];
            for (int i = pixelesInvertidos.length - 1, x = 0; i > -1; i--, x++) {
                pixelesInvertidos[x] = getPix()[i];
            }
            setPix(pixelesInvertidos);
            // Y Guardamos la imagen tambien
            MemoryImageSource mis = new MemoryImageSource(getAnchoImagen(), getPix().length / getAnchoImagen(), getPix(), 0, getAnchoImagen());
            setImagen(Img.toBufferedImage(java.awt.Toolkit.getDefaultToolkit().createImage(mis), Transparency.OPAQUE));
            ret = scanImage();
        }
        //Ahora buscamos la marza izquierda
        // Ahora tenemos que asignar los margenes
        int[][] imgPix = UtilImgParte.arrayToMatriz(getPix(), getAnchoImagen());
        Point iz = buscarMarcaIzquierda(imgPix);
        if (iz != null) {
            Point derecha = buscarMarcaDerecha(imgPix);
            if (derecha != null) {
                //Si no corresponde la y de una y otra marca hay que enderezar
                if (derecha.getY() != iz.getY()) {
                    if (getConf().isMostrarMarcas()) {
                        getGraficos().setColor(Color.PINK);
                        getGraficos().drawRect((int) derecha.getX() - getProporcionado(getConf().getAnchoMarca()), (int) derecha.getY(), getProporcionado(getConf().getAnchoMarca()), getProporcionado(getConf().getAnchoMarca()));
                    }
                    double vX = derecha.getX() - iz.getX();
                    double vY = derecha.getY() - iz.getY();
                    double arcoTan = Math.atan2(vY, vX);
                    log("LA IMAGEN ESTA TORCIDA: ROTANDO IMAGEN " + arcoTan);
                    firePropertyChange("message", null, "Imagen torcida. Enderezando imagen...");
                    BufferedImage imgRotada = (UtilImgParte.rotar(getImagen(), -arcoTan));
                    setImagen(imgRotada);
                    derecha = buscarMarcaDerecha(imgPix);
                }
                //Una vez que tenemos las dos marcas cortamos la imagen por las marcas
                BufferedImage imgFinal = getImagen().getSubimage((int) iz.getX(), (int) iz.getY(), (int) (getImagen().getWidth() - iz.getX() - (getImagen().getWidth() - derecha.getX())), (int) (getImagen().getHeight() - iz.getY()));
                setImagen(imgFinal);
                ret = scanImage();
                setIniCabReal(getProporcionado(getConf().getDistanciaDesdeMarcaACabecera()));
                setIniColReal(getProporcionado(getConf().getDistanciaDesdeMarcaAColumna()));
                setIniPieReal(getProporcionado(getConf().getInicioPie()));
                if (verificarParteRecto()) {
                    //Si ha habido cambios vovemos a escanear la imagen
                    ret = scanImage();
                    //Volvemos a verificar para asignar la cabecera
                    verificarParteRecto();
                }
            } else {
                log("No se ha encontrado marca derecha!: " + getCodigoBarras());
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "No se ha encontrado marca derecha!: {0}", getCodigoBarras());
                getErrores().add("No se ha encontrado la marca derecha del parte " + getCodigoBarras() + ".");
                ret = false;
            }
        } else {
            log("No se ha encontrado marca izquierda!: " + getCodigoBarras());
            Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "No se ha encontrado marca izquierda!: {0}", getCodigoBarras());
            getErrores().add("No se ha encontrado la marca izquierda del parte " + getCodigoBarras() + ".");
            ret = false;
        }
        return ret;
    }

    public boolean procesar() throws InterruptedException {
        getPie().getColumnas().clear();
        getErrores().clear();
        boolean ret = false;
        if (leerCodigoDeBarras()) {
            if (continuarProcesando()) {
                if (procesarImagen()) {
                    ArrayList<FilaDig> filas = cargarFilas();
                    // Ahora por cada fila vemos su recuadros
                    int iniCol = getIniColReal();
                    int ancho = getAnchoImagen() - iniCol;
                    int anchoBloque = getProporcionado(getConf().getAnchoColumna());
                    log("BLOQUES DE :" + anchoBloque / getProporcion());
                    for (FilaDig fila : filas) {
                        //FilaDig fila = filas.elementAt(posFila);
                        // Ahora por cada fila nos interesan los disntintos bloques
                        DatoDigitalizacion d = new DatoDigitalizacion(fila.getPos());
                        //Visor.mostrarImagen(ancho, fila.getPix());
                        for (int x = 0; x < getConf().getNumColumnas(); x++) {
                            firePropertyChange("message", null, "Analizando fila " + fila.getPos() + " casilla " + x + "...");
                            int offSet = getProporcionado((getConf().getAnchoColumna()) * x);
                            int[] bloque = getBloque(anchoBloque, offSet, fila.getPix(), ancho);
                            mostrarMarcasLinea(iniCol, offSet, fila, anchoBloque);
                            int margenSuperior = fila.getAlto() / 3;
                            int margenLateral = getProporcionado(getConf().getMargenLimpiezaLateral());
                            int nuevoAnchoCasilla = anchoBloque - (margenLateral * 2);
                            bloque = UtilImgParte.matrizToArray(UtilImgParte.extraerRectanguloDeMatriz(UtilImgParte.arrayToMatriz(bloque,
                                    anchoBloque), margenLateral,
                                    margenSuperior,
                                    nuevoAnchoCasilla,
                                    fila.getAlto() - (margenSuperior * 2)));

                            int porcent = getPorcentajeNegro(bloque);
                            String str = "FILA " + fila.getPos() + " COL: " + x + " POR:" + porcent;
                            log(str);
                            mostrarMarcasBloque(iniCol, x, margenLateral, margenSuperior, fila, nuevoAnchoCasilla);
                            if (porcent > getConf().getPorcentajeNegroAnulado()) {
                                d.addColumna(ANULADO);
                            } else if (porcent > getConf().getPorcentajeNegroAnuladoDudoso()) {
                                d.addColumna(ANULADO_DUDOSO);
                            } else if (porcent > getConf().getPorcentajeNegroMarcado()) {
                                d.addColumna(MARCADO);
                            } else if (porcent > getConf().getPorcentajeNegroManchado()) {
                                d.addColumna(MANCHADO);
                            } else {
                                d.addColumna(LIMPIO);
                            }
                        }
                        addDato(d);
                    }
                    filas = null;
                    //Ahora analizamos el pie 
                    procesarPie();
                    getGraficos().dispose();
                    if (getConf().isMostrarImagenFinal()) {
                        Visor.mostrarImagen(getImagen(), getProporcion(), new Point(0, 0)).setTitle("Marcas en imagen");
                    }
                    ret = true;

                }
            } else {
                ret = true;
            }
        } else {
            log("No se ha encontrado el código de barras");
            Logger.getLogger(this.getClass().toString()).severe("No se ha encontrado el código de barras");
            firePropertyChange("message", null, "No se ha encontrado el código de barras.");
            getErrores().add("No se ha encontrado el código de barras en el parte.");
        }
        return ret;
    }

    public boolean continuarProcesando() {
        return true;
    }

    public int getNumeroFilasParte() {
        int filas = 0;
        try {
            if (getParteFaltas() != null) {
                int sumFila = (getPagina() - 1) * getConf().getNumFilas();
                int numFilasParte = getParteFaltas().getAsistencia().size();
                filas = numFilasParte - sumFila;
            }
        } catch (SQLException ex) {
            Logger.getLogger(DigitalizacionParte.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoExisteElementoException ex) {
            Logger.getLogger(DigitalizacionParte.class.getName()).log(Level.SEVERE, null, ex);
        }
        return filas;
    }

    public ParteFaltas getParteFaltas() throws SQLException, NoExisteElementoException {
        if (parteFaltas == null) {
            parteFaltas = new ParteFaltas(getIdParte());
        }
        return parteFaltas;
    }

    private ArrayList<FilaDig> cargarFilas() {
        // ajustarInicioFila();
        int iniCab = getIniCabReal();
        final int ancho = getAnchoImagen();
        int porcent = 0;
        //La posición inicial en el array de pixeles
        int ini = 0;
        int tam = 1;
        int countFilas = 0;
        int iniAnterior = iniCab;
        double alturaMedia = getProporcionado(getConf().getAltoFila());
        ArrayList<FilaDig> filas = new ArrayList<FilaDig>();
        //int ultimoPosFin = 0;
        int maxFilas = getNumeroFilasParte();//getConf().getNumFilas();
        int maxFilasConfig = getConf().getNumFilas();
        int posPie = getProporcionado(getConf().getInicioPie() + 5);
        boolean continuar = true;
        boolean autoCrearFilasSiFaltan = true;
        while (ini < getPix().length && countFilas < maxFilas && countFilas < maxFilasConfig && iniCab < posPie && continuar) {
            ini = iniCab * ancho;
            int[] fila = new int[ancho * (getProporcionado(tam))];
            for (int i = 0; i < fila.length && ini < getPix().length; ini++, i++) {
                fila[i] = getPix()[ini];
            }
            int anchoMuestra = (int) getConf().getAnchoMuestraLinea();
            //Visor.mostrarImagen(ancho, fila);
            fila = getBloque(anchoMuestra, 0, fila, ancho);
            //Visor.mostrarImagen(getIniColReal(), fila);
            porcent = getPorcentajeNegro(fila);
            int alto = iniCab - iniAnterior;
            if (porcent != 0) {
                log("PDF:" + porcent);
            }
            if (porcent > getConf().getPorcentajeNegroLineaFila() && alto > 10) {
                if (alturaMedia == 0) {
                    alturaMedia = alto;
                }
                double margenMaximo = (alturaMedia / 5);
                //ultimoPosFin = iniCab;
                if (Math.abs(alturaMedia - alto) > margenMaximo) {
                    log("Encontrada fila: " + (countFilas + 1) + " ALT " + alto + " M " + alturaMedia + " MAX " + margenMaximo);
                    //Ahora tenemos que ver si encaja con x filas
//                    double dif = (alto % alturaMedia);
//                    double dif2 = Math.abs(dif - alturaMedia);
                    if (autoCrearFilasSiFaltan) {//&& (dif == 0 || dif2 < (alturaMedia / 5))) {
                        int filasSaltadas = (int) Math.round(alto / alturaMedia);
                        int filasRestantes = maxFilas - countFilas;
                        //Si hay mas filas de las que faltan tenemos que poner que sean solo las que faltan
                        if (filasSaltadas > filasRestantes) {
                            alto = (int) alturaMedia;
                            for (int i = 0; i < filasRestantes; i++) {
                                FilaDig f = new FilaDig(getFilaX(iniAnterior, alto), alto, countFilas, iniAnterior);
                                filas.add(f);
                                iniAnterior += alto;
                                if (getConf().isMostrarMarcas()) {
                                    getGraficos().setColor(Color.YELLOW);
                                    getGraficos().drawRect(0, iniAnterior, anchoMuestra, 1);
                                }
                                countFilas++;
                            }
                        } else if (filasSaltadas > 0) {
                            //Si hay menos entonces podemos calcular dividiento
                            log("Generando " + filasSaltadas + " filas artificialmente.");
                            alto /= filasSaltadas;
                            for (int i = 0; i < filasSaltadas; i++) {
                                FilaDig f = new FilaDig(getFilaX(iniAnterior, alto), alto, countFilas, iniAnterior);
                                filas.add(f);
                                iniAnterior += alto;
                                if (getConf().isMostrarMarcas()) {
                                    getGraficos().setColor(Color.YELLOW);
                                    getGraficos().drawRect(0, iniAnterior, anchoMuestra, 1);
                                }
                                countFilas++;
                            }
                        }
                        iniAnterior = iniCab;
                    } else {
                        continuar = false;
                    }
                    iniCab++;
                } else {
                    alturaMedia = (alturaMedia + alto) / 2;
                    FilaDig f = new FilaDig(getFilaX(iniAnterior, alto), alto, countFilas, iniAnterior);
                    filas.add(f);
                    if (getConf().isMostrarMarcas()) {
                        getGraficos().setColor(Color.RED);
                        getGraficos().drawRect(0, iniCab, anchoMuestra, 1);
                    }
                    countFilas++;
                    iniAnterior = iniCab;
                    iniCab += 10;
                }
            } else {
                iniCab++;
            }
        }
        //TODO Volver a hacer un metodo para localizar el pie
        //setIniPieReal(ultimoPosFin);
        return filas;
    }

    private boolean verificarParteRecto() {
        int iniCab = getIniCabReal() - 30;
        int ancho = getAnchoImagen();
        int ini = 0;
        int tam = 1;
        int anchoMuestra = (int) getConf().getAnchoMuestraCabecera();
        int porcentNegroLineaCab = (int) getConf().getPorcentajeNegroLineaCab();
        int numOksIzq = 0;
        int numOksDer = 0;
        //Ajustamos el inicio por la izquierda
        int iniCabNuevaIzq = iniCab;
        int iniCabNuevaDer = iniCab;
        boolean izquierdaAsignada = false;
        boolean derechaAsignada = false;
        int numFilasNecesarias = 3;
        while (ini < getPix().length && iniCab < getIniCabReal() + 30 && (!izquierdaAsignada || !derechaAsignada)) {
            ini = iniCab * ancho;
            int[] fila = new int[ancho * (getProporcionado(tam))];
            for (int i = 0; i < fila.length && ini < getPix().length; ini++, i++) {
                fila[i] = getPix()[ini];
            }

            if (!izquierdaAsignada) {
                //tenemos que buscar que haya al menos numFilasNecesarias con el porcentaje de negro deseado
                int[] filaIzq = getBloque(anchoMuestra, 0, fila, ancho);
                int porcentIzq = getPorcentajeNegro(filaIzq);
                log("PNI (" + numOksIzq + "):" + porcentIzq + " [" + iniCab + "]");
                if (porcentIzq > porcentNegroLineaCab) {
                    numOksIzq++;
                } else {
                    if (numOksIzq >= numFilasNecesarias) {
                        iniCabNuevaIzq = iniCab;
                        izquierdaAsignada = true;
                    } else {
                        numOksIzq = 0;
                    }
                }
            }
            if (!derechaAsignada) {
                int[] filaDer = getBloque(anchoMuestra, ancho - anchoMuestra, fila, ancho);
                int porcentDer = getPorcentajeNegro(filaDer);
                log("PND (" + numOksDer + "):" + porcentDer + " [" + iniCab + "]");
                if (porcentDer > porcentNegroLineaCab) {
                    numOksDer++;
                } else {
                    if (numOksDer >= numFilasNecesarias) {
                        iniCabNuevaDer = iniCab;
                        derechaAsignada = true;
                    } else {
                        numOksDer = 0;
                    }
                }
            }
            iniCab++;
        }
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.GREEN);
            getGraficos().drawRect(0, iniCabNuevaIzq, anchoMuestra, 1);
            getGraficos().drawRect(ancho - anchoMuestra, iniCabNuevaDer, anchoMuestra, 1);
        }
        if (iniCabNuevaDer != iniCabNuevaIzq) {
            double vX = ancho;
            double vY = iniCabNuevaDer - iniCabNuevaIzq;
            double arcoTan = Math.atan2(vY, vX);
            //int operador = iniCabNuevaIzq > iniCabNuevaDer ? -1 : 1;
            log("LA IMAGEN ESTA TORCIDA: ROTANDO IMAGEN " + arcoTan);
            firePropertyChange("message", null, "Imagen torcida. Enderezando imagen...");
            BufferedImage imgRotada = (UtilImgParte.rotar(getImagen(), -arcoTan));
            setImagen(imgRotada);
            return true;
        } else {
            int dif = iniCabNuevaDer - getIniCabReal();
            setIniCabReal(iniCabNuevaDer);
            setIniPieReal(getIniPieReal() + dif);
            if (getConf().isMostrarMarcas()) {
                getGraficos().setColor(Color.CYAN);
                getGraficos().drawRect(0, iniCabNuevaDer, ancho, 1);
            }
        }
        return false;
    }

//    private void ajustarInicioFila() {
//        int iniCab = getIniCabReal();
//        int ancho = getAnchoImagen();
//        int porcent = 0;
//        int count = 0;
//        //Marcamos la posicion inicial
//        if (getConf().isMostrarMarcas()) {
//            getGraficos().setColor(Color.MAGENTA);
//            getGraficos().drawRect(0, iniCab, ancho, 1);
//        }
//        while (porcent < 80 && count < 50) {
//            int[] fila = new int[ancho * 1];
//            int ini = (iniCab) * getAnchoImagen();
//            for (int i = 0; i < fila.length && ini < getPix().length; ini++, i++) {
//                fila[i] = getPix()[ini];
//            }
//            porcent = getPorcentajeNegro(fila);
//            count++;
//            iniCab -= 1;
//        }
//        if (iniCab != getIniCabReal()) {
//            iniCab++;
//        }
//        //Asignamos la nueva cabecera
//        if (iniCab != getIniCabReal()) {
//            int dif = iniCab - getIniCabReal();
//            setIniCabReal(iniCab);
//            setIniPieReal(getIniPieReal() + dif);
//            if (getConf().isMostrarMarcas()) {
//                getGraficos().setColor(Color.CYAN);
//                getGraficos().drawRect(0, iniCab, ancho, 1);
//            }
//        }
//    }
    private Point buscarMarcaIzquierda(int[][] img) {
        Point izquierda = null;
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.red);
            getGraficos().drawRect(1, 1, getProporcionado(getConf().getMargenBusquedaMarca()), getProporcionado(getConf().getMargenBusquedaMarca()));
        }
        //Visor.mostrarImagen(getImagen());
        Point punto = null;
        int porcentaje = 0;
        for (int y = 0; y < getProporcionado(getConf().getMargenBusquedaMarca()) && izquierda == null; y++) {
            for (int x = 0; x < getProporcionado(getConf().getMargenBusquedaMarca()) && izquierda == null; x++) {
                int pixel = img[y][x];
                if (!UtilImgParte.esBlanco(pixel)) {
                    int anchoMarca = getProporcionado((getConf().getAnchoMarca()));
                    int[][] marca = UtilImgParte.extraerCuadradoDeMatriz(img, x, y, anchoMarca);
                    // mostrarImagen(anchoMarca, );
                    // Si el cuadrado es negro es la marca
                    int nuevoPorcent = getPorcentajeNegro(UtilImgParte.matrizToArray(marca));
                    if (nuevoPorcent > porcentaje) {
                        punto = new Point(x, y);
                        porcentaje = nuevoPorcent;
                    }
                }
            }
        }
        if (porcentaje >= getConf().getPorcentajeNegroMarcasPosicion()) {
            izquierda = punto;
        } else {
            if (punto == null) {
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "No se ha encontrado ninguna marca izquierda!: {0}", getCodigoBarras());
            } else {
                Logger.getLogger(this.getClass().toString()).log(Level.SEVERE, "Se ha encontrado una marca izquierda pero no cumple el porcentaje {0} tiene {1} : {2}", new Object[]{getConf().getPorcentajeNegroMarcasPosicion(), porcentaje, getCodigoBarras()});
            }
        }
        if (getConf().isMostrarMarcas() && izquierda != null) {
            getGraficos().setColor(Color.PINK);
            getGraficos().drawRect((int) izquierda.getX(), (int) izquierda.getY(), getProporcionado(getConf().getAnchoMarca()), getProporcionado(getConf().getAnchoMarca()));
        }
        return izquierda;
    }

    private int[] getBloque(int ancho, int offset, int[] pixeles,
            int anchoImagen) {
        int filas = pixeles.length / anchoImagen;
        int[] res = new int[filas * ancho];
        for (int i = 0, pos = 0; i < pixeles.length; i++) {
            int posX = i % anchoImagen;
            if (posX >= offset && posX < (offset + ancho)) {
                res[pos] = pixeles[i];
                pos++;
            }
        }
        // mostrarImagen(ancho,filas,res);
        return res;
    }

//    private Vector<int[]> getFilas() {
//        Vector<int[]> filas = new Vector<int[]>();
//        int i = 0;
//        for (i = 0; i < getConf().getNumFilas(); i++) {
//            int[] fila = getFila(i);
//            int pn = getPorcentajeNegro(fila);
//            log("FILA: " + i + " PN:" + pn);
//            filas.add(fila);
//        }
//        return filas;
//    }
    private int getPorcentajeNegro(int[] pixels) {
        int numBlancos = 0;
        for (int i = 0; i < pixels.length; i++) {
            if (UtilImgParte.esBlanco(pixels[i])) {
                numBlancos++;
            }
        }
        double porcent = 100 - ((double) (numBlancos * 100) / (double) pixels.length);
        return (int) Num.round(porcent, 0);
    }

//    public int getAltoFilaReal() {
//        return altoFilaReal;
//    }
//
//    public void setAltoFilaReal(int altoFilaReal) {
//        this.altoFilaReal = altoFilaReal;
//    }
//    private int[] getFila(int numFila) {
//        // Una vez calculados los margenes necesitamos las disntintas filas de
//        // alumnos, nos saltamos la parte del nombre
//        int iniCol = getIniColReal();
//        int ancho = getAnchoImagen() - iniCol;
//        int[] fila = new int[ancho * getAltoFilaReal()];
//        int ini = ((numFila * getAltoFilaReal()) + getIniCabReal()) * getAnchoImagen();
//        int i = 0;
//        for (; i < fila.length && ini < getPix().length; ini++) {
//            int posX = (ini % getAnchoImagen());
//            if (posX >= iniCol) {
//                fila[i] = getPix()[ini];
//                i++;
//            }
//        }
//        if (getConf().isMostrarMarcas()) {
//            int iniFila = getIniCabReal() + (numFila * getAltoFilaReal());
//            getGraficos().setColor(Color.red);
//            getGraficos().drawRect(iniCol, iniFila, ancho, getAltoFilaReal());
//        }
//        // mostrarImagen(ancho, getProporcionado(ALTO_FILA), fila);
//        return fila;
//    }
    private int[] getFilaX(int y, int alto) {
        // Una vez calculados los margenes necesitamos las disntintas filas de
        // alumnos, nos saltamos la parte del nombre
        int iniCol = getIniColReal();
        int ancho = getAnchoImagen() - iniCol;
        int[] fila = new int[ancho * alto];
        int ini = y * getAnchoImagen();
        int i = 0;
        for (; i < fila.length && ini < getPix().length; ini++) {
            int posX = (ini % getAnchoImagen());
            if (posX >= iniCol) {
                fila[i] = getPix()[ini];
                i++;
            }
        }
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.YELLOW);
            getGraficos().drawRect(iniCol, ini, ancho, alto);
        }
        return fila;
    }

    private int[] getFilaPie() {
        //double margenMarca = getMarcaIzquierda().getY();
        double iniFila = getIniPieReal();
        int iniCol = getIniColReal();
        int ancho = getAnchoImagen() - iniCol;
        int[] fila = new int[ancho * (getProporcionado(getConf().getAltoBloqueFirmas()))];
        int ini = (int) (iniFila * getAnchoImagen());
        int i = 0;
        while (i < fila.length && ini < getPix().length) {
            int posX = (ini % getAnchoImagen());
            if (posX >= iniCol) {
                fila[i] = getPix()[ini];
                i++;
            }
            ini++;
        }
        System.out.println("INICIO PIE:" + iniFila + " (" + getProporcion() + ")");
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.ORANGE);
            getGraficos().drawRect(iniCol, (int) iniFila, ancho, getProporcionado(getConf().getAltoBloqueFirmas()));
        }
        return fila;
    }

    public int getIniPieReal() {
        return iniPieReal;
    }

    public void setIniPieReal(int iniPieReal) {
        this.iniPieReal = iniPieReal;
    }

    public int[] getTiraImagen(int x, int y, int ancho, int alto) {

        int[] fila = new int[ancho * alto];
        int ini = (y * getAnchoImagen());
        int i = 0;
        while (i < fila.length && ini < getPix().length) {
            int posX = (ini % getAnchoImagen());
            if (posX >= x) {
                fila[i] = getPix()[ini];
                i++;
            }
            ini++;
        }
        System.out.println("TIRA:" + y + " (" + getProporcion() + ") x " + alto);
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.ORANGE);
            getGraficos().drawRect(x, y, ancho, getProporcionado(getConf().getAltoBloqueFirmas()));
        }
        return fila;
    }

    private int getProporcionado(double dato) {
        return (int) (dato * getProporcion());
    }

    private boolean leerCodigoDeBarras() {
        return leerCodigoDeBarras(0);
    }

    private boolean leerCodigoDeBarras(int intento) {
        //TODO Parametrizar e ajuste de márgenes y el número de intentos
        firePropertyChange("message", null, "Cargando imagen...");
        boolean ret = false;
        BufferedImage im = getImagenNoModificada();
        int alto = im.getHeight() / (10 + intento);
        int margen = 0;
        int margenSup = 0;
        if (intento > 0) {
            margen = im.getWidth() / (10 - intento);
            margenSup = alto / (10 - intento);
        }
        int subImgWidth = im.getWidth() - (margen * 2);
        int subImgHeight = alto - (margenSup * 2);
        if (subImgWidth > 0 && subImgHeight > 0) {
            //Ahora creamos una subimagen de la parte superior
            BufferedImage subImg = im.getSubimage(margen, margenSup, subImgWidth, subImgHeight);
            //Visor.mostrarImagen(subImg);
            //MonochromeBitmapSource source = new BufferedImageMonochromeBitmapSource();
            LuminanceSource source = new BufferedImageLuminanceSource(subImg);
            BinaryBitmap bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            Hashtable<DecodeHintType, Object> hints = null;
            hints = new Hashtable<DecodeHintType, Object>();
            Vector<BarcodeFormat> formatos = new Vector<BarcodeFormat>();
            formatos.add(BarcodeFormat.CODE_128);
            hints.put(DecodeHintType.POSSIBLE_FORMATS, formatos);
            hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
            firePropertyChange("message", null, "Procesando código de barras (" + intento + ")...");
            try {
                Result result = new MultiFormatReader().decode(bitmap, hints);
                ret = true;
                setCodigoBarras(result.getText());
            } catch (ReaderException ex) {
                subImg = null;
                subImg = im.getSubimage(margen, im.getHeight() - (alto - (margenSup * 2)), im.getWidth() - (margen * 2), alto - (margenSup * 2));
                //Visor.mostrarImagen(subImg);
                //source = new BufferedImageMonochromeBitmapSource(subImg);
                source = new BufferedImageLuminanceSource(subImg);
                bitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
                try {
                    Result result = new MultiFormatReader().decode(bitmap, hints);
                    setCodigoBarras(result.getText());
                    ret = true;
                    setGirarImagen(true);
                } catch (ReaderException ex1) {
                    Logger.getLogger(DigitalizacionParte.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            subImg = null;
            System.gc();
            if (!ret && intento < 5) {
                ret = leerCodigoDeBarras(intento + 1);
            }
        }
        return ret;
    }

    /**
     * Convierte la imagen a un array de pixeles asignado los valores de ancho, alto y proporcion de la imagen
     * @return
     * @throws java.lang.InterruptedException
     */
    public boolean scanImage() throws InterruptedException {
        firePropertyChange("message", null, "Procesando imagen...");
        setGraficos(null);
        //setMarcaIzquierda(null);
        BufferedImage img = getImagen();
        int iniAncho = img.getWidth(null);
        int iniAlto = img.getHeight(null);
        setProporcion(iniAncho / (getConf().getAnchoParte()));
        setPix(new int[iniAncho * iniAlto]);
        setAltoImagen(iniAlto);
        setAnchoImagen(iniAncho);
        PixelGrabber pgObj = new PixelGrabber(img, 0, 0, iniAncho, iniAlto, getPix(), 0, iniAncho);
        return pgObj.grabPixels();
    }

    public final BufferedImage cargarImagen(String f) throws IOException {
        BufferedImage input = ImageIO.read(new File(f));
        setImagenNoModificada(input);
        if (getConf().isMostrarMarcas()) {
            //Conertimos la imagen a color por si acaso
            int w = input.getWidth(null);
            int h = input.getHeight(null);
            BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bi.createGraphics();
            g.drawImage(input, 0, 0, null);
            g.dispose();
            input = null;
            input = bi;
        }
        return input;
    }

    @Override
    public String toString() {
        ArrayList<DatoDigitalizacion> d = getDatos();
        StringBuilder sb = new StringBuilder("Parte: " + getCodigoBarras() + "\n");
        for (int i = 0; i < d.size(); i++) {
            sb.append("FILA: ").append(Str.lPad(i, 2)).append(" | ");
            ArrayList<Integer> fs = d.get(i).getColumnas();
            for (int x : fs) {
                sb.append(" ");
                sb.append(Str.lPad(x, 2));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private void log(String txt) {
        if (getConf().isMostrarLogs()) {
            System.out.println(txt);
        }
    }

    private void mostrarMarcasBloque(int iniCol, int x, int margenLateral, int margenSuperior, FilaDig fila, int nuevoAnchoCasilla) {
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.ORANGE);
            getGraficos().drawRect(iniCol + getProporcionado(getConf().getAnchoColumna() * x) + margenLateral, fila.getInicio() + margenSuperior, nuevoAnchoCasilla, fila.getAlto() - (margenSuperior * 2));
        }
    }

    private void mostrarMarcasBloquePie(int iniCol, int x, int margen, int inicioPie, int nuevoAnchoCasilla) {
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.ORANGE);
            getGraficos().drawRect(iniCol + getProporcionado(getConf().getAnchoBloqueFirmas() * x) + margen, inicioPie + margen, nuevoAnchoCasilla, getProporcionado(getConf().getAltoBloqueFirmas()) - (margen * 2));
        }
    }

    private void mostrarMarcasLinea(int iniCol, int offSet, FilaDig fila, int anchoBloque) {
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.green);
            getGraficos().drawRect(iniCol + offSet, fila.getInicio(), anchoBloque, fila.getAlto());
        }
    }

    private void mostrarMarcasLineaPie(int iniCol, int offSet, int inicioPie, int anchoBloque) {
        if (getConf().isMostrarMarcas()) {
            getGraficos().setColor(Color.PINK);
            getGraficos().drawRect(iniCol + offSet, inicioPie, anchoBloque, getProporcionado(getConf().getAltoBloqueFirmas()));
        }
    }

    private void procesarPie() {
        int[] filaPie = getFilaPie();
        int anchoBloque = getProporcionado(getConf().getAnchoBloqueFirmas());
        int iniCol = getIniColReal();
        int ancho = getAnchoImagen() - iniCol;
        int inicioPie = getIniPieReal();//getProporcionado(getConf().getInicioPie());
        log("BLOQUES PIE DE :" + anchoBloque / getProporcion());
        for (int x = 0; x < getConf().getNumCasillasPie(); x++) {
            firePropertyChange("message", null, "Analizando pie, casilla " + (x + 1) + "...");
            int offSet = getProporcionado((getConf().getAnchoBloqueFirmas()) * x);
            int[] bloque = getBloque(anchoBloque, offSet, filaPie, ancho);
            mostrarMarcasLineaPie(iniCol, offSet, inicioPie, anchoBloque);
            int margen = getProporcionado(getConf().getMargenLimpiezaPie());
            bloque = UtilImgParte.matrizToArray(UtilImgParte.extraerRectanguloDeMatriz(UtilImgParte.arrayToMatriz(bloque,
                    anchoBloque), margen,
                    margen,
                    anchoBloque - (margen * 2),
                    getProporcionado(getConf().getAltoBloqueFirmas()) - (margen * 2)));
            int nuevoAnchoCasilla = anchoBloque - (margen * 2);
            int porcent = getPorcentajeNegro(bloque);
            int firmado = porcent >= getConf().getPorcentajeNegroFirmado() ? 1 : 0;
            getPie().addColumna(firmado);
            log("PIE CASILLA :" + x + " POR:" + porcent);
            mostrarMarcasBloquePie(iniCol, x, margen, inicioPie, nuevoAnchoCasilla);
        }
    }
}
