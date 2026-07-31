package org.datayoo.sengee.op.processing.flat;

import org.datayoo.base.types.DataTypeName;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.Parameterization;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.datax.flatter.StructureCollectionMetadata;
import org.datayoo.sengee.datax.flatter.StructureFieldMetadata;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public abstract class AbstractFlatterDescriptor
    extends AbstractProcessingDescriptor {
  public static final String OBJECT_ENTITY_NAME = "o";

  public static final String PARAM_DATA_COLUMN = "dataColumn";
  public static final String PARAM_DATA_EXAMPLE = "dataExample";
  public static final String PARAM_STRUCT_MAPPINGS = "structureMappings";
  public static final String PARAM_MAPPING_NAME = "mappingName";
  public static final String PARAM_FILTER_MODE = "filterMode";
  public static final String PARAM_START_POS = "startPos";
  public static final String PARAM_END_POS = "endPos";
  public static final String PARAM_FILTER = "filter";
  public static final String PARAM_STRUCTURE_FIELDS = "structureFields";
  public static final String PARAM_COLUMN_NAME = "columnName";
  public static final String PARAM_DATA_PATH = "dataPath";
  public static final String PARAM_COLUMN_TYPE = "columnType";
  public static final String PARAM_DATA_FORMAT = "dataFormat";
  public static final String PARAM_FORMAT_FUNC = "formatFunc";
  // 数据格式化函数
  protected StructureCollectionMetadata structureCollectionMetadata;

  public AbstractFlatterDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  public static List<ColumnMetadata> trans2ColumnMetadatas(
      StructureCollectionMetadata structureCollectionMetadata) {
    List<ColumnMetadata> columnMetadatas = new LinkedList<>();
    for (StructureFieldMetadata fieldMetadata : structureCollectionMetadata.getAllStructureFields()) {
      ColumnMetadata columnMetadata = new GeneralColumnMetadata(
          fieldMetadata.getFieldName());
      columnMetadata.setType(
          DataTypeUtils.createDataType(fieldMetadata.getFieldType(), null,
              null));
      columnMetadatas.add(columnMetadata);
    }
    return columnMetadatas;
  }

  public static StructureCollectionMetadata loadStructureMappings(
      GroupParameter structureMappings) {
    StructureCollectionMetadata collectionMetadata = new StructureCollectionMetadata();
    collectionMetadata.setFieldName(
        structureMappings.getParameterValue(PARAM_MAPPING_NAME, "o"));
    collectionMetadata.setDataPath(
        structureMappings.getParameterValue(PARAM_DATA_PATH, "o"));
    String filterMode = structureMappings.getParameterValue(PARAM_FILTER_MODE,
        "filter");
    if (filterMode.equals("index")) {
      collectionMetadata.setStart(
          structureMappings.getParameterValueAsInt(PARAM_START_POS, 0));
      collectionMetadata.setEnd(
          structureMappings.getParameterValueAsInt(PARAM_END_POS, -1));
    } else {
      collectionMetadata.setFilter(
          structureMappings.getParameterValue(PARAM_FILTER));
    }
    loadStructureFields(
        (TableParameter) structureMappings.getParameter(PARAM_STRUCTURE_FIELDS),
        collectionMetadata);
    Parameterization childStructureCollectionMetadata = structureMappings.getParameter(
        PARAM_STRUCT_MAPPINGS);
    if (childStructureCollectionMetadata != null) {
      collectionMetadata.setChildCollectionMetadata(loadStructureMappings(
          (GroupParameter) childStructureCollectionMetadata));
    }
    return collectionMetadata;
  }

  protected static void loadStructureFields(TableParameter tableParameter,
      StructureCollectionMetadata collectionMetadata) {
    if (tableParameter == null)
      return;
    for (RowParameter rowParameter : tableParameter.getParameters()) {
      StructureFieldMetadata structureFieldMetadata = new StructureFieldMetadata();
      structureFieldMetadata.setFieldName(
          rowParameter.getParameterValue(PARAM_COLUMN_NAME));
      String value = rowParameter.getParameterValue(PARAM_COLUMN_TYPE);
      structureFieldMetadata.setFieldType(DataTypeName.valueOf(value));
      structureFieldMetadata.setDataPath(
          rowParameter.getParameterValue(PARAM_DATA_PATH));
      structureFieldMetadata.setDataFormat(
          rowParameter.getParameterValue(PARAM_DATA_FORMAT, null));
      structureFieldMetadata.setFormatFunc(
          rowParameter.getParameterValue(PARAM_FORMAT_FUNC, null));
      collectionMetadata.getStructureFields().add(structureFieldMetadata);
    }
  }

  protected void readParameters() {
    structureCollectionMetadata = loadStructureMappings(
        (GroupParameter) parameters.getParameter(PARAM_STRUCT_MAPPINGS));
  }

  @Override
  protected void validateParameters() {
    String dataColumn = parameters.getParameterValue(PARAM_DATA_COLUMN, "");
    if (dataColumn.isEmpty()) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_DATA_COLUMN));
    }
    if (structureCollectionMetadata.getAllStructureFields().size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_STRUCTURE_FIELDS));
    }
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    List<ColumnMetadata> columnMetadatas = trans2ColumnMetadatas(
        structureCollectionMetadata);
    ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata(
        SengeeConstants.FDT_ANY);
    columnSetMetadata.getColumns().addAll(columnMetadatas);
    return columnSetMetadata;
  }
}
