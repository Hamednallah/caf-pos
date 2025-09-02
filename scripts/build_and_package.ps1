mvn -DskipTests package
$jar = Get-ChildItem target -Filter "*-jar-with-dependencies.jar" | Select-Object -First 1
jpackage --type msi --name CafeteriaPOS --input $jar.DirectoryName --main-jar $jar.Name --win-shortcut --icon installer\icon.ico --dest dist
