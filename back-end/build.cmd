@echo off
setlocal enabledelayedexpansion

echo.
echo ===========================================
echo         BUILD SCRIPT - ContainerView
echo ===========================================
echo.

:: Verificar parametros
set CLEAN_BUILD=false
set SKIP_TESTS=false
set RUN_APP=false
set FORCE_LOMBOK=false

:parse_args
if "%1"=="" goto start_build
if /i "%1"=="clean" set CLEAN_BUILD=true
if /i "%1"=="skip-tests" set SKIP_TESTS=true
if /i "%1"=="run" set RUN_APP=true
if /i "%1"=="lombok" set FORCE_LOMBOK=true
shift
goto parse_args

:start_build

:: Verificar se Maven Wrapper existe
if not exist "mvnw.cmd" (
    echo ERRO: Maven Wrapper nao encontrado!
    echo Execute este script no diretorio back-end do projeto
    pause
    exit /b 1
)

:: Verificar Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ERRO: Java nao encontrado!
    echo Instale Java 17 ou superior
    pause
    exit /b 1
)

echo Java encontrado - OK
echo.

:: Configurar Lombok
echo Configurando Lombok...

:: Criar lombok.config se nao existir
if not exist "lombok.config" (
    echo Criando lombok.config...
    (
        echo # Configuracao do Lombok
        echo config.stopBubbling = true
        echo lombok.addLombokGeneratedAnnotation = true
        echo lombok.anyConstructor.addConstructorProperties = true
        echo lombok.log.fieldName = log
        echo lombok.log.fieldIsStatic = true
    ) > lombok.config
    echo lombok.config criado!
) else (
    echo lombok.config ja existe!
)

:: Verificar se o pom.xml tem a configuracao correta do Lombok
findstr /C:"annotationProcessorPaths" pom.xml >nul
if errorlevel 1 (
    echo.
    echo ============================================
    echo AVISO: pom.xml nao tem configuracao do Lombok!
    echo.
    echo Adicione esta configuracao no maven-compiler-plugin:
    echo.
    echo ^<annotationProcessorPaths^>
    echo   ^<path^>
    echo     ^<groupId^>org.projectlombok^</groupId^>
    echo     ^<artifactId^>lombok^</artifactId^>
    echo     ^<version^>1.18.34^</version^>
    echo   ^</path^>
    echo ^</annotationProcessorPaths^>
    echo.
    echo ============================================
    if "%FORCE_LOMBOK%"=="false" (
        echo.
        choice /C YN /M "Continuar mesmo assim (Y/N)?"
        if errorlevel 2 exit /b 1
    )
)

echo Iniciando build...
echo.

:: Limpeza se solicitada
if "%CLEAN_BUILD%"=="true" (
    echo Executando limpeza...
    call mvnw.cmd clean
    if errorlevel 1 (
        echo ERRO na limpeza!
        pause
        exit /b 1
    )
    echo Limpeza concluida!
    echo.
)

:: Forcar recompilacao do Lombok
echo Forcando processamento do Lombok...
call mvnw.cmd clean compile -Dmaven.compiler.forceJavacCompilerUse=true
if errorlevel 1 (
    echo.
    echo ============================================
    echo ERRO: Falha no processamento do Lombok!
    echo.
    echo Possiveis solucoes:
    echo 1. Verifique se o pom.xml tem annotationProcessorPaths
    echo 2. Execute: mvnw dependency:resolve
    echo 3. Limpe cache: mvnw dependency:purge-local-repository
    echo 4. Verifique versao do Lombok no pom.xml
    echo.
    echo ============================================
    pause
    exit /b 1
)
echo Lombok processado com sucesso!
echo.

:: Testes
if "%SKIP_TESTS%"=="false" (
    echo Executando testes...
    call mvnw.cmd test
    if errorlevel 1 (
        echo AVISO: Alguns testes falharam!
    ) else (
        echo Testes concluidos!
    )
    echo.
)

:: Empacotamento
echo Criando JAR...
if "%SKIP_TESTS%"=="true" (
    call mvnw.cmd package -DskipTests
) else (
    call mvnw.cmd package
)

if errorlevel 1 (
    echo ERRO no empacotamento!
    pause
    exit /b 1
)

:: Verificar JAR criado
for %%f in (target\containerView*.jar) do (
    if not "%%f"=="target\containerView*.jar" (
        set JAR_FILE=%%f
        echo JAR criado com sucesso: %%f
        goto jar_found
    )
)

echo ERRO: JAR nao encontrado!
pause
exit /b 1

:jar_found

:: Executar se solicitado
if "%RUN_APP%"=="true" (
    echo.
    echo Executando aplicacao...
    echo Aplicacao estara disponivel em: http://localhost:8080
    echo Health check: http://localhost:8080/actuator/health
    echo.
    echo Pressione Ctrl+C para parar a aplicacao
    echo.
    java -jar "!JAR_FILE!"
)

echo.
echo ===========================================
echo             BUILD CONCLUIDO!
echo ===========================================
echo.
echo Para executar a aplicacao:
echo   java -jar "!JAR_FILE!"
echo.
echo Opcoes disponiveis:
echo   build.cmd clean          - Limpar antes de compilar
echo   build.cmd skip-tests     - Pular testes
echo   build.cmd run            - Executar apos build
echo   build.cmd lombok         - Forcar configuracao Lombok
echo   build.cmd clean lombok   - Limpeza total com Lombok
echo.
pause