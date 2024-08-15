package dev.sosnovsky.parser;


import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ParserApplication {

    public static void main(String[] args) throws IOException {
        /*if (args.length != 1) {
            System.out.println("Для запуска используйте команду: java -Xmx1G -jar Parser-1.0.jar <file_for_read>");
            return;
        }

        String pathToFile = args[0];*/

        // номер группы, список строк группы
        Map<Integer, Set<String[]>> groups = new HashMap<>();

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
                // главная группа для этой строки
                Integer indexFirstGroup = null;
                for (String word : words) {
                    if (isNotEmpty(word)) {
                        int currentGroup = -1;
                        if (wordsWithPositionAndGroup.containsKey(word)) {
                            if (wordsWithPositionAndGroup.get(word).containsKey(wordPosition)) {
                                currentGroup = wordsWithPositionAndGroup.get(word).get(wordPosition);
                            } else {
                                wordsWithPositionAndGroup.get(word).put(wordPosition, groupIndex);
                                currentGroup = groupIndex++;
                            }
                        } else {
                            Map<Integer, Integer> newMap = new HashMap<>();
                            newMap.put(wordPosition, groupIndex);
                            wordsWithPositionAndGroup.put(word, newMap);
                            currentGroup = groupIndex++;
                        }

                        if (indexFirstGroup == null) {
                            indexFirstGroup = currentGroup;
                            if (groups.containsKey(indexFirstGroup)) {
                                Set<String[]> listFromGroup = groups.get(indexFirstGroup);
                                listFromGroup.add(words);
                                groups.put(indexFirstGroup, listFromGroup);
                            } else {
                                Set<String[]> newSet = new HashSet<>();
                                newSet.add(words);
                                groups.put(indexFirstGroup, newSet);
                            }
                        } else if (indexFirstGroup != currentGroup) {
                            Set<String[]> mainGroup = groups.get(indexFirstGroup);
                            Set<String[]> mergeGroup = groups.getOrDefault(currentGroup, new HashSet<>());
                            mergeGroup.add(words);
                            mainGroup.addAll(mergeGroup);
                            for (String[] s : mergeGroup) {
                                for (int i = 0; i < s.length; i++) {
                                    if (isNotEmpty(s[i])) {
                                        if (wordsWithPositionAndGroup.get(s[i]) == null) {
                                            wordsWithPositionAndGroup.put(s[i], new HashMap<>());
                                        }
                                        wordsWithPositionAndGroup.get(s[i]).put(i, indexFirstGroup);
                                    }
                                }
                            }
                            groups.remove(currentGroup);
                        }
                    }
                    wordPosition++;
                }
            }
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
            System.out.println("\nОшибка чтения файла");
            throw new RuntimeException(e);
        }
    }

    private static void writeResultToFile(Map<Integer, Set<String[]>> groups) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("result.txt"))) {
            writer.write("Количество групп с двумя и более строками: "
                    + groups.values().stream().filter(list -> list.size() >= 2).count());
            writer.newLine();

            List<Set<String[]>> sortedList = groups.values().stream().sorted((g1, g2) -> g2.size() - g1.size()).collect(Collectors.toList());

            int indexGroupForPrint = 1;
            for (Set<String[]> list : sortedList) {
                writer.write("\nГруппа " + indexGroupForPrint++);
                writer.newLine();

                for (String[] str : list) {
                    writer.write(Arrays.toString(str));
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