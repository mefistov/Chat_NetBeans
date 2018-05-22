
package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Vlad
 */
public class Server {
    public static final int PORT = 2018;
    public static ArrayList<Room> roomList;
    public static final String ADMIN_PASSWORD = "admin";
    public static boolean saveLogs = true;
    
    public static void main(String[] args) throws IOException{
    if(saveLogs) System.out.println("Saved logging enabled"); else System.out.println("Saved logging desabled");
    ServerSocket ss = new ServerSocket(PORT);
    Log.log("Server initialized in the port" + PORT);
    roomList = new ArrayList<>();
    Room room = new Room("Main room");
    addRoom(room);
    while(true){
        Socket socket = ss.accept();
        Log.log("Connection established with " + socket.getInetAddress().getHostAddress());
        new Thread(new User(socket, room)).start();
    }
    }
    public static void addRoom(Room room){
    if(getRoom(room.getName()) == null){
        roomList.add(room);
        Log.log("The "+ room.getName() + " room have been created");
    }
    }
    public static void deleteRoom(Room room){
    if(getRoom(room.getName()) != null && !room.getName().equalsIgnoreCase("Main room")){
        room.moveToRoom(roomList.get(0));
        roomList.remove(room);
        Log.log("Room " + room.getName() + " have been deleted");
    }
    }
    public static Room getRoom(String name){
    for(Room room : roomList){
        if(room.getName().equalsIgnoreCase(name)){
            return room;
        }
    }
    return null;
    }
    public static Room[] getRooms(){
        Room[] rooms = new Room[roomList.size()];
        for(int i = 0; i < roomList.size(); i++)
            rooms[i] = roomList.get(i);
        
        return rooms;
    }
    public static boolean roomExists(Room room){
        for(int i =0; i < roomList.size(); i++){
            if(roomList.get(i).getName().equalsIgnoreCase(room.getName())){
                return true;
            }
        }
        return false;
    }   

    
}
