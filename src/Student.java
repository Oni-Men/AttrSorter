/*
name : Student.java
about : グループ分けを行うプログラム
date : 2023Oct4
coder : name : 武藤 太央 (Muto Tao)
        studentID : 254739
*/


public class Student {
    public final Integer id;   // 学生証番号を保存
    public final String name;   // 学生の名前を保存
    public final Integer attribute;   // 学生の属性を保存

    public Student(Integer id, String name, Integer attribute){
        this.id = id;
        this.name = name;
        this.attribute = attribute;
    }

    public String toString() {
        String s = "";
        s += "Id: " + this.id + ", ";
        s += "Name: " + this.name + ", ";
        s += "Attr: " + this.attribute + ";";
        return s;
    }
}
