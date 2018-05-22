/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client.Entites;

/**
 *
 * @author Vlad
 */
public class User {
    
    private String nick;
    private Room room;
    
    public User(String nick, Room room){
        this.nick = nick;
        this.room = room;
}
    
    public String getNick(){
        return nick;
    }
    public void setNick(String nick){
        this.nick = nick;
    }
    public Room getRoom(){
        return room;
    }
    public void setRoom(Room room){
        this.room = room; 
    }
}