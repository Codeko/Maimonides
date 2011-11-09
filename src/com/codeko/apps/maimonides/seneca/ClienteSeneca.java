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
package com.codeko.apps.maimonides.seneca;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Dependencia;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.TramoHorario;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.seneca.operaciones.envioFicherosFaltas.GestorEnvioFaltas;
import com.codeko.apps.maimonides.usr.Rol;
import com.codeko.apps.maimonides.usr.Usuario;
import com.codeko.util.Archivo;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.codeko.util.estructuras.Par;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;

import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.jdesktop.application.Task;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
//TODO Implementar que todas las respuestas se revisen y se busquen cadenas como 'excecido tiempo de inactividad y esas cosa
public class ClienteSeneca extends MaimonidesBean {

    private static ScriptEngine engine = null;
    final static String PERFIL_DIRECCION = "Dirección";
    final static String PERFIL_PROFESOR = "Profesorado";
    final static int COD_PERFIL_DIRECCION = 161;
    final static int COD_PERFIL_PROFESOR = 2;
    boolean loggeado = false;
    String usuario = null;// "gr7693gr";
    String clave = null;// "gr7693gr";
    String claveCodificada = null;
    HttpClient cliente = null;
    private String nombreVentana = "NV_4737";
    private static final String URL_BASE = "https://www.juntadeandalucia.es/educacion/seneca/seneca/jsp/";//"https://seneca.ced.junta-andalucia.es/seneca/jsp/";
    private static String urlBase = null;//"https://www.juntadeandalucia.es/educacion/seneca/seneca/jsp/";
    boolean debugMode = false;
    String ultimoError = "";
    Exception ultimaExcepcion = null;
    String perfilActivo = null;

    public ClienteSeneca(String usuario, String clave) {
        setUsuario(usuario);
        setClave(clave);
    }

    public String getPerfilActivo() {
        return perfilActivo;
    }

    public void setPerfilActivo(String perfilActivo) {
        this.perfilActivo = perfilActivo;
    }

    public static String getUrlBase() {
        if (urlBase == null) {
            urlBase = Preferences.userNodeForPackage(MaimonidesApp.class).get("SENECA_URL_BASE", URL_BASE);
        }
        return urlBase;
    }

    public static void setUrlBase(String urlBase) {
        ClienteSeneca.urlBase = urlBase;
        Preferences.userNodeForPackage(MaimonidesApp.class).put("SENECA_URL_BASE", urlBase);
    }

    public Exception getUltimaExcepcion() {
        return ultimaExcepcion;
    }

    public void setUltimaExcepcion(Exception ultimaExcepcion) {
        this.ultimaExcepcion = ultimaExcepcion;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }

    public String getNombreVentana() {
        return nombreVentana;
    }

    public boolean isLoggeado() {
        return loggeado;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    private void regenerarNombreVentana() {
        //Random generator = new Random();
        //"IGNORAR_NOMBRE";//
        nombreVentana = "IGNORAR_NOMBRE";//"NV_" + generator.nextInt(10) + "" + generator.nextInt(10) + "" + generator.nextInt(10) + "" + generator.nextInt(10);
    }

    private void setLoggeado(boolean loggeado) {
        boolean old = this.loggeado;
        this.loggeado = loggeado;
        if (loggeado) {
            firePropertyChange("message", null, "Acceso a Séneca realizado con éxito.");
        } else if (old & !loggeado) {
            firePropertyChange("message", null, "Salida de Séneca realiza con éxito.");
        } else {
            firePropertyChange("message", null, "No se ha podido acceder a Séneca.");
        }
    }

    private String getClave() {
        return clave;
    }

    public final void setClave(String clave) {
        this.clave = clave;
    }

    public String getClaveCodificada() {
        if (claveCodificada == null) {
            try {
                if (engine == null) {
                    String js = Archivo.getContenido(this.getClass().getResource("resources/cifrado.js"), "latin1");
                    ScriptEngineManager manager = new ScriptEngineManager();
                    engine = manager.getEngineByName("js");
                    engine.eval(js);
                }
                claveCodificada = engine.eval("cifrar('" + getClave() + "');").toString();
            } catch (Exception ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return claveCodificada;
    }

    public void cerrarCliente() {
        if (cliente != null) {
            cliente.getConnectionManager().shutdown();
            cliente = null;
        }
    }

    public HttpClient getCliente() {
        if (cliente == null) {
            try {
                //String sUrl = getUrlBase() + "IdenUsu.jsp?CON_PRUEBA=N&N_V_=" + getNombreVentana() + "&rndval=812273405&NAV_WEB_NOMBRE=Netscape&NAV_WEB_VERSION=5&RESOLUCION=800";
                //URL url = new URL(sUrl);
                //InstaladorCertificados.instalar(url);
                //TODO Esto es una alternativa a la instalación de certificados
                SSLContext ctx = SSLContext.getInstance("TLS");
                X509TrustManager tm = new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                };
                ctx.init(null, new TrustManager[]{tm}, null);
                SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                SchemeRegistry schemeRegistry = new SchemeRegistry();
                schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
                schemeRegistry.register(new Scheme("https", 443, ssf));
                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (X11; U; Linux i686; es-ES; rv:1.9.2.12) Gecko/20101027 Ubuntu/10.04 (lucid) Firefox/3.6.12");
                ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(schemeRegistry);
                cm.setDefaultMaxPerRoute(10);
                cm.setMaxTotal(20);
                HttpConnectionParams.setConnectionTimeout(params, 1000);
                cliente = new DefaultHttpClient(cm, params);

            } catch (Exception ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
                setUltimoError("Error conectando con Séneca: " + ex.getLocalizedMessage());
                setUltimaExcepcion(ex);
            }
        }
        ThreadSafeClientConnManager man = (ThreadSafeClientConnManager) cliente.getConnectionManager();
        man.closeExpiredConnections();
        man.closeIdleConnections(10, TimeUnit.SECONDS);
        Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Hay {0} conexiones abiertas.", man.getConnectionsInPool());
        return cliente;
    }

    public String getUsuario() {
        return usuario;
    }

    public final void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public void hacerLogout() {
        try {
            HttpGet get = new HttpGet(getUrlBase() + "Logout.jsp");
            HttpResponse response = getCliente().execute(get);
            if (isDebugMode()) {
                System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + EntityUtils.toString(response.getEntity()));
            }
            setLoggeado(false);
            get.abort();
            cerrarCliente();
        } catch (Exception ex) {
            Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean hacerLogin() {
        return hacerLogin(1);
    }

    private String cargarDatosUsuarioActual() throws IOException {

        firePropertyChange("message", null, "Cargando datos de usuario");
        HttpGet get = new HttpGet(getUrlBase() + "BarraNavegacion.jsp");
        Logger.getLogger(ClienteSeneca.class.getName()).info("Cargando datos de usuario.");
        HttpResponse response = getCliente().execute(get);
        String texto = EntityUtils.toString(response.getEntity());
        Source source = new Source(texto);
        Element el = source.getElementById("usuario");
        if (el != null) {
            String content = el.getContent().getTextExtractor().toString();
            return content.substring(0, content.indexOf(")"));
        }
        return "";
    }

    private void cargarPerfil() throws UnsupportedEncodingException, IOException {
        firePropertyChange("message", null, "Cargando perfil " + getPerfilActivo());
        //Lo seleccionamos
        HttpPost post = new HttpPost(getUrlBase() + "PuestosOrigenPerfil.jsp?D_PERFIL=" + URLEncoder.encode(getPerfilActivo(), "UTF-8") + "&rndval=72277307");
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("CPERFILES", (getPerfilActivo().equals(PERFIL_DIRECCION) ? COD_PERFIL_DIRECCION : COD_PERFIL_PROFESOR) + ""));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Seleccionando perfil {0}", getPerfilActivo());
        HttpResponse response = getCliente().execute(post);
        String texto = EntityUtils.toString(response.getEntity());
        if (isDebugMode()) {
            System.out.println(post.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + texto);
        }
        if (isOk(response, texto, false)) {
            //Hacemos esto porque se envía la fecha de toma de posesión y no la sabemos
            String src = "document.location.replace('CargarPerfil.jsp?".toLowerCase();
            int posIni = texto.toLowerCase().indexOf(src) + src.length();
            int posFin = texto.indexOf("')", posIni);
            String qs = texto.substring(posIni, posFin);
            HttpGet get = new HttpGet(getUrlBase() + "CargarPerfil.jsp?" + qs);
            Logger.getLogger(ClienteSeneca.class.getName()).info("Cargando perfil de Séneca.");
            response = getCliente().execute(get);
            texto = EntityUtils.toString(response.getEntity());
            if (isDebugMode()) {
                System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + texto);
            }
            setLoggeado(isOk(response, texto, false));
        }
    }

    private void seleccionarPerfil() throws UnsupportedEncodingException, IOException {
        firePropertyChange("message", null, "Cargando lista de perfiles");
        //Ahora tenemos que elegir el perfil
        HttpPost post = new HttpPost(getUrlBase() + "Perfiles.jsp?rndval=72277307");
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("USUARIO", getUsuario()));
        post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        Logger.getLogger(ClienteSeneca.class.getName()).info("Cargando lista de perfile de Séneca.");
        HttpResponse response = getCliente().execute(post);
        String texto = EntityUtils.toString(response.getEntity());
        if (isDebugMode()) {
            System.out.println(post.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + texto);
        }
        if (isOk(response, texto, false)) {
            //tenemos que seleccionar el perfil activo si no existe
            if (texto.indexOf(PERFIL_DIRECCION) != -1) {
                setPerfilActivo(PERFIL_DIRECCION);
            } else if (texto.indexOf(PERFIL_PROFESOR) != -1) {
                setPerfilActivo(PERFIL_PROFESOR);
            } else {
                setPerfilActivo(null);
            }

            if (getPerfilActivo() != null) {
                cargarPerfil();
            } else {
                setLoggeado(false);
                setUltimoError("No se ha podido seleccionar el perfil '" + getPerfilActivo() + "'.");
                setUltimaExcepcion(null);
            }
        } else {
            setLoggeado(false);
            setUltimoError("No se ha podido seleccionar el perfil de usuario.");
            setUltimaExcepcion(null);
        }

    }

    public boolean hacerLogin(int intento) {
        if (!isLoggeado()) {
            if (getCliente() != null) {
                try {
                    regenerarNombreVentana();
                    HttpResponse response=null;
                    String texto=null;
                    firePropertyChange("message", null, "Iniciando sesión en Séneca.");
                    //Abrimos la web de login-> 09/11/2011 nos lo podemos saltar ahora que se pasan estos datos mediante el form de login
//                    HttpGet get = new HttpGet(getUrlBase() + "IdenUsuExt.jsp?CON_PRUEBA=N&N_V_=" + getNombreVentana() + "&rndval=812273405&NAV_WEB_NOMBRE=Netscape&NAV_WEB_VERSION=5&RESOLUCION=800");
//                    Logger.getLogger(ClienteSeneca.class.getName()).info("Abriendo pantalla de login.");
//                    response = getCliente().execute(get);
//                    texto = EntityUtils.toString(response.getEntity());
//                    if (isDebugMode()) {
//                        System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + texto);
//                    }
                    //Simulamos el envío del formulario
                    String urlLogin = getUrlBase() + "ComprobarUsuarioExt.jsp";
                    HttpPost post = new HttpPost(urlLogin);
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                    nameValuePairs.add(new BasicNameValuePair("CLAVECIFRADA", getClaveCodificada()));//TODO Esto tarda demasido
                    nameValuePairs.add(new BasicNameValuePair("CLAVE", getClave()));
                    nameValuePairs.add(new BasicNameValuePair("USUARIO", getUsuario()));
                    nameValuePairs.add(new BasicNameValuePair("C_INTERFAZ", "SENECA"));
                    nameValuePairs.add(new BasicNameValuePair("NAV_WEB_NOMBRE", "Netscape"));
                    nameValuePairs.add(new BasicNameValuePair("NAV_WEB_VERSION", "5"));
                    nameValuePairs.add(new BasicNameValuePair("RESOLUCION", "1024"));
                    nameValuePairs.add(new BasicNameValuePair("rndval", "831815881"));
                    nameValuePairs.add(new BasicNameValuePair("N_V_", getNombreVentana()));
                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    Logger.getLogger(ClienteSeneca.class.getName()).info("Haciendo login en Séneca.");
                    response = getCliente().execute(post);
                    texto = EntityUtils.toString(response.getEntity());
                    setLoggeado((response.getStatusLine().getStatusCode() == 200 && texto.indexOf("IdenUsu.jsp?INCORRECTO") == -1));
                    if (isLoggeado()) {
                        //TODO Por alguna razon esto falla siempre la primera vez y funciona la segunda
                        seleccionarPerfil();
                    } else {
                        setUltimoError("No se ha podido hacer login: Usuario o clave no válidos.");
                        setUltimaExcepcion(null);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
                    setUltimoError("No se ha podido hacer login: " + ex.getLocalizedMessage());
                    setUltimaExcepcion(ex);
                }
            }
        }
        boolean ok = isLoggeado();
        if (!ok && intento < 3) {
            intento++;
            return hacerLogin(intento);
        } else {
            return ok;
        }
    }

    public boolean isOk(HttpResponse response, String txt) throws IOException {
        return isOk(response, txt, true);
    }

    public boolean isOk(HttpResponse response, String txt, boolean reintentarLogin) throws IOException {
        if (txt == null) {
            txt = EntityUtils.toString(response.getEntity());
        }

        boolean ok = (response.getStatusLine().getStatusCode() == 200) && txt.toLowerCase().indexOf("error durante la conexión") == -1 && txt.toLowerCase().indexOf("Pagina de Gestion de Errores Durante la Conexión") == -1;
        if (reintentarLogin && (!ok || txt.toLowerCase().indexOf("excedido tiempo de inactividad") != -1)) {
            //Esto es que nos hemos salido volvemos a hacer login
            setLoggeado(false);
            hacerLogin();
            ok = false;
        }
        return ok;
    }

    public File getArchivoDatosExtendidosAlumnado() {
        File f = null;
        if (hacerLogin()) {
            try {
                //La web del listado
                firePropertyChange("message", null, "Descargando fichero Séneca de datos extendidos de alumnado.");
                HttpPost p1 = new HttpPost(getUrlBase() + "Principal.jsp?rndval=180820631&COD_PAGINA=1182&&N_V_=" + getNombreVentana());
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("CHECKSUM_", MaimonidesApp.getApplication().getAnoEscolar().getAno() + "|"));
                nameValuePairs.add(new BasicNameValuePair("C_ANNO", "" + MaimonidesApp.getApplication().getAnoEscolar().getAno()));
                nameValuePairs.add(new BasicNameValuePair("C_NUMESCOLAR", "-1"));
                nameValuePairs.add(new BasicNameValuePair("C_NUMIDE", "-1"));
                nameValuePairs.add(new BasicNameValuePair("DESHABILITAR_C_ANNO", "-1"));
                nameValuePairs.add(new BasicNameValuePair("F_NACIMIENTO", "-1"));
                nameValuePairs.add(new BasicNameValuePair("N_PERIODO", "-1"));
                nameValuePairs.add(new BasicNameValuePair("T_APELLIDO1", "-1"));
                nameValuePairs.add(new BasicNameValuePair("T_APELLIDO2", "-1"));
                nameValuePairs.add(new BasicNameValuePair("T_NOMBRE", "-1"));
                nameValuePairs.add(new BasicNameValuePair("X_OFERTAMATRIC", "-1"));
                p1.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = getCliente().execute(p1);
                String texto = EntityUtils.toString(response.getEntity());
                if (isDebugMode()) {
                    System.out.println(p1.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + texto);
                }
                //La de las consultas de exportación
                visitarURL("PaginaExportacionDatos.jsp?rndval=566364053&N_V_=" + getNombreVentana() + "&COD_PAGINA_ANTERIOR=1182");
                //Luego pedimos los datos
                HttpPost post = new HttpPost(getUrlBase() + "ExportarDatos.jsp");
                //Pasando los siguientes parametros
                nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "NOMBRE"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "ESTADO_MATRICULA"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "C_NUMESCOLAR"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "C_NUMIDE"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "DIRECCION"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "CODIGO_POSTAL"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "LOCALIDAD_RESIDENCIA"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "F_NACIMIENTO"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "PROVINCIA"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "TELEFONO1"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "TELEFONO2"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "CURSO"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "EXPEDIENTE_CENTRO"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "UNIDAD"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "T_APELLIDO1"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "T_APELLIDO2"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "T_NOMBRE"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "DNI_TUT1"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "APE1_TUT1"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "APE2_TUT1"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "NOM_TUT1"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "L_SEXO1"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "DNI_TUT2"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "APE1_TUT2"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "APE2_TUT2"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "NOM_TUT2"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "L_SEXO2"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "LOC_NAC"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "C_ANNO"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "N_MATOMG"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "T_OBSERVAC"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "PROVINCIA_NAC"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "PAIS_NAC"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "EDAD"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "NACIONAL"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "SEXO"));
                nameValuePairs.add(new BasicNameValuePair("FORMATO_EXPORTACION", "EXCEL"));
                nameValuePairs.add(new BasicNameValuePair("TITULO", "ALUMNADO DEL CENTRO"));
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                response = getCliente().execute(post);
                f = File.createTempFile("mm_", ".xls");
                FileOutputStream fos = new FileOutputStream(f);
                InputStream is = response.getEntity().getContent();
                Archivo.copiarArchivo(is, fos);
                Obj.cerrar(fos, is);
            } catch (Exception ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
            hacerLogout();
        } else {
            Logger.getLogger(ClienteSeneca.class.getName()).severe("No se ha podido hacer login");
        }
        return f;
    }

    private void ping() {
        try {
            String[] webs = {
                "Principal.jsp?COD_PAGINA=7&N_V_=IGNORAR_NOMBRE&ALEATORIO=PMPMQWFFLCHKDRFFFFYX", //                "CEC.jsp?ALEATORIO=AEYXYXQWAEYXHKKVPM",
            //                "inferior.jsp?ALEATORIO=FFHKAEAEFFFFAEPMPM",
            //                "PagMenu.jsp?ALEATORIO=FFAEYXQWPMKVGSLCDR",
            //                "PrincipalMulti.jsp?ALEATORIO=YXQWYXGSDRAEKVGS",
            //                "paginaBlanco.html?ALEATORIO=QWKVKVHKYXDRQWHKFF",
            //                "botonera.jsp?ALEATORIO=HKYXDRYXAEDRGSPMDR"
            };
            for (String s : webs) {
                HttpGet get = new HttpGet(getUrlBase() + s);
                HttpResponse response = getCliente().execute(get);
                response.getStatusLine();
                get.abort();
            }
        } catch (IOException ex) {
            Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int actualizarCodigoFaltasSenecaAlumnos(HashMap<String, String> alumnos, Task tarea) {
        int ret = 0;
        if (hacerLogin()) {
            Logger.getLogger(ClienteSeneca.class.getName()).info("Actualizar código faltas: Login OK.");
            try {
                String regExp = "<INPUT TYPE=\"HIDDEN\" NAME=OpEmergente([0-9]+) VALUE=\"C_ANNO=" + MaimonidesApp.getApplication().getAnoEscolar().getAno() + "&X_CENTRO=([0-9]+)&C_PAGINA=DetMatrAlumCent&X_IDENTIFICADOR=([0-9]+)&HEREDAR_ACC=SI&NUM_FILA=([0-9]+)\">";
                Pattern p = Pattern.compile(regExp, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                Iterator<String> it = alumnos.keySet().iterator();
                int count = 0;
                while (it.hasNext()) {
                    if (tarea != null && tarea.isCancelled()) {
                        break;
                    }
                    //Cada 10 registros hacemos ping para no perder la sesión
                    //TODO Implementar en otros métodos largos
                    if ((count % 10) == 0) {
                        ping();
                    }
                    count++;
                    String codSeneca = it.next();
                    Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Actualizar c\u00f3digo faltas: Procesando alumno con c\u00f3digo s\u00e9neca {0}.", codSeneca);
                    firePropertyChange("message", null, "Buscando código de faltas de Séneca para Código:" + codSeneca + "...");
                    String url = getUrlBase() + "Principal.jsp?rndval=308522874&COD_PAGINA=15453&X_ALUMNO=" + codSeneca + "&N_V_=" + getNombreVentana();
                    HttpGet p1 = new HttpGet(url);
                    HttpResponse response = getCliente().execute(p1);
                    String txt = EntityUtils.toString(response.getEntity());
                    if (isDebugMode()) {
                        System.out.println(p1.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + txt);
                    }
                    if (!isOk(response, txt)) {
                        Logger.getLogger(ClienteSeneca.class.getName()).severe("Excedido tiempo de inactividad. Se anula la recepción.");
                        break;
                    }
                    Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Actualizar c\u00f3digo faltas: Petici\u00f3n enviada Ret {0}.", response.getStatusLine().getStatusCode());
                    //buscamos la cadena con el codigo
                    Matcher m = p.matcher(txt);
                    if (m.find()) {
                        String cod = Str.noNulo(m.group(3)).trim();
                        Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Actualizar c\u00f3digo faltas: Se ha encontrado cod: {0}.", cod);
                        if (!cod.equals("")) {
                            alumnos.put(codSeneca, cod);
                            ret++;
                        }
                    } else {
                        Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Actualizar c\u00f3digo faltas: No se ha encontrado codigo en:\n{0}", txt);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
            hacerLogout();
        } else {
            Logger.getLogger(ClienteSeneca.class.getName()).severe("No se ha podido hacer login");
        }
        return ret;
    }

    public int actualizarCodigoSenecaAlumnos(HashMap<String, Integer> alumnos, Task tarea) {
        int ret = 0;
        if (hacerLogin()) {
            Logger.getLogger(ClienteSeneca.class.getName()).info("Actualizar código séneca. Login OK");
            try {
                Pattern p = Pattern.compile("<INPUT TYPE=\"HIDDEN\" NAME=OpEmergente1 VALUE=\"X_ALUMNO=([0-9]+)&HEREDAR_ACC=SI&NUM_FILA=0\">", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
                Iterator<String> it = alumnos.keySet().iterator();
                while (it.hasNext()) {
                    if (tarea != null && tarea.isCancelled()) {
                        break;
                    }
                    String numEscolar = it.next();
                    Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Actualizar c\u00f3digo s\u00e9neca: Buscando c\u00f3digo S\u00e9neca para N\u00ba Escolar:{0}...", numEscolar);
                    firePropertyChange("message", null, "Buscando código Séneca para Nº Escolar:" + numEscolar + "...");
                    HttpPost p1 = new HttpPost(getUrlBase() + "Principal.jsp?rndval=308522874&COD_PAGINA=1182&&N_V_=" + getNombreVentana());
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("CHECKSUM_", MaimonidesApp.getApplication().getAnoEscolar().getAno() + "|"));
                    nameValuePairs.add(new BasicNameValuePair("C_ANNO", "" + MaimonidesApp.getApplication().getAnoEscolar().getAno()));
                    nameValuePairs.add(new BasicNameValuePair("C_NUMESCOLAR", "11"));
                    nameValuePairs.add(new BasicNameValuePair("C_NUMIDE", "-1"));
                    nameValuePairs.add(new BasicNameValuePair("DESHABILITAR_C_ANNO", "N"));
                    nameValuePairs.add(new BasicNameValuePair("F_NACIMIENTO", "-1"));
                    nameValuePairs.add(new BasicNameValuePair("N_PERIODO", "-1"));
                    nameValuePairs.add(new BasicNameValuePair("T_APELLIDO1", "-1"));
                    nameValuePairs.add(new BasicNameValuePair("T_APELLIDO2", "-1"));
                    nameValuePairs.add(new BasicNameValuePair("T_NOMBRE", "-1"));
                    nameValuePairs.add(new BasicNameValuePair("V1_C_NUMESCOLAR", numEscolar));
                    nameValuePairs.add(new BasicNameValuePair("V1_C_NUMIDE", ""));
                    nameValuePairs.add(new BasicNameValuePair("V1_F_NACIMIENTO", ""));
                    nameValuePairs.add(new BasicNameValuePair("V1_T_APELLIDO1", ""));
                    nameValuePairs.add(new BasicNameValuePair("V1_T_APELLIDO2", ""));
                    nameValuePairs.add(new BasicNameValuePair("V1_T_NOMBRE", ""));
                    nameValuePairs.add(new BasicNameValuePair("V2_C_NUMESCOLAR", ""));
                    nameValuePairs.add(new BasicNameValuePair("V2_C_NUMIDE", ""));
                    nameValuePairs.add(new BasicNameValuePair("V2_F_NACIMIENTO", ""));
                    nameValuePairs.add(new BasicNameValuePair("V2_T_APELLIDO1", ""));
                    nameValuePairs.add(new BasicNameValuePair("V2_T_APELLIDO2", ""));
                    nameValuePairs.add(new BasicNameValuePair("V2_T_NOMBRE", ""));
                    nameValuePairs.add(new BasicNameValuePair("X_OFERTAMATRIC", "-1"));
                    p1.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = getCliente().execute(p1);
                    String txt = EntityUtils.toString(response.getEntity());
                    if (isDebugMode()) {
                        System.out.println(p1.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + txt);
                    }
                    Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Actualizar c\u00f3digo s\u00e9neca: Recuperando listado Ret: {0}", response.getStatusLine().getStatusCode());
                    //buscamos la cadena con el codigo
                    Matcher m = p.matcher(txt);
                    if (m.find()) {
                        int cod = Num.getInt(m.group(1));
                        Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Actualizar c\u00f3digo s\u00e9neca: Encontrado codigo {0}.", cod);
                        if (cod > 0) {
                            alumnos.put(numEscolar, cod);
                            ret++;
                        }
                    } else {
                        Logger.getLogger(ClienteSeneca.class.getName()).log(Level.INFO, "Actualizar c\u00f3digo s\u00e9neca: No se ha encontrado c\u00f3digo en la respuesta:\n{0}", response);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
            hacerLogout();
        } else {
            Logger.getLogger(ClienteSeneca.class.getName()).severe("No se ha podido hacer login");
        }
        return ret;
    }

    private ArrayList<String> getCodigosCurso() {
        ArrayList<String> codigos = new ArrayList<String>();
        try {
            String url = "Principal.jsp?rndval=447615097&COD_PAGINA=2240&N_V_=" + getNombreVentana();
            HttpGet get = new HttpGet(getUrlBase() + url);
            HttpResponse response = getCliente().execute(get);
            String txt = EntityUtils.toString(response.getEntity());
            if (isDebugMode()) {
                System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + txt);
            }
            //Ahora tenemos que procesar este código para sacar las opciones
            Source source = new Source(txt);
            List<Element> lTmp = source.getAllElements("select");
            for (int i = 0; i < lTmp.size(); i++) {
                Element el = lTmp.get(i);
                if (Str.noNulo(el.getAttributeValue("name")).toLowerCase().equals("X_OFERTAMATRIC".toLowerCase())) {
                    lTmp = el.getChildElements();
                    Iterator<Element> elX = lTmp.iterator();
                    while (elX.hasNext()) {
                        Element option = elX.next();
                        int val = Num.getInt(option.getAttributeValue("value"));
                        if (val > 0) {
                            codigos.add(val + "");
                        }
                    }
                    break;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
        return codigos;
    }

    public ArrayList<File> getArchivosMatriculasAlumnado() {

        ArrayList<File> archivos = new ArrayList<File>();
        if (hacerLogin()) {
            //Tenemos que recuperar los códigos de curso
            firePropertyChange("message", null, "Recuperando códigos Séneca para los cursos.");
            for (String codigoCurso : getCodigosCurso()) {
                try {
                    //La web del listado
                    firePropertyChange("message", null, "Descargando fichero Séneca de matriculas de alumnado para el curso: " + codigoCurso + ".");
                    //Nos falta sacar el valor de OFERTAG 299991
                    String ofertag = codigoCurso;
                    String ano = MaimonidesApp.getApplication().getAnoEscolar().getAno() + "";
                    HttpPost p1 = new HttpPost(getUrlBase() + "Principal.jsp?rndval=912428392&COD_PAGINA=11429&OFERTAG=" + ofertag + "&CANNO=" + ano + "&PERIODO=1&N_V_=" + getNombreVentana());
                    ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("CHECKSUM_", ano + "|" + ofertag + "|1|"));
                    nameValuePairs.add(new BasicNameValuePair("CURSO_ACTUAL", ano));
                    nameValuePairs.add(new BasicNameValuePair("C_ANNO", ano));
                    nameValuePairs.add(new BasicNameValuePair("HETAPA", ""));
                    nameValuePairs.add(new BasicNameValuePair("PERIODO", "1"));
                    nameValuePairs.add(new BasicNameValuePair("HVIGENTE", "S"));
                    nameValuePairs.add(new BasicNameValuePair("HOMC", ofertag));
                    nameValuePairs.add(new BasicNameValuePair("OFERTAMAT", ofertag));
                    nameValuePairs.add(new BasicNameValuePair("X_OFERTAMATRIC", ofertag));
                    p1.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = getCliente().execute(p1);
                    String txt = EntityUtils.toString(response.getEntity());
                    if (isDebugMode()) {
                        System.out.println(p1.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + txt);
                    }
                    //La de las consultas de exportación
                    //visitarURL("PaginaExportacionDatos.jsp?rndval=566364053&N_V_=" + getNombreVentana());
                    HttpGet get = new HttpGet(getUrlBase() + "PaginaExportacionDatos.jsp?rndval=566364053&N_V_=" + getNombreVentana());
                    response = getCliente().execute(get);
                    txt = EntityUtils.toString(response.getEntity());
                    if (isDebugMode()) {
                        System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + txt);
                    }
                    //Ahora tenemos que procesar este código para sacar las opciones
                    Source source = new Source(txt);
                    ArrayList<String> columnas = new ArrayList<String>();
                    List<Element> lTmp = source.getAllElements("select");
                    for (int i = 0; i < lTmp.size(); i++) {
                        Element el = lTmp.get(i);
                        if (Str.noNulo(el.getAttributeValue("name")).toLowerCase().equals("COLUMNAS_VISIBLES".toLowerCase())) {
                            lTmp = el.getChildElements();
                            Iterator<Element> elX = lTmp.iterator();
                            while (elX.hasNext()) {
                                Element option = elX.next();
                                String val = option.getAttributeValue("value");
                                if (val != null) {
                                    columnas.add(val);
                                }
                            }
                            break;
                        }
                    }
                    //Luego pedimos los datos
                    HttpPost post = new HttpPost(getUrlBase() + "ExportarDatos.jsp");
                    nameValuePairs = new ArrayList<NameValuePair>();
                    //Añadimos las columnas que hemos recuperado
                    for (String m : columnas) {
                        nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", m));
                    }
                    nameValuePairs.add(new BasicNameValuePair("FORMATO_EXPORTACION", "EXCEL"));
                    nameValuePairs.add(new BasicNameValuePair("TITULO", "MATERIAS DE LAS QUE SE HA MATRICULADO CADA ALUMNO"));
                    post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    response = getCliente().execute(post);
                    File f = File.createTempFile("mm_", ".xls");
                    FileOutputStream fos = new FileOutputStream(f);
                    InputStream is = response.getEntity().getContent();
                    Archivo.copiarArchivo(is, fos);
                    Obj.cerrar(fos, is);
                    archivos.add(f);
                } catch (Exception ex) {
                    Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            hacerLogout();
        } else {
            Logger.getLogger(ClienteSeneca.class.getName()).severe("No se ha podido hacer login");
        }
        return archivos;
    }

    public File getArchivoGeneradoresDeHorarios() {
        File f = null;
        if (hacerLogin()) {
            try {
                firePropertyChange("message", null, "Solicitando fichero Séneca para generadores de horarios.");
                visitarURL("Principal.jsp?rndval=344005786&COD_PAGINA=5004883&MODO=NUEVO&X_TIPINTINF=1&N_V_=" + getNombreVentana());
                String fint = URLEncoder.encode(Fechas.format(new GregorianCalendar(), "dd/MM/yyyy hh:mm"), "latin1");
                visitarURL("Principal.jsp?rndval=456528181&COD_PAGINA=5004903&X_TIPINTINF=1&F_INTINF=" + fint + "&C_ANNO=" + MaimonidesApp.getApplication().getAnoEscolar().getAno() + "&T_OBSERV=&N_V_=" + getNombreVentana());
                HttpGet get = new HttpGet(getUrlBase() + "EnviarFichero.jsp");
                //TODO Habría que comprobar el cod de retorno
                HttpResponse response = getCliente().execute(get);
                if (isDebugMode()) {
                    System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n");
                }
                firePropertyChange("message", null, "Descargando fichero Séneca para generadores de horarios...");
                File tmp = File.createTempFile("maimonides_", ".xml");
                FileOutputStream fos = new FileOutputStream(tmp);
                Archivo.copiarArchivo(response.getEntity().getContent(), fos);
                Obj.cerrar(fos);
                if (tmp.length() > 0) {
                    f = tmp;
                    firePropertyChange("message", null, "Fichero descargado con éxito.");
                }
            } catch (Exception ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
            hacerLogout();
        } else {
            Logger.getLogger(ClienteSeneca.class.getName()).severe("No se ha podido hacer login");
        }
        return f;
    }

    public ArrayList<Par<Profesor, ArrayList<Horario>>> getDatosHorarios(AnoEscolar ano) {
        ArrayList<Par<Profesor, ArrayList<Horario>>> datos = new ArrayList<Par<Profesor, ArrayList<Horario>>>();
        //TODO Mover todo a la clase encargada de esta importación
        if (hacerLogin()) {
            try {
                //ArrayList<Profesor> profesores = Profesor.getProfesores();
                ArrayList<Profesor> profesores = new ArrayList<Profesor>();
                profesores.add(Profesor.getProfesor(530));
                //TODO Hay que verificar que en los formularios esté en el año actual
                //Tenemos que sacar los horarios de cada profesor
                for (Profesor p : profesores) {
                    if (Num.getInt(p.getCodigo()) <= 0) {
                        continue;
                    }
                    ArrayList<Horario> horarios = new ArrayList<Horario>();
                    firePropertyChange("message", null, "Procesando horarios de " + p.getNombreObjeto() + "...");
                    String tomaPos = String.format("%1$td/%1$tm/%1$tY", p.getFechaTomaPosesion());
                    tomaPos = URLEncoder.encode(tomaPos, "utf-8");
                    //Personal -> Personal del centro -> Desplegable en un profesor -> Horario regural
                    String url = "Principal.jsp?rndval=457445156&COD_PAGINA=11613&X_EMPLEADO=" + p.getCodigo() + "&F_TOMAPOS=" + tomaPos + "&C_ANNO=" + MaimonidesApp.getApplication().getAnoEscolar().getAno() + "&N_V_=" + getNombreVentana();
                    HttpGet get = new HttpGet(getUrlBase() + url);
                    HttpResponse response = getCliente().execute(get);
                    String txt = EntityUtils.toString(response.getEntity());
                    if (isDebugMode()) {
                        System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + txt);
                    }
                    Source source = new Source(txt);
                    //Procesamos los combos select para localizar el plan de horarios y otros datos
                    List<Element> lTmp = source.getAllElements("select");
                    int idPlanHorarios = 0;
                    ArrayList<String> sHorData = new ArrayList<String>();
                    for (int i = 0; i < lTmp.size(); i++) {
                        Element el = lTmp.get(i);
                        String name = Str.noNulo(el.getAttributeValue("name")).toLowerCase();
                        if (name.equals("X_HORARIORE_ORIGEN".toLowerCase())) {
                            //Es un select "Copiar la celda" que permite copiar los datos entre celdas
                            //pero que contiene en el value todos los datos que necesitamos
                            sHorData = HtmlUtil.getSelectValues(el, "-1");
                        } else if (name.equals("X_PLAJORESCCEN".toLowerCase())) {
                            //Codigo de plan de horario para acceder a la ficha
                            String sVal = HtmlUtil.getSelectedVal(el);
                            if (sVal != null) {
                                idPlanHorarios = Num.getInt(sVal);
                            }
                        }
                    }
                    //Si hemos recuperado correctamente la id del plan de horarios
                    if (idPlanHorarios > 0) {
                        //Accedemos a los datos que hemos extraido del select
                        for (String hData : sHorData) {

                            String[] data = hData.split("\\|");
                            //El formato es 36737436|1|845233|01/09/2010|31/08/2011|495|555|495|555
                            //Donde:         COD_HORARIO|DIA SEMANA|TRAMO HORARIO|F.INI|F.FIN|495|555|495|555
                            int cod = Num.getInt(data[0]);
                            int dia = Num.getInt(data[1]);
                            firePropertyChange("message", null, "Procesando horarios de " + p.getNombreObjeto() + " " + MaimonidesUtil.getNombreDiaSemana(dia, true) + "...");
                            String codTramoHorario = data[2].trim();
                            String sFIni = data[3];
                            String sFFin = data[4];
                            String codDependencia = null;
                            //El resto de datos son la hora de inicio y final o algo así
                            //Ahora vamos a la ficha del horario en la interfaz en el la tabla de horarios clic -> Detalles
                            String urlHor = "Principal.jsp?rndval=593021341&COD_PAGINA=11733&MODO=EDITAR&X_HORARIORE=" + cod + "&X_PLAJORESCCEN=" + idPlanHorarios + "&N_V_=" + getNombreVentana();
                            HttpGet getHor = new HttpGet(getUrlBase() + urlHor);
                            HttpResponse responseHor = getCliente().execute(getHor);
                            String txtHor = EntityUtils.toString(responseHor.getEntity());
                            if (isDebugMode()) {
                                System.out.println(getHor.getURI() + ":" + responseHor.getStatusLine().getStatusCode() + "\n" + txtHor);
                            }
                            ArrayList<String> sCursoData = new ArrayList<String>();
                            Source sourceHor = new Source(txtHor);
                            List<Element> lSel = sourceHor.getAllElements("select");
                            //Buscamos el select con las asociaciones de cursos/asignaturas "Unidades y materias implicadas: "
                            //Tambien buscamos el select de dependencias

                            for (int i = 0; i < lSel.size(); i++) {
                                Element el = lSel.get(i);
                                String name = Str.noNulo(el.getAttributeValue("name")).toLowerCase();
                                if (name.equals("X_UNIDAD_OFERTAMATRIC_MATERIAOMG".toLowerCase())) {
                                    sCursoData = HtmlUtil.getSelectValues(el);
                                } else if (name.equals("X_DEPENDENCIA".toLowerCase())) {
                                    codDependencia = HtmlUtil.getSelectedVal(el);
                                }
                            }
                            //Buscamos la actividad por si hace falta
                            List<Element> lTds = sourceHor.getAllElementsByClass("celdaTablaFormFila");
                            Actividad actividad = null;
                            for (int i = 0; i < lTds.size(); i++) {
                                Element el = lTds.get(i);
                                String texto = el.getTextExtractor().toString().trim().toLowerCase();
                                if (texto.startsWith("actividad:")) {
                                    String nombreActi = texto.replace("actividad:", "");
                                    try {
                                        actividad = Actividad.getActividad(ano, nombreActi);
                                    } catch (Exception ex) {
                                        Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                    if (actividad != null) {
                                        break;
                                    }
                                }
                            }
                            if (sCursoData.size() > 0) {
                                //Ahora procesamos los values extraidos del select anterior de cursos asignaturas
                                for (String cursoData : sCursoData) {
                                    boolean ok = true;
                                    String[] cData = cursoData.split("-");
                                    //El formato es 1190089-299991-25119
                                    //Donde COD UNIDAD-COD CURSO ALTERNATIVO?-COD MATERIA
                                    int codUnidad = Num.getInt(cData[0]);
                                    int codCursoAlternativo = Num.getInt(cData[1]);
                                    int codMateria = 0;
                                    if (cData.length > 2) {
                                        codMateria = Num.getInt(cData[2]);
                                    }
                                    //Con estos datos ya podemos crear los horarios
                                    Horario h = new Horario();
                                    h.setAnoEscolar(ano);
                                    h.setProfesor(p.getId());
                                    h.setDia(dia);
                                    TramoHorario tramo = TramoHorario.getTramoHorario(ano, codTramoHorario);
                                    if (tramo != null) {
                                        h.setTramo(tramo.getId());
                                        h.setHora(tramo.getHoraHorarios());
                                    } else {
                                        ok = false;
                                    }
                                    if (!Str.noNulo(codDependencia).trim().equals("") && !Str.noNulo(codDependencia).trim().equals("-1")) {
                                        Dependencia d = Dependencia.getDependencia(ano, codDependencia);
                                        if (d != null) {
                                            h.setDependencia(d.getId());
                                        }
                                    }
                                    Unidad u = Unidad.getUnidad(ano, codUnidad);
                                    if (u != null) {
                                        h.setUnidad(u.getId());
                                    } else {
                                        ok = false;
                                    }
                                    if (codMateria > 0) {//Si hay materia la actividad es docencia
                                        h.setActividad(Actividad.getIdActividadDocencia(ano));
                                        Materia m = Materia.getMateria(ano, codMateria);
                                        h.setMateria(m.getId());
                                    } else {
                                        if (actividad != null) {
                                            h.setActividad(actividad.getId());
                                        } else {
                                            ok = false;
                                        }
                                    }
                                    if (ok) {
                                        horarios.add(h);
                                    }
                                }
                            } else {
                                //Si no hay datos de marterias y unidades puede que sea una actividad independiente
                                //así que la generamos igualmente?
                                //TODO Ver si el resto del programa esta preparado para tener actividades que no
                                //se usen en los partes (sobre todo el generador de partes y demás).
                            }
                        }
                        //Ahora guardamos los horarios
                        datos.add(new Par<Profesor, ArrayList<Horario>>(p, horarios));
                        //TODO Hay que tener en cuenta que esto se use para actualizar horarios
//                        ArrayList<IObjetoBD> iobd = new ArrayList<IObjetoBD>();
//                        iobd.addAll(horarios);
//                        guardarObjetosBD(iobd);
                    } else {
                        //TODO Mostrar error
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        hacerLogout();
        return datos;
    }

    public File getArchivoTutores() {
        File f = null;
        if (hacerLogin()) {
            try {
                firePropertyChange("message", null, "Solicitando fichero Séneca con los tutores de cada unidad.");
                visitarURL("Principal.jsp?rndval=264283526&COD_PAGINA=11553&C_ANNO=" + MaimonidesApp.getApplication().getAnoEscolar().getAno() + "&X_OFERTAMATRIC=-3&N_V_=" + getNombreVentana());
                visitarURL("PaginaExportacionDatos.jsp?rndval=300453568&N_V_=" + getNombreVentana() + "&COD_PAGINA_ANTERIOR=11553");
                HttpPost post = new HttpPost(getUrlBase() + "ExportarDatos.jsp");
                ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "T_NOMBRE"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "TIPO"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "N_CAPACIDAD"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "TUTOR"));
                nameValuePairs.add(new BasicNameValuePair("COLUMNAS_VISIBLES", "D_OFERTAMATRIG"));
                nameValuePairs.add(new BasicNameValuePair("FORMATO_EXPORTACION", "CSV"));
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = getCliente().execute(post);
                //TODO Habría que comprobar el cod de retorno
                firePropertyChange("message", null, "Descargando fichero Séneca para generadores de horarios...");
                File tmp = File.createTempFile("maimonides_", ".csv");
                FileOutputStream fos = new FileOutputStream(tmp);
                Archivo.copiarArchivo(response.getEntity().getContent(), fos);
                Obj.cerrar(fos);
                if (tmp.length() > 0) {
                    f = tmp;
                    firePropertyChange("message", null, "Fichero descargado con éxito.");
                }
            } catch (Exception ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
            hacerLogout();
        } else {
            Logger.getLogger(ClienteSeneca.class.getName()).severe("No se ha podido hacer login");
        }
        return f;
    }

    public int enviarFicheroFaltasSeneca(File fichero, String descripcion, MaimonidesBean parentBean) {
        int ok = GestorEnvioFaltas.RET_ERROR_ENVIANDO;
        if (hacerLogin()) {
            final GestorEnvioFaltas gestor = new GestorEnvioFaltas(this);
            gestor.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            if (parentBean != null) {
                parentBean.addPropertyChangeListener("cancelled", new PropertyChangeListener() {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        gestor.setCancelado(true);
                    }
                });
            }
            ok = gestor.enviarFicheroSeneca(fichero, descripcion);
            hacerLogout();
        }
        return ok;
    }

    public int limpiarResultadosEnvioFicherosFaltas() {
        int ok = 0;
        if (hacerLogin()) {
            GestorEnvioFaltas gestor = new GestorEnvioFaltas(this);
            gestor.addPropertyChangeListener(new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
                }
            });
            ok = gestor.limpiarTodosLosResultadosEnvioDeFicheros();
            hacerLogout();
        }
        return ok;
    }

    public boolean visitarURL(String url) throws IOException {
        HttpGet get = new HttpGet(getUrlBase() + url);
        HttpResponse response = getCliente().execute(get);
        String texto = EntityUtils.toString(response.getEntity());
        if (isDebugMode()) {
            System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + texto);
        }
        return isOk(response, texto);
    }

    public Usuario senecaUserLogin() {
        Usuario u = null;
        if (hacerLogin()) {
            u = new Usuario();
            u.setUsuarioVirtual(true);
            u.setNombre(getUsuario());
            u.setRoles(Rol.ROL_PROFESOR);
            try {
                String datosUsuario = cargarDatosUsuarioActual();
                if (!Str.noNulo(datosUsuario).trim().equals("")) {
                    if (datosUsuario.contains(PERFIL_DIRECCION)) {
                        u.setRoles(Rol.ROL_PROFESOR | Rol.ROL_DIRECTIVO | Rol.ROL_JEFE_ESTUDIOS);
                    }
                    datosUsuario = datosUsuario.replace(PERFIL_DIRECCION, "");
                    datosUsuario = datosUsuario.replace(PERFIL_PROFESOR, "");
                    datosUsuario = datosUsuario.replace("(", "");
                    datosUsuario = datosUsuario.replace(")", "");
                    u.setNombre(datosUsuario.trim());
                    //Con el nombre asignado la ficha se encarga automáticamente de cargar el profesor asociado
                }
            } catch (IOException ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        hacerLogout();
        return u;
    }

    public HashMap<String, String> getDatosCentro() {
        HashMap<String, String> datos = new HashMap<String, String>();
        if (hacerLogin()) {
            try {
                HashMap<String, String> mapaTitulos = new HashMap<String, String>();
                mapaTitulos.put("Código de centro:", "codigo_centro");
                mapaTitulos.put("Denominación del centro:", "nombre_centro");
                mapaTitulos.put("Domicilio:", "direccion_centro");
                mapaTitulos.put("Localidad:", "poblacion_centro");
                mapaTitulos.put("Cód. Postal:", "cp_centro");
                mapaTitulos.put("Provincia:", "provincia_centro");
                mapaTitulos.put("Tfno:", "telefono_centro");
                mapaTitulos.put("Fax:", "fax_centro");
                mapaTitulos.put("Correo electrónico:", "email_centro");
                firePropertyChange("message", null, "Cargando datos del centro");
                HttpGet get = new HttpGet(getUrlBase() + "Principal.jsp?rndval=607395803&COD_PAGINA=51&N_V_=" + getNombreVentana());
                Logger.getLogger(ClienteSeneca.class.getName()).info("Cargando datos del centro.");
                HttpResponse response = getCliente().execute(get);
                String texto = EntityUtils.toString(response.getEntity());
                Source source = new Source(texto);
                List<Element> celdas = source.getAllElementsByClass("celdaTablaFormFila");
                String prefijoNombreCentro = "";
                for (Element e : celdas) {
                    List<Element> valores = e.getAllElementsByClass("columnaValor");
                    
                    String valor=null;
                    if (!valores.isEmpty()) {
                        Element elValor = valores.get(0);
                        valor = elValor.getContent().getTextExtractor().toString();
                    } else {
                        valores = e.getAllElementsByClass("entrada");
                        if (!valores.isEmpty()) {
                            Element elValor = valores.get(0);
                            valor=elValor.getAttributeValue("value");
                        }
                    }
                    if (valor != null) {
                        valor=valor.trim();
                        String nombre = e.getContent().getTextExtractor().toString();
                        nombre = nombre.replace(valor, "").trim();
                        if ("Denominación genérica:".equals(nombre)) {
                            prefijoNombreCentro = valor;
                        } else if (mapaTitulos.containsKey(nombre)) {
                            datos.put(mapaTitulos.get(nombre), valor);
                        }
                    }
                }
                datos.put("nombre_centro", (prefijoNombreCentro + " " + datos.get("nombre_centro")).trim());
            } catch (Exception ex) {
                Logger.getLogger(ClienteSeneca.class.getName()).log(Level.SEVERE, null, ex);
                datos = null;
            }
        }
        hacerLogout();
        return datos;
    }
}
