package com.codeko.apps.maimonides.partes;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.ParteFaltas;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class FusionadorPartes {

    public final static int FUSION_IMPOSIBLE = 0;
    public final static int FUSION_NO_ELIMINAR = 1;
    public final static int FUSION_ELIMINAR_SEGUNDO = 2;
    public final static int FUSION_ELIMINAR_PRIMERO = 3;
    boolean mezclarPrimariosConSecundarios = false;
    boolean mezclarDicusConNormal = false;
    boolean mezclarApoyoConNormal = false;
    int maximoAlumnosPorParte = 36;//TODO Este valor debe estar configurado en algun lugar de forma global

    public boolean isMezclarPrimariosConSecundarios() {
        return mezclarPrimariosConSecundarios;
    }

    public void setMezclarPrimariosConSecundarios(boolean mezclarPrimariosConSecundarios) {
        this.mezclarPrimariosConSecundarios = mezclarPrimariosConSecundarios;
    }

    public int getMaximoAlumnosPorParte() {
        return maximoAlumnosPorParte;
    }

    public void setMaximoAlumnosPorParte(int maximoAlumnosPorParte) {
        this.maximoAlumnosPorParte = maximoAlumnosPorParte;
    }

    public boolean isMezclarDicusConNormal() {
        return mezclarDicusConNormal;
    }

    public void setMezclarDicusConNormal(boolean mezclarDicusConNormal) {
        this.mezclarDicusConNormal = mezclarDicusConNormal;
    }

    public boolean isMezclarApoyoConNormal() {
        return mezclarApoyoConNormal;
    }

    public void setMezclarApoyoConNormal(boolean mezclarApoyoConNormal) {
        this.mezclarApoyoConNormal = mezclarApoyoConNormal;
    }

    public int fusionarParte(ParteFaltas parte1, ParteFaltas parte2) {
        int ret = FUSION_IMPOSIBLE;
        //Para que dos partes sean fusionables no deben ser primarios de apoyo (los de apoyo ya están fusionados).
        // && parte1.isDicu()==parte2.isDicu()
        if (parte1 != null && !parte1.isApoyo() && parte2 != null && !parte2.isApoyo()) {
            //Vemos que tengan los mismos alumnos los dos partes
            Logger.getLogger(FusionadorPartes.class.getName()).log(Level.INFO, "Intentando fusionar: {0} con {1}", new Object[]{parte1.getDescripcionObjeto(), parte2.getDescripcionObjeto()});
            ArrayList<Integer> horasParte1 = ParteFaltas.getHorasDistintas(parte1.getHorarios());
            ArrayList<Integer> horasParte2 = ParteFaltas.getHorasDistintas(parte2.getHorarios());
            if (parte1.getAlumnos().equals(parte2.getAlumnos())) {
                if (horasParte1.size() == 1 && horasParte2.size() == 1) {
                    //Hay dos tipos de fusion por exceso de alumnos en una misma hora o por igualdad de alumnos en dos horas distintas
                    //Vemos si la hora es la misma
                    if (horasParte1.get(0).equals(horasParte2.get(0))) {
                        //Fusionamos por division de alumnos
                        ret = fusionarPartesPorDivisionDeAlumnos(parte1, parte2);
                    } else {
                        //Si no tienen la misma hora entonces se pueden fundir eliminado el segundo
                        ret = fusionarPorCombinacion(parte1, parte2);
                    }

                } else {
                    //Si el segundo parte es primario no se pueden fusionar
                    if (!parte2.isPrimario()) {
                        //Verificamos si son fusionables por combinacion
                        //Para eso es necesario que no compartan ninguna hora
                        boolean fusionables = true;
                        for (Integer i : horasParte1) {
                            //Si se solapan en alguna hora ya no son fusionable
                            if (horasParte2.contains(i)) {
                                fusionables = false;
                                break;
                            }
                        }
                        if (fusionables) {
                            ret = fusionarPorCombinacion(parte1, parte2);
                        }
                    }
                }
            }
        }
        return ret;
    }

    public int comprimirPartes(ParteFaltas parte1, ParteFaltas parte2) {
        int ret = FUSION_IMPOSIBLE;
        ArrayList<Integer> horasParte1 = ParteFaltas.getHorasDistintas(parte1.getHorarios());
        ArrayList<Integer> horasParte2 = ParteFaltas.getHorasDistintas(parte2.getHorarios());
        //Este modo de compresión realmente no fusiona sino que comprime partes que no se molestan unos a otros
        boolean fusionar = true;
        if (!isMezclarDicusConNormal()) {
            fusionar = parte1.isDicu() == parte2.isDicu();
        }
        if (!isMezclarApoyoConNormal()) {
            fusionar = parte1.isApoyo() == parte2.isApoyo();
        }
        if (fusionar && !isMezclarPrimariosConSecundarios()) {
            fusionar = !parte1.isPrimario() && !parte2.isPrimario();
        }
        if (fusionar) {
            //Para que no salgan absurdos uno de los partes debe contener las unidades del otro
            fusionar = parte1.getUnidades().containsAll(parte2.getUnidades()) || parte2.getUnidades().containsAll(parte1.getUnidades());
        }
        //Si no hay horas en comun no se van a molestar
        if (fusionar) {
            for (Integer i : horasParte1) {
                if (horasParte2.contains(i)) {
                    fusionar = false;
                    break;
                }
            }
        }
        //Ahora tenemos que ver que entre los dos no tengan mas de 36 alumnos
        if (fusionar) {
            ArrayList<Integer> al = new ArrayList<Integer>();
            for (Alumno a : parte1.getAlumnos()) {
                if (!al.contains(a.getId())) {
                    al.add(a.getId());
                }
            }
            for (Alumno a : parte2.getAlumnos()) {
                if (!al.contains(a.getId())) {
                    al.add(a.getId());
                }
            }

            fusionar = al.size() <= getMaximoAlumnosPorParte();
        }
        if (fusionar) {
            ret = fusionarPorCombinacion(parte1, parte2);
        }
        return ret;
    }

    private int fusionarPorCombinacion(ParteFaltas parte1, ParteFaltas parte2) {
        int ret = FUSION_IMPOSIBLE;
        //En la fusión por combinación siempre fusionamos el segundo parte al primero
        //Para añadir un parte al otro tenemos que añadirle los horarios y los alumnos del segundo al primero
        //Unidades no es necesario ya que deben ser las mismas en los dos partes
        PreparedStatement stQuitarDivididos = null;
        PreparedStatement stHorarios = null;
        try {
            Logger.getLogger(FusionadorPartes.class.getName()).log(Level.SEVERE, "Fusionando partes por combinacion: {0} <- {1}", new Object[]{parte1, parte2});
            //Si el primer parte es primario hay que quitar los horarios divididos que ya no lo son
            if (parte1.isPrimario()) {
                stQuitarDivididos = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM partes_horarios WHERE parte_id=? AND horario_id=? AND dividido=1 ");
                stQuitarDivididos.setInt(1, parte1.getId());
                ArrayList<Horario> quitados = new ArrayList<Horario>();
                for (Horario hDiv : parte1.getHorariosDivididos()) {
                    for (Horario hNuevo : parte2.getHorarios()) {
                        if (hDiv.equivalente(hNuevo)) {
                            //Borramos el div
                            stQuitarDivididos.setInt(2, hDiv.getId());
                            stQuitarDivididos.addBatch();
                            quitados.add(hDiv);
                        }
                    }
                }
                stQuitarDivididos.executeBatch();
                parte1.getHorariosDivididos().removeAll(quitados);
            }

            stHorarios = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE partes_horarios AS pa SET parte_id=? WHERE parte_id=?");
            stHorarios.setInt(1, parte1.getId());
            stHorarios.setInt(2, parte2.getId());
            stHorarios.executeUpdate();
            Obj.cerrar(stHorarios);

            PreparedStatement stAlumnos = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("UPDATE partes_alumnos AS pa SET parte_id=? WHERE parte_id=?");
            stAlumnos.setInt(1, parte1.getId());
            stAlumnos.setInt(2, parte2.getId());
            stAlumnos.executeUpdate();
            Obj.cerrar(stHorarios);

            for (Unidad u : parte2.getUnidades()) {
                if (!parte1.getUnidades().contains(u)) {
                    parte1.getUnidades().add(u);
                }
            }
            parte1.getHorarios().addAll(parte2.getHorarios());
            parte1.asignarPosicionAlumnos();
            ret = FUSION_ELIMINAR_SEGUNDO;
        } catch (SQLException ex) {
            Logger.getLogger(FusionadorPartes.class.getName()).log(Level.SEVERE, "Error combinando partes: " + parte1 + " <- " + parte2, ex);
        }
        Obj.cerrar(stQuitarDivididos, stHorarios);
        return ret;
    }

    private void dividirAlumnos(ParteFaltas parte1, ParteFaltas parte2, Collection<Alumno> alumnos) throws SQLException {
        PreparedStatement st = null;
        try {
            //Recorremos la mitad de los alumnos eliminandolo del parte original
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM partes_alumnos WHERE parte_id=? AND alumno_id=?");
            int i = 0;
            for (Alumno a : alumnos) {
                int idParte = parte1.getId();
                int idAlumno = a.getId();
                if (i >= alumnos.size() / 2) {
                    idParte = parte2.getId();
                }
                st.clearParameters();
                st.setInt(1, idParte);
                st.setInt(2, idAlumno);
                int actu = st.executeUpdate();
                if (actu != 1) {
                    Logger.getLogger(ParteFaltas.class.getName()).log(Level.WARNING, "A la consulta ha actualizado {0}" + " en vez de 1:\n" + " DELETE FROM partes_alumnos WHERE parte_id=" + "{1} AND alumno_id={2}", new Object[]{actu, idParte, idAlumno});
                }
                i++;
            }
        } finally {
            Obj.cerrar(st);
        }
    }

    private int fusionarPartesPorDivisionDeAlumnos(ParteFaltas parte1, ParteFaltas parte2) {
        Logger.getLogger(ParteFaltas.class.getName()).log(Level.INFO, "Fusionando partes por division de alumnos: {0}: {1} y {2}: {3}", new Object[]{parte1.getId(), parte1.getDescripcionObjeto(), parte2.getId(), parte2.getDescripcionObjeto()});
        int ret = FUSION_IMPOSIBLE;
        //Entonces tenemos que dividir a los alumnos entre uno y otro parte
        //TODO En el caso de los desdobles de prácticas hay que buscar una forma de saber a que mitad le toca que mitad
        try {
            MaimonidesApp.getApplication().getConector().getConexion().setAutoCommit(false);
            //generamos dos grupos de alumnos, uno bilingüe y el otro no
            //TODO Esto deberia ser configurable
            ArrayList<Alumno> bi = new ArrayList<Alumno>();
            //de los normales nos interesa que cada unidad se divida en los dos partes
            HashMap<Integer, ArrayList<Alumno>> datos = new HashMap<Integer, ArrayList<Alumno>>();
            for (Alumno a : parte1.getAlumnos()) {
                if (a.isBilingue()) {
                    bi.add(a);
                } else {
                    int idUnidad = a.getUnidad().getId();
                    if (!datos.containsKey(idUnidad)) {
                        datos.put(idUnidad, new ArrayList<Alumno>());
                    }
                    datos.get(idUnidad).add(a);
                }
            }
            dividirAlumnos(parte1, parte2, bi);

            //Pasamos el hasmap a un vector
            Iterator<Integer> it = datos.keySet().iterator();
            ArrayList<ArrayList<Alumno>> listaAlumnos = new ArrayList<ArrayList<Alumno>>();
            while (it.hasNext()) {
                ArrayList<Alumno> grupoAlumnos = datos.get(it.next());
                listaAlumnos.add(grupoAlumnos);
            }
            it = null;
            datos = null;
            ArrayList<Alumno> alumnosFinal = new ArrayList<Alumno>();
            boolean hayDatos = true;
            int contador = 0;
            while (hayDatos) {
                hayDatos = false;
                for (ArrayList<Alumno> ga : listaAlumnos) {
                    if (contador < ga.size()) {
                        hayDatos = true;
                        alumnosFinal.add(ga.get(contador));
                    }
                }
                contador++;
            }
            //Y Asignamos los alumnos
            dividirAlumnos(parte1, parte2, alumnosFinal);
            bi = null;
            ret = FUSION_NO_ELIMINAR;
            parte2.resetearListaAlumnos();
            parte1.resetearListaAlumnos();
            parte1.asignarPosicionAlumnos();
            parte2.asignarPosicionAlumnos();
            MaimonidesApp.getApplication().getConector().getConexion().commit();
        } catch (SQLException ex) {
            Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error fusionando partes.", ex);
            try {
                MaimonidesApp.getApplication().getConector().getConexion().rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(ParteFaltas.class.getName()).log(Level.SEVERE, "Error haciendo Rollback", ex1);
            }
        } finally {
            try {
                MaimonidesApp.getApplication().getConector().getConexion().setAutoCommit(true);
            } catch (SQLException ex) {
                Logger.getLogger(FusionadorPartes.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return ret;
    }
}
