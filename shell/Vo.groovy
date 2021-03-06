
import com.intellij.database.model.DasTable
import com.intellij.database.model.ObjectKind
import com.intellij.database.util.Case
import com.intellij.database.util.DasUtil

import java.text.SimpleDateFormat

/*
 * Created by FanYD on 2017.03.23
 */

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
    def dirName = dir.toString().substring(dir.toString().indexOf("java") + 5).replace("\\", ".") + ";"
    def className = javaClassName(table.getName(), true)
    def fields = calcFields(table)
    new File(dir, className + "Vo.java").withPrintWriter { out -> generate(dirName, out, className, fields) }
}

def generate(dirName, out, className, fields) {
    out.println "/*\n" +
            " * Copyright (C) 2014-2020 Nuhtech Technology(Beijing) Co.,Ltd.\n" +
            " * \n" +
            " * All right reserved.\n" +
            " * \n" +
            " * This software is the confidential and proprietary\n" +
            " * information of Nuhtech Company of China. \n" +
            " * (\"Confidential Information\"). You shall not disclose\n" +
            " * such Confidential Information and shall use it only\n" +
            " * in accordance with the terms of the contract agreement \n" +
            " * you entered into with Nuhtech inc.\n" +
            " * \n" +
            " */"
    out.println ""
    out.println "package $dirName"
    out.println ""
    out.println "import java.io.Serializable;"
    out.println ""
    def fieldMetaExit = false
    fields.each() {
        if (it.type == "Timestamp" || it.type == "Date") {
            fieldMetaExit = true
        }
    }
    if (fieldMetaExit) {
        out.println "import com.nuhtech.medchat.core.vo.FieldMeta;"
        out.println "import com.nuhtech.medchat.core.vo.FieldType;"
        out.println ""
    }
    fields.each() {
        if (it.name == "createDatetime") {
        }
    }
    out.println "/**"
    out.println " * Created by FanYD on " + new SimpleDateFormat("yyyy/MM/dd.").format(new Date())
    out.println " */"
    out.println "public class $className" + "Vo implements Serializable {"
    fields.each() {
        out.println ""
        if (it.annos != "") out.println "    ${it.annos}"
        out.println "    private String ${it.name};"
    }
    fields.each() {
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
    out.println "                \"${fields[0].name}=\" + ${fields[0].name} +"
    fields.each() {
        if (it.name != fields[0].name) {
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

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name : javaName(col.getName(), false),
                           type : typeStr,
                           annos: annotation(typeStr.toString())]]
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