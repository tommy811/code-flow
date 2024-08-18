package test.flow;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import org.junit.Test;
import org.luban.flow.DrawIoConvert;

/**
 * @author 鲁班大叔
 * @date 2021
 */
public class DrawIoConvertTest {

    @Test
    public void loadCssTest() {
        DrawIoConvert convert=new DrawIoConvert();
        System.out.println(convert.loadCssStyle());
    }
}
