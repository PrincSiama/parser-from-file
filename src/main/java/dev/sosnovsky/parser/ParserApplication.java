package dev.sosnovsky.parser;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ParserApplication {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Для запуска используйте команду: java -Xmx1G -jar Parser-1.0.jar <file_for_read>");
            return;
        }

        String pathToFile = args[0];

        // номер группы, список строк группы
        Map<Integer, Set<String>> groups = new HashMap<>();

        // номер позиции слова, слово, номер группы
        Map<Integer, Map<String, Integer>> wordsWithPositionAndGroup = new HashMap<>();


        long startTime = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(pathToFile))) {
            String line;
            int groupIndex = 1;

            while ((line = reader.readLine()) != null) {
                int wordPosition = 0;
                if (isValid(line)) {
                    String[] words = line.split(";");
                    Integer indexFirstGroup = null;

                    for (String word : words) {
                        if (isNotEmpty(word)) {
                            int currentGroup;
                            if (wordsWithPositionAndGroup.containsKey(wordPosition)) {
                                if (wordsWithPositionAndGroup.get(wordPosition).containsKey(word)) {
                                    currentGroup = wordsWithPositionAndGroup.get(wordPosition).get(word);
                                } else {
                                    wordsWithPositionAndGroup.get(wordPosition).put(word, groupIndex);
                                    currentGroup = groupIndex++;
                                }
                            } else {
                                Map<String, Integer> newMap = new HashMap<>();
                                newMap.put(word, groupIndex);
                                wordsWithPositionAndGroup.put(wordPosition, newMap);
                                currentGroup = groupIndex++;
                            }

                            if (indexFirstGroup == null) {
                                indexFirstGroup = currentGroup;
                                if (groups.containsKey(indexFirstGroup)) {
                                    Set<String> listFromGroup = groups.get(indexFirstGroup);
                                    listFromGroup.add(line);
                                    groups.put(indexFirstGroup, listFromGroup);
                                } else {
                                    Set<String> newSet = new HashSet<>(1);
                                    newSet.add(line);
                                    groups.put(indexFirstGroup, newSet);
                                }
                            } else if (indexFirstGroup != currentGroup) {
                                Set<String> mainGroup = groups.get(indexFirstGroup);
                                mainGroup.add(line);
                                Set<String> mergeGroup;
                                if (groups.get(currentGroup) != null) {
                                    mergeGroup = groups.get(currentGroup);
                                    mainGroup.addAll(mergeGroup);
                                    for (String s : mergeGroup) {
                                        String[] ws = s.split(";");
                                        for (int i = 0; i < ws.length; i++) {
                                            if (wordsWithPositionAndGroup.containsKey(i)) {
                                                wordsWithPositionAndGroup.get(i).put(ws[i], indexFirstGroup);
                                            }
                                        }
                                    }
                                }
                                wordsWithPositionAndGroup.get(wordPosition).put(words[wordPosition], indexFirstGroup);
                                groups.put(indexFirstGroup, mainGroup);
                                groups.remove(currentGroup);
                            }
                        }
                        wordPosition++;
                    }
                }
            }
        }

        writeResultToFile(groups);

        System.out.println("\nКоличество групп с двумя и более строками: "
                + groups.values().stream().filter(list -> list.size() >= 2).count());

        System.out.println("Время выполнения составило " + (System.currentTimeMillis() - startTime) / 1000.0 + " сек");
    }

    //todo убрать скобки из вывода
    private static void writeResultToFile(Map<Integer, Set<String>> groups) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
            writer.write("Количество групп с двумя и более строками: "
                    + groups.values().stream().filter(list -> list.size() >= 2).count());
            writer.newLine();

            List<Set<String>> sortedList = groups.values()
                    .stream().sorted((g1, g2) -> g2.size() - g1.size()).collect(Collectors.toList());

            int indexGroupForPrint = 1;
            for (Set<String> list : sortedList) {
                writer.write("\nГруппа " + indexGroupForPrint++);
                writer.newLine();

                for (String str : list) {
                    writer.write(str);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("\nОшибка записи файла");
            throw new RuntimeException(e);
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