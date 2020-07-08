
public class Main {
    public static void main(String[] args) {
        ParserTool parser=new ParserTool(Main.class.getClassLoader().getResource("dizhi.json").getFile());
        String data;
        data ="浙江省绍兴市诸暨市浣东街道西子公寓北区 13905857430,张三，0710-7848077";
        System.out.println(parser.parser(data));
        data ="谷城县粉阳路151号,李小龙 13797755777。010-53162090";
        System.out.println(parser.parser(data));
        data ="汪磊,谷城县石花镇北大街30号 17671161666";
        System.out.println(parser.parser(data));
        data = "7741741。张三风，城关镇东大路幸福小区B栋3楼";//失败,只有镇无法解析出正确地址
        System.out.println(parser.parser(data));
        data = "7741741。张三风，谷城县城关镇东大路幸福小区B栋3楼 ";
        System.out.println(parser.parser(data));
    }

}
