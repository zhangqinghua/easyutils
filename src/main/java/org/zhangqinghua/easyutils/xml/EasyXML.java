package org.zhangqinghua.easyutils.xml;


import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class EasyXML {
    // 文档的根节点名称，用于简化查询路径
    public String rootEleName;
    // XML文档对象, 整个类的作用就是封装doc操作以简化使用
    public Document doc;

    public EasyXML() {
        this("<doc></doc>");
    }

    public EasyXML(Document xml) {
        if (xml == null) {
            createErrDoc("Null Document");
        } else {
            doc = xml;
        }
        rootEleName = doc.getRootElement().getName();
    }

    public EasyXML(String xml) {
        try {
            doc = DocumentHelper.parseText(xml);
        } catch (Exception e) {
            createErrDoc(xml);
        }
        rootEleName = doc.getRootElement().getName();
    }

    /**
     * 创建一个错误的XML文档，防止doc没有内容报错
     */
    private void createErrDoc(String xml) {
        try {
            doc = DocumentHelper.parseText("<error>创建Document失败:  " + xml + "</error>");
        } catch (DocumentException e) {
        }
    }

    /**
     * 判断元素值是否正确
     */
    public boolean isEquals(String path, String val) {
        return getTxt(path).equals(val);
    }

    /**
     * 获取根元素属性
     * 因为这个xml是从子一级的目录开始遍历的, 如果要获取根元素只能用这种方法
     */
    public String getAttr(String attr) {
        String attrVal = doc.getRootElement().attributeValue(attr);
        return attrVal == null ? "" : attrVal;
    }

    /**
     * 获取元素属性
     */
    public String getAttr(String path, String attr) {
        String attrVal = getEle(generatePath(path)).get(0).attributeValue(attr);
        return attrVal == null ? "" : attrVal;
    }

    @SuppressWarnings("deprecation")
    public void setAttr(String path, String attr, Object value) {
        getEle(generatePath(path)).get(0).setAttributeValue(attr, value + "");
    }

    @SuppressWarnings("deprecation")
    public void setAttr(String attr, String value) {
        doc.getRootElement().setAttributeValue(attr, value);
    }

    /**
     * 获取元素值
     *
     * @param path
     * @return
     */
    public String getTxt(String path) {
        return getEle(generatePath(path)).get(0).getText();
    }

    /**
     * 获取子节点XML
     * <p>
     * 如果xml里没有此节点，则新增一个节点并返回
     */
    public EasyXML getXML(String path) {
        return new EasyXML(getEle(generatePath(path)).get(0).asXML());
    }

    /**
     * 追加文本内容，会保留里面原先的节点信息
     */
    public void appendTxt(String path, Object txt) {
        getEle(generatePath(path)).get(0).setText(txt == null ? "" : txt + "");
    }

    /**
     * 设置文本内容,会将里面原先的节点信息清空
     */
    public void setTxt(String path, String txt) {
        Element e = getEle(generatePath(path)).get(0);
        e.clearContent();
        e.setText(txt == null ? "" : txt);
    }

    /**
     * 只能查询一个节点下相同的节点列表
     * 例如 人群-》个人，个人
     * 而不能 动物-》人类，鸟类
     *
     * <xml><man>Tron</man><man>Jenny</man></xml>
     */
    public List<String> getTxtList(String path) {
        List<String> list = new ArrayList<>();
        List<Element> eleList = getEle(generatePath(path), false);
        for (Element element : eleList) {
            list.add(element.getText());
        }
        return list;
    }

    public List<EasyXML> getXMLList(String path) {
        List<EasyXML> list = new ArrayList<>();
        List<Element> eleList = getEle(generatePath(path), false);
        for (Element element : eleList) {
            list.add(new EasyXML(element.asXML()));
        }
        return list;
    }

    public List<String> getKeys(String path) {
        List<String> keys = new ArrayList<>();
        Element e = getEle(generatePath(path)).get(0);
        System.out.println(e.getName());
        if (e.elements() != null) {
            for (Object element : e.elements()) {
                if (element instanceof Element) {
                    keys.add(((Element) element).getName());
                }
            }
        }
        System.out.println("getkeys...");
        return keys;
    }

    /**
     * 转为字符串
     */
    public String asXML() {
        return doc.asXML();
    }

    /**
     * 生成查询路径
     * 1. path = null    -> /root
     * 2. apth = ""      -> /root
     * 3. path = "order" -> /root/order
     */
    private String generatePath(String path) {
        String a = "/" + rootEleName;
        if (path == null || path.equals("")) {
            return a;
        }
        a += "/" + path;
        if (a.endsWith("/")) {
            return a.substring(0, a.length() - 1);
        }
        return a;
    }

    /**
     * 获取元素 如果存在，则返回此元素 如果不存在，则创建此元素然后返回
     */
    private List<Element> getEle(String path) {
        return getEle(path, true);
    }

    /**
     * 获取元素
     *
     * @param path
     * @param isAdd 如果元素不存在是否新增一个元素返回
     * @return
     */
    private List<Element> getEle(String path, boolean isAdd) {
        List<Element> list = new ArrayList<>();
        try {
            doc.selectNodes(path);
            for (Object n : doc.selectNodes(path)) {
                list.add((Element) n);
            }
        } catch (Exception e) {
        }
        if (list.size() == 0 && isAdd) {
            try {
                list.add(getEle(path.substring(0, path.lastIndexOf("/")), true).get(0).addElement(path.substring(path.lastIndexOf("/") + 1, path.length())));
            } catch (Exception e) {
            }
        }
        return list;
    }

    public String asFormatXML() {
        StringWriter out = null;
        try {
            OutputFormat formate = OutputFormat.createPrettyPrint();
            out = new StringWriter();
            XMLWriter writer = new XMLWriter(out, formate);
            writer.write(doc);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (Exception e) {

            }
        }
        return out.toString();
    }

    public static void main(String[] args) {
        EasyXML x = new EasyXML("<doc id='001'><man name='Zhang Qinghua' sex='man'>123</man><man>test</man></doc>");
        System.out.println(x.asFormatXML());
        System.out.println("**************************");
        // 1. 获取节点文本
        System.out.println(x.getTxt("man"));

        // 2. 获取节点列表
        for (String t : x.getTxtList("man")) {
            System.out.println(t);
        }

        // 3. 获取属性
        System.out.println(x.getAttr("man", "sex"));
        System.out.println(x.getAttr("id"));

        // 4. 获取节点
        System.out.println(x.getXML("man1").asXML());

        System.out.println(x.asXML());


        x = new EasyXML("<CPMB2B></CPMB2B>");
        x.setTxt("MessageData", "123");

        System.out.println(x.asXML());
    }
}