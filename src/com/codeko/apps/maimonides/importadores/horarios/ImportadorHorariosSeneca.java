package com.codeko.apps.maimonides.importadores.horarios;

import com.codeko.apps.maimonides.elementos.AnoEscolar;
import com.codeko.apps.maimonides.elementos.Horario;
import com.codeko.apps.maimonides.elementos.Profesor;
import com.codeko.apps.maimonides.seneca.ClienteSeneca;
import com.codeko.util.estructuras.Par;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase encargada de importar los horarios directamente desde la interfaz de Séneca.
 *
 * @author codeko
 */
public class ImportadorHorariosSeneca extends ImportadorHorarios {

    ClienteSeneca cli = null;

    public ImportadorHorariosSeneca(AnoEscolar ano, ClienteSeneca cli) {
        super();
        setCli(cli);
        setAnoEscolar(ano);
    }

    public ClienteSeneca getCli() {
        return cli;
    }

    public final void setCli(ClienteSeneca cli) {
        this.cli = cli;
    }

    @Override
    public boolean importarHorarios() {
        boolean ret = true;
        try {
            ArrayList<Par<Profesor,ArrayList<Horario>>> horarios=getCli().getDatosHorarios(getAnoEscolar());
            ret=guardarHorarios(horarios);
        } catch (Exception ex) {
            Logger.getLogger(ImportadorHorariosSeneca.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
}
