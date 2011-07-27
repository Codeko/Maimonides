Name "Maimónides"
Caption "Maimónides"
Icon "ico.ico"
OutFile "maimonides.exe"
 
SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow
 
Section ""
  SetOutPath $EXEDIR
  ExecWait '"javaw"  -Xms128m -Xmx768m  -cp ".;./lib/*" -jar Maimonides.jar'
SectionEnd
