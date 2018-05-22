package Server;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;

/**
 *
 * @author Vlad
 */
public class Room {

    private ArrayList<User> users;
    private String name;
    private String password;
    private ArrayList<User> bannedUsers;

    public Room(String name) {
        this.name = name;
        this.password = "";
        this.users = new ArrayList<>();
        this.bannedUsers = new ArrayList<>();
    }

    public Room(String name, String password) {
        this.name = name;
        this.password = password;
        this.users = new ArrayList<>();
        this.bannedUsers = new ArrayList<>();
    }
    public String logIn(User user){
        if (userExists(user)) {
                if (!isBanned(user)) {
                    user.setConection(true);
                    users.add(user);
                    broadcast(user.getNick() + " has entered the room " + this.name);
                    updateUsersList();
                    Log.log(user.getNick() + " has entered the room " + this.name);
                    user.submit("ROOM" + this.name);
                    return "200 OK";
                } else {
                    user.setConection(false);
                    return "400 You're banned from this room";
                }
        } else {
            user.setConection(false);
            return "400 User is already in  this room";
        } 
    }
    public String logIn(User user, String password) {
        if (userExists(user)) {
            if (password.equals(this.password)) {
                if (!isBanned(user)) {
                    user.setConection(true);
                    users.add(user);
                    broadcast(user.getNick() + " has entered the room " + this.name);
                    updateUsersList();
                    Log.log(user.getNick() + " has entered the room " + this.name);
                    user.submit("ROOM" + this.name);
                    return "200 OK";
                } else {
                    user.setConection(false);
                    return "400 You're banned from this room";
                }
            } else {
                return "400 The password of the room is incorrect";
            }
        } else {
            user.setConection(false);
            return "400 User is already in  this room";
        }
    }

    public boolean hasPassword() {
        return !this.password.isEmpty();
    }

    public void logOut(User user) {
        if (userExists(user)) {
            users.remove(user);
            broadcast(user.getNick() + " has left the room" + this.name);
            updateUsersList();
            Log.log(user.getNick() + " has left the room" + this.name);
        }
    }

    public boolean userExists(User user) {
        for (User usr : users) {
            if (usr.getNick().equalsIgnoreCase(user.getNick())) {
                return true;
            }
        }
        return false;
    }

    public boolean isBanned(User user) {
        for (User usr : users) {
            if (usr.getNick().equals(user.getNick()) || usr.getIP().equals(user.getIP())) {
                return true;
            }
        }
        return false;
    }

    public void broadcast(String msg) {
        for (User user : users) {
            user.submit(msg);
        }
    }

    public User getUser(String nick) {
        for (User user : users) {
            if (user.getNick().equals(nick)) {
                return user;
            }
        }
        return null;
    }

    public void addBann(User user) {
        bannedUsers.add(user);
    }

    public void removeBann(String nick) {
        for (int i = 0; i < bannedUsers.size(); i++) {
            if (bannedUsers.get(i).getNick().equals(nick)) {
                bannedUsers.remove(i);
                break;
            }
        }
    }
    public void sendPrivateMessage(User sender, User reciver, String message){
       sender.submit("(Private)" + sender.getNick() + ":" + message);
       reciver.submit("(Private)" + sender.getNick() + ":" + message);
    }
    public void moveToRoom(Room destination){
        try{
            for(int i = users.size()-1; i >=0; i--){
                users.get(0).setRoom(destination);
                destination.logOut(users.get(0));
                logOut(users.get(0));
            }
            destination.updateUsersList();
        }catch (ConcurrentModificationException ex){}
        finally{
            updateUsersList();
        }
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public int getUsersamount(){
        return users.size();
    }
    public ArrayList<User> getUsers(){
        return users;
    }
    public void updateUsersList(){
        for(User user : users){
            user.sendToUsersList();
        }
    }
}
