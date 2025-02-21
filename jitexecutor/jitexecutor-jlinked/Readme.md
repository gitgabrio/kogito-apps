Using jLink


1. move to root directory
```bash
cd src/modules/jlinkModule
```
2. compiles module-info in the **out** directory
```bash
javac -d out module-info.java
```
3. compiles the class in the **out** directory
```bash
javac -d out --module-path out net/cardosi/jlink/HelloWorld.java
```
4.  execute compiled module/class the **out** directory
```bash
java --module-path out --module jlinkModule/net.cardosi.jlink.HelloWorld
```
5.  verify dependencies of **jlinkModule** inside **out** directory
```bash
jdeps --module-path out -s --module jlinkModule
```
6. create a custom jre in the **customjre** directory putting module **jlinkModule** from the **out** directory
```bash
jlink --module-path out --add-modules jlinkModule --output customjre
 ```
7. move to bin directory
```bash
cd src/modules/jlinkModule/customjre/bin
 ```
8. verify the modules included in the custom jre
```bash
./java --list-modules
 ```
9.  execute compiled module/class using the custom/embedded jre
```bash
./java --module jlinkModule/net.cardosi.jlink.HelloWorld
 ```

**Script creation**
6. generate script inside our customjre_scripts/bin directory.
```bash
jlink --launcher customjrelauncher=jlinkModule/net.cardosi.jlink.HelloWorld \
   --module-path out \
   --add-modules jlinkModule \
   --output customjre_scripts
```
7. move to bin directory
```bash
cd src/modules/jlinkModule/customjre_scripts/bin
```
7. execute the script
```bash
./customjrelauncher
```
