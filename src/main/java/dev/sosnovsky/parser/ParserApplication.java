package dev.sosnovsky.parser;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ParserApplication {
    private static int groupIndex = 1;

    // номер группы, список строк группы
    private static final Map<Integer, Set<String>> groups = new HashMap<>();

    // номер позиции слова, слово, номер группы
    private static final Map<Integer, Map<String, Integer>> wordsAndGroupsByWordPosition = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Для запуска используйте команду: java -Xmx1G -jar parser-1.0.jar <file_for_read>");
            return;
        }

        long startTime = System.currentTimeMillis();
        String pathToFile = args[0];

        try (BufferedReader reader = new BufferedReader(new FileReader(pathToFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isValid(line)) {
                    parseLine(line);
                }
            }
        } catch (IOException e) {
            System.out.println("\nОшибка чтения файла");
            throw new RuntimeException(e);
        }

        long countGroupWithMoreOneLine = groups.values().stream().filter(list -> list.size() >= 2).count();

        writeResultToFile(countGroupWithMoreOneLine);

        System.out.println("\nКоличество групп с двумя и более строками: " + countGroupWithMoreOneLine);

        System.out.println("Время выполнения составило " + (System.currentTimeMillis() - startTime) / 1000.0 + " сек\n");
    }

    private static void parseLine(String line) {
        String[] words = line.split(";");
        Integer assignedGroupIndex = null;

        for (int wordPosition = 0; wordPosition < words.length; wordPosition++) {
            if (isNotEmpty(words[wordPosition])) {
                int currentGroup;
                if (wordsAndGroupsByWordPosition.containsKey(wordPosition)) {
                    if (wordsAndGroupsByWordPosition.get(wordPosition).containsKey(words[wordPosition])) {
                        currentGroup = wordsAndGroupsByWordPosition.get(wordPosition).get(words[wordPosition]);
                    } else {
                        wordsAndGroupsByWordPosition.get(wordPosition).put(words[wordPosition], groupIndex);
                        currentGroup = groupIndex++;
                    }
                } else {
                    Map<String, Integer> newMap = new HashMap<>();
                    newMap.put(words[wordPosition], groupIndex);
                    wordsAndGroupsByWordPosition.put(wordPosition, newMap);
                    currentGroup = groupIndex++;
                }

                if (assignedGroupIndex == null) {
                    assignedGroupIndex = currentGroup;
                    groups.computeIfAbsent(assignedGroupIndex, s -> new HashSet<>()).add(line);
                } else if (assignedGroupIndex != currentGroup) {
                    mergeGroup(assignedGroupIndex, currentGroup, line, wordPosition, words[wordPosition]);
                }
            }
        }
    }

    private static void mergeGroup(int indexFirstGroup, int currentGroup, String line, int wordPosition, String word) {
        Set<String> assignedGroup = groups.get(indexFirstGroup);
        assignedGroup.add(line);
        if (groups.containsKey(currentGroup)) {
            Set<String> groupToMerge = groups.get(currentGroup);
            assignedGroup.addAll(groupToMerge);
            for (String s : groupToMerge) {
                String[] ws = s.split(";");
                for (int i = 0; i < ws.length; i++) {
                    if (wordsAndGroupsByWordPosition.containsKey(i)) {
                        wordsAndGroupsByWordPosition.get(i).put(ws[i], indexFirstGroup);
                    }
                }
            }
        }
        wordsAndGroupsByWordPosition.get(wordPosition).put(word, indexFirstGroup);
        groups.put(indexFirstGroup, assignedGroup);
        groups.remove(currentGroup);
    }

    private static void writeResultToFile(long countGroupWithMoreOneLine) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
            writer.write("Количество групп с двумя и более строками: " + countGroupWithMoreOneLine);
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
        long countSemicolon = line.chars().filter(ch -> ch == ';').count();
        long countQuotes = line.chars().filter(ch -> ch == '"').count();
        return countQuotes == countSemicolon * 2 + 2;
    }

    private static boolean isNotEmpty(String word) {
        return !(word.isEmpty() || word.equals("\"\""));
    }
}