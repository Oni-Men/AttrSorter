/*
name : Grouping.java
about : グループ分けを行うプログラム
date : 2023Oct4, 7, 8
coder : name : 武藤 太央 (Muto Tao)
        studentID : 254739
*/


import java.io.*;   // ファイルの入出力を行うため
import java.nio.file.*;   // Files型を利用するため
import java.util.*;   // List、HashMapを利用するため


public class Grouping {
    Integer numOfAttributions;   // 属性の種数を保存するフィールド

    void run(String[] args) throws IOException {
        // 変数設定
        Integer groupCount = Integer.valueOf(args[0]);
        Path csvFilePath = Paths.get(args[1]);
        numOfAttributions = Integer.valueOf(args[2]);

        // 入力処理
        var students = loadCSV(csvFilePath);

        // デバッグ用の出力
        // students.forEach((k, v) -> {
        //     System.out.println(v);
        // });

        var groups = new ArrayList<Group>();
        for(Integer i = 0; i < groupCount; i++){
            var gorupName = "Group " + Character.toString((char) ('A' + i));
            groups.add(new Group(gorupName));
        }

        // グループ分けの処理
        Integer memberMax = dicideMemberMax(groupCount, students);   // 各グループの人数の上限を保存する変数に、その値を計算して保存する。(groups - 1)個のグループでグループ分けができるような数を各グループの人数の上限にする。
        ArrayList<Group> resultGroups = sortStudents(groupCount, memberMax, groups, students);   // グループ分けを行う。ただし、最後の属性の学生は除外する。
        Group lastAttribution = findLastAttrStudents(students);   // 最後の属性の学生だけのグループを作る。

        for(Integer j = 1; ; j ++){
            if(students.size() - lastAttribution.students.size() != sumOfsortedStudents(resultGroups)){   // 最後の属性の学生を除外したグループ分けがうまく行っていなければ、各グループの人数の上限を一人増やしてもう一度グループ分けを行う。
                groups.clear();
                for(Integer i = 0; i < groupCount; i++){
                    var gorupName = "Group " + Character.toString((char) ('A' + i));
                    groups.add(new Group(gorupName));
                }
                
                resultGroups = sortStudents(groupCount, memberMax + j, groups, students);
                break;
            }else{
                break;
            }
        }

        resultGroups = addLastAttrStudents(resultGroups, lastAttribution, students.size());   // 最後の属性の学生をグループ分けする。

        // 出力処理
        printGroups(resultGroups);
    }


    HashMap<Integer, Student> loadCSV(Path path) throws IOException {
        var students = new HashMap<Integer, Student>();
        Files.lines(path).forEach(line -> {
            var fields = line.split(",");
            var id = Integer.valueOf(fields[0]);
            var name = fields[1];
            var attribute = Integer.valueOf(fields[2]);
            var student = new Student(id, name, attribute);
            students.put(id, student);
        });

        return students;
    }


    ArrayList<Group> sortStudents(Integer groupCount, Integer memberMax, ArrayList<Group> groups, HashMap<Integer, Student> students) {
        ArrayList<Group> attributions = sortByAttributes(students);   // 学生を属性でグループ分けする。
        ArrayList<Group> sortedAttributions = sortAttributions(attributions);   // 属性を人数の降順にソートする。

        for(Integer i = sortedAttributions.size() - 1; i > -1; i --){   // 人数が少ない属性からグループ分けを行なっていく。
            Integer flag = 0;   // フラグ

            groups = sortGroups(groups);   // グループを人数の降順でソートする。

            for(Integer j = 0; j < groupCount; j ++){   // 属性の分割なしでのグループ分けを試みる。
                if(sortedAttributions.get(i).students.size() < memberMax - groups.get(j).students.size()){   // 属性の人数を各グループの人数の枠の残りと比べ、より少ないグループがあれば、その属性の全員をそのグループに入れる。
                    groups.get(j).students.addAll(sortedAttributions.get(i).students);
                    flag = 1;

                    break;   // 内側のループを抜ける。
                }
            }
            

            if(flag == 0){   // 属性の分割なしでのグループ分けができなかった場合
                Integer div = findDiv(sortedAttributions.get(i).students.size(), memberMax);   // 属性の分割の割る数を求める。
                ArrayList<Group> divisions = divideAtttributions(sortedAttributions.get(i), div);   // 属性を分割する。

                for(Integer k = 0; k < divisions.size(); k ++){   // 属性の分割先一つ一つをグループ分けしていく。
                    for(Integer l = 0; l < groupCount; l ++){
                        if(divisions.get(k).students.size() <= memberMax - groups.get(l).students.size()){   // 属性の分割先の人数を各グループの人数の枠の残りと比べ、より少ないグループがあれば、その属性の分割先の全員をそのグループに入れる。
                            groups.get(l).students.addAll(divisions.get(k).students);

                            break;   // 最も内側のループを抜ける。
                        }
                    }
                }

                divisions.clear();
            }
        }

        return groups;
    }

    Integer dicideMemberMax(Integer groupCount, HashMap<Integer, Student> students) {   // 各グループの人数の上限を計算するメソッド
        Integer memberMax;  // 計算結果を保存するための変数

        for(Integer i = 0; ; i ++){   // (groupsCount - 1)個のグループでグループ分けができるような数を探す。
            if(students.size() < (groupCount - 1) * i){
                memberMax = i;

                break;
            }
        }

        return memberMax;
    }

    ArrayList<Group> sortByAttributes(HashMap<Integer, Student> students) {   // 学生を属性でグループ分けするメソッド。ただし、最後の属性の学生は除外する。
        ArrayList<Group> attributions = new ArrayList<>();   // 属性ごとのグループのArrayList。

        for(Integer i = 1; i < numOfAttributions; i ++){   // 属性で分けるグループの入れ物を、HashMap上に用意する。
            String name = "attribution" + i;
            var attribution = new Group(name);
            attributions.add(attribution);
        }

        for(Integer j = 1; j < numOfAttributions; j ++){   // 学生を一人一人チェックして行って、属性ごとにグループに分ける。ただし、最後の属性の学生は除外する。
            for(Integer k = 1; k <= students.size() - 5; k ++){
                if(j == students.get(k).attribute){
                    attributions.get(j - 1).students.add(students.get(k));   // 属性jのグループに属性がjの学生を入れる。
                }
            }
        }

        // デバック用の出力
        // for(Integer l = 0; l < attributions.size(); l ++){
        //     attributions.get(l).print();
        // }

        return attributions;
    }

    ArrayList<Group> sortAttributions(ArrayList<Group> attributions) {   // 属性を人数の降順にソートするメソッド
        ArrayList<Group> sortedAttributions = new ArrayList<>();
        Integer max = -1;
        int maxGroup = 0;

        while(!(attributions.isEmpty())){   // バブルソートを実行する。
            max = -1;

            for(Integer i = 0; i < attributions.size(); i ++){   // 人数が最も多い属性を見つける。
                if(attributions.get(i).students.size() > max){
                    max = attributions.get(i).students.size();
                    maxGroup = i;
                }
            }

            sortedAttributions.add(attributions.get(maxGroup));   // 人数が最も多い属性をArrayListの最後に追加する。
            attributions.remove(maxGroup);   // 人数が最も多い属性をArrayListから削除する。
        }

        return sortedAttributions;
    }

    ArrayList<Group> sortGroups(ArrayList<Group> groups) {   // グループを人数の昇順でソートするメソッド
        ArrayList<Group> sortedGroups = new ArrayList<>();
        Integer max = -1;
        int maxGroup = 0;

        while(!(groups.isEmpty())){   // バブルソートを実行する。
            maxGroup = 0;

            for(int i = 0; i < groups.size(); i ++){   // 人数が最も多い属性を見つける
                if(groups.get(i).students.size() > max){
                    max = groups.get(i).students.size();
                    maxGroup = i;
                }
            }

            sortedGroups.add(groups.get(maxGroup));   // 人数が最も多いグループをArrayListの最後に追加する。
            groups.remove(maxGroup);   // 人数が最も多い属性をHashMapから削除する。
        }


        return sortedGroups;
    }

    Integer findDiv(Integer numOfPeople, Integer memberMax) {   // 属性の分割の割る数を求めるメソッド。memberMax * div ≧ numOfPeople を満たす最小の整数divを見つける。
        Integer div = numOfPeople / memberMax;

        if(numOfPeople % memberMax != 0){
            div ++;
        }

        return div;
    }

    ArrayList<Group> divideAtttributions(Group attribution, Integer div) {   // 属性を分割するメソッド
        ArrayList<Group> divisions = new ArrayList<>();

        for(Integer i = 0; i < div; i ++){   // 分割先のグループを作る。
            String divisionName = Integer.toString(i);
            divisions.add(new Group(divisionName));
        }

        for(Integer i = 0; i < attribution.students.size(); i ++){   // 属性の学生をdiv個のグループに割り振っていく。
            divisions.get(i % div).students.add(attribution.students.get(i));
        }

        return divisions;
    }


    Group findLastAttrStudents(HashMap<Integer, Student> students){   // 最後の属性の学生を探し出して、一つのグループに保存するメソッド
        Group lastAttribution = new Group("last");

        for(Integer i = 1; i <= students.size(); i ++){
            if(students.get(i).attribute == numOfAttributions){
                lastAttribution.students.add(students.get(i));
            }
        }

        return lastAttribution;
    }

    Integer sumOfsortedStudents(ArrayList<Group> resultGroups) {   // グループ分けされた学生の総数を求めるメソッド
        Integer sum = 0;

        for(Integer i = 0; i < resultGroups.size(); i ++){
            sum += resultGroups.get(i).students.size();
        }

        return sum;
    }

    ArrayList<Group> addLastAttrStudents(ArrayList<Group> resultGroup, Group lastAttribution, Integer numOfStudents){   // 最後の属性の学生をグループ分けするメソッド
        Integer min = numOfStudents;
        int minGroup = -1;
        Integer j = 0;

        for(Integer i = 0; i < lastAttribution.students.size(); i ++){
            min = numOfStudents;
            minGroup = -1;

            for(j = 0; j < resultGroup.size(); j ++){   // 人数が最も多い属性を見つける。

                if(resultGroup.get(j).students.size() < min){
                    min = resultGroup.get(j).students.size();
                    minGroup = j;
                }
            }

            resultGroup.get(minGroup).students.add(lastAttribution.students.get(i));
        }

        return resultGroup;
    }


    void printGroups(List<Group> groups) {
        for(Group group : groups){
            group.print();
        }
    }


    public static void main(String[] args) throws IOException {
        Grouping grouping = new Grouping();
        grouping.run(args);
    }
}
