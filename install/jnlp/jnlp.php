<?php
header('Content-Type: application/x-java-jnlp-file');
// HTTP/1.1 
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
// HTTP/1.0 
header("Pragma: no-cache");
//Propiedades de configuración de la aplicación
$properties = array();
echo '<?xml version="1.0" encoding="utf-8"?>';
$codebase = "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']);
?>
<jnlp
    spec="6.0+"
    codebase="<?= $codebase ?>"
    version="2.10"
    href="jnlp.php">
    <information>
        <title>Maimónides</title>
        <vendor>Codeko</vendor>
        <homepage href="http://maimonides.codeko.com"/>
        <description>Gestión para centros escolares compatible con Séneca</description>
        <description kind="short">Gestión de centros escolares</description>
        <icon kind="default" href="ico.gif"/>
        <icon kind="splash" href="splash.gif"/>
        <offline-allowed/> 
        <shortcut>
            <desktop/>
            <menu submenu="Maimónides"/>
        </shortcut>
    </information>
    <update check="always"/>
    <security>
        <all-permissions/>
    </security>
    <resources>
        <j2se version="1.6+" java-vm-args="-Xmx512m"/>
        <property name="jnlp.packEnabled" value="true"/>
        <property name="sun.awt.disableMixing" value="true"/>
        <?php
        foreach ($properties As $k => $v) {
            echo "<property name=\"$k\" value=\"$v\"/>";
        }
        ?>
        <jar href="Maimonides.jar" main="true"/>
        <?php
        foreach (scandir("lib") As $name) {
            if (strlen($name) > 4 && substr($name, -4) == ".jar") {
                echo "<jar href=\"lib/" . $name . "\"/>\n";
            }
        }
        ?>
    </resources>

    <application-desc main-class="com.codeko.apps.maimonides.MaimonidesApp">

    </application-desc>
</jnlp>

