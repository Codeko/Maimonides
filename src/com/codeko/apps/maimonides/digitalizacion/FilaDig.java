package com.codeko.apps.maimonides.digitalizacion;

/**
 * Copyright Codeko Informática 2008
 * www.codeko.com
 * @author Codeko
 */
public class FilaDig {

    int[] pix = null;
    int alto = 0;
    int pos = 0;
    int inicio = 0;

    public int getInicio() {
        return inicio;
    }

    private void setInicio(int inicio) {
        this.inicio = inicio;
    }

    public FilaDig(int[] pix, int alto, int pos, int inicio) {
        setPix(pix);
        setAlto(alto);
        setPos(pos);
        setInicio(inicio);
    }

    public int getAlto() {
        return alto;
    }

    private void setAlto(int alto) {
        this.alto = alto;
    }

    public int[] getPix() {
        return pix;
    }

    private void setPix(int[] pix) {
        this.pix = pix;
    }

    public int getPos() {
        return pos;
    }

    private void setPos(int pos) {
        this.pos = pos;
    }
}
