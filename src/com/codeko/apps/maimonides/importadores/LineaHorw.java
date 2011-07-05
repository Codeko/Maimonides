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
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Dependencia;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.TramoHorario;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Codeko
 */
public class LineaHorw {

    int diaSemana = 0;
    int hora = 0;
    String nombreAsignatura = "";
    String nombreProfesor = "";
    String nombreAula = "";
    String nombreUnidad = "";
    Integer idUnidad = null;
    //TODO Esto cambiarlo por IDCURSO1 e IDCRUSO2
    ArrayList<Integer> idCursos = new ArrayList<Integer>();
    AnoEscolar anoEscolar = null;
    int tramoHorario = 0;
    Integer idAsignatura = null;
    Integer idActividad = null;
    Integer idProfesor = null;
    Integer idAula = null;
    ImportadorHorw importador = null;
    boolean dicu = false;
    Integer idCursoActivo = null;
    int idAsignaturaHorw = 0;
    int idProfesorHorw = 0;
    int idUnidadHorw = 0;

    public int getIdAsignaturaHorw() {
        return idAsignaturaHorw;
    }

    public void setIdAsignaturaHorw(int idAsignaturaHorw) {
        this.idAsignaturaHorw = idAsignaturaHorw;
    }

    public int getIdProfesorHorw() {
        return idProfesorHorw;
    }

    public void setIdProfesorHorw(int idProfesorHorw) {
        this.idProfesorHorw = idProfesorHorw;
    }

    public int getIdUnidadHorw() {
        return idUnidadHorw;
    }

    public void setIdUnidadHorw(int idUnidadHorw) {
        this.idUnidadHorw = idUnidadHorw;
    }

    public Integer getIdCursoActivo() {
        if (idCursoActivo == null) {
            idCursoActivo = getIdCursos().get(0);
        }
        return idCursoActivo;
    }

    public void setIdCursoActivo(Integer idCursoActivo) {
        this.idCursoActivo = idCursoActivo;
    }

    public boolean isDicu() {
        return dicu;
    }

    public void setDicu(boolean dicu) {
        this.dicu = dicu;
    }

    public LineaHorw(ImportadorHorw importador) {
        setImportador(importador);
    }

    public ImportadorHorw getImportador() {
        return importador;
    }

    public final void setImportador(ImportadorHorw importador) {
        this.importador = importador;
    }

    public ArrayList<Integer> getIdCursos() {
        if (idCursos.isEmpty()) {
            getIdUnidad();
        }
        return idCursos;
    }

    public Integer getIdAula() {
        if (idAula == null) {
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT id FROM `dependencias` WHERE ano=? AND UPPER(nombre)=UPPER(?) ");
                st.setInt(1, getAnoEscolar().getId());
                st.setString(2, getNombreAula());
                res = st.executeQuery();
                if (res.next()) {
                    setIdAula(res.getInt(1));
                } else {
                    getImportador().getDependenciasNoExistentes().add(getNombreAula() + ". Se ha creado automáticamente.");
                    Dependencia d = new Dependencia();
                    d.setAnoEscolar(getAnoEscolar());
                    d.setCodigo(0);
                    d.setNombre(getNombreAula());
                    d.guardar();
                    setIdAula(d.getId());
                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "No existe dependencia: {0} para el a\u00f1o {1}", new Object[]{getNombreAula(), getAnoEscolar()});
                }
                if (res.next()) {
                    getImportador().getDependenciasRepetidas().add(getNombreAula());
                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "Hay dos dependencias: {0} para el a\u00f1o {1}", new Object[]{getNombreAula(), getAnoEscolar()});
                }

            } catch (SQLException ex) {
                Logger.getLogger(LineaHorw.class.getName()).log(Level.SEVERE, "Error recupernado id de aula para nombre '" + getNombreAula() + "'", ex);
            } finally {
                Obj.cerrar(st, res);
            }
        }
        return idAula;
    }

    public void setIdAula(Integer idAula) {
        this.idAula = idAula;
    }

    public Integer getIdProfesor() {
        if (idProfesor == null) {
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT id FROM `profesores` WHERE ano=? AND UPPER(concat(nombre,' ',apellido1,' ',apellido2)) LIKE CONCAT(UPPER(?),?) ");
                st.setInt(1, getAnoEscolar().getId());
                st.setString(2, getNombreProfesor());
                //Si el nombre es de tamaño 35 puede que sea mayor por lo que le añadimos el %
                //Le ponemos 34 por si es el espacio el último caracter y se ha eliminado
                if (getNombreProfesor().length() >= 34) {
                    st.setString(3, "%");
                } else {
                    st.setString(3, "");
                }
                res = st.executeQuery();
                if (res.next()) {
                    setIdProfesor(res.getInt(1));
                } else {

                    getImportador().getProfesoresNoExistentes().add(getNombreProfesor() + ". Se ha creado automáticamente.");
                    crearProfesor();
                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "No existe profesor: {0} para el a\u00f1o {1}", new Object[]{getNombreProfesor(), getAnoEscolar()});

                }
                if (res.next()) {
                    getImportador().getProfesoresRepetidos().add(getNombreProfesor());
                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "Hay dos profesores: {0} para el a\u00f1o {1}", new Object[]{getNombreProfesor(), getAnoEscolar()});
                }

            } catch (SQLException ex) {
                Logger.getLogger(LineaHorw.class.getName()).log(Level.SEVERE, "Error recupernado id de profesor para nombre '" + getNombreProfesor() + "'", ex);
            } finally {
                Obj.cerrar(st, res);
            }
        }
        return idProfesor;
    }

    public void setIdProfesor(Integer idProfesor) {
        this.idProfesor = idProfesor;
    }

    public Integer getIdAsignatura() {
        if (idAsignatura == null) {
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT id FROM `materias` WHERE ano=? AND curso_id=? AND UPPER(nombre) LIKE CONCAT(UPPER(?),?) ");
                st.setInt(1, getAnoEscolar().getId());
                st.setInt(2, getIdCursoActivo());
                st.setString(3, getNombreAsignatura());
                //Si el nombre es de tamaño 35 puede que sea mayor por lo que le añadimos el %
                //Le ponemos 34 por si es el espacio el último caracter y se ha eliminado
                if (getNombreAsignatura().length() >= 34) {
                    st.setString(4, "%");
                } else {
                    st.setString(4, "");
                }
                res = st.executeQuery();
                if (res.next()) {
                    setIdAsignatura(res.getInt(1));
                } else {
                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.WARNING, "No existe asignatura: {0} para la Unidad:{1} ({2}) Curso/s:{3} a\u00f1o {4}. Se verificar\u00e1 si es actividad m\u00e1s adelante.", new Object[]{getNombreAsignatura(), getNombreUnidad(), getIdUnidad(), getIdCursos(), getAnoEscolar()});
                }
                if (res.next() && getIdCursos().size() < 2) {
                    getImportador().getAsignaturasRepetidas().add(getNombreAsignatura() + " Unidad:" + getNombreUnidad() + " (" + getIdUnidad() + ") Curso/s:" + getIdCursos());
                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.WARNING, "Hay dos asignaturas: {0} para la Unidad:{1} ({2}) Curso/s:{3} a\u00f1o {4}", new Object[]{getNombreAsignatura(), getNombreUnidad(), getIdUnidad(), getIdCursos(), getAnoEscolar()});
                }
                st.close();
            } catch (SQLException ex) {
                Logger.getLogger(LineaHorw.class.getName()).log(Level.SEVERE, "Error recupernado id de asignatura para nombre '" + getNombreAsignatura() + "' ID CURSO/s: " + getIdCursos(), ex);
            } finally {
                Obj.cerrar(st, res);
            }
        }
        return idAsignatura;
    }

    public void setIdAsignatura(Integer idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    public Integer getIdActividad() {
        if (idActividad == null) {
            //Si hay asignatura la actividad es la docencia
            if (getIdAsignatura() != null && getIdAsignatura() > 0) {
                idActividad = Actividad.getIdActividadDocencia(getAnoEscolar());
            } else {
                PreparedStatement st = null;
                ResultSet res = null;
                try {
                    st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT id FROM `actividades` WHERE ano=? AND UPPER(descripcion) LIKE CONCAT(UPPER(?),?) ");
                    st.setInt(1, getAnoEscolar().getId());
                    st.setString(2, getNombreAsignatura());
                    //Si el nombre es de tamaño 35 puede que sea mayor por lo que le añadimos el %
                    //Le ponemos 34 por si es el espacio el último caracter y se ha eliminado
                    if (getNombreAsignatura().length() >= 34) {
                        st.setString(3, "%");
                    } else {
                        st.setString(3, "");
                    }
                    res = st.executeQuery();
                    if (res.next()) {
                        setIdActividad(res.getInt(1));
                    } else {

                        crearNuevaMateria();

                    }
                    if (res.next()) {
                        getImportador().getAsignaturasRepetidas().add(getNombreAsignatura() + " Unidad:" + getNombreUnidad() + " (" + getIdUnidad() + ") Curso/s:" + getIdCursos());
                        Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "Hay dos actividad: {0} para la Unidad:{1} ({2}) Curso/s:{3} a\u00f1o {4}", new Object[]{getNombreAsignatura(), getNombreUnidad(), getIdUnidad(), getIdCursos(), getAnoEscolar()});
                    }
                    st.close();
                } catch (SQLException ex) {
                    Logger.getLogger(LineaHorw.class.getName()).log(Level.SEVERE, "Error recupernado id de actividad para nombre '" + getNombreAsignatura() + "' ID CURSO/S: " + getIdCursos(), ex);
                } finally {
                    Obj.cerrar(st, res);
                }
            }
        }
        return idActividad;
    }

    public void setIdActividad(Integer idActividad) {
        this.idActividad = idActividad;
    }

    public int getTramoHorario() {
        return tramoHorario;
    }

    public void setTramoHorario(int tramoHorario) {
        this.tramoHorario = tramoHorario;
    }

    public void setIdUnidad(Integer idUnidad) {
        this.idUnidad = idUnidad;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public int getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(int diaSemana) {
        this.diaSemana = diaSemana;
    }

    public int getHora() {
        return hora;
    }

    public void setHora(int hora) {
        this.hora = hora;
    }

    public String getNombreAsignatura() {
        return nombreAsignatura;
    }

    public void setNombreAsignatura(String nombreAsignatura) {
        this.nombreAsignatura = nombreAsignatura;
    }

    public String getNombreAula() {
        return nombreAula;
    }

    public void setNombreAula(String nombreAula) {
        this.nombreAula = nombreAula;
    }

    public String getNombreProfesor() {
        return nombreProfesor;
    }

    public void setNombreProfesor(String nombreProfesor) {
        this.nombreProfesor = nombreProfesor;
    }

    public String getNombreUnidad() {
        return nombreUnidad;
    }

    public void setNombreUnidad(String nombreUnidad) {
        this.nombreUnidad = nombreUnidad;
    }

    public Integer getIdUnidad() {
        if (this.idUnidad == null) {
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT id,curso_id,curso2_id FROM `unidades` WHERE ano=? AND cursogrupo=?");
                st.setInt(1, getAnoEscolar().getId());
                st.setString(2, getNombreUnidad());
                res = st.executeQuery();
                if (res.next()) {
                    setIdUnidad(res.getInt(1));
                    getIdCursos().add(res.getInt(2));
                    int curso2 = res.getInt(3);
                    if (curso2 > 0) {
                        getIdCursos().add(curso2);
                    }
                } else {

                    getImportador().getUnidadesNoExistentes().add(getNombreUnidad());
                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "No existe unidad: {0} para el a\u00f1o {1} la linea de horario no se guardar\u00e1.", new Object[]{getNombreUnidad(), getAnoEscolar()});

                }
                if (res.next()) {
                    getImportador().getUnidadesRepetidas().add(getNombreUnidad());
                    Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "Hay dos unidades: {0} para el a\u00f1o {1}", new Object[]{getNombreUnidad(), getAnoEscolar()});
                }
            } catch (SQLException ex) {
                Logger.getLogger(LineaHorw.class.getName()).log(Level.SEVERE, "Error recupernado id de unidad para nombre '" + getNombreUnidad() + "'", ex);
            } finally {
                Obj.cerrar(st, res);
            }
        }
        return idUnidad;
    }

    public ArrayList<Horario> getHorarios() {
        ArrayList<Horario> horarios = new ArrayList<Horario>();
        if (getIdUnidad() != null && getIdUnidad().intValue() > 0) {
            //En las unidades mixtas existen diferentes asignaturas para cada curso pero que pueden ser la misma. Así que se puede
            //dar el caso de que una linea de horw corresponda a dos horarios el de la asignatura A para el curso A y el de la asignatura A para el curso B
            //Esto ocurre porque para Séneca cada asignatura sólo puede ser de un curso mientras que la unidad puede ser de varios.
            for (Integer idCurso : getIdCursos()) {
                setIdCursoActivo(idCurso);
                setIdActividad(null);
                setIdAsignatura(null);
                Horario h = new Horario();
                h.setAnoEscolar(getAnoEscolar());
                h.setDia(getDiaSemana());
                h.setTramo(getTramoHorario());
                h.setHora(getHora());
                h.setMateria(getIdAsignatura());
                h.setProfesor(getIdProfesor());
                h.setDependencia(getIdAula());
                h.setActividad(getIdActividad());
                //Volvemos a asignar la asignatura pues si al asignar la actividad no se ha encontrado se crea la asignatura como no evaluable
                h.setMateria(getIdAsignatura());
                h.setUnidad(getIdUnidad());
                h.setDicu(isDicu()?ParteFaltas.DICU_SOLO:ParteFaltas.DICU_NO);//TODO No estoy seguro de si esta conversión es correcta
                if (getIdAsignatura() != null && getIdAsignatura() > 0) {
                    try {
                        Materia m = Materia.getMateria(getIdAsignatura());
                        if (!m.isEvaluable()) {
                            //Ahora tenemos que matricular a todos los alumnos de la unidad
                            String sql = "REPLACE materias_alumnos (materia_id,alumno_id) SELECT ?,id FROM alumnos WHERE borrado=0 AND unidad_id=? ";
                            PreparedStatement stMatriculas = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                            stMatriculas.setInt(1, m.getId());
                            stMatriculas.setInt(2, getIdUnidad());
                            stMatriculas.executeUpdate();
                            stMatriculas.close();
                            Logger.getLogger(LineaHorw.class.getName()).log(Level.INFO, "Creando matriculaci\u00f3n para materia no evaluable: ''{0}'' para unidad: {1}", new Object[]{m, getIdUnidad()});
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(LineaHorw.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //Si no hay materia cancelamos la creación del horario
                if (h != null && (h.getActividad() == null || Num.getInt(h.getActividad()) == 0)) {
                    Logger.getLogger(LineaHorw.class.getName()).log(Level.SEVERE, "No se va a crear el Horario: {0} porque no tiene actividad. La asignatura era: {1}", new Object[]{h, getNombreAsignatura()});
                    h = null;
                } else {
                    horarios.add(h);
                }
            }
        }
        return horarios;
    }

    public void cargarLinea(ArrayList<TramoHorario> tramos, ArrayList<String> datos) {
        //El primer campo es el día de la semana
        setDiaSemana(Num.getInt(datos.get(0)));
        //El segundo campo la hora del día
        setHora(Num.getInt(datos.get(1)));
        //asignamos el tramo horario correspondiente
        //Con la hora tenemos que calcular el tramo
        setTramoHorario(tramos.get(hora - 1).getId());
        setNombreAsignatura(datos.get(2));
        setIdAsignaturaHorw(Num.getInt(datos.get(3)));
        setNombreProfesor(datos.get(5));
        setIdProfesorHorw(Num.getInt(datos.get(6)));
        setNombreAula(datos.get(8));
        String nomUnidad = datos.get(11);
        //Vemos si la unidad es dicu
        setDicu(nomUnidad.contains("DICU"));
        nomUnidad = nomUnidad.replace("DICU", "").trim();
        setNombreUnidad(nomUnidad);
        setIdUnidadHorw(Num.getInt(datos.get(12)));
    }

    private void crearNuevaMateria() {
        boolean crearCurso = true;
        //Si la asignatura no existe para el curso activo pero si para otro curso no la creamos
        if (getIdCursos().size() > 1) {
            int cursoAProbar = -1;
            for (Integer idCur : getIdCursos()) {
                if (!idCur.equals(getIdCursoActivo())) {
                    cursoAProbar = idCur;
                }
            }
            //Ahora buscamos la asignatura
            int cursoActivoReal = getIdCursoActivo();
            setIdCursoActivo(cursoAProbar);
            Integer materia = getIdAsignatura();
            if (materia != null) {
                crearCurso = false;
            }
            setIdCursoActivo(cursoActivoReal);
            setIdAsignatura(null);
        }
        if (crearCurso) {
            try {
                //Si no existe la materia la creamos como no evaluable para los dos cursos
                for (Integer idCurso : getIdCursos()) {
                    Materia m = new Materia();
                    m.setAnoEscolar(getAnoEscolar());
                    m.setCodigo(0);
                    m.setCurso(Curso.getCurso(idCurso));
                    m.setDescripcion(getNombreAsignatura());
                    m.setEvaluable(false);
                    m.setMaximoAlumnos(33);
                    idActividad = Actividad.getIdActividadDocencia(getAnoEscolar());
                    //Vamos a crearla
                    if (m.guardar()) {
                        Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.INFO, "No existe actividad: {0} para la Unidad:{1} ({2}) Curso:{3} a\u00f1o {4}. Se ha creado como asignatura no evaluable.", new Object[]{getNombreAsignatura(), getNombreUnidad(), getIdUnidad(), getIdCursoActivo(), getAnoEscolar()});
                        setIdAsignatura(m.getId());
                    } else {
                        Logger.getLogger(ImportadorArchivosMatriculas.class.getName()).log(Level.WARNING, "No se ha podido crear la asignatura {0} como no evaluable.", m);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(LineaHorw.class.getName()).log(Level.SEVERE, null, ex);
            }
            getImportador().getAsignaturasNoExistentes().add(getNombreAsignatura() + " Unidad:" + getNombreUnidad() + " (" + getIdUnidad() + ") Curso/s:" + getIdCursos() + ". Creada como no evaluable.");
        }
    }

    private void crearProfesor() {
        //Ahora tenemos que crear el profesor
        try {
            Profesor prof = new Profesor();
            prof.setAnoEscolar(getAnoEscolar());
            prof.setPuesto("Desconocido");
            prof.setCodigo(0);
            //Ahora intentamos sacar el nombre y apellidos
            String nom = getNombreProfesor();
            if (nom.trim().equals("")) {
                nom = "- - -";
            }
            String secApp = "";
            String primApp = "";
            if (nom.lastIndexOf(" ") != -1) {
                secApp = nom.substring(nom.lastIndexOf(" ")).trim();
                nom = nom.substring(0, nom.lastIndexOf(" ")).trim();
                if (nom.lastIndexOf(" ") != -1) {
                    primApp = nom.substring(nom.lastIndexOf(" ")).trim();
                    nom = nom.substring(0, nom.lastIndexOf(" ")).trim();
                }
            }
            prof.setNombre(nom);
            prof.setApellido1(primApp);
            prof.setApellido2(secApp);
            prof.guardar();
            setIdProfesor(prof.getId());
        } catch (Exception e) {
            Logger.getLogger(LineaHorw.class.getName()).log(Level.SEVERE, "Error creando profesor '" + getNombreProfesor() + "'", e);
        }
    }
}
