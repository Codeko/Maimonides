/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.codeko.apps.maimonides;

/**
 *
 * @author Codeko
 */
public interface ICargable {
    public void cargar();
    public void vaciar();
    public boolean isCargado();
    public void setCargado(boolean cargado);
}
