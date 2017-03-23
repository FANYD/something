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
    new File(dir, className + "Dao.java").withPrintWriter { out -> generate(dirName, out, className, fields) }
}

def generate(dirName, out, className, fields) {
    out.println "/*\n" +
            " * Copyright (C) 2014-2016 Beijing BaoJiaBei Technology Co.,Ltd.\n" +
            " * \n" +
            " * All right reserved.\n" +
            " * \n" +
            " * This software is the confidential and proprietary\n" +
            " * information of BaoJiaBei Company of China. \n" +
            " * (\"Confidential Information\"). You shall not disclose\n" +
            " * such Confidential Information and shall use it only\n" +
            " * in accordance with the terms of the contract agreement \n" +
            " * you entered into with BaoJiaBei inc.\n" +
            " */"
    out.println ""
    out.println "package $dirName"
    out.println ""
    out.println "import com.baojiabei.provider.core.dao.IGenericDao;"
    out.println ""
    out.println "/**"
    out.println " * Created by FYD on " + new SimpleDateFormat("yyyy/MM/dd.").format(new Date())
    out.println " */"
    out.println "public interface $className" + "Dao extends IGenericDao<$className, " + fields[0].type + "> {"
    out.print "}"
}

def calcFields(table) {
    DasUtil.getColumns(table).reduce([]) { fields, col ->
        def spec = Case.LOWER.apply(col.getDataType().getSpecification())
        def typeStr = typeMapping.find { p, t -> p.matcher(spec).find() }.value
        fields += [[
                           name : javaName(col.getName(), false),
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