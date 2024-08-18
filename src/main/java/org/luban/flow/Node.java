package org.luban.flow;

import java.util.*;
import java.util.stream.Collectors;

public class Node {
    String id;
    String name;
    int line;
    String type;
    String styleType = "normal";
    List<Connection> connections = new ArrayList<>();
    Map<String, String> labels = new HashMap<>();// 连接标签组

    @Override
    public String toString() {
        return String.format("%s %s %s [%s] %sL", id, Arrays.deepToString(connections.toArray()), name.trim(), type, line);
    }

    boolean isType(String typeSource) {
        String original = " " + type + " ";
        for (String s : typeSource.trim().split(" ")) {
            if (!original.contains(" " + s + " ")) {
                return false;
            }
        }
        return true;
    }

    public Connection addConnection(String type, String targetId) {
        Connection e = new Connection(type, targetId);
        connections.add(e);
        return e;
    }

    public static class Connection {
        String type;
        String id;

        public Connection(String type, String id) {
            this.type = type;
            this.id = id;
        }
    }

    public String[] toConnections(){
        String[] result=new String[R_ALL.length];
        for (int i = 0; i < R_ALL.length; i++) {
            int index=i;
            String ids = connections.stream()
                    .filter(d -> d.type.equals(R_ALL[index]))
                    .map(d -> d.id)
                    .collect(Collectors.joining(","));
            result[i]=ids;
        }
        return result;
    }
    public static class IdVal {
        String id;
        String parentId;
        int currentId;

        IdVal(String id) {
            this.id = id;
            parentId = id.substring(0, id.lastIndexOf("."));
            if (parentId.endsWith("A") || parentId.endsWith("B")) {
                parentId = parentId.substring(0, parentId.lastIndexOf("."));
            }
            currentId = Integer.parseInt(id.substring(id.lastIndexOf(".") + 1));
        }

        String getUpId() {
            String pid = id.substring(0, id.lastIndexOf("."));
            return pid + "." + (currentId - 1);
        }

        String getNextId() {
            String pid = id.substring(0, id.lastIndexOf("."));
            return pid + "." + (currentId + 1);
        }

        public IdVal parent() {
            return new IdVal(this.parentId);
        }
    }

    public static final String R_NEXT = "r_next";
    public static final String R_YES = "r_yes";
    public static final String R_NO = "r_no";
    public final static String R_TIPS = "r_tips";
    public final static String R_UNION = "r_union";

    public static final String[] R_ALL = new String[]{
            R_NEXT, R_YES, R_NO, R_TIPS, R_UNION
    };


}