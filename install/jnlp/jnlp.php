<?php
header('Content-Type: application/x-java-jnlp-file');
// HTTP/1.1 
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
// HTTP/1.0 
header("Pragma: no-cache");
$version = file_get_contents("version.txt");
//Propiedades de configuración de la aplicación
$properties = array();
echo '<?xml version="1.0" encoding="UTF-8"?>';
$codebase = "http://" . $_SERVER['HTTP_HOST'] . dirname($_SERVER['REQUEST_URI']) . "/";
?>
<jnlp
    spec="6.0+"
    codebase="<?= $codebase ?>"
    version="<?= $version ?>"
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
    <security>
        <all-permissions/>
    </security>
    <update check="always"/>
    <resources>
        <j2se version="1.6+" java-vm-args="-Xmx512m"/>
        <jar href="Maimonides.jar" size="<?= filesize("Maimonides.jar") ?>" main="true"/>
        <?php
        foreach (scandir("lib") As $name) {
            if (strlen($name) > 4 && substr($name, -4) == ".jar") {
                echo "<jar size=\"" . filesize("lib/" . $name) . "\" href=\"lib/" . $name . "\"/>\n";
            }
        }
        ?>
        <?php
        foreach ($properties As $k => $v) {
            echo "<property name=\"$k\" value=\"$v\"/>";
        }
        ?>
    </resources>

    <application-desc main-class="com.codeko.apps.maimonides.MaimonidesApp">

    </application-desc>
</jnlp>

