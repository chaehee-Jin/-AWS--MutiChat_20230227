package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import dto.request.RequestDto;
import dto.response.ResponseDto;
import lombok.Getter;

@Getter
public class ConnectedSocket extends Thread {

	private static List<ConnectedSocket> connectedsocketList = new ArrayList<>();
	private Socket socket;
	private String username;

	private Gson gson;

	public ConnectedSocket(Socket socket) {
		this.socket = socket;
		gson = new Gson();
	}

	@Override
	public void run() {
		while (true) {
			BufferedReader bufferedReader;
			try {
				bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String requestJson = bufferedReader.readLine();
				
				System.out.println("요청:" + requestJson);
				requestMapping(requestJson);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	private void requestMapping(String requestJson) {
		RequestDto<?> requestDto = gson.fromJson(requestJson, RequestDto.class);

		switch (requestDto.getResource()) {
		case "uernameCheck":
			checkUsername((String)requestDto.getBody());
			break;
		}

	}
	//유저네임을 보내주면 유저네임을 검사

	private void checkUsername(String username) {
		if(username.isBlank()) {
			sendToMe(new ResponseDto<String>("usernameCheckIsBlank", "사용자 이름은 공백일 수 없습니다."));
			return;
		}
		for (ConnectedSocket connectedSocket : connectedsocketList) {
			if (connectedSocket.getUsername().equals(username)) {
				sendToMe(new ResponseDto<String>("usernameCheckIsDuplicate", "이미 사용중인 사용자입니다."));
				return;

			}
		}
		this.username = username;
		connectedsocketList.add(this);
		sendToMe(new ResponseDto<String>("usernameCheckSuccesssfully", null));
	}

	private void sendToMe(ResponseDto<?> responseDto) {

		try {
			OutputStream outputStream = socket.getOutputStream();
			PrintWriter printWriter = new PrintWriter(outputStream, true);

			String responseJson = gson.toJson(responseDto);
			printWriter.println(responseJson);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void sendToAll() {

	}
}
