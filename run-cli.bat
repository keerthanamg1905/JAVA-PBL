@echo off
cd /d "%~dp0"
if not exist "bin" (
    call compile.bat
)
echo Launching EquiShare Pro Interactive Console CLI...
java -cp bin com.equishare.Main --cli %*
