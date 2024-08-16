package dev.sosnovsky.parser;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ParserApplication {
    static int groupIndex = 1;

    // номер группы, список строк группы
    static Map<Integer, Set<String>> groups = new HashMap<>();

    // номер позиции слова, слово, номер группы
    static Map<Integer, Map<String, Integer>> wordsWithPositionAndGroup = new HashMap<>();

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
        int wordPosition = 0;
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
                    groups.computeIfAbsent(indexFirstGroup, s -> new HashSet<>(1)).add(line);
                } else if (indexFirstGroup != currentGroup) {
                    mergeGroup(indexFirstGroup, currentGroup, line, wordPosition, words[wordPosition]);
                }
            }
            wordPosition++;
        }
    }

    private static void mergeGroup(int indexFirstGroup, int currentGroup, String line, int wordPosition, String word) {
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
        wordsWithPositionAndGroup.get(wordPosition).put(word, indexFirstGroup);
        groups.put(indexFirstGroup, mainGroup);
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
        long count = line.chars().filter(ch -> ch == '"').count();
        return count % 2 == 0;
    }

    private static boolean isNotEmpty(String word) {
        return !(word.isEmpty() || word.equals("\"\""));
    }
}