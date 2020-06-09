package com.candy.generator.config;

import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

/**
 * @ProjectName mybatistest
 * @FileName Configxxx
 * @Description
 * @Author CandyMuj
 * @Date 2020/4/30 14:15
 * @Version 1.0
 */
public class CoreConfig {
    // 开启：数据库字段是否开启,下划线转java驼峰命名
    private static final boolean COLUMN_UNDERLINE = true;
    // 开启：数据库表名是否开启,下划线转java驼峰命名
    private static final boolean TABLE_UNDERLINE = true;

    // 项目名称（用于文件生成时的doc内容）
    public static final String PROJECT_NAME = "MyTest";

    // 仅生成的表名设置 这两个配置二选一，不可同时使用 可不区分大小写
    public static final String[] GENERATE_INCLUDE = new String[]{

    };
    // 除了下列表不生成，其他表都生成 可不区分大小写
    public static final String[] GENERATE_EXCLUDE = new String[]{

    };


    /**
     * 数据源配置
     */
    public static class DataSource {
        public static final String URL = "jdbc:mysql://127.0.0.1:3306/test?serverTimezone=Asia/Shanghai&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false";
        public static final String USERNAME = "root";
        public static final String PASSWORD = "123456";
        public static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";


        private DataSource() {
        }
    }


    /**
     * 开启：pojo 生成
     */
    public static class GENERATE_POJO {
        // 是否开启
        public static final boolean ENABLE = true;
        // 是否生成ApiModelProperty注解 使用字段备注生成
        public static final boolean APIMODEL_ENABLE = true;
        // 文件输出目录 格式为 绝对路径+包路径
        // 为了灵活，这里使用绝对路径+包路径的方式
        // 注意：关于目录分隔符请使用统一的分隔符 统一为 \ 或 /
        // 其实我可以在代码里面统一转换成某一个格式的，但是我想了想，那样反而使程序不灵活
        public static final String PATH = System.getProperty("user.dir") + "\\src\\main\\java\\com.example.springboottest.mysqltopojo.pojo";


        private GENERATE_POJO() {
        }
    }

    /**
     * 开启：vo生成
     * 首次必须开启pojo生成
     * 后续也可单独生成vo.java
     */
    public static class GENERATE_VO {
        // 是否开启
        public static final boolean ENABLE = true;
        // 若为空，将默认生成在：${GENERATE_POJO.PATH}/vo
        public static final String PATH = "";


        private GENERATE_VO() {

        }
    }

    /**
     * 开启：mapper.java 生成
     */
    public static class GENERATE_MAPPER {
        public static final boolean ENABLE = true;
        public static final String PATH = System.getProperty("user.dir") + "\\src\\main\\java\\com.example.springboottest.mysqltopojo.mapper";


        private GENERATE_MAPPER() {
        }
    }

    /**
     * 开启：mapper.xml 生成  生成xml
     * 首次生成必须开启 mapper.java 生成
     * 后面也可仅单独重新生成mapper.xml
     */
    public static class GENERATE_MAPPER_XML {
        public static final boolean ENABLE = true;
        public static final String PATH = System.getProperty("user.dir") + "\\src\\main\\resources\\mapper1";


        private GENERATE_MAPPER_XML() {
        }
    }

    /**
     * 开启：service 生成
     */
    public static class GENERATE_SERVICE {
        public static final boolean ENABLE = true;
        // 接口输出目录 必须
        public static final String PATH_IFACE = System.getProperty("user.dir") + "\\src\\main\\java\\com.example.springboottest.mysqltopojo.service";
        // 实现类输出目录 若为空，将默认生成在：${PATH_IFACE}/impl
        public static final String PATH_IMPL = "";


        private GENERATE_SERVICE() {
        }
    }

    /**
     * 开启：controller 生成
     */
    public static class GENERATE_CONTROLLER {
        public static final boolean ENABLE = true;
        public static final String PATH = System.getProperty("user.dir") + "\\src\\main\\java\\com.example.springboottest.mysqltopojo.controller";


        private GENERATE_CONTROLLER() {
        }
    }


    public static NamingStrategy getColumnNaming() {
        if (COLUMN_UNDERLINE) {
            return NamingStrategy.underline_to_camel;
        }

        return NamingStrategy.nochange;
    }

    public static NamingStrategy getNaming() {
        if (TABLE_UNDERLINE) {
            return NamingStrategy.underline_to_camel;
        }

        return NamingStrategy.nochange;
    }
}
