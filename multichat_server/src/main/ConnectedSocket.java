package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import dto.request.RequestDto;
import dto.response.ResponseDto;
import entity.Room;
import lombok.Getter;

@Getter
public class ConnectedSocket extends Thread {

	private static List<ConnectedSocket> connectedsocketList = new ArrayList<>();
	private static List<Room> roomList = new ArrayList<>();
	private static int index = 0;
	private Socket socket;
	private String username;

	private Gson gson;

	// 생성자
	public ConnectedSocket(Socket socket) {
		this.socket = socket;
		gson = new Gson();
		// 인덱스가 하나씩 올라가면 방이하나씩 추가됨
		Room room = new Room("TestRoom" + index, "testUser" + index);
		index++;
		roomList.add(room);
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
		case "usernameCheck":
			checkUsername((String) requestDto.getBody());
			break;
		case "createRoom":
			Room room = new Room((String) requestDto.getBody(), username);
			room.getUsers().add(this);
			roomList.add(room);
			ResponseDto<String> responseDto = new ResponseDto<String>("createRoomSuccessfully", null);
			sendToMe(responseDto);
			refreshUsernameList(username);
			sendToAll(refreshRoomList(), connectedsocketList);
			
			break;

		}

	}
	// 유저네임을 보내주면 유저네임을 검사

	private void checkUsername(String username) {
		if (username.isBlank()) {
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
		sendToMe(new ResponseDto<String>("usernameCheckSuccessfully", null));
		sendToMe(refreshRoomList());

	}

	private ResponseDto<List<Map<String, String>>> refreshRoomList() {
		// 반복을 돌리면서 룸의 이름들을 담아야함
		List<Map<String, String>> roomNameList = new ArrayList<>();

		for (Room room : roomList) {
			Map<String, String> roomInfo = new HashMap<>();
			roomInfo.put("roomName", room.getRoomName());
			roomInfo.put("owner", room.getOwner());
			roomNameList.add(roomInfo);

		}
		ResponseDto<List<Map<String, String>>> responseDto = new ResponseDto<List<Map<String, String>>>(
				"refreshRoomList", roomNameList);
		return responseDto;

	}

	private Room findconnectedRoom(String username) {
		for (Room r : roomList) {
			for (ConnectedSocket cs : r.getUsers()) {
				if (cs.getUsername().equals(username)) {
					return r;

				}
			}

		}
		return null;

	}
	private Room findRoom (Map<String, String> roomInfo) {
		for (Room room : roomList) {
			if(room.getRoomName().equals(roomInfo.get("romName"))&& room.getOwner().equals(roomInfo.get("owner"))) {
				return room;
			}
			
		}
		return null;
	}

	private void refreshUsernameList(String username) {
		Room room  = findconnectedRoom(username);
		List<String> usernameList=  new ArrayList<>();
		usernameList.add("방제목:"+ room.getRoomName());
		for(ConnectedSocket connectedSocket : room.getUsers()) {
			if(connectedSocket.getUsername().equals(room.getOwner())) {
				usernameList.add(connectedSocket.getUsername()+ "(방장)");
				continue;
			}
			usernameList.add(connectedSocket.getUsername());
	}
	ResponseDto<List<String>> responseDto = new ResponseDto<List<String>>("refreshUsernameList", usernameList);
	sendToAll(responseDto, room.getUsers());
	
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

	private void sendToAll(ResponseDto<?> responseDto, List<ConnectedSocket> connectedSockets) {
		for (ConnectedSocket connectedSocket : connectedSockets) {
			try {
				OutputStream outputStream = connectedSocket.getSocket().getOutputStream();
				PrintWriter printWriter = new PrintWriter(outputStream, true);

				String responseJson = gson.toJson(responseDto);
				printWriter.println(responseJson);

			} catch (IOException e) {
				e.printStackTrace();

			}
		}
	}
}