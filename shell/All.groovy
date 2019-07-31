import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

import java.text.SimpleDateFormat

/*
 * Created by FanYD on 2017.03.23
 */
copyRight = "/*\n" +
        " * Copyright (C) 2014-2020 Nuhtech Technology(Beijing) Co.,Ltd.\n" +
        " *\n" +
        " * All right reserved.\n" +
        " *\n" +
        " * This software is the confidential and proprietary\n" +
        " * information of Nuhtech Company of China.\n" +
        " * (\"Confidential Information\"). You shall not disclose\n" +
        " * such Confidential Information and shall use it only\n" +
        " * in accordance with the terms of the contract agreement\n" +
        " * you entered into with Nuhtech inc.\n" +
        " *\n" +
        " */"

typeMappingEntity = [
        (~/(?i)integer/)                  : "Integer",
        (~/(?i)bigint/)                   : "Long",
        (~/(?i)float|double|decimal|real/): "Double",
        (~/(?i)numeric/)                  : "BigDecimal",
        (~/(?i)datetime|timestamp/)       : "Timestamp",
        (~/(?i)date/)                     : "Date",
        (~/(?i)time/)                     : "Time",
        (~/(?i)/)                         : "String"
]
typeMappingCriteria = [
        (~/(?i)integer/)                  : "Integer",
        (~/(?i)bigint/)                   : "Long",
        (~/(?i)float|double|decimal|real/): "Double",
        (~/(?i)/)                         : "String"
]
typeMappingVo = [
        (~/(?i)integer/)                  : "Integer",
        (~/(?i)bigint/)                   : "Long",
        (~/(?i)float|double|decimal|real/): "Double",
        (~/(?i)datetime|timestamp/)       : "Timestamp",
        (~/(?i)date/)                     : "Date",
        (~/(?i)time/)                     : "Time",
        (~/(?i)/)                         : "String"
]
typeMappingVoConverter = [
        (~/(?i)integer/)                  : "Integer",
        (~/(?i)bigint/)                   : "Long",
        (~/(?i)float|double|decimal|real/): "Double",
        (~/(?i)datetime|timestamp/)       : "Timestamp",
        (~/(?i)date/)                     : "Date",
        (~/(?i)time/)                     : "Time",
        (~/(?i)/)                         : "String"
]
typeMappingInterface = [
        (~/(?i)integer/)                  : "Integer",
        (~/(?i)bigint/)                   : "Long",
        (~/(?i)float|double|decimal|real/): "Double",
        (~/(?i)datetime|timestamp/)       : "Timestamp",
        (~/(?i)date/)                     : "Date",
        (~/(?i)time/)                     : "Time",
        (~/(?i)/)                         : "String"
]
typeMappingXml = [
        (~/(?i)integer/)                  : "java.lang.Integer",
        (~/(?i)bigint/)                   : "java.lang.Long",
        (~/(?i)float|double|decimal|real/): "java.lang.Double",
        (~/(?i)datetime|timestamp/)       : "Timestamp",
        (~/(?i)date/)                     : "Date",
        (~/(?i)time/)                     : "Time",
        (~/(?i)/)                         : "java.lang.String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it, dir) }
}

def generate(table, dir) {
    def dirName = dir.toString().substring(dir.toString().indexOf("java") + 5).replace("/", ".") + ";"
    def dirNameCriteria = dirName.replace("entity", "criteria")
    def dirNameVo = dirName.replace("entity", "vo")
    def dirNameVoConverter = dirName.replace("entity", "vo").replace("facade", "provider").replace("facade", "provider").replace(";", "")
    def dirNameDao = dirName.replace("entity", "dao").replace("facade", "provider").replace("facade", "provider").replace(";", "")
    def className = javaClassName(table.getName(), true)
    def fieldsEntity = calcFieldsEntity(table)
    def fieldsCriteria = calcFieldsCriteria(table)
    def fieldsVo = calcFieldsVo(table)
    def fieldsVoConverter = calcFieldsVoConverter(table)
    def fieldsInterface = calcFieldsInterface(table)
    def fieldsXml = calcFieldsXml(table)
    dir = dir.toString()
    def dirCriteria = dir.toString().replace("entity", "criteria");
    def dirVo = dir.toString().replace("entity", "vo");
    def dirVoConverter = dir.toString().replace("entity", "vo").replace("facade", "provider").replace("facade", "provider")
    def dirDao = dir.toString().replace("entity", "dao").replace("facade", "provider").replace("facade", "provider")
    new File(dir).mkdirs()
    new File(dir, className + ".java").withPrintWriter { out -> generateEntity(dirName, out, className, fieldsEntity) }
    new File(dirCriteria).mkdirs()
    new File(dirCriteria, className + "Criteria.java").withPrintWriter { out -> generateCriteria(dirNameCriteria, out, className, fieldsCriteria) }
    new File(dirVo).mkdirs()
    new File(dirVo, className + "Vo.java").withPrintWriter { out -> generateVo(dirNameVo, out, className, fieldsVo) }
    new File(dirVoConverter).mkdirs()
    new File(dirVoConverter, className + "VoConverter.java").withPrintWriter { out -> generateVoConverter(dirNameVoConverter, out, className, fieldsVoConverter) }
    new File(dirDao).mkdirs()
    new File(dirDao, className + "Dao.java").withPrintWriter { out -> generateInterface(dirNameDao, out, className, fieldsInterface) }
    new File(dirDao).mkdirs()
    new File(dirDao, className + "Dao.xml").withPrintWriter { out -> generateXml(dirNameDao, table, out, className, fieldsXml) }
}

def generateEntity(dirName, out, className, fieldsEntity) {
    out.println copyRight
    out.println ""
    out.println "package $dirName"
    out.println ""
    def timestampExit = false
    def dateExit = false
    def timeExit = false
    fieldsEntity.each() {
        if (it.type == "Timestamp") {
            timestampExit = true
        }
        if (it.type == "Date") {
            dateExit = true
        }
        if (it.type == "Time") {
            timeExit = true
        }
    }
    out.println "import java.io.Serializable;"
    if (timestampExit) {
        out.println "import java.sql.Timestamp;"
    }
    if (dateExit) {
        out.println "import java.sql.Date;"
    }
    if (timeExit) {
        out.println "import java.sql.Time;"
    }
    out.println ""
    out.println "/**"
    out.println " * Created by FanYD on " + new SimpleDateFormat("yyyy/MM/dd.").format(new Date())
    out.println " */"
    out.println "public class $className implements Serializable {"
    out.println ""
    out.println "    private static final long serialVersionUID = " + generateUID() + ";"
    fieldsEntity.each() {
        out.println ""
        if (it.annos != "") out.println "  ${it.annos}"
        out.println "    private ${it.type} ${it.name};"
    }
    fieldsEntity.each() {
        out.println ""
        out.println "    public ${it.type} get${it.name.capitalize()}() {"
        out.println "        return ${it.name};"
        out.println "    }"
        out.println ""
        out.println "    public void set${it.name.capitalize()}(${it.type} ${it.name}) {"
        out.println "        this.${it.name} = ${it.name};"
        out.println "    }"
    }
    out.println ""
    out.println "    @Override"
    out.println "    public String toString() {"
    out.println "        return \"${className}{\" +"
    out.println "                \"${fieldsEntity[0].name}=\" + ${fieldsEntity[0].name} +"
    fieldsEntity.each() {
        if (it.name != fieldsEntity[0].name) {
            out.print "                \" ,${it.name}="
            if (it.type == "String") {
                out.println "\'\" + ${it.name} + \'\\\'\' +"
            } else {
                out.println "\" + ${it.name} +"
            }
        }
    }
    out.println "                \'}\';"
    out.println "    }"
    out.print "}"
}

def generateCriteria(dirNameCriteria, out, className, fieldsCriteria) {
    out.println copyRight
    out.println ""
    out.println "package $dirNameCriteria"
    out.println ""
    out.println "import com.nuhtech.medchat.facade.core.criteria.AbstractCriteria;"
    out.println ""
    out.println "/**"
    out.println " * Created by FanYD on " + new SimpleDateFormat("yyyy/MM/dd.").format(new Date())
    out.println " */"
    out.println "public class $className" + "Criteria extends AbstractCriteria {"
    out.println ""
    out.println "    private static final long serialVersionUID = " + generateUID() + ";"
    fieldsCriteria.each() {
        out.println ""
        if (it.annos != "") out.println "  ${it.annos}"
        out.println "    private ${it.type} ${it.name};"
    }
    fieldsCriteria.each() {
        out.println ""
        out.println "    public ${it.type} get${it.name.capitalize()}() {"
        out.println "        return ${it.name};"
        out.println "    }"
        out.println ""
        out.println "    public void set${it.name.capitalize()}(${it.type} ${it.name}) {"
        out.println "        this.${it.name} = ${it.name};"
        out.println "    }"
    }
    out.println ""
    out.println "    @Override"
    out.println "    public String toString() {"
    out.println "        return \"${className}{\" +"
    out.println "                \"${fieldsCriteria[0].name}=\" + ${fieldsCriteria[0].name} +"
    fieldsCriteria.each() {
        if (it.name != fieldsCriteria[0].name) {
            out.print "                \",${it.name}="
            if (it.type == "String") {
                out.println "\'\" + ${it.name} + \'\\\'\' +"
            } else {
                out.println "\" + ${it.name} +"
            }
        }
    }
    out.println "                \'}\';"
    out.println "    }"
    out.print "}"
}

def generateVo(dirNameVo, out, className, fieldsVo) {
    out.println copyRight
    out.println ""
    out.println "package $dirNameVo"
    out.println ""
    out.println "import java.io.Serializable;"
    out.println ""
    def fieldMetaExit = false
    fieldsVo.each() {
        if (it.type == "Timestamp" || it.type == "Date") {
            fieldMetaExit = true
        }
    }
    if (fieldMetaExit) {
        out.println "import com.nuhtech.medchat.core.vo.FieldMeta;"
        out.println "import com.nuhtech.medchat.core.vo.FieldType;"
        out.println ""
    }
    fieldsVo.each() {
        if (it.name == "createDatetime") {
        }
    }
    out.println "/**"
    out.println " * Created by FanYD on " + new SimpleDateFormat("yyyy/MM/dd.").format(new Date())
    out.println " */"
    out.println "public class $className" + "Vo implements Serializable {"
    out.println ""
    out.println "    private static final long serialVersionUID = " + generateUID() + ";"
    fieldsVo.each() {
        out.println ""
        if (it.annos != "") out.println "    ${it.annos}"
        out.println "    private String ${it.name};"
    }
    fieldsVo.each() {
        out.println ""
        out.println "    public String get${it.name.capitalize()}() {"
        out.println "        return ${it.name};"
        out.println "    }"
        out.println ""
        out.println "    public void set${it.name.capitalize()}(String ${it.name}) {"
        out.println "        this.${it.name} = ${it.name};"
        out.println "    }"
    }
    out.println ""
    out.println "    @Override"
    out.println "    public String toString() {"
    out.println "        return \"${className}{\" +"
    out.println "                \"${fieldsVo[0].name}=\" + ${fieldsVo[0].name} +"
    fieldsVo.each() {
        if (it.name != fieldsVo[0].name) {
            out.print "                \",${it.name}="
            if (it.type == "String") {
                out.println "\'\" + ${it.name} + \'\\\'\' +"
            } else {
                out.println "\" + ${it.name} +"
            }
        }
    }
    out.println "                \'}\';"
    out.println "    }"

    out.print "}"
}

def generateVoConverter(dirNameVoConverter, out, className, fieldsVoConverter) {
    def entityImport = "import " + dirNameVoConverter.replace("vo", "entity").replace("provider", "facade").replace("provider", "facade") + ".$className;"
    def voImport = "import " + dirNameVoConverter.replace("provider", "facade").replace("provider", "facade") + ".$className" + "Vo;"
    out.println copyRight
    out.println ""
    out.println "package $dirNameVoConverter;"
    out.println ""
    out.println "import com.nuhtech.medchat.facade.core.vo.VoConfig;"
    out.println "import com.nuhtech.medchat.facade.core.vo.VoConverter;"
    out.println entityImport
    out.println voImport
    out.println "/**"
    out.println " * Created by FanYD on " + new SimpleDateFormat("yyyy/MM/dd.").format(new Date())
    out.println " */"
    out.println "public class $className" + "VoConverter extends VoConverter<$className" + "Vo, $className> {"
    out.println ""
    out.println "    @Override"
    out.println "    public $className" + "Vo convert($className bo, VoConfig config) {"
    out.println "        $className" + "Vo vo = new $className" + "Vo();"
    out.println "        this.autoConvert(vo, bo);"
    out.println "        return vo;"
    out.println "    }"
    out.print "}"
}

def generateInterface(dirNameDao, out, className, fieldsInterface) {
    def entityImport = "import " + dirNameDao.replace("dao", "entity").replace("provider", "facade").replace("provider", "facade") + ".$className;"
    out.println copyRight
    out.println ""
    out.println "package $dirNameDao;"
    out.println ""
    out.println entityImport
    out.println "import com.nuhtech.medchat.provider.core.dao.IGenericDao;"
    out.println ""
    out.println "/**"
    out.println " * Created by FanYD on " + new SimpleDateFormat("yyyy/MM/dd.").format(new Date())
    out.println " */"
    out.println "public interface $className" + "Dao extends IGenericDao<$className, " + fieldsInterface[0].type + "> {"
    out.print "}"
}

def generateXml(dirNameDao, table, out, className, fieldsXml) {
    //maxLength
    def maxLength = 0;
    def isHis = false;
    fieldsXml.each() {
        if (it.sqlName.length() > maxLength) {
            maxLength = it.sqlName.length()
        }
        if (it.name == ("importDatetime")) {
            isHis = true;
        }
    }
    maxLength = maxLength + 4

    out.println "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    out.println "<!DOCTYPE mapper"
    out.println "    PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\""
    out.println "    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
    out.println "<mapper namespace=\"$dirNameDao.$className" + "Dao\">"

    //resultMap
    out.println "    <resultMap id=\"result$className" + "\" type=\"$className\">"
    fieldsXml.each() {
        if (it.sqlName != fieldsXml[0].sqlName) {
            out.print "        <result property=\"${it.name}\""
            for (int i = 0; i < maxLength - it.name.length(); i++) {
                out.print " "
            }
            out.print "column=\"${it.sqlName}\""
            for (int i = 0; i < maxLength - it.sqlName.length(); i++) {
                out.print " "
            }
            out.println "/>"
        } else {
            out.print "        <id     property=\"${it.name}\""
            for (int i = 0; i < maxLength - it.name.length(); i++) {
                out.print " "
            }
            out.print "column=\"${it.sqlName}\""
            for (int i = 0; i < maxLength - it.sqlName.length(); i++) {
                out.print " "
            }
            out.println "/>"

        }
    }
    out.println "    </resultMap>"

    //column
    out.println ""
    out.println "    <sql id=\"column\">"
    out.print "        "
    fieldsXml.each() {
        if (it.sqlName != fieldsXml[0].sqlName) {
            out.print "${it.sqlName}"
            if (it.sqlName != fieldsXml[fieldsXml.size() - 1].sqlName) {
                out.print ", "
            }
        }
    }
    out.println ""
    out.println "    </sql>"

    //getById
    out.println ""
    out.println "    <select id=\"getById\" parameterType=\"" + fieldsXml[0].type + "\" resultMap=\"result$className\">"
    out.println "        select " + fieldsXml[0].sqlName + ","
    out.println "        <include refid=\"column\"/>"
    out.println "        from " + table.getName()
    out.println "        where " + fieldsXml[0].sqlName + " = #{id}"
    out.println "        <if test=\"forUpdate==true\">"
    out.println "            for update of " + table.getName()
    out.println "        </if>"
    out.println "    </select>"

    //insert
    out.println ""
    out.println "    <insert id=\"insert\" parameterType=\"$className\" keyProperty=\"" + fieldsXml[0].name + "\""
    out.println "            useGeneratedKeys=\"true\">"
    out.println "        insert into " + table.getName() + " ("
    out.println "        <include refid=\"column\"/>"
    out.println "        ) values ("
    fieldsXml.each() {
        if (it.sqlName != fieldsXml[0].sqlName) {
            if (isHis == false) {
                if (it.name != ("createDatetime") && it.name != ("updateDatetime")) {
                    out.print "        #{${it.name}}"
                } else {
                    out.print "        current_timestamp"
                }
            }
            if (isHis == true) {
                if (it.name != ("importDatetime")) {
                    out.print "        #{${it.name}}"
                } else {
                    out.print "        current_timestamp"
                }
            }
            if (it.sqlName != fieldsXml[fieldsXml.size() - 1].sqlName) {
                out.println ","
            }
        }
    }
    out.println ""
    out.println "        )"
    out.println "    </insert>"

    //update
    if (isHis == false) {
        out.println ""
        out.println "    <update id=\"update\" parameterType=\"$className\">"
        out.println "        update " + table.getName()
        out.println "        set"
        fieldsXml.each() {
            if (it.sqlName != fieldsXml[0].sqlName) {
                if (it.name != ("updateDatetime")) {
                    if (it.name != ("createDatetime")) {
                        out.print "        ${it.sqlName}"
                        for (int i = 0; i < maxLength - it.sqlName.length(); i++) {
                            out.print " "
                        }
                        out.print "= #{${it.name}}"
                    }
                } else {
                    out.print "        ${it.sqlName}"
                    for (int i = 0; i < maxLength - it.sqlName.length(); i++) {
                        out.print " "
                    }
                    out.print "= current_timestamp"
                }
                if (it.sqlName != fieldsXml[fieldsXml.size() - 1].sqlName && it.sqlName != fieldsXml[fieldsXml.size() - 2].sqlName) {
                    out.println ","
                }
            }
        }
        out.println ""
        out.println "        where " + fieldsXml[0].sqlName + " = #{" + fieldsXml[0].name + "}"
        out.println "    </update>"
    }

    //delete
    out.println ""
    out.println "    <delete id=\"delete\" parameterType=\"" + fieldsXml[0].type + "\">"
    out.println "        delete from " + table.getName()
    out.println "        where " + fieldsXml[0].sqlName + " = #{" + fieldsXml[0].name + "}"
    out.println "    </delete>"

    //queryCount
    out.println ""
    out.println "    <select id=\"queryCount\" resultType=\"java.lang.Integer\" parameterType=\"$className" + "Criteria\">"
    out.println "        select count(" + fieldsXml[0].sqlName + ")"
    out.println "        from " + table.getName()
    out.println "        <include refid=\"conditions\"/>"
    out.println "    </select>"

    //query
    out.println ""
    out.println "    <select id=\"query\" resultMap=\"result$className\" parameterType=\"$className" + "Criteria\">"
    out.println "        select " + fieldsXml[0].sqlName + ","
    out.println "        <include refid=\"column\"/>"
    out.println "        from " + table.getName()
    out.println "        <include refid=\"conditions\"/>"
    fieldsXml.each() {
        if (it.name == ("createDatetime")) {
            out.println "        order by create_datetime desc"
        }
    }
    out.println "        <if test=\"paging==true\">"
    out.println "            limit #{limit} offset #{offset}"
    out.println "        </if>"
    out.println "    </select>"

    //conditions
    out.println ""
    out.println "    <sql id=\"conditions\">"
    out.println "        <where>"
    fieldsXml.each() {
        out.println "            <if test=\"${it.name} != null and ${it.name} != ''\">"
        out.println "                and ${it.sqlName} = #{${it.name}}"
        out.println "            </if>"
    }
    out.println "        </where>"
    out.println "    </sql>"

    out.println "</mapper>"
}

def calcFieldsEntity(table) {
    DasUtil.getColumns(table).reduce([]) { fieldsEntity, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMappingEntity.find { p, t -> p.matcher(spec).find() }.value
        fieldsEntity += [[
                                 name : javaName(col.getName(), false),
                                 type : typeStr,
                                 annos: ""]]
    }
}

def calcFieldsCriteria(table) {
    DasUtil.getColumns(table).reduce([]) { fieldsCriteria, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMappingCriteria.find { p, t -> p.matcher(spec).find() }.value
        fieldsCriteria += [[
                                   name : javaName(col.getName(), false),
                                   type : typeStr,
                                   annos: ""]]
    }
}

def calcFieldsVo(table) {
    DasUtil.getColumns(table).reduce([]) { fieldsVo, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMappingVo.find { p, t -> p.matcher(spec).find() }.value
        fieldsVo += [[
                             name : javaName(col.getName(), false),
                             type : typeStr,
                             annos: annotation(typeStr.toString())]]
    }
}

def calcFieldsVoConverter(table) {
    DasUtil.getColumns(table).reduce([]) { fieldsVoConverter, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMappingVoConverter.find { p, t -> p.matcher(spec).find() }.value
        fieldsVoConverter += [[
                                      name : javaName(col.getName(), false),
                                      type : typeStr,
                                      annos: ""]]
    }
}

def calcFieldsInterface(table) {
    DasUtil.getColumns(table).reduce([]) { fieldsInterface, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMappingInterface.find { p, t -> p.matcher(spec).find() }.value
        fieldsInterface += [[
                                    name : javaName(col.getName(), false),
                                    type : typeStr,
                                    annos: ""]]
    }
}

def calcFieldsXml(table) {
    DasUtil.getColumns(table).reduce([]) { fieldsXml, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMappingXml.find { p, t -> p.matcher(spec).find() }.value
        fieldsXml += [[
                              name   : javaName(col.getName(), false),
                              sqlName: col.getName(),
                              type   : typeStr,
                              annos  : ""]]
    }
}

def javaName(str, capitalize) {
    def s = str.split(/[^\p{Alnum}]/).collect { def s = Case.LOWER.apply(it).capitalize() }.join("")
    capitalize ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}

def javaClassName(str, capitalize) {
    def s = str.split(/[^\p{Alnum}]/)
    List list = s.toList()
    list.remove(s[0])
    s = list.collect { s = Case.LOWER.apply(it).capitalize() }.join("")
    capitalize ? s : Case.LOWER.apply(s[0]) + s[1..-1]
}

def annotation(String str) {
    def s = ""
    if (str == "Timestamp") {
        s = "@FieldMeta(type = FieldType.DATETIME)"
    } else if (str == "Date") {
        s = "@FieldMeta(type = FieldType.DATE)"
    } else {
        s = ""
    }
}

private static String generateUID() {
    StringBuffer id = new StringBuffer("")

    // 符号位
    Random r = new Random()
    int flag = r.nextInt(2) % 2
    if (flag == 1) {
        id.append("-")
    }

    // 首位
    r = new Random()
    int first = r.nextInt(9) % 9 + 1
    id.append(first)

    // 次位
    int second
    r = new Random()
    if (first == 9) { // 防止数值越界
        second = r.nextInt(2) % 2
    } else {
        second = r.nextInt(9) % 10
    }
    id.append(second)

    // 其余位
    int max = 9
    int min = 0
    for (int i = 0; i < 17; i++) {
        Random random = new Random()
        int s = random.nextInt(max) % (max - min + 1) + min
        id.append(s)
    }
    id.append("L")

    return id.toString()
}
