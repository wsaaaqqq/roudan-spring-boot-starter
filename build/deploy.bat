@echo off
pushd ..
setlocal enabledelayedexpansion

rem ============================================================
rem  Usage:
rem    deploy.bat          -> build + sign + publish to Maven Central
rem    deploy.bat test     -> dry run: build + sign to local .m2 only (NO upload)
rem ============================================================
set MODE=%1

rem Clean stale GPG lock files
powershell -Command "del $env:USERPROFILE\.gnupg\public-keys.d\*.lock -Force -ErrorAction SilentlyContinue; del $env:USERPROFILE\.gnupg\public-keys.d\.#lk* -Force -ErrorAction SilentlyContinue; del $env:USERPROFILE\.gnupg\private-keys-v1.d\*lock -Force -ErrorAction SilentlyContinue; del $env:USERPROFILE\.gnupg\*lock -Force -ErrorAction SilentlyContinue; del $env:USERPROFILE\.gnupg\.#lk* -Force -ErrorAction SilentlyContinue"

rem Pre-launch keyboxd
gpgconf --launch keyboxd

rem Extract GPG passphrase from Maven settings.xml
set MAVEN_GPG_PASSPHRASE=
for /f "usebackq tokens=*" %%a in (`powershell -Command "[xml]$s = Get-Content $env:USERPROFILE\.m2\settings.xml; $s.settings.servers.server | Where-Object { $_.id -eq 'xdb-gpg-passphrase' } | Select-Object -ExpandProperty password"`) do set MAVEN_GPG_PASSPHRASE=%%a

if "%MAVEN_GPG_PASSPHRASE%"=="" (
    echo [ERROR] Failed to read GPG passphrase from settings.xml ^(server id: xdb-gpg-passphrase^)
    popd
    exit /b 1
)

if /i "%MODE%"=="test" (
    echo [DRY RUN] build + sign to local .m2 only, NO upload
    call mvn clean install -DskipTests
) else (
    echo [PUBLISH] deploy to Maven Central ^(autoPublish^)
    call mvn clean deploy -DskipTests
)
set RESULT=%ERRORLEVEL%

popd
if %RESULT% neq 0 (
    echo [ERROR] Failed with code %RESULT%
    exit /b %RESULT%
)
if /i "%MODE%"=="test" (
    echo [OK] Dry run success. Artifacts + .asc signatures are in your local .m2, nothing was uploaded.
) else (
    echo [OK] Published to Maven Central. Check https://central.sonatype.com/publishing/deployments
)
