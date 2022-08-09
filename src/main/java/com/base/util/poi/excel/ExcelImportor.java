package com.base.util.poi.excel;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.*;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.imageio.ImageIO;

import com.alibaba.fastjson.JSONObject;
import com.base.util.poi.ExcelCustomModel;
import org.apache.commons.net.ntp.TimeStamp;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;

import com.base.util.common.CommonUtil;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;


/**
 * excel导入工具类
 * 
 * @ClassName: ExcelImportor
 * @Description: TODO
 * @author dh
 * @date 2020年6月8日
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ExcelImportor {

	/** 文件输入流 */
	private InputStream in;

	/** 用于辨别传入文件的类型 */
	private byte[] header = new byte[8];

	/** 当前excel对象 */
	private Workbook workbook;

	/** 总共的sheet页数量 */
	private LinkedList<Sheet> sheets = new LinkedList<>();

	/** sheet对象 */
	private Sheet sheet;

	/** 行 */
	private Row row;

	/** 单元格 */
	private Cell cell;

	/** 从第几行开始取数据 */
	private int startRow = 1;

	/** 每个sheet页的开始行 */
	private LinkedList<Integer> sheetStartRow = new LinkedList<>();

	/** 列信息设置 */
	private LinkedList<LinkedList<ExcelModel>> columnNameList = new LinkedList<>();

	/** 不解析的列下标 */
	private Map<Integer, List<Integer>> exceptColumnNumMap = new HashMap<>();

	/** 解析出来的数据 */
	private LinkedList<LinkedList<Map<String, Object>>> datas = new LinkedList<>();

	/** 错误消息 */
	private StringBuilder errorMessage = new StringBuilder();

	private FormulaEvaluator formulaEvaluator;


	public ExcelImportor(Object excelModel){

	}

	public ExcelImportor(InputStream in) {
		this(in,null);
	}

	public  ExcelImportor(InputStream in,Object excelModel){
		try {
			ByteArrayOutputStream outPut = cloneInputStream(in);
			ByteArrayInputStream byteIn = new ByteArrayInputStream(outPut.toByteArray());
			IOUtils.readFully(byteIn, header);
			this.in = new ByteArrayInputStream(outPut.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

		init();
		if(excelModel!=null)
			handleExcelModel(excelModel);
	}


	/**
	 * 处理Excel注解模型
	 * @param excelModel
	 */
	private void handleExcelModel(Object excelModel){


	}

	public String getWorkBookType(){
		String type=null;
		try {
			ByteArrayInputStream heanerIn = new ByteArrayInputStream(header);
			if (POIFSFileSystem.hasPOIFSHeader(heanerIn)) {
				type = "xls";
			}
			if (POIXMLDocument.hasOOXMLHeader(heanerIn)) {
				type="xlsx";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return type;
	}

	/**
	 * 获取解析出错的原因
	 * 
	 * @Title: getErrorMessage
	 * @Description: TODO
	 * @return
	 * @return String
	 */
	public String getErrorMessage() {
		return errorMessage.toString();
	}

	/**
	 * 克隆字节输入流
	 * 
	 * @Title: cloneInputStream
	 * @Description: TODO
	 * @param input
	 * @return
	 * @return ByteArrayOutputStream
	 */
	private ByteArrayOutputStream cloneInputStream(InputStream input) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len;
			while ((len = input.read(buffer)) > -1) {
				baos.write(buffer, 0, len);
			}
			baos.flush();
			return baos;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 初始化excel
	 * 
	 * @Title: init
	 * @Description: TODO
	 * @return void
	 */
	private void init() {

		try {
			workbook = WorkbookFactory.create(in);
			ByteArrayInputStream heanerIn = new ByteArrayInputStream(header);
			if (POIFSFileSystem.hasPOIFSHeader(heanerIn)) {
				formulaEvaluator = new HSSFFormulaEvaluator((HSSFWorkbook) workbook);
			}
			if (POIXMLDocument.hasOOXMLHeader(heanerIn)) {
				formulaEvaluator = new XSSFFormulaEvaluator((XSSFWorkbook) workbook);
			}
			int sheetNum = workbook.getNumberOfSheets();
			for (int i = 0; i < sheetNum; i++) {
				Sheet sheetAt = workbook.getSheetAt(i);
				if(!sheetAt.getSheetName().equals("下拉常量数据")){
					sheets.add(sheetAt);
				}
			}
		} catch (InvalidFormatException | IOException e) {
			e.printStackTrace();
		}
	}

	public String getSheetName(int i){
		return workbook.getSheetName(i);
	}

	/**
	 * 解析Excel文件
	 * 
	 * @Title: analysisExcel
	 * @Description: TODO
	 * @return void
	 */
	public boolean analysisExcel() {
		// 遍历每一个sheet然后取出数据
		for (int s = 0; s < sheets.size(); s++) {
			List<Integer> exceptColumnNum = exceptColumnNumMap.get(s);
			sheet = sheets.get(s);
			if (s < sheetStartRow.size()) {
				startRow = sheetStartRow.get(s);
			}
			int realRows = sheet.getLastRowNum();
			if(startRow > realRows){
				datas.add(new LinkedList<>());
				continue;
			}
			startRow = Math.min(startRow, sheet.getLastRowNum());
			int lastRowNum = sheet.getLastRowNum();
			LinkedList<Map<String, Object>> rowData = new LinkedList<>();
			List<ExcelCustomModel> excelCustomModels = new ArrayList<>();
			for (int r = startRow; r <= lastRowNum; r++) {
				row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				short minColIx = row.getFirstCellNum();
				short maxColIx = row.getLastCellNum();
				if(minColIx <0 || maxColIx < 0){
					continue;
				}
				if(exceptColumnNum!=null && maxColIx<exceptColumnNum.size()) {
					errorMessage.append("excel列数和设置的列数不符");
					return false;
				}
				int physicalIndex = 0;
				Map<String, Object> cellData = new LinkedHashMap<>();
				Map<String, Object> originalCellData = new LinkedHashMap<>();
				for (short colIx = minColIx; colIx <= maxColIx; colIx++) {
					cell = row.getCell(colIx);
					if (s >= columnNameList.size())
						continue;

					if (physicalIndex > columnNameList.get(s).size() - 1)
						continue;
//					physicalIndex = Math.min(columnNameList.get(s).size()-1, physicalIndex);
					ExcelModel excelModel = columnNameList.get(s).get(physicalIndex);
					String fieldName = excelModel.getFieldName();
					Object value = null;

					if (exceptColumnNum!=null && exceptColumnNum.contains((int) colIx)) {
						continue;
					}
					if (cell == null && !excelModel.isPicture()) {
						if(!excelModel.isNullAble()){
							errorMessage.append(sheet.getSheetName() + "的第" + (r + 1) + "行的第" + (colIx+1)
									+ "列的值不能为空").append("\n");
							return false;
						}
						physicalIndex++;
						continue;
					}
					try {
						if (cell != null)
							value = getCellValue(cell);

						if(!excelModel.isNullAble() && (value ==null || (value+"").trim().length()==0)) {
							errorMessage.append(sheet.getSheetName() + "的第" + (r + 1) + "行的第" + (colIx+1)
									+ "列的值不能为空").append("\n");
							return false;
						}
					} catch (Exception e) {
						e.printStackTrace();
						errorMessage.append(sheet.getSheetName() + "的第" + (r + 1) + "行的第" + (colIx+1)
								+ "列的值获取出错，请检查excel文件格式核对格式模板，出错原因是：" + e.getMessage()).append("\n");
						return false;
					}
					try {
						if (excelModel.isDate() && value != null) {
							value = DateUtil.getJavaDate(Double.parseDouble(value.toString()) );
							value = CommonUtil.formatDate(value, excelModel.getPattern());
						}
					} catch (Exception e) {
							e.printStackTrace();
					}
					originalCellData.put(fieldName, value);
					if (excelModel.isNeedtranslate() && excelModel.getTranslateMappingInfo() != null) {
						Object temp = getFromMap(excelModel.getTranslateMappingInfo(), value, null);
						final Object originalValue = value;
						if (temp != null) {
							value = temp;
						}else if(excelModel.isNeedAddTranslationException()){
							errorMessage.append(sheet.getSheetName() + "的第" + (r + 1) + "行的第" + (colIx+1)
									+ "列的值转换出错，出错原因是：" +"当前列转换只有以下几个值："+ excelModel.getTranslateMappingInfo().keySet()).append("\n");
							return false;
						}
					} else if (excelModel.isPicture()) {
						String downPath = excelModel.getImageDownPath();
						if (downPath == null) {
							throw new RuntimeException("文件下载路径为空");
						}
						value = getPicture(r, colIx, downPath, excelModel.getImageVisitPrex());
					}
					if(excelModel.getExcelCustomValidate()!=null){
						if(excelCustomModels.size() != 0 ){
							long count = excelCustomModels.stream().filter(excelCustomModel -> excelCustomModel.getExcelModel() == excelModel).count();
							if(count ==0){
								ExcelCustomModel excelCustomModel = new ExcelCustomModel();
								excelCustomModel.setExcelModel(excelModel);
								excelCustomModel.setCurrentCellNum(colIx);
								excelCustomModel.setCurrentRowNum(r);
								excelCustomModel.setCell(cell);
								excelCustomModel.setRow(row);
								excelCustomModel.setCurrentValue(value);
								excelCustomModels.add(excelCustomModel);
							}
						}else{
							ExcelCustomModel excelCustomModel = new ExcelCustomModel();
							excelCustomModel.setExcelModel(excelModel);
							excelCustomModel.setCurrentCellNum(colIx);
							excelCustomModel.setCurrentRowNum(r);
							excelCustomModel.setCell(cell);
							excelCustomModel.setRow(row);
							excelCustomModel.setCurrentValue(value);
							excelCustomModels.add(excelCustomModel);
						}

					}

					cellData.put(fieldName, value);
					physicalIndex++;
				}
				Map<String,Object> finalCellData = new HashMap<>();
				Map<String,Object> finalOriginalCellData = new HashMap<>();
				finalCellData.putAll(cellData);
				if(excelCustomModels.size() > 0  && errorMessage.length() == 0){
					for (ExcelCustomModel excelCustomModel : excelCustomModels){
						if(errorMessage.length() > 0){
							break;
						}
						ExcelCustomValidate excelCustomValidate = excelCustomModel.getExcelModel().getExcelCustomValidate();
						ExcelRowData<?> excelRowData = new ExcelRowData<Object>() {
							@Override
							public Row getRow() {
								return excelCustomModel.getRow();
							}

							@Override
							public Cell getCell() {
								return excelCustomModel.getCell();
							}

							@Override
							public Object getRowData() {
								return finalCellData;
							}

							@Override
							public <U> U getRowData(Class<U> uClass) {
								JSONObject object = (JSONObject) JSONObject.toJSON(finalCellData);
								return object.toJavaObject(uClass);
							}

							@Override
							public Object currentValue() {
								return excelCustomModel.getCurrentValue();
							}

							@Override
							public int currentCellNum() {
								return excelCustomModel.getCurrentCellNum();
							}

							@Override
							public int currentRowNum() {
								return excelCustomModel.getCurrentRowNum();
							}

							@Override
							public <U> U getOriginalCellData(Class<U> uClass) {
								JSONObject object = (JSONObject) JSONObject.toJSON(finalOriginalCellData);
								return object.toJavaObject(uClass);
							}

							@Override
							public Map<String, Object> getOriginalCellData() {
								return finalOriginalCellData;
							}
						};
						boolean validate = excelCustomValidate.validate(excelRowData);
						if(!validate){
							errorMessage.append(sheet.getSheetName() + "的第" + (r + 1) + "行的第" + (excelCustomModel.getCurrentCellNum()+1)
									+ "列的值获取出错，请检查excel文件格式核对格式模板，出错原因是：" + excelCustomValidate.errorMessage()).append("\n");
						}
					}
				}
				if(errorMessage.length() > 0 ){
					return false;
				}
				rowData.add(cellData);
			}
			datas.add(rowData);
		}
		if (errorMessage.length() == 0)
			return true;
		else
			return false;
	}


	/**
	 * 从map中取值
	 * 
	 * @Title: getFromMap
	 * @Description: TODO
	 * @param map
	 * @param key
	 * @return
	 * @return Object
	 */
	private Object getFromMap(Map<?, ?> map, Object key, Class type) {
		Object value = null;
		Set<?> keys = map.keySet();
		Iterator<?> it = keys.iterator();
		while (it.hasNext()) {
			Object mapKey = it.next();
			if (key == mapKey || mapKey.equals(key.toString())) {
				value = map.get(mapKey);
				if (type != null)
					value = caseObject(value, type);

			}
		}
		return value;
	}

	/**
	 * 将数据类型转换为指定的类型
	 * 
	 * @Title: caseObject
	 * @Description: TODO
	 * @param value
	 * @param type
	 * @return
	 * @return Object
	 */
	public Object caseObject(Object value, Class type) {
		Object result = null;
		if (value == null || value.toString().trim().length() == 0)
			return type.cast(null);
		if (type != null) {
			String className = type.getCanonicalName();
			switch (className) {
			case "java.lang.String":
				result = value + "";
				break;
			case "java.lang.Integer":
			case "int":
				if ((value + "").indexOf(".") != -1) {
					value = (value + "").subSequence(0, (value + "").indexOf("."));
				}
				result = Integer.valueOf(value.toString());
				break;
			case "java.lang.Double":
			case "double":
				result = Double.valueOf(value.toString());
				break;
			case "java.lang.Long":
			case "long":
				result = Long.valueOf(value.toString());
				break;
			case "java.lang.Boolean":
			case "boolean":
				result = Boolean.valueOf(value.toString());
				break;
			case "java.lang.Float":
			case "float":
				result = Float.valueOf(value.toString());
				break;
			case "java.lang.Short":
			case "short":
				result = Short.valueOf(value.toString());
				break;
			case "java.util.Date":
				result = dateHandle(value.toString());
				break;
			case "java.sql.Timestamp":
				result = timeStampHandle(value.toString());
				break;
			case "java.sql.Date":
				result = sqlDateHandle(value.toString());
				break;
			}
		}
		return result;
	}
	public Date dateHandle(String source){
		Date parse = null;
		SimpleDateFormat simpleDateFormat;
		try {
			simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			parse = simpleDateFormat.parse(source);
		} catch (ParseException e) {
			try {
				simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
				parse = simpleDateFormat.parse(source);
			} catch (ParseException ex) {
				try {
					simpleDateFormat = new SimpleDateFormat("yyyy-MM");
					parse = simpleDateFormat.parse(source);
				} catch (ParseException exc) {
					exc.printStackTrace();
				}
			}
		}
		return parse;
	}

	public TimeStamp timeStampHandle(String source){
		return null;
	}

	public java.sql.Date sqlDateHandle(String source){
		return null;
	}

	/**
	 * 获取excel的数据的java对象
	 * 
	 * @Title: getObject
	 * @Description: TODO
	 * @param sheetNum
	 * @param clazz
	 * @return
	 * @return List<T>
	 */
	public <T> List<T> getObject(int sheetNum, Class<T> clazz) {
		LinkedList<T> objets = new LinkedList<>();
		if (sheetNum < datas.size()) {
			LinkedList<Map<String, Object>> sheetData = datas.get(sheetNum);
			if(clazz.isAssignableFrom(Map.class)) {
				return (List<T>) sheetData;
			}
			for (Map<String, Object> map : sheetData) {
				try {
					T t = clazz.newInstance();
					LinkedList<ExcelModel> listModels = columnNameList.get(sheetNum);
					for (ExcelModel excelModel : listModels) {
						String field = excelModel.getFieldName();
						Field javaField =ReflectionUtils.findField(clazz,field) ;//clazz.getDeclaredField(field);
						Character first = new Character(field.charAt(0));
						String setMethod = "set" + String.valueOf(first).toUpperCase() + field.substring(1);
						Method method = ReflectionUtils.findMethod(clazz,setMethod,javaField.getType());//clazz.getDeclaredMethod(setMethod, javaField.getType());
						if(method!=null){
							Object value = getFromMap(map, field, javaField.getType());
							if (value != null)
								method.invoke(t, value);
						}

					}
					objets.add(t);
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}  catch (SecurityException e) {
					e.printStackTrace();
				}  catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return objets;
	}

	/**
	 * 获取cell中的值
	 * 
	 * @Title: getCellValue
	 * @Description: TODO
	 * @param cell
	 * @return
	 * @return Object
	 */
	public Object getCellValue(Cell cell) {
		Object value = null;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BLANK:
		case Cell.CELL_TYPE_STRING:
			value = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			value = cell.getBooleanCellValue();
			break;
		case Cell.CELL_TYPE_ERROR:
			value = cell.getErrorCellValue();
			break;
		case Cell.CELL_TYPE_FORMULA:
			value =getCellValue(formulaEvaluator.evaluateInCell(cell));
			break;
		case Cell.CELL_TYPE_NUMERIC:
			value = cell.getNumericCellValue();
			value=formart(value);
			break;
		}
		return value;
	}
	
	public Object formart(Object object) {
		NumberFormat nf = NumberFormat.getInstance();
		String s = nf.format(object);
		if (s.indexOf(",") >= 0) {
		    s = s.replace(",", "");
		}
		return s;
	}

	/**
	 * 获取excel中的图片
	 * 
	 * @Title: getPicture
	 * @Description: TODO
	 * @param row
	 * @param cell
	 * @param downPath
	 * @param imageVisitPrex
	 * @return
	 * @return String
	 */
	private String getPicture(int row, int cell, String downPath, String imageVisitPrex) {
		ByteArrayInputStream heanerIn = new ByteArrayInputStream(header);
		String value = null;
		boolean hasPicture = false;
		try {
			if (POIFSFileSystem.hasPOIFSHeader(heanerIn)) {
				HSSFSheet sheet = (HSSFSheet) this.sheet;
				List<HSSFPictureData> pictures = (List<HSSFPictureData>) workbook.getAllPictures();
				if (pictures.size() != 0) {
					for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
						HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
						if (shape instanceof HSSFPicture) {
							HSSFPicture pic = (HSSFPicture) shape;
							int pictureIndex = pic.getPictureIndex() - 1;
							HSSFPictureData picData = pictures.get(pictureIndex);
							int pictureRow = anchor.getRow1();
							int pictureCol = anchor.getCol1();
							if (pictureRow == row && pictureCol == cell) {
								ByteArrayInputStream input = new ByteArrayInputStream(picData.getData());
								BufferedImage image = ImageIO.read(input);
								String uuid = UUID.randomUUID().toString().replaceAll("-", "")
										+ System.currentTimeMillis();
								imageVisitPrex += uuid + "." + picData.suggestFileExtension();
								value = downPath + uuid + "." + picData.suggestFileExtension();
								ImageIO.write(image, picData.suggestFileExtension(), new FileOutputStream(value));
								hasPicture = true;
								break;
							}
						}
					}
				}
			}
			if (POIXMLDocument.hasOOXMLHeader(heanerIn)) {
				XSSFSheet sheet = (XSSFSheet) this.sheet;
				for (POIXMLDocumentPart dr : sheet.getRelations()) {
					if (dr instanceof XSSFDrawing) {
						XSSFDrawing drawing = (XSSFDrawing) dr;
						List<XSSFShape> shapes = drawing.getShapes();
						for (XSSFShape shape : shapes) {
							XSSFPicture pic = (XSSFPicture) shape;
							XSSFClientAnchor anchor = pic.getPreferredSize();
							CTMarker ctMarker = anchor.getFrom();
							int pictureRow = ctMarker.getRow();
							int pictureCol = ctMarker.getCol();
							if (pictureRow == row && pictureCol == cell) {
								ByteArrayInputStream input = new ByteArrayInputStream(pic.getPictureData().getData());
								BufferedImage image = ImageIO.read(input);
								String uuid = UUID.randomUUID().toString().replaceAll("-", "")
										+ System.currentTimeMillis();
								imageVisitPrex += uuid + "." + pic.getPictureData().suggestFileExtension();
								value = downPath + uuid + "." + pic.getPictureData().suggestFileExtension();
								ImageIO.write(image, pic.getPictureData().suggestFileExtension(),
										new FileOutputStream(value));
								hasPicture = true;
								break;
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!hasPicture)
			imageVisitPrex = null;
		return imageVisitPrex;
	}

	/**
	 * 设置需要忽略的列
	 * 
	 * @Title: addExceptColumnNum
	 * @Description: TODO
	 * @param sheetNum    sheet页下标
	 * @param columnIndex 列下标
	 * @return void
	 */
	public void addExceptColumnNum(int sheetNum, int columnIndex) {
		if (sheetNum < 0)
			throw new IllegalArgumentException("num < 0");
		if (columnIndex < 0)
			throw new IllegalArgumentException("num < 0");
		if (exceptColumnNumMap.containsKey(sheetNum)) {
			List<Integer> child = exceptColumnNumMap.get(sheetNum);
			child.add(columnIndex);
		} else {
			List<Integer> child = new ArrayList<>();
			child.add(columnIndex);
			exceptColumnNumMap.put(sheetNum, child);
		}

	}

	/**
	 * 设置需要忽略的列
	 * 
	 * @Title: addExceptColumnNums
	 * @Description: TODO
	 * @param sheetNum         sheet页下标
	 * @param exceptColumnNums 忽略列信息
	 * @return void
	 */
	public void addExceptColumnNums(int sheetNum, List<Integer> exceptColumnNums) {
		if (sheetNum < 0)
			throw new IllegalArgumentException("num < 0");
		this.exceptColumnNumMap.put(sheetNum, exceptColumnNums);
	}

	public void addSheetStartRow(int startRow) {
		this.sheetStartRow.add(startRow);
	}

	public void addSheetStartRows(LinkedList<Integer> sheetStartRow) {
		this.sheetStartRow.addAll(sheetStartRow);
	}

	/**
	 * 设置开始取数据的行下标
	 * 
	 * @Title: setStartRow
	 * @Description: TODO
	 * @param startRow
	 * @return void
	 */
	public void setStartRow(int startRow) {
		this.startRow = startRow;
	}

	/**
	 * 设置列对应的属性名称
	 * 
	 * @Title: addColumnName
	 * @Description: TODO
	 * @param excelModels 列名称
	 * @return void
	 */
	public void addColumnName(LinkedList<ExcelModel> excelModels) {
		this.columnNameList.add(excelModels);
	}

	/**
	 * 设置列对应的属性名称
	 * 
	 * @Title: addColumnNames
	 * @Description: TODO
	 * @param excelModels
	 * @return void
	 */
	public void addColumnNames(LinkedList<LinkedList<ExcelModel>> excelModels) {
		this.columnNameList.addAll(excelModels);
	}

	/**
	 * 生成Excel模型对象
	 * 
	 * @Title: generateExcelModel
	 * @Description: TODO
	 * @param fieldName            字段名称
	 * @param needtranslate        字段是否需要自定义翻译
	 * @param translateMappingInfo 自定义翻译
	 * @param isPicture            是否是图片
	 * @param imageDownPath        图片下载地址
	 * @param imageVisitPrex       图片访问前缀
	 * @return
	 * @return ExcelModel
	 */
	public static ExcelModel generateExcelModel(String fieldName, boolean needtranslate,
			Map<Object, Object> translateMappingInfo, boolean isPicture, String imageDownPath, String imageVisitPrex) {
		return new ExcelModel(fieldName, needtranslate, translateMappingInfo, isPicture, imageDownPath, imageVisitPrex);
	}

	/**
	 * 生成Excel模型对象
	 * 
	 * @Title: generateExcelModel
	 * @Description: TODO
	 * @param fieldName 字段名称
	 * @return
	 * @return ExcelModel
	 */
	public static ExcelModel generateExcelModel(String fieldName) {
		return new ExcelModel(fieldName);
	}

	public static ExcelModel generateExcelModel(String fieldName,boolean nullAble) {
		return new ExcelModel(fieldName,nullAble);
	}

	/**
	 * 生成ExcelModel对象
	 * 
	 * @Title: generateExcelModel
	 * @Description: TODO
	 * @param fieldName            字段
	 * @param translateMappingInfo 自定义翻译对象
	 * @return
	 * @return ExcelModel
	 */
	public static ExcelModel generateExcelModel(String fieldName, Map<Object, Object> translateMappingInfo) {
		return new ExcelModel(fieldName, translateMappingInfo);
	}

	/**
	 * 生成ExcelModel对象
	 * 
	 * @Title: generateExcelModel
	 * @Description: TODO
	 * @param fieldName      字段名称
	 * @param imageVisitPrex 图片访问前缀
	 * @param imageDownPath  图片下载路径
	 * @return
	 * @return ExcelModel
	 */
	public static ExcelModel generateExcelModel(String fieldName, String imageVisitPrex, String imageDownPath) {
		return new ExcelModel(fieldName, imageVisitPrex, imageDownPath);
	}

	/**
	 * 生成ExcelModel对象
	 * 
	 * @Title: generateExcelModel
	 * @Description: TODO
	 * @param fieldName 字段类型
	 * @param pattern   时间格式化类型 默认是 yyyy-MM-dd HH:mm:ss
	 * @return
	 * @return ExcelModel
	 */
	public static ExcelModel generateExcelModel(String fieldName, String pattern) {
		return new ExcelModel(fieldName, pattern);
	}

	/**
	 * 	获取当前excel的sheet数量
	 * @return
	 */
	public int getSheetNums(){
		return workbook.getNumberOfSheets();
	}

	/**
	 * 获取当前excel的workbook对象
	 * @return
	 */
	public  Workbook getWorkbook(){
		return  workbook;
	}

}
