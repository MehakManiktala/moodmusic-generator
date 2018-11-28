/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicgenerator;

import java.util.ArrayList;
import java.util.Random;

import jm.JMC;
import jm.audio.Instrument;
import jm.audio.RTMixer;
import jm.music.data.Note;
import jm.music.rt.RTLine;
/**
 *
 * @author user
 */
public class RTMusicGenerator extends RTMixer {

    public static EmotionHandler Emotion = new EmotionHandler();

    static double[][][] map = {MidiHandler.Maj1,MidiHandler.Min2,MidiHandler.Min3,MidiHandler.Maj4,MidiHandler.Maj5,MidiHandler.Min6,MidiHandler.Dim7}; 

    static double[] ValMap = {JMC.QUAVER, JMC.CROTCHET, JMC.MINIM ,JMC.SEMIBREVE};
    
    public static RTLine[] initLines(Instrument[] instruments) {
    	
		ArrayList<RTLine> artlines = new ArrayList<RTLine>();		
		Random randnum =  new Random();
        MarkovChain NoteMC = new MarkovChain();
        MarkovChain OctMC = new MarkovChain();
        MarkovChain ValMC = new MarkovChain();
        MarkovChain ChordMC = new MarkovChain();
        @SuppressWarnings("unused")
		int transposer = randnum.nextInt(12) + 1;
        int index = 0;
        
        NoteMC.transition = map[randnum.nextInt(6)];
        NoteMC.states = 7;
        NoteMC.curState = 1;
        
        OctMC.transition = Emotion.OctMC;
        OctMC.states = 2;
        OctMC.curState = 1;
       
        ValMC.transition = Emotion.ValueMC;
        ValMC.states = 4;
        ValMC.curState = 1;
        
        ChordMC.transition = Emotion.ChordMC;
        ChordMC.states = 7;
        ChordMC.curState = 1;
        
        //MidiHandler Melody = new MidiHandler();
        //MidiHandler chordRoot = new MidiHandler();
        //MidiHandler chordMid = new MidiHandler();
        //MidiHandler chordDom = new MidiHandler();
        
		RTLine rootChordLine = new RTLine(instruments) {

			@Override
			public Note getNextNote() {
                if (index % 8 == 0)
                    ChordMC.nextState();
                Note cRoot = new Note(0, JMC.QN);
                cRoot.setPitch(Emotion.emoMap[ChordMC.curState - 1] - 12);
                ValMC.nextState();
                cRoot.setLength(ValMap[ValMC.curState - 1]);
                System.out.println("cDom pitch: " + cRoot.getPitch());
				return cRoot;
			}
			
		};
		artlines.add(rootChordLine);
		/*
		RTLine melodyLine = new RTLine(instruments) {

			@Override
			public Note getNextNote() {
	            NoteMC.transition = map[ChordMC.curState - 1];
				return null;
			}


		};
		*/
		RTLine[] rtline = new RTLine[0];
    	return artlines.<RTLine>toArray(rtline);
    }

	public RTMusicGenerator(Instrument[] instruments) {
		
		super(initLines(instruments));
		Emotion.setPleasantness();
		
	}
}
