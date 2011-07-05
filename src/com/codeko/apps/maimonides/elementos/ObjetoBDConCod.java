package com.codeko.apps.maimonides.elementos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.util.Num;
import com.codeko.util.Obj;
import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
public abstract class ObjetoBDConCod extends ObjetoBD implements IObjetoBDConCod {

    static boolean soloInsertar = false;

    public static boolean isSoloInsertar() {
        return ObjetoBDConCod.soloInsertar;
    }

    public static void setSoloInsertar(boolean soloInsertar) {
        ObjetoBDConCod.soloInsertar = soloInsertar;
    }

    public abstract boolean _guardar(boolean recrearEliminados);

    @Override
    public boolean guardar() {
        return guardar(false);
    }

    @Override
    public boolean guardar(boolean recrearEliminados) {
        boolean g = true;
        //Cargamos la ID si no existe
        cargarIdDesdeCod();
        if (isSoloInsertar() && Num.getInt(getId()) != 0) {
            g = false;
        }
        if (g) {
            return _guardar(recrearEliminados);
        }
        if(recrearEliminados) {
            recuperarBorrado();
        }
        return true;
    }

    @Override
    public int cargarIdDesdeCod() {
        if ((getId() == null || Num.getInt(getId()) == 0) && Num.getInt(getCodigo()) != 0) {
            String sql = "SELECT id FROM " + getTabla() + " WHERE ano=? AND cod=? ";
            PreparedStatement st = null;
            ResultSet res = null;
            try {
                st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
                st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
                st.setInt(2, getCodigo());
                res = st.executeQuery();
                if (res.next()) {
                    setId(res.getInt(1));
                }
            } catch (SQLException ex) {
                Logger.getLogger(Profesor.class.getName()).log(Level.SEVERE, null, ex);
            }
            Obj.cerrar(res, st);
        }
        return Num.getInt(getId());
    }

    @Override
    public boolean recuperarBorrado() {
        boolean ret = false;
        String sql = "UPDATE " + getTabla() + " SET fbaja=NULL WHERE ano=? AND id=? ";
        PreparedStatement st = null;

        try {
            st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement(sql);
            st.setInt(1, MaimonidesApp.getApplication().getAnoEscolar().getId());
            st.setInt(2, getId());
            int res = st.executeUpdate();
            ret = res > 0;
        } catch (SQLException ex) {
            Logger.getLogger(Profesor.class.getName()).log(Level.SEVERE, null, ex);
        }
        Obj.cerrar(st);
        return ret;
    }
}
