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
package com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.util.EntityUtils;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class GestorEnvioFaltas extends MaimonidesBean {

    ClienteSeneca cli = null;
    static int MAX_INTENTOS_VERIFICAR_ESTADO = 0;//TODO Hacer esto configuarble
    static int POS_ESTADO = 3;
    static int POS_ERROR = 5;
    static int POS_COD = 8;
    public static int RET_ERROR_ENVIANDO = -1;
    public static int RET_ERROR_PROCESANDO = 0;
    public static int RET_OK = 1;
    boolean cancelado = false;

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
    }

    public ClienteSeneca getCli() {
        return cli;
    }

    public final void setCli(ClienteSeneca cli) {
        this.cli = cli;
    }

    public GestorEnvioFaltas(ClienteSeneca cli) {
        setCli(cli);
    }

    public ArrayList<ResultadoEnvioFicheroFaltas> getResultadosEnvioFicheros(String str) {
        //Ahora tenemos que buscar las lineas de identificación
        firePropertyChange("message", null, "Recuperando resultados de envío de ficheros de faltas...");
        ArrayList<ResultadoEnvioFicheroFaltas> lineas = new ArrayList<ResultadoEnvioFicheroFaltas>();
        if (Str.noNulo(str).equals("")) {
            Logger.getLogger(GestorEnvioFaltas.class.getName()).warning("Se ha recibido una página de resultados vacía o nula!.");
            return lineas;
        }
        Scanner sc = new Scanner(str);
        while (sc.hasNextLine()) {
            String linea = sc.nextLine().toLowerCase();
            System.out.println(linea);
            String busqueda1 = "<a  HREF=\"javascript:showOpciones(".toLowerCase();
            String busqueda2 = "<INPUT TYPE=\"HIDDEN\" NAME=OpEmergente".toLowerCase();
            if (linea.startsWith(busqueda1)) {
                //Estamos ante una nueva linea
                //Ahora vamos contando los font
                int fCount = 0;
                int margen = "<FONT  FD_TIPO_FORMATEADOR=\"FD_EN_LINEAS\" >".length();
                //El código tras el showopcines es el número de identificacion para mas adelante
                int num = Num.getInt(linea.substring(busqueda1.length(), linea.indexOf(")", busqueda1.length() + 1)));
                if (num > 0) {
                    ResultadoEnvioFicheroFaltas lts = new ResultadoEnvioFicheroFaltas();
                    lts.setPos(num);
                    while (sc.hasNextLine()) {
                        String linea2 = sc.nextLine().toLowerCase();
                        if (linea2.startsWith("<TD NOWRAP  CLASS=\"cuerpo".toLowerCase())) {
                            //Entonces nos interesa la siguiente linea
                            linea2 = sc.nextLine().toLowerCase();
                            fCount++;
                            System.out.println(fCount + ":" + linea2);
                            if (fCount == 3) {
                                lts.setResultado(linea2.substring(margen, linea2.lastIndexOf("</FONT>".toLowerCase())));
                            } else if (fCount == 5) {
                                lts.setError(linea2.replace("</TD>".toLowerCase(), "").replace("<br>", " \n").replace("&nbsp;", " "));
                            } else if (fCount == 9) {
                                lts.setNombre(linea2.substring(margen, linea2.lastIndexOf("</FONT>".toLowerCase())));
                            }
                            if (fCount >= Num.max(POS_COD, POS_ERROR, POS_ESTADO, 9).intValue()) {
                                break;
                            }
                        }
                    }
                    lineas.add(lts);
                }
            } else if (linea.startsWith(busqueda2)) {
                String busq1 = "VALUE=\"".toLowerCase();
                int posBusq1 = linea.indexOf(busq1);
                //Ahora calculamos el identificador
                int pos = Num.getInt(linea.substring(busqueda2.length(), posBusq1).trim());
                String params = linea.substring(posBusq1 + busq1.length(), linea.lastIndexOf(">") - 1);
                //Buscamos la linea y le asignamos los parametros
                for (ResultadoEnvioFicheroFaltas l : lineas) {
                    if (l.getPos() == pos) {
                        l.setParams(params);
                        break;
                    }
                }
            }
        }
        return lineas;
    }

    public int limpiarTodosLosResultadosEnvioDeFicheros() {
        int total = 0;
        int count = 0;
        firePropertyChange("message", null, "Limpiando resultados de envío de ficheros de faltas...");
        ArrayList<ResultadoEnvioFicheroFaltas> lineas = getResultadosEnvioFicheros(getPaginaResultadoEnvios());
        //Limitamos el borrado a 20 cargas
        while (lineas.size() > 0 && count < 20 && !isCancelado()) {
            count++;
            for (ResultadoEnvioFicheroFaltas l : lineas) {
                if (l.borrar(getCli())) {
                    total++;
                    firePropertyChange("message", null, "Limpiando resultados de envío de ficheros de faltas (" + total + ")...");
                } else {
                    firePropertyChange("message", null, "No se ha podido borrar la información de envío " + l.getNombre());
                }
            }
            lineas = getResultadosEnvioFicheros(getPaginaResultadoEnvios());
        }
        return total;
    }

    public String getPaginaResultadoEnvios() {
        return getPaginaResultadoEnvios(1);
    }

    public String getPaginaResultadoEnvios(int intento) {
        Logger.getLogger(GestorEnvioFaltas.class.getName()).log(Level.INFO, "Intentando recuperar p\u00e1gina de resultados de env\u00edos. Intento {0}", intento);
        if (intento > 5) {
            return null;
        }
        try {
            if (getCli().hacerLogin()) {
                //tenemos que esperar hasta x minutos leyendo lo siguiente para ver si esta ok
                //Si esta ok borrarlo si no moverlo
                firePropertyChange("message", null, "Recuperando página de resultados de envío de ficheros de faltas...");
                HttpGet get = new HttpGet(ClienteSeneca.getUrlBase() + "Principal.jsp?rndval=578955782&COD_PAGINA=5004863&C_SENINT=I&X_TIPINTINF=6&X_CENTRO=&N_V_=" + getCli().getNombreVentana());// + "&COD_PAGINA_ANTERIOR=5004863&TIEMPO_PAGINA_ANTERIOR=1532&TIEMPO_PAGINA_ANTERIOR_CON_BOTONERA=2068");
                HttpResponse response = getCli().getCliente().execute(get);
                String txt = EntityUtils.toString(response.getEntity());
                if (getCli().isOk(response, txt)) {
                    return txt;
                } else {
                    return getPaginaResultadoEnvios(++intento);
                }
            } else {
                return getPaginaResultadoEnvios(++intento);
            }
        } catch (IOException ex) {
            Logger.getLogger(GestorEnvioFaltas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private boolean isEnvioRealizadoConExito(String nombre) {
        boolean ok = false;
        try {
            //Tenemos que esperar hasta obtener los resultados del envío
            boolean continuar = true;
            int intentos = 0;
            while (continuar && !isCancelado()) {
                intentos++;
                Logger.getLogger(GestorEnvioFaltas.class.getName()).log(Level.INFO, "Intento de verificar estado: {0}", intentos);
                //Sacamos el listado de resultados
                ArrayList<ResultadoEnvioFicheroFaltas> lineas = getResultadosEnvioFicheros(getPaginaResultadoEnvios());
                //Buscamos el resultado que nos piden
                ResultadoEnvioFicheroFaltas r = null;
                for (ResultadoEnvioFicheroFaltas res : lineas) {
                    if (res.getNombre().trim().equals(("MM:" + nombre).toLowerCase())) {
                        r = res;
                        break;
                    }
                }
                if (r == null) {
                    //Si no está el envío ni siquiera procesando es que algo a ido mal
                    continuar = false;
                    firePropertyChange("message", nombre, "No se encuentra el resultado del envío. Puede que no se haya realizado correctamente.");
                    firePropertyChange("error", nombre, "No se encuentra el resultado del envío. Puede que no se haya realizado correctamente.");
                } else {
                    //Tenemos que ver el estado del envío
                    if (r.getEstado() == ResultadoEnvioFicheroFaltas.ESTADO_PROCESANDO) {
                        firePropertyChange("message", nombre, "El fichero está siendo procesado por Séneca...");
                        try {
                            Thread.sleep(6000);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(GestorEnvioFaltas.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else if (r.getEstado() == ResultadoEnvioFicheroFaltas.ESTADO_EXITO) {
                        firePropertyChange("message", nombre, "Fichero enviado correctamente. Eliminando linea de envío...");
                        r.borrar(getCli());
                        ok = true;
                        continuar = false;
                    } else {
                        firePropertyChange("message", nombre, "Se ha producido un error enviando el fichero a Séneca. Revise la web para verificarlo.");
                        firePropertyChange("error", nombre, "<html>Se ha producido un error enviando el fichero a Séneca:<br/><b>'" + r.getError() + "'</b><br/>El código de envío es:<br/><b>" + r.getNombre() + "</b><br/><br/>");
                        continuar = false;
                    }
                }
                if ((MAX_INTENTOS_VERIFICAR_ESTADO != 0 && intentos > MAX_INTENTOS_VERIFICAR_ESTADO) && continuar) {
                    firePropertyChange("message", nombre, "Se ha superado el tiempo de espera para verificar si el fichero está enviado. Revise la web para verificarlo.");
                    firePropertyChange("error", nombre, "Se ha superado el tiempo de espera para verificar si el fichero está enviado.\nRevise la web para verificarlo.\nEl código de envío es: " + r.getNombre() + "");
                    continuar = false;
                }
            }
            if (isCancelado()) {
                firePropertyChange("message", nombre, "Se ha cancelado manualmente la operación.");
            }
        } catch (Exception e) {
            Logger.getLogger(GestorEnvioFaltas.class.getName()).log(Level.SEVERE, null, e);
        }
        return ok;
    }

    public int enviarFicheroSeneca(File fichero, String descripcion) {
        //fichero=new File("prueba.xml");
        int ok = RET_ERROR_ENVIANDO;
        try {
            //Ahora enviamos el fichero
            getCli().visitarURL("Principal.jsp?rndval=470649490&COD_PAGINA=5005684&MODO=NUEVO&X_TIPINTINF=6&N_V_=" + getCli().getNombreVentana());
            String captcha = getCli().getCaptcha();
            if (captcha == null) {
                ok = RET_ERROR_ENVIANDO;
            } else {
                //Y enviamos el fichero
                String url = ClienteSeneca.getUrlBase() + "Principal.jsp?rndval=77730635&COD_PAGINA=5005364&TIPO_PARAMETROS_PETICION=MULTIPART_FORMDATA&TAM_MAXIMO_FICHERO_UPLOAD=5M&N_V_=" + getCli().getNombreVentana() + "&PAG_NO_VISIBLE_=S";//"&COD_PAGINA_ANTERIOR=5005684&TIEMPO_PAGINA_ANTERIOR=1092&TIEMPO_PAGINA_ANTERIOR_CON_BOTONERA=1347";
                HttpPost post = new HttpPost(url);
                //post.addRequestHeader("Referer", r5);
                String ano = MaimonidesApp.getApplication().getAnoEscolar().getAno() + "";
                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                FileBody bin = new FileBody(fichero);
                reqEntity.addPart("RUTA_FICHERO", (ContentBody) bin);
                reqEntity.addPart("KAPTCHA", (ContentBody) new StringBody(captcha+"asd"));
                //reqEntity.addPart("X_TIPINTINF", (ContentBody) new StringBody("6"));
                reqEntity.addPart("F_INTINF", (ContentBody) new StringBody(Fechas.format(new GregorianCalendar(), "dd/MM/yyyy hh:mm")));
                reqEntity.addPart("C_SENINT", (ContentBody) new StringBody(""));
                reqEntity.addPart("CHECKSUM_", (ContentBody) new StringBody(ano + "|"));
                //reqEntity.addPart("CHECKSUM_", (ContentBody) new StringBody(""));
                reqEntity.addPart("C_ANNO", (ContentBody) new StringBody(ano + ""));
                reqEntity.addPart("T_OBSERV", (ContentBody) new StringBody("MM:" + URLEncoder.encode(descripcion, "latin1")));
                
                post.setEntity(reqEntity);
                HttpResponse response = getCli().getCliente().execute(post);
                String texto = EntityUtils.toString(response.getEntity());
                if (getCli().isDebugMode()) {
                    System.out.println(post.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + texto);
                }
                if (getCli().isOk(response, texto)) {
                    //Vemos si ha habido error
                    if (texto.toLowerCase().contains("PAG_SALTO=PaginaError.jsp".toLowerCase())) {
                        String error = getCli().getURL("PaginaError.jsp?ALEATORIO=" + Math.floor(Math.random() * 1000));
                        if (error.toLowerCase().contains("El texto de la imagen introducido es".toLowerCase())) {
                            JOptionPane.showMessageDialog(MaimonidesApp.getApplication().getMainFrame(), "El captcha introducido no es correcto. Inténtelo de nuevo.", "Error en captcha", JOptionPane.ERROR_MESSAGE);
                            //Si hay fallo del captcha volvemos a intentarlo
                            ok=enviarFicheroSeneca(fichero, descripcion);
                        } else {
                            ok=RET_ERROR_PROCESANDO;
                        }
                    } else {
                        //texto=getCli().getURL("PaginaVuelta.jsp?ALEATORIO="+ Math.floor(Math.random() * 1000));
                        //texto=getCli().getURL("Principal.jsp?EN_RECARGA_=S&COD_PAGINA=5005684&ALEATORIO="+ Math.floor(Math.random() * 1000)+"&N_V_=IGNORAR_NOMBRE");
                        //Tenemos que ver que la operación se haya realizado correctamente
                        ok = isEnvioRealizadoConExito(descripcion) ? RET_OK : RET_ERROR_PROCESANDO;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ok;
    }
}
