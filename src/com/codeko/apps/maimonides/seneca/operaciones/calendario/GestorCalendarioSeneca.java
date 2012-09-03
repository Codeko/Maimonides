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


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeko.apps.maimonides.seneca.operaciones.calendario;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.calendario.DiaCalendarioEscolar;
import com.codeko.apps.maimonides.calendario.PanelCalendarioEscolar;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author codeko
 */
public class GestorCalendarioSeneca extends MaimonidesBean {

    ClienteSeneca cliente = null;
    static ArrayList<DiaCalendarioEscolar> calendario = null;

    public static ArrayList<DiaCalendarioEscolar> getCalendario() {
        if (calendario == null) {
            String sql = "SELECT * FROM calendario_escolar WHERE ano=?";
            PreparedStatement st = null;
            calendario = new ArrayList<DiaCalendarioEscolar>();
            try {
                st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                ResultSet res = st.executeQuery();
                while (res.next()) {
                    DiaCalendarioEscolar d = new DiaCalendarioEscolar();
                    d.cargarDesdeResultSet(res);
                    calendario.add(d);
                }
            } catch (SQLException ex) {
                Logger.getLogger(PanelCalendarioEscolar.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st);
        }
        return calendario;
    }

    public static boolean isFestivo(GregorianCalendar fecha) {
        boolean ret = false;
        for (DiaCalendarioEscolar d : getCalendario()) {
            if (Fechas.getDiferenciaTiempoEn(fecha, d.getDia(), Calendar.DAY_OF_MONTH) == 0) {
                return true;
            }
        }
        return ret;
    }

    public static boolean isFestivoDocente(GregorianCalendar fecha) {
        boolean ret = false;
        for (DiaCalendarioEscolar d : getCalendario()) {
            if (Fechas.getDiferenciaTiempoEn(fecha, d.getDia(), Calendar.DAY_OF_MONTH) == 0) {
                return d.getDocentes();
            }
        }
        return ret;
    }

    public static boolean isFestivoPersonal(GregorianCalendar fecha) {
        boolean ret = false;
        for (DiaCalendarioEscolar d : getCalendario()) {
            if (Fechas.getDiferenciaTiempoEn(fecha, d.getDia(), Calendar.DAY_OF_MONTH) == 0) {
                return d.getPersonal();
            }
        }
        return ret;
    }

    public static DiaCalendarioEscolar getDia(GregorianCalendar fecha) {
        for (DiaCalendarioEscolar d : getCalendario()) {
            if (Fechas.getDiferenciaTiempoEn(fecha, d.getDia(), Calendar.DAY_OF_MONTH) == 0) {
                return d;
            }
        }
        return null;
    }

    public static void resetearCalendario() {
        calendario = null;
    }

    public ClienteSeneca getCliente() {
        return cliente;
    }

    private void setCliente(ClienteSeneca cliente) {
        this.cliente = cliente;
    }

    public GestorCalendarioSeneca(ClienteSeneca cli) {
        setCliente(cli);
    }

    public ArrayList<DiaCalendarioEscolar> getDiasFestivos() {
        ArrayList<DiaCalendarioEscolar> ret = null;
        if (getCliente().hacerLogin()) {
            ret = new ArrayList<DiaCalendarioEscolar>();
            try {
                //Pagina: Centro-> Calendario y Jornada -> Calendario escolar -> Dias festivos
                firePropertyChange("message", null, "Cargando datos de calendario...");
                boolean continuar = true;
                String url = "Principal.jsp?rndval=572558106&COD_PAGINA="+getCliente().getCodigoPagina("200CalEscCent") +"&DESDE_MENU_=S&PRIMERA_VISITA_=S&N_V_=" + getCliente().getNombreVentana();
                int cont = 10;
                int pagina = 1;
                while (continuar && cont > 0) {
                    cont--;
                    HttpGet get = new HttpGet(ClienteSeneca.getUrlBase() + url);
                    HttpResponse response = getCliente().getCliente().execute(get);
                    String txt = EntityUtils.toString(response.getEntity());
                    if (getCliente().isDebugMode()) {
                        System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + txt);
                    }
                    if (response.getStatusLine().getStatusCode() == 200) {
                        Source s = new Source(txt);
                        //Buscamos la tabla de datos
                        Pattern p = Pattern.compile("TableData", Pattern.CASE_INSENSITIVE);
                        List<Element> l = s.getAllElements("class", p);
                        if (!l.isEmpty()) {
                            Element tabla = l.get(0);
                            //Ahora cogemos todos los tr
                            l = tabla.getAllElements("tr");
                            if (!l.isEmpty()) {
                                //Empezamos por el 1 porque el 0 es la cabecera
                                for (int i = 1; i < l.size(); i++) {
                                    Element tr = l.get(i);
                                    //Ahora del tr tenemos que sacar el texto de cada td
                                    List<Element> tds = tr.getAllElements("td");
                                    if (tds.size() == 5) {
                                        String sFecha = tds.get(0).getTextExtractor().toString();
                                        String descripcion = tds.get(1).getTextExtractor().toString();
                                        String ambito = tds.get(2).getTextExtractor().toString();
                                        String profesorado = tds.get(3).getTextExtractor().toString();
                                        String personal = tds.get(4).getTextExtractor().toString();
                                        DiaCalendarioEscolar dia = new DiaCalendarioEscolar();
                                        dia.setDescripcion(descripcion);
                                        dia.setAmbito(ambito);
                                        dia.setDocentes(!profesorado.trim().toLowerCase().equals("no"));
                                        dia.setPersonal(!personal.trim().toLowerCase().equals("no"));
                                        dia.setDia(Fechas.toGregorianCalendar(Fechas.parse(sFecha, "dd/MM/yyyy")));
                                        ret.add(dia);
                                    }
                                }
                            }
                            //Ahora cambiamos la url de la petición
                            url = "Principal.jsp?rndval=265924307&EN_RECARGA_=S&COD_PAGINA="+getCliente().getCodigoPagina("200CalEscCent")+"&ALEATORIO=PMFFPMAEDRAEDRKVLCFF&N_V_=" + getCliente().getNombreVentana();
                            //Y llamamos al paginar para ir a la siguiente página
                            HttpPost post = new HttpPost( ClienteSeneca.getUrlBase() + "Paginar.jsp?MODO=PAGINAR&IR_A=" + (pagina + 1) + "&PAGINA=" + pagina + "&");
                            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                            nameValuePairs.add(new BasicNameValuePair("CHECKSUM_", MaimonidesApp.getApplication().getAnoEscolar().getAno() + "|"));
                            nameValuePairs.add(new BasicNameValuePair("C_ANNO", "" + MaimonidesApp.getApplication().getAnoEscolar().getAno()));
                            response = getCliente().getCliente().execute(post);
                            pagina++;
                        } else {
                            continuar = false;
                        }
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(GestorCalendarioSeneca.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }
}
