package com.codeko.apps.maimonides.mantenimiento;

import com.codeko.apps.maimonides.MaimonidesApp;
import com.codeko.apps.maimonides.MaimonidesBean;
import com.codeko.util.Archivo;
import com.codeko.util.Fechas;
import com.codeko.util.Obj;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class Backup extends MaimonidesBean {

    File ultimoDestino = null;
    Process ultimoProceso = null;

    public Process getUltimoProceso() {
        return ultimoProceso;
    }

    public void setUltimoProceso(Process ultimoProceso) {
        this.ultimoProceso = ultimoProceso;
    }

    public File getUltimoDestino() {
        return ultimoDestino;
    }

    public void setUltimoDestino(File ultimoDestino) {
        this.ultimoDestino = ultimoDestino;
    }

    public File hacerBackup(boolean comprimido) {
        return hacerBackup(MaimonidesApp.getApplication().getConfiguracion().getCarpetaCopias(), comprimido);
    }

    public File hacerBackup(File directorio, boolean comprimido) {
        firePropertyChange("message", null, "Realizando copia de seguridad...");
        File f = new File(directorio, "Maimonides_" + Fechas.format(new GregorianCalendar(), "yyyy-MM-dd_HH.mm.ss") + ".sql");
        directorio.mkdirs();
        setUltimoDestino(f);
        File exe = new File("bin", "mysqldump.exe");
        if (exe.exists()) {
            ProcessBuilder pb = new ProcessBuilder(exe.getAbsolutePath(), "-v", "-C", "--triggers", "-R", "-h", MaimonidesApp.getApplication().getConector().getHost(), "-r", f.getAbsolutePath(), "-u", MaimonidesApp.getApplication().getConector().getUsuario(), "-p" + MaimonidesApp.getApplication().getConector().getClave(), MaimonidesApp.getApplication().getConector().getBaseDeDatos());
            Logger.getLogger(Backup.class.getName()).info(pb.command().toString());
            try {
                pb.redirectErrorStream(true);
                Process p = pb.start();
                setUltimoProceso(p);
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String l = null;
                while ((l = br.readLine()) != null) {
                    firePropertyChange("message", null, l);
                }
                Obj.cerrar(br);
                if (p.waitFor() != 0) {
                    f.delete();
                    f = null;
                    firePropertyChange("message", null, "Error realizando copia de seguridad.");
                } else {
                    if (comprimido) {
                        firePropertyChange("message", null, "Comprimiendo copia de seguridad.");
                        File zip = new File(f.getParentFile(), f.getName() + ".zip");
                        Archivo.comprimirZip(zip, f);
                        f.delete();
                        f = zip;
                    }
                    firePropertyChange("message", null, "Copia de seguridad realizada con éxito.");
                }
            } catch (Exception ex) {
                Logger.getLogger(Backup.class.getName()).log(Level.SEVERE, null, ex);
                firePropertyChange("message", null, "Error realizando copia de seguridad.");
            }
        }//TODO Quizás habría que hacer algo si no existe el archivo
        return f;
    }

    public boolean esNecesarioHacerBackup() {
        return (Fechas.getDiferenciaTiempoEn(new GregorianCalendar(), MaimonidesApp.getApplication().getConfiguracion().getUltimoBackup(), GregorianCalendar.DAY_OF_MONTH) > 0);
    }

    public boolean importar(File f) {
        firePropertyChange("message", null, "Restaurando copia de seguridad...");
        boolean ret = false;
        if (f.exists()) {
            if (f.getName().toLowerCase().endsWith(".zip")) {
                firePropertyChange("message", null, "Descomprimiendo copia de seguridad...");
                ArrayList<File> arch = Archivo.descomprimirZip(f, new File(System.getProperty("java.io.tmpdir")), true);
                if (arch.size() > 1 || arch.isEmpty()) {
                    firePropertyChange("message", null, "El archivo no es una copia de seguridad válida...");
                    return false;
                } else {
                    f = arch.get(0);
                }
            }
            try {
                File tmpDest = File.createTempFile("backup", ".sql");
                if (f.renameTo(tmpDest)) {
                    f = tmpDest;
                }
                ProcessBuilder pb = new ProcessBuilder("bin/importar.bat", new File("bin/mysql.exe").getAbsolutePath(), MaimonidesApp.getApplication().getConector().getHost(), MaimonidesApp.getApplication().getConector().getUsuario(), MaimonidesApp.getApplication().getConector().getClave(), MaimonidesApp.getApplication().getConector().getBaseDeDatos(), f.getAbsolutePath());
                Logger.getLogger(Backup.class.getName()).info(pb.command().toString());
                Process p = pb.start();
                InputStream is = p.getInputStream();
                int c = -1;
                while ((c = is.read()) != -1) {
                    System.out.print((char) c);
                }
                if (p.waitFor() != 0) {
                    firePropertyChange("message", null, "Error restaurando copia de seguridad.");
                    Logger.getLogger(Backup.class.getName()).log(Level.INFO, "ERROR: RET:{0}", p.exitValue());
                } else {
                    firePropertyChange("message", null, "Copia de seguridad restaurada con éxito.");
                    ret = true;
                }
                tmpDest.delete();
                f.delete();
            } catch (Exception ex) {
                Logger.getLogger(Backup.class.getName()).log(Level.SEVERE, null, ex);
                firePropertyChange("message", null, "Error restaurando copia de seguridad.");
            }
        } else {
            firePropertyChange("message", null, "No se puede acceder al archivo de copia de seguridad.");
        }
        return ret;
    }
}
