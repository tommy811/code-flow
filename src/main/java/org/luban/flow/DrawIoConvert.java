package org.luban.flow;
/**
 * @Copyright 源码阅读网 http://coderead.cn
 */

import com.steadystate.css.dom.CSSStyleRuleImpl;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.luban.flow.common.UtilJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleSheet;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 鲁班大叔
 * @date 2021
 */
public class DrawIoConvert {
    private static Configuration cfg;
    static final Logger logger = LoggerFactory.getLogger(DrawIoConvert.class);

    static {
        initFreemark();
    }

    private static void initFreemark() {
        cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setClassForTemplateLoading(DrawIoConvert.class, "/templates/flow");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);


    }

    public String convertToDrawIo(Node[] nodes) {
        // edges 连边
        // columns 列名
        String[] columns = new String[]{"id", "name", "type", "styletype", "r_yes", "r_no", "r_next"};
        // datas 数据
        String dataRows = Arrays.stream(nodes)
                .sorted(Comparator.comparing(n -> n.id))
                .map(this::joinNodeData)// 拼接数据行
                .collect(Collectors.joining("\r\n"));
        try {
            Template template = cfg.getTemplate("drawIo.ftl");
            StringWriter out = new StringWriter();
            HashMap root = new HashMap();   // 数据根节点
            root.put("dataRows", dataRows);
            Map<String, String> styles = loadCssStyle();
            root.put("styles_json", UtilJson.writeValueAsString(styles));
            root.put("styles",styles);
            root.put("layout",loadLayoutStyle());
            // 连线列
            root.put("joinCloumns", Arrays.stream(Node.R_ALL)
                    .collect(Collectors.joining(",")));
            template.process(root, out);
            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException("freemark 模板加载失败", e);
        } catch (TemplateException e) {
            throw new RuntimeException("流程模板解析失败", e);
        }
    }

    private String joinNodeData(Node n) {
        // 基础行
        Stream<String> baseCloumn = Stream.of(n.id, textWrap(n.name.trim(), 30),
                "rectangle", n.type, n.styleType);
        // 关联行
        Stream<String> joinColumn = Arrays.stream(n.toConnections());

        return  Stream.concat(baseCloumn,joinColumn)
                .map(s -> s.replaceAll("\\\"", "\\\\\""))
                .map(s -> s.replaceAll("\n", "<br>"))
                .map(s -> String.format("\"%s\"", s))// 为字段添加双引号
                .collect(Collectors.joining(","));
    }

    // 文件超出后自动换行
    private static String textWrap(String text, int lineMaxLength) {
        StringBuilder builder = new StringBuilder();
        char[] chars = text.toCharArray();
        for (int i = 0, k = 0; i < chars.length; i++, k++) {
            builder.append(chars[i]);
            if (chars[i] == '\n') {
                k = 0;
            } else if (k == lineMaxLength - 1) {
                builder.append('\n');
                k = 0;
            }
        }
        return builder.toString();
    }

    private String loadLayoutStyle() throws IOException, TemplateException {
        Template template = cfg.getTemplate("layout.json");
        StringWriter out = new StringWriter();
        template.process(new HashMap<>(), out);
        return out.toString().replaceAll("\n", "");
    }

    // 将css样式文件转换成 drawio 样式
    public Map<String, String> loadCssStyle() {
        try {
            Template template = cfg.getTemplate("drawIo.css");
            StringWriter out = new StringWriter();
            template.process(new HashMap<>(), out);

            InputSource source = new InputSource(new StringReader(out.toString()));
            CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
            CSSStyleSheet sheet = parser.parseStyleSheet(source, null, null);
            CSSRuleList rules = sheet.getCssRules();
            Map<String, String> result = new HashMap<>();
            for (int i = 0; i < rules.getLength(); i++) {
                CSSStyleRuleImpl impl = (CSSStyleRuleImpl) rules.item(i);
                CSSStyleDeclaration style = impl.getStyle();
                StringBuilder builder = new StringBuilder();

                for (int k = 0; k < style.getLength(); k++) {
                    builder.append(style.item(k));
                    builder.append("=");
                    builder.append(getCssValue(style, k));
                    builder.append(";");
                }
                result.put(impl.getSelectorText().trim(), builder.toString());
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    // 将css 转换成drawio 样式值
    private String getCssValue(CSSStyleDeclaration style, int index) {
        String item = style.item(index);
        String value = style.getPropertyValue(item).trim();
        style.getPropertyCSSValue(item);
        String prefix = "var(--";
        if (value.startsWith(prefix)) {
            value = value.replace(prefix, "%").replace(")", "%");
            ;
        }
        return value;
    }

    public static void main(String[] args) {
        System.out.println(textWrap("123456789 1234\n56789 123456789 12356", 10));
    }
}
