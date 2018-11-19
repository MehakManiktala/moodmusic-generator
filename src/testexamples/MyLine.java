package testexamples;

import inst.SimpleFMInst;
import jm.JMC;
import jm.music.data.*;
import jm.audio.*;
import jm.music.rt.*;

public final class MyLine extends RTLine implements JMC {
		
	public static void main(String[] args) {
		Instrument inst = new SimpleFMInst(44100, 800, 34.4);
		new MyLine(new Instrument[] {inst});
	}
	
	public MyLine(Instrument[] insts) {
		super(insts);
		RTMixer mixer = new RTMixer(new RTLine[] {this});
		mixer.begin();
	}

	public Note getNextNote() {
		Note n = new Note((int)(Math.random() * 12 + 60), QN);
		return n;
	}
}