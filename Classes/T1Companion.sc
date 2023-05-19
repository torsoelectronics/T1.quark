T1Companion {
    var <synthMachine;
    var <setups;

    *start{|setupName=\default|
        ^super.new.init(setupName);
    }

    availableSetups{
        ^setups.keys.asArray;
    }

    init{|setupName|
        setups = Dictionary.new;

        Server.local.doWhenBooted{

            this.loadSetupFiles();
            Server.local.sync;

            T1.loadSynthDefs();
            Server.local.sync;

            synthMachine = T1SynthMachine.start();
            Server.local.sync;

            this.loadSetup(setupName);
            Server.local.sync;
        }
    }

    loadSetupFiles{
        PathName(T1.path +/+ "setups").filesDo{|file|
            "%: Loading setup file %".format(this.class.name, file.fileName).postln;
            setups.put(file.fileNameWithoutExtension.asSymbol, file.fullPath.load);
        };
    }

    loadSetup{|setupName|
        var setupDict = setups[setupName];

        setupDict.notNil.if({
            setupDict.do{|voice|
                var synthName = voice[\synthName];
                var args = voice[\args];
                var midiChannel = voice[\midiChannel];
                "%: Mapping synth % to channel % with args %".format(this.class.name, synthName, midiChannel, args).postln;

                synthMachine.setSynthDefForChannel(
                    synthDefName: synthName,
                    midiChannel: midiChannel,
                    args: args
                );
            }
        }, {
            "setup % not found".format(setupName).error;
        })

    }

}
