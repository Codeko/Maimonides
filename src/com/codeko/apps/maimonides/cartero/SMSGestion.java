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


package com.codeko.apps.maimonides.cartero;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Cripto;
import java.util.ArrayList;
import java.util.List;
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

public class SMSGestion {

    private static final String WEB = "http://www.smsgestion.es/servicio/";
    String entidad = "";
    String claveServicio = "";
    HttpClient cliente = new DefaultHttpClient();

    public HttpClient getCliente() {
        return cliente;
    }

    private String getClaveServicio() {
        return claveServicio;
    }

    private void setClaveServicio(String claveServicio) {
        this.claveServicio = claveServicio;
    }

    private String getEntidad() {
        return entidad;
    }

    private void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public SMSGestion() {
        setClaveServicio(MaimonidesApp.getApplication().getConfiguracion().get("smsGestion.clave_servicio", ""));
        setEntidad(MaimonidesApp.getApplication().getConfiguracion().get("smsGestion.entidad", ""));
    }

    public boolean enviarSMS(String texto, ArrayList<String> numeros) {
        boolean ret = false;
        try {
            String control = System.currentTimeMillis() + "";
            String hash = Cripto.md5(getClaveServicio() + control);
            String xml = getXML(texto, numeros);
            HttpPost post = new HttpPost(WEB);
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
            nameValuePairs.add(new BasicNameValuePair("ent", getEntidad()));
            nameValuePairs.add(new BasicNameValuePair("control", control));
            nameValuePairs.add(new BasicNameValuePair("hash", hash));
            nameValuePairs.add(new BasicNameValuePair("op", "enviarmensaje"));
            nameValuePairs.add(new BasicNameValuePair("archivo", xml));
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs, "ISO-8859-1"));
            HttpResponse response = getCliente().execute(post);
            String textResp = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() == 200) {
                Logger.getLogger(SMSGestion.class.getName()).info(textResp);
                ret = textResp.toLowerCase().indexOf("error") == -1;
            } else {
                ret = false;
                Logger.getLogger(SMSGestion.class.getName()).log(Level.INFO, "{0}\n{1}", new Object[]{response.getStatusLine().getStatusCode(), textResp});
            }
        } catch (Exception ex) {
            Logger.getLogger(SMSGestion.class.getName()).log(Level.SEVERE, null, ex);
            ret = false;
        }
        return ret;
    }

    private String getXML(String texto, ArrayList<String> numeros) {
        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?> "
                + "<mensaje> "
                + "<tipoenvio>sms</tipoenvio> "
                + "<texto><![CDATA[" + texto + "]]></texto> "
                + "<destinatarios> ";
        for (String s : numeros) {
            xml += "<destinatario> " + "<movil>" + s + "</movil> " + "</destinatario> ";
        }
        xml += "</destinatarios> </mensaje>";
        return xml;
    }
}
