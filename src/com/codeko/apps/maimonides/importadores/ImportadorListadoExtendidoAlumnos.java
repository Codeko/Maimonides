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

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Tutor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ImportadorListadoExtendidoAlumnos extends MaimonidesBean {

    File archivoAlumnosSeneca = null;
    AnoEscolar ano = null;
    boolean crearNuevos = true;
    boolean borrarNoExistentes = false;
    ArrayList<Unidad> unidades = new ArrayList<Unidad>();
    boolean cancelado = false;

    public boolean isCancelado() {
        return cancelado;
    }

    public void setCancelado(boolean cancelado) {
        this.cancelado = cancelado;
    }

    public AnoEscolar getAno() {
        return ano;
    }

    public final void setAno(AnoEscolar ano) {
        this.ano = ano;
    }

    public File getArchivoAlumnosSeneca() {
        return archivoAlumnosSeneca;
    }

    public final void setArchivoAlumnosSeneca(File archivoAlumnosSeneca) {
        this.archivoAlumnosSeneca = archivoAlumnosSeneca;
    }

    public boolean isBorrarNoExistentes() {
        return borrarNoExistentes;
    }

    public void setBorrarNoExistentes(boolean borrarNoExistentes) {
        this.borrarNoExistentes = borrarNoExistentes;
    }

    public boolean isCrearNuevos() {
        return crearNuevos;
    }

    public void setCrearNuevos(boolean crearNuevos) {
        this.crearNuevos = crearNuevos;
    }

    public ArrayList<Unidad> getUnidades() {
        return unidades;
    }

    public void setUnidades(ArrayList<Unidad> unidades) {
        this.unidades = unidades;
    }

    public ImportadorListadoExtendidoAlumnos(AnoEscolar ano, File fichero) {
        setAno(ano);
        setArchivoAlumnosSeneca(fichero);
    }

    public boolean importar() {
        boolean ret = false;
        boolean crearCursosUnidades = true;
        try {
            FileInputStream fis = new FileInputStream(getArchivoAlumnosSeneca());
            POIFSFileSystem fs = new POIFSFileSystem(fis);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.rowIterator();
            //La primera linea es de cabeceras y la saltamos
            //Las 5 primeras lineas no nos interesan
            for (int i = 0; i < 5; i++) {
                HSSFRow row = (HSSFRow) rows.next();
                Iterator<Cell> cells = row.cellIterator();
                HSSFCell cell = (HSSFCell) cells.next();
                String c = cell.toString();
                if (c.toLowerCase().equals("Alumno/a".toLowerCase())) {
                    break;
                }
            }
            //Creamos una cache de alumnos para evitar el problema de las dobles matriculaciones
            ArrayList<Alumno> alumnos = new ArrayList<Alumno>();
            while (rows.hasNext() && !isCancelado()) {
                HSSFRow row = (HSSFRow) rows.next();
                Iterator<Cell> cells = row.cellIterator();
                Alumno a = null;
                boolean borrar = false;
                int pos = -1;
                boolean continuar = true;
                String sCurso = "";
                while (cells.hasNext() && continuar && !isCancelado()) {
                    pos++;
                    HSSFCell cell = (HSSFCell) cells.next();
                    String c = cell.toString();
                    if (c.trim().equals("")) {
                        Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.FINE, "Campo {0} vacio.", pos);
                    } else {
                        switch (pos) {
                            case 0://Nombre ignorado
                                break;
                            case 1://Estado matricula
                                if (c.toLowerCase().equals("anulada") || c.toLowerCase().equals("trasladada")) {
                                    borrar = true;
                                    Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.INFO, "Marcando alumno para borrar por {0}", c);
                                }
                                break;
                            case 2://Num escolar
                                a = Alumno.getAlumnoDesdeNumEscolar(c);

                                if (a != null) {
                                    //Vemos si intentamos borrar un alumno que en el mismo fichero dice que esta matriculado
                                    //debido al problema de doble matrícula de Séneca
                                    if (borrar && alumnos.contains(a)) {
                                        borrar = false;
                                        continuar = false;//Y dejamos de leer esta linea
                                        Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.INFO, "Ignorando borrado de alumno matriculado en el mismo fichero {0}", a);
                                    } else {
                                        if (borrar && !a.isBorrado()) {
                                            Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.INFO, "Borrando alumno {0}", a);
                                            a.borrar();
                                            firePropertyChange("borrarAlumno", null, a);
                                            continuar = false;//Y dejamos de leer esta linea
                                            a = null;
                                        } else if (!borrar && a.isBorrado()) {
                                            //Si se ha borrado se recupera y se marca como nuevo alumno
                                            a.setBorrado(false);
                                            firePropertyChange("nuevoAlumno", null, a);
                                        }
                                    }
                                } else if (!borrar) {
                                    //Hay que crearlo
                                    Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.INFO, "Creando nuevo alumno con numero escolar ''{0}''", c);
                                    a = new Alumno();
                                    a.setNumeroEscolar(c);
                                    firePropertyChange("nuevoAlumno", null, a);
                                } else {
                                    Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).info("Alumno no existente y borrado, ignorado...");
                                    continuar = false;//Y dejamos de leer esta linea
                                }
                                //Añadimos el alumno a la cache
                                if (a != null && !alumnos.contains(a)) {
                                    alumnos.add(a);
                                }
                                break;
                            case 3://DNI
                                a.setDni(c);
                                break;
                            case 4://Direccion
                                a.setDireccion(c);
                                break;
                            case 5://Cp
                                a.setCp(c);
                                break;
                            case 6://Localidad
                                a.setPoblacion(c);
                                break;
                            case 7://Fecha nacimiento
                                try {
                                    Date cal = Fechas.parse(c, "dd/MM/yyyy");
                                    GregorianCalendar fn = new GregorianCalendar();
                                    fn.setTime(cal);
                                    a.setFechaNacimiento(fn);
                                } catch (Exception e) {
                                    Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.SEVERE, "Error procesando fecha '" + c + "' de alumno " + a + "", e);
                                }
                                break;
                            case 8://Provincia, se ignora ya que viene dado por el CP
                                break;
                            case 9://Telefono
                                a.setTelefono(c);
                                break;
                            case 10://Telefono de urgencias
                                a.setTelefonoUrgencia(c);
                                break;
                            case 11://Curso. No nos vale porque está escrito como les da la gana
                                sCurso = c;//Pero quizás lo necesitemos tras asignar la unidad
                                break;
                            case 12://Expendiente
                                a.setExpediente(c);
                                break;
                            case 13://Unidad
                                try {
                                    Unidad u = Unidad.getUnidadPorNombreOriginal(getAno(), c);
                                    if (crearCursosUnidades && u == null) {
                                        Curso cursoActual = Curso.getCurso(getAno(), sCurso);
                                        if (cursoActual == null) {
                                            cursoActual = new Curso(getAno());
                                            cursoActual.setDescripcion(sCurso);
                                            cursoActual.setCurso(sCurso.charAt(0) + "");
                                            cursoActual.guardar();
                                        }
                                        u = new Unidad();
                                        u.setAnoEscolar(getAno());
                                        u.setIdCurso(cursoActual.getId());
                                        u.setDescripcion(sCurso);
                                        u.cargarDatosImportacionDesdeNombre(c);
                                        u.guardar();
                                        if (!(cursoActual.getCurso().length() > 1)) {
                                            if (u.getCurso().startsWith(cursoActual.getCurso())) {
                                                cursoActual.setCurso(u.getCurso());
                                            } else {
                                                cursoActual.setCurso(cursoActual.getCurso() + u.getCurso());
                                            }
                                            cursoActual.guardar();
                                        }
                                    }

                                    if (u != null) {
                                        a.setUnidad(u);
                                        //de aquí tenemos que sacar el curso
                                        if (u.getIdCurso2() != null) {
                                            //Si la unidad tiene 2 cursos el alumno puede estar en uno u otro
                                            //Si ya está asignado no hacemos nada logicamente
                                            if (a.getIdCurso() == null) {
                                                //Si no tenemos que ver si el texto de curso es de uno o de otro
                                                Curso c1 = Curso.getCurso(u.getIdCurso());
                                                Curso c2 = Curso.getCurso(u.getIdCurso2());
                                                if (sCurso.equals(c2.getDescripcion())) {
                                                    a.setIdCurso(c2.getId());
                                                } else if (sCurso.equals(c1.getDescripcion())) {
                                                    a.setIdCurso(c1.getId());
                                                } else {
                                                    firePropertyChange("errorCurso", c, a);
                                                }
                                            }
                                        } else {
                                            a.setIdCurso(u.getIdCurso());
                                        }
                                    } else {
                                        firePropertyChange("errorUnidad", c, a);
                                        Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.SEVERE, "No existe la unidad ''{0}''", c);
                                    }
                                } catch (Exception ex) {
                                    firePropertyChange("errorUnidad", c, a);
                                    Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.SEVERE, "No existe la unidad '" + c + "'", ex);
                                }
                                break;
                            case 14://Primer apellido
                                a.setApellido1(c);
                                break;
                            case 15://Segundo apellido
                                a.setApellido2(c);
                                break;
                            case 16://Nombre
                                a.setNombre(c);
                                break;
                            case 17://Dni tutor 1
                                getTutor(a, 1).setDni(c);
                                break;
                            case 18://Primer apellido tutor 1
                                getTutor(a, 1).setApellido1(c);
                                break;
                            case 19://Segundo apellido tutor 1
                                getTutor(a, 1).setApellido2(c);
                                break;
                            case 20://Nombre tutor 1
                                getTutor(a, 1).setNombre(c);
                                break;
                            case 21://Sexo tutor 1
                                getTutor(a, 1).setSexo(c);
                                break;
                            case 22://Dni tutor 2
                                getTutor(a, 2).setDni(c);
                                break;
                            case 23://Primer apellido tutor 2
                                getTutor(a, 2).setApellido1(c);
                                break;
                            case 24://Segundo apellido tutor 2
                                getTutor(a, 2).setApellido2(c);
                                break;
                            case 25://Nombre tutor 2
                                getTutor(a, 2).setNombre(c);
                                break;
                            case 26://Sexo tutor 2
                                getTutor(a, 2).setSexo(c);
                                break;
                            case 27://Localidad nacimiento
                                a.setLocalidadNacimiento(c);
                                break;
                            case 28://Año matricual
                                break;
                            case 29://Numero de mtraiculas en este curso
                                break;
                            case 30://Observaciones matricula
                                if (!a.getObservaciones().toLowerCase().contains(c.toLowerCase())) {
                                    if (!a.getObservaciones().trim().equals("")) {
                                        a.setObservaciones(a.getObservaciones() + "\n");
                                    }
                                    a.setObservaciones(a.getObservaciones() + "Observaciones matrícula:\n" + c);
                                }
                                break;
                            case 31://provincia nacimiento
                                a.setProvinciaNacimiento(c);
                                break;
                            case 32://Pais nacimeinto
                                a.setPaisNacimiento(c);
                                break;
                            case 33://Edad a fecha.... bla bla
                                break;
                            case 34://Nacionalidad
                                a.setNacionalidad(c);
                                break;
                            case 35://Sexo
                                a.setSexo(c);
                                break;
                        }
                    }
                }
                if (a != null && !isCancelado()) {
                    firePropertyChange("procesado", null, a);
                    firePropertyChange("message", null, "Alumno: " + a);
                    try {
                        if (!a.guardar()) {
                            firePropertyChange("errorGuardando", null, a);
                        } else {
                            firePropertyChange("guardado", null, a);
                        }
                    } catch (Exception e) {
                        Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.SEVERE, "Error guardando alumno " + a + "", e);
                        firePropertyChange("errorGuardando", e, a);
                    }
                }
            }
            alumnos = null;//Ayudando al GC
            Obj.cerrar(fis);
            ret = true;
        } catch (Exception ex) {
            Logger.getLogger(ImportadorListadoExtendidoAlumnos.class.getName()).log(Level.SEVERE, null, ex);
            firePropertyChange("errorGeneral", ex, ex.getLocalizedMessage());
        }
        return ret;
    }

    public Tutor getTutor(Alumno a, int numTutor) {
        Tutor t = null;
        if (a.getTutor() != null) {
            t = a.getTutor();
        } else {
            t = new Tutor(Num.getInt(a.getId()), numTutor);
            if (numTutor == 1) {
                a.setTutor(t);
            } else {
                a.setTutor2(t);
            }
        }
        return t;
    }
}
