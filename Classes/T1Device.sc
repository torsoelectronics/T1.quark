/*

         88
  ,d     88
  88     88
MM88MMM  88  ,adPPYba,   ,adPPYba,
  88     88  I8[    ""  a8"     "8a
  88     88   `"Y8ba,   8b       d8
  88,    88  aa    ]8I  "8a,   ,a8"
  "Y888  88  `"YbbdP"'   `"YbbdP"'
         88

Copyright Torso Electronics 2022
All rights reserved.

*-0-*-0-*-0-*-0-*-0-*-0-*-0-*-0-*-0-*

T1Device.sc

*/

/*
*
* This class is a convenient interface for connecting to a T1 device and registering responder functions to react to it's midi messages
*
*/
T1Device{
    const numTracks = 16;
    var <fixed;

    // Global dispatchers - these contain the MIDIFuncs responsible for receiving midi in SC
    var <stopDispatcher, <startDispatcher;

    // Local dispatchers - each track object contains responders local to that track
    var <tracks;

    // Used to hack MIDIIn to say whether our controller is connected or not
    var connectMethod = 'isT1Connected';

    *start{|debugMode=false, permanent=true|
        ^super.new.init(debugMode, permanent)
    }

    init{|debugMode, permanent|
        fixed = permanent;
        this.connect();

        tracks = numTracks.collect{|midiChannel|
            T1Track.new(midiChannel: midiChannel, debugMode: debugMode, permanent: permanent);
        };

        // Set default responders if necessary
        if(debugMode, {
            "%: Using default responder functions".format(this.controllerName).postln;
            this.registerDefaultResponderFuncs()
        });

        this.postWelcome();
    }

    registerDefaultResponderFuncs{
        this.setStartFunc(
            {|val, num, chan|
                "%: Start message received: val %".format(this.controllerName, val).postln
            }
        );

        this.setStopFunc(
            {|val, num, chan|
                "%: Stop message received: val ".format(this.controllerName, val).postln
            }
        );
    }

    controllerName{
        ^"T-1"
    }

    postWelcome{
var text = [
"         88" ,
"  ,d     88" ,
"  88     88" ,
"MM88MMM  88  ,adPPYba,   ,adPPYba," ,
"  88     88  I8[    \"\"  a8\"     \"8a" ,
"  88     88   `\"Y8ba,   8b       d8" ,
"  88,    88  aa    ]8I  \"8a,   ,a8\"" ,
"  \"Y888  88  `\"YbbdP\"'   `\"YbbdP\"'" ,
"         88"];

text.do{|line|
    line.postln;
}

    }
    // An overengineered way of connecting only this controller to SuperCollider. Much faster (at least on Linux) than connecting all.
    connect{
        // Connect midi controller
        if(MIDIClient.initialized.not, {
            "MIDIClient not initialized... initializing now".postln;
            MIDIClient.init;
        });

        // This ratsnest connects only this controller and not all, which is much faster than the latter.
        MIDIClient.sources.do{|src, srcNum|
            if(src.device == this.controllerName.asString, {
                if(try{MIDIIn.connectMethod}.isNil, {
                    var isSource = MIDIClient.sources.any({|e|
                        e.device==this.controllerName.asString
                    });

                    if(isSource, {
                        "Connecting %".format(this.controllerName).postln;

                        MIDIIn
                        .connect(srcNum, src)
                        .addUniqueMethod(connectMethod, {
                            true
                        });

                        "Connected %... ".format(this.controllerName).postln;
                    });
                }, {
                    "% is already connected... (device is busy)".format(
                        this.controllerName
                    ).warn
                });
            });
        };
    }

    //------------------------------------------------------------------//
    //                    Global responder functions                    //
    //------------------------------------------------------------------//

    setStartFunc{|newFunc|
        if(startDispatcher.isNil.not, {
            startDispatcher.free();
        });

        startDispatcher = MIDIFunc.start(func:newFunc);

        // Persist between Cmd periods
        if(fixed, {
            startDispatcher.permanent();
        })
    }

    setStopFunc{|newFunc|
        if(stopDispatcher.isNil.not, {
            stopDispatcher.free();
        });

        stopDispatcher = MIDIFunc.stop(func:newFunc);

        // Persist between Cmd periods
        if(fixed, {
            stopDispatcher.permanent();
        })
    }

    //------------------------------------------------------------------//
    //                      Track local responders                      //
    //------------------------------------------------------------------//

    setNoteOnFunc{|trackNum, newFunc|
        tracks[trackNum].setNoteOnFunc(newFunc);
    }

    setNoteOffFunc{|trackNum, newFunc|
        tracks[trackNum].setNoteOffFunc(newFunc);
    }

    setCCResponderFunc{|trackNum, knobName, newFunc|
        tracks[trackNum].setCCResponderFunc(knobName, newFunc);
    }

}
