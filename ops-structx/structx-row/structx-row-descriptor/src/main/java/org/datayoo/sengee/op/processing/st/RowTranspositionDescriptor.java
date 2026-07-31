package org.datayoo.sengee.op.processing.st;

import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.HashSet;
import java.util.Set;

/**
 * 行列转换
 *
 * @author hhn
 */
@OpDefiner(name = "RowTransposition",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,structure")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"headerColumn\" c_Option=\"false\" c_Alias=\"列头\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待转换列\" c_Compox=\"sengee-columns-selector\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名\"></parameter>"
            + "</head>" + "</parametertable>"
            + "<parametertable name=\"groupColumnSet\" c_Alias=\"分组设置\" c_Compox=\"sengee-columns-selector\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"true\" c_Alias=\"列名\"></parameter>"
            + "</head>" + "</parametertable>" + "</parametergroup>"
            + "<parametergroup name=\"outputColumnSet\" c_Alias=\"列集\" c_Compox=\"sightx-tab\">"
            + "<parametertable name=\"outputColumnSet\" c_Alias=\"输出列集\">"
            + "<head>"
            + "<parameter name=\"columnName\" c_Alias=\"列名\"></parameter>"
            + "<parameter name=\"columnType\" c_Alias=\"数据类型\" c_Compox=\"sightx-datatype\">String</parameter>"
            + "</head>" + "</parametertable>" + "</parametergroup>"
            + "</parameters>",
    compoxes = {})
public class RowTranspositionDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_HEADER_COLUMN = "headerColumn";
  public static final String PARAM_GROUP_COLUMN_SET = "groupColumnSet";

  public static final String PARAM_OUTPUT_COLUMN_SET = "outputColumnSet";

  public static final String PARAM_COLUMN_TYPE = "columnType";

  public RowTranspositionDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata("/");
    TableParameter outputTableParameter = (TableParameter) this.parameters.getParameter(
        PARAM_OUTPUT_COLUMN_SET);
    TableParameter groupTableParameter = (TableParameter) this.parameters.getParameter(
        PARAM_GROUP_COLUMN_SET);
    for (RowParameter rowParameter : groupTableParameter.getParameters()) {
      ColumnMetadata columnMetadata = new GeneralColumnMetadata(
          rowParameter.getParameterValue(PARAM_COLUMN_NAME));
      columnMetadata.setType(DataTypeUtils.STRING_TYPE);
      columnSetMetadata.addColumn(columnMetadata);
    }
    ColumnMetadata metaColumnMetadata = new GeneralColumnMetadata("META");
    metaColumnMetadata.setType(DataTypeUtils.STRING_TYPE);
    columnSetMetadata.addColumn(metaColumnMetadata);
    for (RowParameter rowParameter : outputTableParameter.getParameters()) {
      String columnName = rowParameter.getParameterValue(PARAM_COLUMN_NAME);
      String columnType = rowParameter.getParameterValue(PARAM_COLUMN_TYPE);
      ColumnMetadata columnMetadata = new GeneralColumnMetadata(columnName);
      columnMetadata.setType(
          DataTypeUtils.createDataTypeByTypeString(columnType));
      columnSetMetadata.addColumn(columnMetadata);
    }
    return columnSetMetadata;
  }

  @Override
  protected void readParameters() {
  }

  @Override
  public void validateParameters() {
    Set<String> columnsSet = new HashSet<>();
    String headColumn = this.parameters.getParameterValue(PARAM_HEADER_COLUMN);
    columnsSet.add(headColumn);
    TableParameter columnSetParameter = getColumnSetParameter();
    if (columnSetParameter.getParameters().size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_COLUMN_SET));
    }
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      if (!columnsSet.add(rowParameter.getParameterValue(PARAM_COLUMN_NAME))) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_RESOURCE_DUPLICATED, this.alias,
            rowParameter.getParameterValue(PARAM_COLUMN_NAME)));
      }
    }
    TableParameter groupTableParameter = (TableParameter) this.parameters.getParameter(
        PARAM_GROUP_COLUMN_SET);
    for (RowParameter rowParameter : groupTableParameter.getParameters()) {
      if (!columnsSet.add(rowParameter.getParameterValue(PARAM_COLUMN_NAME))) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_RESOURCE_DUPLICATED, this.alias,
            rowParameter.getParameterValue(PARAM_COLUMN_NAME)));
      }
    }
    TableParameter outputTableParameter = (TableParameter) this.parameters.getParameter(
        PARAM_OUTPUT_COLUMN_SET);
    if (outputTableParameter.getParameters().size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_OUTPUT_COLUMN_SET));
    }
  }
}
