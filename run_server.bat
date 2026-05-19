@echo off
echo ============================================
echo   Starting Chat Server...
echo ============================================
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
"%JAVA_HOME%\bin\java.exe" -cp out server.ServerApp
pause
