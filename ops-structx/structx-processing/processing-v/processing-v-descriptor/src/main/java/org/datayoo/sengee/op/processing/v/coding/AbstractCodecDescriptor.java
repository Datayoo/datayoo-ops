package org.datayoo.sengee.op.processing.v.coding;

import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.datax.sd.*;
import org.datayoo.datax.util.sd.ColumnSetMetadataUtils;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2022/6/15 12:18 AM
 */
public abstract class AbstractCodecDescriptor
    extends AbstractProcessingDescriptor {

  public static final String PARAM_WORKING_MODE = "workingMode";
  // 覆写
  public static final String WM_OVERWITE = "overwrite";
  // 添加列
  public static final String WM_ADD_COLUMN = "addColumn";

  protected String workingMode;

  public AbstractCodecDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  public AbstractCodecDescriptor(String id, FlowNode parent,
      CompilationContext compilationContext) {
    super(id, parent, compilationContext);
  }

  @Override
  protected void innerInitialize() {

  }

  @Override
  protected void innerDestroy() {

  }

  @Override
  protected void readParameters() {
    super.readParameters();
    workingMode = parameters.getParameterValue(PARAM_WORKING_MODE, WM_OVERWITE);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    FlowPort inputPort = this.inputPorts.iterator().next();
    ColumnSetMetadata columnSetMetadata = this.columnSetMetadataMap.get(
        inputPort.getName());
    String[] columns = new String[0];
    if (this.columns != null && this.columns.size() > 0) {
      columns = this.columns.toArray(columns);
    }
    return createCodecOutputColumnSetMetadata(columnSetMetadata, workingMode,
        columns, getColumnSuffix(), getOutputDataType());
  }

  public static ColumnSetMetadata createCodecOutputColumnSetMetadata(
      ColumnSetMetadata inputColumnSetMetadata, String workingMode,
      String[] columns, String suffix, DataType dataType) {
    if (workingMode.equals(WM_OVERWITE)) {
      ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata(
          inputColumnSetMetadata);
      if (columns != null && columns.length > 0) {
        ColumnSetMetadataUtils.updateColumnTypes(columnSetMetadata, columns,
            DataTypeUtils.STRING_TYPE);
      }
      return columnSetMetadata;
    } else {
      return createCodecOutputColumnSetMetadata(inputColumnSetMetadata, columns,
          suffix,dataType);
    }
  }

  protected static ColumnSetMetadata createCodecOutputColumnSetMetadata(
      ColumnSetMetadata inputColumnSetMetadata, String[] columns,
      String suffix,DataType dataType) {
    ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata(
        inputColumnSetMetadata);
    for (String column : columns) {
      column = String.format("%s_%s", column, suffix);
      ColumnMetadata columnMetadata = new GeneralColumnMetadata(column);
      columnMetadata.setType(dataType);
      columnSetMetadata.addColumn(columnMetadata);
    }
    return columnSetMetadata;
  }

  protected abstract String getColumnSuffix();

  protected DataType getOutputDataType() {
    return DataTypeUtils.STRING_TYPE;
  }

  @Override
  protected void validateParameters() {
    super.validateParameters();
    validateColumns();
  }

  protected void validateColumns() {
    ColumnSetMetadata columnSetMetadata = getInputColumnSetMetadata();
    for (String column : columns) {
      validateCodecColumn(columnSetMetadata.getColumn(column));
    }
  }

  protected void validateCodecColumn(ColumnMetadata columnMetadata) {
    if (columnMetadata.getType() == DataTypeUtils.STRING_TYPE
        || columnMetadata.getType() == DataTypeUtils.BINARY_TYPE) {
      return;
    }
    throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
        OperatorsExceptionConstants.ECM_OPD_COL_INVALID_TYPE, this.alias,
        columnMetadata.getName(), "{String,Binary}"));
  }
}
