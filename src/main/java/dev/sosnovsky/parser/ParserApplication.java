package dev.sosnovsky.parser;


import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ParserApplication {

    public static void main(String[] args) throws IOException {
        /*if (args.length != 1) {
            System.out.println("Для запуска используйте команду: java -Xmx1G -jar Parser-1.0.jar <file_for_read>");
            return;
        }*/

//        String pathToFile = args[0];

        // номер группы, список строк группы
        Map<Integer, List<String[]>> groups = new HashMap<>();

        // слово, номер позиции слова, номер группы
        Map<String, Map<Integer, Integer>> wordsWithPositionAndGroup = new HashMap<>();


        long startTime = System.currentTimeMillis();

        String pathToFile = "lng2.txt";

        Set<String> lines = getLinesFromFile(pathToFile);

        System.out.println();
        System.out.println("Из файла получено " + lines.size() + " уникальных строк");

        int groupIndex = 1;
        for (String line : lines) {
            int wordPosition = 0;
            if (isValid(line)) {
                String[] words = line.split(";");
                // позиция слова, группа
                Set<Integer> lineGroups = new HashSet<>();
                for (String word : words) {
                    if (isNotEmpty(word)) {
                        if (wordsWithPositionAndGroup.containsKey(word)) {
                            if (wordsWithPositionAndGroup.get(word).containsKey(wordPosition)) {
                                int currentGroup = wordsWithPositionAndGroup.get(word).get(wordPosition);
//                                groups.get(currentGroup).add(line);
                                lineGroups.add(currentGroup);
                            } else {
                                wordsWithPositionAndGroup.get(word).put(wordPosition, groupIndex);
//                                List<String> lineToGroup = new ArrayList<>();
//                                lineToGroup.add(line);
//                                groups.put(groupIndex, lineToGroup);
                                lineGroups.add(groupIndex);
                            }
                        } else {
                            Map<Integer, Integer> newMap = new HashMap<>();
                            newMap.put(wordPosition, groupIndex);
                            wordsWithPositionAndGroup.put(word, newMap);
//                            List<String> lineToGroup = new ArrayList<>();
//                            lineToGroup.add(line);
//                            groups.put(groupIndex, lineToGroup);
                            lineGroups.add(groupIndex);
                        }

                    }

                }

                if (lineGroups.size() == 1) {
                    List<String[]> lineToGroup = new ArrayList<>();
                    lineToGroup.add(words);
                    groups.put(groupIndex, lineToGroup);
                } else if (lineGroups.size() > 1) {
                    List<Integer> groupIndexByLine = new ArrayList<>(lineGroups);
                    int indexMainGroup = groupIndexByLine.get(0);
                    List<String[]> mainGroup = groups.getOrDefault(indexMainGroup, new ArrayList<>());
                    mainGroup.add(words);

                    for (int i = 1; i < groupIndexByLine.size(); i++) {
                        int indexMergeGroup = groupIndexByLine.get(i);
                        if (groups.get(indexMergeGroup) != null) {
                            List<String[]> mergeGroup = groups.get(indexMergeGroup);
                            mainGroup.addAll(mergeGroup);
                            for (String[] ws : groups.get(indexMergeGroup)) {
                                for (int j = 0; j < ws.length; j++) {
                                    String word = ws[i];
                                    if (isNotEmpty(word)) {
                                        wordsWithPositionAndGroup.get(word).put(j, indexMainGroup);
                                    }
                                }
                            }
                        }
                        groups.put(indexMainGroup, mainGroup);
                        groups.remove(indexMergeGroup);
                    }
                } else {
                    continue;
                }

                wordPosition++;
            }
            groupIndex++;

        }

        writeResultToFile(groups);

        System.out.println("Количество групп с двумя и более строками: "
                + groups.values().stream().filter(list -> list.size() >= 2).count());


        System.out.println("Время выполнения составило " + (System.currentTimeMillis() - startTime) / 1000.0 + " сек");
    }

    private static Set<String> getLinesFromFile(String pathToFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(pathToFile))) {
            return br.lines().collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeResultToFile(Map<Integer, List<String[]>> groups) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
            writer.write("Количество групп: " + groups.size() + "\n");

            Set<Integer> keys = groups.keySet();

            for (Integer num : keys) {
//                if (groups.get(num).size() > 1) {
                    writer.write("\n Группа " + num);
                    writer.newLine();

                    for (String[] group : groups.get(num)) {
                        writer.write(Arrays.toString(group));
                        writer.newLine();
                    }
//                }
            }

        }

    }


    private static boolean isValid(String line) {
        long count = line.chars().filter(ch -> ch == '"').count();
        return count % 2 == 0;
    }

    private static boolean isNotEmpty(String word) {
        return !(word.isEmpty() || word.equals("\"\""));
    }
}

























    /*private static class DSU {
        private final Map<String, String> parent = new HashMap<>();

        public String find(String s) {
            parent.putIfAbsent(s, s);
            if (!parent.get(s).equals(s)) {
                parent.put(s, find(parent.get(s)));
            }
            return parent.get(s);
        }

        public void union(String s1, String s2) {
            String root1 = find(s1);
            String root2 = find(s2);
            if (!root1.equals(root2)) {
                parent.put(root1, root2);
            }
        }
    }


    public static void main(String[] args) throws IOException {

        *//*if (args.length != 1) {
            System.out.println("Usage: java -jar Grouping.jar <input_file>");
            return;
        }*//*

        long startTime = System.currentTimeMillis();
        String inputFile = "lng.txt";
        Set<String> uniqueLines = new HashSet<>();
        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isValid(line)) {
                    if (uniqueLines.add(line)) {
                        rows.add(line.split(";"));
                    }
                }
            }
        }

        DSU dsu = new DSU();
        Map<Integer, Map<String, List<String>>> columnToValueToLines = new HashMap<>();

        for (String[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (!row[i].isEmpty()) {
                    columnToValueToLines.putIfAbsent(i, new HashMap<>());
                    Map<String, List<String>> valueToLines = columnToValueToLines.get(i);
                    valueToLines.putIfAbsent(row[i], new ArrayList<>());
                    for (String other : valueToLines.get(row[i])) {
                        dsu.union(other, String.join(";", row));
                    }
                    valueToLines.get(row[i]).add(String.join(";", row));
                }
            }
        }

        Map<String, List<String>> groups = new HashMap<>();
        for (String line : uniqueLines) {
            String root = dsu.find(line);
            groups.putIfAbsent(root, new ArrayList<>());
            groups.get(root).add(line);
        }

        List<List<String>> resultGroups = new ArrayList<>();
        for (List<String> group : groups.values()) {
            if (group.size() > 1) {
                resultGroups.add(group);
            }
        }

        resultGroups.sort((a, b) -> b.size() - a.size());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"))) {
            writer.write("Количество групп с более чем одним элементом: " + resultGroups.size() + "\n");
            for (int i = 0; i < resultGroups.size(); i++) {
                writer.write("Группа " + (i + 1) + "\n");
                for (String line : resultGroups.get(i)) {
                    writer.write(line + "\n");
                }
                writer.write("\n");
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Количество групп с более чем одним элементом: " + resultGroups.size());
        System.out.println("Время выполнения: " + (endTime - startTime) + " ms");
    }

    private static boolean isValid(String line) {
        // Проверка на корректность строки
        return line.chars().filter(ch -> ch == '"').count() % 2 == 0;
    }
    }*/