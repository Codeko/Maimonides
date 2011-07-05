/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.codeko.apps.maimonides.elementos;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.mysql.jdbc.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author codeko
 */
abstract public class ObjetoBD extends MaimonidesBean implements IObjetoBD {

    Integer id = null;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean borrar() {
        boolean ret = false;
        if (getId() != null) {
            try {
                PreparedStatement st = (PreparedStatement) MaimonidesApp.getApplication().getConector().getConexion().prepareStatement("DELETE FROM " + getTabla() + " WHERE id=?");
                st.setInt(1, getId());
                ret = st.executeUpdate() > 0;
                if (ret) {
                    firePropertyChange("borrado", null, true);
                    resetearCache();
                    //setId(null);
                }
            } catch (SQLException ex) {
                Logger.getLogger(ObjetoBD.class.getName()).log(Level.SEVERE, "Error borrando " + getNombreObjeto() + ": " + this, ex);
            }
        }
        return ret;
    }

    protected void resetearCache() {
    }
}
