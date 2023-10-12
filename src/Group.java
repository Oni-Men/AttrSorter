import java.util.*;

public class Group {
    public final String groupName;
    public final List<Student> students;

    public Group(String groupName) {
        this.groupName = groupName;
        this.students = new ArrayList<Student>();
    }

    public void print() {
        System.out.printf("--- %s ---%n", this.groupName);
        for (Student student : this.students) {
            System.out.printf("%d, %s, %d%n", student.id, student.name, student.attribute);
        }
    }

}
