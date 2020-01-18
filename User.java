import java.sql.Date;import java.sql.Time;
class User {private String unitid, studid, name;private Date date;private Time timein, timeout;private int bookno;
public User (String unitid,String studid, String name, Date date, Time timein, Time timeout, int bookno){
this.unitid=unitid;this.studid=studid;this.name=name;this.date=date;this.timein=timein;this.timeout=timeout;this.bookno=bookno;}
public String getunitid(){return unitid;}public String getstudid(){return studid;}
public String getname(){return name;}public Date getdate(){    return date;}public Time gettimein(){    return timein;}
public Time gettimeout(){    return timeout;}public int getbookno(){return bookno;}}
