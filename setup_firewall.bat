@echo off
title Firewall Setup - Port 1156

:: ============================================
:: Self-elevate to Administrator automatically
:: (UAC popup will appear - click YES)
:: ============================================
net session >nul 2>&1
if %errorlevel% neq 0 (
    echo [*] Requesting Administrator access...
    echo     Please click YES on the UAC popup.
    echo.
    powershell -Command "Start-Process '%~f0' -Verb RunAs"
    exit /b
)

:: Now running as Admin
cls
echo ===================================================
echo    Distributed Chat - Firewall Setup
echo    Opening port 1156 for RMI connections
echo ===================================================
echo.

echo [*] Removing old rules if they exist...
netsh advfirewall firewall delete rule name="Distributed Chat RMI 1156 IN"  >nul 2>&1
netsh advfirewall firewall delete rule name="Distributed Chat RMI 1156 OUT" >nul 2>&1

echo [*] Adding inbound rule for port 1156...
netsh advfirewall firewall add rule ^
    name="Distributed Chat RMI 1156 IN" ^
    dir=in ^
    action=allow ^
    protocol=TCP ^
    localport=1156

if %errorlevel% equ 0 (
    echo [+] Inbound rule added successfully!
) else (
    echo [-] Failed to add inbound rule.
)

echo [*] Adding outbound rule for port 1156...
netsh advfirewall firewall add rule ^
    name="Distributed Chat RMI 1156 OUT" ^
    dir=out ^
    action=allow ^
    protocol=TCP ^
    localport=1156

if %errorlevel% equ 0 (
    echo [+] Outbound rule added successfully!
) else (
    echo [-] Failed to add outbound rule.
)

echo.
echo ===================================================
echo  [DONE] Port 1156 is now open in Windows Firewall!
echo         Client PCs on the network can connect.
echo ===================================================
echo.
echo  Next Steps:
echo  1. Make sure MySQL is running (XAMPP / WAMP)
echo  2. Run compile.bat  - compile the project
echo  3. Run run_server.bat - start the server
echo  4. Copy client_package\ to other PCs and
echo     run run_client.bat there
echo.
pause
