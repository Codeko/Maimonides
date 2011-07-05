package com.codeko.apps.maimonides.ayuda;

import com.codeko.apps.maimonides.*;
import java.awt.Component;
import java.awt.MenuItem;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.help.HelpSetException;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Ayuda {

    private static HelpBroker helpBroker = null;
    private static HelpSet helpSet = null;

    private static HelpBroker getHelpBroker() {
        if (helpBroker == null) {
            helpBroker = getHelpSet().createHelpBroker();
        }
        return helpBroker;
    }

    private static HelpSet getHelpSet() {
        if (helpSet == null) {
            try {
                helpSet = new HelpSet(null, Ayuda.class.getResource("maimonides-hs.xml"));
            } catch (HelpSetException ex) {
                Logger.getLogger(Ayuda.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return helpSet;
    }

    public static void addHelpOnButton(Component componente) {
        getHelpBroker().enableHelpOnButton(componente, "maimonides.about", getHelpSet());
    }

    public static void addHelpOnButton(Component componente, String id) {
        getHelpBroker().enableHelpOnButton(componente, id, getHelpSet());
    }

    public static void addHelpOnButton(MenuItem menu) {
        getHelpBroker().enableHelpOnButton(menu, "maimonides.about", getHelpSet());
    }

    public static void addHelpOnButton(MenuItem menu, String id) {
        getHelpBroker().enableHelpOnButton(menu, id, getHelpSet());
    }

    public static void addHelpKey(Component componente) {
        getHelpBroker().enableHelpKey(componente, "maimonides.about", getHelpSet());
    }

    public static void addHelpKey(Component componente, String id) {
        getHelpBroker().enableHelpKey(componente, id, getHelpSet());
    }

    public static void mostrar() {
        mostrar(MaimonidesApp.getMaimonidesView().getFrame());
    }

    public static void mostrar(String id) {
        getHelpBroker().showID(id, "javax.help.SecondaryWindow", "principal");
    }

    public static void mostrar(Object obj) {
        mostrar(CSH.getHelpIDString(obj, null));
    }

}
