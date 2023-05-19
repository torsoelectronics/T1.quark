T1 {
    *initClass{

    }

    *thisPackageName{
        ^'T1'
    }

    *path{
        ^Main.packages.asDict[this.thisPackageName]
    }

    *loadSynthDefs{
        load(this.path +/+ "synthdefs.scd")
    }
}
