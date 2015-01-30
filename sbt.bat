@echo off
pushd "%~dp0"

:: This script will configure the JVM memory constraints and fire up the SBT instance.
:: If no arguments were provided to the script, SBT will start in shell mode.

:: -XX:MaxPermSize JVM option is not required if you are running on JVM 8+
:: input.encoding is neccessary to fix jline problems in SBT shell

java ^
  -XX:MaxPermSize=256m ^
  -Dinput.encoding=Cp1252 ^
  -Xss2m -Xms1g -Xmx1g ^
  -jar project\strap\gruj_vs_sbt-launch-0.13.x.jar ^
  %*

popd
