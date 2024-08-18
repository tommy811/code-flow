package org.luban.flow;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.luban.flow.common.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author 鲁班大叔
 * @date 2021
 */
public class FlowHandler {
    static class $ddd$ {

    }

    public static String VERSION_1_6 = JavaCore.VERSION_1_6;
    public static String VERSION_1_7 = JavaCore.VERSION_1_7;
    public static String VERSION_1_8 = JavaCore.VERSION_1_8;
    public static String VERSION_1_9 = JavaCore.VERSION_9;
    public static String VERSION_1_10 = JavaCore.VERSION_10;
    public static String VERSION_1_11 = JavaCore.VERSION_11;
    private static final String CLASS_TEMPLATE;
    private static final String METHOD_TEMPLATE_NAME = "$coderead_method_tempate$";

    static {
        CLASS_TEMPLATE = "public class $Coderead_class_tempate${ \r\n " +
                " void " + METHOD_TEMPLATE_NAME + "() {\n" +
                "%s\n" +
                "}}\n";
    }

    public Map<String, Node> parseCode(String method_src, String version) {
        ASTParser parser = ASTParser.newParser(AST.JLS13); //设置Java语言规范版本

        String src = String.format(CLASS_TEMPLATE, method_src);

        parser.setKind(ASTParser.K_COMPILATION_UNIT); //
        parser.setResolveBindings(false);
        Map<String, String> compilerOptions = JavaCore.getOptions();
        compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, version); //设置Java语言版本
        compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, version);
        compilerOptions.put(JavaCore.COMPILER_SOURCE, version);
        parser.setCompilerOptions(compilerOptions); //设置编译选项
        parser.setSource(src.toCharArray());
        CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
        MethodDeclaration[] methodNode = new MethodDeclaration[1];
        compilationUnit.accept(new ASTVisitor() { // 访问者模式的优点
            @Override
            public boolean visit(MethodDeclaration node) {
                if (node.getName().getIdentifier().equals(METHOD_TEMPLATE_NAME)) {
                    methodNode[0] = node;
                    return false;
                }
                return true;
            }
        });
        Assert.notNull(methodNode[0]);
        Map<String, Node> nodes = new HashMap<>();
        methodNode[0].accept(new FlowVisit(compilationUnit,src, nodes));
        buildConnect(nodes);            //构建节点
        clearEmptyNodes(nodes);         //移除空节点
      /*  nodes.values().stream().sorted((Comparator.comparing(o -> o.line)))
                .forEach(System.out::println);*/
        return nodes;
    }
    // 建立连接
    private void buildConnect(Map<String, Node> nodes) {
        nodes.forEach((k, v) -> {
            // 是否为注释 节点
            if (v.isType("comment")) {
                return;
            }
            // 是否为if 节点
            if (v.isType("if")) {
                if (nodes.containsKey(v.id + ".A.0")) { // IF THEN
                    v.addConnection(Node.R_YES,v.id + ".A.0");
                }
                if (nodes.containsKey(v.id + ".B.0")) { // IF ELSE
                    v.addConnection(Node.R_NO,v.id + ".B.0");
                    return;
                }
            }
            // 结束循环
            if (v.isType("for if") || v.isType("where if") || v.isType("break")) {
                // 找出循环的节点
                Node loopNextNode = getLoopNextNode(nodes, v);
                if (loopNextNode != null) {
                    v.addConnection(Node.R_NEXT,loopNextNode.id);
                }
                return;
            }
            // visit
            if (v.isType("for update")) {
                // 'for update' 指向它的 上一个节点 'for if',
                Node.IdVal idVal = new Node.IdVal(v.id);
                v.addConnection(Node.R_NEXT,nodes.get(idVal.getUpId()).id);
            }
            // where
            Node nextNode = getNextNode(nodes, v);
            if (nextNode != null) {
                v.addConnection(Node.R_NEXT,nextNode.id);
            }
        });
    }

    private Map<String, Node> clearEmptyNodes(Map<String, Node> nodes) {
        for (Node node : nodes.values()) {
            node.connections.stream()
                    .map(c -> nodes.get(c.id))     //获取下个节点
                    .filter(cn -> cn.isType("empty"))//获取空节点
                    .peek(cn -> node.connections.remove(cn.id))//移除空节点
                    .map(cn -> getNotEmptyNode(nodes, cn))//获取下一个空节点
                    .filter(Objects::nonNull)
                    .forEach(cn ->  node.addConnection(Node.R_NEXT,cn.id));//添加新连接的节点（非空）
        }
        return nodes;
    }

    private Node getNotEmptyNode(Map<String, Node> nodes, Node node) {
        if (!node.isType("empty")) {
            return node;
        }
        if (node.connections.isEmpty()) {
            return null;
        }
        Assert.isTrue(node.connections.size() > 1,
                "空节点，不能指向两个节点，id=" + node.id);
        Assert.isTrue(nodes.containsKey(node.connections.get(0)), "节点不存在：id=" + node.connections.get(0));
        Node nextNode = nodes.get(node.connections.get(0));
        return getNextNode(nodes, nextNode);
    }


    // 找出循环结束后的下一个节点
    private Node getLoopNextNode(Map<String, Node> nodes, Node node) {
        do {
            if (node.isType("for if")) {
                Node updateNode = nodes.get(new Node.IdVal(node.id).getNextId());
                return getNextNode(nodes, updateNode);
            } else if (node.isType("while if")) {
                return getNextNode(nodes, node);
            } else if (!nodes.containsKey(new Node.IdVal(node.id).parentId)) {
                throw new IllegalArgumentException("节点不在循环当中：" + node.id);
            } else {    // 回退到父节点
                node = nodes.get(new Node.IdVal(node.id).parentId);
            }
        } while (true);

    }

    private Node getNextNode(Map<String, Node> nodes, Node node) {
        Node.IdVal idVal = new Node.IdVal(node.id);
        while (true) {
            if (nodes.containsKey(idVal.getNextId())) {
                return nodes.get(idVal.getNextId());
            } else if (idVal.parentId.equals("0")) { // 不存在下一个节占，且父点为0
                return null;
            }
            if (nodes.get(idVal.parentId).isType("while if")) {
                return nodes.get(idVal.parentId);// where if 可被循环指向
            }
            idVal = idVal.parent();
        }
    }

    // 获取注释
    private Node[] buildCommentNode(String src,CompilationUnit unit,ASTNode node) {
        int startPosition = unit.getExtendedStartPosition(node);
        Object o = unit.getCommentList().get(startPosition);
        return null;
    }

}
