package com.codeko.apps.maimonides.alumnos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.Alumno;
import com.codeko.apps.maimonides.elementos.Unidad;
import com.codeko.apps.maimonides.usr.Permisos;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.beans.Beans;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class BuscadorAlumnos extends MaimonidesBean {

    PreparedStatement st = null;
    ArrayList<ResultadoBusquedaAlumno> resultadoBusqueda = new ArrayList<ResultadoBusquedaAlumno>();
    boolean filtrarPorUsuario = true;
    Thread threadBusqueda = null;

    public boolean isFiltrarPorUsuario() {
        return filtrarPorUsuario;
    }

    public void setFiltrarPorUsuario(boolean filtrarPorUsuario) {
        this.filtrarPorUsuario = filtrarPorUsuario;
    }

    private String procesarCadena(String exp) {
        return procesarCadena(exp, true);
    }

    private String procesarCadena(String exp, boolean or) {
        String proc = " " + (or ? "OR" : "") + " REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(" + exp + "),'á','a'),'é','e'),'í','i'),'ó','o'),'ú','u') LIKE REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(?),'á','a'),'é','e'),'í','i'),'ó','o'),'ú','u') ";
        return proc;
    }

    public ArrayList<ResultadoBusquedaAlumno> getResultadoBusqueda() {
        return resultadoBusqueda;
    }

    public void addResultadoBusqueda(ResultadoBusquedaAlumno rba) {
        getResultadoBusqueda().add(rba);
    }

    private PreparedStatement getSt() throws SQLException {
        if (st == null) {
            Unidad u = null;
            if (isFiltrarPorUsuario()) {
                u = Permisos.getFiltroUnidad();
            }
            String sql = "SELECT a.id,CONCAT(a.apellido1,' ',a.apellido2,', ',a.nombre),u.cursogrupo FROM alumnos AS a JOIN unidades AS u ON u.id=a.unidad_id WHERE a.borrado=0 AND a.ano=? "
                    + " AND ( a.cod=? "
                    + " OR a.numescolar=? "
                    + procesarCadena("CONCAT(a.apellido1,' ',a.apellido2,', ',a.nombre)")
                    + procesarCadena("CONCAT(a.apellido1,' ',a.apellido2,', ',a.nombre)")
                    + procesarCadena("CONCAT(a.nombre,' ',a.apellido1,' ',a.apellido2)")
                    + procesarCadena("CONCAT(a.nombre,' ',a.apellido1,' ',a.apellido2)")
                    + " ) AND ( "
                    + procesarCadena("u.cursogrupo", false)
                    + ") "
                    //Si hay filtro de unidad lo asignamos
                    + ((u != null) ? " AND u.id='" + u.getId() + "' " : "")
                    + " ORDER BY u.posicion ASC," + Alumno.getCampoOrdenNombre("a") + " ASC LIMIT 0,100";
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
        }
        return st;
    }

    public void prepararBusqueda() {
    }

    public void buscar(final String texto) {
        if (threadBusqueda != null && threadBusqueda.isAlive()) {
            threadBusqueda.interrupt();
            try {
                threadBusqueda.join();
            } catch (InterruptedException ex) {
                Logger.getLogger(BuscadorAlumnos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        prepararBusqueda();
        threadBusqueda = new Thread() {

            @Override
            public void run() {
                _buscar(texto);
            }
        };
        threadBusqueda.setPriority(Thread.NORM_PRIORITY);
        threadBusqueda.start();

    }

    public void dispose() {
        Obj.cerrar(st);
    }

    private void _buscar(final String texto) {
        if (!Beans.isDesignTime()) {
            getResultadoBusqueda().clear();
            String texto2 = texto.toLowerCase();
            String grupo = "%";
            String alumno = texto2;
            if (texto2.contains(",")) {
                alumno = texto2.substring(0, texto2.indexOf(",")).trim();
                grupo = texto2.substring(texto2.indexOf(",") + 1).trim();
                if (grupo.length() == 2) {
                    grupo = grupo.charAt(0) + "%" + grupo.charAt(1);
                }
            }
            try {
                getSt().setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                getSt().setInt(2, Num.getInt(texto, -999));
                getSt().setString(3, texto2);
                String alumnoLike = alumno + "%";
                String alumnoLike2 = "% " + alumno + "%";
                String grupoLike = grupo + "%";
                getSt().setString(4, alumnoLike);
                getSt().setString(5, alumnoLike2);
                getSt().setString(6, alumnoLike);
                getSt().setString(7, alumnoLike2);
                getSt().setString(8, grupoLike);
                ResultSet res = st.executeQuery();
                while (res.next() && !Thread.interrupted()) {
                    ResultadoBusquedaAlumno rba = new ResultadoBusquedaAlumno();
                    rba.setId(res.getInt(1));
                    rba.setNombre(res.getString(2));
                    rba.setUnidad(res.getString(3));
                    System.out.println(rba.getUnidad() + " " + rba.getNombre());
                    addResultadoBusqueda(rba);
                }
                Obj.cerrar(res);
            } catch (SQLException ex) {
                Logger.getLogger(BuscadorAlumnos.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
