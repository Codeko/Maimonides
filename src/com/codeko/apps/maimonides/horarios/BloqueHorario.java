package com.codeko.apps.maimonides.horarios;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Actividad;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Dependencia;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Materia;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.TramoHorario;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 * Un bloque horario son un conjunto de horarios/horarios de alumno con las siguientes características:
 * Una o varias asignaturas equivalentes
 * Una o varias unidades
 * Un profesor
 * Un aula
 * Una hora y un día
 */
public class BloqueHorario extends MaimonidesBean implements Cloneable, Comparable<BloqueHorario> {

    AnoEscolar ano = null;
    int hora = 0;
    int dia = 0;
    int diaAnterior = 0;
    int horaAnterior = 0;
    ArrayList<Materia> materias = new ArrayList<Materia>();
    Actividad actividad = null;
    Dependencia dependencia = null;
    Profesor profesor = null;
    ArrayList<Unidad> unidades = new ArrayList<Unidad>();
    boolean activo = true;
    boolean materiasCambiadas = false;
    ArrayList<MateriaVirtual> materiasVirtuales = new ArrayList<MateriaVirtual>();
    Integer dicu = 0;
    private Boolean hayConflictos = null;
    ArrayList<ConflictoHorario> conflictos = new ArrayList<ConflictoHorario>();

    public BloqueHorario(AnoEscolar ano, int dia, int hora, Actividad actividad, Profesor profesor, Dependencia dependencia) {
        setAno(ano);
        setDia(dia);
        setHora(hora);
        setActividad(actividad);
        setProfesor(profesor);
        setDependencia(dependencia);
    }

    public BloqueHorario(AnoEscolar ano, int dia, int hora, int actividadId, int profesorId, int dependenciaId) {
        setAno(ano);
        setDia(dia);
        setHora(hora);
        try {
            setActividad(Actividad.getActividad(actividadId));
        } catch (Exception ex) {
            Logger.getLogger(BloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            setProfesor(Profesor.getProfesor(profesorId));
        } catch (Exception ex) {
            Logger.getLogger(BloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (dependenciaId > 0) {
                setDependencia(Dependencia.getDependencia(dependenciaId));
            }
        } catch (Exception ex) {
            Logger.getLogger(BloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getDiaAnterior() {
        return diaAnterior;
    }

    public void setDiaAnterior(int diaAnterior) {
        this.diaAnterior = diaAnterior;
    }

    public int getHoraAnterior() {
        return horaAnterior;
    }

    public void setHoraAnterior(int horaAnterior) {
        this.horaAnterior = horaAnterior;
    }

    public Dependencia getDependencia() {
        return dependencia;
    }

    public final void setDependencia(Dependencia dependencia) {
        this.dependencia = dependencia;
    }

    public boolean isMateriasCambiadas() {
        return materiasCambiadas;
    }

    public void setMateriasCambiadas(boolean materiasCambiadas) {
        this.materiasCambiadas = materiasCambiadas;
    }

    public boolean isDicu() {
        return dicu == 1;
    }

    public Integer getDicu() {
        return dicu;
    }

    public void setDicu(Integer dicu) {
        this.dicu = dicu;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public ArrayList<Materia> getMaterias() {
        return materias;
    }

    public ArrayList<Unidad> getUnidades() {
        return unidades;
    }

    public void addUnidad(Unidad u) {
        if (!getUnidades().contains(u)) {
            getUnidades().add(u);
        }
        Collections.sort(getUnidades());
    }

    public void addMateria(Materia m) {
        if (!getMaterias().contains(m)) {
            getMaterias().add(m);
            setMateriasCambiadas(true);
        }
    }

    public ArrayList<MateriaVirtual> getMateriasVirtuales() {
        if (isMateriasCambiadas()) {
            materiasVirtuales.clear();
            for (Materia m : getMaterias()) {
                MateriaVirtual mv = new MateriaVirtual(m.getCodigoMateria(), m.getDescripcion());
                if (!materiasVirtuales.contains(mv)) {
                    materiasVirtuales.add(mv);
                }
            }
        }
        return materiasVirtuales;
    }

    public boolean addMateriaVirtual(MateriaVirtual materia) {
        boolean ret = true;
        //Una materia virtual se convierte en materia para cada curso de cada unidad
        for (Unidad u : getUnidades()) {
            Materia m = materia.getMateria(getAno(), u.getIdCurso());
            if (m != null) {
                addMateria(m);
            } else {
                ret = false;
            }
            if (u.getIdCurso2() != null) {
                m = materia.getMateria(getAno(), u.getIdCurso2());
                if (m != null) {
                    addMateria(m);
                }
            }
        }
        return ret;
    }

    public boolean addMateriasVirtuales(Collection<MateriaVirtual> materias) {
        boolean ret = true;
        for (MateriaVirtual m : materias) {
            if (!addMateriaVirtual(m)) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    public ArrayList<Horario> getHorarios() {
        ArrayList<Horario> horarios = new ArrayList<Horario>();
        if (isDatosCompletos()) {
            String sql = "SELECT * FROM horarios_ AS h WHERE h.ano=? AND h.hora=? AND h.dia=? AND h.profesor_id=? AND actividad_id=? AND h.dicu=? ";
            if (getActividad().getNecesitaUnidad()) {
                ArrayList<Integer> uds = new ArrayList<Integer>();
                for (Unidad u : getUnidades()) {
                    uds.add(u.getId());
                }
                sql += " AND h.unidad_id IN (" + Str.implode(uds, ",") + ") ";
            }
            if (getActividad().getNecesitaMateria()) {
                ArrayList<Integer> mats = new ArrayList<Integer>();
                for (Materia m : getMaterias()) {
                    mats.add(m.getId());
                }
                sql += " AND h.materia_id IN (" + Str.implode(mats, ",") + ") ";
            }
            boolean hayDependencia = false;
            if (getDependencia() != null && Num.getInt(getDependencia().getId()) > 0) {
                sql += " AND aula_id=? ";
                hayDependencia = true;
            }
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, getAno().getId());
                st.setInt(2, getHora());
                st.setInt(3, getDia());
                st.setInt(4, getProfesor().getId());
                st.setInt(5, getActividad().getId());
                st.setInt(6, getDicu());
                if (hayDependencia) {
                    st.setInt(7, getDependencia().getId());
                }
                res = st.executeQuery();
                while (res.next()) {
                    Horario h = new Horario();
                    h.cargarDesdeResultSet(res);
                    horarios.add(h);
                }
            } catch (SQLException ex) {
                Logger.getLogger(BloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(st, res);
        }
        return horarios;
    }

    public Actividad getActividad() {
        return actividad;
    }

    public final void setActividad(Actividad actividad) {
        this.actividad = actividad;
    }

    public AnoEscolar getAno() {
        return ano;
    }

    public final void setAno(AnoEscolar ano) {
        this.ano = ano;
    }

    public int getDia() {
        return dia;
    }

    public final void setDia(int dia) {
        setDiaAnterior(this.dia);
        this.dia = dia;
    }

    public int getHora() {
        return hora;
    }

    public final void setHora(int hora) {
        setHoraAnterior(this.hora);
        this.hora = hora;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public final void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public boolean isDatosCompletos() {
        boolean completos = true;
        //&& getDependencia() != null
        //TODO Ver si es necesario que la dependencia no sea nula
        if (getActividad() != null && getProfesor() != null) {
            if (getActividad().getNecesitaUnidad() && getUnidades().isEmpty()) {
                completos = false;
            }
            if (completos && getActividad().getNecesitaMateria() && getMaterias().isEmpty()) {
                completos = false;
            }
        } else {
            completos = false;
        }
        return completos;
    }

    public boolean eliminar() {
        boolean ret = eliminar_();
        if (ret) {
            firePropertyChange("bloqueHorarioEliminado", null, this);
        }
        return ret;
    }

    private boolean eliminar_() {
        boolean ret = true;
        //Si no están los datos completos es que es un horario nuevo y el original no está completo
        if (isDatosCompletos()) {
            //Ahora tenemos que recuperar los horarios asociados a este bloque horario
            //Por cada horario lo eliminamos
            for (Horario h : getHorarios()) {
                ret = h.eliminar() & ret;
            }
        }
        return ret;
    }

    public boolean mover(int dia, int hora) {
        boolean ret = false;
        if (eliminar_()) {
            setDia(dia);
            setHora(hora);
            if (guardar_()) {
                firePropertyChange("bloqueHorarioMovido", null, this);
            }
        }
        return ret;
    }

    public boolean guardar() {
        boolean ret = guardar_();
        if (ret) {
            firePropertyChange("bloqueHorarioGuardado", null, this);
        }
        return ret;
    }

    private boolean guardar_() {
        boolean ret = false;
        if (isDatosCompletos()) {
            ret = true;
            //Se crea un horario por cada unidad/asignatura
            ArrayList<Horario> horarios = new ArrayList<Horario>();
            if (getActividad().getNecesitaUnidad()) {
                for (Unidad u : getUnidades()) {
                    if (getActividad().getNecesitaMateria()) {
                        for (Materia m : getMaterias()) {
                            Horario h = getHorario(u, m);
                            if (h != null) {
                                horarios.add(h);
                            }
                            ret = h != null && ret;
                        }
                    } else {
                        Horario h = getHorario(u, null);
                        if (h != null) {
                            horarios.add(h);
                        }
                        ret = h != null && ret;
                    }
                }
            } else {
                //TODO Ahora mismo no contemplamos las actividades que no necesiten unidad
            }
            if (ret) {
                //Una vez tenemos los horarios vamos guardando y asignando a los alumnos
                String sql = "REPLACE alumnos_horarios SELECT h.id AS horario_id,a.id AS alumno_id,true AS activo FROM horarios_ AS h "
                        + " JOIN alumnos AS a ON a.unidad_id=h.unidad_id WHERE h.id=? ";
                PreparedStatement st = null;
                try {
                    st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                    for (Horario h : horarios) {
                        if (h.guardar()) {
                            st.setInt(1, h.getId());
                            int actu = st.executeUpdate();
                            System.out.println("Actualizado " + actu);
                        } else {
                            //TODO LOG el ema
                            ret = false;
                        }
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(BloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
                    ret = false;
                }
                Obj.cerrar(st);
            } else {
                //TODO Log
            }
        }

        return ret;
    }

    private Horario getHorario(Unidad u, Materia m) {
        Horario h = new Horario();
        h.setAnoEscolar(getAno());
        h.setDia(getDia());
        h.setHora(getHora());
        if (getDependencia() != null) {
            h.setDependencia(getDependencia().getId());
        }
        if (m != null) {
            h.setMateria(m.getId());
        }
        h.setProfesor(getProfesor().getId());
        h.setActividad(getActividad().getId());
        if (u != null) {
            h.setUnidad(u.getId());
        }
        h.setActivo(isActivo());
        h.setDicu(getDicu());
        TramoHorario t = TramoHorario.getTramoParaHoraEstandar(getAno(), getHora());
        if (t != null) {
            h.setTramo(t.getId());
        } else {
            h = null;
        }
        return h;
    }

    public void resetearConflictos() {
        hayConflictos = null;
    }

    public boolean hayConflictos() {
        if (hayConflictos == null) {
            getConflictos().clear();
            ArrayList<Horario> hs = getHorarios();
            for (Horario h : hs) {
                getConflictos().addAll(h.getConflictos());
            }
            //En el caso de los conflictos por falta de alumnos el conflicto debe estar en todas las asignaturas no en parte.
            //TODO Implementar el tema de los alumnos no matriculados
            //TODO Implementar conflicto de asignatura DICU para alumnos no DICU
            ArrayList<ConflictoHorario> borrar = new ArrayList<ConflictoHorario>();
            for (ConflictoHorario ch : getConflictos()) {
                if (ch.getTipo() == ConflictoHorario.NO_HAY_ALUMNOS) {
                    borrar.add(ch);
                }
            }
            getConflictos().removeAll(borrar);
            hayConflictos = getConflictos().size() > 0;
        }
        return hayConflictos;
    }

    public ArrayList<ConflictoHorario> getConflictos() {
        return conflictos;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(BloqueHorario.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("");
        if (getMateriasVirtuales().isEmpty() && getActividad()!=null) {
            sb.append(getActividad()).append("<br/>");
        } else {
            for (MateriaVirtual mv : getMateriasVirtuales()) {
                sb.append(mv).append("<br/>");
            }
        }
        return sb.toString();
    }

    @Override
    public int compareTo(BloqueHorario o) {
        return toString().compareTo(o.toString());
    }
}
