package test.flow;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */


import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.luban.flow.DrawIoConvert;
import org.luban.flow.FlowHandler;
import org.luban.flow.Node;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author 鲁班大叔
 * @date 2021
 */
public class FlowHandlerTest {

    private String codeText;
    private String code_if;
    private String code_try_catch;


    @Before
    public void init() throws IOException {
        InputStream src = FlowHandlerTest.class.getResourceAsStream("/src");
        codeText = IOUtils.toString(src);
        code_if=IOUtils.toString(FlowHandlerTest.class.getResourceAsStream("/src_if"));
        code_try_catch=IOUtils.toString(FlowHandlerTest.class.getResourceAsStream("/src_try_catch"));
    }

    @Test
    public void test() {
        FlowHandler handler = new FlowHandler();
        Map<String, Node> nodes = handler.parseCode(codeText, FlowHandler.VERSION_1_8);
        assert !nodes.isEmpty();
    }

    @Test
    public void test_if() {
        FlowHandler handler = new FlowHandler();
        Map<String, Node> nodes = handler.parseCode(code_if, FlowHandler.VERSION_1_8);
        assert !nodes.isEmpty();
    }

    @Test
    public void test_try_catch() {
        FlowHandler handler = null;
        try {
            handler = new FlowHandler();
        } catch (IllegalArgumentException |IllegalStateException e) {
            e.printStackTrace();
        }
        Map<String, Node> nodes = handler.parseCode(code_try_catch, FlowHandler.VERSION_1_8);
        assert !nodes.isEmpty();
    }

    // 生成Drawio 节点
    @Test
    public void testByDrawIo() {
        FlowHandler handler = new FlowHandler();
        Map<String, Node> nodes = handler.parseCode(codeText, FlowHandler.VERSION_1_8);
        DrawIoConvert convert=new DrawIoConvert();
        String result = convert.convertToDrawIo(nodes.values().toArray(new Node[0]));
        System.out.println(result);
    }
}
