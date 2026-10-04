@echo off
cd /d "%~dp0"
echo ========================================================
echo       Pushing EquiShare Pro to GitHub Repository
echo       https://github.com/keerthanamg1905/JAVA-PBL.git
echo ========================================================
echo.
git push -u origin main
echo.
if %ERRORLEVEL% equ 0 (
    echo [SUCCESS] Repository pushed to GitHub successfully!
) else (
    echo [ERROR] Push failed. If prompted, please authorize GitHub login in your browser.
)
pause
