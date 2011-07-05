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


package com.codeko.apps.maimonides.convivencia;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.alumnos.IAlumno;
import com.codeko.apps.maimonides.cartero.Carta;
import com.codeko.apps.maimonides.convivencia.config.ConfiguracionMedidasExpulsion;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.apps.maimonides.elementos.ObjetoBD;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.swing.CdkAutoTablaCol;
import com.codeko.util.Fechas;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.codeko.util.Str;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class ParteConvivencia extends ObjetoBD implements IAlumno {
    // ESTADOS

    @CdkAutoTablaCol(ignorar = true)
    public static final int ESTADO_PENDIENTE = 0;
    @CdkAutoTablaCol(ignorar = true)
    public static final int ESTADO_IGNORADO = 1;
    @CdkAutoTablaCol(ignorar = true)
    public static final int ESTADO_SANCIONADO = 2;
    //MASCARAS SITUACIONES
    @CdkAutoTablaCol(ignorar = true)
    public static final int SIT_REVISADO_TUTOR = 2;
    @CdkAutoTablaCol(ignorar = true)
    public static final int SIT_REVISADO_JE = 4;
    @CdkAutoTablaCol(ignorar = true)
    public static final int SIT_ENVIADO_SENECA = 8;
    //MASCARSA NOTIFICADO
    @CdkAutoTablaCol(ignorar = true)
    public static final int MASCARA_INFORMADO_ALUMNO = 2;
    @CdkAutoTablaCol(ignorar = true)
    public static final int MASCARA_INFORMADO_TUTOR = 4;
    @CdkAutoTablaCol(ignorar = true)
    public static final int MASCARA_INFORMADO_PADRES = 8;
    @CdkAutoTablaCol(ignorar = true)
    AnoEscolar anoEscolar = MaimonidesApp.getApplication().getAnoEscolar();
    GregorianCalendar fecha = new GregorianCalendar();
    Alumno alumno = null;
    Profesor profesor = null;
    int estado = 0;
    @CdkAutoTablaCol(titulo = "Situación")
    int situacion = 0;
    String descripcion = "";
    @CdkAutoTablaCol(ignorar = true)
    String observaciones = "";
    TramoHorario tramoHorario = null;
    @CdkAutoTablaCol(titulo = "Gravedad")
    int tipo = 0;
    @CdkAutoTablaCol(titulo = "Notificados")
    int informados = 0;
    @CdkAutoTablaCol(ignorar = true)
    Integer expulsionID = null;
    @CdkAutoTablaCol(ignorar = true)
    ArrayList<Conducta> conductas = null;
    @CdkAutoTablaCol(ignorar = true)
    ArrayList<Conducta> medidas = null;

    public ParteConvivencia() {
    }

    public ParteConvivencia(ResultSet res) throws SQLException, Exception {
        cargarDesdeResultSet(res);
    }

    private ParteConvivencia(int id) throws Exception {
        PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT * FROM conv_partes WHERE id=?");
        st.setInt(1, id);
        ResultSet res = st.executeQuery();
        if (res.next()) {
            cargarDesdeResultSet(res);
        }
        Obj.cerrar(st, res);
    }

    public static ParteConvivencia getParte(int id) throws Exception {
        ParteConvivencia h = new ParteConvivencia(id);
        return h;
    }

    public ArrayList<Conducta> getConductas() {
        if (conductas == null) {
            conductas = getConductas(TipoConducta.TIPO_CONDUCTA);
        }
        return conductas;
    }

    public ArrayList<Conducta> getMedidas() {
        if (medidas == null) {
            medidas = getConductas(TipoConducta.TIPO_MEDIDA);
        }
        return medidas;
    }

    public void setConductas(ArrayList<Conducta> conductas) {
        this.conductas = conductas;
    }

    public void setMedidas(ArrayList<Conducta> medidas) {
        this.medidas = medidas;
    }

    public static String getTextoEstado(int estado) {
        String texto = "";
        switch (estado) {
            case ESTADO_PENDIENTE:
                texto = "Pendiente";
                break;
            case ESTADO_IGNORADO:
                texto = "Ignorado";
                break;
            case ESTADO_SANCIONADO:
                texto = "Sancionado";
                break;
        }
        return texto;
    }

    public static String getTextoSituacion(int situacion) {
        String ret = "";
        ArrayList<String> sits = new ArrayList<String>();
        if ((situacion & SIT_REVISADO_TUTOR) == SIT_REVISADO_TUTOR) {
            sits.add("Revisado por el tutor");
        }
        if ((situacion & SIT_REVISADO_JE) == SIT_REVISADO_JE) {
            sits.add("Revisado por el J.E.");
        }
        if ((situacion & SIT_ENVIADO_SENECA) == SIT_ENVIADO_SENECA) {
            sits.add("Enviado a Séneca");
        }
        ret = Str.implode(sits, ", ");
        return ret;
    }

    public static String getTextoNotificados(int notificados) {
        String ret = "";
        ArrayList<String> nots = new ArrayList<String>();
        if ((notificados & MASCARA_INFORMADO_ALUMNO) == MASCARA_INFORMADO_ALUMNO) {
            nots.add("Alumno");
        }
        if ((notificados & MASCARA_INFORMADO_PADRES) == MASCARA_INFORMADO_PADRES) {
            nots.add("Padres/Tutores");
        }
        if ((notificados & MASCARA_INFORMADO_TUTOR) == MASCARA_INFORMADO_TUTOR) {
            nots.add("Tutor");
        }
        ret = Str.implode(nots, ", ");
        return ret;
    }

    private ArrayList<Conducta> getConductas(int tipo) {
        ArrayList<Conducta> conduc = new ArrayList<Conducta>();
        //Ahora lo cargamos
        if (getId() != null) {
            PreparedStatement ps = null;
            ResultSet res = null;
            try {
                ps = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT cc.* FROM conv_conductas AS cc JOIN conv_tipos AS ct ON cc.tipo_id=ct.id JOIN conv_lineas AS cl ON cl.conducta_id=cc.id WHERE cl.parte_id=? AND ct.tipo=? ");
                ps.setInt(1, getId());
                ps.setInt(2, tipo);
                res = ps.executeQuery();
                while (res.next()) {
                    Conducta c = new Conducta();
                    try {
                        c.cargarDesdeResultSet(res);
                        conduc.add(c);
                    } catch (Exception ex) {
                        Logger.getLogger(ParteConvivencia.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } catch (SQLException ex) {
                Logger.getLogger(ParteConvivencia.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(ps, res);
        }
        return conduc;
    }

    public final void cargarDesdeResultSet(ResultSet res) throws SQLException, Exception {
        setId(res.getInt("id"));
        setAnoEscolar(AnoEscolar.getAnoEscolar(res.getInt("ano")));
        setFecha(Fechas.toGregorianCalendar(res.getDate("fecha")));
        setAlumno(Alumno.getAlumno(res.getInt("alumno_id")));
        setProfesor(Profesor.getProfesor(res.getInt("profesor_id")));
        setEstado(res.getInt("estado"));
        setSituacion(res.getInt("situacion"));
        setDescripcion(res.getString("descripcion"));
        setObservaciones(res.getString("observaciones"));
        setTramoHorario(TramoHorario.geTramoHorario(res.getInt("tramo_horario_id")));
        setTipo(res.getInt("tipo"));
        setInformados(res.getInt("informados"));
        int i = Num.getInt(res.getObject("expulsion_id"));
        setExpulsionID(i == 0 ? null : i);
    }

    @Override
    public Alumno getAlumno() {
        return alumno;
    }

    public void setAlumno(Alumno alumno) {
        this.alumno = alumno;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public void setAnoEscolar(AnoEscolar anoEscolar) {
        this.anoEscolar = anoEscolar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public int getSituacion() {
        return situacion;
    }

    public void setSituacion(int situacion) {
        this.situacion = situacion;
    }

    public Integer getExpulsionID() {
        return expulsionID;
    }

    public void setExpulsionID(Integer expulsionID) {
        this.expulsionID = expulsionID;
        //Ahora verificamos si tiene medidas asignadas
        if (getMedidas().isEmpty()) {
            //Si no las tiene le asignamos la medida
            asignarMedidasPorExpulsion(false);
        }
    }

    public GregorianCalendar getFecha() {
        return fecha;
    }

    public void setFecha(GregorianCalendar fecha) {
        this.fecha = fecha;
    }

    public int getInformados() {
        return informados;
    }

    public void setInformados(int informados) {
        this.informados = informados;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public TramoHorario getTramoHorario() {
        return tramoHorario;
    }

    public void setTramoHorario(TramoHorario tramoHorario) {
        this.tramoHorario = tramoHorario;
    }

    @Override
    public boolean guardar() {
        boolean ret = false;
        try {
            String sql = "UPDATE conv_partes SET ano=?,fecha=?,alumno_id=?,profesor_id=?,estado=?,descripcion=?,observaciones=?,tramo_horario_id=?,tipo=?,informados=?,expulsion_id=?,situacion=? WHERE id=?";
            if (getId() == null) {
                sql = "INSERT INTO conv_partes(ano,fecha,alumno_id,profesor_id,estado,descripcion,observaciones,tramo_horario_id,tipo,informados,expulsion_id,situacion,id) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
            }
            PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            st.setInt(1, getAnoEscolar().getId());
            st.setDate(2, new java.sql.Date(getFecha().getTimeInMillis()));
            st.setInt(3, getAlumno().getId());
            st.setInt(4, getProfesor().getId());
            st.setInt(5, getEstado());
            st.setString(6, getDescripcion());
            st.setString(7, getObservaciones());
            st.setInt(8, getTramoHorario().getId());
            st.setInt(9, getTipo());
            st.setInt(10, getInformados());
            st.setObject(11, getExpulsionID());
            st.setInt(12, getSituacion());
            st.setObject(13, getId());
            ret = st.executeUpdate() > 0;
            if (ret && getId() == null) {
                setId((int) st.getLastInsertID());
            }
            st.close();
            //Ahora tenemos que guardar las conductas y medidas
            ret = guardarConductasMedidas();
        } catch (SQLException ex) {
            Logger.getLogger(Conducta.class.getName()).log(Level.SEVERE, "Error guardando datos de parte de convivencia: " + this, ex);
        }
        return ret;
    }

    @Override
    public String getNombreObjeto() {
        return "Parte de convivencia";
    }

    @Override
    public String getDescripcionObjeto() {
        return getDescripcion();
    }

    private boolean guardarConductasMedidas() {
        boolean ret = false;
        ArrayList<Conducta> cm = new ArrayList<Conducta>();
        cm.addAll(getConductas());
        cm.addAll(getMedidas());
        PreparedStatement st = null;
        PreparedStatement st2 = null;
        try {
            st2 = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM conv_lineas WHERE parte_id=?");
            st2.setInt(1, getId());
            st2.executeUpdate();
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("REPLACE conv_lineas SET parte_id=?, conducta_id=?");
            for (Conducta c : cm) {
                st.setInt(1, getId());
                st.setInt(2, c.getId());
                st.addBatch();
            }
            st.executeBatch();
            st.clearBatch();
            ret = true;
        } catch (SQLException ex) {
            Logger.getLogger(ParteConvivencia.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, st2);
        return ret;
    }

    @Override
    public String getTabla() {
        return "conv_partes";
    }

    public static ArrayList<ParteConvivencia> getPartes(Alumno alumno, GregorianCalendar fIni, GregorianCalendar fFin, Profesor profesor, Curso curso, Unidad unidad, Integer tipo, Integer estado, Integer situacion, Integer noSituacion, Integer notificado, Integer noNotificado, ArrayList<Conducta> conductas) {
        ArrayList<ParteConvivencia> partes = new ArrayList<ParteConvivencia>();
        PreparedStatement st = null;
        ResultSet res = null;
        try {
            String extra = "";
            if (alumno != null) {
                extra += " AND cp.alumno_id=? ";
            }
            if (fIni != null) {
                extra += " AND cp.fecha>=? ";
            }
            if (fFin != null) {
                extra += " AND cp.fecha<=? ";
            }
            if (profesor != null) {
                extra += " AND cp.profesor_id=? ";
            }
            if (curso != null) {
                extra += " AND a.curso_id=? ";
            }
            if (unidad != null) {
                extra += " AND a.unidad_id=? ";
            }
            if (tipo != null) {
                extra += " AND cp.tipo=? ";
            }
            if (estado != null) {
                extra += " AND cp.estado=? ";
            }
            if (situacion != null) {
                extra += " AND ((cp.situacion & ?) = ?) ";
            }
            if (noSituacion != null) {
                extra += " AND ((cp.situacion & ?) != ?) ";
            }
            if (notificado != null) {
                extra += " AND ((cp.informados & ?) = ?) ";
            }
            if (noNotificado != null) {
                extra += " AND ((cp.informados & ?) != ?) ";
            }
            String extraJoin = "";
            if (conductas != null && conductas.size() > 0) {
                extraJoin = " JOIN conv_lineas AS cl ON cl.parte_id = cp.id ";
                ArrayList<Integer> ids = new ArrayList<Integer>();
                for (Conducta c : conductas) {
                    ids.add(c.getId());
                }
                String sIds = Str.implode(ids, ",", "0");
                extra += " AND cl.conducta_id IN (" + sIds + ") ";
            }
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("SELECT cp.* FROM conv_partes AS cp JOIN alumnos AS a ON a.id=cp.alumno_id " + extraJoin + " WHERE cp.ano=? " + extra);
            int cont = 1;
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            cont++;
            if (alumno != null) {
                st.setInt(cont, alumno.getId());
                cont++;
            }
            if (fIni != null) {
                st.setDate(cont, new java.sql.Date(fIni.getTime().getTime()));
                cont++;
            }
            if (fFin != null) {
                st.setDate(cont, new java.sql.Date(fFin.getTime().getTime()));
                cont++;
            }
            if (profesor != null) {
                st.setInt(cont, profesor.getId());
                cont++;
            }
            if (curso != null) {
                st.setInt(cont, curso.getId());
                cont++;
            }
            if (unidad != null) {
                st.setInt(cont, unidad.getId());
                cont++;
            }
            if (tipo != null) {
                st.setInt(cont, tipo);
                cont++;
            }
            if (estado != null) {
                st.setInt(cont, estado);
                cont++;
            }
            if (situacion != null) {
                st.setInt(cont, situacion);
                cont++;
                st.setInt(cont, situacion);
                cont++;
            }
            if (noSituacion != null) {
                st.setInt(cont, noSituacion);
                cont++;
                st.setInt(cont, noSituacion);
                cont++;
            }

            if (notificado != null) {
                st.setInt(cont, notificado);
                cont++;
                st.setInt(cont, notificado);
                cont++;
            }
            if (noNotificado != null) {
                st.setInt(cont, noNotificado);
                cont++;
                st.setInt(cont, noNotificado);
                cont++;
            }
            res = st.executeQuery();
            while (res.next()) {
                ParteConvivencia p = new ParteConvivencia();
                p.cargarDesdeResultSet(res);
                partes.add(p);
            }
        } catch (Exception ex) {
            Logger.getLogger(ParteConvivencia.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st, res);

        return partes;
    }

    boolean isEnviadoSeneca() {
        return (getSituacion() & SIT_ENVIADO_SENECA) == SIT_ENVIADO_SENECA;
    }

    boolean isNotificadoTutores() {
        return (getInformados() & MASCARA_INFORMADO_PADRES) == MASCARA_INFORMADO_PADRES;
    }

    public void setEnviadoSeneca(boolean b) {
        int sit = getSituacion();
        if (b) {
            sit = sit | SIT_ENVIADO_SENECA;
        } else {
            sit = sit ^ SIT_ENVIADO_SENECA;
        }
        setSituacion(sit);
    }

    @CdkAutoTablaCol(titulo = "Enviable a Séneca")
    public Boolean isEnviableSeneca() {
        return validarParte().isEmpty();
    }

    public ArrayList<String> validarParte() {
        ArrayList<String> errores = new ArrayList<String>();
        //vemos que haya profesor, con código y fecha de toma de posesion
        if (getProfesor() == null) {
            errores.add("El parte no tiene asignado un profesor.");
        } else {
            if (Num.getInt(getProfesor().getCodigo()) == 0) {
                errores.add("El profesor asignado no tiene código Séneca.");
            }
            if (getProfesor().getFechaTomaPosesion() == null) {
                errores.add("El profesor asignado no tiene fecha de toma de posiseión.");
            }
        }

        //que haya alumno con codigo de faltas
        if (getAlumno() == null) {
            errores.add("El parte no tiene asignado alumno.");
        } else if (Str.noNulo(getAlumno().getCodFaltas()).trim().equals("")) {
            errores.add("El alumno no tiene asignado código identificador de Séneca.");
        }
        //que haya tramo horario con código
        if (getTramoHorario() == null) {
            errores.add("El parte no tiene asociado un tramo horario.");
        } else if (Str.noNulo(getTramoHorario().getCodigo()).trim().equals("")) {
            errores.add("El tramo horario no tiene código de Séneca.");
        }
        //Que haya al menos una conducta con código
        boolean hayConductaConCod = false;
        for (Conducta c : getConductas()) {
            if (!c.getCodigo().trim().equals("")) {
                hayConductaConCod = true;
                break;
            }
        }
        if (!hayConductaConCod) {
            if (getConductas().isEmpty()) {
                errores.add("El parte no tiene asignada ninguna conducta.");
            } else {
                errores.add("Las conductas asociadas al parte no tiene código Séneca.");
            }
        }
        //Ahora vemos el estado si es sancionado debe tener las medidas aplicadas
        if (getEstado() == ESTADO_SANCIONADO) {
            if (getMedidas().isEmpty()) {
                //Si no tiene medidas vemos si tiene asociada una expulsión
                if (Num.getInt(getExpulsionID()) > 0) {
                    if (!asignarMedidasPorExpulsion()) {
                        errores.add("El parte está marcado como sancionado pero no tiene ninguna medida asociada.");
                    }
                } else {
                    errores.add("El parte está marcado como sancionado pero no tiene ninguna medida asociada.");
                }
            }
        }
        return errores;
    }

    public static boolean isEnviarSenecaPartesIgnorados() {
        return Num.getInt(MaimonidesApp.getApplication().getConfiguracion().get("seneca_convivencia_enviar_ignorados", "0")) > 0;
    }

    public boolean asignarMedidasPorExpulsion() {
        return asignarMedidasPorExpulsion(true);
    }

    public boolean asignarMedidasPorExpulsion(boolean guardar) {
        boolean ret = true;
        //Si no tiene medidas vemos si tiene asociada una expulsión
        if (Num.getInt(getExpulsionID()) > 0) {
            try {
                //Entonces le asignamos la medida de expulsión
                //Para ello vemos el número de días de la expulsion
                Expulsion e = Expulsion.getExpulsion(getExpulsionID());
                int dias = e.getDias();
                //Asignar las medidas correspondientes
                Conducta c = ConfiguracionMedidasExpulsion.getMedidaParaExpulsion(dias);
                if (c != null) {
                    if (!getMedidas().contains(c)) {
                        getMedidas().add(c);
                    }
                } else {
                    ret = false;
                }
            } catch (Exception ex) {
                Logger.getLogger(ParteConvivencia.class.getName()).log(Level.SEVERE, null, ex);
                ret = false;
            }
        } else {
            ret = false;
        }
        if (ret && guardar) {
            guardar();
        }
        return ret;
    }

    public void addDatosExtraImpresion(Map<String, Object> data, Carta carta) {
        data.put("descripcion", this.getDescripcion());
        data.put("estado", this.getEstado());
        data.put("textoEstado", ParteConvivencia.getTextoEstado(this.getEstado()));
        data.put("fecha", Fechas.format(this.getFecha()));
        data.put("observaciones", this.getObservaciones());
        data.put("profesor", this.getProfesor().getDescripcionObjeto());
        data.put("situacion", this.getSituacion());
        data.put("textoSituacion", ParteConvivencia.getTextoSituacion(this.getSituacion()));
        data.put("tipo", this.getTipo());
        data.put("textoTipo", TipoConducta.getNombreGravedad(this.getTipo()));
        ArrayList<HashMap<String, String>> conductasParte = new ArrayList<HashMap<String, String>>();
        for (Conducta c : this.getConductas()) {
            HashMap<String, String> conducta = new HashMap<String, String>();
            conducta.put("descripcion", c.getDescripcion());
            conducta.put("tipo", Str.noNulo(c.getTipo().getTipo()));
            conducta.put("textoTipo", TipoConducta.getNombreGravedad(c.getTipo().getTipo()));
            conductasParte.add(conducta);
        }
        data.put("conductas", conductasParte);
    }
}
