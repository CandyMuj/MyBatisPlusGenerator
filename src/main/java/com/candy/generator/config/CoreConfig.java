package com.candy.generator.config;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.candy.generator.util.YmlConfig;

import java.util.List;

/**
 * @Description
 * @Author CandyMuj
 * @Date 2020/4/30 14:15
 * @Version 1.0
 */
public class CoreConfig {
    // 开启：数据库字段是否开启,下划线转java驼峰命名
    private static final boolean COLUMN_UNDERLINE = YmlConfig.getBoolean("gloab.column-underline");
    // 开启：数据库表名是否开启,下划线转java驼峰命名
    private static final boolean TABLE_UNDERLINE = YmlConfig.getBoolean("gloab.table-underline");

    // 仅生成的表名设置 这两个配置二选一，不可同时使用 可不区分大小写
    public static final String[] GENERATE_INCLUDE;
    // 除了下列表不生成，其他表都生成 可不区分大小写
    public static final String[] GENERATE_EXCLUDE;
    // 路径配置
    public static final String PROJECT_PATH = YmlConfig.getString("module.project_path");


    static {
        List<String> includeList = YmlConfig.getList("gloab.generate-include");
        GENERATE_INCLUDE = includeList != null ? includeList.toArray(new String[0]) : new String[]{};

        List<String> excludeList = YmlConfig.getList("gloab.generate-exclude");
        GENERATE_EXCLUDE = excludeList != null ? excludeList.toArray(new String[0]) : new String[]{};
    }


    /**
     * 数据源配置
     */
    public static class DataSource {
        public static final String URL = YmlConfig.getString("gloab.datasource.url");
        public static final String USERNAME = YmlConfig.getString("gloab.datasource.username");
        public static final String PASSWORD = YmlConfig.getString("gloab.datasource.password");
        public static final String DRIVER_CLASS_NAME = YmlConfig.getString("gloab.datasource.driver-class-name");

        private DataSource() {
        }
    }


    /**
     * 开启：pojo 生成
     */
    public static class GENERATE_POJO {
        // 是否开启
        public static final boolean ENABLE = YmlConfig.getBoolean("module.pojo.enable");
        // 是否生成ApiModelProperty注解 使用字段备注生成
        public static final boolean APIMODEL_ENABLE = YmlConfig.getBoolean("module.pojo.apimodel-enable");
        // 文件输出目录 格式为 绝对路径+包路径
        // 为了灵活，这里使用绝对路径+包路径的方式
        // 注意：关于目录分隔符请使用统一的分隔符 统一为 \ 或 /
        // 其实我可以在代码里面统一转换成某一个格式的，但是我想了想，那样反而使程序不灵活
        public static final String PATH = PROJECT_PATH.concat(YmlConfig.getString("module.pojo.path"));

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
        public static final boolean ENABLE = YmlConfig.getBoolean("module.vo.enable");
        // 若为空，将默认生成在：${GENERATE_POJO.PATH}/vo
        public static final String PATH;

        static {
            String path = YmlConfig.getString("module.vo.path");
            PATH = StrUtil.isNotBlank(path) ? PROJECT_PATH.concat(path) : "";
        }

        private GENERATE_VO() {
        }
    }

    /**
     * 开启：mapper.java 生成
     */
    public static class GENERATE_MAPPER {
        public static final boolean ENABLE = YmlConfig.getBoolean("module.mapper.enable");
        public static final String PATH = PROJECT_PATH.concat(YmlConfig.getString("module.mapper.path"));

        private GENERATE_MAPPER() {
        }
    }

    /**
     * 开启：mapper.xml 生成  生成xml
     * 首次生成必须开启 mapper.java 生成
     * 后面也可仅单独重新生成mapper.xml
     */
    public static class GENERATE_MAPPER_XML {
        public static final boolean ENABLE = YmlConfig.getBoolean("module.mapper-xml.enable");
        public static final String PATH = PROJECT_PATH.concat(YmlConfig.getString("module.mapper-xml.path"));

        private GENERATE_MAPPER_XML() {
        }
    }

    /**
     * 开启：service 生成
     */
    public static class GENERATE_SERVICE {
        public static final boolean ENABLE = YmlConfig.getBoolean("module.service.enable");
        // 接口输出目录 必须
        public static final String PATH_IFACE = PROJECT_PATH.concat(YmlConfig.getString("module.service.path-iface"));
        // 实现类输出目录 若为空，将默认生成在：${PATH_IFACE}/impl
        public static final String PATH_IMPL;

        static {
            String path = YmlConfig.getString("module.service.path-impl");
            PATH_IMPL = StrUtil.isNotBlank(path) ? PROJECT_PATH.concat(path) : "";
        }

        private GENERATE_SERVICE() {
        }
    }

    /**
     * 开启：controller 生成
     */
    public static class GENERATE_CONTROLLER {
        public static final boolean ENABLE = YmlConfig.getBoolean("module.controller.enable");
        public static final String PATH = PROJECT_PATH.concat(YmlConfig.getString("module.controller.path"));

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
