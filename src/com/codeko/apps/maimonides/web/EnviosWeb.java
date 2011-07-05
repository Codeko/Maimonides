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


package com.codeko.apps.maimonides.web;

import com.codeko.apps.maimonides.Configuracion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.util.Str;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class EnviosWeb extends MaimonidesBean {

    public static final int TIPO_ENVIO_USUARIOS = 1;
    public static final int TIPO_ENVIO_FALTAS = 2;
    public static final short RETORNO_ERROR = 0;
    public static final short RETORNO_OK = 1;
    public static final short RETORNO_NO_WEB = -1;
    JSONArray elementos = new JSONArray();
    int tipo = 0;
    String URL_COMUNICACION = "/maimonides/";
    String ultimoMensaje = "";
    String datosRespuestaUltimoComando = "";

    public String getUltimoMensaje() {
        return ultimoMensaje;
    }

    public void setUltimoMensaje(String ultimoMensaje) {
        this.ultimoMensaje = ultimoMensaje;
    }

    public String getDatosRespuestaUltimoComando() {
        return datosRespuestaUltimoComando;
    }

    public void setDatosRespuestaUltimoComando(String datosRespuestaUltimoComando) {
        this.datosRespuestaUltimoComando = datosRespuestaUltimoComando;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    private JSONArray getElementos() {
        return elementos;
    }

    public EnviosWeb(int tipo) {
        setTipo(tipo);
    }

    public void addElemento(Object obj) {
        JSONObject o = new JSONObject(obj);
        getElementos().put(o);
    }

    public void addElementos(Collection elementos) {
        for (Object obj : elementos) {
            addElemento(obj);
        }
    }

    @Override
    public String toString() {
        return getElementos().toString();
    }

    public static Vector<UsuarioWeb> getUsuariosProfesoresWeb(Collection<Profesor> profesores) {
        Vector<UsuarioWeb> ret = new Vector<UsuarioWeb>();
        for (Profesor p : profesores) {
            try {
                Unidad u = Unidad.getUnidadPorTutor(p.getId());
                String departamento = Str.noNulo(p.getPuesto()).replace("P.E.S.", "").replace("P.T.F.P.", "").trim();
                UsuarioWeb uw = new UsuarioWeb(UsuarioWeb.TIPO_PROFESOR, p.getId(), p.getNombre(), p.getApellido1(), p.getApellido2(), p.getEmail(), departamento, u);
                ret.add(uw);
            } catch (Exception e) {
                Logger.getLogger(EnviosWeb.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return ret;
    }

    public static ArrayList<UsuarioWeb> getUsuariosAlumnosWeb(Collection<Alumno> alumnos) {
        ArrayList<UsuarioWeb> ret = new ArrayList<UsuarioWeb>();
        for (Alumno a : alumnos) {
            try {
                Unidad u = a.getUnidad();
                String departamento = "";
                UsuarioWeb uw = new UsuarioWeb(UsuarioWeb.TIPO_PROFESOR, a.getId(), a.getNombre(), a.getApellido1(), a.getApellido2(), a.getEmail(), departamento, u);
                ret.add(uw);
            } catch (Exception e) {
                Logger.getLogger(EnviosWeb.class.getName()).log(Level.SEVERE, null, e);
            }
        }
        return ret;
    }

    public static boolean hayEnviosWeb() {
        return MaimonidesApp.getApplication().getConfiguracion().get(Configuracion.CENTRO_WEB_COMPATIBLE, "0").equals("1");
    }

    public short enviar() {
        short ret = RETORNO_ERROR;
        try {
            //Primero vemos que haya web confiugurada
            String web = MaimonidesApp.getApplication().getConfiguracion().get(Configuracion.CENTRO_WEB, "");
            if (EnviosWeb.hayEnviosWeb()) {
                String usr = MaimonidesApp.getApplication().getConfiguracion().get(Configuracion.WEB_USUARIO, "");
                String pass = MaimonidesApp.getApplication().getConfiguracion().get(Configuracion.WEB_CLAVE, "");
                JSONObject objTipo = new JSONObject();
                objTipo.put("tipo", "" + getTipo());
                objTipo.put("parametros", "");
                objTipo.put("datos", getElementos());
                objTipo.put("count", getElementos().length());
                String msg = objTipo.toString();
                System.out.println(msg.length() + "\n" + msg);
                //Ahora mandamos el post con los datos
                HttpClient cliente = new DefaultHttpClient();
                HttpPost post = new HttpPost(web + URL_COMUNICACION);
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
                nameValuePairs.add(new BasicNameValuePair("datos", msg));
                nameValuePairs.add(new BasicNameValuePair("usr", usr));
                nameValuePairs.add(new BasicNameValuePair("pass", pass));
                post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = cliente.execute(post);
                if (response.getStatusLine().getStatusCode() == 200) {
                    String txt = EntityUtils.toString(response.getEntity(), "UTF-8");
                    Logger.getLogger(EnviosWeb.class.getName()).info("Respuesta envío web:" + txt);
                    try {
                        JSONObject json = new JSONObject(txt);
                        int exito = json.getInt("exito");
                        String mensaje = json.getString("msg");
                        String datos = json.getString("datos");
                        setDatosRespuestaUltimoComando(datos);
                        setUltimoMensaje(mensaje);
                        ret = exito > 0 ? RETORNO_OK : RETORNO_ERROR;
                    } catch (Exception e) {
                        Logger.getLogger(EnviosWeb.class.getName()).log(Level.SEVERE, null, e);
                        setUltimoMensaje("La web ha devuelto una respuesta inválida al envío de datos.");
                    }
                } else {
                    setUltimoMensaje("Ha habido un error de comunicación con la web.\nVerifique que la URL de la web es correcta.\nError devuelto:" + response.getStatusLine().getStatusCode());
                }
            } else {
                ret = RETORNO_NO_WEB;
                setUltimoMensaje("No está configurada la web donde se deben enviar los datos.\nPuede configurar esta en Archivo->Configuración->Datos del centro");
            }
        } catch (Exception ex) {
            Logger.getLogger(EnviosWeb.class.getName()).log(Level.SEVERE, null, ex);
            setUltimoMensaje("Se ha producido un error en las comunicaciones:\n" + ex.getLocalizedMessage());
        }
        return ret;
    }
}
