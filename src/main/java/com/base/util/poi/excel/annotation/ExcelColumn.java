package com.base.util.poi.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 	excel字段属性设置注解
 * @ClassName: ExcelColumn
 * @Description: TODO
 * @author dh
 * @date 2020年9月7日
 *
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelColumn {


	/**
	 * 表头对应的名称
	 * @return String
	 */
	String columnName() default "";

	/**
	 * 字段在excel中的顺序对应
	 * @return int
	 */
	int index();

	/**
	 * 字段是否可以为空默认可以为空
	 * @return boolean
	 */
	boolean nullable() default true;

	/**
	 * 单元格的宽度
	 * @return int
	 */
	int columnWidth() default 20;

	/**
	 * 是否需要合并单元格
	 * @return boolean
	 */
	boolean needMergeCell() default false;


}
