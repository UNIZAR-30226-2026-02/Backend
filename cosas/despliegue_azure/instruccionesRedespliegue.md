# INSTRUCCIONES PARA VOLVER A SUBIR LA APP A AZURE EN CASO DE QUE HAYA CAMBIOS

**MUY IMPORTANTE**: estar en la ruta raiz del proyecto de SpringBoot por ejemplo, `C:\Users\lidia\OneDrive\Documentos\GitHub\PS\Backend\codenames`. Esto es por si las github actions fallan. En principio estará automatizado. 

1. *COMPILACIÓN DEL NUEVO CÓDIGO*: se vuelve a compilar y generar el archivo.jar con el comando `mvn clean package -DskipTests` (no tocar el nombre por defecto del archivo .jar porque es el que espera luego azure).

2. *SUBIR EL NUEVO .JAR A AZURE*: ejecutar el comando `az webapp deploy --resource-group codenamesRG --name CodenamesWeb --src-path target/codenames-0.0.1-SNAPSHOT.jar --type jar`. Este paso si en diez minutos no ha dado "éxito" se abortara solo, de normal le cuesta unos 3-5 mins como mucho.

3. *REINICIAR EL SERVIDOR*: ejecutar el comando `az webapp restart --resource-group codenamesRG --name CodenamesWeb`. Para verificar que este paso sale bien se pueden monitorizar los logs desde otra terminal usando el comando: `az webapp log tail --resource-group codenamesRG --name CodenamesWeb`. Los logs llevan cierto retraso respecto a lo que sucede en la realidad, un decalaje de unos 2-3 minutos.
