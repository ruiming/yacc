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
    set bnf=!testPath!\input.bnf
    echo \\ Testing !testName!
    for /f "delims=" %%b in ('"dir !testPath! /b | findstr /E .tok"') do (
        set fileName=%%~b
        set filePath=!testPath!\!fileName!
        for /f "delims=#" %%c in ('"java -cp %out% Main !bnf! !filePath!"') do (
            set result=%%~c
            if !result!==true (
                echo True  !testName!\!fileName!
            ) else if !result!==false (
                echo False !testName!\!fileName!
            ) else (
                echo !result! !testName!\!fileName!
            )
        )
    )
)