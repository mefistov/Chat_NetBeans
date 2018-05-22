
package Client;

import Client.Entites.User;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Vlad
 */
public class ChatWindow extends JFrame{
    ConnectionManager con;
    DefaultListModel<String> dlm;
    
    
    public ChatWindow() {
        con = ConnectionManager.getInstance();
        con.setServer(readIP(), 2018);
        con.setInterfece(this);
        con.submit("Nick" + readNick());
        dlm = new DefaultListModel<>();
        initComponents();
        setComponentExtras();

        new Thread(new Runnable() {
            @Override
            public void run() {
                con.serverListener();
            }
        }).start();
       addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e){
                con.submit("EXIT");
            }
        });
    }
    
     @SuppressWarnings("unchecked")
     private void initComponents() {
         fieldMsg = new JTextField();
         btnSubmit = new JButton();
         jScrollPane2 = new JScrollPane();
         areaMessages = new JTextArea();
         jScrollPane3 = new JScrollPane();
         jList1 = new JList();
         
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            
         fieldMsg.addKeyListener(new KeyAdapter(){
             public void actionPeromed(KeyEvent evt){
                 fieldMsgKeyPressed(evt);
             }
         });  
         btnSubmit.setText("Submit");
         btnSubmit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent evt) {
                 btnSubmitActionPerfomed(evt);
             }
         });
         areaMessages.setEditable(false);
         areaMessages.setColumns(20);
         areaMessages.setLineWrap(true);
         areaMessages.setRows(5);
         areaMessages.setToolTipText("");
         areaMessages.setWrapStyleWord(true);
         jScrollPane2.setViewportView(areaMessages);
         
         jList1.setModel(dlm);
         jList1.setFixedCellHeight(20);
         jScrollPane3.setViewportView(jList1);
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(fieldMsg)
                    .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                    .addComponent(btnSubmit, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
         layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)
                    .addComponent(jScrollPane3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldMsg, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSubmit))
                .addContainerGap())
        );
         pack();
     }
    private void btnSubmitActionPerfomed(ActionEvent evt) {
        con.submit(fieldMsg.getText());
        fieldMsg.setText("");
    }
    private void fieldMsgKeyPressed(KeyEvent evt){
        if(evt.getKeyCode() == KeyEvent.VK_ENTER){
            btnSubmitActionPerfomed(null);
        }
    }
        public static void main(String args[]){
            try{
                for(UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()){
                    if("Nimbus".equals(info.getName())){
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
            }
        }catch(ClassNotFoundException ex){
            Logger.getLogger(ChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(ChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            Logger.getLogger(ChatWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
            java.awt.EventQueue.invokeLater(new Runnable(){
                @Override
                public void run() {
                    new ChatWindow().setVisible(true);
                }
            });          
}
        private javax.swing.JTextArea areaMessages;
        private javax.swing.JButton btnSubmit;
        private javax.swing.JTextField fieldMsg;
        private javax.swing.JList jList1;
        private javax.swing.JScrollPane jScrollPane2;
        private javax.swing.JScrollPane jScrollPane3;
        
        public void addUser(User u){
            dlm.addElement(u.getNick());
        }
        public void addMessage(String s){
            areaMessages.append(s + "\n");
        }
        public void cleanList(){
            dlm.clear();
        }
        private void setComponentExtras() {
            DefaultCaret caret = (DefaultCaret)areaMessages.getCaret();
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            jList1.setFixedCellHeight(20);
            setLocationRelativeTo(null);
            fieldMsg.requestFocus();
        }
        private String readIP(){
            return JOptionPane.showInputDialog(null, "Enter the server IP ", "127.0.0.1");
        }
        private String readNick(){
            return JOptionPane.showInputDialog("Enter a user`s login ", "User");
        }
}