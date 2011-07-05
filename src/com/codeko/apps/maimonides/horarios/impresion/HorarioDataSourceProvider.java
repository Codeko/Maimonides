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


package com.codeko.apps.maimonides.horarios.impresion;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRAbstractBeanDataSourceProvider;
import net.sf.jasperreports.engine.design.JRDesignField;

/**
 *
 * @author codeko
 */
public class HorarioDataSourceProvider extends JRAbstractBeanDataSourceProvider{
    HorarioDataSource hds=null;
    public HorarioDataSourceProvider(HorarioDataSource hds){
        super(HorarioImprimible.class);
        this.hds=hds;
    }

    @Override
    public JRDataSource create(JasperReport jr) throws JRException {
        return hds;
    }

    @Override
    public void dispose(JRDataSource jrds) throws JRException {

    }

    @Override
    public JRField[] getFields(JasperReport report) throws JRException {
        JRField[] campos=new JRField[1];
        JRDesignField f=new JRDesignField();
        f.setName("titulo");
        f.setValueClass(String.class);
        campos[0]=f;
        return campos;
    }

}
