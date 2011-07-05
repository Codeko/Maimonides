package com.codeko.apps.maimonides.importadores;

import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.apps.maimonides.elementos.AnoEscolar;

/**
 *
 * @author codeko
 */
public class ImportadorBase extends MaimonidesBean {

    private String mensajeError = "";
    private AnoEscolar anoEscolar = null;

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public AnoEscolar getAnoEscolar() {
        return anoEscolar;
    }

    public final void setAnoEscolar(AnoEscolar anoEscolar) {
        firePropertyChange("anoEscolar", this.anoEscolar, anoEscolar);
        this.anoEscolar = anoEscolar;
    }
}
