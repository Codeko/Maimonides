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
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.materias.ControlMatriculas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.jdesktop.application.Task;

/**
 *
 * @author Codeko
 */
public class ImportadorArchivosMatriculas extends MaimonidesBean {

    private ArrayList<File> archivosMatriculas = null;
    private AnoEscolar anoEscolar = null;
    private ArrayList<String> alumnosNoEncontrados = new ArrayList<String>();
    boolean cancelado = false;

    public ArrayList<String> getAlumnosNoEncontrados() {
        return alumnosNoEncontrados;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        firePropertyChange("anoEscolar", this.anoEscolar, anoEscolar);
        this.anoEscolar = anoEscolar;
    }

    public ImportadorArchivosMatriculas(AnoEscolar anoEscolar, ArrayList<File> archivosMatriculas) {
        setAnoEscolar(anoEscolar);
        setArchivosMatriculas(archivosMatriculas);
    }

    public ArrayList<File> getArchivosMatriculas() {
        return archivosMatriculas;
    }

    public final void setArchivosMatriculas(ArrayList<File> archivosMatriculas) {
        firePropertyChange("archivosMatriculas", this.archivosMatriculas, archivosMatriculas);
        this.archivosMatriculas = archivosMatriculas;
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
    }

    public boolean importarMatriculasExcel(Task tarea) {
        boolean ret = false;
        getAlumnosNoEncontrados().clear();
        ControlMatriculas control = new ControlMatriculas();
        control.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });

        firePropertyChange("message", null, "Procensando matrículas...");

        for (File f : getArchivosMatriculas()) {
            if (tarea != null && tarea.isCancelled()) {
                firePropertyChange("message", null, "Cancelando...");
                break;
            }
            Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "Procesando archivo de matr\u00edculas:{0}", f);
            //Cada archivo hay que procesarlo
            try {
                PreparedStatement stDicu = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE alumnos SET dicu=? WHERE id=?");
                //Ahora vamos necesitamos cada código de asignatura
                ArrayList<Integer> codMaterias = new ArrayList<Integer>();
                ArrayList<String> nombreMaterias = new ArrayList<String>();
                FileInputStream fis = new FileInputStream(f);
                POIFSFileSystem fs = new POIFSFileSystem(fis);
                HSSFWorkbook wb = new HSSFWorkbook(fs);
                HSSFSheet sheet = wb.getSheetAt(0);
                Iterator rows = sheet.rowIterator();
                //Las 4 primeras lineas no nos interesan
                for (int i = 0; i < 3; i++) {
                    rows.next();
                }
                //La quinta es la cabecera
                procesarCabeceraExcel((HSSFRow) rows.next(), nombreMaterias, codMaterias);
                while (rows.hasNext() && !isCancelado()) {
                    if (tarea != null && tarea.isCancelled()) {
                        break;
                    }
                    HSSFRow row = (HSSFRow) rows.next();
                    Iterator cells = row.cellIterator();
                    HSSFCell celdaNombre = (HSSFCell) cells.next();
                    String nombre = celdaNombre.getRichStringCellValue().getString();
                    boolean esDicu = false;
                    if (nombre != null && !nombre.trim().equals("Total")) {
                        nombre = nombre.trim();
                        firePropertyChange("message", null, "Procensando matrículas: " + nombre + "...");
                        Alumno a = Alumno.getAlumno(nombre);
                        //Avanzamos al campo cursogrupo
                        HSSFCell celdaUnidad = (HSSFCell) cells.next();
                        String nombreOriginal = celdaUnidad.getRichStringCellValue().getString();
                        if (a != null && a.getId() != null) {
                            if (a.getUnidad()==null || !a.getUnidad().getNombreOriginal().equals(nombreOriginal)) {
                                try {
                                    //TODO Implementar mensaje de cambio de unidad a.getMensajesUltimaOperacion()
                                    Unidad u = Unidad.getUnidadPorNombreOriginal(getAnoEscolar(), nombreOriginal);
                                    a.setUnidad(u);
                                    if(a.getObjetoCurso()==null){
                                        a.setIdCurso(u.getIdCurso());
                                    }
                                    a.guardar();
                                    firePropertyChange("cambioUnidad", a, u);
                                } catch (Exception e) {
                                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.SEVERE, "Error cargando unidad para '" + nombreOriginal + "'", e);
                                }
                            }
                            int cont = 0;
                            //Ahora vamos avanzando en cada matricula
                            while (cells.hasNext()) {
                                HSSFCell celda = (HSSFCell) cells.next();
                                String val = celda.getRichStringCellValue().getString();
                                //Aquí se dan asignaturas que no estaban en el otro fichero.
                                Materia materia = Materia.confirmarExistencia(codMaterias.get(cont), nombreMaterias.get(cont), a);
                                boolean matricular = (val.equals("MATR") || val.equals("PEND"));
                                //Hacemos la asignación
                                control.matricular(a, materia, matricular);
//                                if (!actu) {
//                                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.WARNING, "El alumno " + a + " ya tenía asignada la asignatura " + codMaterias.elementAt(cont) + ".");
//                                }
                                //vemos si la asignatura es dicu y entonces marcamos al alumnos como dicu
                                if (matricular && nombreMaterias.get(cont).toLowerCase().contains("ámbito")) {
                                    esDicu = true;
                                }
                                cont++;
                                materia = null;
                                val = null;
                            }
                            try {
                                stDicu.setBoolean(1, esDicu);
                                stDicu.setInt(2, a.getId());
                                stDicu.executeUpdate();
                            } catch (Exception ex) {
                                Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.SEVERE, "Error aplicando dicu a alumno '" + a + "'", ex);
                            }
                        } else {
                            Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.FINE, "El alumno {0} no existe.", nombre);
                            getAlumnosNoEncontrados().add(nombre + " (" + nombreOriginal + "). Fichero de matrícula: '" + f.getName() + "'");
                        }
                        a = null;
                    }
                }
                Obj.cerrar(stDicu, fis);
            } catch (Exception ex) {
                Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        ret = true;

        if (ret) {
            limpiarMatriculasCursosAnteriores();
        }
        return ret;
    }

    public boolean importarMatriculas() {
        boolean ret = false;
        getAlumnosNoEncontrados().clear();
        ControlMatriculas control = new ControlMatriculas();
        control.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            }
        });

        firePropertyChange("message", null, "Procensando matrículas...");
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        for (File f : getArchivosMatriculas()) {
            Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "Procesando archivo de matr\u00edculas:{0}", f);
            //Cada archivo hay que procesarlo
            Scanner sc = null;
            try {
                PreparedStatement stDicu = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE alumnos SET dicu=? WHERE id=?");
                sc = new Scanner(f, "latin1");
                String primeraLinea = sc.nextLine();
                //Ahora vamos necesitamos cada código de asignatura
                ArrayList<Integer> codMaterias = new ArrayList<Integer>();
                ArrayList<String> nombreMaterias = new ArrayList<String>();
                Matcher m = procesarCabecera(primeraLinea, nombreMaterias, codMaterias);
                primeraLinea = null;
                //ya tenemos asignadas las asignaturas
                while (sc.hasNextLine()) {
                    String linea = sc.nextLine();
                    m = p.matcher(linea);
                    m.find();
                    String nombre = m.group(1);
                    boolean esDicu = false;
                    if (nombre != null && !nombre.trim().equals("Total")) {
                        nombre = nombre.trim();
                        firePropertyChange("message", null, "Procensando matrículas: " + nombre + "...");
                        Alumno a = Alumno.getAlumno(nombre);
                        //Avanzamos al campo cursogrupo
                        m.find();
                        String nombreOriginal = m.group(1);
                        if (a != null && a.getId() != null) {
                            if (!a.getUnidad().getNombreOriginal().equals(nombreOriginal)) {
                                try {
                                    //TODO Implementar mensaje de cambio de unidad a.getMensajesUltimaOperacion()
                                    Unidad u = Unidad.getUnidadPorNombreOriginal(getAnoEscolar(), nombreOriginal);
                                    a.setUnidad(u);
                                    a.guardar();
                                    firePropertyChange("cambioUnidad", a, u);
                                } catch (Exception e) {
                                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.SEVERE, "Error cargando unidad para '" + nombreOriginal + "'", e);
                                }
                            }
                            int cont = 0;
                            //Ahora vamos avanzando en cada matricula
                            while (m.find()) {
                                String val = m.group(1);
                                //Aquí se dan asignaturas que no estaban en el otro fichero.
                                Materia materia = Materia.confirmarExistencia(codMaterias.get(cont), nombreMaterias.get(cont), a);
                                boolean matricular = (val.equals("MATR") || val.equals("PEND"));
                                //Hacemos la asignación
                                control.matricular(a, materia, matricular);
//                                if (!actu) {
//                                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.WARNING, "El alumno " + a + " ya tenía asignada la asignatura " + codMaterias.elementAt(cont) + ".");
//                                }
                                //vemos si la asignatura es dicu y entonces marcamos al alumnos como dicu
                                if (matricular && nombreMaterias.get(cont).toLowerCase().contains("ámbito")) {
                                    esDicu = true;
                                }
                                cont++;
                                materia = null;
                                val = null;
                            }
                            try {
                                stDicu.setBoolean(1, esDicu);
                                stDicu.setInt(2, a.getId());
                                stDicu.executeUpdate();
                            } catch (Exception ex) {
                                Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.SEVERE, "Error aplicando dicu a alumno '" + a + "'", ex);
                            }
                        } else {
                            Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.FINE, "El alumno {0} no existe.", nombre);
                            getAlumnosNoEncontrados().add(nombre + " (" + nombreOriginal + "). Fichero de matrícula: '" + f.getName() + "'");
                        }
                        a = null;
                    }
                }
                stDicu.close();
            } catch (Exception ex) {
                Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.SEVERE, null, ex);
            }
            sc = null;
        }
        ret = true;

        if (ret) {
            limpiarMatriculasCursosAnteriores();
        }
        return ret;
    }

    private void limpiarMatriculasCursosAnteriores() {
        //tenemos que quitar las matriculaciones de repeticiones de años anteriores. 
        PreparedStatement stBorrar = null;
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            stBorrar = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM materias_alumnos WHERE materia_id=? AND alumno_id=? ");
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("select ma.* from alumnos AS a JOIN unidades AS u ON a.unidad_id=u.id "
                    + " JOIN materias_alumnos AS ma ON ma.alumno_id=a.id "
                    + " JOIN materias AS m ON m.id=ma.materia_id "
                    + " WHERE u.curso_id!=m.curso_id AND u.curso2_id!=m.curso_id  AND m.ano=?");
            st.setInt(1, getAnoEscolar().getId());
            res = st.executeQuery();
            while (res.next()) {
                stBorrar.setInt(1, res.getInt("materia_id"));
                stBorrar.setInt(2, res.getInt("alumno_id"));
                stBorrar.addBatch();
            }
            stBorrar.executeBatch();
        } catch (SQLException ex) {
            Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(stBorrar, st, res);
        }
    }

    private Matcher procesarCabecera(String primeraLinea, ArrayList<String> nombresAsignaturas, ArrayList<Integer> asignaturas) {
        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Pattern p2 = Pattern.compile("^(.*)\\( ([0-9]*) \\)$");
        //Procesamos la primera linea
        Matcher m = p.matcher(primeraLinea);
        //Los dos primeros campos no nos interesan
        m.find();
        m.find();
        firePropertyChange("message", null, "Procensando matrículas: procesando cabecera...");
        while (m.find()) {
            String sCodAsign = m.group(1);
            Matcher m2 = p2.matcher(sCodAsign);
            if (m2.find()) {
                nombresAsignaturas.add(m2.group(1));
                asignaturas.add(Num.getInt(m2.group(2)));
            }
        }
        primeraLinea = null;
        return m;
    }

    private void procesarCabeceraExcel(HSSFRow row, ArrayList<String> nombresAsignaturas, ArrayList<Integer> asignaturas) {
        Pattern p = Pattern.compile("^(.*)\\( ([0-9]*) \\)$");
        //Procesamos la primera linea
        Iterator cells = row.cellIterator();
        //Los dos primeros campos no nos interesan
        cells.next();
        cells.next();
        firePropertyChange("message", null, "Procensando matrículas: procesando cabecera...");
        while (cells.hasNext()) {
            HSSFCell celda = (HSSFCell) cells.next();
            String sCodAsign = celda.getRichStringCellValue().getString();
            Matcher m = p.matcher(sCodAsign);
            if (m.find()) {
                nombresAsignaturas.add(m.group(1));
                asignaturas.add(Num.getInt(m.group(2)));
            }
        }
    }
}
