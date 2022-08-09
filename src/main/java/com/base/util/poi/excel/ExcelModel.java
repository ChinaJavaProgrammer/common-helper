package com.base.util.poi.excel;

import com.baomidou.mybatisplus.extension.api.R;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * excel字段属性类
 * @ClassName: ExcelModel
 * @Description: TODO
 * @author dh
 * @date 2020年6月8日
 *
 */
public class ExcelModel implements ExcelTranslateHandler{
	
    /**取值字段的名称*/
    private String fieldName;

    /**是否需要翻译成自定义的值*/
    private boolean needtranslate=false;

	/**
	 * 自定义翻译
	 */
	private Function<ExcelRowData,Object> biFunction=null;

    /**需要翻译的值的映射*/
    private Map<Object,Object> translateMappingInfo = new HashMap<>();

    /**是否是图片*/
    private boolean isPicture=false;
    
    /**是否是时间类型*/
    private boolean isDate=false;
    
    /**需要格式化的时间格式*/
    private String pattern="yyyy-MM-dd HH:mm:ss";
    
    /**图片访问地址前缀，ip以及端口信息*/
    private String imageVisitPrex;
    
    /**上传excel时如果有图片则需要保存，这需要填此属性，进行下载*/
    private String imageDownPath;
    
    /**字段是否能为空，默认不为空*/
    private boolean nullAble=false;

    private boolean needHandle = false;

    /**如果字段转换出错是是否需要记录错误信息*/
    private boolean needAddTranslationException = true;

    private boolean isInteger = false;

    private boolean isFloat = false;

    private boolean isDouble = false;

    private ExcelCustomValidate excelCustomValidate;

	private boolean isListBox = false;

	private Set<String> listTextBox = new HashSet<>();

	private String strFormula;

	public String getStrFormula() {
		return strFormula;
	}

	public ExcelModel setStrFormula(String strFormula) {
		this.strFormula = strFormula;
		return this;
	}


	public ExcelModel setListBox(boolean listBox) {
		isListBox = listBox;
		return this;
	}

	public boolean isListBox() {
		return isListBox;
	}

	public Set<String> getListTextBox() {
		return listTextBox;
	}

	/**
	 * 设置是否需要添加下拉列表
	 * @param listTextBox
	 */
	public ExcelModel setListTextBox(Set<String> listTextBox){
		isListBox = true;
		this.listTextBox = listTextBox;
		return this;
	}


	public ExcelModel setInteger(boolean integer) {
		isInteger = integer;
		return this;
	}

	public ExcelModel setFloat(boolean aFloat) {
		isFloat = aFloat;
		return this;
	}

	public ExcelModel setDouble(boolean aDouble) {
		isDouble = aDouble;
		return this;
	}

	public ExcelModel setExcelCustomValidate(ExcelCustomValidate excelCustomValidate) {
		this.excelCustomValidate = excelCustomValidate;
		return this;
	}

	public ExcelCustomValidate getExcelCustomValidate() {
		return excelCustomValidate;
	}

	/**
	 * 自定义翻译
	 * @param biFunction
	 */
	public  ExcelModel  setBiFunction(Function<ExcelRowData,Object> biFunction){
    	if(biFunction!=null){
			this.biFunction = biFunction;
			needHandle=true;
		}
		return this;
	}

    public ExcelModel(String fieldName, boolean needtranslate, Map<Object, Object> translateMappingInfo, boolean isPicture) {
        this.fieldName = fieldName;
        this.needtranslate = needtranslate;
        this.translateMappingInfo = translateMappingInfo;
        this.isPicture = isPicture;
    }

	@Override
	public Object handler(ExcelRowData rowData) {
		if(needHandle())
			return biFunction.apply(rowData);
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean needHandle() {
		return needHandle;
	}

	public boolean isNullAble() {
		return nullAble;
	}


	public ExcelModel setNullAble(boolean nullAble) {
		this.nullAble = nullAble;
		return this;
	}





	/**
     * 	当字段是普通字段的时候
     * @param fieldName
     */
    public ExcelModel(String fieldName) {
		this(fieldName,false);
	}

	public ExcelModel(String fieldName, boolean nullAble){
		super();
		this.fieldName = fieldName;
		this.nullAble = nullAble;
	}
    
    
    
    
    /**
     * 	当解析的字段需要翻译成指定值的时候
     * @param fieldName	字段名称
     * @param translateMappingInfo	自定义翻译的Map
     */
	public ExcelModel(String fieldName, Map<Object, Object> translateMappingInfo) {
		super();
		this.fieldName = fieldName;
		this.needtranslate=true;
		this.translateMappingInfo = translateMappingInfo;
	}
	
	


	/**
	 * 	当解析字段为图片时
	 * @param fieldName	字段名称
	 * @param imageVisitPrex	图片的访问前缀
	 * @param imageDownPath		图片的下载路径
	 */
	public ExcelModel(String fieldName, String imageVisitPrex, String imageDownPath) {
		super();
		this.fieldName = fieldName;
		this.imageVisitPrex = imageVisitPrex;
		this.imageDownPath = imageDownPath;
		this.isPicture=true;
	}

	
	public ExcelModel(String fieldName, String pattern) {
		super();
		this.fieldName = fieldName;
		this.isDate = true;
		this.pattern=pattern;
	}


	public ExcelModel(String fieldName, boolean needtranslate, Map<Object, Object> translateMappingInfo,
			boolean isPicture, String imageDownPath,String imageVisitPrex) {
		super();
		this.fieldName = fieldName;
		this.needtranslate = needtranslate;
		this.translateMappingInfo = translateMappingInfo;
		this.isPicture = isPicture;
		this.imageDownPath = imageDownPath;
		this.imageVisitPrex=imageVisitPrex;
	}



	public ExcelModel setImageDownPath(String imageDownPath) {
    	this.imageDownPath=imageDownPath;
		return this;
    }

    public String getFieldName() {
        return fieldName;
    }

    public ExcelModel setFieldName(String fieldName) {
        this.fieldName = fieldName;
		return this;
    }

    public boolean isNeedtranslate() {
        return needtranslate;
    }

    public ExcelModel setNeedtranslate(boolean needtranslate) {
        this.needtranslate = needtranslate;
		return this;
    }

    public Map<Object, Object> getTranslateMappingInfo() {
        return translateMappingInfo;
    }

    public ExcelModel setTranslateMappingInfo(Map<Object, Object> translateMappingInfo) {
        this.translateMappingInfo = translateMappingInfo;
		return this;
    }

    public boolean isPicture() {
        return isPicture;
    }

    public ExcelModel setPicture(boolean picture) {
        isPicture = picture;
		return this;
    }



	public String getImageDownPath() {
		return imageDownPath;
	}



	public String getImageVisitPrex() {
		return imageVisitPrex;
	}



	public ExcelModel setImageVisitPrex(String imageVisitPrex) {
		this.imageVisitPrex = imageVisitPrex;
		return this;
	}
    
	public ExcelModel setDate(boolean isDate) {
		this.isDate=isDate;
		return this;
	}
    
	public boolean isDate() {
		return isDate;
	}


	public String getPattern() {
		return pattern;
	}


	public ExcelModel setPattern(String pattern) {
		isDate=true;
		this.pattern = pattern;
		return this;
	}


	public boolean isNeedAddTranslationException() {
		return needAddTranslationException;
	}

	public ExcelModel setNeedAddTranslationException(boolean needAddTranslationException) {
		this.needAddTranslationException = needAddTranslationException;
		return this;
	}
}
