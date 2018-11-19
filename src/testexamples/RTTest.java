package testexamples;

import jm.audio.RTMixer;
import jm.audio.Instrument;
import jm.music.rt.RTLine;
import inst.PluckInst;
import jm.JMC;

public class RTTest implements JMC{
	public static void main(String[] args){
		int sampleRate = 44100;
		Instrument inst = new PluckInst(sampleRate);
		Instrument[] insts = new Instrument[] {inst};
		RTLine[] rtlines = {new RealtimeMelody(insts)};
		final RTMixer rtm = new RTMixer(rtlines);
		rtm.begin();
	}
}