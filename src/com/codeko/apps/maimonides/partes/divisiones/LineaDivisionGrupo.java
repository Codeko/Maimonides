package com.codeko.apps.maimonides.partes.divisiones;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.IObjetoTabla;
import com.mysql.jdbc.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class LineaDivisionGrupo extends MaimonidesBean implements IObjetoTabla {

    ArrayList<Horario> horarios = new ArrayList<Horario>();
    Unidad unidad = null;

    public LineaDivisionGrupo(ArrayList<Horario> horarios, Unidad unidad) {
        if (horarios != null) {
            getHorarios().addAll(horarios);
        }
        if (unidad != null) {
            setUnidad(unidad);
        }
    }

    public Unidad getUnidad() {
        return unidad;
    }

    public final void setUnidad(Unidad unidad) {
        this.unidad = unidad;
    }

    public final ArrayList<Horario> getHorarios() {
        return horarios;
    }

    @Override
    public int getNumeroDeCampos() {
        return 1 + getHorarios().size();
    }

    @Override
    public Object getValueAt(int index) {
        Object val = null;
        if (index == 0) {
            val = this;
        } else {
            index = index - 1;
            if (index < getHorarios().size()) {
                val = getHorarios().get(index).isActivo();
            }
        }
        return val;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getUnidad().getCursoGrupo());
        if (getHorarios().get(0).isDicu()) {
            sb.append(" D.I.C.U.");
        }
        return sb.toString();
    }

    @Override
    public String getTitleAt(int index) {
        String val = "-";
        if (index == 0) {
            val = "Unidad";
        } else {
            index = index - 1;
            if (index < getHorarios().size()) {
                Profesor p = getHorarios().get(index).getObjetoProfesor();
                if (p != null) {
                    val = p.getDescripcionObjeto();
                }
            }
        }
        return val;
    }

    @Override
    public Class getClassAt(int index) {
        Class val = null;
        if (index == 0) {
            val = LineaDivisionGrupo.class;
        } else {
            val = Boolean.class;
        }
        return val;
    }

    @Override
    public boolean setValueAt(int index, Object val) {
        boolean ret = false;
        if (index > 0) {
            index = index - 1;
            if (index < getHorarios().size()) {
                getHorarios().get(index).setActivo((Boolean) val);
                ret = true;
                getHorarios().get(index).guardar();
            }
        }
        return ret;
    }

    @Override
    public boolean esCampoEditable(int index) {
        return index > 0;
    }

    void guardarEnSimilares() {
        String sql = "UPDATE horarios AS h "
                + " SET h.activo=? "
                + " WHERE h.materia_id=? AND h.profesor_id=? AND h.unidad_id=?";
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            for (Horario h : getHorarios()) {
                st.setBoolean(1, h.isActivo());
                st.setInt(2, h.getMateria());
                st.setInt(3, h.getProfesor());
                st.setInt(4, h.getUnidad());
                st.addBatch();
            }
            st.executeBatch();
            st.close();
        } catch (SQLException ex) {
            Logger.getLogger(LineaDivisionAlumno.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
