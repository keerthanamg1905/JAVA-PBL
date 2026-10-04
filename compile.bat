@echo off
cd /d "%~dp0"
echo ========================================================
echo       Compiling EquiShare Pro Java Project
echo ========================================================
if not exist "bin" mkdir bin
if not exist "data" mkdir data

javac -encoding UTF-8 -d bin src\com\equishare\model\*.java src\com\equishare\service\*.java src\com\equishare\storage\*.java src\com\equishare\exception\*.java src\com\equishare\util\*.java src\com\equishare\ui\cli\*.java src\com\equishare\ui\swing\*.java src\com\equishare\Main.java

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] Compilation finished successfully!
    echo Output class files are in the 'bin' folder.
) else (
    echo.
    echo [ERROR] Compilation failed with errors!
)
