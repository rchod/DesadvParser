import java.awt.EventQueue;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;


import java.awt.ScrollPane;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JMenu;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;


public class EdiGUI {

	JFrame frame;
	private JLabel jlabel;
	private JPanel jp1 = new JPanel();
	private JPanel jp2 = new JPanel();
	private JFileChooser fileChooser = new JFileChooser();
	private JPanel jp3 = new JPanel();
	private JTextPane textArea = new JTextPane();
	private String result = "<html>";
	private File selectedFile;
	private ScrollPane scrollPane = new ScrollPane();;
	private String text;
	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu mnLancerLaVrification = new JMenu("Ex\u00E9cution");
	private final JMenuItem mntmLancer = new JMenuItem("Ex\u00E9cuter");
	private final JMenu mnParamtrage = new JMenu("Param\u00E9trage");
	private final JMenuItem mntmStructureDunDesadv = new JMenuItem("Structure d'un DESADV");
	private final JMenuItem mntmQuestCeQuun = new JMenuItem("Qu'est ce qu'un DESADV?");
	private final JMenuItem mntmDgrouper = new JMenuItem("D\u00E9grouper");
	JCheckBoxMenuItem chckbxmntmIgnorerLesErreurs;
	JCheckBoxMenuItem chckbxmntmLectureSeulePour;

	/** 
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EdiGUI window = new EdiGUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public EdiGUI() {
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 700, 700);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		try {
			File iconFile = PathUtils.resolveEdiSrcPath("EDI-Logo.gif");
			if (iconFile.exists()) {
				Image icon = ImageIO.read(iconFile);
				if (icon != null) {
					frame.setIconImage(icon);
				}
			}
		} catch (Exception e) {
			// Icon file not found, continue without icon
		}
		frame.setTitle("DESADV Parser");
		
		frame.getContentPane().add(jp1, BorderLayout.CENTER);
        jp1.setLayout(null);
        jp2.setLayout(null);
        
        textArea.setBounds(new Rectangle(10, 10, 674, 423));
        JScrollPane sp = new JScrollPane(textArea);
        sp.setBounds(new Rectangle(10, 10, 674, 423));
        jp1.add( sp );        

        TextLineNumber tln = new TextLineNumber(textArea);
        sp.setRowHeaderView( tln );
        
        
        mntmLancer.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		HashSet<String> errors = null;
        		textArea.getHighlighter().removeAllHighlights();
        		result = "<html>";
        		scrollPane.removeAll();
        		System.out.println("V�rification en cours");
        		
        		PrintWriter writer = null;
				File tempFile = PathUtils.resolveEdiSrcPath("desadv.tmp");
				try {
					writer = new PrintWriter(tempFile, "UTF-8");
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		writer.println(textArea.getText());
        		writer.close();
        		
        		InputStreamReader inputReader = new InputStreamReader(System.in);
        		OutputStreamWriter outputWriter = new OutputStreamWriter(System.out);
				EDItoXML edixml = new EDItoXML(inputReader, outputWriter);
        		String[] s = {tempFile.getAbsolutePath()};

        		DefaultHighlighter.DefaultHighlightPainter highlightPainter = 
        		        new DefaultHighlighter.DefaultHighlightPainter(Color.RED);
        		
        		
        		try {

        			
					  errors =  (HashSet<String>) edixml.main(s).get(0);
				        
		                if(chckbxmntmIgnorerLesErreurs.isSelected()){
		                	System.out.println("++++++++++++ selected ++++++++++++++++++");
		                	Iterator<String> iterator = errors.iterator();
		                	 while (iterator.hasNext()){
		                		 String s1 = iterator.next();
		                		 if(s1.contains("Alphanumeric")){
		                			 errors.remove(s1);
		                		 }
		                	 }
		                }
					  
				        for (String s1: errors) {
				        	result = result+"<div style='color:red;width:100%' >"+s1+"</div><hr>";
				        	
				        	int line = getErrorLine(s1);
				        	int startPos = getPosHighlight(line-1)[0];
				        	int endPos = getPosHighlight(line-1)[1]; 
		        		    try {
								textArea.getHighlighter().addHighlight(startPos, endPos,highlightPainter);
							} catch (BadLocationException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}	
				        	
				        }		        
				        
						JLabel lblNewLabe2 = new JLabel(result);        
				        scrollPane.add(lblNewLabe2);
				        
				        if(errors.size()==0)
				        	scrollPane.add(new JLabel("<html><span bgcolor='green'>Aucune erreure detect�e</span>"));
					  
				}catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					if(e.getMessage().split(":").length>1)
						scrollPane.add(new JLabel("<html><span bgcolor='red'>"+e.getMessage().split(":")[1]+"</span>"));
					else
						scrollPane.add(new JLabel("<html><span bgcolor='red'>"+e.getMessage()+"</span>"));
				
				}
        		
        	}

			private int getErrorLine(String s) {
				// TODO Auto-generated method stub
				String regex = ".*\\[segment ([0-9]+)\\].*";
				Pattern p = Pattern.compile(regex);
			    Matcher m = p.matcher(s);
				if(m.find())
					return Integer.parseInt(m.group(1));
				return 0;
			}

			private int[] getPosHighlight(int line) {
				// TODO Auto-generated method stub
				String[] lines = textArea.getText().split(System.lineSeparator());
				
				
				int i=0;
				int pos = 0;
				while( i < lines.length){
					if(i==line) break;
					pos = pos+lines[i].length()+1;
					
					i++;
				}
				
				int[] s = {pos,pos+lines[i].length()};
				return s;
			}
        });
        
        scrollPane.setBounds(10, 439, 674, 192);
        jp1.add(scrollPane);
        
        JLabel lblNewLabe2 = new JLabel(result);
        lblNewLabe2.setBounds(10, 527, 666, 141);
        jp1.add(lblNewLabe2);
        
        frame.setJMenuBar(menuBar);
        
        JMenu mnNewMenu = new JMenu("Fichier");
        menuBar.add(mnNewMenu);
        
        JMenuItem mntmOuvrir = new JMenuItem("Ouvrir ...");
        mnNewMenu.add(mntmOuvrir);
        
        JMenu mnEdition = new JMenu("Edition");
        menuBar.add(mnEdition);
        mntmDgrouper.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent arg0) {
        		String desadv = textArea.getText();
        		textArea.setText(desadv.replaceAll("'"+System.lineSeparator(), "'").replaceAll("'", "'"+System.lineSeparator()));
        	}
        });
        
        mnEdition.add(mntmDgrouper);
        
         chckbxmntmLectureSeulePour = new JCheckBoxMenuItem("Lecture seule pour le document actuel");
        mnEdition.add(chckbxmntmLectureSeulePour);
        
        menuBar.add(mnParamtrage);
        
         chckbxmntmIgnorerLesErreurs = new JCheckBoxMenuItem("Ignorer les erreurs Alphanum\u00E9riques");
        mnParamtrage.add(chckbxmntmIgnorerLesErreurs);
        
        menuBar.add(mnLancerLaVrification);
        
        mnLancerLaVrification.add(mntmLancer);
        
        JMenu mnAide = new JMenu("Aide");
        menuBar.add(mnAide);
        
        mnAide.add(mntmQuestCeQuun);
        
        mnAide.add(mntmStructureDunDesadv);
        

        mntmOuvrir.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
                try {
                    // Open an input stream
                	fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                	int result = fileChooser.showOpenDialog(jp3);
                	if (result == JFileChooser.APPROVE_OPTION) {
                	    selectedFile = fileChooser.getSelectedFile();
                	    System.out.println(selectedFile.getAbsolutePath());
                	    text = new Scanner(selectedFile).useDelimiter("\\A").next();
                	    textArea.setText(text);
                	    
                	}
                	
                	
                }catch (Exception e1) {
        			e1.printStackTrace();
        		}
        	}
        });
        
        chckbxmntmLectureSeulePour.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		
                try {
                	if(textArea.isEditable())
                		textArea.setEditable(false);
                	else
                		textArea.setEditable(true);
                	
                }catch (Exception e1) {
        			e1.printStackTrace();
        		}
        	}
        });
        

	}
}
