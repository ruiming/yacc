import java.util.*;
import java.io.*;

class Main {
    public static Map<String, String> map = new HashMap<String, String>();
    public static Map<String, String> parent = new HashMap<String, String>();

    public static void main(String args[]) throws Exception {
        Main.readBnf("./test.txt");
        System.out.println(Main.checkRecursive());
    }
    
    // 判断是否为 LL1 文法
    // 判断左递归
    private static boolean checkRecursive() {
        Set<String> bnf        = map.keySet();
        Iterator<String> it    = bnf.iterator();
        List<Set<String>> rows = new ArrayList<Set<String>>();
        while (it.hasNext()) {
            String key       = it.next().trim();
            String value     = map.get(key).trim();
            String[] items   = value.split("\\|");
            Set<String> sets = new LinkedHashSet<String>();
            boolean newSet   = true;
            // Set 模拟并查集
            for (Set<String>row: rows) {
                if (row.contains(key)) {
                    sets = row;
                    newSet = false;
                    break;
                }
            }
            if (newSet) sets.add(key);
            for (String item: items) {
                item = item.trim().split(" ");
                // 判断直接左递归和间接左递归
                if (sets.contains(item.trim())) {
                    return false;
                } else {
                    sets.add(item.trim());
                }
            }
            if (newSet) rows.add(sets);
        }
        // 判断间接左递归
        for (Set<String>row: rows) {
            it = row.iterator();  
            while (it.hasNext()) {  
                String str = it.next();  
                System.out.print(str);  
            }  
            System.out.println("");
        }
        return true;
    }

    // 并查集
    private static void Union(String i, String j) {
        Map.put(i, j);
    }

    private static String Find(String i) {
        while (Map.get(i) != null) {
            i = Map.get(i);
        }
        return i;
    }

    // 读取并解析 BNF 文件到 Map
    private static void readBnf(String path) throws Exception {
        StringBuffer sb       = new StringBuffer();
        InputStream is        = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line           = reader.readLine();
        while (line != null) {
            String[] items = line.split("::=");
            if (items.length != 2) {
                throw new Exception("Illegal BNF file!");
            } else {
                map.put(items[0].trim(), items[1].trim());
                line = reader.readLine();
            }
        }
        reader.close();
        is.close();
    }
}