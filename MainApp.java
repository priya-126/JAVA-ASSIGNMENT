import java.util.Scanner;
public class MainApp {
    static Scanner sc=new Scanner(System.in);
    static String ask(String prompt){System.out.print(prompt);return sc.nextLine().trim();}
    static int integer(String prompt){return Integer.parseInt(ask(prompt));}
    static double decimal(String prompt){return Double.parseDouble(ask(prompt));}
    public static void main(String[] args){
        StudentDAO students=new StudentDAO();AcademicDAO academics=new AcademicDAO();
        while(true){
            System.out.println("\n=== KEC R2024 ACADEMIC SYSTEM ===");
            System.out.println("1 Add Student | 2 Add Course | 3 Enroll | 4 Attendance");
            System.out.println("5 CIA1 | 6 CIA2 | 7 Assignment | 8 ESE");
            System.out.println("9 Mark Sheet | 10 Defaulters | 11 Class Summary | 12 Exit");
            try{
                int choice=integer("Choice: ");if(choice==12)break;
                switch(choice){
                    case 1 -> students.addStudent(ask("Roll: "),ask("Name: "),ask("Department: "),integer("Semester: "));
                    case 2 -> academics.addCourse(ask("Code: "),ask("Title: "),integer("Credits: "),ask("Type (THEORY/PRACTICAL/EMBEDDED): ").toUpperCase());
                    case 3 -> academics.enroll(ask("Roll: "),ask("Course code: "),integer("Semester: "));
                    case 4 -> academics.attendance(integer("Enrollment ID: "),integer("Classes held: "),integer("Classes attended: "),ask("Condonation approved (y/n): ").equalsIgnoreCase("y"));
                    case 5,6,7 -> academics.mark(integer("Enrollment ID: "),choice==5?"cia1":choice==6?"cia2":"assignment_mark",decimal("Marks (0-100): "),false);
                    case 8 -> {int id=integer("Enrollment ID: ");boolean ab=ask("Absent? (y/n): ").equalsIgnoreCase("y");academics.mark(id,"ese",ab?null:decimal("ESE marks (0-100): "),ab);}
                    case 9 -> academics.marksheet(ask("Roll: "),integer("Semester: "));
                    case 10 -> academics.defaulters();
                    case 11 -> academics.classSummary(integer("Semester: "));
                    default -> {System.out.println("Invalid choice");continue;}
                }
                if(choice<=8)System.out.println("Saved successfully.");
            }catch(Exception e){System.out.println("Error: "+e.getMessage());}
        }
    }
}
