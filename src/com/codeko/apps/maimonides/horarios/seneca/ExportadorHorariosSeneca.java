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


package com.codeko.apps.maimonides.horarios.seneca;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.Dependencia;
import com.codeko.apps.maimonides.elementos.Materia;

import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.TramoHorario;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.mantenimiento.Mantenimiento;
import com.codeko.util.Archivo;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
public class ExportadorHorariosSeneca extends MaimonidesBean {
//TODO Implementar el envío directo a Séneca
    public File exportarHorariosXMLSeneca() throws IOException {
        File ficheroXml =null;
        //Primero precargamos los profesores, unidades, etc
        firePropertyChange("message", null, "Precargando datos de profesores...");
        Profesor.getProfesores();
        firePropertyChange("message", null, "Precargando datos de unidades...");
        Unidad.getUnidades();
        firePropertyChange("message", null, "Precargando datos de dependencias...");
        Dependencia.getDependencias();
        firePropertyChange("message", null, "Precargando datos de tramos horarios...");
        TramoHorario.getTramosHorarios();
        firePropertyChange("message", null, "Precargando datos de materias...");
        Materia.getMaterias();
        firePropertyChange("message", null, "Precargando datos de actividades...");
        Actividad.getActividades();
        firePropertyChange("message", null, "Precargando datos de unidades...");
        Unidad.getUnidades();
        firePropertyChange("message", null, "Precargando datos de cursos...");
        Curso.getCursos();
        int contadorProfesores = 1;
        int contadorActividad = 1;
        //Ahora cargamos todos los horarios del año escolar
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            firePropertyChange("message", null, "Procesando horarios...");
            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement("SELECT * FROM horarios_ WHERE ano=? ORDER BY profesor_id ");
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            res = st.executeQuery();
            int ultimoProfesor = -1;
            ArrayList<String> bloquesProfesores = new ArrayList<String>();
            ArrayList<String> bloquesActividad = new ArrayList<String>();
            while (res.next()) {
                try {
                    int idProfesor = res.getInt("profesor_id");
                    int dia = res.getInt("dia");
                    int idMateria = res.getInt("materia_id");
                    int idActividad = res.getInt("actividad_id");
                    int idUnidad = res.getInt("unidad_id");
                    int idDependencia = res.getInt("aula_id");
                    Dependencia dependencia = null;
                    if (idDependencia > 0) {
                        try {
                            dependencia = Dependencia.getDependencia(idDependencia);
                        } catch (Exception ex) {
                            Logger.getLogger(ExportadorHorariosSeneca.class.getName()).log(Level.SEVERE, "No existe la dependencia " + idDependencia + ".", ex);
                        }
                    }
                    TramoHorario tramo = TramoHorario.getTramoHorario(res.getInt("tramo_id"));
                    Materia materia = null;
                    Actividad actividad = Actividad.getActividad(idActividad);
                    Unidad unidad = Unidad.getUnidad(idUnidad);
                    Curso curso = Curso.getCurso(unidad.getIdCurso());
                    if (idMateria > 0) {
                        materia = Materia.getMateria(idMateria);
                    }
                    if (idProfesor != ultimoProfesor) {
                        //Si cambiamos de profesor tenemos que crear un nuevo registro
                        //si el profesor anterior no era -1
                        if (ultimoProfesor != -1) {
                            String bloqueProfesor = getBloqueProfesor(contadorProfesores, idProfesor, bloquesActividad);
                            if (bloqueProfesor != null) {
                                bloquesProfesores.add(bloqueProfesor);
                                contadorProfesores++;
                            }
                            bloquesActividad.clear();
                            contadorActividad = 1;
                        }
                    }
                    ultimoProfesor = idProfesor;
                    //Añadimos el bloque de actividades
                    bloquesActividad.add(getXMLBloqueHorario(MaimonidesApp.getApplication().getAnoEscolar(), contadorActividad, dia, tramo, dependencia, curso, unidad, actividad, materia));
                    contadorActividad++;
                } catch (Exception e) {
                    Logger.getLogger(ExportadorHorariosSeneca.class.getName()).log(Level.SEVERE, "Ha habido un error procesando el horario.", e);
                }
            }
            //Añadimos el último profesor
            if (ultimoProfesor != -1) {
                String bloqueProfesor = getBloqueProfesor(contadorProfesores, ultimoProfesor, bloquesActividad);
                if (bloqueProfesor != null) {
                    bloquesProfesores.add(bloqueProfesor);
                }
            }
            //Ahora generamos el resto del documento
            GregorianCalendar fecha = new GregorianCalendar();
            StringBuilder xml = new StringBuilder();
            xml.append(String.format("<SERVICIO modulo=\"HORARIOS\" tipo=\"I\" autor=\"Maimonides %s\" fecha=\"%2$tm/%2$td/%2$tY %tT\">\n", Mantenimiento.getAplicationVersion(), fecha, fecha));
            xml.append("<BLOQUE_DATOS>\n");
            xml.append(String.format("\t<grupo_datos seq=\"ANNO_ACADEMICO\">\n\t\t<dato nombre_dato=\"C_ANNO\">%d</dato>\n\t</grupo_datos>\n", MaimonidesApp.getApplication().getAnoEscolar().getAno()));
            xml.append(String.format("\t<grupo_datos seq=\"HORARIOS_REGULARES\" registros=\"%d\">\n", bloquesProfesores.size()));
            xml.append(Str.implode(bloquesProfesores, ""));
            xml.append("\t</grupo_datos>\n");
            xml.append("</BLOQUE_DATOS>\n");
            xml.append("</SERVICIO>\n");
            //Ahora guardamos el contenido en el fichero
            ficheroXml= File.createTempFile("horarios_seneca", ".xml");
            Archivo.setContenido(xml.toString(),"ISO-8859-1", ficheroXml, false);
        } catch (SQLException ex) {
            Logger.getLogger(ExportadorHorariosSeneca.class.getName()).log(Level.SEVERE, "Error recuperando listado de horarios.", ex);
        }
        Obj.cerrar(st, res);
        return ficheroXml;
    }

    private String getBloqueProfesor(int contadorProfesores, int idProfesor, ArrayList<String> bloquesActividad) {
        try {
            Profesor p = Profesor.getProfesor(idProfesor);
            if (Num.getInt(p.getCodigo()) > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("\t<grupo_datos seq=\"HORARIO_REGULAR_PROFESOR_%d\" registros=\"%d\">\n", contadorProfesores, bloquesActividad.size()));
                sb.append(String.format("\t\t<dato nombre_dato=\"X_EMPLEADO\">%d</dato>\n", p.getCodigo()));
                sb.append(String.format("\t\t<dato nombre_dato=\"F_TOMAPOS\">%1$tm/%1$td/%1$tY</dato>\n", p.getFechaTomaPosesion()));
                sb.append(Str.implode(bloquesActividad, ""));
                sb.append("\t</grupo_datos>\n");
                return sb.toString();
            } else {
                firePropertyChange("message", null, "Ignorando horarios de profesor " + p + " por no tener código Séneca.");
            }
        } catch (Exception ex) {
            Logger.getLogger(ExportadorHorariosSeneca.class.getName()).log(Level.SEVERE, "Error recuperando profesor " + idProfesor + ".", ex);
        }
        return null;
    }

    private String getXMLBloqueHorario(AnoEscolar ano, int contador, int dia, TramoHorario tramo, Dependencia dependencia, Curso curso, Unidad unidad, Actividad actividad, Materia materia) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("\t\t<grupo_datos seq=\"ACTIVIDAD_%d\">\n", contador));
        sb.append(String.format("\t\t\t<dato nombre_dato=\"N_DIASEMANA\">%d</dato>\n", dia));
        sb.append(String.format("\t\t\t<dato nombre_dato=\"X_TRAMO\">%d</dato>\n", tramo.getCodigo()));
        if (dependencia != null && Num.getInt(dependencia.getCodigo()) > 0) {
            sb.append(String.format("\t\t\t<dato nombre_dato=\"X_DEPENDENCIA\">%d</dato>\n", dependencia.getCodigo()));
        } else {
            sb.append("\t\t\t<dato nombre_dato=\"X_DEPENDENCIA\"/>\n");
        }
        sb.append(String.format("\t\t\t<dato nombre_dato=\"X_UNIDAD\">%d</dato>\n", unidad.getCodigo()));
        sb.append(String.format("\t\t\t<dato nombre_dato=\"X_OFERTAMATRIG\">%d</dato>\n", curso.getCodigo()));
        if (materia == null) {
            sb.append("\t\t\t<dato nombre_dato=\"X_MATERIAOMG\"/>\n");
        } else {
            sb.append(String.format("\t\t\t<dato nombre_dato=\"X_MATERIAOMG\">%d</dato>\n", materia.getCodigo()));
        }
        sb.append(String.format("\t\t\t<dato nombre_dato=\"F_INICIO\">01/09/%d</dato>\n", ano.getAno()));
        sb.append(String.format("\t\t\t<dato nombre_dato=\"F_FIN\">31/08/%d</dato>\n", (ano.getAno() + 1)));
        sb.append(String.format("\t\t\t<dato nombre_dato=\"N_HORINI\">615</dato>\n", tramo.getHini()));
        sb.append(String.format("\t\t\t<dato nombre_dato=\"N_HORFIN\">675</dato>\n", tramo.getHfin()));
        sb.append(String.format("\t\t\t<dato nombre_dato=\"X_ACTIVIDAD\">%d</dato>\n", actividad.getCodigo()));
        sb.append("\t\t</grupo_datos>\n");
        return sb.toString();
    }
}
