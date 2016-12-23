@echo YACC Test
@echo Author: Ruiming Zhuang


@echo off
set path=%path%:C:\Windows\System32
set src=%cd%\src
set out=%cd%\bin
set test=%cd%\testcases

@echo Compiling Start
javac %src%\main.java -d %out% -encoding utf-8
@echo Compiling End

@echo Testing Start

@echo off & setlocal EnableDelayedExpansion

for /f "delims=" %%a in ('"dir %test% /B"') do (
    set testName=%%~a
    set testPath=%test%\!testName!

    echo ***** Testing !testName!
    java -cp %out% Main !testPath!
    echo ***** Testing End
)
pause