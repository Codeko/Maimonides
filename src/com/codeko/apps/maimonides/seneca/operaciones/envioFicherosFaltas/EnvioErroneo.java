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

import com.codeko.util.Num;
import com.codeko.util.Obj;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Properties;

/**
 *
 * @author codeko
 */
public class EnvioErroneo {

    GregorianCalendar desde = null;
    GregorianCalendar hasta = null;
    String[] cursos = null;
    File ficheroEnvio = null;
    File ficheroPropiedades = null;
    String error="";
    ArrayList<File> erroneos = new ArrayList<File>();
    ArrayList<File> fallidos = new ArrayList<File>();
    ArrayList<AlumnoEnvioErroneo> alumnosFallidos = new ArrayList<AlumnoEnvioErroneo>();

    public EnvioErroneo(File propiedades) throws IOException {
        Properties p = new Properties();
        FileInputStream fis = new FileInputStream(propiedades);
        p.load(fis);
        Obj.cerrar(fis);
        GregorianCalendar d = new GregorianCalendar();
        d.setTimeInMillis(Num.getLong(p.get("desde")));
        setDesde(d);
        GregorianCalendar h = new GregorianCalendar();
        h.setTimeInMillis(Num.getLong(p.get("hasta")));
        setHasta(h);
        setFicheroPropiedades(propiedades);
        File envio = new File(p.getProperty("archivo"));
        setFicheroEnvio(envio);
        this.cursos = p.getProperty("cursos", "").split(",");
        setError(p.getProperty("error", ""));
    }

    public String getError() {
        return error;
    }

    public final void setError(String error) {
        this.error = error;
    }

    public ArrayList<AlumnoEnvioErroneo> getAlumnosFallidos() {
        return alumnosFallidos;
    }

    public ArrayList<File> getErroneos() {
        return erroneos;
    }

    public ArrayList<File> getFallidos() {
        return fallidos;
    }

    public String[] getCursos() {
        return cursos;
    }

    public GregorianCalendar getDesde() {
        return desde;
    }

    public final void setDesde(GregorianCalendar desde) {
        this.desde = desde;
    }

    public File getFicheroEnvio() {
        return ficheroEnvio;
    }

    public final void setFicheroEnvio(File ficheroEnvio) {
        this.ficheroEnvio = ficheroEnvio;
    }

    public File getFicheroPropiedades() {
        return ficheroPropiedades;
    }

    public final void setFicheroPropiedades(File ficheroPropiedades) {
        this.ficheroPropiedades = ficheroPropiedades;
    }

    public GregorianCalendar getHasta() {
        return hasta;
    }

    public final void setHasta(GregorianCalendar hasta) {
        this.hasta = hasta;
    }

    @Override
    public String toString() {
        String str=getError();
        if(str.equals("")){
            str=getFicheroEnvio().getName();
        }else{
            str=" ("+getFicheroEnvio().getName()+")";
        }
        return getFicheroEnvio().getName();
    }
}
