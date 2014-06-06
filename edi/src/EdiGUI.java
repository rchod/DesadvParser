import java.awt.EventQueue;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Rectangle;

import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JScrollPane;

import java.awt.ScrollPane;


public class EdiGUI {

	JFrame frame;
	private JLabel jlabel;
	private JPanel jp1 = new JPanel();
	private JPanel jp2 = new JPanel();
	private JFileChooser fileChooser = new JFileChooser();
	private JPanel jp3 = new JPanel();
	private JTextArea textArea = new JTextArea();
	private JButton btnNewButton_1 = new JButton("Lancer la vérification");
	private String result = "<html>";
	private File selectedFile;
	private ScrollPane scrollPane = new ScrollPane();;
	private String text;

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
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().add(jp1, BorderLayout.CENTER);
        jp1.setLayout(null);
        jp2.setLayout(null);
        
        
        textArea.setBounds(new Rectangle(10, 50, 670, 400));
        jp1.add(textArea);
        
        JButton btnNewButton = new JButton("Ouvrir un fichier");

        btnNewButton.addActionListener(new ActionListener() {
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
        
        btnNewButton.setBounds(250, 10, 180, 30);
        jp1.add(btnNewButton);
        btnNewButton_1.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent arg0) {
        		HashSet<String> errors = null;
        		result = "<html>";
        		scrollPane.removeAll();
        		System.out.println("Vérification en cours");
        		
        		PrintWriter writer = null;
				try {
					writer = new PrintWriter("src\\desadv.tmp", "UTF-8");
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
        		String[] s = {"src\\desadv.tmp"};

        		
        		try {
					  errors = edixml.main(s);
				        for (String s1 : errors) {
				        	result = result+"<span bgcolor='red'>"+s1+"</span><br>";
				        }
						JLabel lblNewLabe2 = new JLabel(result);        
				        scrollPane.add(lblNewLabe2);
				        
				        if(errors.size()==0)
				        	scrollPane.add(new JLabel("<html><span bgcolor='green'>Aucune erreure detectée</span>"));
					  
				}catch (Exception e) {
					// TODO Auto-generated catch block
					scrollPane.add(new JLabel("<html><span bgcolor='red'>"+e.getMessage().split(":")[1]+"</span>"));
				}
        		
        	}
        });
        btnNewButton_1.setBounds(10, 10, 170, 30);
        
        jp1.add(btnNewButton_1);
        
        scrollPane.setBounds(10, 450, 670, 200);
        jp1.add(scrollPane);
        
        JLabel lblNewLabe2 = new JLabel(result);        
        scrollPane.add(lblNewLabe2);
        
        
        
        
        
        
        
        
        
        
        
        
        
        /*
        jtp.addChangeListener(new ChangeListener() { //add the Listener

            public void stateChanged(ChangeEvent e) {

                System.out.println(""+jtp.getSelectedIndex());

                if(jtp.getSelectedIndex()==1) //Index starts at 0, so Index 2 = Tab3
                {
            		

                }
            }
        });
  */
        
	}
}
