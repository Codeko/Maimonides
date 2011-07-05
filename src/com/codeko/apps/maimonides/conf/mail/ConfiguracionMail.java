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


package com.codeko.apps.maimonides.conf.mail;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Num;
import com.codeko.util.Str;
import java.beans.Beans;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ConfiguracionMail {

    String nombre = "";
    boolean ssl = true;
    int puerto = 465;
    int maximoCaracteres = 0;
    String host = "";
    String usuario = "";
    String clave = "";
    String from = "";
    String extra = "";
    String pie = "";

    public ConfiguracionMail(String nombre) {
        this.nombre = nombre;
        if (!Beans.isDesignTime()) {
            ssl = MaimonidesApp.getApplication().getConfiguracion().get(getNombre() + "_mail_ssl", "1").equals("1");
            puerto = Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get(getNombre() + "_mail_puerto", "465"));
            maximoCaracteres = Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get(getNombre() + "_mail_maximoCaracteres", "0"));
            host = MaimonidesApp.getApplication().getConfiguracion().get(getNombre() + "_mail_host", "");
            usuario = MaimonidesApp.getApplication().getConfiguracion().get(getNombre() + "_mail_usuario", "");
            clave = MaimonidesApp.getApplication().getConfiguracion().get(getNombre() + "_mail_clave", "");
            from = MaimonidesApp.getApplication().getConfiguracion().get(getNombre() + "_mail_from", "");
            extra = MaimonidesApp.getApplication().getConfiguracion().get(getNombre() + "_mail_extra", "");
            pie = MaimonidesApp.getApplication().getConfiguracion().get(getNombre() + "_mail_pie", "");
        }
    }
    
    public static boolean isConfigurado(String nombre){
        ConfiguracionMail c=new ConfiguracionMail(nombre);
        return !Str.noNulo(c.getHost()).equals("") && !Str.noNulo(c.getFrom()).equals("");
    }

    public void aplicarConfiguracion(Email email) throws EmailException {
        email.setHostName(getHost());
        email.setDebug(MaimonidesApp.isDebug());
        email.setSSL(isSsl());
        email.setSmtpPort(getPuerto());
        email.setAuthenticator(new DefaultAuthenticator(getUsuario(), getClave()));
        email.setFrom(getFrom(), MaimonidesApp.getApplication().getConfiguracion().get("nombre_centro", "Maimónides"));
    }

    public String getPie() {
        return pie;
    }

    public void setPie(String pie) {
        this.pie = pie;
        MaimonidesApp.getApplication().getConfiguracion().set(getNombre() + "_mail_pie", pie);
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
        MaimonidesApp.getApplication().getConfiguracion().set(getNombre() + "_mail_extra", extra);
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
        MaimonidesApp.getApplication().getConfiguracion().set(getNombre() + "_mail_clave", clave);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
        MaimonidesApp.getApplication().getConfiguracion().set(getNombre() + "_mail_from", from);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
        MaimonidesApp.getApplication().getConfiguracion().set(getNombre() + "_mail_host", host);
    }

    public int getMaximoCaracteres() {
        return maximoCaracteres;
    }

    public void setMaximoCaracteres(int maximoCaracteres) {
        this.maximoCaracteres = maximoCaracteres;
        MaimonidesApp.getApplication().getConfiguracion().set(getNombre() + "_mail_maximoCaracteres", Str.noNulo(maximoCaracteres));
    }

    public final String getNombre() {
        return nombre;
    }

    public int getPuerto() {
        return puerto;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
        MaimonidesApp.getApplication().getConfiguracion().set(getNombre() + "_mail_puerto", Str.noNulo(puerto));
    }

    public boolean isSsl() {
        return ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
        MaimonidesApp.getApplication().getConfiguracion().set(getNombre() + "_mail_ssl", ssl ? "1" : "0");
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
        MaimonidesApp.getApplication().getConfiguracion().set(getNombre() + "_mail_usuario", usuario);
    }
}
