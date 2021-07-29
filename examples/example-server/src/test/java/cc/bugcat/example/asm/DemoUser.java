package cc.bugcat.example.asm;


import java.util.Date;

public class DemoUser {
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
    public String getUser() {return user;}
    public void setUser(String user) {this.user = user;}
    public Date getDate() {return date;}
    public void setDate(Date date) {this.date = date;}
    private String name;
    private String user;
    private Date date;
    
    public Object invoke(int index){
        switch ( index ) {
            case 0: return name;
            case 1: return user;
            case 2: return date;
            default: return null;
        }
    }
}
