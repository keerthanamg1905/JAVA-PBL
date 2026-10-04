@echo off
cd /d "%~dp0"
if not exist "bin" (
    call compile.bat
)
echo Launching EquiShare Pro GUI...
start javaw -cp bin com.equishare.Main %*
