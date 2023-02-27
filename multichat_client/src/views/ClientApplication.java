package views;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.gson.Gson;

import dto.request.RequestDto;

import java.awt.CardLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ClientApplication extends JFrame {

	private static final long serialVersionUID = 1L;

	private Gson gson;
	private Socket socket;

	private JPanel mainPanel;
	private CardLayout mainCard;

	private JTextField usernameField;

	private JTextField sendMessageField;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ClientApplication frame = new ClientApplication();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ClientApplication() {
		/* ============<<init >>=========== */
		gson = new Gson();
		try {
			socket = new Socket("127.0.0.1", 9090);
			ClientRecive clientRecive = new ClientRecive(socket);
			clientRecive.start();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(this, "서버에 접속될 수 없습니다", "접속오류", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();

		} 

		/* ============<<frame set>>=========== */

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(600, 150, 480, 800);
		/* ============<<Panel>>=========== */

		mainPanel = new JPanel();
		JPanel loginPanel = new JPanel();
		JPanel roomListPanel = new JPanel();
		JPanel roomPanel = new JPanel();

		/* ============<<layout >>=========== */
		mainCard = new CardLayout(0, 0);

		mainPanel.setLayout(mainCard);
		loginPanel.setLayout(null);
		roomListPanel.setLayout(null);
		roomPanel.setLayout(null);

		/* ============<<Panel set>>=========== */
		setContentPane(mainPanel);

		mainPanel.add(loginPanel, "loginPanel");
		mainPanel.add(roomListPanel, "roomListPanel");
		mainPanel.add(roomPanel, "roomPanel");

		/* ============<<login Panel>>=========== */
		JButton enterButton = new JButton("접속하기");

		usernameField = new JTextField();
		usernameField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					enterButton.doClick();
				}
			}
		});
		usernameField.setBounds(66, 522, 287, 46);
		loginPanel.add(usernameField);
		usernameField.setColumns(10);

		enterButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				RequestDto<String> usernameCheckReqDto = new RequestDto<String>("usernameCheck",
						usernameField.getText());
				sendRequest(usernameCheckReqDto);

			}
		});
		enterButton.setBounds(66, 591, 287, 46);
		loginPanel.add(enterButton);

		/* ============<<roomList Panel>>=========== */

		JScrollPane roomListScroll = new JScrollPane();
		roomListScroll.setBounds(97, 0, 357, 751);
		roomListPanel.add(roomListScroll);

		JList roomList = new JList();
		roomListScroll.setViewportView(roomList);

		JButton createButton = new JButton("방생성");
		createButton.setBounds(12, 10, 73, 71);
		roomListPanel.add(createButton);

		/* ===========<<room Panel>>=========== */
		JScrollPane joinUserListScroll = new JScrollPane();
		joinUserListScroll.setBounds(0, 0, 357, 94);
		roomPanel.add(joinUserListScroll);

		JList joinUserList = new JList();
		joinUserListScroll.setViewportView(joinUserList);

		JButton roomExitButton = new JButton("나가기");
		roomExitButton.setBounds(357, 0, 97, 94);
		roomPanel.add(roomExitButton);

		JScrollPane chattingContentScroll = new JScrollPane();
		chattingContentScroll.setBounds(0, 94, 454, 599);
		roomPanel.add(chattingContentScroll);

		JTextArea chattingContent = new JTextArea();
		chattingContentScroll.setViewportView(chattingContent);

		sendMessageField = new JTextField();
		sendMessageField.setBounds(0, 691, 367, 60);
		roomPanel.add(sendMessageField);
		sendMessageField.setColumns(10);

		JButton sendButton = new JButton("전송");
		sendButton.setBounds(367, 691, 87, 60);
		roomPanel.add(sendButton);

	}

	private void sendRequest(RequestDto<?> requestDto) {
		String reqJson = gson.toJson(requestDto);
		OutputStream outputStream = null;
		PrintWriter printWriter = null;

		try {
			outputStream = socket.getOutputStream();
			printWriter = new PrintWriter(socket.getOutputStream(), true);
			printWriter.println(reqJson);
			System.out.println("클라이언트 -> 서버:" + reqJson);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}