package com.candy.generator;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.DbType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.config.rules.QuerySQL;
import com.candy.generator.config.CoreConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @ProjectName mybatistest
 * @FileName TestMain
 * @Description ***
 * *. 适用于springboot，mybatis-plus，mysql，lombok构建的项目；暂不适用于oracle，因为类型转换和一些语法是不同和返回值不同，没有处理其他数据库
 * *. 暂只支持单表内单主键的情况
 * @Author CandyMuj
 * @Date 2020/4/30 14:04
 * @Version 1.0
 */
@Slf4j
public class MyBatisPlusGenerator {
    private final DataSourceConfig dataSourceConfig = new DataSourceConfig();
    private final StrategyConfig strategyConfig = new StrategyConfig();
    private QuerySQL querySQL;


    // 文件目录相关的配置
    private static final String PATH_SEPARATOR = File.separator;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String TEMP_PATH = System.getProperty("java.io.tmpdir") + "{GENERATOR_TEMP_FILE}" + PATH_SEPARATOR;
    private static final String PROJECT_PATH = System.getProperty("user.dir") + PATH_SEPARATOR;
    private static final String TEMPLATES_PATH = PROJECT_PATH + "src" + PATH_SEPARATOR + "main" + PATH_SEPARATOR + "resources" + PATH_SEPARATOR + "templates";


    /**
     * 可生成如下内容，都是可选的
     * pojo
     * mapper
     * mapper.xml  生成xml，必须开启 mapper.java 生成
     * Iservice,serviceimpl 接口和实现同步生成
     * controller
     *
     * @param args
     */
    public static void main(String[] args) {
        new MyBatisPlusGenerator().run();
    }


    private void run() {
        log.debug("==========================准备生成文件...==========================");
        cleanTempFile();

        this.dataSourceConfig.setUrl(CoreConfig.DataSource.URL);
        this.dataSourceConfig.setUsername(CoreConfig.DataSource.USERNAME);
        this.dataSourceConfig.setPassword(CoreConfig.DataSource.PASSWORD);
        this.dataSourceConfig.setDriverName(CoreConfig.DataSource.DRIVER_CLASS_NAME);

        try (Connection connection = this.dataSourceConfig.getConn()) {
            this.querySQL = this.getQuerySQL(this.dataSourceConfig.getDbType());

            // 获取所有的表
            List<TableInfo> tableInfos = this.getTablesInfo(connection);

            if (tableInfos != null && tableInfos.size() > 0) {
                generateFile(tableInfos);
            } else {
                log.info("未获取到表信息");
            }
        } catch (Exception e) {
            log.info("异常...", e);
        } finally {
            cleanTempFile();
        }

        log.debug("==========================文件生成完成！！！==========================");
    }

    private QuerySQL getQuerySQL(DbType dbType) {
        QuerySQL[] arr$ = QuerySQL.values();
        for (QuerySQL qs : arr$) {
            if (qs.getDbType().equals(dbType.getValue())) {
                return qs;
            }
        }

        return QuerySQL.MYSQL;
    }

    private List<TableInfo> getTablesInfo(Connection connection) throws Exception {
        List<TableInfo> tableList = new ArrayList<>();
        PreparedStatement preparedStatement = null;

        String tableCommentsSql = this.querySQL.getTableCommentsSql();
        if (QuerySQL.POSTGRE_SQL == this.querySQL) {
            tableCommentsSql = String.format(tableCommentsSql, this.dataSourceConfig.getSchemaname());
        }

        preparedStatement = connection.prepareStatement(tableCommentsSql);
        ResultSet results = preparedStatement.executeQuery();

        while (true) {
            while (results.next()) {
                String tableName = results.getString(this.querySQL.getTableName());
                if (StrUtil.isNotEmpty(tableName)) {
                    String tableComment = results.getString(this.querySQL.getTableComment());
                    TableInfo tableInfo = new TableInfo();
                    tableInfo.setName(tableName);
                    tableInfo.setComment(tableComment);
                    tableList.add(tableInfo);
                } else {
                    log.error("当前数据库为空！！！");
                }
            }

            Iterator<TableInfo> infoIterator = tableList.iterator();
            while (infoIterator.hasNext()) {
                this.convertTableFields(connection, infoIterator.next(), CoreConfig.getColumnNaming());
            }
            break;
        }

        return this.processTable(tableList, CoreConfig.getNaming());
    }

    private TableInfo convertTableFields(Connection connection, TableInfo tableInfo, NamingStrategy strategy) {
        boolean haveId = false;
        List<TableField> fieldList = new ArrayList<>();

        try {
            String tableFieldsSql = this.querySQL.getTableFieldsSql();
            if (QuerySQL.POSTGRE_SQL == this.querySQL) {
                tableFieldsSql = String.format(tableFieldsSql, this.dataSourceConfig.getSchemaname(), tableInfo.getName());
            } else {
                tableFieldsSql = String.format(tableFieldsSql, tableInfo.getName());
            }

            PreparedStatement preparedStatement = connection.prepareStatement(tableFieldsSql);
            ResultSet results = preparedStatement.executeQuery();

            label64:
            while (true) {
                while (true) {
                    if (!results.next()) {
                        break label64;
                    }

                    TableField field = new TableField();
                    String key = results.getString(this.querySQL.getFieldKey());
                    boolean isId = StrUtil.isNotEmpty(key) && key.toUpperCase().equals("PRI");
                    if (isId && !haveId) {
                        field.setKeyFlag(true);
                        if (this.isKeyIdentity(results)) {
                            field.setKeyIdentityFlag(true);
                        }

                        haveId = true;
                    } else {
                        field.setKeyFlag(false);
                    }

                    field.setName(results.getString(this.querySQL.getFieldName()));
                    field.setType(results.getString(this.querySQL.getFieldType()));
                    field.setPropertyName(strategyConfig, this.processName(field.getName(), strategy));
                    field.setColumnType(this.dataSourceConfig.getTypeConvert().processTypeConvert(field.getType()));
                    field.setComment(results.getString(this.querySQL.getFieldComment()));

                    fieldList.add(field);
                }
            }
        } catch (SQLException var15) {
            log.error("SQL Exception：{}", var15.getMessage());
        }

        tableInfo.setFields(fieldList);
        return tableInfo;
    }

    private boolean isKeyIdentity(ResultSet results) throws SQLException {
        if (QuerySQL.MYSQL == this.querySQL) {
            String extra = results.getString("Extra");
            if ("auto_increment".equals(extra)) {
                return true;
            }
        } else if (QuerySQL.SQL_SERVER == this.querySQL) {
            int isIdentity = results.getInt("isIdentity");
            return 1 == isIdentity;
        }

        return false;
    }

    private List<TableInfo> processTable(List<TableInfo> tableList, NamingStrategy strategy) {
        TableInfo tableInfo;
        for (Iterator<TableInfo> i$ = tableList.iterator(); i$.hasNext(); this.checkTableIdTableFieldAnnotation(tableInfo)) {
            tableInfo = i$.next();

            tableInfo.setEntityName(strategyConfig, NamingStrategy.capitalFirst(this.processName(tableInfo.getName(), strategy)));
            tableInfo.setMapperName(tableInfo.getEntityName() + "Mapper");
            tableInfo.setXmlName(tableInfo.getEntityName() + "Mapper");
            tableInfo.setServiceName("I" + tableInfo.getEntityName() + "Service");
            tableInfo.setServiceImplName(tableInfo.getEntityName() + "ServiceImpl");
            tableInfo.setControllerName(tableInfo.getEntityName() + "Controller");
        }

        return tableList;
    }

    private void checkTableIdTableFieldAnnotation(TableInfo tableInfo) {
        boolean importTableFieldAnnotaion = CoreConfig.getNaming().equals(NamingStrategy.underline_to_camel);
        boolean importTableIdAnnotaion = false;
        boolean importInputIdType = false;

        Iterator<TableField> i$ = tableInfo.getFields().iterator();
        while (i$.hasNext()) {
            TableField tf = i$.next();
            if (tf.isKeyFlag()) {
                importTableIdAnnotaion = true;

                if (tf.isKeyIdentityFlag()) {
                    importInputIdType = true;
                }

                break;
            }
        }

        if (importTableFieldAnnotaion) {
            tableInfo.getImportPackages().add(com.baomidou.mybatisplus.annotations.TableField.class.getCanonicalName());
        }

        if (importTableIdAnnotaion) {
            tableInfo.getImportPackages().add(TableId.class.getCanonicalName());
        }

        if (importInputIdType) {
            tableInfo.getImportPackages().add(IdType.class.getCanonicalName());
        }
    }

    private String processName(String name, NamingStrategy strategy) {
        String propertyName;
        if (strategy == NamingStrategy.underline_to_camel) {
            propertyName = NamingStrategy.underlineToCamel(name);
        } else {
            propertyName = name;
        }

        return propertyName;
    }


    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    private void generateFile(List<TableInfo> tableList) throws Exception {
        File[] templates = new File(TEMPLATES_PATH).listFiles();
        Map<String, File> templateMap = new HashMap<>();
        if (templates != null) {
            for (File f : templates) {
                templateMap.put(f.getName(), f);
            }
        }

        for (TableInfo tableInfo : tableList) {
            // pojo.java
            if (CoreConfig.GENERATE_POJO.ENABLE) {
                log.debug("Generate Pojo.java ... table name => {}", tableInfo.getName());

                String pojoPath = getTargetPath(CoreConfig.GENERATE_POJO.PATH);
                Assert.isTrue(StrUtil.isNotBlank(pojoPath), "未获取到 pojo.java 路径");
                log.debug("Generate Pojo.java to : {}", pojoPath);

                // 导入的包
                Set<String> importStr = new HashSet<>();
                // 除了主键以外的字段
                Set<String> fieldStr = new HashSet<>();

                File template = templateMap.get("pojo.java.mj");
                String str = FileUtil.readUtf8String(template)
                        .replace("${NAME}", tableInfo.getEntityName())
                        .replace("${DATE}", DateUtil.format(new Date(), "yyyy/MM/dd HH:mm"))
                        .replace("${PROJECTNAME}", CoreConfig.PROJECT_NAME);

                // 是否含有主键，如果没有的话，需删除模板的一些内容
                boolean hasKey = false;
                for (TableField field : tableInfo.getFields()) {
                    // 主键
                    if (field.isKeyFlag()) {
                        hasKey = true;
                        str = str.replace("${TABLEID}", "this." + field.getPropertyName());

                        importStr.add(getPkgStr(TableId.class.getName()));
                        if (field.isKeyIdentityFlag()) {
                            str = str.replace("${IdType}", "");
                        } else {
                            str = str.replace("${IdType}", "(type = IdType.INPUT)");
                            importStr.add(getPkgStr(IdType.class.getName()));
                        }

                        str = str.replace("${IdField}", getFieldStr(field));
                    }
                    // 非主键
                    else {
                        fieldStr.add(getFieldStr(field));
                    }

                    if (field.getColumnType() != null && field.getColumnType().getPkg() != null) {
                        importStr.add(getPkgStr(field.getColumnType().getPkg()));
                    }
                }

                if (!hasKey) {
                    str = str
                            .replace("${TABLEID}", "null")
                            .replaceAll(LINE_SEPARATOR + ".*@TableId\\$\\{IdType}" + LINE_SEPARATOR, LINE_SEPARATOR)
                            .replaceAll(LINE_SEPARATOR + ".*\\$\\{IdField}" + LINE_SEPARATOR, LINE_SEPARATOR);
                }

                str = str
                        // 处理包
                        .replace("${PACKAGE}", getPackgStr(CoreConfig.GENERATE_POJO.PATH))
                        .replace("${PkgList}", StringUtils.join(importStr, LINE_SEPARATOR + "    "))
                        // 处理字段
                        .replace("${FieldList}", StringUtils.join(fieldStr, LINE_SEPARATOR + "    "));


                String tempPath = TEMP_PATH + pojoPath.replace(":", "${temp}") + tableInfo.getEntityName() + ".java";
                log.debug("write temp file to : {}", tempPath);
                FileUtil.writeUtf8String(str, tempPath);

                log.debug("Generate Pojo.java Success... table name => {}", tableInfo.getName());
            }


            // mapper.java
            if (CoreConfig.GENERATE_MAPPER.ENABLE) {
                log.debug("Generate Mapper.java ... table name => {}", tableInfo.getName());

                String mapperPath = getTargetPath(CoreConfig.GENERATE_MAPPER.PATH);
                Assert.isTrue(StrUtil.isNotBlank(mapperPath), "未获取到 mapper.java 路径");
                log.debug("Generate Mapper.java to : {}", mapperPath);

                File template = templateMap.get("mapper.java.mj");
                String str = FileUtil.readUtf8String(template)
                        .replace("${NAME}", tableInfo.getMapperName())
                        .replace("${POJONAME}", tableInfo.getEntityName())
                        .replace("${PACKAGE}", getPackgStr(CoreConfig.GENERATE_MAPPER.PATH))
                        .replace("${POJOPKG}", getPackgStr(CoreConfig.GENERATE_POJO.PATH) + "." + tableInfo.getEntityName())
                        .replace("${DATE}", DateUtil.format(new Date(), "yyyy/MM/dd HH:mm"))
                        .replace("${PROJECTNAME}", CoreConfig.PROJECT_NAME);

                String tempPath = TEMP_PATH + mapperPath.replace(":", "${temp}") + tableInfo.getMapperName() + ".java";
                log.debug("write temp file to : {}", tempPath);
                FileUtil.writeUtf8String(str, tempPath);

                log.debug("Generate Mapper.java Success... table name => {}", tableInfo.getName());
            }


            // mapper.xml
            if (CoreConfig.GENERATE_MAPPER_XML.ENABLE) {
                log.debug("Generate Mapper.xml ... table name => {}", tableInfo.getName());

                String mapperPath = getTargetPath(CoreConfig.GENERATE_MAPPER_XML.PATH);
                Assert.isTrue(StrUtil.isNotBlank(mapperPath), "未获取到 mapper.xml 路径");
                log.debug("Generate Mapper.xml to : {}", mapperPath);

                File template = templateMap.get("mapper.xml.mj");
                String str = FileUtil.readUtf8String(template)
                        .replace("${MapperJavaPkg}", getPackgStr(CoreConfig.GENERATE_MAPPER.PATH) + "." + tableInfo.getMapperName());

                String tempPath = TEMP_PATH + mapperPath.replace(":", "${temp}") + tableInfo.getMapperName() + ".xml";
                log.debug("write temp file to : {}", tempPath);
                FileUtil.writeUtf8String(str, tempPath);

                log.debug("Generate Mapper.xml Success... table name => {}", tableInfo.getName());
            }


            // service.java
            if (CoreConfig.GENERATE_SERVICE.ENABLE) {
                log.debug("Generate Service.java ... table name => {}", tableInfo.getName());

                // 先生成接口
                String iServicePath = getTargetPath(CoreConfig.GENERATE_SERVICE.PATH_IFACE);
                Assert.isTrue(StrUtil.isNotBlank(iServicePath), "未获取到 iservice.java 路径");
                log.debug("Generate IService.java to : {}", iServicePath);

                File iTemplate = templateMap.get("iservice.java.mj");
                String iStr = FileUtil.readUtf8String(iTemplate)
                        .replace("${NAME}", tableInfo.getServiceName())
                        .replace("${POJONAME}", tableInfo.getEntityName())
                        .replace("${PACKAGE}", getPackgStr(CoreConfig.GENERATE_SERVICE.PATH_IFACE))
                        .replace("${POJOPKG}", getPackgStr(CoreConfig.GENERATE_POJO.PATH) + "." + tableInfo.getEntityName())
                        .replace("${DATE}", DateUtil.format(new Date(), "yyyy/MM/dd HH:mm"))
                        .replace("${PROJECTNAME}", CoreConfig.PROJECT_NAME);

                String iTempPath = TEMP_PATH + iServicePath.replace(":", "${temp}") + tableInfo.getServiceName() + ".java";
                log.debug("write temp file to : {}", iTempPath);
                FileUtil.writeUtf8String(iStr, iTempPath);


                // 生成实现
                String implPath = getTargetPath(CoreConfig.GENERATE_SERVICE.PATH_IMPL);
                if (StrUtil.isBlank(implPath)) {
                    implPath = iServicePath + "impl" + PATH_SEPARATOR;
                    log.info("未获取到 serviceimpl.java 路径! 将使用默认路径 即 ${PATH_IFACE}/impl");
                }
                log.debug("Generate ServiceImpl.java to : {}", implPath);

                String implPkg = getPackgStr(CoreConfig.GENERATE_SERVICE.PATH_IMPL);
                if (StrUtil.isBlank(implPkg)) {
                    implPkg = getPackgStr(CoreConfig.GENERATE_SERVICE.PATH_IFACE) + ".impl";
                }

                File impltemplate = templateMap.get("serviceImpl.java.mj");
                String implStr = FileUtil.readUtf8String(impltemplate)
                        .replace("${NAME}", tableInfo.getServiceImplName())
                        .replace("${MapperName}", tableInfo.getMapperName())
                        .replace("${POJONAME}", tableInfo.getEntityName())
                        .replace("${IServiceName}", tableInfo.getServiceName())
                        .replace("${PACKAGE}", implPkg)
                        .replace("${MapperPkg}", getPackgStr(CoreConfig.GENERATE_MAPPER.PATH) + "." + tableInfo.getMapperName())
                        .replace("${PojoPkg}", getPackgStr(CoreConfig.GENERATE_POJO.PATH) + "." + tableInfo.getEntityName())
                        .replace("${IServicePkg}", getPackgStr(CoreConfig.GENERATE_SERVICE.PATH_IFACE) + "." + tableInfo.getServiceName())
                        .replace("${DATE}", DateUtil.format(new Date(), "yyyy/MM/dd HH:mm"))
                        .replace("${PROJECTNAME}", CoreConfig.PROJECT_NAME);

                String implTempPath = TEMP_PATH + implPath.replace(":", "${temp}") + tableInfo.getServiceImplName() + ".java";
                log.debug("write temp file to : {}", implTempPath);
                FileUtil.writeUtf8String(implStr, implTempPath);

                log.debug("Generate Service.java Success... table name => {}", tableInfo.getName());
            }


            // controller.java
            if (CoreConfig.GENERATE_CONTROLLER.ENABLE) {
                log.debug("Generate Controller.java ... table name => {}", tableInfo.getName());

                String controllerPath = getTargetPath(CoreConfig.GENERATE_CONTROLLER.PATH);
                Assert.isTrue(StrUtil.isNotBlank(controllerPath), "未获取到 controller.java 路径");
                log.debug("Generate Controller.java to : {}", controllerPath);

                File template = templateMap.get("controller.java.mj");
                String str = FileUtil.readUtf8String(template)
                        .replace("${NAME}", tableInfo.getControllerName())
                        .replace("${PACKAGE}", getPackgStr(CoreConfig.GENERATE_CONTROLLER.PATH))
                        .replace("${DATE}", DateUtil.format(new Date(), "yyyy/MM/dd HH:mm"))
                        .replace("${PROJECTNAME}", CoreConfig.PROJECT_NAME)
                        .replace("${URI}", tableInfo.getEntityPath())
                        .replace("${APIDESC}", StrUtil.isNotBlank(tableInfo.getComment()) ? tableInfo.getComment() : "自动生成Controller");

                String tempPath = TEMP_PATH + controllerPath.replace(":", "${temp}") + tableInfo.getControllerName() + ".java";
                log.debug("write temp file to : {}", tempPath);
                FileUtil.writeUtf8String(str, tempPath);

                log.debug("Generate Controller.java Success... table name => {}", tableInfo.getName());
            }
        }

        // 获取临时目录的文件，并全部移动到正式的目录下
        List<File> allFiles = FileUtil.loopFiles(TEMP_PATH);
        for (File file : allFiles) {
            String targetPath = file.getAbsolutePath().replace(TEMP_PATH, "").replace("${temp}", ":");
            FileUtil.move(file, new File(targetPath), true);
        }
    }


    /**
     * 根据路径，解析package
     */
    private String getPackgStr(String path) {
        if (StrUtil.isBlank(path)) return null;
        int start;
        if ((start = path.lastIndexOf("\\")) == -1 && (start = path.lastIndexOf("/")) == -1) {
            throw new RuntimeException("未解析到package名");
        }

        return path.substring(start + 1);
    }

    private String getPkgStr(String pkgName) {
        return "import " + pkgName + ";";
    }

    private String getFieldStr(TableField field) {
        return "private " + field.getPropertyType() + " " + field.getPropertyName() + ";";
    }

    /**
     * 如果路径的末尾没有添加 目录分隔符，那么就根据当前系统来添加一下分隔符
     * 并将包名转换为分隔符
     */
    private String getTargetPath(String path) {
        if (StrUtil.isBlank(path)) return null;
        path = path.replace(".", PATH_SEPARATOR);
        if (path.lastIndexOf("\\") != path.length() - 1 && path.lastIndexOf("/") != path.length() - 1) {
            path += PATH_SEPARATOR;
        }
        return path;
    }

    /**
     * 清空临时文件
     */
    private void cleanTempFile() {
        log.debug("清空临时文件 ...");
        FileUtil.del(TEMP_PATH);
    }

}
