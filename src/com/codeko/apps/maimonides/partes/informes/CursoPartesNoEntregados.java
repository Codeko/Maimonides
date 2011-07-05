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


package com.codeko.apps.maimonides.partes.informes;

import com.codeko.apps.maimonides.elementos.Curso;
import com.codeko.swing.IObjetoTabla;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class CursoPartesNoEntregados implements IObjetoTabla {

    int partesNoFirmados = 0;
    Curso curso = null;

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public CursoPartesNoEntregados() {
    }

    public CursoPartesNoEntregados(Curso c, int partesNoFirmados) {
        setCurso(c);
        setPartesNoFirmados(partesNoFirmados);
    }

    public int getPartesNoFirmados() {
        return partesNoFirmados;
    }

    public void setPartesNoFirmados(int partesNoFirmados) {
        this.partesNoFirmados = partesNoFirmados;
    }

    @Override
    public int getNumeroDeCampos() {
        return 2;
    }

    @Override
    public Object getValueAt(int index) {
        Object val = getPartesNoFirmados();
        if (index != 0) {
            val = getCurso().getCurso();
        }
        return val;
    }

    @Override
    public String getTitleAt(int index) {
        String val = "Nº Partes";
        if (index != 0) {
            val = "Curso";
        }
        return val;
    }

    @Override
    public Class getClassAt(int index) {
        Class val = Integer.class;
        if (index != 0) {
            val = String.class;
        }
        return val;
    }

    @Override
    public boolean setValueAt(int index, Object val) {
        return false;
    }

    @Override
    public boolean esCampoEditable(int index) {
        return false;
    }
}
