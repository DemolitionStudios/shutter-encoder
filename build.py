import subprocess
from urllib.parse import quote
from urllib.request import urlretrieve
from tempfile import mkdtemp
import os
import shutil
import platform


def build_jre(jre_dir):
    process = subprocess.Popen(['jlink',
                                '--compress', '0',
                                '--strip-debug',
                                '--no-header-files',
                                '--no-man-pages',
                                '--add-modules',
                                'java.base,java.datatransfer,java.desktop,java.logging,java.security.sasl,java.xml,jdk.crypto.ec',
                                '--output', jre_dir
                                ],
                               stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE)
    stdout, stderr = process.communicate()
    if stderr:
        raise Exception(stderr)
    return stdout


def build_win_package():
    return build_package('exe',
                         ['--win-shortcut-prompt',
                          '--win-dir-chooser',
                          '--win-menu',
                          '--win-menu-group', 'Shutter Encoder',
                          ])


def build_mac_package():
    return build_package('pkg', ['--mac-package-identifier', 'ShutterEncoder'])


def build_package(package_type, additional_opts):
    process = subprocess.Popen(['jpackage',
                                '--type', package_type,
                                '--name', 'Shutter Encoder',
                                '--runtime-image', 'JRE',
                                '--resource-dir', 'Library',
                                '--resource-dir', 'Languages',
                                '--input', '.',
                                '--main-jar', 'Shutter Encoder.jar',
                                '--license-file', 'LICENCE.txt',
                                '--icon', 'icon.ico',
                                '--app-version', '7.0',
                                ] + additional_opts,
                               stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE)
    stdout, stderr = process.communicate()
    if stderr:
        raise Exception(stderr)
    return stdout


win_package_name = "Shutter Encoder 16.8 Windows 64bits"


def download_shutter_encoder_package():
    url = "https://www.shutterencoder.com/" + quote(win_package_name + ".zip")
    temp_dir = mkdtemp()
    print("Downloading zip from " + url + " to " + temp_dir)
    filename, _ = urlretrieve(url, os.path.join(temp_dir, "shutterencoder.zip"))
    shutil.unpack_archive(filename, temp_dir)
    return os.path.join(temp_dir, win_package_name)


current_dir = os.curdir
output_dir = os.path.join(current_dir, "out")
if not os.path.exists(output_dir):
    os.mkdir(output_dir)
custom_jre_dir = os.path.join(output_dir, "JRE")
if not os.path.exists(custom_jre_dir):
    build_jre(custom_jre_dir)
# copy fonts to JRE
jre_fonts_dir = os.path.join(custom_jre_dir, "lib", "fonts")
if not os.path.exists(jre_fonts_dir):
    shutil.copytree(os.path.join(current_dir, "fonts"), jre_fonts_dir)
# copy Library
output_lib_dir = os.path.join(output_dir, "Library")
if not os.path.exists(output_lib_dir):
    se_package_dir = download_shutter_encoder_package()
    shutil.copytree(os.path.join(se_package_dir, "Library"), output_lib_dir)
# copy Languages
output_lang_dir = os.path.join(output_dir, "Languages")
if not os.path.exists(output_lang_dir):
    shutil.copytree(os.path.join(current_dir, "Languages"), output_lang_dir)
# copy "Shutter Encoder.jar"
output_jar_filename = os.path.join(output_dir, "Shutter Encoder.jar")
if not os.path.exists(output_jar_filename):
    shutil.copyfile(os.path.join(current_dir, "Shutter Encoder.jar"), output_jar_filename)
# copy LICENCE.txt
output_licence_filename = os.path.join(output_dir, "LICENCE.txt")
if not os.path.exists(output_licence_filename):
    shutil.copyfile(os.path.join(current_dir, "LICENCE.txt"), output_licence_filename)
# copy icon.ico
icon_filename = os.path.join(current_dir, "icon.ico")
output_icon_filename = os.path.join(output_dir, "icon.ico")
if os.path.exists(icon_filename) and not os.path.exists(output_icon_filename):
    shutil.copyfile(icon_filename, output_icon_filename)
os.chdir(output_dir)
system_name = platform.system()
if system_name == "Windows":
    build_win_package()
elif system_name == "Darwin":
    build_mac_package()
else:
    print(system_name + " not supported")
