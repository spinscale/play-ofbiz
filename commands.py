# Here you can create play commands that are specific to the module, and extend existing commands
import subprocess
import os

MODULE = 'ofbiz'

# Commands that are specific to your module

COMMANDS = ['ofbiz:create', 'ofbiz:help']

def execute(**kargs):
    command = kargs.get("command")
    app = kargs.get("app")
    args = kargs.get("args")
    env = kargs.get("env")

    if command == "ofbiz:create":
        print "~ Compiling your entities"
        app.check()
        java_cmd = app.java_cmd(['-Xmx64m'], className='play.modules.ofbiz.EntityModelReader', args=['conf/ofbiz/ofbiz-entity/'])
        subprocess.call(java_cmd, env=os.environ)

    if command == "ofbiz:help":
        print "Usage:\n"
        print "       play ofbiz:create      - Creates objects from all entities in conf/ofbiz/ofbiz-entity/*.xml and puts them into app/models/"
        print "                              - WARNING: Overwrites existing entities"

# This will be executed before any command (new, run...)
#def before(**kargs):
#    command = kargs.get("command")
#    app = kargs.get("app")
#    args = kargs.get("args")
#    env = kargs.get("env")


# This will be executed after any command (new, run...)
#def after(**kargs):
#    command = kargs.get("command")
#    app = kargs.get("app")
#    args = kargs.get("args")
#    env = kargs.get("env")
#
#    if command == "new":
#        pass
