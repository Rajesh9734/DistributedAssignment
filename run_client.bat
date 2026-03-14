@echo off
echo Compiling Client...
javac -cp "src;lib/sqlite-jdbc.jar" src/client/ClientApp.java src/client/LoginFrame.java src/client/HRDashboard.java src/client/EmployeeDashboard.java src/client/RMIClient.java src/common/User.java src/common/Employee.java src/common/FamilyDetail.java src/common/LeaveApplication.java src/common/HRMInterface.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation Failed!
    pause
    exit /b %ERRORLEVEL%
)

echo Starting Client Application...
java -cp "src;lib/sqlite-jdbc.jar" client.ClientApp
pause
