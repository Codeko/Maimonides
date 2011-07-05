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
