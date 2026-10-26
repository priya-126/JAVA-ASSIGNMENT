import java.sql.*;
public class AcademicDAO {
    public void addCourse(String code,String title,int credits,String type) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO courses VALUES(?,?,?,?)")){
            p.setString(1,code);p.setString(2,title);p.setInt(3,credits);p.setString(4,type);p.executeUpdate();
        }
    }
    public void enroll(String roll,String code,int sem) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO enrollments(roll_no,course_code,semester) VALUES(?,?,?)")){
            p.setString(1,roll);p.setString(2,code);p.setInt(3,sem);p.executeUpdate();
        }
    }
    public void attendance(int id,int held,int attended,boolean approved) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE enrollments SET classes_held=?,classes_attended=?,condonation_approved=? WHERE id=?")){
            p.setInt(1,held);p.setInt(2,attended);p.setBoolean(3,approved);p.setInt(4,id);if(p.executeUpdate()==0)throw new SQLException("Enrollment not found");
        }
    }
    public void mark(int id,String field,Double value,boolean absent) throws SQLException {
        if(!field.equals("cia1")&&!field.equals("cia2")&&!field.equals("assignment_mark")&&!field.equals("ese"))throw new IllegalArgumentException("Invalid assessment");
        if(value!=null&&(value<0||value>100))throw new IllegalArgumentException("Marks must be 0-100");
        if(absent&&!field.equals("ese"))throw new IllegalArgumentException("Only ESE supports AB");
        try(Connection c=DBConnection.getConnection()){
            try(PreparedStatement p=c.prepareStatement("INSERT IGNORE INTO assessment_marks(enrollment_id) VALUES(?)")){p.setInt(1,id);p.executeUpdate();}
            String sql=field.equals("ese")?"UPDATE assessment_marks SET ese=?,ese_absent=? WHERE enrollment_id=?":"UPDATE assessment_marks SET "+field+"=? WHERE enrollment_id=?";
            try(PreparedStatement p=c.prepareStatement(sql)){
                if(value==null)p.setNull(1,Types.DECIMAL);else p.setDouble(1,value);
                if(field.equals("ese")){p.setBoolean(2,absent);p.setInt(3,id);}else p.setInt(2,id);
                p.executeUpdate();
            }
        }
    }
    static class Result {
        String grade,status;int points;Double cia,ese,total;
        Result(String grade,String status,int points,Double cia,Double ese,Double total){this.grade=grade;this.status=status;this.points=points;this.cia=cia;this.ese=ese;this.total=total;}
    }
    // CIA1, CIA2 and assignment are averaged equally: configurable project assumption.
    static Result evaluate(String type,int held,int attended,boolean approved,Double m1,Double m2,Double assignment,Double ese,boolean absent){
        double attendance=held==0?0:100.0*attended/held;
        if(attendance<65)return new Result("SA","BARRED",0,null,ese,null);
        if(attendance<75&&!approved)return new Result("PENDING","CONDONATION REQUIRED",0,null,ese,null);
        if(absent)return new Result("AB","ABSENT",0,null,null,null);
        if(m1==null||m2==null||assignment==null||ese==null)return new Result("PENDING","INCOMPLETE MARKS",0,null,ese,null);
        double cw=type.equals("THEORY")?0.4:type.equals("PRACTICAL")?0.6:0.5;
        double ew=1-cw;
        double cia=((m1+m2+assignment)/3.0)*cw;
        double external=ese*ew;
        double total=cia+external;
        double minEse=type.equals("PRACTICAL")?50:45;
        if(ese<minEse||total<50)return new Result("RA","FAIL",0,cia,external,total);
        // Grade bands interpreted by whole-number mark after rounding.
        long rounded=Math.round(total);
        String grade=rounded>=91?"O":rounded>=81?"A+":rounded>=71?"A":rounded>=61?"B+":"B";
        int gp=grade.equals("O")?10:grade.equals("A+")?9:grade.equals("A")?8:grade.equals("B+")?7:6;
        return new Result(grade,"PASS",gp,cia,external,total);
    }
    private static final String BASE="SELECT e.id,e.roll_no,e.semester,e.classes_held,e.classes_attended,e.condonation_approved,c.course_code,c.title,c.credits,c.course_type,m.cia1,m.cia2,m.assignment_mark,m.ese,COALESCE(m.ese_absent,FALSE) AS ese_absent FROM enrollments e JOIN courses c ON c.course_code=e.course_code LEFT JOIN assessment_marks m ON m.enrollment_id=e.id ";
    private Result result(ResultSet r)throws SQLException{
        return evaluate(r.getString("course_type"),r.getInt("classes_held"),r.getInt("classes_attended"),r.getBoolean("condonation_approved"),num(r,"cia1"),num(r,"cia2"),num(r,"assignment_mark"),num(r,"ese"),r.getBoolean("ese_absent"));
    }
    private Double num(ResultSet r,String col)throws SQLException{double v=r.getDouble(col);return r.wasNull()?null:v;}
    private String fmt(Double x){return x==null?"--":String.format("%.2f",x);}
    public void marksheet(String roll,int sem)throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(BASE+"WHERE e.roll_no=? AND e.semester=? ORDER BY c.course_code")){
            p.setString(1,roll);p.setInt(2,sem);try(ResultSet r=p.executeQuery()){
                int credits=0,weighted=0;boolean any=false,complete=true;
                System.out.println("\nMARK SHEET: "+roll+" | Semester "+sem);
                System.out.printf("%-12s %-7s %-7s %-7s %-7s %-7s %-7s %-9s %-9s%n","COURSE","CIA1","CIA2","OTHER","CIA/W","ESE/W","TOTAL","GRADE","STATUS");
                while(r.next()){
                    any=true;Result v=result(r);int cr=r.getInt("credits");credits+=cr;weighted+=cr*v.points;
                    if(v.grade.equals("PENDING"))complete=false;
                    System.out.printf("%-12s %-7s %-7s %-7s %-7s %-7s %-7s %-9s %-9s%n",r.getString("course_code"),fmt(num(r,"cia1")),fmt(num(r,"cia2")),fmt(num(r,"assignment_mark")),fmt(v.cia),fmt(v.ese),fmt(v.total),v.grade,v.status);
                }
                if(!any){System.out.println("No enrollments found.");return;}
                if(!complete){System.out.println("SGPA: PENDING (incomplete assessments/condonation)");return;}
                double sgpa=credits==0?0:(double)weighted/credits;
                System.out.printf("SGPA: %.2f | Equivalent Percentage: %.2f%%%n",sgpa,sgpa*10);
            }
        }
    }
    public void defaulters()throws SQLException{
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT e.roll_no,e.course_code,e.semester,e.classes_held,e.classes_attended FROM enrollments e WHERE e.classes_held=0 OR e.classes_attended*100.0/e.classes_held<65 ORDER BY e.roll_no");ResultSet r=p.executeQuery()){
            System.out.println("ROLL | COURSE | SEM | ATTENDANCE | GRADE");
            while(r.next())System.out.printf("%s | %s | %d | %.2f%% | SA%n",r.getString(1),r.getString(2),r.getInt(3),r.getInt(4)==0?0:100.0*r.getInt(5)/r.getInt(4));
        }
    }
    public void classSummary(int sem)throws SQLException{
        String sql=BASE+"WHERE e.semester=? ORDER BY e.roll_no";
        java.util.Map<String,java.util.Map<String,int[]>> data=new java.util.TreeMap<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setInt(1,sem);try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    String roll=r.getString("roll_no");String dept;
                    try(PreparedStatement s=c.prepareStatement("SELECT department FROM students WHERE roll_no=?")){s.setString(1,roll);try(ResultSet q=s.executeQuery()){q.next();dept=q.getString(1);}}
                    int[] stats=data.computeIfAbsent(dept,k->new java.util.TreeMap<>()).computeIfAbsent(roll,k->new int[4]);
                    Result v=result(r);stats[0]+=r.getInt("credits");stats[1]+=r.getInt("credits")*v.points;
                    if(!v.status.equals("PASS"))stats[2]=1;
                    if(v.grade.equals("PENDING"))stats[3]=1;
                }
            }
        }
        for(var dept:data.entrySet()){
            int pass=0,fail=0,pending=0;double sgpaSum=0;int count=0;
            for(int[] s:dept.getValue().values()){
                if(s[3]==1){pending++;continue;}
                if(s[2]==1)fail++;else pass++;
                sgpaSum+=s[0]==0?0:(double)s[1]/s[0];count++;
            }
            System.out.printf("%s | Pass: %d | Fail: %d | Pending: %d | Avg SGPA: %s%n",dept.getKey(),pass,fail,pending,count==0?"N/A":String.format("%.2f",sgpaSum/count));
        }
    }
}
