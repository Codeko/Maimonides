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
package com.codeko.apps.maimonides.partes;

import com.codeko.apps.maimonides.conf.Configuracion;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.convivencia.Expulsion;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.LineaParteAlumno;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSourceProvider;

/**
 * Copyright Codeko Informática 2008
 * @author Codeko
 */
public class ParteDataSourceProvider extends JRAbstractBeanDataSourceProvider implements JRDataSource {

    MaimonidesBean bean = null;
    AnoEscolar anoEscolar = null;
    GregorianCalendar fecha = null;
    ArrayList<ParteFaltas> partes = new ArrayList<ParteFaltas>();
    ArrayList<AsistenciaAlumno> asistencia = new ArrayList<AsistenciaAlumno>();
    int posicion = 0;
    int posicionAlumno = -1;
    boolean aplicarFiltrosImpresion = false;
    private ArrayList<Integer> filtroIdProfs = new ArrayList<Integer>();
    private ArrayList<Integer> filtroIdUds = new ArrayList<Integer>();
    private ArrayList<Integer> FiltroIdAulas = new ArrayList<Integer>();

    public MaimonidesBean getBean() {
        if (bean == null) {
            bean = new MaimonidesBean();
        }
        return bean;
    }

    public final void setBean(MaimonidesBean bean) {
        this.bean = bean;
    }

    public boolean isAplicarFiltrosImpresion() {
        return aplicarFiltrosImpresion;
    }

    public void setAplicarFiltrosImpresion(boolean aplicarFiltrosImpresion) {
        this.aplicarFiltrosImpresion = aplicarFiltrosImpresion;
    }

    public ArrayList<AsistenciaAlumno> getAsistencia() {
        return asistencia;
    }

    public int getPosicionAlumno() {
        return posicionAlumno;
    }

    public void setPosicionAlumno(int posicionAlumno) {
        this.posicionAlumno = posicionAlumno;
    }

    public int getPosicion() {
        return posicion;
    }

    public void setPosicion(int posicion) {
        this.posicion = posicion;
    }

    public ArrayList<ParteFaltas> getPartes() {
        return partes;
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public final void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public ParteDataSourceProvider(AnoEscolar anoEscolar, GregorianCalendar fecha, MaimonidesBean bean) {
        super(ParteFaltas.class);
        setAnoEscolar(anoEscolar);
        setFecha(fecha);
        setBean(bean);
    }

    public void cargarPartes(String curso) {
        getPartes().clear();
        PreparedStatement st = null;
        ResultSet res = null;
        getBean().firePropertyChange("message", null, "Cargando datos partes de " + curso + "...");
        try {
            //st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM partes WHERE ano=? AND fecha=? AND curso=?");
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT distinct p.* FROM partes AS p "
                    + " LEFT JOIN cursos AS c ON c.curso=p.curso "
                    + " LEFT JOIN unidades AS u ON u.id=p.unidad_id "
                    + " WHERE p.ano=? AND p.fecha=? AND p.curso=? "
                    + " ORDER BY IFNULL(c.posicion,99999),IFNULL(u.posicion,99999),p.curso,p.primario DESC,p.descripcion,p.id");
            st.setInt(1, getAnoEscolar().getId());
            st.setString(2, Fechas.getFechaFormatoBD(getFecha()));
            st.setString(3, curso);
            res = st.executeQuery();
            boolean finalAplicarFiltrosImpresion=cargarFiltrosImpresion();
            while (res.next()) {
                ParteFaltas parte = new ParteFaltas();
                if (parte.cargarDesdeResultSet(res)) {
                    boolean add = true;
                    if (finalAplicarFiltrosImpresion) {
                        add = checkFiltroEnParte(parte);
                    }
                    if (add) {
                        getPartes().add(parte);
                    } else {
                        getBean().firePropertyChange("message", null, "Parte " + parte.getDescripcionObjeto() + "marcado para no ser impreso.");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ParteDataSourceProvider.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st, res);
        }
    }

    public void cargarPartes(ParteFaltas parte) {
        getPartes().clear();
        getPartes().add(parte);
    }

    private boolean cargarFiltrosImpresion() {
        boolean finalAplicarFiltrosImpresion = false;
        if (isAplicarFiltrosImpresion()) {
            String idsProfesores = MaimonidesApp.getApplication().getConfiguracion().get("filtro-imp-partes-prof-ano-" + getAnoEscolar().getId(), "");
            if (!idsProfesores.equals("")) {
                String[] ids = idsProfesores.split(",");
                for (String id : ids) {
                    int v = Num.getInt(id);
                    if (v > 0) {
                        filtroIdProfs.add(v);
                        finalAplicarFiltrosImpresion = true;
                    }
                }
            }
            String idsUnidades = MaimonidesApp.getApplication().getConfiguracion().get("filtro-imp-partes-unidad-ano-" + getAnoEscolar().getId(), "");
            if (!idsUnidades.equals("")) {
                String[] ids = idsUnidades.split(",");
                for (String id : ids) {
                    int v = Num.getInt(id);
                    if (v > 0) {
                        filtroIdUds.add(v);
                        finalAplicarFiltrosImpresion = true;
                    }
                }
            }

            String idsAulas = MaimonidesApp.getApplication().getConfiguracion().get("filtro-imp-partes-unidad-ano-" + getAnoEscolar().getId(), "");
            if (!idsAulas.equals("")) {
                String[] ids = idsAulas.split(",");
                for (String id : ids) {
                    int v = Num.getInt(id);
                    if (v > 0) {
                        FiltroIdAulas.add(v);
                        finalAplicarFiltrosImpresion = true;
                    }
                }
            }
        }
        return finalAplicarFiltrosImpresion;
    }

    private boolean checkFiltroEnParte(ParteFaltas parte) {
        boolean add = true;
        ArrayList<Horario> horarios = parte.getHorarios();
        if (!filtroIdProfs.isEmpty()) {
            boolean todos = true;
            for (Horario h : horarios) {
                if (!filtroIdProfs.contains(h.getProfesor())) {
                    todos = false;
                    break;
                }
            }
            add = !todos;
        }

        if (add) {
            if (!filtroIdUds.isEmpty()) {
                boolean todos = true;
                for (Horario h : horarios) {
                    if (!filtroIdUds.contains(h.getUnidad())) {
                        todos = false;
                        break;
                    }
                }
                add = !todos;
            }
        }

        if (add) {
            if (!FiltroIdAulas.isEmpty()) {
                boolean todos = true;
                for (Horario h : horarios) {
                    if (!FiltroIdAulas.contains(h.getDependencia())) {
                        todos = false;
                        break;
                    }
                }
                add = !todos;
            }
        }
        return add;
    }

    public void cargarPartes() {
        getPartes().clear();
        PreparedStatement st = null;
        ResultSet res = null;
        getBean().firePropertyChange("message", null, "Cargando datos de " + Fechas.format(getFecha()) + "...");
        try {
            Profesor p = null;
            Unidad u = Permisos.getFiltroUnidad();
            String extra = "";
            String where = "";
            boolean addJoinsHorarios = false;
            boolean addJoinsUnidades = false;
            if (Permisos.isUsuarioSoloProfesor()) {
                p = Permisos.getFiltroProfesor();
                if (p != null) {
                    //Si hay filtro de profesor debemos mostrar solo los partes donde el
                    //profesor está
                    where = " h.profesor_id=? ";
                    addJoinsHorarios = true;
                    //Si hay filtro de unidad se podrá también ver el parte de
                    //Esa unidad
                    if (u != null) {
                        addJoinsUnidades = true;
                        where += " OR u2.id=? ";
                    }
                    where = " AND (" + where + ") ";
                }
            }

            if (addJoinsHorarios) {
                extra += " JOIN partes_horarios AS ph ON ph.parte_id=p.id ";
                extra += " JOIN horarios_ AS h ON ph.horario_id=h.id ";
            }

            if (addJoinsUnidades) {
                extra += " JOIN unidades AS u2 ON h.unidad_id=u2.id ";
            }
            String sql = "SELECT distinct p.* FROM partes AS p "
                    + " LEFT JOIN cursos AS c ON c.curso=p.curso "
                    + " LEFT JOIN unidades AS u ON u.id=p.unidad_id "
                    + extra
                    + " WHERE p.ano=? AND p.fecha=? " + where
                    + " ORDER BY IFNULL(c.posicion,99999),IFNULL(u.posicion,99999),p.curso,p.primario DESC,p.descripcion,p.id";
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, getAnoEscolar().getId());
            st.setString(2, Fechas.getFechaFormatoBD(getFecha()));
            if (p != null) {
                st.setInt(3, p.getId());
                if (u != null) {
                    st.setInt(4, u.getId());
                }
            }
            res = st.executeQuery();

            boolean finalAplicarFiltrosImpresion = cargarFiltrosImpresion();
            while (res.next()) {
                ParteFaltas parte = new ParteFaltas();
                if (parte.cargarDesdeResultSet(res)) {
                    getBean().firePropertyChange("message", null, "Cargando parte " + parte.getDescripcionObjeto() + "...");
                    boolean add = true;
                    if (finalAplicarFiltrosImpresion) {
                        add = checkFiltroEnParte(parte);
                    }
                    if (add) {
                        getPartes().add(parte);
                    } else {
                        getBean().firePropertyChange("message", null, "Parte " + parte.getDescripcionObjeto() + "marcado para no ser impreso.");
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(ParteDataSourceProvider.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st, res);
        }
        getBean().firePropertyChange("message", null, "Datos de " + Fechas.format(getFecha()) + " cargados.");
    }

    @Override
    public JRDataSource create(JasperReport arg0) throws JRException {

        return this;
    }

    @Override
    public void dispose(JRDataSource arg0) throws JRException {
        getPartes().clear();
    }

    @Override
    public boolean supportsGetFieldsOperation() {
        return false;
    }

    private void cargarAsistencias() {
        //cargamos las lineas de asistencia de los alumnos del parte
        getAsistencia().clear();
        for (Alumno a : getParteActual().getAlumnos()) {
            AsistenciaAlumno asis = new AsistenciaAlumno(getParteActual(), a);
            getAsistencia().add(asis);
        }
    }

    @Override
    public boolean next() throws JRException {
        //TODO Implementar que no haya ningún parte
        if (getPosicionAlumno() == -1) {
            cargarAsistencias();
        }
        //Avanzamos la posicion del alumno
        setPosicionAlumno(getPosicionAlumno() + 1);
        //vemos si hay más datos de alumno para esa posicion
        boolean ok = true;
        if (getParteActual().getAlumnos().size() <= getPosicionAlumno()) {
            //Entonces nos movemos al siguiente parte
            setPosicion(getPosicion() + 1);
            setPosicionAlumno(0);
            ok = getPosicion() < getPartes().size();
            if (ok) {
                getBean().firePropertyChange("progress", (getPosicion() * 100) / getPartes().size(), ((getPosicion() + 1) * 100) / getPartes().size());
                getBean().firePropertyChange("message", null, "Rellenando parte " + getParteActual().getDescripcionObjeto() + "...");
                //cargamos las lineas de asistencia de los alumnos del parte
                cargarAsistencias();
            }
        }
        //FIXME Esto no debería darse en ningún caso pero se da
        while (ok && getParteActual().getAlumnos().isEmpty()) {
            Logger.getLogger(ParteDataSourceProvider.class.getName()).log(Level.SEVERE, "El parte no tiene alumnos: {0}", getParteActual());
            ok = next();
        }
        return ok;
    }

    public ParteFaltas getParteActual() {
        return getPartes().get(getPosicion());
    }

    public Alumno getAlumnoActual() {
        return getParteActual().getAlumnos().get(getPosicionAlumno());
    }

    @Override
    public Object getFieldValue(JRField field) throws JRException {
        Object ret = null;
        if (field.getName().equals("unidades")) {
            ret = getParteActual().getNombreParte().replace(" Curso", "");
        } else if (field.getName().equals("fecha")) {
            ret = getParteActual().getFecha().getTime();
        } else if (field.getName().startsWith("horaX")) {
            ret = getParteActual().getCabeceras().get(Num.getInt(Num.limpiar(field.getName())) - 1);
            String nom = ret.toString();
            if (nom.indexOf("-") != -1) {
                ret = nom.substring(nom.indexOf("-") + 1).trim();
            } else {
                ret = "";
            }
        } else if (field.getName().startsWith("hora")) {
            ret = getParteActual().getCabeceras().get(Num.getInt(Num.limpiar(field.getName())) - 1);
            String nom = ret.toString();
            if (nom.indexOf("-") != -1) {
                ret = nom.substring(0, nom.indexOf("-")).trim();
            }
        } else if (field.getName().equals("idParte")) {
            ret = getParteActual().getId();
        } else if (field.getName().equals("nombreAlumno")) {
            Alumno a = getAlumnoActual();
            StringBuilder sb = new StringBuilder();
            sb.append(a.getApellido1());
            sb.append(" ");
            sb.append(a.getApellido2());
            sb.append(", ");
            sb.append(a.getNombre());
            ret = sb.toString();
        } else if (field.getName().equals("posicionAlumno")) {
            ret = getPosicionAlumno() + 1;
        } else if (field.getName().equals("infoExtraCabecera")) {
            if (getParteActual().isApoyo()) {
                ret = "APOYO";
            } else {
                //Si es un parte principal mostramos el tutor si no la asignatura
                if (getParteActual().isPrimario()) {
                    if (getParteActual().getUnidades().get(0).getIdProfesor() > 0) {
                        try {
                            ret = "Tutor: " + Profesor.getNombreProfesor(getParteActual().getUnidades().get(0).getIdProfesor());
                        } catch (Exception e) {
                            ret = "Tutor: ";
                        }
                    } else {
                        ret = "Tutor: ";
                    }
                } else {
                    ArrayList<Materia> mats = getParteActual().getMaterias();
                    if (mats.size() == 1) {
                        ret = "Asignatura: " + getParteActual().getHorarios().get(0).getObjetoMateria().getDescripcion();
                    } else {
                        StringBuilder sb = new StringBuilder("Asignaturas: ");
                        boolean primeraMat = true;
                        for (Materia m : mats) {
                            if (!primeraMat) {
                                sb.append(" - ");
                            } else {
                                primeraMat = false;
                            }
                            sb.append(m.getCodigoMateria());
                        }
                        ret = sb.toString();
                    }
                }
            }
        } else if (field.getName().equals("alumnoExpulsado")) {
            ret = Expulsion.isAlumnoExpulsado(getAlumnoActual(), getParteActual().getFecha());
        } else if (field.getName().equals("apoyo")) {
            ret = getParteActual().isApoyo();
        } else if (field.getName().equals("curso")) {
            ret = getAlumnoActual().getUnidad().getCursoGrupo();
        } else if (field.getName().equals("pie")) {
            ret = getParteActual().getTextoPie();
        } else if (field.getName().startsWith("tieneClase")) {
            ret = true;
            int hora = Num.getInt(Num.limpiar(field.getName()));
            //Buscamos la asistencia de este alumno
            for (AsistenciaAlumno asis : getAsistencia()) {
                if (asis.getAlumno().getId().equals(getAlumnoActual().getId())) {
                    LineaParteAlumno l = asis.getLineaHora(hora);
                    ret = l != null;
                }
            }
        } else if (field.getName().startsWith("tieneApoyo")) {
            ret = false;
            //Si es de apoyo no queremos la marca de apoyo
            if (!getParteActual().isApoyo()) {
                int hora = Num.getInt(Num.limpiar(field.getName()));
                //Buscamos la asistencia de este alumno
                for (AsistenciaAlumno asis : getAsistencia()) {
                    if (asis.getAlumno().getId().equals(getAlumnoActual().getId())) {
                        LineaParteAlumno l = asis.getLineaHora(hora);
                        ret = l != null && l.isApoyo();
                    }
                }
            }
        } else {
            HashMap<String, Object> datosBase = Configuracion.getDatosBaseImpresion();
            if (datosBase.containsKey(field.getName())) {
                ret = datosBase.get(field.getName());
            }
        }
        return ret;
    }
}
