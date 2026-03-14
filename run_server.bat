@echo off
echo Compiling Project...
javac -cp "src;lib/sqlite-jdbc.jar" src/common/*.java src/server/*.java src/client/*.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation Failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Starting HRM Server...
echo (Check src/server/HRMServer.java for source)
java -cp "src;lib/sqlite-jdbc.jar" server.HRMServer
pause
