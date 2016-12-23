import java.io.*;
import java.util.*;

class Main {
    // 存储BNF
    private static Map<String, String> map = new LinkedHashMap<>();
    // 存储所有符号的 First 集合
    private static Map<String, List<String>> first = new HashMap<>();
    // 存储所有符号的 Follow 集合
    private static Map<String, List<String>> follow = new HashMap<>();
    // 存储分析表
    private static Map<String, Map<String, String>> table = new HashMap<>();

    public static void main(String args[]) throws Exception {
        try {
            readBnf(args[0] + "\\input.bnf");
            calculateFirstCollection();
            calculateFollowCollection();
            checkRecursion();
            buildTable();
        } catch (Exception e) {
            System.out.println(e.toString());
            System.exit(0);
        }
        System.out.println("LL! Parser Build OK, Testing Now");
        File[] files = getTestFiles(args[0]);
        for (File file : files) {
            boolean result = test(file.getAbsolutePath());
            if (result) {
                System.out.println("TRUE  " + file.getName());
            } else {
                System.out.println("FALSE " + file.getName());
            }
        }
    }

    // 构造 First 集
    private static void calculateFirstCollection() throws Exception {
        Set<String> bnf = map.keySet();
        int putCount = 1;
        while (putCount != 0) {
            putCount = 0;
            for (String key : bnf) {
                String value = map.get(key).trim();
                String[] items = value.split("\\|");
                // parent[value].add(key)
                for (String item : items) {
                    String[] words = item.trim().split(" ");
                    boolean empty = true;
                    for (String word : words) {
                        if (empty && key.equals(word)) {
                            // 构造 First 集出问题，存在左递归
                            throw new Exception("Illegal LL1 grammar(Exists Left Recursion)");
                        } else if (empty && !getFirst(key).containsAll(getFirst(word))) {
                            putCount ++;
                            for (String w : getFirst(word)) {
                                // first[key].add(w)
                                addToList(w, key, first);
                            }
                        } else {
                            break;
                        }
                        empty = getFirst(word).contains("\"\"");
                    }
                }
            }
        }
    }

    // 构造 Follow 集
    private static void calculateFollowCollection() throws Exception {
        Set<String> bnf = map.keySet();
        // follow(S).add("$")
        addToList("$", map.keySet().iterator().next(), follow);
        // 获取终结符号
        Set<String> ends = getEndSyntax();
        int putCount = 1;
        while (putCount != 0) {
            putCount = 0;
            for (String key : bnf) {
                String value = map.get(key).trim();
                String[] items = value.split("\\|");
                for (String item : items) {
                    String[] words = item.trim().split(" ");
                    int length = words.length;
                    // A -> aBb
                    for (int i=1; i<length; i++) {
                        if (!ends.contains(words[i - 1])) {
                            for (String p : getFirstOfHandle(item.substring(item.indexOf(words[i])).trim())) {
                                if (!p.equals("\"\"") && !getFollow(words[i - 1]).contains(p)) {
                                    // follow[words[i-1]].add(p)
                                    putCount ++;
                                    addToList(p, words[i - 1], follow);
                                }
                            }
                        }
                    }
                    int i = length;
                    // A -> aB
                    if (!ends.contains(words[i - 1])) {
                        for (String p : getFollow(key)) {
                            if (!p.equals("\"\"") && !getFollow(words[i - 1]).contains(p)) {
                                addToList(p, words[i - 1], follow);
                                putCount ++;
                            }
                        }
                    }
                    // A -> aBb 且 FIRST(b).contains("\"\"")
                    while (i --> 1) {
                        String temp = item.substring(item.indexOf(words[i])).trim();
                        if (getFirstOfHandle(temp).contains("\"\"") && !ends.contains(words[i])) {
                        for (String p : getFollow(key)) {
                            if (!p.equals("\"\"") && !getFollow(words[i - 1]).contains(p)) {
                                addToList(p, words[i - 1], follow);
                                putCount ++;
                            }
                        }
                        break;
                    }
                    }
                }
            }
        }
    }

    // 判断是否为 LL1 文法
    private static void checkRecursion() throws Exception {
        for (String key: map.keySet()) {
            String value = map.get(key).trim();
            String[] items = value.split("\\|");
            boolean flag = false;
            List<String> list = new ArrayList<>();
            for (String item : items) {
                // FIRST(a) 和 FIRST(b) 不相交
                List<String> firstList = getFirstOfHandle(item);
                if (firstList.contains("\"\"")) {
                    flag = true;
                }
                for (String word: list) {
                    if (firstList.contains(word)) {
                        throw new Exception("Illegal LL1 grammar(Multi Ways To Choose)");
                    }
                    if (flag && getFollowOfHandle(key).contains(word)) {
                        throw new Exception("Illegal LL1 grammar(Exists Left Recursion)");
                    }
                }
                list.addAll(firstList);
            }
        }
    }

    // 构造分析表
    private static void buildTable() {
        Set<String> bnf = map.keySet();
        Set<String> ends = getEndSyntax();
        for (String key : bnf) {
            String value = map.get(key).trim();
            String[] items = value.split("\\|");
            Map<String, String> next = getNext(key);
            for (String item : items) {
                for (String syntax: getFirstOfHandle(item.trim())) {
                    if (ends.contains(syntax) && !syntax.equals("\"\"")) {
                        next.put(syntax, item.trim());
                        table.put(key, next);
                    }
                }
                if (getFirstOfHandle(item.trim()).contains("\"\"")) {
                    for (String syntax: getFollow(key)) {
                        if (ends.contains(syntax) && !syntax.equals("\"\"")) {
                            next.put(syntax, item.trim());
                            table.put(key, next);
                        }
                    }
                }
            }
        }
    }

    // 获取 Next
    private static Map<String, String> getNext(String key) {
        if (table.get(key) == null) {
            return new HashMap<>();
        } else {
            return table.get(key);
        }
    }

    // 判断输入是否符合语法定义
    private static boolean test(String path) throws Exception {
        InputStream is        = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        Stack<String> stack   = new Stack<>();
        String top            = map.keySet().iterator().next();
        String line           = reader.readLine();
        if (line == null) {
            line = "$";
        }
        stack.push(top);
        while (!stack.isEmpty()) {
            if (stack.peek().equals(line.trim())) {
                stack.pop();
                line = reader.readLine();
                if (line == null) {
                    line = "$";
                }
            } else if (getEndSyntax().contains(stack.peek())) {
                return false;
            } else if (table.get(stack.peek()) == null || table.get(stack.peek()).get(line) == null) {
                return false;
            } else {
                String[] items = table.get(stack.peek()).get(line).split(" ");
                stack.pop();
                for (int i=items.length-1; i>=0; --i) {
                    if (!items[i].equals("\"\"")) {
                        stack.push(items[i]);
                    }
                }
            }
        }
        reader.close();
        is.close();
        return line.equals("$");
    }

    // 获取指定句柄的 First 集
    private static List<String> getFirstOfHandle(String handle) {
        List<String> list = new ArrayList<>();
        boolean none = true;
        for (String item : handle.trim().split(" ")) {
            list.addAll(getFirst(item));
            if (list.contains("\"\"")) {
                list.remove("\"\"");
            } else {
                none = false;
                break;
            }
        }
        if (none) {
            list.add("\"\"");
        }
        return list;
    }

    // 获取指定句柄的 Follow 集
    private static List<String> getFollowOfHandle(String handle) {
        Set<String> ends = getEndSyntax();
        String[] items = handle.trim().split(" ");
        if (ends.contains(items[items.length - 1])) {
            List<String> temp = new ArrayList<>();
            temp.add(items[items.length - 1]);
            return temp;
        } else {
            return getFollow(items[items.length - 1]);
        }
    }

    // 获取指定 key 的 First 集
    private static List<String> getFirst(String key) {
        Set<String> ends = getEndSyntax();
        if (first.get(key) != null) {
            return first.get(key);
        } else {
            // 判断是否为非终结符号
            if (!map.containsKey(key) && ends.contains(key)) {
                List<String> row = new ArrayList<>();
                row.add(key);
                first.put(key, row);
                return row;
            } else {
                return new ArrayList<>();
            }
        }
    }

    // 获取指定 key 的 Follow 集
    private static List<String> getFollow(String key) {
        if (follow.get(key) != null) {
            return follow.get(key);
        } else {
            return new ArrayList<>();
        }
    }

    // 获取终结符号
    private static Set<String> getEndSyntax() {
        Set<String> bnf = map.keySet();
        Set<String> syntax = new HashSet<>();
        for (String key : bnf) {
            String value = map.get(key).trim();
            String[] items = value.split("\\|");
            for (String item: items) {
                for (String word: item.split(" ")) {
                    // 没有在产生式左端出现则为非终结符号
                    if (!bnf.contains(word.trim())) {
                        syntax.add(word.trim());
                    }
                }
            }
        }
        syntax.add("$");
        return syntax;
    }

    // list[j].add(i)
    private static void addToList(String parent, String child, Map<String, List<String>> list) {
        if (list.get(child) == null) {
            List<String> row = new ArrayList<>();
            row.add(parent);
            list.put(child, row);
        } else if (!list.get(child).contains(parent)) {
            list.get(child).add(parent);
        }
    }

    // 读取并解析 BNF 文件到 Map
    private static void readBnf(String path) throws Exception {
        InputStream is        = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line           = reader.readLine();
        while (line != null) {
            String[] items = line.split(" ::= ");
            if (items.length != 2) {
                throw new Exception("Illegal BNF file!");
            } else {
                if (map.get(items[0].trim()) != null) {
                    map.replace(items[0].trim(), map.get(items[0].trim()) + " | " + items[1].trim());
                } else {
                    map.put(items[0].trim(), items[1].trim());
                }
                line = reader.readLine();
            }
        }
        reader.close();
        is.close();
    }

    // 读取测试文件
    private static File[] getTestFiles(String path) throws Exception {
        File[] files = (new File(path)).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().contains(".tok");
            }
        });
        Arrays.sort(files, new Comparator<File>(){
            public int compare(File f1, File f2) {
                String s1 = f1.getName().substring(11, f1.getName().indexOf("."));
                String s2 = f2.getName().substring(11, f2.getName().indexOf("."));
                return Integer.valueOf(s1).compareTo(Integer.valueOf(s2));
            }
        });
        return files;
    }

}