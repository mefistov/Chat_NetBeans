package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import Server.Server;
import java.util.Random;

/**
 *
 * @author Vlad
 */
public class User implements Runnable {

    private String nick;
    private BufferedReader reader;
    private BufferedWriter writer;
    private long logingTime;
    private boolean conected,
            superUser,
            ticOn;
    private String IP;
    private long ping;
    private Room room;
    private long lastTic;

    public User(String nick) {
        this.nick = nick;
    }

    public User(Socket socket, Room room) throws IOException {
        this.room = room;
        this.logingTime = logingTime;
        this.IP = socket.getInetAddress().getHostAddress();
        this.ping = 0;
        this.superUser = false;
        this.ticOn = true;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new BufferedWriter(new PrintWriter(socket.getOutputStream()));
    }

    @Override
    public void run() {
        String login = recive();
        if (!login.startsWith("NICK")) {
            submit("400 Invalid package received");
            Log.log("Invalid login package: " + login);
            conected = false;
        } else if (login.split("[ ]")[1].length() >= 12) {
            submit("400 The chosen nick is too long, enter a nick of at most 12 characters");
            Log.log("A user has tried to enter with a nickname too long." + login.split("[ ]")[1]);
        } else {
            conected = !room.userExists(this);
        }
        if (conected) {
            nick = login.split("[ ]")[1];
            submit(room.logIn(this));
            sendToUsersList();

            if (ticOn) {
                lastTic = System.currentTimeMillis();
                asyncTicCheck();
            }
            submit("ROOM" + room.getName());

            do {
                String packeg = recive();

                if (packeg != null && !packeg.isEmpty()) {
                    analyzePackeg(packeg);

                }
            } while (conected);
            submit("400 You have been disconnected from the chat");
            room.logOut(this);
        }
    }

    public void analyzePackeg(String s) {
        if (s.startsWith("EXIT")) {
            conected = false;
        } else if (s.startsWith("TIC")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                submit("500 Incorrect syntax");
                Log.log("Invalid packeg; " + s);
            } else {
                lastTic = System.currentTimeMillis();
                ping = System.currentTimeMillis() - Long.parseLong(p[1]);
            }
        } else if (s.startsWith("/NICK ")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                submit("500 Incorrect syntax");
                Log.log("Invalid packeg; " + s);
            } else {
                String havenName = nick;
                if (room.userExists(new User(p[1]))) {
                    submit("There is already a user called " + nick + " in the room.");
                    nick = havenName;
                } else if (!(p[1].length() > 12)) {
                    nick = p[1];
                    room.updateUsersList();
                    Log.log(havenName + " has changed its name to " + nick);
                    room.broadcast(havenName + " has changed its name to " + nick);
                    submit("200 OK");
                } else {
                    submit("500 The nick chosen is too long. Max 12 characters");
                }
            }
        } else if (s.startsWith("/WHO ")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                submit("500 Incorect syntax");
                Log.log("Invalid packeg: " + s);
            } else if (!room.userExists(new User(p[1]))) {
                submit("500 There is no user with the name" + p[1]);
            } else {
                User temporary = room.getUser(p[1]);
                submit("======================\nName: " + temporary.getNick() + "\nIP: " + temporary.getIP() + "\nPing: " + temporary.getPing() + "ms\nEntered: " + new Date(temporary.getLogingTime()).toGMTString() + "======================");
                Log.log("WHO requests from " + nick + " on " + temporary.getNick());
            }
        } else if (s.startsWith("/HELP ")) {
            String[] p;
            p = s.split("[ ]");
            submit("====================\nCommands\n========================\n- /WHO <usr>: Shows user information");
            submit("- /P <usr> <msg>: Send a private message to a user in the room\n- /NICK <new>: Change your username");
            submit("- /C <name> [PW]: Create a new room and put it in it. Optional password.\n- /J <name> [PW]: Switch to the specified room");
            submit("- /LIST: Lists of the rooms available on the server\n- /DATED: Generates a random number between 1 and 6\n- EXIT: Exits the chat\n======================");
        } else if (s.startsWith("/P")) {
            String[] p;
            p = s.split("[ ]");
            if (!room.userExists(new User(p[1]))) {
                submit("500 There is no user with the name" + p[1]);
            } else {
                User temporary = room.getUser(p[1]);
                room.sendPrivateMessage(this, temporary, s.substring(3 + temporary.getNick().length() + 1));
                Log.log("Private message of " + this.getNick() + " and " + temporary.getNick() + ": " + s.substring(3 + temporary.getNick().length()));
            }
        } else if (s.startsWith("/EXUDE ")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                if (!p[1].equals("admin")) {
                    submit("500 The password for EXUDE privileges is incorrect");
                } else {
                    superUser = true;
                    submit("Admin  privileges have been obtained");
                    Log.log("Admin  privileges have been obtained by" + nick);
                }
            }

        } else if (s.startsWith("/KICK ")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                submit("500 Incorect syntax");
                Log.log("Invalid packeg: " + s);
            } else if (!superUser) {
                submit("500 Insufficient permits");
                Log.log("Attempt to use KICK command without admin privileges: " + nick);
            } else if (!room.userExists(new User(p[1]))) {
                submit("500 There is no user with the name" + p[1]);
            } else {
                User temporary = room.getUser(p[1]);
                temporary.conected = false;
                room.broadcast(nick + " has thrown out " + temporary.getNick() + " from the room.");
                temporary.submit("400 You have been expelled from the chat by " + nick);
                Log.log(nick + " has thrown out " + temporary.getNick() + " from the room.");
            }
        } else if (s.startsWith("/BAN ")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                submit("500 Incorect syntax");
                Log.log("Invalid packeg: " + s);
            } else if (!superUser) {
                submit("500 Insufficient permits");
                Log.log("Attempt to use Ban command without admin privileges: " + nick);
            } else if (!room.userExists(new User(p[1]))) {
                submit("500 There is no user with the name" + p[1]);
            } else {
                User temporary = room.getUser(p[1]);
                temporary.conected = false;
                room.addBann(temporary);
                room.broadcast(nick + " has banned " + temporary.getNick() + " in this room");
                temporary.submit("400 You have been expelled from the chat by" + nick);
                Log.log(nick + " have banned " + temporary.getNick());
            }
        } else if (s.startsWith("/UNBAN ")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                submit("500 Incorect syntax");
                Log.log("Invalid packeg: " + s);
            } else if (!superUser) {
                submit("500 Insufficient permits");
                Log.log("Attempt to use UnBan command without admin privileges: " + nick);
            } else if (!room.isBanned(new User(p[1]))) {
                submit("500 The user" + p[1] + " is not banned");
            } else {
                room.removeBann(p[1]);
                Log.log(nick + " has removed the ban from " + p[1]);
            }
        } else if (s.startsWith("/C ")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                submit("500 Incorect syntax");
                Log.log("Invalid packeg: " + s);
            } else if (!Server.roomExists(new Room(p[1]))) {
                Room rm = null;
                if (p.length == 2) {
                    rm = new Room(p[1]);
                } else if (p.length == 3) {
                    rm = new Room(p[1], p[2]);
                }
                if (rm != null) {
                    Server.addRoom(rm);
                    room.logOut(this);
                    rm.logIn(this);
                    room = rm;
                    submit("ROOM " + room.getName());
                    room.updateUsersList();
                }
            } else {
                submit("500 There is already a room with that name");
            }
        } else if (s.startsWith("/J ")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                submit("500 Incorect syntax");
                Log.log("Invalid packeg: " + s);
            } else if (!Server.roomExists(new Room(p[1]))) {
                if (!Server.getRoom(p[1]).isBanned(this)) {
                    if (p.length == 2) {
                        Room rm = Server.getRoom(p[1]);
                        if (rm.hasPassword()) {
                            submit("500 This room requires a password");
                        } else {
                            room.logOut(this);
                            rm.logIn(this);
                            room = rm;
                            submit("ROOM" + room.getName());
                            room.updateUsersList();
                        }
                    } else if (p.length == 3) {
                        Room rm = Server.getRoom(p[1]);
                        if (!rm.getPassword().equalsIgnoreCase(p[2])) {
                            submit("500 Incorrect password");
                        } else {
                            room.logOut(this);
                            rm.logIn(this);
                            room = rm;
                            submit("ROOM" + room.getName());
                            room.updateUsersList();

                        }
                    }
                } else {
                    submit("500 You can not access the room " + p[1] + " because you are banned in it");
                }
            } else {
                submit("500 There is no room called" + p[1]);
            }
        } else if (s.startsWith("/D ")) {
            String[] p;
            p = s.split("[ ]");
            if (p.length > 2) {
                submit("500 Incorect syntax");
                Log.log("Invalid packeg: " + s);
            } else if (p.length == 2 && superUser) {
                if (p[1].equalsIgnoreCase("Admin")) {
                    submit("500 The main room can not be eliminated!");
                } else if (Server.roomExists(new Room(p[1]))) {
                    Room rm = Server.getRoom(p[1]);
                    rm.broadcast(nick + "has removed the room");
                    Server.deleteRoom(rm);
                } else {
                    submit("500 There is no room called " + p[1]);
                }
            }
            } else if (s.startsWith("/LIST ")) {
                Room[] rm = Server.getRooms();
                submit("========================");
                submit("Rooms available:" + rm.length);
                submit("========================");
                for (Room rm1 : rm) {
                    submit(rm1.getName() + " - Users: " + rm1.getUsersamount() + ((rm1.hasPassword()) ? " (with password)" : ""));
                }
                submit("========================");
            } else if (s.startsWith("/DICE ")) {
                int number = new Random().nextInt(6) + 1;
                room.broadcast(nick + " roll a dice and get a " + number);
            } else{ 
                if (s.length() < 400) {
                    room.broadcast(nick + " : " + s);
                    Log.log(" Received from" + nick + " message in the room " + room.getName() + ". Content: " + s);
                } else {
                    Log.log("Received too long message from " + nick);
            }
            }
        }
    public void sendToUsersList() {
        StringBuilder strb = new StringBuilder();
        strb.append("LIST ");
        for (User user : room.getUsers()) {
            strb.append(user.getNick());
            strb.append(" ");
        }
        submit(strb.toString());
    }

    public void submit(String s) {
        try {
            writer.write(s + "/n");
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String recive() {
        String s = "";
        try {
            s = reader.readLine();
        } catch (Exception ex) {
        }
        return s;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public long getLogingTime() {
        return logingTime;
    }

    public void setLogingTime(long logingTime) {
        this.logingTime = logingTime;
    }

    public String getIP() {
        return IP;
    }

    public void setIp(String IP) {
        this.IP = IP;
    }

    public long getPing() {
        return ping;
    }

    public void setPing(long ping) {
        this.ping = ping;
    }

    public boolean isSuperUser() {
        return superUser;
    }

    public boolean isConnected() {
        return conected;
    }

    public void setConection(Boolean conected) {
        this.conected = conected;
    }

    private void asyncTicCheck() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (conected) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                    if (System.currentTimeMillis() - lastTic >= 7000) {
                        submit("400 Disconnected due to inactivity");
                        conected = false;
                        Log.log(nick + " disconnected due to inactivity");
                    }
                }
            }

        }).start();
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
