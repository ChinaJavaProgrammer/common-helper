package com.base.util.poi.excel.annotation;

import com.base.util.poi.excel.ExcelCreator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 	excel文件属性设置注解
 * @ClassName: ExcelInfo
 * @Description: TODO
 * @author dh
 * @date 2020年9月7日
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelInfo {

	/**
	 * 表示当前这个excel映射信息作用域那些sheet页
	 * @return int[]
	 */
	int[] sheetNum() default 0;

	/**
	 * excel的类型，导出的时候必填，导入的时候可以不管
	 * @return String
	 */
	String excelType() default ExcelCreator.XLSX;



	/**
	 *  是否需要排序
	 * @return boolean
	 */
	boolean needOrder() default false;

	/**
	 * 标题行高度
	 * @return int
	 */
	int titleHeight() default 2000;

	/**
	 * 表头行高度
	 * @return int
	 */
	int headerHeight() default 2000;

	/**
	 * 开始读取的行
	 * @return int
	 */
	int startRow() default 0;

	/**
	 * 不解析的列号
	 * @return int []
	 */
	int [] exceptColumnNum () default {};

	/**
	 * sheet页名称
	 * @return String
	 */
	String sheetName() default "";


	/**
	 * 如果是excel导出需要从url下载图片并放入excel的单元格中那么，可以指定超时时间默认是2000ms超时
	 * @return int
	 */
	int imageReadTimeOut() default 2000;

	/**
	 * 设置图片地址分隔符，只针对导出多张图片的情况，其他情况无效
	 * @return String
	 */
	String imageSeparator() default "";


	/**
	 * 图片嵌入单元格的类型，一共三种
	 * @return int
	 */
	int pictureInnerType() default ExcelCreator.MOVE_AND_RESIZE;
}
