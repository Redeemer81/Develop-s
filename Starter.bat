@ECHO OFF
@title ������
ECHO.             ������������������������������������������������������
ECHO.             ��          Enter�� ������ ������ �����˴ϴ�.       ��
ECHO.             ������������������������������������������������������
ECHO.            
pause >nul
color A
start /b launch_world.bat
ping localhost -w 10>nul

start /b launch_login.bat
ping localhost -w 10>nul

start /b launch_channel.bat