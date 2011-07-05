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
