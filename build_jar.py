import subprocess
import os
import glob
import shutil


def build_jar():
    process = subprocess.Popen(['jar',
                                '-cfe', 'app.jar',
                                'application.Shutter', '.'
                                ],
                               stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE)
    stdout, stderr = process.communicate()
    if stderr:
        raise Exception(stderr)
    return stdout


current_dir = os.curdir
output_dir = os.path.join(current_dir, "out_jar")
output_dir_abs = os.path.abspath(output_dir)
current_dir_abs = os.path.abspath(output_dir)
if os.path.exists(output_dir):
    shutil.rmtree(output_dir)
os.mkdir(output_dir)
os.system(
    'javac -encoding utf8 -classpath "lib/*" -sourcepath src -d out_jar '
    'src/application/*.java src/functions/*.java src/library/*.java src/settings/*.java')
# copy jars content
for filename in glob.glob("lib/*.jar"):
    shutil.unpack_archive(filename, output_dir, "zip")
# copy resources
shutil.copytree(os.path.join(current_dir, "src", "contents"), os.path.join(output_dir, "contents"))
os.chdir(output_dir)
build_jar()
shutil.copyfile("app.jar", os.path.abspath(os.path.join(os.path.dirname(__file__), 'Shutter Encoder.jar')))
