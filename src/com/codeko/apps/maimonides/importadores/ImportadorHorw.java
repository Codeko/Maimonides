package com.codeko.apps.maimonides.importadores;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.alumnos.MatriculacionAlumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.IObjetoBD;
import com.codeko.apps.maimonides.elementos.TramoHorario;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Codeko
 */
public class ImportadorHorw extends MaimonidesBean {

    private File archivoHorw = null;
    private AnoEscolar anoEscolar = null;
    private LinkedHashSet<String> unidadesNoExistentes = new LinkedHashSet<String>();
    private LinkedHashSet<String> asignaturasNoExistentes = new LinkedHashSet<String>();
    private LinkedHashSet<String> dependenciasNoExistentes = new LinkedHashSet<String>();
    private LinkedHashSet<String> profesoresNoExistentes = new LinkedHashSet<String>();
    private LinkedHashSet<String> unidadesRepetidas = new LinkedHashSet<String>();
    private LinkedHashSet<String> asignaturasRepetidas = new LinkedHashSet<String>();
    private LinkedHashSet<String> dependenciasRepetidas = new LinkedHashSet<String>();
    private LinkedHashSet<String> profesoresRepetidos = new LinkedHashSet<String>();
    private LinkedHashSet<String> erroresLineasHorw = new LinkedHashSet<String>();

    public LinkedHashSet<String> getErroresLineasHorw() {
        return erroresLineasHorw;
    }

    public LinkedHashSet<String> getAsignaturasNoExistentes() {
        return asignaturasNoExistentes;
    }

    public LinkedHashSet<String> getDependenciasNoExistentes() {
        return dependenciasNoExistentes;
    }

    public LinkedHashSet<String> getProfesoresNoExistentes() {
        return profesoresNoExistentes;
    }

    public LinkedHashSet<String> getUnidadesNoExistentes() {
        return unidadesNoExistentes;
    }

    public LinkedHashSet<String> getAsignaturasRepetidas() {
        return asignaturasRepetidas;
    }

    public LinkedHashSet<String> getDependenciasRepetidas() {
        return dependenciasRepetidas;
    }

    public LinkedHashSet<String> getProfesoresRepetidos() {
        return profesoresRepetidos;
    }

    public LinkedHashSet<String> getUnidadesRepetidas() {
        return unidadesRepetidas;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        firePropertyChange("anoEscolar", this.anoEscolar, anoEscolar);
        this.anoEscolar = anoEscolar;
    }

    public ImportadorHorw(AnoEscolar anoEscolar, File archivoHorw) {
        setAnoEscolar(anoEscolar);
        setArchivoHorw(archivoHorw);
    }

    public File getArchivoHorw() {
        return archivoHorw;
    }

    public final void setArchivoHorw(File archivoHorw) {
        firePropertyChange("archivoHorw", this.archivoHorw, archivoHorw);
        this.archivoHorw = archivoHorw;
    }

    public boolean importarHorarios() {
        boolean ret = false;
        try {
            firePropertyChange("setMensaje", null, "Procensando horarios de Horw...");
            Logger.getLogger(ImportadorHorw.class.getName()).log(Level.INFO, "Procesando archivo Horw:{0}", getArchivoHorw());
            PreparedStatement stSelectTramos = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM `tramos` WHERE ano=? AND fin-inicio>30 ORDER BY inicio ASC");
            stSelectTramos.setInt(1, getAnoEscolar().getId());
            ResultSet res = stSelectTramos.executeQuery();
            ArrayList<TramoHorario> tramos = new ArrayList<TramoHorario>();
            while (res.next()) {
                TramoHorario t = new TramoHorario();
                t.cargarDesdeResultSet(res);
                tramos.add(t);
            }
            Obj.cerrar(stSelectTramos, res);
            Scanner sc;
            Pattern p = Pattern.compile("\"([^\"]*)\"");
            try {
                sc = new Scanner(getArchivoHorw(),"latin1");
                Matcher m;
                ArrayList<IObjetoBD> horarios = new ArrayList<IObjetoBD>();
                while (sc.hasNextLine()) {
                    LineaHorw lineaH = new LineaHorw(this);
                    lineaH.setAnoEscolar(getAnoEscolar());
                    String linea = sc.nextLine();
                    m = p.matcher(linea);
                    ArrayList<String> datos = new ArrayList<String>();
                    while (m.find()) {
                        datos.add(m.group(1));
                    }
                    lineaH.cargarLinea(tramos, datos);
                    ArrayList<Horario> h = lineaH.getHorarios();
                    if (h != null && h.size() > 0) {
                        horarios.addAll(h);
                    } else {
                        getErroresLineasHorw().add("La siguiente linea no se ha procesado bien: " + linea);
                    }
                }
                ret = guardarObjetosBD(horarios);
                firePropertyChange("setMensaje", null, "Eliminando horarios residuales");
                eliminarHorariosRepetidos();
                //Ahora creamos la asignación de horarios
                firePropertyChange("setMensaje", null, "Asignado horarios a alumnos");
                MatriculacionAlumno.reasignarHorariosAlumnos();
                sc.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(ImportadorHorw.class.getName()).log(Level.SEVERE, "No se ha encontrado el archivo de Horw: " + getArchivoHorw(), ex);
            }

        } catch (Exception ex) {
            Logger.getLogger(ImportadorHorw.class.getName()).log(Level.SEVERE, "Error procesando archivo de Horw.", ex);
        } finally {
        }
        return ret;
    }

   

    private void eliminarHorariosRepetidos() throws SQLException {
        String sql = "select id from horarios WHERE ano=? " +
                " GROUP BY ano,dia,tramo_id,hora,aula_id,materia_id,profesor_id,actividad_id,unidad_id,dicu,borrado,activo " +
                " HAVING count(*) >1";
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        st.setInt(1, getAnoEscolar().getId());
        ResultSet res = st.executeQuery();
        PreparedStatement stBorrar = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE horarios WHERE id=? ");
        while (res.next()) {
            stBorrar.setInt(1, res.getInt(1));
            stBorrar.addBatch();
        }
        stBorrar.executeBatch();
        Obj.cerrar(stBorrar, st, res);
    }
}
