package com.codeko.apps.maimonides.horarios;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class BloqueHorarioTransferible implements Transferable {

    public static final DataFlavor BH_FLAVOR = new DataFlavor("codeko/maimonides-bloque-horario", "BloqueHorario");
    BloqueHorario bloque = null;

    public BloqueHorarioTransferible(BloqueHorario bloque) {
        this.bloque = bloque;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] flavs = new DataFlavor[]{BH_FLAVOR};
        return flavs;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavor.equals(BH_FLAVOR));
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        return bloque;
    }
}
