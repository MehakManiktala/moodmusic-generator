package pipeline;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import inst.FractalInst;
import inst.SimpleFMInst;
import jm.audio.Instrument;
import jm.util.Play;
import musicgenerator.EmotionHandler;
import musicgenerator.MusicGenerator;
import musicgenerator.RTMusicGenerator;

@SuppressWarnings("serial")
public class SentencePlayer extends javax.swing.JFrame  {

	private static final String TERMINATION = "$T3RMN@+N$";
    private static SwingWorker<Void,String> worker;

	private static WatchService watcher;

	Instrument piano = new FractalInst(30);
	Instrument[] instruments = new Instrument[] {piano};
	
	MusicGenerator musicGen = new MusicGenerator();

	//UI Components
	private JTextArea textArea;
	private Highlighter highlighter;
	HighlightPainter painter;
	private StringBuilder inputString;
	private List<MoodSentence> sentenceBuffer = new LinkedList<MoodSentence>();
	private boolean terminationSwitch = false;

	public static enum Mood{
		Sad(1.5),
		Happy(.7),
		Surprised(.4),
		Neutral(1), Drowsy(2), Quiescent(1.2), Active(.5), Calm(1.3), Fear(0.6);
		
	   double multiplier;
	   Mood(double m) {
	      multiplier = m;
	   }
	}
	private static class MoodSentence{

		@SuppressWarnings("unused")
		public MoodSentence(String st, String mood, int start, int end) {

			this.sentence = st;

			mood = mood.toLowerCase();
			for (Mood m : Mood.values()) {
				if (mood.equals(m.name().toLowerCase())) {
					this.mood = m;
					break;
				}
			}
		}
		
		public MoodSentence(String st, Mood mood, int start, int end) {

			this.sentence = st;
			this.mood = mood;
		}

		public String sentence = "";
		public Mood mood = Mood.Neutral;
		public int startIndex = 0;
		public int endIndex = 0;
	}

	private void initComponents(String sentenceDir) {

		textArea = new JTextArea(10, 30);

		String text = "Waiting for sentences in "+sentenceDir;

		textArea.setText(text);

		highlighter = textArea.getHighlighter();
		painter = new DefaultHighlighter.DefaultHighlightPainter(Color.pink);

	}


	void SetEmotion(String mood) {
		//TODO
	}

	public static void main(String[]args) {
		System.out.println("Running");

		SentencePlayer player = new SentencePlayer();

		String path = null;
		if (args.length > 0) {
			if (args[0]!=null)
				path = args[0];
		}
		final String fpath = path!=null? path: System.getProperty("user.dir")+"\\moodinput";
		
		player.initComponents(fpath);

		
		Instrument inst = new SimpleFMInst(44100, 800, 34.4);
		final RTMusicGenerator mixer = new RTMusicGenerator(new Instrument[] {inst});
		mixer.begin();
		
		//File-change-watcher thread
		new SwingWorker<Void, String>(){
			@Override
			protected Void doInBackground() throws Exception {
				System.out.println("Attempting to watch directory "+fpath);
				try {
					watcher = FileSystems.getDefault().newWatchService();
					Path dir = Paths.get(fpath);
					WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);//, ENTRY_MODIFY);
					System.out.print("Watching directory "+dir.toAbsolutePath().toString());
					player.inputString = new StringBuilder();
					//put poller in new thread called mood-change reader
					while(player.terminationSwitch==false){

						for (WatchEvent<?> event: key.pollEvents()) {
							WatchEvent.Kind<?> kind = event.kind();
							if (kind == OVERFLOW) {
								continue;
							}
							@SuppressWarnings("unchecked")
							WatchEvent<Path> ev = (WatchEvent<Path>)event;
							Path filename = ev.context();
							File file = new File(filename.toAbsolutePath().toString()); 
							System.out.println("New changes spotted in "+file.getAbsolutePath());

							BufferedReader br;
							try {
								br = new BufferedReader(new FileReader(file));
								String st; 
								while ((st = br.readLine()) != null) {
									if (st.equals(TERMINATION)) {
										player.terminationSwitch = true;
										break;
									}
									int mood_index = new Random().nextInt(Mood.values().length);//"Neutral";//TODO parse mood
									Mood mood = Mood.values()[mood_index];
									System.out.println(mood.name());
									player.sentenceBuffer.add(new MoodSentence(st, mood, player.inputString.length(), player.inputString.length()+st.length()-1));
									player.inputString.append(st);
									System.out.println(st); 

									try {
										player.highlighter.addHighlight(0, player.inputString.length(), player.painter );
									} catch (BadLocationException e) {
										e.printStackTrace();
									}


								}
								player.textArea.setText(player.inputString.toString());


							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 

						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}
		}.execute();
		
		//UI thread
		new SwingWorker<Void, String>(){
			@Override
			protected Void doInBackground() throws Exception {
				JOptionPane.showMessageDialog(null, new JScrollPane(player.textArea));  
				return null;
			}
		}.execute();
		
		//Music player thread
		int pace_duration = 5;
		int next_index = 0;
		Mood starting_mood = Mood.Neutral;
		new SwingWorker<Void, String>(){
			@Override
			protected Void doInBackground() throws Exception {
				
				if (next_index >= player.sentenceBuffer.size()) {
					Thread.sleep(pace_duration);
				}
				else {
					MoodSentence next = player.sentenceBuffer.get(next_index);
					EmotionHandler emotion = player.musicGen.Emotion;
					switch(next.mood) {
					case Happy:
						emotion.setPleasantness();
						break;
					case Neutral:
						break;
					case Calm:
						emotion.setLowNegativeAffect();
						break;
					case Active:
						emotion.setHighPositiveAffect();
						break;
					case Quiescent:
						emotion.setDisengagement();
						break;
					case Surprised:
						emotion.setStrongEngagement();
						break;
					case Drowsy:
				        emotion.setLowPositiveAffect();
				        break;
					case Sad:
						emotion.setUnpleasantness();
						break;
					case Fear:
				        emotion.setHighNegativeAffect();
				        break;
					default:
						break;
					}
			        if(worker!=null) worker.cancel(true);
			        worker = new SwingWorker<Void, String>(){
			            @Override
			            protected Void doInBackground() throws Exception {
			            	player.musicGen.scr.empty();
			            	player.musicGen.Generate(120);
			                Play.midi(player.musicGen.scr);
			                return null;
			            }
			        };
					Thread.sleep(determineDuration(next));
				}
				

				
				return null;
			}
		}.execute();



	}
	static int determineDuration(MoodSentence sentence){
		
		int duration = sentence.sentence.length();
		duration*= sentence.mood.multiplier;
		return duration;
	}
}
