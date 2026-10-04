@echo off
cd /d "%~dp0"
if not exist "bin" (
    call compile.bat
)
javac -cp bin -encoding UTF-8 -d bin src\com\equishare\WorkflowTest.java
java -cp bin com.equishare.WorkflowTest
