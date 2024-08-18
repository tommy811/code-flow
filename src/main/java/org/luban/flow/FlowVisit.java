package org.luban.flow;

import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FlowVisit extends ASTVisitor {

    String parentId = "0";
    int currentId = 0;
    String src;
    Map<String, Node> nodes;

    CompilationUnit unit;
    Predicate<ASTNode> ignoreStatement;
    static final String POST_CALL_LISTENER = "POST_CALL";

    {
        ignoreStatement = node -> node instanceof SynchronizedStatement;
    }

    public FlowVisit(CompilationUnit unit, String src, Map<String, Node> nodes) {
        this.src = src;
        this.nodes = nodes;
        this.unit = unit;
    }

    public boolean preVisit2(ASTNode node) {
        if (node.getProperty("PRE_INSTALL") != null) {
            ((Runnable) node.getProperty("PRE_INSTALL")).run();
        }
        // 重置父节点
        if (node.getProperty("pid") != null) {
            this.parentId = (String) node.getProperty("pid");
            currentId = 0;
        }
        if (node instanceof Block || !(node instanceof Statement) || ignoreStatement.test(node)) {
            return true;
        }

        Node cnode = null;
        if (node instanceof IfStatement) {
            cnode = buildIfNode((IfStatement) node);
        } else if (node instanceof ForStatement) {
            for (Node n : buildForLoopNode((ForStatement) node)) {
                nodes.put(n.id, n);
            }
        } else if (node instanceof EnhancedForStatement) {//增强集合循环
            for (Node n : buildEnhancedForNode((EnhancedForStatement) node)) {
                nodes.put(n.id, n);
            }
        } else if (node instanceof WhileStatement) {
            cnode = buildWhileNode((WhileStatement) node);
        } else if (node instanceof DoStatement) {
            cnode = buildDoWhileNode((DoStatement) node);
        } else if (node instanceof BreakStatement) {
            cnode = new Node();
            cnode.id = createNextId();
            cnode.name = node.toString();
            cnode.type = "break";
            cnode.line = getLineNumber(node);
        } else if (node instanceof TryStatement) {
//            System.out.println(node);
            cnode = buildTryNode((TryStatement) node);
        } else {
            cnode = new Node();
            cnode.id = createNextId();
            cnode.name = node.toString();
            cnode.type = "statement";
            cnode.line = getLineNumber(node);
            if (node instanceof ReturnStatement) {
                cnode.styleType="return";
            }
        }
        if (cnode != null) {
            buildCommentNode(cnode, node);// 构建注释节点
            nodes.put(cnode.id, cnode);
        }
        return true;
    }

    @Override
    public boolean visit(CatchClause node) {
//        System.out.println(node);
        Node cnode = new Node();
        cnode.id = createNextId();
        cnode.name = node.getException().toString();
        cnode.type = "if try catch";
        cnode.line = getLineNumber(node);
        cnode.styleType="catch";
        nodes.put(cnode.id, cnode);

        preInstallId(node.getBody(), cnode.id + ".A");
        /*TryStatement tryStatement = (TryStatement) node.getParent();
        for (Object catchClause : tryStatement.catchClauses()) {

        }*/
        // if节点结束后 还原ID
        postBackId(node);

        return true;
    }



    // 构建try节点
    private Node buildTryNode(final TryStatement tryNode) {
        Node node = new Node();
        //  try 开始节点
        node.name = "try";
        node.type = "start try ";
        node.styleType="try";
        node.id = createNextId();
        node.line = getLineNumber(tryNode);
        tryNode.setProperty(POST_CALL_LISTENER, (Runnable) () -> {
            Node endNode = new Node();
            endNode.name = "end try";
            endNode.type = "end try ";
            endNode.styleType="try";
            endNode.id=createNextId();
            endNode.line = node.line;
//            endNode.addConnection(Node.R_UNION,node.id);
            node.addConnection(Node.R_UNION,endNode.id);
            nodes.put(endNode.id,endNode);
            //
        });
        //  try 结束节点
        return node;
    }



    private String createNextId() {
        return parentId + "." + (currentId++);
    }

    private Node buildIfNode(IfStatement ifNode) {
        Node cnode = new Node();

        cnode.id = createNextId();
        cnode.name = ifNode.getExpression().toString();
        cnode.type = "if";
        cnode.line = getLineNumber(ifNode);
        cnode.styleType="if";
        // 设置子节点的父ID
        preInstallId(ifNode.getThenStatement(), cnode.id + ".A");
        if (ifNode.getElseStatement() != null) {
            preInstallId(ifNode.getElseStatement(), cnode.id + ".B");
        }
        // if节点结速后 还原ID
        postBackId(ifNode);
        return cnode;
    }

    // 构建
    private Node buildWhileNode(WhileStatement whileNode) {
        Node cnode = new Node();
        cnode.id = createNextId();
        cnode.name = whileNode.getExpression().toString();
        cnode.type = "while if";
        cnode.line = getLineNumber(whileNode);
        // 设置子节点的父ID
        preInstallId(whileNode.getBody(), cnode.id + ".A");
        // if节点结速后 还原ID
        postBackId(whileNode);
        return cnode;
    }

    private Node buildDoWhileNode(DoStatement doNode) {
        Node cnode = new Node();
        cnode.id = createNextId();
        cnode.name = doNode.getExpression().toString();
        cnode.type = "do while if";
        // 设置子节点的父ID
        preInstallId(doNode.getBody(), cnode.id + ".A");
        // if节点结速后 还原ID
        postBackId(doNode);
        cnode.line = getLineNumber(doNode);
        return cnode;
    }


    private void preInstallId(ASTNode node, String parentId) {
        node.setProperty("PRE_INSTALL", (Runnable) () -> {
            this.parentId = parentId;
            this.currentId = 0;
        });
    }

    private void postBackId(ASTNode node) {
        String oldParentId = this.parentId;
        int oldCurrentId = this.currentId;

        node.setProperty(POST_CALL_LISTENER, (Runnable) () -> {
            this.parentId = oldParentId;
            this.currentId = oldCurrentId;
        });
    }

    // 构建 集合遍历
    private List<Node> buildEnhancedForNode(EnhancedForStatement forNode) {
        List<Node> results = new ArrayList<>();

        // 变量声明
        Node initNode = new Node();
        initNode.name = String.format("for(%s %s)", forNode.getParameter(), forNode.getExpression());
        initNode.type = "for init";
        initNode.id = createNextId();
        initNode.line = getLineNumber(forNode);
        results.add(initNode);
        buildCommentNode(initNode, forNode);
        // for（）
        Node conditionNode = new Node();
        conditionNode.name = forNode.getExpression().toString() + ".hasNext()";
        conditionNode.id = createNextId();
        conditionNode.type = "for if";
        conditionNode.line = getLineNumber(forNode);
        results.add(conditionNode);

        Node udpateNode = new Node();
        udpateNode.name = forNode.getExpression().toString() + ".next()";
        udpateNode.type = "for update";
        udpateNode.id = createNextId();
        udpateNode.line = getLineNumber(forNode);
        results.add(udpateNode);

        /*Node node = new Node();
        String name = String.format("for(%s : %s)", forNode.getParameter(), forNode.getExpression());
        node.name=name;
        node.id=createNextId();
        node.line = getLineNumber(forNode);
        node.type = "for if update";
        results.add(node);*/
        // 设置子节点的父ID
        preInstallId(forNode.getBody(), conditionNode.id + ".A");

        postBackId(forNode);
        return results;
    }

    private List<Node> buildForLoopNode(ForStatement forNode) {
        List<Node> results = new ArrayList<>();
        // 构建初始化节点
        List initializers = forNode.initializers();
        if (!initializers.isEmpty()) {
            Node initNode = new Node();
            initNode.name = initializers.get(0).toString();
            initNode.type = "for init";
            initNode.id = createNextId();
            initNode.line = getLineNumber(forNode);
            results.add(initNode);
            buildCommentNode(initNode, forNode);
        }
        // 条件节点
        Node conditionNode = new Node();
        if (forNode.getExpression() == null) {
            conditionNode.name = "true";
        } else {
            conditionNode.name = forNode.getExpression().toString();
        }
        conditionNode.id = createNextId();
        conditionNode.type = "for if";
        conditionNode.line = getLineNumber(forNode);
        preInstallId(forNode.getBody(), conditionNode.id + ".A");
        results.add(conditionNode);
        // 更新
        Node updaterNode = new Node();
        updaterNode.id = createNextId();
        updaterNode.type = "for update";
        updaterNode.line = getLineNumber(forNode);
        if (forNode.updaters().isEmpty()) {
            updaterNode.type += " empty";// 空节点
            updaterNode.name = "<empty>";
        } else {
            updaterNode.name = forNode.updaters().get(0).toString();

        }
        results.add(updaterNode);
        // for循环结束后返回 父ID
        postBackId(forNode);
        return results;
    }

    @Override
    public void postVisit(ASTNode node) {
        if (node.getProperty("POST_CALL") != null) { // 还原父ID
            ((Runnable) node.getProperty("POST_CALL")).run();
        }
    }

    public int getLineNumber(ASTNode node) {
        return src.substring(0, node.getStartPosition()).split(
                "\n"
        ).length;
    }

    private void buildCommentNode(Node node, ASTNode astNode) {

        int index = unit.firstLeadingCommentIndex(astNode);
        if (index < 0) {
            index = unit.lastTrailingCommentIndex(astNode);
        }
        if (index < 0) {
            return;
        }
        Comment comment = (Comment) unit.getCommentList().get(index);
        int pos = comment.getStartPosition();
        String commentText = src.substring(pos, pos + comment.getLength());
        String cid = node.id + ".comment.1";
        Node commentNode = new Node();
        commentNode.id = cid;
        commentNode.name = commentText;
        //html class = for update
        commentNode.type = "comment";// lime block javaDoc
        commentNode.styleType = "comment";
        commentNode.line = unit.getLineNumber(pos);
        commentNode.addConnection(Node.R_TIPS,node.id);
        nodes.put(cid, commentNode);
    }

    public static void main(String[] args) {

        try {
            throw new RuntimeException("ddd");
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            System.out.println("dd");
        }

        // end try case: case:2 case:3
    }


}