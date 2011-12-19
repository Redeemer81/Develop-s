@ECHO OFF
@title 구동기
ECHO.             ┌─────────────────────────┐
ECHO.             │          Enter를 누르면 서버가 가동됩니다.       │
ECHO.             └─────────────────────────┘
ECHO.            
pause >nul
color A
start /b launch_world.bat
ping localhost -w 10>nul

start /b launch_login.bat
ping localhost -w 10>nul

start /b launch_channel.bat