package com.base.util.poi.excel;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.base.util.poi.excel.annotation.*;
import com.base.util.poi.excel.core.BusinessSXSSFWorkbook;
import com.base.util.poi.excel.core.BusinessXSSFWorkbook;
import com.base.util.poi.excel.exception.IllegalNoSuchAnnotationException;
import com.base.util.poi.excel.exception.ReflectionBeanException;
import com.base.util.webbaseconfig.exception.BusinessException;
import com.base.util.zip.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFDataValidationHelper;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 	生成Excel工具类
 * 
 * @ClassName: ExcelCreator
 * @Description: TODO
 * @author dh
 * @date 2020年6月8日
 *
 */
@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
public class ExcelCreator {

	public static final String XLSX = "xlsx";

	public static final String XLX = "xls";


	private static final Logger logger = LoggerFactory.getLogger(ExcelCreator.class);

	/**
	 * 改变单元格并重新计算图片大小
	 */
	public static final int MOVE_AND_RESIZE = 0;


	/**
	 * 改变单元格不重新计算图片大小
	 */
	public static final int MOVE_DONT_RESIZE = 2;


	/**
	 * 不随单元格的改变而改变
	 */
	public static final int DONT_MOVE_AND_RESIZE = 3;

	/**
	 * 多图片分隔符
	 */
	public static final String IMAGE_SPLIT=",";

	/**
	 * 图片分隔符
	 */
	public String imagesSeparator = IMAGE_SPLIT;

	/**
	 * 当前列表数
	 */
	private int currentListNum=1;


	/**
	 * 隐藏的sheet页面
	 */
	private Sheet hiddenSheetListBox;

	/** 当前Excel类型 */
	private String currentExcelType;
	/** Excel对象 */
	private Workbook book;

	/** 当前sheet对象 */
	private Sheet sheet;

	/**
	 * 行信息
	 */
	private Row row;

	/**
	 * 单元格
	 */
	private Cell cell;

	/**
	 * 行高
	 */
	private Integer rowHeight;

	/** 图片工具 */
	private Drawing drawing;

	/** 是否需要增加序号 */
	private boolean needOrderNum = false;

	/** 需要插入的数据 */
	private Object object;

	/** 表头信息 */
	private String[] header;

	/** 标题 */
	private String title;

	/** sheet页名称默认是sheet0 */
	private String sheetName;

	/** 插入字段和下标的映射 */
	private Map<Integer, ExcelModel> columnMappingInfo = new LinkedHashMap<>();

	/** 需要合并单元格的信息,要合并的列下标以及对哪个字段进行合并 */
	private Map<Integer, String> columnMergeInfo = new HashMap<>();

	/** 如果有多个sheet页那么添加子类对象即可 */
	private LinkedList<ExcelCreator> child = new LinkedList<>();

	/** 标题样式 */
	private CellStyle titleCellStyle;

	/** 数据单元格样式 */
	private CellStyle cellStyle;

	/**
	 * 默认行高
	 */
	private static final int DEFAULT_ROW_HEIGHT = 1000;

	/**
	 * 默认列宽
	 */
	private static final int DEFAULT_COLUMN_WIDTH = 20;

	/**
	 * 是否设置了列宽
	 */
	private boolean isSettingColumnWidth = false;
	/**默认图片下载超时时间*/
	private static final int DEFAULT_IMAGE_READ_TIME_OUT=2000;
	/**图片下载超时时间*/
	private int imageReadTimeOut;

	/**插入的图片随单元格变化类型*/
	private int pictureType=MOVE_AND_RESIZE;

	/**数据行*/
	private int rowNum;


	/**
	 * 是否是大数据导出
	 */
	private boolean isBigData=false;


	/**
	 * 数据校验
	 */
	private DataValidationConstraint listBoxValidate;


	/**自定义添加内容，默认加载标题后面*/
	private final LinkedList<String> diyRowContext = new LinkedList<>();

	/**
	 * 自定义行文本是否需要合并
	 */
	private final Map<Integer,Boolean> diyRowContextPropertyInfo = new HashMap<>();

	/**
	 * 标题高度
	 */
	private int titleRowHeight = DEFAULT_ROW_HEIGHT;

	/**
	 * 表头高度
	 */
	private int headerRowHeight = DEFAULT_ROW_HEIGHT;

	/**
	 * 标题合并对象
	 */
	private CellRangeAddress tileCellRangeAddress;

	/**
	 * 自定义行信息合并列表
	 */
	private List<CellRangeAddress> diyRowContextCellRangeAddress = new ArrayList<>();

	/**
	 * 存放列的最大扩容大小
	 */
	private final Map<Integer,Integer> columnMaxMapping = new HashMap<>();

	/**
	 * 当前图片下载临时地址
	 */
	private String currentPictureDownLoadDir;

	/**
	 * 图片计数器
	 */
	private AtomicInteger imageNum = new AtomicInteger(0);

	/**
	 * 图片后缀
	 */
	private Map<Integer,String> pictureIndexMapping = new HashMap<>();

	/**
	 * 图片文件夹是否创建成功
	 */
	private boolean pictureDirCreate = false;

	/**
	 * 创建的图片下载的任务
	 */
	private final List<ImageDownLoadTask> imageDownLoadTasks = new ArrayList<>();

	/**
	 * 图片下载计数器
	 */
	private CountDownLatch count;

	/**
	 * 临时文件夹
	 */
	private static final String property = "java.io.tmpdir";

	/**
	 * 是否有图片数据
	 */
	private boolean hasPicture = false;

	/**
	 * Excel临时文件创建
	 */
	private  File tempWorkFile;

	/**
	 * 图片下载线程池
	 */
	private static   final ThreadPoolExecutor executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), r -> {
		Thread thread = new Thread(r,"picture downLoad task");
		return thread;
	});
	static {
		executor.allowCoreThreadTimeOut(true);
	}
	/**
	 * 设置图片地址分隔符，只针对导出多张图片的情况，其他情况无效
	 * @param imagesSeparator 分隔符
	 */
	public void setImagesSeparator(String imagesSeparator){
		this.imagesSeparator = imagesSeparator;
	}


	/**
	 * 设置标题高度
	 * @param titleRowHeight 标题高度
	 */
	public void setTitleRowHeight(int titleRowHeight) {
		this.titleRowHeight = titleRowHeight;
	}

	/**
	 * 设置表头高度
	 * @param headerRowHeight 表头高度
	 */
	public void setHeaderRowHeight(int headerRowHeight) {
		this.headerRowHeight = headerRowHeight;
	}

	/**
	 * 创建一个ExcelCreator对象
	 * @param sheetName   sheet页名称
	 */
	public ExcelCreator(String sheetName) {
		this(XLSX,sheetName,false);
	}

	/**
	 * 创建一个ExcelCreator对象
	 * @param excelInfo 使用ExcelInfo注解的对象，代替之前使用api操作excel的方式
	 */
	public ExcelCreator(Object excelInfo){
		ExcelAnnotationHandler excelAnnotationHandler = new ExcelAnnotationHandler(excelInfo);
		ExcelAnnotationProperty excelAnnotationProperty = excelAnnotationHandler.getExcelAnnotationProperty();
		setTitle(excelAnnotationProperty.getTitle());
		setHeader(excelAnnotationProperty.getHeader());
		setObject(excelAnnotationProperty.getExcelData());
		setColumnMergeInfo(excelAnnotationProperty.getMergeInfo());
		Map<Integer, ExcelModel> columnMappingInfo = new HashMap<>();
		List<ExcelModel> excelModels = excelAnnotationProperty.getExcelModels();
		if(excelModels!=null && excelModels.size()>0){
			excelModels.forEach( excelModel -> columnMappingInfo.put(columnMappingInfo.size(),excelModel));
		}
		setColumnMappingInfo(columnMappingInfo);
		ExcelInfo excelInfo1 = excelAnnotationProperty.getExcelInfo();
		this.sheetName = excelInfo1.sheetName();
		this.currentExcelType = excelInfo1.excelType();
		this.setNeedOrderNum(excelInfo1.needOrder());
		this.pictureType = excelInfo1.pictureInnerType();
		this.imageReadTimeOut = excelInfo1.imageReadTimeOut();
		setTitleRowHeight(excelInfo1.titleHeight());
		setHeaderRowHeight(excelInfo1.headerHeight());
		setImagesSeparator(StringUtils.hasText(excelInfo1.imageSeparator()) ? excelInfo1.imageSeparator() : IMAGE_SPLIT);
		init(currentExcelType,true);
	}

	/**
	 * 创建一个ExcelCreator对象
	 * @param excelType excel类型
	 * @param sheetName sheet页的名称
	 * @param bigData    是否使用SAASFWorkbook处理数据
	 */
	public ExcelCreator(String excelType, String sheetName,boolean bigData){
		this.sheetName = sheetName;
		init(excelType,bigData);
	}

	/**
	 * 设置图片嵌入单元格的方式参考 {@code ExcelCreator.MOVE_AND_RESIZE} MOVE_DONT_RESIZE DONT_MOVE_AND_RESIZE
	 * @param pictureType 图片类型分别有三个常量 MOVE_DONT_RESIZE DONT_MOVE_AND_RESIZE MOVE_AND_RESIZE
	 */
	public  void setPictureType(int pictureType){
		this.pictureType=pictureType;
	}

	/**
	 * 曾加自定义信息在表头之后加入 ，默认要合并
	 * @param context 内容
	 */
	public void addDiyRowContext(String context){
		addDiyRowContext(context,true);
	}

	/**
	 * 曾加自定义信息在表头之后加入
	 * @param context 内容
	 * @param needMerge 是否需要合并单元格
	 */
	public void addDiyRowContext(String context,boolean needMerge){
		diyRowContextPropertyInfo.put(diyRowContext.size(),needMerge);
		diyRowContext.add(context);
	}

	/**
	 * 设置下载图片超时时间
	 * @param imageReadTimeOut 毫秒
	 */
	public  void setImageReadTimeOut(int imageReadTimeOut){
		this.imageReadTimeOut = imageReadTimeOut;
	}

	/**
	 * 创建一个excel对象
	 * @param excelType	excel的类型
	 * @param sheetName	sheet名称
	 */
	public ExcelCreator(String excelType, String sheetName) {
		this(excelType,sheetName,false);
	}

	/**
	 * 自定义宽度
	 * @param columnIndex 列号
	 * @param width  高度
	 */
	public void setColumnWidth(int columnIndex, int width) {
		isSettingColumnWidth = true;
		sheet.setColumnWidth(columnIndex, width);
	}

	/**
	 * 	设置行高，如果不设置默认为DEFAULT_ROW_HEIGHT
	 * @Title: setRowHeight
	 * @Description: TODO
	 * @param rowHeight 行高
	 * @return void
	 */
	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}

	/**
	 * 自己设置excel
	 * @param book 工作簿对象
	 */
	public ExcelCreator(Workbook book) {
		this.book = book;
	}

	private void init(String excelType,boolean bigData) {
		// 检查excel类型
		checkExcelType(excelType);
		// 创建工作簿对象
		createWorkBook(excelType,bigData);
	}

	/**
	 * 	创建excel
	 */
	public ExcelCreator createExcel() {
		//创建临时图片存放文件夹
		createTempleFileDir();
		//校验图片数据列的最大列数
		checkPictureMaxSize();
		// 添加表头以及标题对象
		settingHeaderAndTitle(true);
		//excel数据下拉框添加校验
		checkListBox();
		//下载图片
		downLoadPicture();
		// 添加数据到单元格
		settingData();
		// 合并单元格操作
		mergeCell();
		// 操作子sheet页
		generateChildSheet();
		//解压excel文件并且合并图片文件
		decompressionPictureDirAndCompression();
		return this;
	}

	/**
	 * 解压excel文件合并图片文件
	 */
	private void decompressionPictureDirAndCompression() {
		if(pictureDirCreate && hasPicture){
			long id = IdWorker.getId();
			String tempDir = System.getProperty(property)+File.separator+ id+"."+currentExcelType;
			String tempUnzipDir =  System.getProperty(property)+File.separator+ id;
			logger.debug("temp Excel File ："+tempDir);
			tempWorkFile = new File(tempDir);
			if(!tempWorkFile.exists()){
				try (FileOutputStream fo = new FileOutputStream(tempWorkFile)){
					tempWorkFile.createNewFile();
					getWorkBook().write(fo);
					File tempUnzipDirFile = new File(tempUnzipDir);
					ZipUtil.unzip(tempDir,tempUnzipDir);
					addPictureIntoTempDir(tempUnzipDir);
					tempWorkFile.delete();
					ZipUtil.zip(tempUnzipDir,tempDir);
					FileUtils.deleteDirectory(new File(currentPictureDownLoadDir));
					FileUtils.deleteDirectory(new File(tempUnzipDir));
				} catch (IOException e) {
					throw new BusinessException("temp Excel File create false",e);
				}
			}
		}
	}

	private void addPictureIntoTempDir(String tempUnzipDir) throws IOException {
		logger.debug("addPictureIntoExcel");
		String currentPictureDownLoadDir = this.currentPictureDownLoadDir;
		File pictureDir = new File(currentPictureDownLoadDir);
		File temp = new File(tempUnzipDir+"/xl/media");
		if(!temp.exists()){
			temp.mkdirs();
		}

		File[] files = pictureDir.listFiles();
		int countNum =10;
		int indexNum = 1;
		int totalPageNum = (files.length  +  10  - 1) / 10;
		logger.debug("picture add task num :"+totalPageNum);
		CountDownLatch imgCount = new CountDownLatch(totalPageNum);
		List<ImageAddTask> imageAddTasks = new ArrayList<>();
		List<File> imgFile = new ArrayList<>();
		for(File imageFile : files){
			if(countNum -- == 0){
				ImageAddTask imageAddTask = new ImageAddTask(imgFile,imgCount,tempUnzipDir);
				imageAddTasks.add(imageAddTask);
				imgFile = new ArrayList<>();
				countNum=9;
			}
			imgFile.add(imageFile);
		}
		if(imgFile.size() > 0){
			ImageAddTask imageAddTask = new ImageAddTask(imgFile,imgCount,tempUnzipDir);
			imageAddTasks.add(imageAddTask);
		}
		logger.debug("add task num:"+ imageAddTasks.size());
		imageAddTasks.forEach(executor::execute);
		try {
			imgCount.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建临时图片文件夹
	 */
	private void createTempleFileDir() {

		currentPictureDownLoadDir = System.getProperty(property);
		currentPictureDownLoadDir+=File.separator+ IdWorker.getId();
		logger.debug("picture downLoad dir :"+currentPictureDownLoadDir);
		File file = new File(currentPictureDownLoadDir);
		if(!file.exists()){
			try {
				boolean mkdirs = file.mkdirs();
				pictureDirCreate = mkdirs;
				if(!mkdirs){
					logger.debug("picture downLoad dir create false");
				}
			} catch (Exception e) {
				logger.error("picture downLoad dir create false",e);
			}
		}else{
			pictureDirCreate = true;
		}
	}

	/**
	 * 图片下载
	 */
	private void downLoadPicture() {
		if(imageDownLoadTasks.size() > 0){
			hasPicture =true;
		}
		if(pictureDirCreate && hasPicture){
				imageDownLoadTasks.forEach(executor::execute);
			try {
				this.count.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 校验图片列的最大列数
	 */
	private void checkPictureMaxSize() {
		int size = columnMappingInfo.size();
		Map<Integer, ExcelModel> pictureExcelModel = new LinkedHashMap<>();
		for(int i = 0 ; i < size ; i++){
			ExcelModel excelModel = columnMappingInfo.get(i);
			if(excelModel.isPicture()){
				pictureExcelModel.put(i,excelModel);
			}
		}
		if(pictureExcelModel.size()!=0){
			List list = changeList();
			int countNum =10;
			int indexNum = 1;
			int totalPageNum = (list.size()  +  10  - 1) / 10;
			logger.debug("picture task num :"+totalPageNum);
			count = new CountDownLatch(totalPageNum);
			Map<Integer,String> pictureIndexMapping = new HashMap<>();
			int totalNum = 0;
			for(Object obj : list){
				if( countNum == 0 ){
					ImageDownLoadTask task = new ImageDownLoadTask(pictureIndexMapping,currentPictureDownLoadDir,imageReadTimeOut,count);
					imageDownLoadTasks.add(task);
					pictureIndexMapping = new HashMap<>();
					countNum = 10;
				}

				for(Map.Entry<Integer,ExcelModel> entry : pictureExcelModel.entrySet()){
					Integer key = entry.getKey();
					ExcelModel value = entry.getValue();
					String fieldName = value.getFieldName();
					Object picture = getValue(fieldName, obj);
					if(picture!=null){
						String s = picture.toString().trim();
						String[] split = s.split(imagesSeparator);
						for(String url : split){
							pictureIndexMapping.put(indexNum++,url);
							totalNum++;
						}
						int length = split.length-1;
						if( length > 0){
							Integer max = columnMaxMapping.computeIfAbsent(key, k -> length);
							if(length > max){
								columnMaxMapping.put(key, length);
							}
						}
					}
				}
				countNum--;
			}
			if(book instanceof  BusinessXSSFWorkbook){
				for(int i = 0 ; i < totalNum ; i++){
					((BusinessXSSFWorkbook) book).addPicture(i+1);
				}
			}else if (book instanceof  SXSSFWorkbook){
				for(int i = 0 ; i < totalNum ; i++){
					((BusinessSXSSFWorkbook) book).addPicture(i+1);
				}
			}

			if(pictureIndexMapping.size() > 0){
				ImageDownLoadTask task = new ImageDownLoadTask(pictureIndexMapping,currentPictureDownLoadDir,imageReadTimeOut,count);
				imageDownLoadTasks.add(task);
			}
			List<String> newHeader = new ArrayList<>();
			for(int i = 0 ;  i< header.length ; i ++){
				Integer maxNum = columnMaxMapping.get(i);
				String headName = header[i];
				newHeader.add(headName);
				if(maxNum!=null){
					while(maxNum-->0){
						newHeader.add(headName);
					}
				}
			}
			header = newHeader.toArray(new String[0]);
		}
	}

	/**
	 * 	处理多个sheet
	 *
	 * @Title: generateChildSheet
	 * @Description: TODO
	 * @return void
	 */
	private void generateChildSheet() {
		if (child != null && child.size() > 0) {
			int  concurrentRowNum = currentListNum;
			for (ExcelCreator excelCreator : child) {
				excelCreator.currentListNum = concurrentRowNum;
				excelCreator.book = this.book;
				if (excelCreator.sheetName!=null && excelCreator.sheetName.length() > 0 )
					excelCreator.sheet = excelCreator.book.createSheet(excelCreator.sheetName);
				else
					excelCreator.sheet = excelCreator.book.createSheet();
				excelCreator.drawing = sheet.createDrawingPatriarch();
				excelCreator.currentExcelType = this.currentExcelType;
				excelCreator.hiddenSheetListBox = hiddenSheetListBox;
				// 默认样式设置
				excelCreator.defaultCellStyle();
				excelCreator.createExcel();
				concurrentRowNum = excelCreator.currentListNum;
			}
			currentListNum = concurrentRowNum;
		}
	}

	/**
	 * 	合并单元格操作
	 *
	 * @Title: mergeCell
	 * @Description: TODO
	 * @return void
	 */
	private void mergeCell() {
		//合并标题
		if(tileCellRangeAddress!=null){
			sheet.addMergedRegion(tileCellRangeAddress);
		}
		//合并自定义行信息
		if(diyRowContextCellRangeAddress.size() > 0){
			diyRowContextCellRangeAddress.forEach( f -> sheet.addMergedRegion(f));
		}
		int rowNum = 0;
		if (title != null && title.trim().length() != 0) {
			rowNum += 1;
		}
		if (header != null && header.length != 0) {
			rowNum += 1;
		}
		rowNum+=diyRowContext.size();
		for (Map.Entry<Integer, String> entry : columnMergeInfo.entrySet()) {
			int indexColumn = entry.getKey();
			int index = 0;
			if (needOrderNum)
				indexColumn += 1;
			String field = entry.getValue();
			List dataList = changeList();
			Object currentValue =null;
			for (int i = 0; i < dataList.size(); i++) {
				Object data = dataList.get(i);
				Object value = getValue(field, data);
				if (currentValue==null) {
					currentValue = value;
				} else if (!(currentValue+"").equals(value+"") || ((currentValue+"").equals(value+"") && i == dataList.size() - 1)) {
					int lastRow;
					int firstRow=index+rowNum;
					if(((currentValue+"").equals(value+"") && i == dataList.size() - 1)) {
						lastRow=i + rowNum;
					}else {
						lastRow=i - 1 + rowNum;
						index = i;
					}


					sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, indexColumn, indexColumn));
					currentValue = value;
				}
			}
		}
	}

	/**
	 * 	通过反射获取对象中对应属性的值
	 *
	 * @Title: getValue
	 * @Description: TODO
	 * @param field  字段名称
	 * @param data   需要反射的对象
	 * @return Object
	 */
	private Object getValue(String field, Object data) {
		Object value = "";
		if (data instanceof Map) {
			Map map = (Map) data;
			Set keygen = map.keySet();
			for (Object key : keygen) {
				if (field == key || key.equals(field)) {
					value = map.get(key);
					break;
				}
			}
		} else {
			Character first = field.charAt(0);
			String getMethod = "get" + String.valueOf(first).toUpperCase() + field.substring(1);
			try {

				Method method = ReflectionUtils.findMethod(data.getClass(),getMethod);
				if(method==null){
					throw new RuntimeException(field+" 没有对应的get方法");
				}
				value = method.invoke(data);
			}  catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return value;
	}

	/**
	 * 	将单个对象添加为list列表
	 * @Title: changeList
	 * @return List
	 */
	private List changeList() {
		List<Map<?, ?>> data = new LinkedList<>();
		if (object instanceof Map) {
			Map map = (Map) object;
			data.add(map);
		} else if (object instanceof List) {
			List list = (List) object;
			data.addAll(list);
		}
		return data;
	}

	/**
	 * 添加数据进入单元格
	 */
	private void settingData() {
		List data = changeList();
		Map<Integer,List<Integer>> headerNum = new LinkedHashMap<>();
		for (int i = 0; i < data.size(); i++) {
			Object obj = data.get(i);
			row = sheet.createRow(rowNum + i);
			if (rowHeight != null) {
				row.setHeight(rowHeight.shortValue());
			} else {
				row.setHeight((short) DEFAULT_ROW_HEIGHT);
			}
			int length = header.length;
			if (needOrderNum)
				length += 1;
			int max = 0;
			for (int j = 0; j < length; j++) {
				cell = row.createCell(j);
				cell.setCellStyle(cellStyle);
				if (needOrderNum && j == 0) {
					cell.setCellValue(i + 1);
				} else {
					ExcelModel excelModel;
					if (needOrderNum) {
						excelModel = columnMappingInfo.get(j-max - 1);
					} else {
						excelModel = columnMappingInfo.get(j-max);
					}

					String field = excelModel.getFieldName();
					boolean needtranslate = excelModel.isNeedtranslate();
					boolean needHandle;
					needHandle = ((ExcelTranslateHandler)excelModel).needHandle();
					boolean picture = excelModel.isPicture();
					Map<Object, Object> translate = excelModel.getTranslateMappingInfo();
					boolean listBox = excelModel.isListBox();
					if(listBox){
						setCellListBox(j, i + rowNum, j, i + rowNum,excelModel.getStrFormula());
					}else if (!needtranslate && !needHandle && !picture) {
						setCellValue(cell, field, obj, null, true);
					}else if(needHandle){
						int finalI = i;
						int finalJ = j;
						ExcelRowData<?> excelRowData = new ExcelRowData() {
							@Override
							public Row getRow() {
								return row;
							}

							@Override
							public Cell getCell() {
								return cell;
							}

							@Override
							public Object getRowData() {
								return obj;
							}

							@Override
							public Object getRowData(Class aClass) {
								return null;
							}

							@Override
							public Object currentValue() {
								return getValue(field,obj);
							}

							@Override
							public int currentCellNum() {
								return finalJ;
							}

							@Override
							public int currentRowNum() {
								return rowNum + finalI;
							}

							@Override
							public Object getOriginalCellData(Class aClass) {
								return null;
							}

							@Override
							public Map<String, Object> getOriginalCellData() {
								return null;
							}
						};
						Object handler = ((ExcelTranslateHandler) excelModel).handler(excelRowData);
						setCellValue(cell, handler);
					} else if (needtranslate) {
						setCellValue(cell, field, obj, translate, true);
					} else {
						int realIndex = j-max;
						if(needOrderNum){
							realIndex--;
						}
						Object value = setCellValue(cell, field, obj, null, false);
						max = setPicture(j, i + rowNum, j, i + rowNum, (String) value,cell);
						Integer columnMaxNum = columnMaxMapping.get(realIndex);
						if(columnMaxNum !=null){
							max = columnMaxNum;
						}
						j+=max;
					}
				}
			}
		}
	}


	private void setCellListBox(int firstRow,int lastRow, int firstCol, int lastCol,String strFormula) {

		// 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
		CellRangeAddressList regions = new CellRangeAddressList(firstRow,lastRow, firstCol, lastCol);
		// 数据有效性对象
		DataValidationHelper help = createDataValidationHelper();
		listBoxValidate = createDataValidationConstraint(strFormula);
		DataValidation validation = help.createValidation(listBoxValidate, regions);
		sheet.addValidationData(validation);
	}

	public DataValidationConstraint createDataValidationConstraint(String strFormula){
		return 	new XSSFDataValidationConstraint(DataValidationConstraint.ValidationType.LIST,strFormula);
	}


	private DataValidationHelper createDataValidationHelper() {
		if(currentExcelType.equals(XLSX)){
			if(isBigData){
				return ((SXSSFSheet) sheet).getDataValidationHelper();
			}else{
				return new XSSFDataValidationHelper((XSSFSheet) sheet);
			}
		}
		return new HSSFDataValidationHelper((HSSFSheet) sheet);
	}

//	/**
//	 * 新的头信息处理针对有多张图片的情况可能需要表头增加相关列数
//	 * @param headerNum 列号对应的数量
//	 */
//	private  void  handleNewHeader(Map<Integer,Integer> columnMaxMapping){
//		List<String> newHeader = new LinkedList<>();
//		for(int i = 0 ; i < header.length ; i ++){
//			if(columnMaxMapping.containsKey(i)){
//				List<Integer> value = headerNum.get(key);
//				value = value.stream().sorted().collect(Collectors.toList());
//				Integer num = value.get(value.size() - 1);
//				if(num == 0 ){
//					newHeader.add(header[needOrderNum ? key-1 : key]);
//				}else{
//					int n = num+1;
//					while( n > 0 ){
//						newHeader.add(header[i]);
//						n --;
//					}
//					if(columnMergeInfo.containsKey(i)){
//						String column = columnMergeInfo.get(i);
//						columnMergeInfo.remove(i);
//						columnMergeInfo.put(i+num,column);
//					}
//				}
//			}else{
//				newHeader.add(header[i]);
//			}
//		}
//
//		if(newHeader.size() > header.length){
//			header = newHeader.toArray(new String[0]);
//			rowNum = 0 ;
//			settingHeaderAndTitle(true);
//		}
//	}

	/**
	 * 	向单元格中设置图片对象
	 *
	 * @Title: setPicture
	 * @Description: TODO
	 * @param startColumn 开始列
	 * @param startRow    开始行
	 * @param endColumn   结束列
	 * @param endRow	  结束行
	 * @param imageUrl    图片的url路径
	 * @return int 返回图片增加的单元格数量
	 */
	private int setPicture(int startColumn, int startRow, int endColumn, int endRow, String imageUrl,Cell cell) {
		if (imageUrl == null || imageUrl.trim().length() == 0) {
			return 0;
		}
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		BufferedImage bufferImg;
		String [] imageArray = imageUrl.split(imagesSeparator);
		int count = 0 ;
		for (String s : imageArray) {

			ClientAnchor anchor;
			if (currentExcelType.equals(XLSX)) {
				anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) startColumn, startRow, (short) endColumn + 1,
						endRow + 1);
			} else {
				anchor = new HSSFClientAnchor(0, 0, 0, 0, (short) startColumn, startRow, (short) (endColumn + 1),
						endRow + 1);
			}
			if (pictureType != MOVE_AND_RESIZE && pictureType != DONT_MOVE_AND_RESIZE && pictureType != MOVE_DONT_RESIZE) {
				anchor.setAnchorType(MOVE_AND_RESIZE);
			} else {
				anchor.setAnchorType(pictureType);
			}
			int i;
			if(pictureDirCreate && hasPicture){
				i = imageNum.getAndIncrement();
			}else{
				try {
					byteArrayOut.reset();
					if (s.startsWith("http")) {
						URL url = new URL(s);
						URLConnection urlConnection = url.openConnection();
						urlConnection.setConnectTimeout(imageReadTimeOut);
						bufferImg = ImageIO.read(ImageIO.createImageInputStream(urlConnection.getInputStream()));
					} else {
						bufferImg = ImageIO.read(new File(s));
					}
					if (bufferImg == null) {
						continue;
					}
					ImageIO.write(bufferImg, "jpg", byteArrayOut);
				} catch (Exception e) {
					e.printStackTrace();
					cell.setCellValue("图片下载失败");
					continue;
				}
				i = book.addPicture(byteArrayOut.toByteArray(), Workbook.PICTURE_TYPE_JPEG);
			}
			drawing.createPicture(anchor, i);
			count++;
			startColumn += 1;
			endColumn += 1;

		}
		return count == 0 ? count : count-1;
	}


	/**
	 * 单元格值转换
	 * @param cell 单元格对象
	 * @param field 字段名称
	 * @param data 数据
	 * @param translate    转换的Map
	 * @param needSetValue  是否需要设置值进入cell对象
	 */
	private Object setCellValue(Cell cell, String field, Object data, Map<Object, Object> translate,
								boolean needSetValue) {
		Object value = getValue(field, data);
		if (value == null)
			return "";
		if (translate != null && translate.size() > 0) {
			Set keygen = translate.keySet();
			for (Object key : keygen) {
				if (value == key || key.toString().equals(value.toString())) {
					value = translate.get(key);
					break;
				}
			}
		}
		if (needSetValue) {
			changeType(cell, value);
		}
		return value;
	}

	/**
	 * 单元格值转换
	 * @param cell 单元格对象
	 * @param value 需要转换的值
	 */
	private void setCellValue(Cell cell, Object value) {
		changeType(cell, value);
	}

	public void changeType(Cell cell, Object value) {
		if (value == null) {
			cell.setCellValue("");
			return;
		}
		cell.setCellValue(value.toString());

	}

	/**
	 * 添加表头以及标题信息
	 * @param needHandle 是否需要预先处理表头表头信息
	 */
	private void settingHeaderAndTitle(boolean needHandle) {
		// 添加标题信息
		if (title != null && title.trim().length() > 0) {
			row = sheet.createRow(rowNum);
			row.setHeight((short) titleRowHeight);
			cell = row.createCell(0);
			cell.setCellStyle(titleCellStyle);
			cell.setCellValue(title);
			if(needHandle){
				if (header != null && header.length > 0) {
					int headerLength = header.length;
					if (needOrderNum) {
						headerLength += 1;
					}
					for(int i=0;i<headerLength;i++){
						cell = row.createCell(i);
						cell.setCellStyle(titleCellStyle);
						cell.setCellValue(title);
					}
					tileCellRangeAddress = new CellRangeAddress(0, 0, 0, headerLength -1);
				} else if (columnMappingInfo != null && columnMappingInfo.size() > 0) {
					int size = columnMappingInfo.size();
					if (!needOrderNum) {
						size -= 1;
					}
					tileCellRangeAddress = new CellRangeAddress(0, 0, 0, size);
				}
			}
			rowNum++;

		}
		if(diyRowContext.size() > 0){
			diyRowContextCellRangeAddress.clear();
			int dsize = 0;
			for(String context : diyRowContext){
				if(needHandle){
					row = sheet.createRow(rowNum);
					row.setHeight((short) titleRowHeight);

					if (header != null && header.length > 0) {
						int headerLength = header.length;
						if (needOrderNum) {
							headerLength += 1;
						}
						setText(dsize, context, headerLength-1);
					} else if (columnMappingInfo != null && columnMappingInfo.size() > 0) {
						int size = columnMappingInfo.size();
						if (!needOrderNum) {
							size -= 1;
						}
						setText(dsize, context, size);
					}

				}
				rowNum++;
				dsize++;
			}

		}
		// 添加表头
		if (header != null && header.length > 0) {
			if(needHandle){
				row = sheet.createRow(rowNum);
				row.setHeight((short) headerRowHeight);
				int length = header.length;
				if (needOrderNum) {
					length += 1;
				}
				String headerName=null;
				int startColumn = 0 ;
				int endColumn = 0;
				for (int i = 0; i < length; i++) {
					cell = row.createCell(i);
					cell.setCellStyle(cellStyle);
					if (!isSettingColumnWidth)
						sheet.setColumnWidth(i, DEFAULT_COLUMN_WIDTH * 255);
					if (needOrderNum && i == 0) {
						cell.setCellValue("序号");
					} else if (needOrderNum) {
						if(headerName == null ){
							headerName = header[i - 1];
							startColumn = endColumn = i;
						}else if(headerName.equals(header[i - 1])){
							endColumn++;
							if(i == length -1){
								setMergeColumn(rowNum,rowNum,startColumn,endColumn);
							}
						}else if(!Objects.equals(headerName,header[i-1])){
							if(startColumn!=endColumn){
								setMergeColumn(rowNum,rowNum,startColumn,endColumn);
							}
							startColumn = endColumn = i;
							headerName = header[i-1];
						}
						cell.setCellValue(header[i - 1]);
					} else {
						if(headerName == null ){
							headerName = header[i];
							startColumn = endColumn = i;
						}else if(headerName.equals(header[i])){
							endColumn++;
						}else if(!Objects.equals(headerName,header[i])){
							if(startColumn!=endColumn){
								setMergeColumn(rowNum,rowNum,startColumn,endColumn);
							}
							startColumn = endColumn = i;
							headerName = header[i];
						}
						cell.setCellValue(header[i]);
					}
				}
			}
			rowNum++;
		}

	}

	private void setText(int dsize, String context, int size) {
		for(int i=0;i<size;i++){
			cell = row.createCell(i);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(context);
		}

		if(diyRowContextPropertyInfo.get(dsize)){
			CellRangeAddress cellRangeAddress = new CellRangeAddress(rowNum, rowNum, 0, size);
			diyRowContextCellRangeAddress.add(cellRangeAddress);
		}
	}

	/**
	 * 	返回工作簿对象
	 *
	 * @return  Workbook
	 */
	public Workbook getWorkBook() {
		return book;
	}

	/**
	 * 通过response对象导出
	 * @param response  httpServletResponse对象
	 * @param exportFileName 导出的文件名称
	 */
	public void exportByWResponse(HttpServletResponse response, String exportFileName) {
		try {
			logger.debug("下载成功");
			response.setContentType("application/octet-stream");
			if (currentExcelType!=null && !exportFileName.endsWith(currentExcelType)) {
				exportFileName += "." + currentExcelType;
			}
			String outFileName = URLEncoder.encode(exportFileName, "UTF-8");
			response.setHeader("Content-Disposition", "attachment;fileName=" + outFileName);
			if(pictureDirCreate && hasPicture){
				FileInputStream fileInputStream = new FileInputStream(tempWorkFile);
				IOUtils.copy(fileInputStream,response.getOutputStream());
				fileInputStream.close();
			}else{
				assert book != null;
				book.write(response.getOutputStream());
			}
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			tempWorkFile.delete();
		}
	}

	/**
	 * 导出excel文件到本地
	 * @param filepath 本地路径
	 */
	public void exportLocal(String filepath) {
		try {
			if (!filepath.endsWith(currentExcelType)) {
				filepath += "." + currentExcelType;
			}
			File file = new File(filepath);
			if(!file.exists()){
				file.createNewFile();
			}
			if(pictureDirCreate && hasPicture){
				IOUtils.copy(new FileInputStream(tempWorkFile),new FileOutputStream(file));
			}else{
				book.write(new FileOutputStream(filepath));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			tempWorkFile.delete();
		}

	}

	/**
	 * 检查excel类型是否正确
	 * @param excelType xlsx xls
	 */
	private void checkExcelType(String excelType) {
		if (excelType == null || excelType.trim().length() == 0)
			throw new IllegalArgumentException("excel类型不能为空");

		if (!XLSX.equals(excelType) && !XLX.equals(excelType))
			throw new IllegalArgumentException("excel类型只能为xlsx、xls");
	}

	/**
	 * 创建一个工作簿对象
	 * @param excelType  excel的类型，有两种 XLSX或者 XLS
	 * @param bigData	 设置是否需要使用SAXXFWorkbook来处理大数据量的数据，以防内存溢出（牺牲磁盘空间，会在用户目录下创建一个临时文件保存之前的excel信息）
	 */
	public void createWorkBook(String excelType,boolean bigData) {
		if(bigData){
			book = getSXSSFWorkbook();
			if (!XLSX.equals(excelType))
				excelType = XLSX;
		}else{
			if (XLSX.equals(excelType))
				book = getXSSFWorkBook();
			else
				book = getHSSFWorkBook();
		}

		if (sheetName != null && sheetName.trim().length() > 0)
			sheet = book.createSheet(sheetName);
		else
			sheet = book.createSheet();

		drawing = sheet.createDrawingPatriarch();
		currentExcelType = excelType;
		// 默认样式设置
		defaultCellStyle();

		imageReadTimeOut = DEFAULT_IMAGE_READ_TIME_OUT;
		hiddenSheetListBox = book.createSheet("下拉常量数据");
		book.setSheetHidden(book.getSheetIndex(hiddenSheetListBox),1);

	}

	private void checkListBox() {
		int initNum = 0;
		if(needOrderNum)
			initNum = 1;
		int finalInitNum = initNum;
		columnMappingInfo.forEach( (index, model) ->{
			if(model.isListBox()){
				int start = currentListNum;
				Set<String> listTextBox = model.getListTextBox();
				paddingSheetListBox(listTextBox);
				String format = String.format("下拉常量数据!$A$%s:$A$%s", start, currentListNum-1);
				model.setStrFormula(format);
				int realIndex = index + finalInitNum;
				setCellListBox(0,1000,realIndex,realIndex,model.getStrFormula());
				currentListNum++;
			}
		});
	}

	private void paddingSheetListBox(Set<String> listTextBox) {
		Iterator<String> iterator = listTextBox.iterator();
		int end = currentListNum + listTextBox.size();
		for(;currentListNum < end ; currentListNum++ ){
			Row row = hiddenSheetListBox.createRow(currentListNum - 1);
			Cell cell = row.createCell(0);
			cell.setCellValue(iterator.next());
		}
	}

	/**
	 * 	设置默认样式
	 */
	private void defaultCellStyle() {
		// 默认的值单元格的样式
		cellStyle = book.createCellStyle();
		Font font = book.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 12);
		cellStyle.setFont(font);
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		cellStyle.setWrapText(true);
		cellStyle.setBorderBottom((short) 1);
		cellStyle.setBorderLeft((short) 1);
		cellStyle.setBorderTop((short) 1);
		cellStyle.setBorderRight((short) 1);
		CellStyle headerCellStyle = book.createCellStyle();
		headerCellStyle.cloneStyleFrom(cellStyle);
		font = book.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 14);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerCellStyle.setFont(font);
		// 标题样式
		titleCellStyle = book.createCellStyle();
		titleCellStyle.cloneStyleFrom(cellStyle);
		font = book.createFont();
		font.setFontName("宋体");
		font.setFontHeightInPoints((short) 16);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		titleCellStyle.setFont(font);
	}

	/**
	 * 自定义合并单元格操作
	 * @param firstRow	起始行
	 * @param lastRow   结束行
	 * @param firstCol	起始列
	 * @param lastCol  结束列
	 */
	public  void setMergeColumn(int firstRow,int lastRow,int firstCol,int lastCol){
		sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
	}

	/**
	 * 获取是否需要增加序号一列
	 * @return true or  false
	 */
	public boolean isNeedOrderNum() {
		return needOrderNum;
	}

	/**
	 *	 单元格中是否需要设置序号这一列，默认不设置，如果设置那么第一列就是序号列
	 *
	 * @Title: setNeedOrderNum
	 * @Description: TODO
	 * @param needOrderNum 是否需要自动增加序号一列
	 * @return void
	 */
	public void setNeedOrderNum(boolean needOrderNum) {
		this.needOrderNum = needOrderNum;
	}

	/**
	 * 获取插入的数据
	 * @return Object： Map or List or List<Customer>
	 */
	public Object getObject() {
		return object;
	}

	/**
	 *	 设置需要插入的数据
	 *
	 * @Title: setObject
	 * @Description: TODO
	 * @param object excel需要插入的信息可以Map可以是List也可以是自定义的对象
	 * @return void
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	/**
	 * 获取表格表头信息
	 * @return String[] 表头数组对象
	 */
	public String[] getHeader() {
		return header;
	}

	/**
	 *	 设置表格表头，单元格第二行
	 *
	 * @Title: setHeader
	 * @Description: TODO
	 * @param header 表头数组
	 * @return void
	 */
	public void setHeader(String[] header) {
		this.header = header;
	}

	/**
	 * 获取标题名称
	 * @return String
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * 	设置标题名称，单元格第一行
	 *
	 * @Title: setTitle
	 * @Description: TODO
	 * @param title 标题信息
	 * @return void
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * 获取sheet页的名称
	 * @return String
	 */
	public String getSheetName() {
		return sheetName;
	}

	/**
	 * 设置sheet页的名称
	 * @param sheetName sheet页的名称
	 */
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	/**
	 * 获取列与对象字段的映射关系
	 * @return Map<Integer, ExcelModel>
	 */
	public Map<Integer, ExcelModel> getColumnMappingInfo() {
		return columnMappingInfo;
	}

	/**
	 * 	设置列与对象字段的映射关系
	 *
	 * @Title: setColumnMappingInfo
	 * @Description: TODO
	 * @param columnMappingInfo 列号以及字段的属性对象ExcelModel
	 * @return void
	 */
	public void setColumnMappingInfo(Map<Integer, ExcelModel> columnMappingInfo) {
		this.columnMappingInfo = columnMappingInfo;
	}

	/**
	 * 获取列合并信息
	 * @return Map<Integer, String>
	 */
	public Map<Integer, String> getColumnMergeInfo() {
		return columnMergeInfo;
	}

	/**
	 *	 设置列合并信息
	 *
	 * @Title: setColumnMergeInfo
	 * @Description: TODO
	 * @param columnMergeInfo 合并信息 需要合并的列以及已什么字段进行合并
	 * @return void
	 */
	public void setColumnMergeInfo(Map<Integer, String> columnMergeInfo) {
		this.columnMergeInfo = columnMergeInfo;
	}

	/**
	 * 获取子sheet的对象列表
	 * @return LinkedList<ExcelCreator>
	 */
	public LinkedList<ExcelCreator> getChild() {
		return child;
	}

	/**
	 * 	如果有多个shee页则添加子ExcelCreator对象即可
	 *
	 * @Title: setChild
	 * @Description: TODO
	 * @param child 子ExcelCreator对象
	 * @return void
	 */
	public void setChild(LinkedList<ExcelCreator> child) {
		this.child = child;
	}

	/**
	 * 获取03版的WorkBook对象 也就是以xls结尾的excel
	 * @return HSSFWorkbook
	 */
	private HSSFWorkbook getHSSFWorkBook() {
		return new HSSFWorkbook();
	}

	/**
	 * 获取 07版的WorkBook对象 ，也就是后缀为xlsx的excel
	 * @return XSSFWorkbook
	 */
	private XSSFWorkbook getXSSFWorkBook() {
		return new BusinessXSSFWorkbook();
	}

	/**
	 * 获取大数据类型的WorkBook 放置过大excel数据导致内存溢出
	 * @return SXSSFWorkbook
	 */
	private SXSSFWorkbook getSXSSFWorkbook(){
		isBigData = true;
		return new BusinessSXSSFWorkbook(new BusinessXSSFWorkbook());
	}

	/**
	 * 	生成Excel字段属性信息
	 *
	 * @Title: generate
	 * @Description: TODO
	 * @param fieldName            字段名称
	 * @param needtranslate        是否需要翻译成自定义的值
	 * @param translateMappingInfo 自定义翻译信息
	 * @param isPicture            此列是否是图片列
	 * @return ExcelModel
	 */
	public static ExcelModel generate(String fieldName, boolean needtranslate, Map<Object, Object> translateMappingInfo,
									  boolean isPicture) {
		return new ExcelModel(fieldName, needtranslate, translateMappingInfo, isPicture);
	}

	/**
	 * 生成ExcelModel对象
	 * @param fieldName 字段名称
	 * @return ExcelModel
	 */
	public static ExcelModel generate(String fieldName) {
		return new ExcelModel(fieldName, false, null, false);
	}

	/**
	 * 生成ExcelModel对象
	 * @param fieldName	字段名称
	 * @param translateMappingInfo	转换map
	 * @return	ExcelModel
	 */
	public static ExcelModel generate(String fieldName, Map<Object, Object> translateMappingInfo) {
		return new ExcelModel(fieldName, true, translateMappingInfo, false);
	}

	/**
	 * 生成ExcelModel对象
	 * @param fieldName      字段名称
	 * @param imageVisitPrev	图片访问前缀
	 * @param imageDownPath		图片下载地址
	 * @return	ExcelModel
	 */
	public static ExcelModel generate(String fieldName, String imageVisitPrev, String imageDownPath) {
		return new ExcelModel(fieldName,  imageVisitPrev,  imageDownPath);
	}

	/**
	 * 导出excel
	 * @param clazz 需要导出的Excel模型对象
	 * @param data	需要导出的数据
	 * @param response	response对象
	 * @param excelName	导出名称
	 */
	public static ExcelCreator build(Class clazz,List data,HttpServletResponse response,String excelName){
		return build(clazz,data,response,excelName,null);
	}

	/**
	 * 导出excel
	 * @param clazz	需要导出的Excel模型对象
	 * @param data	需要导出的数据
	 * @param response	response对象
	 * @param excelName	导出名称
	 * @param tile	标题
	 */
	public static ExcelCreator build(Class clazz,List data,HttpServletResponse response,String excelName,String tile){
		Object excelInfo = validateExcelInfoAndInputData(clazz, data, tile);
		return new ExcelCreator(excelInfo);
	}

	/**
	 * 验证数据的合法性以及设置参数到对应实体中
	 * @param clazz	excel模型对象
	 * @param data	数据
	 */
	private static Object  validateExcelInfoAndInputData(Class clazz,List data,String title){
		ExcelInfo annotation =  clazz.getClass().getAnnotation(ExcelInfo.class);
		if(annotation == null){
			throw new IllegalNoSuchAnnotationException(clazz.getName()+"，没有ExcelInfo注解");
		}
		try {
			Object o = clazz.newInstance();
			Field[] declaredFields = clazz.getDeclaredFields();
			Field titleField = null;
			Field dataField = null;
			for(Field field : declaredFields){
					if(titleField ==null && field.getAnnotation(ExcelTitle.class)!=null){
						titleField = field;
					}else if(dataField == null &&  field.getAnnotation(ExcelData.class)!=null ){
						dataField = field;
					}
			}
			if(titleField!=null && title!=null){
				titleField.setAccessible(true);
				titleField.set(o,title);
			}
			if(dataField!=null && data!=null){
				dataField.setAccessible(true);
				dataField.set(o,data);
			}
			return o;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ReflectionBeanException(e);
		}
	}

	/**
	 * 验证数据的合法性以及设置参数到对应实体中
	 * @param clazz	 excel模型对象
	 */
	private static Object  validateExcelInfoAndInputData(Class clazz){
		return 	validateExcelInfoAndInputData(clazz,null,null);
	}
}