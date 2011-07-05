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
import com.codeko.apps.maimonides.alumnos.PanelEnvioSMS;
import com.codeko.apps.maimonides.conf.mail.ConfiguracionMail;
import com.codeko.apps.maimonides.elementos.Alumno;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class SMS {

    public static boolean enviarSMS(String numero, String texto, boolean voz) {
        ArrayList<String> numeros = new ArrayList<String>();
        numeros.add(numero);
        return enviarSMS(numeros, texto, voz);
    }

    public static boolean enviarSMS(ArrayList<String> numeros, String texto, boolean voz) {
        boolean smsGestion = MaimonidesApp.getApplication().getConfiguracion().get("sms.servicio", "mail").equals("smsGestion");
        if (MaimonidesApp.isDebug()) {
            numeros = new ArrayList<String>();
            numeros.add("610212922");
        }
        boolean ret = false;
        if (smsGestion && !voz) {
            SMSGestion smsg = new SMSGestion();
            ret = smsg.enviarSMS(texto, numeros);
        } else {
            ConfiguracionMail conf = new ConfiguracionMail(voz ? "VOZ" : "SMS");
            try {
                SimpleEmail email = new SimpleEmail();
                conf.aplicarConfiguracion(email);
                for (String s : numeros) {
                    email.addTo(s + conf.getExtra());
                }
                email.setSubject("");
                email.setMsg(texto + conf.getPie());
                email.send();
                ret = true;
            } catch (EmailException ex) {
                Logger.getLogger(PanelEnvioSMS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }

    public static boolean enviarSMS(String texto, ArrayList<Alumno> alumnos, boolean voz) {
        boolean smsGestion = MaimonidesApp.getApplication().getConfiguracion().get("sms.servicio", "mail").equals("smsGestion");
        final String SMS_DEBUG = "610212922";
        boolean ret = false;
        ArrayList<Carta> cartas = new ArrayList<Carta>();
        if (smsGestion && !voz) {
            SMSGestion smsg = new SMSGestion();
            ArrayList<String> numeros = new ArrayList<String>();
            if (MaimonidesApp.isDebug()) {
                numeros.add(SMS_DEBUG);
            } else {
                for (Alumno a : alumnos) {
                    numeros.add(a.getSms());
                    Carta carta = new Carta();
                    carta.setAlumno(a);
                    carta.addModo(Alumno.NOTIFICAR_SMS);
                    carta.addParametro(a.getSms());
                    carta.setLocalizador(System.currentTimeMillis() + "-" + a.getId());
                    carta.setNombre("Notificación manual");
                    carta.setTipo(Carta.TIPO_CARTA_NOTIFICACION_MANUAL);
                    cartas.add(carta);
                    carta.setDescripcion("SMS:\n" + texto);
                    cartas.add(carta);
                }
            }
            ret = smsg.enviarSMS(texto, numeros);
        } else {
            ConfiguracionMail conf = new ConfiguracionMail(voz ? "VOZ" : "SMS");
            try {
                SimpleEmail email = new SimpleEmail();
                conf.aplicarConfiguracion(email);
                if (MaimonidesApp.isDebug()) {
                    email.addTo(SMS_DEBUG + conf.getExtra());
                } else {
                    for (Alumno a : alumnos) {
                        email.addTo(a.getSms() + conf.getExtra());
                        Carta carta = new Carta();
                        carta.setAlumno(a);
                        if (voz) {
                            carta.addModo(Alumno.NOTIFICAR_TELEFONO);
                        } else {
                            carta.addModo(Alumno.NOTIFICAR_SMS);
                        }
                        carta.addParametro(a.getSms());
                        carta.setLocalizador(System.currentTimeMillis() + "-" + a.getId());
                        carta.setNombre("Notificación manual");
                        carta.setTipo(Carta.TIPO_CARTA_NOTIFICACION_MANUAL);
                        cartas.add(carta);
                        carta.setDescripcion("SMS:\n" + texto);
                        cartas.add(carta);
                    }
                }
                email.setSubject("");
                email.setMsg(texto + conf.getPie());
                email.send();
                ret = true;
            } catch (EmailException ex) {
                Logger.getLogger(PanelEnvioSMS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (ret) {
            for (Carta c : cartas) {
                c.guardar();
            }
        }
        return ret;
    }
}
