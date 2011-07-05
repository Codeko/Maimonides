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


package com.codeko.apps.maimonides.importadores;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ImportadorArchivoTutores extends MaimonidesBean {

    private File archivoTutores = null;
    private AnoEscolar anoEscolar = null;
    private ArrayList<String> errores = new ArrayList<String>();

    public ArrayList<String> getErrores() {
        return errores;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        firePropertyChange("anoEscolar", this.anoEscolar, anoEscolar);
        this.anoEscolar = anoEscolar;
    }

    public ImportadorArchivoTutores(AnoEscolar anoEscolar, File archivoTutores) {
        setAnoEscolar(anoEscolar);
        setArchivoTutores(archivoTutores);
    }

    public File getArchivoTutores() {
        return archivoTutores;
    }

    public final void setArchivoTutores(File archivoTutores) {
        firePropertyChange("archivoTutores", this.archivoTutores, archivoTutores);
        this.archivoTutores = archivoTutores;
    }

    public boolean importarTutores() {
        boolean ret = false;
        getErrores().clear();
        try {
            firePropertyChange("setMensaje", null, "Procensando tutores...");
            Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).info("Procesando archivo de tutores");
            Pattern p = Pattern.compile("\"([^\"]*)\"");
            Logger.getLogger(ImportadorArchivoTutores.class.getName()).log(Level.INFO, "Procesando archivo:''{0}''", getArchivoTutores().getAbsolutePath());
            //String txt=Archivo.getContenido(getArchivoTutores(), "latin1");
            Scanner sc = new Scanner(getArchivoTutores(), "latin1");
            //La primera linea la ignoramos
            if (sc.hasNextLine()) {
                sc.nextLine();
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE unidades SET capacidad=?, tutor_id=? WHERE id=? ");
                while (sc.hasNextLine()) {
                    String linea = sc.nextLine();
                    Matcher m = p.matcher(linea);
                    m.find();
                    String sUnidad = m.group(1);
                    //Ignoramos la columna de tipo
                    m.find();
                    //CAPACIDAD
                    m.find();
                    int capacidad = Num.getInt(m.group(1));
                    //Ignoramos la capacidad actual
                    //m.find(); Esta columna hScanner line delimitera desaparecido en 2009
                    //Profesor
                    m.find();
                    String sProfesor = m.group(1);
                    //El resto no nos interesa
                    //Ahora hay que buscar la unidad
                    Unidad u = Unidad.getUnidadPorNombreOriginal(getAnoEscolar(), sUnidad);
                    if (u == null) {
                        getErrores().add("No existe la unidad " + sUnidad);
                    }
                    //Ahora hay que buscar al profesor
                    int profesor = getProfesor(sProfesor);
                    if (u != null && profesor > 0) {
                        firePropertyChange("setMensaje", null, "Asignando tutor " + u.getCursoGrupo() + ": " + sProfesor);
                        st.setInt(1, capacidad);
                        st.setInt(2, profesor);
                        st.setInt(3, u.getId());
                        st.executeUpdate();
                    }
                }
                st.close();
                ret = true;
            }
            sc.close();
        } catch (Exception ex) {
            Logger.getLogger(ImportadorArchivoTutores.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
        }
        return ret;
    }

    private int getProfesor(String sProfesor) {
        int prof = 0;
        try {
            if (sProfesor.indexOf("(") > -1) {
                sProfesor = sProfesor.substring(0, sProfesor.indexOf("(")).trim();
            }
            if (!sProfesor.trim().equals("")) {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT id FROM profesores WHERE CONCAT(apellido1,' ',apellido2,', ',nombre)=? AND ano=?");
                st.setString(1, sProfesor);
                st.setInt(2, getAnoEscolar().getId());
                ResultSet res = st.executeQuery();
                if (res.next()) {
                    prof = res.getInt(1);
                }
                Obj.cerrar(st, res);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ImportadorArchivoTutores.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prof;
    }
}
