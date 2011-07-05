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

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 * La tabla de resultados tiene la siguiente estructura
 *
<a  HREF="javascript:showOpciones(1)"  FD_TIPO_FORMATEADOR="FD_EN_LINEAS" >2008-2009</a> </TD>
<TD NOWRAP  CLASS="cuerpo1"  ALIGN="LEFT" >
<FONT  FD_FORMATO_FECHA="dd'/'MM'/'yyyy HH':'mm':'ss"  FD_TIPO_FORMATEADOR="FD_FORMATO_FECHA" >27/03/2009 19:22:16</FONT></TD>
<TD NOWRAP  CLASS="cuerpo1"  ALIGN="LEFT" >
<FONT  FD_TIPO_FORMATEADOR="FD_EN_LINEAS" >Gómez Reyes, Pilar</FONT></TD>
<TD NOWRAP  CLASS="cuerpo1"  ALIGN="LEFT" >
<FONT  FD_TIPO_FORMATEADOR="FD_EN_LINEAS" >Finalizado correctamente</FONT></TD>

<TD NOWRAP  CLASS="cuerpo1"  ALIGN="RIGHT" >
<FONT  FD_TIPO_FORMATEADOR="FD_EN_LINEAS" >0</FONT></TD>
<TD NOWRAP  CLASS="cuerpo1"  ALIGN="LEFT" >
&nbsp;
</TD>
<TD NOWRAP  CLASS="cuerpo1"  ALIGN="RIGHT" >
<FONT  FD_TIPO_FORMATEADOR="FD_EN_LINEAS" >2</FONT></TD>
<TD NOWRAP  CLASS="cuerpo1"  ALIGN="RIGHT" >
<FONT  FD_TIPO_FORMATEADOR="FD_EN_LINEAS" >2</FONT></TD>
<TD NOWRAP  CLASS="cuerpo1"  ALIGN="RIGHT" >
<FONT  FD_TIPO_FORMATEADOR="FD_EN_LINEAS" >5</FONT></TD>
<TD NOWRAP  CLASS="cuerpo1"  ALIGN="LEFT" >
<FONT  FD_TIPO_FORMATEADOR="FD_EN_LINEAS" >Maim?nides:Faltas 10-11-2008 1BTO</FONT></TD>

</TR>
</TABLE></TD>
</TR>
</TABLE>
 * Una vez terminada la tabla aparece la siguiente estrucutura
<INPUT TYPE="HIDDEN" NAME=OpEmergente1 VALUE="X_INTINF=109853&HEREDAR_ACC=SI&NUM_FILA=0">
 * Nos intenresa el resultado, la descripción para identificar el envío y el input que nos da la id para borrar el envío
 */
public class ResultadoEnvioFicheroFaltas extends MaimonidesBean {

    int pos = 0;
    String resultado = "";
    String nombre = "";
    String params = "";
    String error = "";
    public static short ESTADO_PROCESANDO = 0;
    public static short ESTADO_ERROR = -1;
    public static short ESTADO_EXITO = 1;
    public static short ESTADO_DESCONOCIDO = 2;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado.trim().toLowerCase();
    }

    public short getEstado() {
        short estado = ESTADO_DESCONOCIDO;
        if (getResultado().equals("Finalizado correctamente".toLowerCase())) {
            estado = ESTADO_EXITO;
        } else if (getResultado().equals("En proceso".toLowerCase())) {
            estado = ESTADO_PROCESANDO;
        } else if (getResultado().equals("Finalizado con errores".toLowerCase())) {
            estado = ESTADO_ERROR;
        }
        return estado;
    }

    public boolean borrar(ClienteSeneca cli) {
        boolean ok = false;
        try {
            if (!getParams().trim().equals("")) {
                String url = ClienteSeneca.getUrlBase() + "Principal.jsp?rndval=31417255&COD_PAGINA=13079&" + getParams() + "&MODO=BORRAR&N_V_=" + cli.getNombreVentana() + "&COD_PAGINA_ANTERIOR=5004863&TIEMPO_PAGINA_ANTERIOR=2751&TIEMPO_PAGINA_ANTERIOR_CON_BOTONERA=2889&PAG_NO_VISIBLE_=S";
                HttpGet get = new HttpGet(url);
                HttpResponse response = cli.getCliente().execute(get);
                String txt = EntityUtils.toString(response.getEntity());
                if (cli.isDebugMode()) {
                    System.out.println(get.getURI() + ":" + response.getStatusLine().getStatusCode() + "\n" + txt);
                }
                ok = cli.isOk(response, txt);
            }
        } catch (IOException ex) {
            Logger.getLogger(ResultadoEnvioFicheroFaltas.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ok;
    }
}
