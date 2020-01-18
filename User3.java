import java.sql.Time;
class User3 {private String studid, name; private Time timein, timeout;  private int bookno;
public User3 (String studid, String name, Time timein, Time timeout, int bookno){
this.studid=studid;this.name=name;this.timein=timein;this.timeout=timeout;this.bookno=bookno;}
public String getstudid(){return studid;}public String getname(){return name;}
public Time gettimein(){return timein;}public Time gettimeout(){return timeout;}
public int getbookno(){return bookno;}}
