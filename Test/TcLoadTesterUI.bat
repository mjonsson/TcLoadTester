@echo on

:: Set the SOA_CLIENT to the path of your Teamcenter Java SOA Client libraries (default "Libs" folder)
set SOA_CLIENT=
:: If not JAVA_HOME set on system, you can set it here
::set JAVA_HOME=
:: Set JMX binding and port
set JMX_BINDING=0.0.0.0
set JMX_PORT=9010

:: !!! Do NOT perform any changes below this line !!!

:: Verify we have a JRE to use
set JAVA=
if "x%JAVA_HOME%"=="x" set JAVA=java
if "x%JAVA%"=="x" set JAVA=%JAVA_HOME%\bin\java
if "x%SOA_CLIENT%"=="x" set SOA_CLIENT=%~dp0Libs
if not exist "%SOA_CLIENT%\xerces.jar" goto NO_SOA

:: Verify that SWT_LIB exist
set SWT_LIB=%~dp0swt_x86_64.jar
if not exist "%SWT_LIB%" goto NO_SWT
set MODULES=%~dp0Modules

:: Start TcLoadTester
"%JAVA%" -Xmx1024m -Xms32m -XX:PermSize=32m -XX:MaxPermSize=128m -XX:-UseParallelGC -Dcom.sun.management.jmxremote -Djava.rmi.server.hostname=%JMX_BINDING% -Dcom.sun.management.jmxremote.port=%JMX_PORT% -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -cp "%~dp0\TcLoadTester.jar;%SWT_LIB%;%MODULES%\*;%SOA_CLIENT%\*" com.siemens.tcloadtester.TcLoadTester %*

goto EXIT

:NO_SOA
echo Please set the SOA_CLIENT variable to point to your SOA Client java libraries.

goto EXIT

:NO_SWT
echo Could not locate SWT libraries.

goto EXIT

:EXIT
exit /b