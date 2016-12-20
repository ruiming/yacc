import java.util.*;
import java.io.*;

class Main {
    public static Map<String, String> map = new HashMap<String, String>();

    public static void main(String args[]) throws Exception {
        Main.readBnf("./test.txt");
        System.out.println(Main.checkRecursive());
    }
    
    // 判断是否为 LL1 文法
    // 判断左递归
    private static boolean checkRecursive() {
        Set<String> bnf     = map.keySet();
        Iterator<String> it = bnf.iterator();
        while (it.hasNext()) {
            String key     = it.next();
            String value   = map.get(key);
            String[] items = value.split("\\|");
            for (String item: items) {
                item = item.trim();           
                if (item.indexOf(key) == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // 读取并解析 BNF 文件到 Map
    private static void readBnf(String path) throws Exception {
        StringBuffer sb       = new StringBuffer();
        InputStream is        = new FileInputStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line           = reader.readLine();
        int i                 = 0;
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