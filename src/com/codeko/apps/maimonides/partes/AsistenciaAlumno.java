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

import com.codeko.apps.maimonides.elementos.LineaParteAlumno;
import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.MaimonidesUtil;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.excepciones.NoExisteElementoException;
import com.codeko.swing.IObjetoTabla;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 * Almacena los datos de asistencia de un alumno para un día o un parte
 */
public class AsistenciaAlumno extends MaimonidesBean implements IObjetoTabla {

    ParteFaltas parte = null;
    GregorianCalendar fecha = null;
    ArrayList<LineaParteAlumno> lineas = new ArrayList<LineaParteAlumno>();
    Alumno alumno = null;
    Integer posicion = null;
    HashMap<Integer, String> localizaciones = new HashMap<Integer, String>();
    public static final int POS_INI_HORAS = 3;

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }

    public Alumno getAlumno() {
        return alumno;
    }

    public final void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public ArrayList<LineaParteAlumno> getLineas() {
        return lineas;
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public final void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public ParteFaltas getParte() {
        return parte;
    }

    public final void setParte(ParteFaltas parte) {
        this.parte = parte;
    }

    public AsistenciaAlumno() {
    }

    public AsistenciaAlumno(ParteFaltas parte, Alumno alumno) {
        setParte(parte);
        setAlumno(alumno);
        cargarLineasPorParte();
    }

    public AsistenciaAlumno(GregorianCalendar fecha, AnoEscolar anoEscolar, Alumno alumno) {
        setFecha(fecha);
        setAlumno(alumno);
        cargarLineasPorFecha();
    }

    public ArrayList<Integer> getHorasSaltosAsistencia() {
        ArrayList<Integer> saltos = new ArrayList<Integer>();
        ArrayList<Integer> tmp = new ArrayList<Integer>();
        boolean hayFalta = false;
        boolean hayAsistencia = false;
        for (int i = 0; i < 6; i++) {
            LineaParteAlumno l = getLineaHora(i + 1);
            if (l != null) {
                //Si hay falta marcamos como que hay faltas
                if (l.getAsistencia() == ParteFaltas.FALTA_INJUSTIFICADA || l.getAsistencia() == ParteFaltas.FALTA_JUSTIFICADA) {
                    hayFalta = true;
                    //Si hay asistencia entonces es que hay un salto
                    //Entonces volcamos el temporar en el final
                    if (hayAsistencia) {
                        saltos.addAll(tmp);
                        tmp.clear();
                        hayAsistencia = false;
                    }
                } else if (l.getAsistencia() == ParteFaltas.FALTA_ASISTENCIA) {
                    //Si hay asistencia despues de falta tenemos candidados a saltos
                    if (hayFalta) {
                        hayAsistencia = true;
                        //Y lo metemos en el tmp
                        tmp.add(i + 1);
                    }
                }
            }
        }
        return saltos;
    }

    protected void cargarLinea(ResultSet res) throws SQLException, NoExisteElementoException {
        LineaParteAlumno l = new LineaParteAlumno();
        l.setAlumno(getAlumno());
        l.setHorario(Horario.getHorario(res.getInt("horario_id")));
        if (getParte() == null) {
            l.setParte(new ParteFaltas(res.getInt("parte_id")));
        } else {
            l.setParte(getParte());
        }
        l.setAsistencia(res.getInt("asistencia"));
        l.setDividido(res.getBoolean("dividido"));
        l.setFirmado(res.getBoolean("firmado"));
        setPosicion(res.getInt("posicion"));
        getLineas().add(l);
    }

    private void cargarLineasPorParte() {
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT pa.*,ph.dividido,ph.firmado FROM partes_alumnos AS pa JOIN partes_horarios AS ph ON ph.parte_id=pa.parte_id AND ph.horario_id=pa.horario_id WHERE  pa.alumno_id=? AND pa.parte_id=?  ");
            st.setInt(1, getAlumno().getId());
            st.setInt(2, getParte().getId());
            ResultSet res = st.executeQuery();
            cargarLineas(res);
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(AsistenciaAlumno.class.getName()).log(Level.SEVERE, "Error cargando lineas por Alumno: " + getAlumno() + " Parte: " + getParte(), ex);
        }
    }

    private void cargarLineasPorFecha() {
        try {
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT pa.*,ph.dividido,ph.firmado FROM partes_alumnos AS pa JOIN partes AS p ON p.id=pa.parte_id JOIN partes_horarios AS ph ON ph.parte_id=pa.parte_id AND ph.horario_id=pa.horario_id WHERE pa.alumno_id=? AND p.fecha=?  ");
            st.setInt(1, getAlumno().getId());
            st.setDate(2, new java.sql.Date(getFecha().getTime().getTime()));
            ResultSet res = st.executeQuery();
            cargarLineas(res);
            Obj.cerrar(st, res);
        } catch (Exception ex) {
            Logger.getLogger(AsistenciaAlumno.class.getName()).log(Level.SEVERE, "Error cargando lineas por Alumno: " + getAlumno() + " y Fecha: " + Fechas.format(getFecha()), ex);
        }
    }

    protected void cargarLineas(ResultSet res) throws SQLException, NoExisteElementoException {
        getLineas().clear();
        int pos = -1;
        while (res.next()) {
            cargarLinea(res);
            if (pos != -1 && pos != getPosicion()) {
                Logger.getLogger(AsistenciaAlumno.class.getName()).log(Level.SEVERE, "Hay dos posiciones diferentes para la misma asistencia!: {0}", this);
            }
        }
    }

    @Override
    public int getNumeroDeCampos() {
        return 10;
    }

    @Override
    public Object getValueAt(int index) {
        Object valor = null;
        switch (index) {
            case 0:
                valor = getPosicion().floatValue();
                break;
            case 1:
                if (getAlumno() != null) {
                    valor = getAlumno().getId();
                }
                break;
            case 2:
                if (getAlumno() != null) {
                    valor = getAlumno();
                }
                break;
            case 3:
                if (getAlumno() != null) {
                    valor = getAlumno().getUnidad().getCursoGrupo();
                }
                break;
//            case 4:
//            case 5:
//            case 6:
//            case 7:
//            case 8:
//            case 9:
            default://Para el resto de casos estamos asignando una falta
                int pos = index - POS_INI_HORAS;
                LineaParteAlumno lin = getLineaHora(pos);
                //valor=lin!=null?lin.getHorario()+"":"NULO";
                //TODO Aquí deberiamos devolver la asistencia y que decida la tabla como actuar
                if (lin != null) {
                    valor = lin.getAsistencia();
                } else {
                    //TODO En las localizaciones sería interesante poder hacer clic y acceder al parte en concreto.
                    if (!localizaciones.containsKey(pos)) {
                        if (getParte() != null && getAlumno() != null) {
                            localizaciones.put(pos, getLocalizacionAlumno(pos, getAlumno().getId(), getParte().getId(), getParte().getFecha()));
                        }
                    }
                    valor = localizaciones.get(pos);
                }
                break;

        }
        return valor;
    }

    public String getLocalizacionAlumno(int hora, int alumno, int parteActual, GregorianCalendar fecha) {
        String loc = null;
        String sql = "SELECT p.descripcion FROM partes_alumnos AS pa " +
                " JOIN horarios AS h ON h.id=pa.horario_id JOIN partes AS p ON p.id=pa.parte_id " +
                " JOIN alumnos_horarios AS ah ON ah.horario_id=h.id AND ah.alumno_id=pa.alumno_id " +
                " WHERE h.activo AND ah.activo AND h.hora=? AND pa.alumno_id=? AND p.fecha=? AND p.id!=? ";
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, hora);
            st.setInt(2, alumno);
            st.setDate(3, new java.sql.Date(fecha.getTime().getTime()));
            st.setInt(4, parteActual);
            res = st.executeQuery();
            if (res != null && res.next()) {
                loc = res.getString(1);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AsistenciaAlumno.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            Obj.cerrar(st, res);
        }
        return loc;
    }

    public LineaParteAlumno getLineaHora(int hora) {
        LineaParteAlumno lin = null;
        for (LineaParteAlumno l : getLineas()) {
            if (l.getHorario().getHora().intValue() == hora && !l.isDividido()) {
                lin = l;
                break;
            }
        }
        return lin;
    }

    @Override
    public String getTitleAt(int index) {
        String titulo = "";
        switch (index) {
            case 0:
                titulo = "Nº";
                break;
            case 1:
                titulo = "Código";
                break;
            case 2:
                titulo = "Alumno";
                break;
            case 3:
                titulo = "Unidad";
                break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                if (getParte() != null) {
                    titulo = getParte().getCabeceras().get(index - (POS_INI_HORAS + 1));
                } else {
                    titulo = (index - POS_INI_HORAS) + "ª";
                }
                break;

        }
        return titulo;
    }

    @Override
    public Class getClassAt(int index) {
        Class clase = null;
        switch (index) {
            case 0:
                clase = Float.class;
                break;
            case 1:
                clase = Integer.class;
                break;
            case 2:
                clase = Alumno.class;
                break;
            case 3:
                clase = Unidad.class;
                break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                clase = Integer.class;
                break;

        }
        return clase;
    }

    @Override
    public boolean setValueAt(int index, Object valor) {
        boolean ret = false;
        switch (index) {
            case 0:
                break;
            case 1:
                //Codigo
                break;
            case 2:
                //Nombre
                break;
            case 3:
                //Unidad
                break;
//            case 4:
//            case 5:
//            case 6:
//            case 7:
//            case 8:
//            case 9:
            default://Para el resto de valores son horas
                valor = Str.noNulo(valor);
                int pos = index - POS_INI_HORAS;
                LineaParteAlumno lin = getLineaHora(pos);
                if (lin == null) {
                    lin = crearLineaParteAlumno(pos);
                }
                if (lin != null) {
                    //TODO Hay demasiados sitios con esta lista. hacer funciones para usar comunmente
                    if (valor.equals(" ")) {
                        ret = true;
                        lin.setAsistencia(ParteFaltas.FALTA_INDETERMINADA);
                    } else if (valor.equals("A")) {
                        ret = true;
                        lin.setAsistencia(ParteFaltas.FALTA_ASISTENCIA);
                    } else if (valor.equals("E")) {
                        ret = true;
                        lin.setAsistencia(ParteFaltas.FALTA_EXPULSION);
                    } else if (valor.equals("I")) {
                        ret = true;
                        lin.setAsistencia(ParteFaltas.FALTA_INJUSTIFICADA);
                    } else if (valor.equals("J")) {
                        ret = true;
                        lin.setAsistencia(ParteFaltas.FALTA_JUSTIFICADA);
                    } else if (valor.equals("R")) {
                        ret = true;
                        lin.setAsistencia(ParteFaltas.FALTA_RETRASO);
                    }
                }
                if (ret) {
                    ret = lin.guardarAsistencia();
                }
                break;

        }
        return ret;
    }

    public LineaParteAlumno crearLineaParteAlumno(int hora) {
        //TODO Esto debería mandar mensajes de estado y resultado
        LineaParteAlumno lin = null;
        //Tenemos que sacar el horario del alumno para esta hora

        int dia = MaimonidesUtil.getDiaFecha(getFecha());
        //Ahora tenemos que ver el horario de ese bloque del alumno
        String sql = "SELECT h.* FROM horarios_ AS h " +
                " JOIN alumnos_horarios AS ah ON ah.horario_id=h.id  " +
                " JOIN alumnos AS a ON ah.alumno_id=a.id " +
                " WHERE a.id=? AND a.borrado=0 AND h.dia=? AND h.hora=? AND ah.activo=1";
        PreparedStatement st = null;
        ResultSet res = null;
        PreparedStatement st2 = null;
        ResultSet res2 = null;
        try {
            st = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement(sql);
            st.setInt(1, getAlumno().getId());
            st.setInt(2, dia);
            st.setInt(3, hora);
            res = st.executeQuery();
            //TODO Puede que haya varios horarios habría que ver que hacer (ofrecer opcion)
            if (res.next()) {
                Horario h = new Horario();
                h.cargarDesdeResultSet(res);
                //Ya tenemos el horario ahora vemos si existe un parte que cumpla esas condiciones
                sql = "SELECT p.id FROM partes_horarios AS ph " +
                        " JOIN partes AS p ON ph.parte_id=p.id " +
                        " WHERE ph.horario_id=? AND p.fecha=?";
                st2 = (PreparedStatement) MaimonidesApp.getConexion().prepareStatement(sql);
                st2.setInt(1, h.getId());
                st2.setDate(2, new java.sql.Date(getFecha().getTimeInMillis()));
                res2 = st2.executeQuery();
                ParteFaltas pf = null;
                if (res2.next()) {
                    int parteId = res2.getInt(1);
                    try {
                        pf = new ParteFaltas(parteId);
                        //Ya tenemos el parte de faltas con lo que podemos crear la linea
                        pf.insertarLineaAlumno(getAlumno(), h);
                    } catch (NoExisteElementoException ex) {
                        Logger.getLogger(AsistenciaAlumno.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    //Si no hay parte creamos unos
                    pf = new ParteFaltas();
                    pf.setAnoEscolar(MaimonidesApp.getApplication().getAnoEscolar());
                    pf.setCurso(getAlumno().getObjetoCurso().getCurso());
                    pf.setFecha(getFecha());
                    pf.setPrimario(false);
                    pf.getHorarios().add(h);
                    pf.getUnidades().add(getAlumno().getUnidad());
                    if (!pf.guardar()) {
                        pf = null;
                    }
                    //Al crear el parte se crea la linea también por lo que no hace falta
                }
                if (pf != null) {
                    //Ahora añadimos la linea al vector de lineas
                    LineaParteAlumno l = new LineaParteAlumno(pf, h, getAlumno());
                    getLineas().add(l);
                    lin = l;
                }
            }
            //Si no hay horario no se va a poder crear la linea
        } catch (SQLException ex) {
            Logger.getLogger(AsistenciaAlumno.class.getName()).log(Level.SEVERE, null, ex);
        }
        return lin;
    }

    @Override
    public boolean esCampoEditable(int index) {
        boolean editable = false;
        switch (index) {
            case 0:
                break;
            case 1:
                //Codigo
                break;
            case 2:
                //Nombre
                break;
            case 3:
                //Unidad
                break;
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                int pos = index - POS_INI_HORAS;
                LineaParteAlumno lin = getLineaHora(pos);
                editable = lin != null;
                break;

        }
        return editable;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        sb.append("FECHA:");
        sb.append(Fechas.format(getFecha()));
        sb.append(", PARTE:");
        sb.append(getParte());
        sb.append(", ALUMNO:");
        sb.append(getAlumno());
        sb.append("]");
        return sb.toString();
    }
}
