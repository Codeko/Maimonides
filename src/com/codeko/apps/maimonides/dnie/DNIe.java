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
package com.codeko.apps.maimonides.dnie;

import com.codeko.apps.maimonides.MaimonidesBean;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import com.codeko.util.Obj;
import java.io.BufferedOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.swingx.util.OS;

import java.security.cert.X509Certificate;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.util.Enumeration;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.ocsp.BasicOCSPResp;
import org.bouncycastle.ocsp.CertificateID;
import org.bouncycastle.ocsp.OCSPReq;
import org.bouncycastle.ocsp.OCSPReqGenerator;
import org.bouncycastle.ocsp.OCSPResp;
import org.bouncycastle.ocsp.SingleResp;

/**
 *
 * @author Codeko <codeko@codeko.com>
 */
public class DNIe extends MaimonidesBean {

    private static final String confLinux = "name=OpenSC-OpenDNIe\nlibrary=/usr/lib/opensc-pkcs11.so\n";
    private static final String confWindows = "name=OpenSC-OpenDNIe\r\nlibrary=C:\\WINDOWS\\system32\\opensc-pkcs11.dll\r\n";
    private static final String confMac = "name=OpenSC-OpenDNIe\nlibrary=/usr/local/lib/opensc-pkcs11.so\n";
    private static final String certAlias = "CertAutenticacion";
    private static final String certFirma = "CertFirmaDigital";
    KeyStore keyStore = null;
    private CardTerminal cardTerminal = null;
    private String nif = null;
    private String fullName = null;
    private String pin = null;

    public DNIe(CardTerminal ct) {
        setCardTerminal(ct);
    }

    private String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    private KeyStore getKeyStore() throws Exception {
        if (keyStore == null) {
            Provider p = new sun.security.pkcs11.SunPKCS11(new ByteArrayInputStream(DNIe.getConfig()));
            Security.addProvider(p);
            keyStore = KeyStore.getInstance("PKCS11", p);
            try {
                keyStore.load(null, getPin().toCharArray());
            } catch (Exception e) {
                keyStore = null;
                throw e;
            }
        }
        return keyStore;
    }

    public String getFullName() {
        return fullName;
    }

    private void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNif() {
        return nif;
    }

    private void setNif(String nif) {
        this.nif = nif;
    }

    public CardTerminal getCardTerminal() {
        return cardTerminal;
    }

    private void setCardTerminal(CardTerminal cardTerminal) {
        this.cardTerminal = cardTerminal;
    }

    public boolean loadPublicData() {
        firePropertyChange("message", null, "Conectando con la tarjeta...");
        Card c = null;
        CardChannel ch = null;
        try {
            System.setProperty("sun.security.smartcardio.t0GetResponse", "false");

            c = getCardTerminal().connect("T=0");
            ch = c.getBasicChannel();
            firePropertyChange("message", null, "Leyendo datos públicos de la tarjeta...");


            int offset = 0;
            byte[] command = null;
            ResponseAPDU r = null;
//            command = new byte[]{(byte) 0x00, (byte) 0xa4, (byte) 0x04, (byte) 0x00, (byte) 0x0b, (byte) 0x4D, (byte) 0x61, (byte) 0x73, (byte) 0x74, (byte) 0x65, (byte) 0x72, (byte) 0x2E, (byte) 0x46, (byte) 0x69, (byte) 0x6C, (byte) 0x65};
//            r = ch.transmit(new CommandAPDU(command));
//            System.out.println(r.getSW());
//            if ((byte) r.getSW() != (byte) 0x9000) {
//                System.out.println("SW incorrecto");
//                return false;
//            }

            //Seleccionamos el directorio PKCS#15 5015
            command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x50, (byte) 0x15};
            r = ch.transmit(new CommandAPDU(command));

            if ((byte) r.getSW() != (byte) 0x9000) {
                System.out.println("SW incorrecto P1: " + r.getSW());
                //return false;
            }

            //Seleccionamos el Certificate Directory File (CDF) del DNIe 6004
            command = new byte[]{(byte) 0x00, (byte) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x02, (byte) 0x60, (byte) 0x04};
            r = ch.transmit(new CommandAPDU(command));

            if ((byte) r.getSW() != (byte) 0x9000) {
                System.out.println("SW incorrecto p2: " + r.getSW());
                //return false;
            }

//            command = new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x01, (byte) 0x00, (byte) 0xFF};
//            r = ch.transmit(new CommandAPDU(command));
//            System.out.append("Nombre: " + new String(r.getData()));
//            

//            r = ch.transmit(new CommandAPDU(new BigInteger("00CA02C200", 16).toByteArray()));
//            System.out.append("Nombre: " + new String(r.getData()));
//            r = ch.transmit(new CommandAPDU(new BigInteger("00CA02D009", 16).toByteArray()));
//            System.out.append("\r\nDNI: " + new String(r.getData()));
//            r = ch.transmit(new CommandAPDU(new BigInteger("00CA02E109", 16).toByteArray()));
//            System.out.append("\r\nIDESP: " + new String(r.getData()));

            //Leemos FF bytes del archivo
            command = new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0xFF};
            r = ch.transmit(new CommandAPDU(command));


            if ((byte) r.getSW() == (byte) 0x9000) {
                byte[] r2 = r.getData();
//                try {
//                    String s = new String(r2);
//                    System.out.println(s);
//                    int porcent = s.indexOf("%");
//                    int fin = s.indexOf("(", porcent);
//                    setFullName(s.substring(porcent + 1, fin));
//                } catch (Exception ex) {
//                    Logger.getLogger(DNIe.class.getName()).log(Level.SEVERE, null, ex);
//                }

                if (r2[4] == 0x30) {
                    offset = 4;
                    offset += r2[offset + 1] + 2; //Obviamos la seccion del Label           
                }

                if (r2[offset] == 0x30) {
                    offset += r2[offset + 1] + 2; //Obviamos la seccion de la informacion sobre la fecha de expedición etc
                }

                if (r2[offset] == (byte) 0xA1) {
                    //El certificado empieza aquí
                    byte[] r3 = new byte[9];
                    //118
                    //Nos posicionamos en el byte donde empieza el NIF y leemos sus 9 bytes
                    for (int z = 0; z < 9; z++) {
                        r3[z] = r2[109 + z];
                    }
                    setNif(new String(r3));
                    return true;
                }

            }
        } catch (Exception e) {
            Logger.getLogger(DNIe.class.getName()).log(Level.SEVERE, null, e);
            firePropertyChange("message", null, "Error leyendo datos de tarjeta. Pruebe a volver a conectarla.");
        } finally {
            if (c != null) {
                try {
                    c.disconnect(false);
                } catch (CardException ex) {
                    Logger.getLogger(DNIe.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Obj.cerrar(ch);
        }
        return false;
    }

    private X509Certificate getCertificadoAutentificacion() throws Exception {
        return (X509Certificate) getCertificado(certAlias);
    }

    private Certificate getCertificadoFirma() throws Exception {
        return getCertificado(certFirma);
    }

    private Certificate getCertificado(String alias) throws Exception {
        Certificate myCert = null;
        Enumeration<String> aliases = getKeyStore().aliases();
        while (aliases.hasMoreElements()) {
            String currAlias = aliases.nextElement();
            if (currAlias.equals(alias)) {
                myCert = getKeyStore().getCertificate(currAlias);
            }
        }
        return myCert;
    }

    private static byte[] getConfig() throws Exception {
        String customConfig = com.codeko.apps.maimonides.dnie.Configuracion.getOpenSCPath();
        if (!customConfig.trim().equals("")) {
            return customConfig.getBytes();
        } else if (OS.isLinux()) {
            return confLinux.getBytes();
        } else if (OS.isWindows()) {
            return confWindows.getBytes();
        } else if (OS.isMacOSX()) {
            return confMac.getBytes();
        }
        throw new Exception("No está configurada la ruta a OpenSC y no se puede detectar automáticamente");
    }

    public boolean autentificarValidar(String pin) {
        setPin(pin);
        return autentificar() && validar();
    }

    public boolean autentificar() {
        boolean result = false;
        try {
            firePropertyChange("message", null, "Verficando clave de DNIe");
            Certificate c = getCertificadoAutentificacion();
            if (c != null) {
                if (!(c instanceof X509Certificate)) {
                    throw new Exception("Los datos no corresponden a un certificado válido");
                }
                X509Certificate x509 = (X509Certificate) c;
                System.out.println(x509.getSubjectDN());
                x509.checkValidity(); // throw exception if certificate expired or not yet valid
                boolean flags[] = x509.getKeyUsage();
                if (!flags[0]) // check digitalSignature usage flag
                {
                    throw new Exception("El certificado no es válido para autenticación");
                }

                /* FASE 2: creación de un reto (challenge) para verificacion de claves */
                byte[] challenge = new byte[8];
                for (int n = 0; n < 8; n++) {
                    challenge[n] = new Double(256.0 * Math.random()).byteValue();
                }

                /* FASE 3: firma del reto */

                /* Extraemos la referencia a la clave privada del certificado */
                Key prkey = getKeyStore().getKey(certAlias, getPin().toCharArray());
                if (!(prkey instanceof PrivateKey)) {
                    throw new Exception("El certificado no tiene asociada una clave privada");
                }

                /* preparamos y ejecutamos la operacion de firma */
                Signature sig = Signature.getInstance("SHA1withRSA");
                sig.initSign((PrivateKey) prkey);
                sig.update(challenge);
                byte signature[] = sig.sign();

                /* FASE 4: verificacion de la firma */

                /* verificamos la firma realizada */
                sig = Signature.getInstance("SHA1withRSA"); // create a new signature instance for verify
                sig.initVerify(c);
                sig.update(challenge);
                result = sig.verify(signature);

            }
        } catch (Exception e) {
            Logger.getLogger(DNIe.class.getName()).log(Level.SEVERE, null, e);
            firePropertyChange("message", null, "Clave de DNIe no válida");
        }
        return result;
    }

    private X509Certificate getCertCAIntermedia(X509Certificate cert)
            throws CertificateException, FileNotFoundException {
        //http://www.dnielectronico.es/seccion_integradores/auto_cert_sub.html
        /* En la validación OCSP se tendrá que usar el certificado de la CA subordinada que emitió el certificado */
        String issuerCN = cert.getIssuerX500Principal().getName("CANONICAL");
        CertificateFactory cfIssuer = CertificateFactory.getInstance("X.509");
        X509Certificate certCA = null;
        if (issuerCN.contains("cn=ac dnie 001")) {
            certCA = (X509Certificate) cfIssuer.generateCertificate(this.getClass().getResourceAsStream("certs/ACDNIE001-SHA1.crt"));
        } else if (issuerCN.contains("cn=ac dnie 002")) {
            certCA = (X509Certificate) cfIssuer.generateCertificate(this.getClass().getResourceAsStream("certs/ACDNIE002-SHA1.crt"));
        } else if (issuerCN.contains("cn=ac dnie 003")) {
            certCA = (X509Certificate) cfIssuer.generateCertificate(this.getClass().getResourceAsStream("certs/ACDNIE003-SHA1.crt"));
        }
        return certCA;
    }

    private X509Certificate getCertCARaiz() throws CertificateException, FileNotFoundException {
        //http://www.dnielectronico.es/seccion_integradores/autoridades_cert.html
        CertificateFactory cfIssuer = CertificateFactory.getInstance("X.509");
        X509Certificate certCA =
                (X509Certificate) cfIssuer.generateCertificate(this.getClass().getResourceAsStream("/certs/ACRAIZ-SHA1.crt"));
        return certCA;
    }

    public boolean validar() {
        boolean ret = false;
        try {
            firePropertyChange("message", null, "Verificando validez y caducidad del certificado.");
            X509Certificate c = getCertificadoAutentificacion();
            /* Carga del proveedor necesario para la petición OCSP  */
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            /* Se crea un nuveo objeto OCSPReqGenerator para realizar la petición OCSP  */
            OCSPReqGenerator ocspReqGen = new OCSPReqGenerator();
            /* Se añaden el certificado de la CA intermedia y los certificados a verificar (número de serie) a la petición OCSP  */

            /* Se añade el certificado que se desea verificar a la petición OCSP */
            CertificateID certid = new CertificateID(CertificateID.HASH_SHA1, getCertCAIntermedia(c), c.getSerialNumber());
            ocspReqGen.addRequest(certid);
            /* Generación de la petición OCSP */
            OCSPReq ocspReq = ocspReqGen.generate();

            /* Establecimiento de la conexión con el servidor OCSP del DNIe  */

            /* Introducir la URL del servidor OCSP del DNIe */
            URL url = new URL("http://ocsp.dnie.es");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            /* Indicar las propiedas de la peticion HTTP */
            con.setRequestProperty("Content-Type", "application/ocsp-request");
            con.setRequestProperty("Accept", "application/ocsp-response");
            con.setDoOutput(true);
            OutputStream out = con.getOutputStream();
            DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(out));
            /*Envío de la petición OCSP al servidor OCSP del DNIe  */
            dataOut.write(ocspReq.getEncoded());
            dataOut.flush();
            dataOut.close();


            /* Parseo de la respuesta y obtención del estado del certificado retornado por el OCSP  */
            InputStream in = con.getInputStream();
            BasicOCSPResp basicResp = (BasicOCSPResp) new OCSPResp(in).getResponseObject();


            /* cierre de conexion y limpieza */
            con.disconnect();
            out.close();
            in.close();
            for (SingleResp singResp : basicResp.getResponses()) {
                Object status = singResp.getCertStatus();
                String serial = Integer.toHexString(singResp.getCertID().getSerialNumber().intValue());
                if (status instanceof org.bouncycastle.ocsp.UnknownStatus) {
                    System.out.println("Certificado con numero de serie " + serial + " desconocido");
                } else if (status instanceof org.bouncycastle.ocsp.RevokedStatus) {
                    System.out.println("Certificado con numero de serie " + serial + " revocado");
                } else {
                    System.out.println("Certificado con numero de serie " + serial + " valido");
                    ret = true;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(DNIe.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    public byte[] firmar(File archivo) throws Exception {
        Key key = getKeyStore().getKey(certFirma, getPin().toCharArray());
        if (!(key instanceof PrivateKey)) {
            throw new Exception("El certificado no tiene asociada una clave privada");
        }
        /* volcamos a memoria el fichero */
        FileInputStream fis = new FileInputStream(archivo);
        /* preparamos la operacion de firma */
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign((PrivateKey) key);
        sig.update(IOUtils.toByteArray(fis));
        fis.close();
        /* firmamos los datos y retornamos el resultado */
        return sig.sign();
    }

    @Override
    public String toString() {
        return getFullName() + "(" + getNif() + ")";
    }
}
