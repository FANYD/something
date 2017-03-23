import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

/*
 * Created by FanYD on 2017.03.23
 */

packageName = "com.baojiabei.facade.entity.;"
typeMapping = [
        (~/(?i)integer/)                  : "Integer",
        (~/(?i)bigint/)                   : "Long",
        (~/(?i)float|double|decimal|real/): "Double",
        (~/(?i)datetime|timestamp/)       : "Timestamp",
        (~/(?i)date/)                     : "Date",
        (~/(?i)time/)                     : "Time",
        (~/(?i)/)                         : "String"
]

FILES.chooseDirectoryAndSave("Choose directory", "Choose where to store generated files") { dir ->
    SELECTION.filter { it instanceof DasTable && it.getKind() == ObjectKind.TABLE }.each { generate(it, dir) }
}

def generate(table, dir) {
    def dirName = dir.toString().substring(dir.toString().indexOf("java") + 5).replace("\\", ".")
    def className = javaClassName(table.getName(), true)
    def fields = calcFields(table)
    new File(dir, className + "Dao.xml").withPrintWriter { out -> generate(dirName, table, out, className, fields) }
}

def generate(dirName, table, out, className, fields) {
    //maxLength
    def maxLength = 0;
    fields.each() {
        if (it.sqlName.length() > maxLength) {
            maxLength = it.sqlName.length()
        }
    }
    maxLength = maxLength + 4

    out.println "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
    out.println "<!DOCTYPE mapper"
    out.println "    PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\""
    out.println "    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">"
    out.println "<mapper namespace=\"$dirName.$className" + "Dao\">"

    //resultMap
    out.println "    <resultMap id=\"result$className" + "\" type=\"$className\">"
    fields.each() {
        if (it.sqlName != fields[0].sqlName) {
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
    fields.each() {
        if (it.sqlName != fields[0].sqlName) {
            out.print "${it.sqlName}"
            if (it.sqlName != fields[fields.size() -1].sqlName) {
                out.print ", "
            }
        }
    }
    out.println ""
    out.println "    </sql>"

    //getById
    out.println ""
    out.println "    <select id=\"getById\" parameterType=\"" + fields[0].type + "\" resultMap=\"result$className\">"
    out.println "        select " + fields[0].sqlName + ","
    out.println "        <include refid=\"column\"/>"
    out.println "        from " + table.getName()
    out.println "        where " + fields[0].sqlName + " = #{id}"
    out.println "        <if test=\"forUpdate==true\">"
    out.println "            for update of " + table.getName()
    out.println "        </if>"
    out.println "    </select>"

    //insert
    out.println ""
    out.println "    <insert id=\"insert\" parameterType=\"$className\" keyProperty=\"" + fields[0].name + "\""
    out.println "            useGeneratedKeys=\"true\">"
    out.println "        insert into " + table.getName() + " ("
    out.println "        <include refid=\"column\"/>"
    out.println "        ) values ("
    fields.each() {
        if (it.sqlName != fields[0].sqlName) {
            if (it.name != ("createDatetime") && it.name != ("updateDatetime")) {
                out.print "        #{${it.name}}"
            } else {
                out.print "        current_timestamp"
            }
            if (it.sqlName != fields[fields.size() -1].sqlName) {
                out.println ","
            }
        }
    }
    out.println ""
    out.println "    </insert>"

    //update
    out.println ""
    out.println "    <update id=\"update\" parameterType=\"$className\">"
    out.println "        update " + table.getName()
    out.println "        set"
    fields.each() {
        if (it.sqlName != fields[0].sqlName && it.name != ("createDatetime")) {
            if (it.name != ("updateDatetime")) {
                out.print "        ${it.sqlName}"
                for (int i = 0; i < maxLength - it.sqlName.length(); i++) {
                    out.print " "
                }
                out.print "= #{${it.name}}"
            } else {
                out.print "        ${it.sqlName}"
                for (int i = 0; i < maxLength - it.sqlName.length(); i++) {
                    out.print " "
                }
                out.print "= current_timestamp"
            }
            if (it.sqlName != fields[fields.size() -1].sqlName) {
                out.println ","
            }
        }
    }
    out.println ""
    out.println "    </update>"

    //delete
    out.println ""
    out.println "    <delete id=\"delete\" parameterType=\"" + fields[0].type + "\">"
    out.println "        delete from " + table.getName()
    out.println "        where " + fields[0].sqlName + " = #{" + fields[0].name + "}"
    out.println "    </delete>"

    //queryCount
    out.println ""
    out.println "    <select id=\"queryCount\" resultType=\"java.lang.Integer\" parameterType=\"$className" + "Criteria\">"
    out.println "        select count(" + fields[0].sqlName + ")"
    out.println "        from " + table.getName()
    out.println "        <include refid=\"conditions\"/>"
    out.println "    </select>"

    //query
    out.println ""
    out.println "    <select id=\"query\" resultMap=\"result$className\" parameterType=\"$className" + "Criteria\">"
    out.println "        select " + fields[0].sqlName + ","
    out.println "        <include refid=\"column\"/>"
    out.println "        from " + table.getName()
    out.println "        <include refid=\"conditions\"/>"
    fields.each() {
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
    fields.each() {
        out.println "            <if test=\"${it.name} != null and ${it.name} != ''\">"
        out.println "                and ${it.sqlName} = #{${it.name}}"
        out.println "            </if>"
    }
    out.println "        </where>"
    out.println "    </sql>"

    out.println "</mapper>"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name : javaName(col.getName(), false),
                           sqlName : col.getName(),
                           type : typeStr,
                           annos: ""]]
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