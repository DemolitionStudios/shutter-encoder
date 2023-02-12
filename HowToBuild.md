# How to build an installable package

## Prerequisites

- Git
- JDK (14 or later, I'm using 17, for example)

## Build a custom runtime using `jlink`

Clone repository:
```shell
git clone https://github.com/paulpacifico/shutter-encoder.git
```
And then, run `jlink` from the project's root directory:
```shell
jlink --compress 0 --strip-debug --no-header-files --no-man-pages --add-modules java.base,java.datatransfer,java.desktop,java.logging,java.security.sasl,java.xml,jdk.crypto.ec --output JRE
```
Copy the `fonts` directory to the resulting `JRE/lib`.

Now you can run a fat jar right from the project's root directory using a custom runtime:
```shell
# for Linux/macOS
./JRE/bin/java -jar "Shutter Encoder.jar"
# for Windows
JRE\bin\java -jar "Shutter Encoder.jar"
```

## Build an installable package using `jpackage`

Create an empty directory (named `out`, for example) and copy `JRE/`, `Languages/`, `Shutter Encoder.jar` and the license file into it.
We also need to download all the software from [this list](Library/sources.txt). The easiest way is to download the distribution from the official website ([Windows distro](https://www.shutterencoder.com/Shutter%20Encoder%2016.8%20Windows%2064bits.zip)).

Note: application icon should be in `.ico` format.
Use `ImageMagick` to convert `icon.png` to `icon.ico`. Run the following command from the project's root directory:
```shell
convert -background transparent src/contents/icon.png icon.ico
```
and copy the resulting `.ico` into `out` directory.

### Windows

- Install .Net Framework - https://www.microsoft.com/en-in/download/confirmation.aspx?id=22.
- Install latest WiX Toolset v3.x - https://github.com/wixtoolset/wix3/releases.
- Run `jpackage`:
```shell
jpackage --type exe \ 
  --name "My Shutter Encoder" \ 
  --runtime-image JRE \ 
  --resource-dir Library --resource-dir Languages \ 
  --input . \ 
  --main-jar "Shutter Encoder.jar" \ 
  --license-file LICENSE.txt \ 
  --icon icon.ico \ 
  --app-version 7.0 \ 
  --win-menu --win-menu-group MyGroup \ 
  --win-shortcut-prompt \ 
  --win-dir-chooser 
```

That's it! Run `.exe` to install Shutter Encoder to your system.

### macOS

Simply run `jpackage`:
```shell
jpackage --type pkg \ 
  --name "My Shutter Encoder" \ 
  --runtime-image JRE \ 
  --resource-dir Library --resource-dir Languages \ 
  --input . \ 
  --main-jar "Shutter Encoder.jar" \ 
  --license-file LICENSE.txt \ 
  --icon icon.ico \ 
  --app-version 7.0 \ 
  --mac-package-identifier "My Shutter Encoder"
```

## Create installable package using script

**Prerequisites: Python 3**
```shell
git clone https://github.com/javacques/shutter-encoder.git
cd shutter-encoder
python3 build.py
```
