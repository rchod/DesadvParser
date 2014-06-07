import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;


public class Scroll {

	private static JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Scroll window = new Scroll();
					window.frame.setVisible(true);
					
					
					
				    JPanel middlePanel = new JPanel ();
				    middlePanel.setBorder ( new TitledBorder ( new EtchedBorder (), "Display Area" ) );

				    // create the middle panel components

				    JTextArea display = new JTextArea ( 16, 58 );
				    display.setEditable ( false ); // set textArea non-editable
				    JScrollPane scroll = new JScrollPane ( display );
				    scroll.setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

				    //Add Textarea in to middle panel
				    middlePanel.add ( scroll );

				    // My code
				    frame.add ( middlePanel );
				    frame.pack ();
				    frame.setLocationRelativeTo ( null );
				    frame.setVisible ( true );
					
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Scroll() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
