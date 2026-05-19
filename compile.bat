@echo off
echo ============================================
echo   Java Chat Room - Compiling...
echo ============================================

set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set JAVAC="%JAVA_HOME%\bin\javac.exe"

if not exist "out" mkdir out

%JAVAC% -d out src\common\*.java src\server\*.java src\client\*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo   Compilation successful!
    echo   Output directory: out\
    echo.
    echo   To run the server:  run_server.bat
    echo   To run the client:  run_client.bat
) else (
    echo.
    echo   Compilation FAILED. Please check errors above.
)

echo ============================================
pause
